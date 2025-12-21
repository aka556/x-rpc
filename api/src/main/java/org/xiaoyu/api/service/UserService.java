package org.xiaoyu.api.service;

import org.xiaoyu.api.annotation.Retryable;
import org.xiaoyu.api.pojo.User;

public interface UserService {
    @Retryable
    User getUserById(Integer id); // 获取用户

    @Retryable
    Integer insertUser(User user); // 插入用户
}
