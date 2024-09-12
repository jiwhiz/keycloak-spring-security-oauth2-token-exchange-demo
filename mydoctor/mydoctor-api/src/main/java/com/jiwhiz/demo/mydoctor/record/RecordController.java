package com.jiwhiz.demo.mydoctor.record;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class RecordController {

    private final WebClient webClient;

    @GetMapping("/records")
    public List<Message> getHealthRecords(
        @RegisteredOAuth2AuthorizedClient("mydoctor-client")
        OAuth2AuthorizedClient doctorAuthClient
    ) {
        String token = doctorAuthClient.getAccessToken().getTokenValue();
        log.debug("Exchanged access token is:\n {}\n", token);

        var result = webClient.get()
            .uri("http://api.myhealth:8082/api/records")
            .headers((headers) -> headers.setBearerAuth(token))
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<Message>>() {})
            .block()
            ;
        log.debug("Return result from MyHealth API Server: {}", result);
        return result;
    }
}
