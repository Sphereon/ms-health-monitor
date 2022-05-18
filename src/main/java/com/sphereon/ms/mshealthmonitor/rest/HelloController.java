package com.sphereon.ms.mshealthmonitor.rest;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    String hello() {
        return "<html><body><b>I'm okay...</b></body></html>";
    }
}
