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

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * 网络工具类
 * <p>
 * <p>
 * 127.xxx.xxx.xxx 属于"loopback" 地址，即只能你自己的本机可见，就是本机地址，比较常见的有127.0.0.1；
 * 192.168.xxx.xxx 属于private 私有地址(site local address)，属于本地组织内部访问，只能在本地局域网可见。
 * 同样10.xxx.xxx.xxx、从172.16.xxx.xxx 到 172.31.xxx.xxx都是私有地址，也是属于组织内部访问；
 * 169.254.xxx.xxx 属于连接本地地址（link local IP），在单独网段可用
 * 从224.xxx.xxx.xxx 到 239.xxx.xxx.xxx 属于组播地址
 * 比较特殊的255.255.255.255 属于广播地址
 * 除此之外的地址就是点对点的可用的公开IPv4地址
 */
public class Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    private Utils() {
        throw new AssertionError("Not instantiable: " + Utils.class);
    }

    /**
     * 循环直到下一个毫秒
     *
     * @param lastTime 原毫秒数
     */
    public static long tilNextMillis(long lastTime) {
        long timestamp = currentTime();
        while (timestamp <= lastTime) {
            timestamp = currentTime();
        }
        return timestamp;
    }

    public static long currentTime() {
        return System.currentTimeMillis();
    }

    public static Integer getPid() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        if (Strings.isNullOrEmpty(name)) {
            return null;
        }
        return Integer.parseInt(name.split("@")[0]);
    }

    public static String getIpV4(String interfaceName) {
        try {
            List<InetAddress> ipList = getInetAddress(interfaceName);
            if (ipList.isEmpty()) {
                return null;
            }
            return ipList.get(0).toString().replaceAll("^/+", "");
        } catch (Exception e) {
            LOGGER.warn("Utils get IP warn", e);
        }
        return null;
    }


    /**
     * 获取本机的一个IPV4地址
     *
     * @return IPV4地址
     */
    public static String getIpv4() {
        try {
            InetAddress lanIp = getFirstInetAddress();
            if (lanIp == null) {
                return null;
            }
            return lanIp.toString().replaceAll("^/+", "");
        } catch (Exception e) {
            LOGGER.warn("Utils get IP warn", e);
        }
        return null;
    }

    /**
     * 获取本机对一个mac地址,mac地址会去掉-.
     *
     * @return mac地址
     */
    public static String getMac() {
        try {
            InetAddress lanIp = getFirstInetAddress();
            if (lanIp == null) {
                return null;
            }
            return getMac(lanIp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public static List<InetAddress> getInetAddress() throws SocketException {
        List<InetAddress> ipList = new ArrayList<>(5);
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface ni = interfaces.nextElement();
            Enumeration<InetAddress> allAddress = ni.getInetAddresses();
            while (allAddress.hasMoreElements()) {
                InetAddress address = allAddress.nextElement();
                if (!address.isLoopbackAddress() && !(address instanceof Inet6Address)) {
                    // skip the loopback addr
                    // skip the IPv6 addr
                    ipList.add(address);
                }
            }
        }
        return ipList;
    }

    public static List<InetAddress> getInetAddress(String interfaceName) throws SocketException {
        List<InetAddress> ipList = new ArrayList<>(5);
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface ni = interfaces.nextElement();
            if (!ni.getDisplayName().equals(interfaceName)) {
                continue;
            }
            Enumeration<InetAddress> allAddress = ni.getInetAddresses();
            while (allAddress.hasMoreElements()) {
                InetAddress address = allAddress.nextElement();
                if (!address.isLoopbackAddress() && !(address instanceof Inet6Address)) {
                    // skip the loopback addr
                    // skip the IPv6 addr
                    ipList.add(address);
                }
            }
        }
        return ipList;
    }

    /**
     * 返回第一个有效IP
     */
    public static InetAddress getFirstInetAddress() throws SocketException, UnknownHostException {
        List<InetAddress> ipList = getInetAddress();
        for (InetAddress address : ipList) {
            if (address.isSiteLocalAddress()) {
                return InetAddress.getByName(address.getHostAddress());
            }
        }
        return null;
    }

    /**
     * 根据IP地址获取mac地址
     *
     * @param ip ip地址
     * @return mac
     */
    public static String getMac(InetAddress ip) {
        String address = null;
        try {
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            byte[] mac = network.getHardwareAddress();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X", mac[i]));
            }
            address = sb.toString();

        } catch (SocketException e) {
            e.printStackTrace();
        }
        return address;
    }

}
