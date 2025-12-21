package org.xiaoyu.core.test.balance;

import org.junit.Before;
import org.junit.Test;
import org.xiaoyu.core.client.serviceCenter.balance.Impl.RandomLoadBalance;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 随机负载均衡测试
 */
public class RandomLoadBalanceTest {
    private RandomLoadBalance randomLoadBalance;

    @Before
    public void setUp() {
        randomLoadBalance = new RandomLoadBalance();
    }

    @Test
    public void testBalanceWithNonEmptyList() {
        List<String> addressList = Arrays.asList("server1", "server2", "server3");
        // 选择一个服务器
        String server = randomLoadBalance.balance(addressList);
        assertTrue(addressList.contains(server));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBalanceWithEmptyList() {
       randomLoadBalance.balance(Arrays.asList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBalanceWithNullList() {
        String server = randomLoadBalance.balance(null);
        assertNull("server should be null", server);
    }

    @Test
    public void testAddNode() {
        randomLoadBalance.addNode("server4");

        List<String> addressList = Arrays.asList("server1", "server2", "server3", "server4");
        assertTrue(addressList.contains("server4"));
    }

    @Test
    public void testRemoveNode() {
        randomLoadBalance.addNode("server2");
        randomLoadBalance.removeNode("server2");

        List<String> addressList = Arrays.asList("server1", "server3");
        assertFalse(addressList.contains("server2"));
    }
}
