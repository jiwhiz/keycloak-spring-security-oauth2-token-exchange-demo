package com.jiwhiz.demo.mydoctor.appointment;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.security.Principal;

import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AppointmentController {

    @GetMapping("/appointments")
    public List<Message> getAppt(Principal principal) {
        return List.of(new Message("Appt 1"), new Message("Appt 2"), new Message("Appt 3"));
    }
}
