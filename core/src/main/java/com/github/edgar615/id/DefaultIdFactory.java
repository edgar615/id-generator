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

/**
 * 默认的ID生成类. 64 bits组成：41bits的时间戳+22bits的自增序列
 * <p>
 * 0    00000.....000 0000000000000000000000 |    |___________| |____________________| |        | |
 * 1bit     41bit             22bit
 * </p>
 * 第一段:1bit 预留 实际上是作为long的符号位 第二段:41bit 时间标记 记录的是当前时间与元年的时间差 第三段:22bit 单毫秒内自增序列 每毫秒最多可以生成4194304个ID
 * <p>
 * 该ID的生成策略只能保证在同一台机器上，ID不重复。 每毫秒可以生成4194304个ID，足够项目使用了。
 * <p>
 * 单例类。
 *
 * @author Edgar
 */
class DefaultIdFactory implements IdFactory<Long>, TimeExtracter<Long>, SeqExtracter<Long> {

  /**
   * 自增序列的位数
   */
  private static final int SEQ_BIT = 22;

  /**
   * 最大序列号
   */
  private static final int SEQ_MASK = -1 ^ (-1 << SEQ_BIT);

  /**
   * 时间的左移位数
   */
  private static final int TIME_LEFT_BIT = SEQ_BIT;

  /**
   * 每次初始化对序列值.
   */
  private static final int INIT_SEQ = 0;

  /**
   * 单例对象
   */
  private static final DefaultIdFactory INSTANCE = new DefaultIdFactory();

  /**
   * 上次时间戳
   */
  private volatile long lastTime = -1L;

  /**
   * 自增序列
   */
  private volatile long seqId = INIT_SEQ;

  private DefaultIdFactory() {
  }

  /**
   * 64 bits组成：41bits的时间戳+22bits的自增序列
   *
   * @return ID
   */
  @Override
  public synchronized Long nextId() {
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
    long id = time << TIME_LEFT_BIT;
    id |= seqId & SEQ_MASK;
    return id;
  }

  /**
   * 返回DefaultIdFactory的单例对象
   *
   * @return IdFactory
   */
  static DefaultIdFactory instance() {
    return INSTANCE;
  }

  /**
   * 从ID中提取时间. 将ID左移22位，提取出时间
   *
   * @param id ID
   * @return 时间
   */
  @Override
  public long fetchTime(Long id) {
    return id >> TIME_LEFT_BIT;
  }


  @Override
  public long fetchSeq(Long id) {
    return (id ^ (fetchTime(id) << TIME_LEFT_BIT));
  }
}
