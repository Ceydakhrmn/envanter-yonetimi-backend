package com.efsora.envanter;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MerhabaController {

    @GetMapping("/selam")
    public String selamVer() {
        return "Hello Ceyda! Your first Backend service at Efsora is ready.";
    }
}
