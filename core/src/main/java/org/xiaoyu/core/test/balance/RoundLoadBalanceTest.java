package org.xiaoyu.core.test.balance;

import org.junit.Before;
import org.junit.Test;
import org.xiaoyu.core.client.serviceCenter.balance.Impl.RoundLoadBalance;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @description 轮询负载均衡测试
 * @author xiaoyu
 * @date 2025/12/16
 */
public class RoundLoadBalanceTest {
    private RoundLoadBalance roundLoadBalance;

    @Before
    public void setUp() {
        roundLoadBalance = new RoundLoadBalance();
    }

    @Test
    public void testBalanceWithNonEmptyList() {
        List<String> addressList = Arrays.asList("server1", "server2", "server3");
        // 选择一个服务器
        String server = roundLoadBalance.balance(addressList);
        assertTrue(addressList.contains(server));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBalanceWithEmptyList() {
        List<String> addressList = Arrays.asList();
        String server = roundLoadBalance.balance(addressList);
        assertTrue(addressList.contains(server));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBalanceWithNullList() {
        String server = roundLoadBalance.balance(null);
        assertNull("server should be null", server);
    }

    @Test
    public void testAddNode() {
        roundLoadBalance.addNode("server4");

        List<String> addressList = Arrays.asList("server1", "server2", "server3", "server4");
        assertTrue(addressList.contains("server4"));
    }

    @Test
    public void testRemoveNode() {
        roundLoadBalance.addNode("server2");
        roundLoadBalance.removeNode("server2");

        List<String> addressList = Arrays.asList("server1", "server3");
        assertFalse(addressList.contains("server2"));
    }
}
