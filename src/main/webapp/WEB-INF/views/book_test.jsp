<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<link href="https://fonts.googleapis.com/css2?family=Noto+Serif+KR:wght@400;600;700&display=swap" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Noto+Serif+KR:wght@400;700&display=swap" rel="stylesheet">
<head>
    <title>추억 책 만들기</title>

    <style>
/* 🔥 폰트 */
body {
    margin: 0;
    font-family: 'Pretendard', 'Apple SD Gothic Neo', sans-serif;
    background: #f5efe6;
    display: flex;
    justify-content: center;
}

/* 컨테이너 */
.container {
    width: 1280px;
    display: flex;
    margin-top: 20px;
    border-radius: 24px;
    overflow: hidden;
    box-shadow: 0 25px 50px rgba(0,0,0,0.08);
     align-items: stretch;
}

/* 왼쪽 */
.left {
    flex: 1;
    background:
        linear-gradient(rgba(245,230,211,0.75), rgba(245,230,211,0.75)),
        url('<%=request.getContextPath()%>/images/dog.png');

    background-size: cover;
    background-position: center;
    background-repeat: no-repeat;

    padding: 80px;
    display: flex;
    flex-direction: column;
    justify-content: center;
}

.left small {
    color: #d96c4b;
    font-weight: 600;
    margin-bottom: 15px;
}

/* 🔥 타이틀 */
.left h1 {
    font-family: 'Noto Serif KR', serif;
    font-size: 54px;
    line-height: 1.25;
    margin-bottom: 20px;
    color: #2e2a27;
    font-weight: 700;
    letter-spacing: -1px;
}

.left h1 span {
    color: #e2553f;
}

.left p {
    font-size: 15px;
    color: #6b5b53;
    margin-bottom: 35px;
    line-height: 1.7;
}

/* 🔥 리스트 */
.left ul {
    padding-left: 20px;
}

.left li {
    margin-bottom: 12px;
    color: #5c4a42;
    font-size: 15px;
    line-height: 1.6;
}

/* 오른쪽 */
.right {
    width: 420px;
    background: #f4f4f4;
    padding: 40px;
    
   max-height: 90vh;
    overflow-y: auto;
}

/* 🔥 라벨 */
label {
    display: block;
    margin-top: 18px;
    font-size: 13px;
    font-weight: 600;
    color: #4a3b33;
}

/* 🔥 입력창 (동글 핵심) */
input, textarea {
    width: 100%;
    padding: 14px;
    margin-top: 6px;
    border-radius: 22px;
    border: 1px solid #e5dcd3;
    font-size: 14px;
    background: #fdfaf6;
    transition: all 0.2s ease;
}

/* 포커스 효과 */
input:focus, textarea:focus {
    border-color: #e2553f;
    box-shadow: 0 0 0 3px rgba(226,85,63,0.12);
    outline: none;
}

/* placeholder */
::placeholder {
    color: #b7a79b;
}

/* textarea */
textarea {
    height: 130px;
}

/* 🔥 업로드 박스 */
.upload-box {
    border: 2px dashed #e3d6c8;
    border-radius: 22px;
    text-align: center;
    padding: 40px;
    margin-top: 12px;
    cursor: pointer;
    background: #fffaf5;
    position: relative;
    transition: 0.2s;
      z-index: 1;
}

.upload-box:hover {
    border-color: #e2553f;
    background: #fff3eb;
}

.upload-box input {
    position: absolute;
    width: 100%;
    height: 100%;
    opacity: 0;
    cursor: pointer;
     z-index: 2;s
}

.upload-box span {
    display: block;
    color: #9c8b7f;
    margin-top: 6px;
    font-size: 14px;
}

/* 미리보기 */
.preview-box img {
    width: 100%;
     max-height: 220px;
    margin-top: 12px;
    border-radius: 14px;
}

/* 🔥 버튼 */
.btn-area {
    margin-top: 30px;
    display: flex;
    gap: 12px;
    z-index: 3;
}

.btn-main {
    flex: 1;
    padding: 16px;
    border-radius: 999px;
    border: none;
    background: linear-gradient(135deg, #e2553f, #e86f52);
    color: white;
    font-weight: 600;
    cursor: pointer;
    font-size: 15px;
    box-shadow: 0 8px 18px rgba(226,85,63,0.25);
    transition: 0.2s;
}

.btn-main:hover {
    transform: translateY(-2px);
}

.btn-sub {
    flex: 1;
    padding: 16px;
    border-radius: 999px;
    border: none;
    background: #e0dedb;
    cursor: pointer;
    font-weight: 500;
}

/* 기타 */
.hidden { display: none !important; }

.locked-field {
    background: #f3eee7 !important;
    cursor: not-allowed;
}

 </style>

    <!-- 기존 로직 그대로 -->
    <script>
        let savedProjectId = null;

        window.onload = function () {
            const fileInput = document.getElementById("profileImageFile");
            if (fileInput) fileInput.addEventListener("change", previewProfileImage);

            const urlParams = new URLSearchParams(window.location.search);
            const isLockedMode = urlParams.get("locked") === "true";
            savedProjectId = urlParams.get("bookProjectId");

            document.getElementById("bookSpecCode").innerHTML =
                '<option value="PHOTOBOOK_A4_SC" selected>A4 포토북</option>';

            document.getElementById("templateCode").innerHTML =
                '<option value="58edh76I0rYa" selected>템플릿</option>';

            loadProjectIfNeeded().then(function () {
                applyLockedMode(isLockedMode);
            });
        };

        function previewProfileImage(event) {
            const file = event.target.files[0];
            const previewImage = document.getElementById("profileImagePreview");

            if (!file) {
                previewImage.style.display = "none";
                return;
            }

            const reader = new FileReader();
            reader.onload = function (e) {
                previewImage.src = e.target.result;
                previewImage.style.display = "block";
            };
            reader.readAsDataURL(file);
        }

        async function loadProjectIfNeeded() {
            if (!savedProjectId) return;

            const res = await fetch("/api/book-projects/" + savedProjectId);
            if (!res.ok) return;

            const project = await res.json();

            document.getElementById("title").value = project.title || "";
            document.getElementById("coverSubtitle").value = project.coverSubtitle || "";
            document.getElementById("dedicationText").value = project.dedicationText || "";
        }

        function saveBookProject() {
            const petName = document.getElementById("petName").value.trim();
            const title = document.getElementById("title").value.trim();

            if (!petName || !title) {
                alert("필수값 입력");
                return;
            }

            const formData = new FormData();

            formData.append("petName", petName);
            formData.append("memorialDate", document.getElementById("memorialDate").value);
            formData.append("title", title);
            formData.append("coverTitle", title);
            formData.append("coverSubtitle", document.getElementById("coverSubtitle").value);
            formData.append("dedicationText", document.getElementById("dedicationText").value);
            formData.append("bookSpecUid", "PHOTOBOOK_A4_SC");
            formData.append("contentTemplateUid", "58edh76I0rYa");

            const file = document.getElementById("profileImageFile").files[0];
            if (file) formData.append("file", file);

            fetch("/api/book-projects", { method: "POST", body: formData })
                .then(res => res.text())
                .then(id => location.href = "/memory-add?bookProjectId=" + id);
        }
    </script>
</head>

<body>

<div class="container">

    <!-- 왼쪽 -->
    <div class="left">
        <small>반려견 추억 보관소</small>

        <h1>
            소중한 추억을 <br>
            <span>책으로 남기다</span>
        </h1>

        <p>
            반려견과의 모든 순간을 아름다운 포토북으로 만들어
            영원히 간직하세요.
        </p>

        <ul>
            <li>✔ 손쉬운 사진 업로드</li>
            <li>✔ 감성적인 포토북 디자인</li>
            <li>✔ 고품질 인쇄 및 배송</li>
        </ul>
    </div>

    <!-- 오른쪽 -->
    <div class="right">

        <label>반려견 이름 *</label>
        <input id="petName" placeholder="예: 뽀삐">

        <label>추모일</label>
        <input type="date" id="memorialDate">

        <label>책 제목 *</label>
        <input id="title" placeholder="예: 우리 함께한 날들">

        <label>부제</label>
        <input id="coverSubtitle" placeholder="표지에 들어갈 부제">

        <label>헌정 문구</label>
        <textarea id="dedicationText" placeholder="마지막으로 전하고 싶은 메시지"></textarea>

        <label>대표 사진</label>
       <label class="upload-box">
    <input type="file" id="profileImageFile">
    <div class="upload-inner">
        <div>+</div>
        <span>사진을 선택하세요</span>
    </div>
</label>

        <div class="preview-box">
            <img id="profileImagePreview" style="display:none;">
        </div>

        <select id="bookSpecCode" class="hidden"></select>
        <select id="templateCode" class="hidden"></select>

        <div class="btn-area">
            <button class="btn-main" onclick="saveBookProject()">시작하기</button>
            <button class="btn-sub">초기화</button>
        </div>

    </div>

</div>

</body>
</html>