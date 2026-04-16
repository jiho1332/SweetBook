USE sweetbook_db;

INSERT INTO member (email, password, name)
VALUES ('test@test.com', '1234', '테스터');

INSERT INTO pet (
    member_id,
    name,
    color,
    breed,
    relationship_label,
    memorial_date,
    pet_token,
    profile_image_url
) VALUES (
    1,
    '하양이',
    '흰색',
    '말티즈',
    '동생',
    '2025-12-01',
    'PET_1',
    NULL
);

INSERT INTO memory (
    pet_id,
    chapter_type,
    display_order,
    title,
    content,
    image_url,
    is_representative,
    recorded_at
) VALUES
(1, 'INTRO', 1, '처음 만난 날', '처음 우리 집에 왔던 날, 너무 작고 하얘서 눈을 뗄 수 없었습니다.', '/images/memory1.jpg', 'Y', '2024-01-10 15:00:00'),
(1, 'DAILY', 2, '산책을 좋아하던 아이', '매일 저녁 산책을 나가면 꼬리를 흔들며 먼저 문 앞에 서 있었습니다.', '/images/memory2.jpg', 'N', '2024-03-12 18:30:00'),
(1, 'ILLNESS', 3, '아프기 시작했던 시간', '조금씩 기운이 없어졌지만 끝까지 저희를 바라보던 눈빛이 기억납니다.', '/images/memory3.jpg', 'N', '2025-09-01 11:00:00'),
(1, 'FAREWELL', 4, '마지막 인사', '마지막 날, 고맙고 사랑한다고 꼭 전해주고 싶었습니다.', '/images/memory4.jpg', 'N', '2025-12-01 09:00:00'),
(1, 'AFTER', 5, '그 후의 시간', '여전히 집안 곳곳에서 네 흔적을 찾게 되지만 함께한 기억은 선명합니다.', '/images/memory5.jpg', 'N', '2025-12-20 20:00:00');

INSERT INTO book_project (
    pet_id,
    title,
    cover_title,
    cover_subtitle,
    dedication_text,
    template_code,
    book_spec_code,
    book_spec_uid,
    cover_template_uid,
    content_template_uid,
    sweetbook_book_id,
    book_uid,
    status,
    content_count
) VALUES (
    1,
    '하양이와 함께한 시간',
    '하양이와 함께한 시간',
    '나의 작은 동생을 기억하며',
    '우리 집에 와줘서 고마워.',
    '1lAx3XrHlrTt',
    'PHOTOBOOK_A5_SC',
    'PHOTOBOOK_A5_SC',
    NULL,
    '1lAx3XrHlrTt',
    NULL,
    NULL,
    'DRAFT',
    0
);

COMMIT;
