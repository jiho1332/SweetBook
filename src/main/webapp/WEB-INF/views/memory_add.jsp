<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>추억 추가 - 대시보드형</title>
    <style>
        body { 
            font-family: 'Pretendard', 'Malgun Gothic', sans-serif; 
            margin: 0; 
            background: #f7f1e8; 
            color: #3b2f2f; 
            line-height: 1.5;
        }

        .wrap { 
            max-width: 1200px; /* 전체 너비를 유지하거나 필요시 1000px로 줄여보세요 */
            margin: 0 auto; 
            padding: 30px 20px; 
        }

        .hero { 
            background: linear-gradient(135deg, #f5e6d3, #f8f3ec); 
            border-radius: 24px; 
            padding: 30px; 
            margin-bottom: 30px; 
            box-shadow: 0 10px 24px rgba(0, 0, 0, 0.04);
            text-align: center;
        }
        .hero h1 { margin: 0 0 10px; font-size: 28px; color: #d98d52; }
        .hero p { margin: 0; color: #6b5b53; }

        .main-container {
            display: grid;
            grid-template-columns: 350px 1fr; /* 사이드바를 살짝 줄여 목록 공간 확보 */
            gap: 40px; /* 간격을 넓혀서 자연스럽게 채워지도록 설정 */
            align-items: start;
        }

        .side-panel {
            position: sticky;
            top: 20px;
        }

        .card { 
            background: #fffdf9; 
            border-radius: 20px; 
            padding: 24px; 
            margin-bottom: 24px; 
            box-shadow: 0 8px 20px rgba(0, 0, 0, 0.05); 
            border: 1px solid #efe5d5;
        }

        .section-title { 
            margin: 0 0 18px; 
            font-size: 20px; 
            font-weight: bold;
            border-bottom: 2px solid #f5e6d3;
            padding-bottom: 8px;
        }

        .info-box { margin-bottom: 10px; }
        .book-info-line { font-size: 16px; margin-bottom: 8px; font-weight: 500; }
        .book-info-line span { color: #d98d52; font-weight: bold; }

        .row { display: flex; flex-direction: column; margin-bottom: 16px; }
        label { margin-bottom: 8px; font-weight: bold; color: #5a463d; font-size: 14px; }
        input, textarea { 
            padding: 12px 14px; 
            border: 1px solid #dbcfc2; 
            border-radius: 12px; 
            background: white; 
            font-size: 14px; 
            outline: none; 
            box-sizing: border-box; 
            width: 100%; 
        }
        textarea { min-height: 140px; resize: none; }

        .preview-box { 
            background: #fcfaf6; 
            border-radius: 12px; 
            border: 1px dashed #dbcfc2; 
            text-align: center;
            overflow: hidden;
            margin-top: 10px;
        }
        #memoryImagePreview { 
            width: 100%; 
            display: none; 
            object-fit: cover;
        }
        .hint { padding: 20px; color: #aaa; font-size: 13px; }

        .btn-area { display: flex; flex-direction: column; gap: 10px; }
        button { 
            border: none; 
            border-radius: 12px; 
            padding: 14px; 
            cursor: pointer; 
            font-size: 15px; 
            font-weight: bold; 
            transition: all 0.2s;
        }
        .btn-main { background: #d98d52; color: white; }
        .btn-main:hover { background: #c27a43; }
        .btn-sub { background: #ead7c4; color: #5c4638; }

        /* --- [수정된 목록 영역] --- */
        .list-section {
            width: 100%;
        }

        .memory-list-grid {
            display: grid;
            /* 목록이 1개일 때도 너무 작아지지 않게 너비 조정 */
            grid-template-columns: repeat(auto-fill, minmax(400px, 1fr)); 
            gap: 25px;
        }

        /* 만약 화면이 아주 크더라도 목록이 너무 퍼지지 않게 하려면 아래 주석 해제 */
        /* .memory-list-grid { max-width: 900px; } */

        .memory-item { 
            background: #fff;
            border: 1px solid #eadfce; 
            border-radius: 20px; 
            padding: 24px; 
            box-shadow: 0 10px 25px rgba(0,0,0,0.03);
            display: flex;
            flex-direction: column;
        }
        .memory-item .order { 
            font-size: 12px; 
            color: #b3a394; 
            font-weight: bold; 
            margin-bottom: 10px;
        }
        .memory-item img { 
            width: 100%; 
            /* 이미지 높이를 고정하여 균형을 맞춤 */
            aspect-ratio: 4 / 3;
            object-fit: cover; 
            border-radius: 14px; 
            border: 1px solid #f0e8dc;
            margin-bottom: 18px;
        }
        .memory-item .comment { 
            font-size: 16px; /* 폰트를 조금 키워 텍스트 영역을 채움 */
            line-height: 1.8; 
            white-space: pre-wrap; 
            color: #4d3b33; 
        }

        @media (max-width: 1100px) {
            .memory-list-grid { grid-template-columns: 1fr; }
        }

        @media (max-width: 900px) {
            .main-container { grid-template-columns: 1fr; }
            .side-panel { position: static; }
        }
    </style>

    <script>
        let bookProjectId = null;
        let petId = null;

        window.onload = function () {
            const params = new URLSearchParams(window.location.search);
            bookProjectId = params.get("bookProjectId");

            if (!bookProjectId) {
                alert("bookProjectId가 없습니다.");
                window.location.href = "/test/book";
                return;
            }
            document.getElementById("memoryImageFile").addEventListener("change", previewMemoryImage);
            loadBookProject();
        };

        function previewMemoryImage(event) {
            const file = event.target.files[0];
            const previewImage = document.getElementById("memoryImagePreview");
            const hint = document.querySelector(".hint");
            
            if (!file) {
                previewImage.style.display = "none";
                hint.style.display = "block";
                return;
            }
            const reader = new FileReader();
            reader.onload = function (e) {
                previewImage.src = e.target.result;
                previewImage.style.display = "block";
                hint.style.display = "none";
            };
            reader.readAsDataURL(file);
        }

        function loadBookProject() {
            fetch("/api/book-projects/" + encodeURIComponent(bookProjectId))
                .then(function(response) { return response.json(); })
                .then(function(project) {
                    petId = project.petId;
                    document.getElementById("projectTitleText").innerText = project.title || "-";
                    return fetch("/api/pets/" + encodeURIComponent(petId));
                })
                .then(function(res) { return res.json(); })
                .then(function(pet) {
                    document.getElementById("petNameText").innerText = pet.name || "-";
                    loadMemoryList();
                })
                .catch(function(error) { console.error("로딩 에러:", error); });
        }

        function saveMemory() {
            const content = document.getElementById("memoryContent").value.trim();
            if (!petId || !content) {
                alert("코멘트를 입력해주세요.");
                return;
            }

            const formData = new FormData();
            formData.append("petId", petId);
            formData.append("content", content);

            const file = document.getElementById("memoryImageFile").files[0];
            if (file) formData.append("file", file);

            fetch("/api/memories", { method: "POST", body: formData })
                .then(function(res) { return res.ok ? res.text() : res.text().then(function(t) { throw new Error(t); }); })
                .then(function() {
                    document.getElementById("memoryContent").value = "";
                    document.getElementById("memoryImageFile").value = "";
                    document.getElementById("memoryImagePreview").style.display = "none";
                    document.querySelector(".hint").style.display = "block";
                    alert("추억이 성공적으로 추가되었습니다!");
                    loadMemoryList();
                })
                .catch(function(err) { alert("추억 저장 실패: " + err.message); });
        }

        function loadMemoryList() {
            if (!petId) return;
            fetch("/api/memories/pet/" + encodeURIComponent(petId))
                .then(function(res) { return res.json(); })
                .then(function(memories) {
                    const listBox = document.getElementById("memoryList");
                    document.getElementById("memoryCountText").innerText = memories.length + "개";
                    listBox.innerHTML = "";

                    if (memories.length === 0) {
                        listBox.innerHTML = "<div class='card' style='grid-column: 1/-1; text-align:center;'>저장된 추억이 없습니다. 첫 추억을 남겨보세요!</div>";
                        return;
                    }

                    memories.forEach(function (m, i) {
                        let html = "<div class='memory-item'>";
                        html += "<span class='order'>#SCENE " + (i + 1) + "</span>";
                        if (m.imageUrl) {
                            html += "<img src='" + escapeHtml(m.imageUrl) + "' alt='추억 이미지'>";
                        }
                        html += "<div class='comment'>" + escapeHtml(m.content || "") + "</div>";
                        html += "</div>";
                        listBox.innerHTML += html;
                    });
                });
        }

        function escapeHtml(text) {
            return String(text).replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;").replaceAll("'", "&#39;");
        }

        function goBackToBookInfo() {
            if (bookProjectId) {
                window.location.href = "/test/book?bookProjectId=" + encodeURIComponent(bookProjectId) + "&locked=true";
            }
        }

        function goReviewPage() {
            if (bookProjectId) {
                window.location.href = "/review?bookProjectId=" + encodeURIComponent(bookProjectId);
            }
        }
    </script>
</head>
<body>

<div class="wrap">
    <div class="hero">
        <h1>나만의 추억 보관함</h1>
        <p>기록들이 모여 아름다운 책이 됩니다.</p>
    </div>

    <div class="main-container">
        <div class="side-panel">
            <div class="card">
                <h2 class="section-title">📌 소중한 기록 정보</h2>
                <div class="info-box">
                    <div class="book-info-line">반려견: <span id="petNameText">로딩 중...</span></div>
                    <div class="book-info-line">책 제목: <span id="projectTitleText">로딩 중...</span></div>
                    <div class="book-info-line">등록된 추억: <span id="memoryCountText">0개</span></div>
                </div>
            </div>

            <div class="card">
                <h2 class="section-title">📝 추억 남기기</h2>
                <div class="row">
                    <label for="memoryImageFile">📷 사진 선택</label>
                    <input type="file" id="memoryImageFile" accept="image/*">
                    <div class="preview-box">
                        <div class="hint">사진 미리보기</div>
                        <img id="memoryImagePreview" alt="미리보기">
                    </div>
                </div>
                <div class="row">
                    <label for="memoryContent">✍️ 코멘트 작성</label>
                    <textarea id="memoryContent" placeholder="여기에 추억을 자유롭게 적어주세요."></textarea>
                </div>
                <div class="btn-area">
                    <button type="button" class="btn-main" onclick="saveMemory()">추억 저장하기</button>
                    <button type="button" class="btn-sub" onclick="goReviewPage()">전체 미리보기</button>
                    <button type="button" class="btn-sub" onclick="goBackToBookInfo()">기본 정보 수정</button>
                </div>
            </div>
        </div>

        <div class="list-section">
            <h2 class="section-title">📚 저장된 추억 목록</h2>
            <div id="memoryList" class="memory-list-grid">
                </div>
        </div>
    </div>
</div>

</body>
</html>