package com.github.edgar615.id.db.dao;

import com.github.edgar615.id.db.TicketSegment;
import org.omg.CORBA.SystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * ticket的服务，直接通过X锁限制并发冲突
 */
public class TicketDaoImpl implements TicketDao {

  private final JdbcTemplate jdbcTemplate;

  public TicketDaoImpl(DataSource dataSource) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }

  /**
   * 更新ticket，每个业务类型的分片有且只有一记录，所以查询的时候不适应ticket_date查询 1.查询ticket_date
   * 2.如果ticket_date小于当前时间，更新ticket_date, min_ticket, max_ticket
   *
   * @param bizType 业务类型
   * @param shardingKey 分片
   * @return ticket段
   */
  @Override
  @Transactional
  public TicketSegment updateMaxIdAndGet(String bizType, int shardingKey) {
    return jdbcTemplate.execute((ConnectionCallback<TicketSegment>) connection -> {
      TicketSegment ticketSegment = selectMaxId(connection, bizType, shardingKey);
      if (ticketSegment == null) {
        throw SystemException.create(DefaultErrorCode.UNKOWN)
            .setDetails("create ticket failed:" + bizType + "[" + shardingKey + "]");
      }
      long today = DateUtils.beginOfDay(new Date());
      long currentTicketDay = DateUtils.beginOfDay(ticketSegment.getTicketDate());
      if (currentTicketDay < today) {
        // 重新计算ticket
        resume(connection, bizType, shardingKey, ticketSegment.getStep());
      } else {
        updateMaxId(connection, bizType, shardingKey);
      }
      return selectMaxId(connection, bizType, shardingKey);
    });
  }

  @Override
  @Transactional
  public TicketSegment updateMaxIdByCustomStepAndGet(String bizType, int shardingKey, int step) {
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    return jdbcTemplate.execute((ConnectionCallback<TicketSegment>) connection -> {
      TicketSegment ticketSegment = selectMaxId(connection, bizType, shardingKey);
      if (ticketSegment == null) {
        throw SystemException.create(DefaultErrorCode.UNKOWN)
            .setDetails("create ticket failed:" + bizType + "[" + shardingKey + "]");
      }
      long today = DateUtils.beginOfDay(new Date());
      long currentTicketDay = DateUtils.beginOfDay(ticketSegment.getTicketDate());
      if (currentTicketDay < today) {
        // 重新计算ticket
        resume(connection, bizType, shardingKey, ticketSegment.getStep());
      } else {
        updateCustomStep(connection, bizType, shardingKey, step);
      }
      return selectMaxId(connection, bizType, shardingKey);
    });
  }

  @Override
  public List<Ticket> findAllTicket() {
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    return jdbcTemplate
        .query("select * from ticket", BeanPropertyRowMapper.newInstance(Ticket.class));
  }

  @Override
  public void resumeForNow(String bizType, int shardingKey) {
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    jdbcTemplate.execute((ConnectionCallback<Void>) connection -> {
      TicketSegment ticketSegment = selectMaxId(connection, bizType, shardingKey);
      if (ticketSegment == null) {
        throw SystemException.create(DefaultErrorCode.UNKOWN)
            .setDetails("create ticket failed:" + bizType + "[" + shardingKey + "]");
      }
      long today = DateUtils.beginOfDay(new Date());
      long currentTicketDay = DateUtils.beginOfDay(ticketSegment.getTicketDate());
      if (currentTicketDay < today) {
        // 重新计算ticket
        resume(connection, bizType, shardingKey, ticketSegment.getStep());
      }
      return null;
    });
  }

  private void updateMaxId(Connection connection, String bizType, int shardingKey)
      throws SQLException {
    PreparedStatement preparedStatement = connection.prepareStatement(
        "update ticket set min_ticket = max_ticket, max_ticket = max_ticket + step where biz_type = ? and sharding_key = ?");
    try {
      preparedStatement.setString(1, bizType);
      preparedStatement.setInt(2, shardingKey);
      preparedStatement.executeUpdate();
    } finally {
      preparedStatement.close();
    }
  }

  private void updateCustomStep(Connection connection, String bizType, int shardingKey, int step)
      throws SQLException {
    PreparedStatement preparedStatement = connection.prepareStatement(
        "update ticket set min_ticket = max_ticket, max_ticket = max_ticket + ? where biz_type = ? and sharding_key = ?");
    try {
      preparedStatement.setInt(1, step);
      preparedStatement.setString(2, bizType);
      preparedStatement.setInt(3, shardingKey);
      preparedStatement.executeUpdate();
    } finally {
      preparedStatement.close();
    }
  }

  private void resume(Connection connection, String bizType, int shardingKey, int step)
      throws SQLException {
    String ticketDay = DateUtils.unixToDate(Instant.now().getEpochSecond());
    Integer randNum = Integer.parseInt(Randoms.randomNumber(3));
    PreparedStatement preparedStatement = connection.prepareStatement(
        "update ticket set min_ticket = ?, max_ticket = ?, ticket_date = ?, rand_num = ? where biz_type = ? and sharding_key = ?");
    try {
      preparedStatement.setInt(1, 0);
      preparedStatement.setInt(2, step);
      preparedStatement.setString(3, ticketDay);
      preparedStatement.setInt(4, randNum);
      preparedStatement.setString(5, bizType);
      preparedStatement.setInt(6, shardingKey);
      preparedStatement.executeUpdate();
    } finally {
      preparedStatement.close();
    }
  }

  private TicketSegment selectMaxId(Connection connection, String bizType, int shardingKey)
      throws SQLException {
    PreparedStatement preparedStatement = connection.prepareStatement(
        "select min_ticket, max_ticket, ticket_date, step, rand_num from ticket where biz_type = ? and sharding_key = ? for update");
    preparedStatement.setString(1, bizType);
    preparedStatement.setInt(2, shardingKey);
    ResultSet resultSet = preparedStatement.executeQuery();
    try {
      if (resultSet.next()) {
        int minTicket = resultSet.getInt("min_ticket");
        int maxTicket = resultSet.getInt("max_ticket");
        int step = resultSet.getInt("step");
        String ticketDate = resultSet.getString("ticket_date");
        int randNum = resultSet.getInt("rand_num");
        return TicketSegment.create(ticketDate, minTicket, maxTicket, step, randNum);
      } else {
        return null;
      }
    } finally {
      preparedStatement.close();
    }

  }
}
