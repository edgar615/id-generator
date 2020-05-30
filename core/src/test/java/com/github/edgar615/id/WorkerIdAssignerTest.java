package com.github.edgar615.id;

import org.junit.Test;

public class WorkerIdAssignerTest {

    @Test
    public void testMac() {
        System.out.println(new MacWorkerIdAssigner().assignWorkerId());
    }

    @Test
    public void testIp() {
        System.out.println(new IpWorkerIdAssigner().assignWorkerId());
    }

    @Test
    public void testIpPid() {
        System.out.println(new IPAndPidWorkIdAssigner().assignWorkerId());
    }

    @Test
    public void testMacPid() {
        System.out.println(new MacAndPidWorkIdAssigner().assignWorkerId());
    }
}
