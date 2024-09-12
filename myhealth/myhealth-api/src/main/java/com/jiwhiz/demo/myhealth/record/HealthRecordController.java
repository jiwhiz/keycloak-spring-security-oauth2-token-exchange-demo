package com.jiwhiz.demo.myhealth.record;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api")
public class HealthRecordController {

    @GetMapping("/records")
    public List<Message> getHealthRecords() {
        return List.of(
            new Message("Record one"),
            new Message("Record two"),
            new Message("Record three")
        );
    }
}
