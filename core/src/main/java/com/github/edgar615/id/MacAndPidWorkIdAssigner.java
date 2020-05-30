package com.github.edgar615.id;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;

/**
 * 获取IP的二进制表示的后5位作为datacenter的id，pid后5位作为workerID。
 *
 * Created by edgar on 16-11-26.
 *
 * @author Edgar
 */
public class MacAndPidWorkIdAssigner implements WorkerIdAssigner {
    @Override
    public int assignWorkerId() {
        String mac = Utils.getMac();
        String hexMac = Long.toBinaryString(Long.parseLong(mac, 16));
        String hexPid = Integer.toBinaryString(Utils.getPid());
        hexPid = Strings.padStart(hexPid, 5, '0');
        if (hexPid.length() > 5) {
            hexPid = hexPid.substring(hexPid.length() - 5);
        }
        return Integer.parseInt(hexMac.substring(hexMac.length() - 5) + hexPid, 2);
    }
}
