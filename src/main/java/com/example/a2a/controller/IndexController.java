package com.example.a2a.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 首页控制器
 * 处理根路径请求，重定向到前端页面
 */
@Controller
public class IndexController {

    /**
     * 根路径重定向到首页
     * @return 首页视图
     */
    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }
}
