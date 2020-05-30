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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * ID生成类.
 * <p>
 * snowflake是来自于Twitter的一个开源算法 0    00000.....000 00000 00000 000000000000 |    |___________|
 * |_____| |______| |______________| |         |                  |          |            | 1bit
 * 41bit      5bit    5bit     12bit
 * </p>
 * 核心代码就是毫秒级时间41位+机器ID 10位+毫秒内序列12位 第一段:1bit 预留 实际上是作为long的符号位 第二段:41bit 时间标记 记录的是当前时间与元年的时间差
 * 第三段:5bit 数据中心Id标记  记录的是数据中心的Id,5bit最大可以表示32个数据中心,这么多数据中心能保证全球范围内服务可用 第四段:5bit 节点标记
 * 记录的是单个数据中心里面服务节点的Id,同理也是最大可以有32个节点,这个除非是整个数据中心离线,否则可以保证服务永远可用 第五段:12bit 单毫秒内自增序列 每毫秒可以生成4096个ID
 * <p>
 * instagram对他做来一点改造，直接在数据库的实现，通过触发器实现 使用41 bit来存放时间，精确到毫秒. 使用12 bit来存放逻辑分片ID,可以分4096个片。 使用10
 * bit来存放自增长ID，意味着每台机器，每秒最多可以生成1024个ID
 * <p>
 * <p>
 * 该类采用instagram的方案，但是将分片ID作为了serverId，在程序中指定。足够满足一般的项目需求。 在分布式环境下，多个服务需要保证serverId的唯一性，否则各个服务可能会生成重复的主键.
 *
 * @author Edgar
 */
class SnowflakeIdFactory implements IdFactory<Long>, TimeExtracter<Long>, SeqExtracter<Long>,
    ShardingExtracter<Long> {
  private static final Logger LOGGER = LoggerFactory.getLogger(SnowflakeIdFactory.class);
  /**
   * 自增序列的位数
   */
  private static final int SEQ_BIT = 12;

  /**
   * 节点标识的位数
   */
  private static final int SERVER_BIT = 10;

  /**
   * 最大序列号
   */
  private static final int SEQ_MASK = -1 ^ (-1 << SEQ_BIT);

  /**
   * 最大server
   */
  private static final int SERVER_MASK = -1 ^ (-1 << SERVER_BIT);


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
   * 用来保存每个服务节点的ID生成类，保证每个节点只有一个实例.
   */
  private static final ConcurrentMap<Integer, SnowflakeIdFactory> FACTORY_HOLDER =
      new ConcurrentHashMap<>();

  /**
   * 服务器id
   */
  private final int serverId;

  /**
   * 上次时间戳
   */
  private volatile long lastTime = -1L;

  /**
   * 自增序列
   */
  private volatile long seqId = INIT_SEQ;

  private SnowflakeIdFactory(int serverId) {
    this.serverId = serverId & SERVER_MASK;
  }

  @Override
  public synchronized Long nextId() {
    long time = Utils.currentTime();
    if (time < lastTime) {
      //当前时间小于上次时间，说明时钟不对
      long offset = lastTime - time;
      if (offset <= 5) {
        try {
          wait(offset << 1);
          time = Utils.currentTime();
          if (time < lastTime) {
            throw new IllegalStateException("Clock moved backwards.");
          }
        } catch (InterruptedException e) {
          LOGGER.error("wait interrupted");
          throw new IllegalStateException("Clock moved backwards.");
        }
      } else {
        throw new IllegalStateException("Clock moved backwards.");
      }
    }
    if (time == lastTime) {
      seqId = (seqId + 1) & SEQ_MASK;
      if (seqId == 0) {
        //说明该毫秒下对序列已经自增完毕，等待下一个毫秒
        Utils.tilNextMillis(lastTime);
      }
    } else {
      seqId = INIT_SEQ;
    }
    lastTime = time;
    long id = time << TIME_LEFT_BIT;
    id |= serverId << SERVER_LEFT_BIT;
    id |= seqId;// & SEQ_MASK;
    return id;
  }

  /**
   * 使用服务器id创建一个IdFactory,每个服务器id始终只有一个实例，避免同一个服务器id生成重复的id.
   * <p>
   * <b>在分布式环境下，必须保证每个分布式服务使用的serverId不同，否则还是可能生成重复的id</b>
   *
   * @param serverId 服务器id
   * @return IdFactory
   */
  static SnowflakeIdFactory create(int serverId) {
    SnowflakeIdFactory idFactory = FACTORY_HOLDER.get(serverId);
    if (idFactory != null) {
      return idFactory;
    }
    idFactory = FACTORY_HOLDER.putIfAbsent(serverId, new SnowflakeIdFactory(serverId));
    if (idFactory != null) {
      return idFactory;
    } else {
      return FACTORY_HOLDER.get(serverId);
    }
  }

  /**
   * 从主键中提取时间. 将ID左移22位，提取出时间
   *
   * @param id 主键
   * @return 时间
   */
  @Override
  public long fetchTime(Long id) {
    return id >> TIME_LEFT_BIT;
  }

  @Override
  public long fetchSharding(Long id) {
    return (id ^ (fetchTime(id) << TIME_LEFT_BIT)) >> SERVER_LEFT_BIT;
  }

  @Override
  public long fetchSeq(Long id) {
    return (id ^ (fetchTime(id) << TIME_LEFT_BIT)) ^ (fetchSharding(id) << SERVER_LEFT_BIT);
  }
}
