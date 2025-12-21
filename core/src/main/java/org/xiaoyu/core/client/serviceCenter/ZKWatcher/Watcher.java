package org.xiaoyu.core.client.serviceCenter.ZKWatcher;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.xiaoyu.core.client.cache.ServiceCache;

public class Watcher {
    // zookeeper客户端
    private CuratorFramework client;
    // 服务缓存
    ServiceCache cache;

    public Watcher(CuratorFramework client, ServiceCache cache) {
        this.client = client;
        this.cache = cache;
    }

    /**
     * 监听当前节点和子节点的
     * 创建、删除
     */
    public void watchToUpdate(String path) throws InterruptedException {
        CuratorCache curatorCache = CuratorCache.build(client, "/");
        curatorCache.listenable().addListener(new CuratorCacheListener() {
            @Override
            public void event(Type type, ChildData childData, ChildData childData1) {
                // 第一个参数：事件类型（枚举）
                // 第二个参数：节点更新前的状态、数据
                // 第三个参数：节点更新后的状态、数据
                // 创建节点时：节点刚被创建，不存在 更新前节点 ，所以第二个参数为 null
                // 删除节点时：节点被删除，不存在 更新后节点 ，所以第三个参数为 null
                // 节点创建时没有赋予值 create /curator/app1 只创建节点，在这种情况下，更新前节点的 data 为 null，
                // 获取不到更新前节点的数据
                switch (type.name()) {
                    case "NODE_CREATED": // 节点创建
                        String[] pathList = parsePath(childData1);
                        if (pathList.length <= 2) break;
                        else {
                            String serviceName = pathList[1];
                            String address = pathList[2];
                            cache.addServiceToCache(serviceName, address);
                        }
                        break;
                    // 节点更新
                    case "NODE_CHANGED":
                        if (childData.getData() != null) {
                            System.out.println("修改前的数据: " + new String(childData.getData()));
                        } else {
                            System.out.println("节点第一次赋值");
                        }
                        String[] oldPathList = parsePath(childData);
                        String[] newPathList = parsePath(childData1);
                        cache.replaceServiceToCache(oldPathList[1], oldPathList[2], newPathList[2]);
                        System.out.println("修改后的数据" + new String(childData1.getData()));
                        break;
                    case "NODE_DELETED":
                        String[] pathListDelete = parsePath(childData);
                        // 判断节点是否完整
                        if (pathListDelete.length <= 2) break;
                        else {
                            String serviceName = pathListDelete[1];
                            String address = pathListDelete[2];
                            cache.deleteServiceFromCache(serviceName, address);
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        // 开启监听
        curatorCache.start();
    }

    // 地址解析
    private String[] parsePath(ChildData childData) {
        String path = new String(childData.getPath());
        return path.split("/");
    }
}
