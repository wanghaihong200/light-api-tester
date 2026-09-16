package com.wang.light.example.service;

import com.wang.light.api.client.ApiClient;
import com.wang.light.api.client.ApiResponse;
import com.wang.light.api.service.BaseApiService;
import com.wang.light.example.model.CreateUserReq;
import com.wang.light.example.model.User;
import org.springframework.stereotype.Component;

/**
 * 接口定义代码化示例：继承 BaseApiService，一个接口一个方法（Spring Bean）。
 */
@Component
public class UserApiService extends BaseApiService {

    public UserApiService(ApiClient client) {
        super(client);
    }

    public User getUser(long id) {
        return get("/api/users/" + id).execute().as("$.data", User.class);
    }

    public User createUser(String name) {
        return post("/api/users").body(new CreateUserReq(name)).execute().as("$.data", User.class);
    }

    public ApiResponse listUsers(int page) {
        return get("/api/users").query("page", page).execute();
    }
}
