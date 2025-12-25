package org.xiaoyu.consumer;

import lombok.extern.slf4j.Slf4j;
import org.xiaoyu.api.pojo.User;
import org.xiaoyu.api.service.UserService;
import org.xiaoyu.core.client.proxy.ClientProxy;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class ConsumerTest {
    private static final int THREAD_POOL_SIZE = 20; // 线程池大小
    private static final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public static void main(String[] args) throws InterruptedException {
        ClientProxy clientProxy = new ClientProxy();
        // 通过动态服务发现机制获取服务端地址
        UserService proxy = clientProxy.getProxy(UserService.class);

        for (int i = 0; i < 120; i++) {
            Integer index = i;
            if (i % 30 == 0) {
                Thread.sleep(10000);
            }

            new Thread(() -> {
                try {
                    User user = proxy.getUserById(index);
                    if (user != null) {
                        log.info("从服务端得到的user={}", user);
                    }

                    Integer id = proxy.insertUser(User.builder().id(index)
                            .userName("User" + index).gender(true).build());
                    log.info("向客户端插入了id为: {} 的用户", id);
                } catch (NullPointerException e) {
                    log.info("User为空");
                    e.printStackTrace();
                }
            }).start();
        }

        executorService.shutdown();
        clientProxy.close();
    }
}
