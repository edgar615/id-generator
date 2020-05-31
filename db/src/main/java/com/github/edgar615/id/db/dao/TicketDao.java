package com.github.edgar615.id.db.dao;

import com.github.edgar615.id.db.TicketSegment;

import java.util.List;

public interface TicketDao {

  TicketSegment updateMaxIdAndGet(String bizType, int shardingKey);

  TicketSegment updateMaxIdByCustomStepAndGet(String bizType, int shardingKey, int step);

  /**
   * 查找所有的ticket，如果数据很大，会造成卡顿
   * @return
   */
//  List<Ticket> findAllTicket();

  void resumeForNow(String bizType, int shardingKey);
}
