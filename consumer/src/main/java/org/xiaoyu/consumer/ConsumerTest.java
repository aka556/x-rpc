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

            executorService.submit(() -> {
                try {
                    User user = proxy.getUserById(index);
                    if (user != null) {
                        log.info("从服务端得到的user={}", user);
                    } else {
                        log.warn("从服务端得到的user为null, userId={}",index);
                    }

                    Integer id = proxy.insertUser(User.builder().id(index)
                            .userName("User" + index).gender(true).build());
                    if (id != null) {
                        log.info("向服务端插入user的id={}", id);
                    } else {
                        log.warn("插入失败，返回的id为null, userId={}", index);
                    }

                } catch (NullPointerException e) {
                    log.error("调用服务时发生异常，userId={}", index, e);
                }
            });
        }

        executorService.shutdown();
        clientProxy.close();
    }
}
