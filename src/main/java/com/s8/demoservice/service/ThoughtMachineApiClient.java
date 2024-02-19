package com.s8.demoservice.service;

import com.s8.demoservice.AppConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

@Component
public class ThoughtMachineApiClient {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private AppConfiguration configuration;

    @PostConstruct
    private void init(){
        ignoreSslCertificates();
    }

    public <T> ResponseEntity<T> get(String baseUrl, String queryString, Class<T> responseType){
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Auth-Token", configuration.getThoughtMachineAuthToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("body", headers);
        String url = baseUrl + (queryString == null || queryString.isEmpty()? "": ("?" + queryString));
        return restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
    }

    public <T> ResponseEntity<T> post(String url, String body, Class<T> responseType){
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Auth-Token", configuration.getThoughtMachineAuthToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url, HttpMethod.POST, entity, responseType);
    }

    public <T> ResponseEntity<T> put(String url, String body, Class<T> responseType){
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Auth-Token", configuration.getThoughtMachineAuthToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url, HttpMethod.PUT, entity, responseType);
    }

    private void ignoreSslCertificates() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }
            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }};

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception ignored) {
        }
    }
}
