package com.sweetbook.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sweetbook.vo.BookRequestPreviewVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SweetBookApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${sweetbook.api.key}")
    private String apiKey;

    @Value("${sweetbook.api.base-url}")
    private String baseUrl;

    public SweetBookApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String createBook(BookRequestPreviewVO preview) {

        String url = baseUrl.trim() + "/books";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey.trim());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("title", preview.getTitle());
        requestBody.put("bookSpecUid", preview.getBookSpecCode());

        System.out.println("===== Books API 요청 =====");
        System.out.println("url = " + url);
        System.out.println("requestBody = " + requestBody);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, request, String.class);

            String body = response.getBody();

            System.out.println("===== Books API 응답 =====");
            System.out.println(body);

            JsonNode jsonNode = objectMapper.readTree(body);

            if (jsonNode == null) {
                throw new RuntimeException("Books API 응답이 비어 있습니다.");
            }

            JsonNode successNode = jsonNode.get("success");
            if (successNode == null || !successNode.asBoolean()) {
                throw new RuntimeException("Books API 실패 응답: " + body);
            }

            JsonNode dataNode = jsonNode.get("data");
            if (dataNode == null || dataNode.get("bookUid") == null) {
                throw new RuntimeException("Books API 응답 이상: " + body);
            }

            String bookUid = dataNode.get("bookUid").asText();

            System.out.println("===== Books API 생성 성공 =====");
            System.out.println("bookUid = " + bookUid);

            return bookUid;

        } catch (HttpClientErrorException e) {
            System.out.println("===== Books API 호출 실패 =====");
            System.out.println("status = " + e.getStatusCode());
            System.out.println("responseBody = " + e.getResponseBodyAsString());
            throw new RuntimeException(
                    "Books API 호출 실패: " + e.getStatusCode() + " / " + e.getResponseBodyAsString(),
                    e
            );
        } catch (Exception e) {
            System.out.println("===== Books API 호출 실패 =====");
            e.printStackTrace();
            throw new RuntimeException("Books API 호출 실패: " + e.getMessage(), e);
        }
    }

    public String getBookSpecs() {
        String url = baseUrl.trim() + "/book-specs";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey.trim());

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        System.out.println("===== book-specs =====");
        System.out.println(response.getBody());

        return response.getBody();
    }

    public String getTemplates() {
        String url = baseUrl.trim() + "/templates";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey.trim());

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        System.out.println("===== templates =====");
        System.out.println(response.getBody());

        return response.getBody();
    }

    public String addContents(String bookUid, BookRequestPreviewVO preview) {

        String url = baseUrl.trim() + "/books/" + bookUid + "/contents";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey.trim());
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        body.add("templateUid", preview.getTemplateCode());

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("title", preview.getTitle());

        if (preview.getPages() != null && !preview.getPages().isEmpty()) {
            for (int i = 0; i < preview.getPages().size(); i++) {
                String imageUrl = preview.getPages().get(i).getImageUrl();
                String text = preview.getPages().get(i).getText();

                parameters.put("text" + (i + 1), text);

                if (imageUrl != null && !imageUrl.isBlank()) {
                    parameters.put("image" + (i + 1), imageUrl);
                }
            }
        }

        try {
            body.add("parameters", objectMapper.writeValueAsString(parameters));

            System.out.println("===== Contents API 요청 =====");
            System.out.println("url = " + url);
            System.out.println("templateUid = " + preview.getTemplateCode());
            System.out.println("parameters = " + objectMapper.writeValueAsString(parameters));

            HttpEntity<MultiValueMap<String, Object>> request =
                    new HttpEntity<>(body, headers);

            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, request, String.class);

            String responseBody = response.getBody();

            System.out.println("===== Contents API 응답 =====");
            System.out.println(responseBody);

            return responseBody;

        } catch (HttpClientErrorException e) {
            System.out.println("===== Contents API 호출 실패 =====");
            System.out.println("status = " + e.getStatusCode());
            System.out.println("responseBody = " + e.getResponseBodyAsString());
            throw new RuntimeException(
                    "Contents API 호출 실패: " + e.getStatusCode() + " / " + e.getResponseBodyAsString(),
                    e
            );
        } catch (Exception e) {
            System.out.println("===== Contents API 호출 실패 =====");
            e.printStackTrace();
            throw new RuntimeException("Contents API 호출 실패: " + e.getMessage(), e);
        }
    }

    public String finalizeBook(String bookUid) {
        String url = baseUrl.trim() + "/books/" + bookUid + "/finalization";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey.trim());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>("{}", headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, request, String.class);

            String body = response.getBody();

            System.out.println("===== Finalization API 응답 =====");
            System.out.println(body);

            return body;

        } catch (HttpClientErrorException e) {
            System.out.println("===== Finalization API 호출 실패 =====");
            System.out.println("status = " + e.getStatusCode());
            System.out.println("responseBody = " + e.getResponseBodyAsString());
            throw new RuntimeException(
                    "Finalization API 호출 실패: " + e.getStatusCode() + " / " + e.getResponseBodyAsString(),
                    e
            );
        } catch (Exception e) {
            System.out.println("===== Finalization API 호출 실패 =====");
            e.printStackTrace();
            throw new RuntimeException("Finalization API 호출 실패: " + e.getMessage(), e);
        }
    }
 // 🔥 외부 책 목록 조회 (추가)
    public String getBooks() {
        String url = baseUrl.trim() + "/books";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey.trim());

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            System.out.println("===== Books 목록 조회 =====");
            System.out.println(response.getBody());

            return response.getBody();

        } catch (HttpClientErrorException e) {
            System.out.println("===== Books 목록 조회 실패 =====");
            System.out.println("status = " + e.getStatusCode());
            System.out.println("responseBody = " + e.getResponseBodyAsString());
            throw new RuntimeException(
                    "Books 목록 조회 실패: " + e.getStatusCode() + " / " + e.getResponseBodyAsString(),
                    e
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Books 목록 조회 실패: " + e.getMessage(), e);
        }
    }
}