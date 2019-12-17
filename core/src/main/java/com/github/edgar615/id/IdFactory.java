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
 * 主键生成的工厂类. Created by edgar on 16-4-2.
 *
 * @author Edgar
 */
public interface IdFactory<T> {

  /**
   * 主键生成方法
   *
   * @return 主键
   */
  T nextId();

  /**
   * 创建一个默认的IdFactory.
   *
   * @return IdFactory
   */
  static IdFactory<Long> defaultFactory() {
    return DefaultIdFactory.instance();
  }

  /**
   * 根据serverId创建一个IdFacory,每个serverId只会创建一个IdFactory
   *
   * @param serverId 服务器ID
   * @return IdFactory
   */
  static IdFactory<Long> simpleSnowflake(int serverId) {
    return SimpleSnowflakeIdFactory.create(serverId);
  }

  /**
   * 创建一个Boundary Flake的IdFactory
   *
   * @return IdFactory
   */
  static IdFactory<String> boundaryflake() {
    return BoundaryFlakeIdFactory.instance();
  }

  /**
   * 循环直到下一个毫秒
   *
   * @param lastTime 原毫秒数
   */
  default long tilNextMillis(long lastTime) {
    long timestamp = currentTime();
    while (timestamp <= lastTime) {
      timestamp = currentTime();
    }
    return timestamp;
  }

  default long currentTime() {
    return System.currentTimeMillis();
  }

}
