package org.xiaoyu.provider.Impl;

import lombok.extern.slf4j.Slf4j;
import org.xiaoyu.api.pojo.User;
import org.xiaoyu.api.service.UserService;

import java.util.Random;
import java.util.UUID;

@Slf4j
public class UserServiceImpl implements UserService {
    @Override
    public User getUserById(Integer id) {
        log.info("客户端查询了id为: " + id + " 的用户");
        Random random = new Random();
        User user =  User.builder().id(id)
                .userName(UUID.randomUUID().toString())
                .gender(random.nextBoolean())
                .build();
        log.info(("返回用户信息: { }" + user));
        return user;
    }

    @Override
    public Integer insertUser(User user) {
        log.info("插入数据成功"+user.getUserName());
        return user.getId();
    }
}
