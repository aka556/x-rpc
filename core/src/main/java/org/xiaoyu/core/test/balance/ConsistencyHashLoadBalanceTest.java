package org.xiaoyu.core.test.balance;

import org.junit.Before;
import org.junit.Test;
import org.xiaoyu.core.client.serviceCenter.balance.Impl.ConsistencyHashLoadBalance;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 一致性哈希负载均衡测试
 */
public class ConsistencyHashLoadBalanceTest {
    // 一致性哈希负载均衡实例
    private ConsistencyHashLoadBalance consistencyHashLoadBalance;

    @Before
    public void setUp() {
        consistencyHashLoadBalance = new ConsistencyHashLoadBalance();
    }

    /**
     * 测试初始化
     */
    @Test
    public void testInit() {
        // 模拟真实节点
        List<String> nodes = Arrays.asList("server1", "server2", "server3");
        consistencyHashLoadBalance.init(nodes);

        assertFalse("shards should not be empty", consistencyHashLoadBalance.getShards().isEmpty());
        assertTrue("realNodes should contain all nodes", consistencyHashLoadBalance.getRealNodes().containsAll(nodes));
    }

    /**
     * 测试获取服务器
     */
    @Test
    public void testGetServer() {
        // 模拟真实节点
        List<String> nodes = Arrays.asList("server1", "server2", "server3");
        consistencyHashLoadBalance.init(nodes);

        // 模拟请求
        String server = consistencyHashLoadBalance.getServer("request1", nodes);
        assertNotNull("server should not be null", server);
        assertTrue("server should be one of the real nodes", nodes.contains(server));

        // 保证多个请求的分配在不同节点
        String server2 = consistencyHashLoadBalance.getServer("request2", nodes);
        assertNotEquals("server should be different from server2", server, server2);
    }

    /**
     * 测试添加节点
     */
    @Test
    public void testAddNode() {
        // 模拟真实节点
        List<String> nodes = Arrays.asList("server1", "server2", "server3");
        consistencyHashLoadBalance.init(nodes);

        // 添加节点
        consistencyHashLoadBalance.addNode("server4");

        // 确保添加的节点被添加到真实节点列表中
        assertTrue("server4 should be added to realNodes", consistencyHashLoadBalance.getRealNodes().contains("server4"));
        assertFalse("shards should contain virtual node3", consistencyHashLoadBalance.getShards().isEmpty());
    }

    /**
     * 测试删除节点
     */
    @Test
    public void testRemoveNode() {
        List<String> nodes = Arrays.asList("server1", "server2", "server3");
        consistencyHashLoadBalance.init(nodes);

        // 删除节点
        consistencyHashLoadBalance.removeNode("server2");
        assertFalse("server2 should be removed from realNodes", consistencyHashLoadBalance.getRealNodes().contains("server2"));
        assertFalse("shards should not contain virtual node2", consistencyHashLoadBalance
                .getShards().values().stream().anyMatch(s -> s.contains("server2")));
    }

    /**
     * 测试负载均衡空列表
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBalanceWithEmptyList() {
        consistencyHashLoadBalance.balance(Arrays.asList());
    }

    /**
     * 测试负载均衡为null list
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBalanceWithNullList() {
        assertNull(consistencyHashLoadBalance.balance(null));
    }

    /**
     * 测试返回虚拟节点个数
     */
    @Test
    public void testGetVirtualNum() {
        assertEquals(5, ConsistencyHashLoadBalance.getVirtualNum());
    }
}
