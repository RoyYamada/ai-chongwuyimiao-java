package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PrintController {
    private static final Logger log = LoggerFactory.getLogger(PrintController.class);

    @GetMapping("/print")
    public String print(@RequestParam(defaultValue = "Hello") String msg) {
        log.info("打印: {}", msg);
        return "已打印: " + msg;
    }
}
