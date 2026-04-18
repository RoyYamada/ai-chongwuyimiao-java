package com.example.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;
import java.util.TimeZone;

@Configuration
public class ObjectMapperConfig {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // 设置时区为系统默认时区
        mapper.setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
        
        // 注册JavaTime模块
        JavaTimeModule module = new JavaTimeModule();
        mapper.registerModule(module);
        
        // 禁用时间戳格式，使用ISO格式
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        return mapper;
    }
}
