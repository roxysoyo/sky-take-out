package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;


@Service
@Slf4j
public class UserServiceImpl implements UserService {

    public static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;

    /**
     * 微信登录
     *
     * @param userLoginDTO
     * @return
     */
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        // 调用微信服务接口，获取openid
        String openid = getOpenid(userLoginDTO.getCode());

        // 判断openid是否为空，为空表示登录失败，抛出业务异常
        if (openid == null) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        // 判断是否为新用户
        // 如果是新用户，自动完成注册
        User user = userMapper.getByOpenid(openid);

        if (user == null) {
            //当前是一个新用户，自动完成注册
            user = new User();
            user.setOpenid(openid);
            user.setCreateTime(LocalDateTime.now());
            userMapper.insert(user);
        }
        // 返回这个用户对象
        return user;
    }

    /**
     * 调用微信服务接口，获取openid
     *
     * @param code
     * @return
     */
    private String getOpenid(String code) {
        // 调用微信服务接口，获取openid
        HashMap<String, String> map = new HashMap<>();
        map.put("appid", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");
        String json = HttpClientUtil.doGet(WX_LOGIN, map);
        log.info("微信登录返回结果:{}", json);
        JSONObject jsonObject = JSON.parseObject(json);
        return jsonObject.getString("openid");
    }
}
