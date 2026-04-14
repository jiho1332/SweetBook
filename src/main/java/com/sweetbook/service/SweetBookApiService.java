package com.sweetbook.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sweetbook.vo.BookProjectVO;
import com.sweetbook.vo.MemoryVO;
import com.sweetbook.vo.PetVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class SweetBookApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${sweetbook.api.key}")
    private String apiKey;

    @Value("${sweetbook.api.base-url}")
    private String baseUrl;

    public SweetBookApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public String getBookSpecs() {
        String url = buildUrl("/book-specs");
        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders());

        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            System.out.println("===== book-specs =====");
            System.out.println(response.getBody());

            return response.getBody();
        } catch (HttpClientErrorException e) {
            printHttpError("book-specs 조회 실패", e);
            throw new RuntimeException(
                    "book-specs 조회 실패: " + e.getStatusCode() + " / " + e.getResponseBodyAsString(),
                    e
            );
        } catch (Exception e) {
            System.out.println("===== book-specs 조회 실패 =====");
            e.printStackTrace();
            throw new RuntimeException("book-specs 조회 실패: " + e.getMessage(), e);
        }
    }

    public String getTemplates() {
        String url = buildUrl("/templates");
        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders());

        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            System.out.println("===== templates =====");
            System.out.println(response.getBody());

            return response.getBody();
        } catch (HttpClientErrorException e) {
            printHttpError("templates 조회 실패", e);
            throw new RuntimeException(
                    "templates 조회 실패: " + e.getStatusCode() + " / " + e.getResponseBodyAsString(),
                    e
            );
        } catch (Exception e) {
            System.out.println("===== templates 조회 실패 =====");
            e.printStackTrace();
            throw new RuntimeException("templates 조회 실패: " + e.getMessage(), e);
        }
    }

    public String getBooks() {
        String url = buildUrl("/books");
        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders());

        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            System.out.println("===== books =====");
            System.out.println(response.getBody());

            return response.getBody();
        } catch (HttpClientErrorException e) {
            printHttpError("books 조회 실패", e);
            throw new RuntimeException(
                    "books 조회 실패: " + e.getStatusCode() + " / " + e.getResponseBodyAsString(),
                    e
            );
        } catch (Exception e) {
            System.out.println("===== books 조회 실패 =====");
            e.printStackTrace();
            throw new RuntimeException("books 조회 실패: " + e.getMessage(), e);
        }
    }

    public String createBook(BookProjectVO project) {
        validateProjectForCreate(project);

        String url = buildUrl("/books");

        HttpHeaders headers = createAuthHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("title", safe(project.getTitle()));
        requestBody.put("bookSpecUid", safe(project.getBookSpecUid()));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            System.out.println("===== Books API 요청 =====");
            System.out.println("url = " + url);
            System.out.println("requestBody = " + objectMapper.writeValueAsString(requestBody));

            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, request, String.class);

            String body = response.getBody();

            System.out.println("===== Books API 응답 =====");
            System.out.println(body);

            JsonNode root = objectMapper.readTree(body);
            ensureSuccessResponse(root, "Books API 실패 응답: " + body);

            JsonNode dataNode = root.path("data");
            JsonNode bookUidNode = dataNode.path("bookUid");

            if (bookUidNode.isMissingNode() || bookUidNode.asText().isBlank()) {
                throw new RuntimeException("Books API 응답에 bookUid가 없습니다. body=" + body);
            }

            String bookUid = bookUidNode.asText();

            System.out.println("===== Books API 생성 성공 =====");
            System.out.println("bookUid = " + bookUid);

            return bookUid;

        } catch (HttpClientErrorException e) {
            printHttpError("Books API 호출 실패", e);
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

    public String addContent(String bookUid, BookProjectVO project, PetVO petVO, MemoryVO memoryVO) {
        validateForAddContent(bookUid, project, memoryVO);

        String url = buildUrl("/books/" + bookUid + "/contents");

        HttpHeaders headers = createAuthHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("templateUid", safe(project.getContentTemplateUid()));
        body.add("parameters", buildParametersJson(project, petVO, memoryVO));

        if (!safe(memoryVO.getImageUrl()).isBlank()) {
            body.add("photo", downloadImageAsResource(memoryVO.getImageUrl()));
        }

        try {
            System.out.println("===== Contents API 요청 =====");
            System.out.println("url = " + url);
            System.out.println("templateUid = " + project.getContentTemplateUid());
            System.out.println("memoryId = " + memoryVO.getMemoryId());
            System.out.println("parameters = " + buildParametersJson(project, petVO, memoryVO));

            HttpEntity<MultiValueMap<String, Object>> request =
                    new HttpEntity<>(body, headers);

            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, request, String.class);

            String responseBody = response.getBody();

            System.out.println("===== Contents API 응답 =====");
            System.out.println(responseBody);

            return responseBody;

        } catch (HttpClientErrorException e) {
            printHttpError("Contents API 호출 실패", e);
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
        if (safe(bookUid).isBlank()) {
            throw new IllegalArgumentException("bookUid가 비어 있습니다.");
        }

        String url = buildUrl("/books/" + bookUid + "/finalization");

        HttpHeaders headers = createAuthHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>("{}", headers);

        try {
            System.out.println("===== Finalization API 요청 =====");
            System.out.println("url = " + url);

            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, request, String.class);

            String body = response.getBody();

            System.out.println("===== Finalization API 응답 =====");
            System.out.println(body);

            return body;

        } catch (HttpClientErrorException e) {
            printHttpError("Finalization API 호출 실패", e);
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

    private String buildParametersJson(BookProjectVO project, PetVO petVO, MemoryVO memoryVO) {
        try {
            Map<String, Object> parameters = new LinkedHashMap<>();
            parameters.put("bookTitle", safe(project.getTitle()));
            parameters.put("coverTitle", safe(project.getCoverTitle()));
            parameters.put("coverSubtitle", safe(project.getCoverSubtitle()));
            parameters.put("dedicationText", safe(project.getDedicationText()));
            parameters.put("petName", petVO != null ? safe(petVO.getName()) : "");
            parameters.put("memoryTitle", safe(memoryVO.getTitle()));
            parameters.put("memoryText", safe(memoryVO.getContent()));
            parameters.put("chapterType", safe(memoryVO.getChapterType()));
            parameters.put("displayOrder", memoryVO.getDisplayOrder());

            if (memoryVO.getRecordedAt() != null) {
                parameters.put(
                        "recordedAt",
                        memoryVO.getRecordedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                );
            } else {
                parameters.put("recordedAt", "");
            }

            return objectMapper.writeValueAsString(parameters);
        } catch (Exception e) {
            throw new RuntimeException("parameters JSON 생성 실패", e);
        }
    }

    private ByteArrayResource downloadImageAsResource(String imageUrl) {
        try (InputStream inputStream = new URL(imageUrl).openStream()) {
            byte[] bytes = inputStream.readAllBytes();

            return new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return "memory-image.jpg";
                }
            };
        } catch (Exception e) {
            throw new RuntimeException("이미지 다운로드 실패: " + imageUrl, e);
        }
    }

    private void validateProjectForCreate(BookProjectVO project) {
        if (project == null) {
            throw new IllegalArgumentException("project가 null입니다.");
        }
        if (safe(project.getTitle()).isBlank()) {
            throw new IllegalArgumentException("책 제목이 비어 있습니다.");
        }
        if (safe(project.getBookSpecUid()).isBlank()) {
            throw new IllegalArgumentException("bookSpecUid가 비어 있습니다.");
        }
    }

    private void validateForAddContent(String bookUid, BookProjectVO project, MemoryVO memoryVO) {
        if (safe(bookUid).isBlank()) {
            throw new IllegalArgumentException("bookUid가 비어 있습니다.");
        }
        if (project == null) {
            throw new IllegalArgumentException("project가 null입니다.");
        }
        if (safe(project.getContentTemplateUid()).isBlank()) {
            throw new IllegalArgumentException("contentTemplateUid가 비어 있습니다.");
        }
        if (memoryVO == null) {
            throw new IllegalArgumentException("memory가 null입니다.");
        }
    }

    private void ensureSuccessResponse(JsonNode root, String failMessage) {
        if (root == null || root.path("success").isMissingNode() || !root.path("success").asBoolean()) {
            throw new RuntimeException(failMessage);
        }
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(safe(apiKey));
        headers.setAccept(MediaType.parseMediaTypes("application/json, text/plain, */*"));
        return headers;
    }

    private String buildUrl(String path) {
        return safe(baseUrl) + path;
    }

    private void printHttpError(String title, HttpClientErrorException e) {
        System.out.println("===== " + title + " =====");
        System.out.println("status = " + e.getStatusCode());
        System.out.println("responseBody = " + e.getResponseBodyAsString());
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}