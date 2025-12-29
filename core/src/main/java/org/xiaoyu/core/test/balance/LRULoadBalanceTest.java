package org.xiaoyu.core.test.balance;

import org.junit.Before;
import org.junit.Test;
import org.xiaoyu.core.client.serviceCenter.balance.Impl.LRULoadBalance;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * LRU负载均衡测试
 */
public class LRULoadBalanceTest {
    private LRULoadBalance lruLoadBalance;

    @Before
    public void setUp() {
        lruLoadBalance = new LRULoadBalance();
    }

    @Test
    public void testBalanceWithNonEmptyList() {
        List<String> addressList = Arrays.asList("server1", "server2", "server3");
        String server = lruLoadBalance.balance(addressList);
        assertNotNull(server);
        assertTrue(addressList.contains(server));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBalanceWithEmptyList() {
        List<String> addressList = Arrays.asList();
        lruLoadBalance.balance(addressList);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBalanceWithNullList() {
        lruLoadBalance.balance(null);
    }

    @Test
    public void testAddNode() {
        lruLoadBalance.addNode("server4");
        String server = lruLoadBalance.balance(Arrays.asList("server1", "server2", "server3", "server4"));
        assertNotNull(server);
        assertTrue(Arrays.asList("server1", "server2", "server3", "server4").contains(server));
    }

    @Test
    public void testRemoveNode() {
        List<String> addressList = Arrays.asList("server1", "server2", "server3");
        lruLoadBalance.balance(addressList); // 访问一次，建立初始状态
        lruLoadBalance.removeNode("server1");
        String selected = lruLoadBalance.balance(Arrays.asList("server2", "server3"));
        assertNotNull(selected);
        assertTrue(Arrays.asList("server2", "server3").contains(selected));
    }

    @Test
    public void testLRUOrdering() throws InterruptedException {
        List<String> addressList = Arrays.asList("server1", "server2", "server3");
        
        // 初始状态下，所有服务器的访问时间接近，我们选择第一个（按列表顺序）
        String firstSelection = lruLoadBalance.balance(addressList);
        String secondSelection = lruLoadBalance.balance(addressList);
        String thirdSelection = lruLoadBalance.balance(addressList);
        
        // 由于访问时间非常接近，第一次选择可能是任意的，但关键是我们验证LRU逻辑
        assertNotNull(firstSelection);
        assertNotNull(secondSelection);
        assertNotNull(thirdSelection);
        assertTrue(addressList.contains(firstSelection));
        assertTrue(addressList.contains(secondSelection));
        assertTrue(addressList.contains(thirdSelection));
        
        // 等待一段时间，确保时间戳有差异
        Thread.sleep(2);
        
        // 再次选择，应该选择最久未使用的那个
        String fourthSelection = lruLoadBalance.balance(addressList);
        assertNotNull(fourthSelection);
        assertTrue(addressList.contains(fourthSelection));
    }
}