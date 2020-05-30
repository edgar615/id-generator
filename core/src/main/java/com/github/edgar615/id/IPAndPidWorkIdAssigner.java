package com.github.edgar615.id;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;

/**
 * 获取IP的二进制表示的后5位作为datacenter的id，pid去5位作为workerID。
 *
 * Created by edgar on 16-11-26.
 *
 * @author Edgar
 */
public class IPAndPidWorkIdAssigner implements WorkerIdAssigner {
    @Override
    public int assignWorkerId() {
        String ip = Utils.getIpv4();
        StringBuilder hexIp = new StringBuilder();
        Splitter.on(".").omitEmptyStrings().trimResults().split(ip)
                .forEach(s -> hexIp
                        .append(Strings.padStart(Integer.toBinaryString(Integer.parseInt(s)), 8, '0')));
        String hexPid = Integer.toBinaryString(Utils.getPid());
        hexPid = Strings.padStart(hexPid, 5, '0');
        if (hexPid.length() > 5) {
            hexPid = hexPid.substring(hexPid.length() - 5);
        }
        return Integer.parseInt(hexIp.substring(27) + hexPid, 2);
    }
}
