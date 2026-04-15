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
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class SweetBookApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${sweetbook.api.key}")
    private String apiKey;

    @Value("${sweetbook.api.base-url}")
    private String baseUrl;

    @Value("${app.base-url:http://localhost:8081}")
    private String appBaseUrl;

    public SweetBookApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public String getTemplates() {
        try {
            return restTemplate.exchange(
                    buildUrl("/templates"),
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    String.class
            ).getBody();
        } catch (Exception e) {
            throw new RuntimeException("템플릿 전체 조회 실패: " + e.getMessage(), e);
        }
    }

    public List<Map<String, String>> getTemplatesByBookSpecUid(String bookSpecUid) {
        try {
            String url = buildUrl("/templates?bookSpecUid=" + bookSpecUid + "&templateKind=content");
            String res = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    String.class
            ).getBody();

            JsonNode nodes = objectMapper.readTree(res).path("data").path("templates");
            List<Map<String, String>> result = new ArrayList<>();

            for (JsonNode n : nodes) {
                Map<String, String> row = new LinkedHashMap<>();
                row.put("templateUid", firstNotBlank(
                        n.path("templateUid").asText(),
                        n.path("uid").asText()
                ));
                row.put("templateName", firstNotBlank(
                        n.path("templateName").asText(),
                        n.path("name").asText()
                ));
                row.put("templateKind", firstNotBlank(
                        n.path("templateKind").asText(),
                        "content"
                ));
                row.put("bookSpecUid", firstNotBlank(
                        n.path("bookSpecUid").asText(),
                        bookSpecUid
                ));
                result.add(row);
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("판형별 템플릿 조회 실패: " + e.getMessage(), e);
        }
    }

    public String getBookSpecs() {
        try {
            return restTemplate.exchange(
                    buildUrl("/book-specs"),
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    String.class
            ).getBody();
        } catch (Exception e) {
            throw new RuntimeException("판형 조회 실패: " + e.getMessage(), e);
        }
    }

    public List<Map<String, String>> getNormalizedBookSpecs() {
        try {
            JsonNode root = objectMapper.readTree(getBookSpecs());
            JsonNode specsNode = root.path("data").isArray()
                    ? root.path("data")
                    : root.path("data").path("bookSpecs");

            List<Map<String, String>> result = new ArrayList<>();
            for (JsonNode node : specsNode) {
                String uid = firstNotBlank(
                        node.path("bookSpecUid").asText(),
                        node.path("uid").asText()
                );
                if (uid.isBlank()) {
                    continue;
                }

                Map<String, String> row = new LinkedHashMap<>();
                row.put("bookSpecUid", uid);
                row.put("name", firstNotBlank(
                        node.path("name").asText(),
                        node.path("title").asText(),
                        uid
                ));
                result.add(row);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("정규화된 판형 조회 실패: " + e.getMessage(), e);
        }
    }

    public String getBooks() {
        try {
            return restTemplate.exchange(
                    buildUrl("/books"),
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    String.class
            ).getBody();
        } catch (Exception e) {
            throw new RuntimeException("책 목록 조회 실패: " + e.getMessage(), e);
        }
    }

    public String createBook(BookProjectVO project) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("title", project.getTitle());
            body.put("bookSpecUid", project.getBookSpecUid());

            HttpHeaders headers = createAuthHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String res = restTemplate.postForEntity(
                    buildUrl("/books"),
                    new HttpEntity<>(body, headers),
                    String.class
            ).getBody();

            String bookUid = objectMapper.readTree(res).path("data").path("bookUid").asText();
            if (bookUid == null || bookUid.isBlank()) {
                throw new RuntimeException("bookUid가 응답에 없습니다.");
            }
            return bookUid;
        } catch (Exception e) {
            throw new RuntimeException("책 draft 생성 실패: " + e.getMessage(), e);
        }
    }

    public String addCover(String bookUid, BookProjectVO project, PetVO petVO) {
        if (petVO == null) {
            throw new IllegalArgumentException("pet 정보가 없습니다.");
        }
        if (petVO.getProfileImageUrl() == null || petVO.getProfileImageUrl().isBlank()) {
            throw new IllegalArgumentException("대표 이미지가 없습니다.");
        }

        try {
            String coverTemplateUid = resolveCoverTemplateUid(project.getBookSpecUid());
            ByteArrayResource coverImage =
                    downloadImageAsResource(normalizeImageUrl(petVO.getProfileImageUrl()));

            HttpHeaders headers = createAuthHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("templateUid", coverTemplateUid);
            body.add("parameters", buildCoverParametersJson(project, petVO));
            body.add("coverPhoto", coverImage);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    buildUrl("/books/" + bookUid + "/cover"),
                    new HttpEntity<>(body, headers),
                    String.class
            );

            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("표지 추가 실패: " + e.getMessage(), e);
        }
    }

    private String resolveCoverTemplateUid(String bookSpecUid) {
        try {
            String url = buildUrl("/templates?bookSpecUid=" + bookSpecUid + "&templateKind=cover");
            String res = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders()),
                    String.class
            ).getBody();

            JsonNode coverNodes = objectMapper.readTree(res).path("data").path("templates");
            if (!coverNodes.isArray() || coverNodes.isEmpty()) {
                throw new RuntimeException("표지 템플릿이 없습니다.");
            }

            String coverTemplateUid = firstNotBlank(
                    coverNodes.get(0).path("templateUid").asText(),
                    coverNodes.get(0).path("uid").asText()
            );

            if (coverTemplateUid.isBlank()) {
                throw new RuntimeException("표지 템플릿 UID를 찾지 못했습니다.");
            }

            return coverTemplateUid;
        } catch (Exception e) {
            throw new RuntimeException("표지 템플릿 조회 실패: " + e.getMessage(), e);
        }
    }

    public String addContent(String bookUid, BookProjectVO project, PetVO petVO, MemoryVO memoryVO) {
        try {
            HttpHeaders headers = createAuthHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("templateUid", project.getContentTemplateUid());
            body.add("parameters", buildContentParametersJson(project, petVO, memoryVO));

            if (memoryVO.getImageUrl() != null && !memoryVO.getImageUrl().isBlank()) {
                ByteArrayResource img = downloadImageAsResource(normalizeImageUrl(memoryVO.getImageUrl()));
                body.add("photo1", img);
                body.add("photo", img);
            }

            ResponseEntity<String> response = restTemplate.postForEntity(
                    buildUrl("/books/" + bookUid + "/contents"),
                    new HttpEntity<>(body, headers),
                    String.class
            );

            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException(
                    "본문 추가 실패: memoryId=" + memoryVO.getMemoryId() + ", " + e.getMessage(),
                    e
            );
        }
    }

    public String finalizeBook(String bookUid) {
        try {
            HttpHeaders headers = createAuthHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            return restTemplate.postForEntity(
                    buildUrl("/books/" + bookUid + "/finalization"),
                    new HttpEntity<>("{}", headers),
                    String.class
            ).getBody();
        } catch (Exception e) {
            throw new RuntimeException("최종화 실패: " + e.getMessage(), e);
        }
    }

    private String buildCoverParametersJson(BookProjectVO project, PetVO petVO) {
        try {
            Map<String, Object> map = new LinkedHashMap<>();

            String petName = petVO != null ? nullSafe(petVO.getName()) : "친구";
            String coverTitle = firstNotBlank(project.getCoverTitle(), project.getTitle(), petName);
            String coverSubtitle = firstNotBlank(project.getCoverSubtitle(), "");
            String dedicationText = firstNotBlank(project.getDedicationText(), "");

            map.put("bookTitle", firstNotBlank(project.getTitle(), coverTitle));
            map.put("coverTitle", coverTitle);
            map.put("coverSubtitle", coverSubtitle);
            map.put("dedicationText", dedicationText);
            map.put("petName", petName);
            map.put("childName", petName);
            map.put("title", coverTitle);
            map.put("subtitle", coverSubtitle);
            map.put("text", dedicationText);
            map.put("content", dedicationText);

            map.put("schoolName", petName + "의 추억");
            map.put("volumeLabel", "MEMORY BOOK");
            map.put("periodText", firstNotBlank(coverSubtitle, "함께한 시간"));

            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            throw new RuntimeException("표지 parameters 생성 실패: " + e.getMessage(), e);
        }
    }

    private String buildContentParametersJson(BookProjectVO project, PetVO petVO, MemoryVO memoryVO) {
        try {
            Map<String, Object> map = new LinkedHashMap<>();

            String petName = petVO != null ? nullSafe(petVO.getName()) : "친구";
            String text = nullSafe(memoryVO.getContent());
            String title = firstNotBlank(memoryVO.getTitle(), petName);

            map.put("bookTitle", firstNotBlank(project.getTitle(), title));
            map.put("coverTitle", firstNotBlank(project.getCoverTitle(), project.getTitle(), petName));
            map.put("coverSubtitle", firstNotBlank(project.getCoverSubtitle(), ""));
            map.put("dedicationText", firstNotBlank(project.getDedicationText(), ""));

            map.put("petName", petName);
            map.put("childName", petName);
            map.put("schoolName", "우리집");

            map.put("title", title);
            map.put("text", text);
            map.put("content", text);
            map.put("diaryText", text);
            map.put("memoryTitle", title);
            map.put("memoryText", text);
            map.put("chapterType", firstNotBlank(memoryVO.getChapterType(), "DAILY"));
            map.put("displayOrder", memoryVO.getDisplayOrder() == null ? 1 : memoryVO.getDisplayOrder());

            LocalDateTime now = LocalDateTime.now();
            map.put("year", String.valueOf(now.getYear()));
            map.put("month", String.valueOf(now.getMonthValue()));
            map.put("date", String.valueOf(now.getDayOfMonth()));
            map.put("dayLabel", "일");
            map.put("recordedAt", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            throw new RuntimeException("본문 parameters 생성 실패: " + e.getMessage(), e);
        }
    }

    private ByteArrayResource downloadImageAsResource(String imageUrl) {
        try {
            byte[] bytes = restTemplate.getForObject(imageUrl, byte[].class);

            String filename = "image.jpg";
            if (imageUrl != null) {
                String lower = imageUrl.toLowerCase(Locale.ROOT);
                if (lower.endsWith(".png")) {
                    filename = "image.png";
                } else if (lower.endsWith(".jpeg")) {
                    filename = "image.jpeg";
                } else if (lower.endsWith(".jpg")) {
                    filename = "image.jpg";
                }
            }

            String finalFilename = filename;

            return new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return finalFilename;
                }
            };
        } catch (Exception e) {
            throw new RuntimeException("이미지 다운로드 실패: " + imageUrl, e);
        }
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    private String normalizeImageUrl(String url) {
        if (url == null || url.isBlank()) {
            return url;
        }
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }
        return appBaseUrl + (url.startsWith("/") ? url : "/" + url);
    }

    private String buildUrl(String path) {
        return baseUrl + path;
    }

    private String firstNotBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) {
                return v.trim();
            }
        }
        return "";
    }

    private String nullSafe(String value) {
        return value == null ? "" : value.trim();
    }
    public String chargeSandboxCredit(int amount) {
        try {
            HttpHeaders headers = createAuthHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Idempotency-Key", "charge-" + UUID.randomUUID());

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("amount", amount);
            body.put("memo", "과제용 주문 테스트 충전");

            return restTemplate.postForEntity(
                    buildUrl("/credits/sandbox/charge"),
                    new HttpEntity<>(body, headers),
                    String.class
            ).getBody();
        } catch (Exception e) {
            throw new RuntimeException("Sandbox 충전 실패: " + e.getMessage(), e);
        }
    }

    public String createOrder(String bookUid) {
        try {
            HttpHeaders headers = createAuthHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Idempotency-Key", "order-" + UUID.randomUUID());

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("bookUid", bookUid);
            item.put("quantity", 1);

            Map<String, Object> shipping = new LinkedHashMap<>();
            shipping.put("recipientName", "홍길동");
            shipping.put("recipientPhone", "010-1234-5678");
            shipping.put("postalCode", "06101");
            shipping.put("address1", "서울시 강남구 테헤란로 123");
            shipping.put("address2", "4층 401호");
            shipping.put("memo", "과제 테스트 주문");

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("items", List.of(item));
            body.put("shipping", shipping);
            body.put("externalRef", "SWEETBOOK-DEMO-" + UUID.randomUUID());

            return restTemplate.postForEntity(
                    buildUrl("/orders"),
                    new HttpEntity<>(body, headers),
                    String.class
            ).getBody();
        } catch (Exception e) {
            throw new RuntimeException("주문 생성 실패: " + e.getMessage(), e);
        }
    }
}