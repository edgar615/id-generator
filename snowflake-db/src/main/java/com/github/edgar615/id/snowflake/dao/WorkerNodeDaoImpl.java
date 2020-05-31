package com.github.edgar615.id.snowflake.dao;

import com.github.edgar615.id.snowflake.WorkerNode;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

public class WorkerNodeDaoImpl implements WorkerNodeDao {

    private static final String QUERY_SQL = "select * from WORKER_NODE where HOST_NAME = ?";
    private static final String UPDATE_SQL = "UPDATE WORKER_NODE SET HOST_NAME = ?, TYPE = ?, STATE = ?, LAUNCH_TIME = ?, MODIFIED_TIME = ? WHERE WORKER_NODE_ID = ?";

    private final JdbcTemplate jdbcTemplate;

    public WorkerNodeDaoImpl(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public int addWorkerNode(WorkerNode workerNode) {
        return jdbcTemplate.update(UPDATE_SQL, workerNode.getNodeHost(), workerNode.getType(),
                workerNode.getState(), workerNode.getLaunchTime(), workerNode.getModifidTime());
    }

    @Override
    public WorkerNode queryWorkerNode(String host) {
        List<WorkerNode> workerNodes = jdbcTemplate.query(QUERY_SQL, new Object[]{host}, BeanPropertyRowMapper.newInstance(WorkerNode.class));
        if (workerNodes.isEmpty()) {
            return null;
        }
        return workerNodes.get(0);
    }
}
