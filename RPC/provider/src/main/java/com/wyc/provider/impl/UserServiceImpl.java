package com.wyc.provider.impl;

import com.wyc.common.model.User;
import com.wyc.common.service.UserService;

/**
 * 实现UserService接口
 */
public class UserServiceImpl implements UserService {
    @Override
    public User getUser(User user) {
        System.out.println("Hello 我的名字是: "  + user.getName());
        return user;
    }

    @Override
    public int getNumber() {
        return 1;
    }
}
