/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.edgar615.id;

import com.github.edgar615.util.net.NetUtils;
import java.math.BigInteger;

/**
 * Snowflake 的变种 Boundary Flake的简单实现. ID由128bit组成
 * <p>
 * 第一段:64bit 时间标记 记录的是当前时间与元年的时间差 第二段:48bit mac地址 第三段:16bit 单毫秒内自增序列
 * </p>
 *
 * @author Edgar  Date 2016/6/21
 */
public class BoundaryFlakeIdFactory implements IdFactory<String>, TimeExtracter<String>,
    ShardingExtracter<String>, SeqExtracter<String> {

  /**
   * 自增序列的位数
   */
  private static final int SEQ_BIT = 16;

  /**
   * 节点标识的位数
   */
  private static final int SERVER_BIT = 48;

  /**
   * 最大序列号
   */
  private static final int SEQ_MASK = -1 ^ (-1 << SEQ_BIT);

  /**
   * 时间的左移位数
   */
  private static final int TIME_LEFT_BIT = SEQ_BIT + SERVER_BIT;

  /**
   * SERVER的左移位数
   */
  private static final int SERVER_LEFT_BIT = SEQ_BIT;

  /**
   * 每次初始化对序列值.
   */
  private static final int INIT_SEQ = 0;

  /**
   * 将mac地址作为节点ID
   */
  private static final long SERVER_ID = Long.parseLong(NetUtils.getMac(), 16);

  /**
   * 单例对象
   */
  private static final BoundaryFlakeIdFactory INSTANCE = new BoundaryFlakeIdFactory();

  /**
   * 上次时间戳
   */
  private volatile long lastTime = -1L;

  /**
   * 自增序列
   */
  private volatile long seqId = INIT_SEQ;

  private BoundaryFlakeIdFactory() {
  }

  @Override
  public synchronized String nextId() {
    long time = currentTime();
    if (time < lastTime) {
      //当前时间小于上次时间，说明时钟不对
      throw new IllegalStateException("Clock moved backwards.");
    }
    if (time == lastTime) {
      seqId = (seqId + 1) & SEQ_MASK;
      if (seqId == 0) {
        //说明该毫秒下对序列已经自增完毕，等待下一个毫秒
        tilNextMillis(lastTime);
      }
    } else {
      seqId = INIT_SEQ;
    }
    lastTime = time;
    BigInteger id = new BigInteger(time + "").shiftLeft(TIME_LEFT_BIT);
    id = id.or(new BigInteger(SERVER_ID + "").shiftLeft(SERVER_LEFT_BIT));
    id = id.or(new BigInteger(seqId + ""));
    return id.toString();
  }

  /**
   * 返回BoundaryFlakeIdFactory的单例对象
   *
   * @return IdFactory
   */
  static BoundaryFlakeIdFactory instance() {
    return INSTANCE;
  }

  @Override
  public long fetchSeq(String id) {
    BigInteger newId = new BigInteger(id);
    BigInteger id1 = newId.xor(newId.shiftRight(TIME_LEFT_BIT).shiftLeft(TIME_LEFT_BIT));
    BigInteger id2 = newId.xor(newId.shiftRight(TIME_LEFT_BIT).shiftLeft(TIME_LEFT_BIT))
        .shiftRight(SERVER_LEFT_BIT).shiftLeft(SERVER_LEFT_BIT);
    return id1.xor(id2).longValue();
  }

  @Override
  public long fetchSharding(String id) {
    BigInteger newId = new BigInteger(id);
    return newId.xor(newId.shiftRight(TIME_LEFT_BIT).shiftLeft(TIME_LEFT_BIT))
        .shiftRight(SERVER_LEFT_BIT).longValue();
  }

  @Override
  public long fetchTime(String id) {
    return new BigInteger(id).shiftRight(TIME_LEFT_BIT).longValue();
  }
}
