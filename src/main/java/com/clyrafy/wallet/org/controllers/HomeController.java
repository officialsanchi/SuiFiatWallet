package com.clyrafy.wallet.org.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/home/{name}")
    public String ourHomeController(@PathVariable("name") String name) {
        return String.format( "Hi %s, Server is working", name );
    }
}
