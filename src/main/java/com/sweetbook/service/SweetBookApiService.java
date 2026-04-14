package com.sweetbook.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sweetbook.vo.BookRequestPreviewVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.HashMap;
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

            System.out.println("===== Books 목록 조회 =====");
            System.out.println(response.getBody());

            return response.getBody();
        } catch (HttpClientErrorException e) {
            printHttpError("Books 목록 조회 실패", e);
            throw new RuntimeException(
                    "Books 목록 조회 실패: " + e.getStatusCode() + " / " + e.getResponseBodyAsString(),
                    e
            );
        } catch (Exception e) {
            System.out.println("===== Books 목록 조회 실패 =====");
            e.printStackTrace();
            throw new RuntimeException("Books 목록 조회 실패: " + e.getMessage(), e);
        }
    }

    public String createBook(BookRequestPreviewVO preview) {
        validatePreviewForCreate(preview);

        String url = buildUrl("/books");

        HttpHeaders headers = createAuthHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("title", safe(preview.getTitle()));
        requestBody.put("bookSpecUid", safe(preview.getBookSpecCode()));

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

    public String addContents(String bookUid, BookRequestPreviewVO preview) {
        validatePreviewForContents(bookUid, preview);

        TemplateInfo templateInfo = loadTemplateInfo(preview.getTemplateCode());
        validateTemplateAndSpec(preview, templateInfo);

        String url = buildUrl("/books/" + bookUid + "/contents");

        HttpHeaders headers = createAuthHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("templateUid", safe(preview.getTemplateCode()));

        Map<String, Object> parameters = buildTemplateParameters(preview, templateInfo);
        String parametersJson;

        try {
            parametersJson = objectMapper.writeValueAsString(parameters);
            body.add("parameters", parametersJson);

            System.out.println("===== Contents API 요청 =====");
            System.out.println("url = " + url);
            System.out.println("templateUid = " + preview.getTemplateCode());
            System.out.println("templateName = " + templateInfo.templateName());
            System.out.println("bookSpecUid = " + templateInfo.bookSpecUid());
            System.out.println("parameters = " + parametersJson);

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

    private Map<String, Object> buildTemplateParameters(BookRequestPreviewVO preview, TemplateInfo templateInfo) {
        String templateName = safe(templateInfo.templateName()).toLowerCase();
        String title = safe(preview.getTitle());

        if (templateName.contains("알림장b_내지_fill".toLowerCase())) {
            return buildNotifyFillParameters(preview, title);
        }

        if (templateName.contains("gallery")) {
            return buildGalleryParameters(preview, title);
        }

        if (templateName.contains("photo")) {
            return buildPhotoParameters(preview, title);
        }

        return buildGenericTextImageParameters(preview, title);
    }

    private Map<String, Object> buildNotifyFillParameters(BookRequestPreviewVO preview, String title) {
        Map<String, Object> parameters = new HashMap<>();

        LocalDate today = LocalDate.now();

        parameters.put("year", String.valueOf(today.getYear()));
        parameters.put("month", String.format("%02d", today.getMonthValue()));
        parameters.put("date", String.format("%02d", today.getDayOfMonth()));
        parameters.put("bookTitle", title);

        parameters.put("weatherLabel1", "기억");
        parameters.put("weatherValue1", truncate(title, 20));

        parameters.put("mealLabel1", "추억");
        parameters.put("mealValue1", extractPageText(preview, 0, "소중한 하루"));

        parameters.put("napLabel1", "마음");
        parameters.put("napValue1", extractPageText(preview, 1, "늘 그리워"));

        parameters.put("pointColor", "#D98D52");

        addImageParameterIfExists(parameters, "image1", preview, 0);
        addImageParameterIfExists(parameters, "image2", preview, 1);
        addImageParameterIfExists(parameters, "image3", preview, 2);

        return parameters;
    }

    private Map<String, Object> buildGalleryParameters(BookRequestPreviewVO preview, String title) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("title", title);

        for (int i = 0; i < getPageCount(preview); i++) {
            addImageParameterIfExists(parameters, "image" + (i + 1), preview, i);
            addTextParameterIfExists(parameters, "text" + (i + 1), preview, i);
        }

        return parameters;
    }

    private Map<String, Object> buildPhotoParameters(BookRequestPreviewVO preview, String title) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("title", title);

        if (getPageCount(preview) > 0) {
            addImageParameterIfExists(parameters, "image1", preview, 0);
            addTextParameterIfExists(parameters, "text1", preview, 0);
        }

        if (getPageCount(preview) > 1) {
            addImageParameterIfExists(parameters, "image2", preview, 1);
            addTextParameterIfExists(parameters, "text2", preview, 1);
        }

        return parameters;
    }

    private Map<String, Object> buildGenericTextImageParameters(BookRequestPreviewVO preview, String title) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("title", title);

        for (int i = 0; i < getPageCount(preview); i++) {
            addTextParameterIfExists(parameters, "text" + (i + 1), preview, i);
            addImageParameterIfExists(parameters, "image" + (i + 1), preview, i);
        }

        if (parameters.size() == 1) {
            parameters.put("text1", "소중한 추억을 담았습니다.");
        }

        return parameters;
    }

    private void addTextParameterIfExists(Map<String, Object> parameters, String key,
                                          BookRequestPreviewVO preview, int pageIndex) {
        String text = extractPageText(preview, pageIndex, "");
        if (!text.isBlank()) {
            parameters.put(key, text);
        }
    }

    private void addImageParameterIfExists(Map<String, Object> parameters, String key,
                                           BookRequestPreviewVO preview, int pageIndex) {
        String imageUrl = extractPageImageUrl(preview, pageIndex);
        if (!imageUrl.isBlank()) {
            parameters.put(key, imageUrl);
        }
    }

    private String extractPageText(BookRequestPreviewVO preview, int pageIndex, String defaultValue) {
        try {
            if (preview.getPages() == null || preview.getPages().size() <= pageIndex || preview.getPages().get(pageIndex) == null) {
                return defaultValue;
            }

            String text = safe(preview.getPages().get(pageIndex).getText());
            return text.isBlank() ? defaultValue : truncate(text, 60);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String extractPageImageUrl(BookRequestPreviewVO preview, int pageIndex) {
        try {
            if (preview.getPages() == null || preview.getPages().size() <= pageIndex || preview.getPages().get(pageIndex) == null) {
                return "";
            }
            return safe(preview.getPages().get(pageIndex).getImageUrl());
        } catch (Exception e) {
            return "";
        }
    }

    private int getPageCount(BookRequestPreviewVO preview) {
        return preview.getPages() == null ? 0 : preview.getPages().size();
    }

    private TemplateInfo loadTemplateInfo(String templateUid) {
        try {
            String templateResponse = getTemplates();
            JsonNode root = objectMapper.readTree(templateResponse);

            JsonNode templatesNode = root.path("data").path("templates");
            if (!templatesNode.isArray()) {
                throw new RuntimeException("templates 응답 구조가 올바르지 않습니다.");
            }

            for (JsonNode templateNode : templatesNode) {
                String currentUid = templateNode.path("templateUid").asText("");
                if (templateUid.equals(currentUid)) {
                    String templateName = templateNode.path("templateName").asText("");
                    String bookSpecUid = templateNode.path("bookSpecUid").asText("");
                    String templateKind = templateNode.path("templateKind").asText("");
                    return new TemplateInfo(currentUid, templateName, bookSpecUid, templateKind);
                }
            }

            throw new RuntimeException("선택한 templateUid에 해당하는 템플릿을 찾지 못했습니다. templateUid=" + templateUid);

        } catch (Exception e) {
            throw new RuntimeException("템플릿 정보 조회 실패: " + e.getMessage(), e);
        }
    }

    private void validateTemplateAndSpec(BookRequestPreviewVO preview, TemplateInfo templateInfo) {
        if (!"content".equalsIgnoreCase(safe(templateInfo.templateKind()))) {
            throw new IllegalArgumentException(
                    "선택한 템플릿은 내지(content) 템플릿이 아닙니다. templateUid=" + templateInfo.templateUid()
            );
        }

        if (!safe(templateInfo.bookSpecUid()).equals(safe(preview.getBookSpecCode()))) {
            throw new IllegalArgumentException(
                    "선택한 템플릿과 판형이 맞지 않습니다. templateBookSpecUid="
                            + templateInfo.bookSpecUid()
                            + ", projectBookSpecCode="
                            + preview.getBookSpecCode()
            );
        }
    }

    private void validatePreviewForCreate(BookRequestPreviewVO preview) {
        if (preview == null) {
            throw new IllegalArgumentException("preview가 null입니다.");
        }
        if (safe(preview.getTitle()).isBlank()) {
            throw new IllegalArgumentException("책 제목이 비어 있습니다.");
        }
        if (safe(preview.getBookSpecCode()).isBlank()) {
            throw new IllegalArgumentException("판형 코드(bookSpecCode)가 비어 있습니다.");
        }
    }

    private void validatePreviewForContents(String bookUid, BookRequestPreviewVO preview) {
        if (safe(bookUid).isBlank()) {
            throw new IllegalArgumentException("bookUid가 비어 있습니다.");
        }
        if (preview == null) {
            throw new IllegalArgumentException("preview가 null입니다.");
        }
        if (safe(preview.getTemplateCode()).isBlank()) {
            throw new IllegalArgumentException("템플릿 코드(templateCode)가 비어 있습니다.");
        }
        if (safe(preview.getBookSpecCode()).isBlank()) {
            throw new IllegalArgumentException("판형 코드(bookSpecCode)가 비어 있습니다.");
        }
        if (safe(preview.getTitle()).isBlank()) {
            throw new IllegalArgumentException("책 제목(title)이 비어 있습니다.");
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

    private String truncate(String value, int maxLength) {
        String text = safe(value);
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }

    private record TemplateInfo(
            String templateUid,
            String templateName,
            String bookSpecUid,
            String templateKind
    ) {
    }
}