package com.github.edgar615.id.db;

public class TicketSegment {

  private final long minTicket;

  private final long maxTicket;

  private final int step;

  private final int randNum;

  private TicketSegment(long minTicket, long maxTicket, int step, int randNum) {
    this.minTicket = minTicket;
    this.maxTicket = maxTicket;
    this.step = step;
    this.randNum = randNum;
  }

  public static TicketSegment create( long minTicket, long maxTicket, int step, int randNum) {
    return new TicketSegment(minTicket, maxTicket, step, randNum);
  }

  public long getMinTicket() {
    return minTicket;
  }

  public long getMaxTicket() {
    return maxTicket;
  }

  public int getStep() {
    return step;
  }

  public int getRandNum() {
    return randNum;
  }
}
