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

import com.google.common.base.Splitter;
import com.google.common.base.Strings;

/**
 * 获取IP的二进制表示的后10位，设置成workerId。
 *
 * Created by edgar on 16-11-26.
 *
 * @author Edgar
 */
public class IpWorkerIdAssigner implements WorkerIdAssigner {

  @Override
  public int assignWorkerId() {
    String ip = Utils.getIpv4();
    StringBuilder hexIp = new StringBuilder();
    Splitter.on(".").omitEmptyStrings().trimResults().split(ip)
        .forEach(s -> hexIp
            .append(Strings.padStart(Integer.toBinaryString(Integer.parseInt(s)), 8, '0')));
    return Integer.parseInt(hexIp.substring(22), 2);
  }
}
