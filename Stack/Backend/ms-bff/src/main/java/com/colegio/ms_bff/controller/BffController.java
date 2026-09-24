package com.colegio.bff.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api")
public class BffController {

    private final RestClient restClient;

    @Value("${services.academico-url}")
    private String academicoUrl;

    @Value("${services.administracion-url}")
    private String administracionUrl;

    @Value("${services.internal-token}")
    private String internalToken;

    public BffController() {
        this.restClient = RestClient.create();
    }

    // Ruta 1: /api/clases/** -> Enruta hacia ms-academico
    @RequestMapping(value = "/clases/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyClases(
            HttpServletRequest request,
            @RequestBody(required = false) byte[] body,
            @RequestHeader HttpHeaders headers) {

        String path = request.getRequestURI().replace("/api/clases", "/api/academico");
        String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
        String targetUrl = academicoUrl + path + queryString;

        return restClient.method(HttpMethod.valueOf(request.getMethod()))
                .uri(targetUrl)
                .headers(h -> {
                    h.putAll(headers);
                    h.set("X-Internal-Token", internalToken);
                })
                .body(body != null ? body : new byte[0])
                .retrieve()
                .toEntity(byte[].class);
    }

    // Ruta 2: /api/catalogo/** -> Enruta hacia ms-administracion
    @RequestMapping(value = "/catalogo/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyCatalogo(
            HttpServletRequest request,
            @RequestBody(required = false) byte[] body,
            @RequestHeader HttpHeaders headers) {

        String path = request.getRequestURI().replace("/api/catalogo", "/api/admin");
        String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
        String targetUrl = administracionUrl + path + queryString;

        return restClient.method(HttpMethod.valueOf(request.getMethod()))
                .uri(targetUrl)
                .headers(h -> {
                    h.putAll(headers);
                    h.set("X-Internal-Token", internalToken);
                })
                .body(body != null ? body : new byte[0])
                .retrieve()
                .toEntity(byte[].class);
    }
}