package com.example.demo;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimestampTest {
    public static void main(String[] args) {
        // 测试时间戳
        long timestamp1 = 1776441600;
        long timestamp2 = 1776528000;
        
        // 转换为Instant
        Instant instant1 = Instant.ofEpochSecond(timestamp1);
        Instant instant2 = Instant.ofEpochSecond(timestamp2);
        
        // 转换为本地时间
        LocalDateTime dateTime1 = LocalDateTime.ofInstant(instant1, ZoneId.systemDefault());
        LocalDateTime dateTime2 = LocalDateTime.ofInstant(instant2, ZoneId.systemDefault());
        
        // 格式化输出
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println("Timestamp 1: " + timestamp1 + " -> " + dateTime1.format(formatter));
        System.out.println("Timestamp 2: " + timestamp2 + " -> " + dateTime2.format(formatter));
        
        // 转换为UTC时间
        LocalDateTime utcDateTime1 = LocalDateTime.ofInstant(instant1, ZoneId.of("UTC"));
        LocalDateTime utcDateTime2 = LocalDateTime.ofInstant(instant2, ZoneId.of("UTC"));
        System.out.println("UTC Timestamp 1: " + timestamp1 + " -> " + utcDateTime1.format(formatter));
        System.out.println("UTC Timestamp 2: " + timestamp2 + " -> " + utcDateTime2.format(formatter));
    }
}