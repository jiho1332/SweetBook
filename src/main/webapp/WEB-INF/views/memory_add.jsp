<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>추억 추가</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 0; background: #f7f1e8; color: #3b2f2f; }
        .wrap { max-width: 980px; margin: 0 auto; padding: 40px 20px 60px; }
        .hero { background: linear-gradient(135deg, #f5e6d3, #f8f3ec); border-radius: 24px; padding: 32px; margin-bottom: 24px; box-shadow: 0 10px 24px rgba(0, 0, 0, 0.06); }
        .hero h1 { margin: 0 0 10px; font-size: 30px; }
        .hero p { margin: 0; color: #6b5b53; line-height: 1.6; }
        .card { background: #fffdf9; border-radius: 20px; padding: 28px; margin-bottom: 24px; box-shadow: 0 8px 20px rgba(0, 0, 0, 0.05); }
        .section-title { margin: 0 0 20px; font-size: 22px; }
        .info-box, .list-box { margin-top: 22px; padding: 16px; border-radius: 14px; background: #fff; border: 1px solid #eadfce; }
        .info-box { display: flex; flex-direction: column; gap: 10px; }
        .book-info-line { font-size: 18px; font-weight: bold; }
        .sub-info-line { color: #6e5b50; font-size: 15px; }
        .book-thumb { width: 180px; height: 180px; object-fit: cover; border-radius: 16px; border: 1px solid #e5d8c8; display: none; margin-top: 10px; }
        .grid { display: grid; grid-template-columns: 1fr; gap: 18px; }
        .row { display: flex; flex-direction: column; }
        label { margin-bottom: 8px; font-weight: bold; color: #5a463d; }
        input, textarea { padding: 12px 14px; border: 1px solid #dbcfc2; border-radius: 12px; background: white; font-size: 14px; outline: none; box-sizing: border-box; width: 100%; }
        textarea { min-height: 160px; resize: vertical; }
        .btn-area { margin-top: 22px; display: flex; gap: 10px; flex-wrap: wrap; }
        button { border: none; border-radius: 12px; padding: 12px 18px; cursor: pointer; font-size: 14px; font-weight: bold; }
        .btn-main { background: #d98d52; color: white; }
        .btn-sub { background: #ead7c4; color: #5c4638; }
        .preview-box { background: #fcfaf6; padding: 16px; border-radius: 14px; border: 1px solid #eadfce; margin-top: 10px; }
        .preview-box img { max-width: 240px; max-height: 240px; border-radius: 14px; display: none; border: 1px solid #e5d8c8; object-fit: cover; }
        .hint { margin-top: 6px; font-size: 13px; color: #7a675c; }
        .memory-item { border: 1px solid #eadfce; border-radius: 14px; padding: 18px; background: #fcfaf6; margin-bottom: 14px; }
        .memory-item .order { font-size: 13px; color: #8b7566; margin-bottom: 10px; }
        .memory-item .title { font-size: 22px; font-weight: bold; margin-bottom: 12px; }
        .memory-item .comment { line-height: 1.7; white-space: pre-wrap; margin-top: 14px; color: #4d3b33; }
        .memory-item img { width: 180px; height: 180px; object-fit: cover; border-radius: 12px; border: 1px solid #e5d8c8; display: block; margin-top: 12px; }
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
                    if (pet.profileImageUrl) {
                        const petThumb = document.getElementById("petThumb");
                        petThumb.src = pet.profileImageUrl;
                        petThumb.style.display = "block";
                    }
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
                        listBox.innerHTML = "<div class='memory-item'>저장된 추억이 없습니다.</div>";
                        return;
                    }

                    memories.forEach(function (m, i) {
                        let html = "<div class='memory-item'>";
                        html += "<div class='order'>장면 " + (i + 1) + "</div>";
                        html += "<div class='title'>추억 " + (i + 1) + "</div>";
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
        <h1>추억을 하나씩 담아보세요</h1>
        <p>사진과 코멘트를 차곡차곡 쌓으면 나중에 한 권의 책으로 이어집니다.</p>
    </div>

    <div class="card">
        <h2 class="section-title">📌 현재 책 정보</h2>
        <div class="info-box">
            <div class="book-info-line">반려견 이름: <span id="petNameText">-</span></div>
            <div class="book-info-line">책 제목: <span id="projectTitleText">-</span></div>
            <div class="sub-info-line">추가된 추억: <span id="memoryCountText">0개</span></div>
            <img id="petThumb" class="book-thumb" alt="대표 사진">
        </div>
    </div>

    <div class="card">
        <h2 class="section-title">📝 추억 추가</h2>
        <div class="grid">
            <div class="row">
                <label for="memoryImageFile">사진 추가</label>
                <input type="file" id="memoryImageFile" accept="image/*">
            </div>
            <div class="row">
                <label for="memoryContent">코멘트</label>
                <textarea id="memoryContent" placeholder="사진에 담긴 추억을 적어주세요."></textarea>
            </div>
            <div class="row">
                <div class="preview-box">
                    <div id="memoryImagePreviewText" class="hint">미리보기</div>
                    <img id="memoryImagePreview" alt="추억 사진 미리보기">
                </div>
            </div>
        </div>

        <div class="btn-area">
            <button type="button" class="btn-main" onclick="saveMemory()">추억 추가</button>
            <button type="button" class="btn-sub" onclick="goBackToBookInfo()">책 기본정보로 돌아가기</button>
            <button type="button" class="btn-sub" onclick="goReviewPage()">미리보기 보러가기</button>
        </div>
    </div>

    <div class="card">
        <h2 class="section-title">📚 저장된 추억 목록</h2>
        <div id="memoryList" class="list-box"></div>
    </div>
</div>
</body>
</html>