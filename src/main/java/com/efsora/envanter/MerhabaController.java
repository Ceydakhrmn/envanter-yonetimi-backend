package com.efsora.envanter;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MerhabaController {

    @GetMapping("/selam")
    public String selamVer() {
        return "Selam Ceyda! Efsora'daki ilk Backend servisin hazır.";
    }
}
