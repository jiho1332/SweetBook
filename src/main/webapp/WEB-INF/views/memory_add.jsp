<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>추억 추가</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            background: #f7f1e8;
            color: #3b2f2f;
        }

        .wrap {
            max-width: 980px;
            margin: 0 auto;
            padding: 40px 20px 60px;
        }

        .hero {
            background: linear-gradient(135deg, #f5e6d3, #f8f3ec);
            border-radius: 24px;
            padding: 32px;
            margin-bottom: 24px;
            box-shadow: 0 10px 24px rgba(0, 0, 0, 0.06);
        }

        .hero h1 {
            margin: 0 0 10px;
            font-size: 30px;
        }

        .hero p {
            margin: 0;
            color: #6b5b53;
            line-height: 1.6;
        }

        .card {
            background: #fffdf9;
            border-radius: 20px;
            padding: 28px;
            margin-bottom: 24px;
            box-shadow: 0 8px 20px rgba(0, 0, 0, 0.05);
        }

        .section-title {
            margin: 0 0 20px;
            font-size: 22px;
        }

        .info-box, .result-box, .preview-box, .list-box {
            margin-top: 22px;
            padding: 16px;
            border-radius: 14px;
            background: #fff;
            border: 1px solid #eadfce;
        }

        .info-box {
            display: flex;
            flex-direction: column;
            gap: 10px;
        }

        .book-info-line {
            font-size: 18px;
            font-weight: bold;
        }

        .sub-info-line {
            color: #6e5b50;
            font-size: 15px;
        }

        .book-thumb {
            width: 180px;
            height: 180px;
            object-fit: cover;
            border-radius: 16px;
            border: 1px solid #e5d8c8;
            display: none;
            margin-top: 10px;
        }

        .grid {
            display: grid;
            grid-template-columns: 1fr;
            gap: 18px;
        }

        .row {
            display: flex;
            flex-direction: column;
        }

        label {
            margin-bottom: 8px;
            font-weight: bold;
            color: #5a463d;
        }

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

        input[type="file"] {
            padding: 10px;
        }

        textarea {
            min-height: 160px;
            resize: vertical;
        }

        .btn-area {
            margin-top: 22px;
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
        }

        button {
            border: none;
            border-radius: 12px;
            padding: 12px 18px;
            cursor: pointer;
            font-size: 14px;
            font-weight: bold;
        }

        .btn-main {
            background: #d98d52;
            color: white;
        }

        .btn-sub {
            background: #ead7c4;
            color: #5c4638;
        }

        .preview-box {
            background: #fcfaf6;
        }

        .preview-box img {
            max-width: 240px;
            max-height: 240px;
            border-radius: 14px;
            display: none;
            border: 1px solid #e5d8c8;
            object-fit: cover;
        }

        .hint {
            margin-top: 6px;
            font-size: 13px;
            color: #7a675c;
        }

        .memory-item {
            border: 1px solid #eadfce;
            border-radius: 14px;
            padding: 18px;
            background: #fcfaf6;
            margin-bottom: 14px;
        }

        .memory-item .order {
            font-size: 13px;
            color: #8b7566;
            margin-bottom: 10px;
        }

        .memory-item .title {
            font-size: 22px;
            font-weight: bold;
            margin-bottom: 12px;
        }

        .memory-item .comment {
            line-height: 1.7;
            white-space: pre-wrap;
            margin-top: 14px;
            color: #4d3b33;
        }

        .memory-item img {
            width: 180px;
            height: 180px;
            object-fit: cover;
            border-radius: 12px;
            border: 1px solid #e5d8c8;
            display: block;
            margin-top: 12px;
        }

        .result-box {
            display: none;
            white-space: pre-wrap;
        }
    </style>

    <script>
        let bookProjectId = null;
        let petId = null;
        let petName = "";
        let projectTitle = "";
        let currentProject = null;
        let currentPet = null;

        window.onload = function () {
            const params = new URLSearchParams(window.location.search);
            bookProjectId = params.get("bookProjectId");

            if (!bookProjectId) {
                alert("bookProjectId가 없습니다.");
                location.href = "/test/book";
                return;
            }

            const fileInput = document.getElementById("memoryImageFile");
            if (fileInput) {
                fileInput.addEventListener("change", previewMemoryImage);
            }

            loadBookProject();
        };

        function setResult(message) {
            const box = document.getElementById("result");
            if (!message || !message.trim()) {
                box.style.display = "none";
                box.innerText = "";
                return;
            }
            box.style.display = "block";
            box.innerText = message;
        }

        function previewMemoryImage(event) {
            const file = event.target.files[0];
            const previewImage = document.getElementById("memoryImagePreview");
            const previewText = document.getElementById("memoryImagePreviewText");

            if (!file) {
                previewImage.style.display = "none";
                previewImage.src = "";
                previewText.innerText = "선택된 사진이 없습니다.";
                return;
            }

            const reader = new FileReader();
            reader.onload = function (e) {
                previewImage.src = e.target.result;
                previewImage.style.display = "block";
                previewText.innerText = "선택한 추억 사진 미리보기";
            };
            reader.readAsDataURL(file);
        }

        function loadBookProject() {
            fetch("/api/book-projects/" + encodeURIComponent(bookProjectId))
                .then(response => {
                    if (!response.ok) {
                        throw new Error("책 프로젝트 조회 실패");
                    }
                    return response.json();
                })
                .then(project => {
                    currentProject = project;
                    petId = project.petId;
                    projectTitle = project.title || "";
                    document.getElementById("projectTitleText").innerText = projectTitle || "-";

                    if (!petId) {
                        throw new Error("petId가 없습니다.");
                    }

                    return loadPetInfo();
                })
                .then(() => {
                    loadMemoryList();
                })
                .catch(error => {
                    setResult("책 프로젝트 조회 실패: " + error.message);
                });
        }

        function loadPetInfo() {
            return fetch("/api/pets/" + encodeURIComponent(petId))
                .then(response => {
                    if (!response.ok) {
                        throw new Error("반려견 정보 조회 실패");
                    }
                    return response.json();
                })
                .then(pet => {
                    currentPet = pet;
                    petName = pet.name || "";
                    document.getElementById("petNameText").innerText = petName || "-";

                    const petThumb = document.getElementById("petThumb");
                    if (pet.profileImageUrl) {
                        petThumb.src = pet.profileImageUrl;
                        petThumb.style.display = "block";
                    } else {
                        petThumb.style.display = "none";
                        petThumb.src = "";
                    }
                })
                .catch(error => {
                    throw new Error("반려견 정보 조회 실패: " + error.message);
                });
        }

        function validateMemoryForm() {
            const comment = document.getElementById("memoryContent").value.trim();

            if (!petId) {
                alert("petId를 찾지 못했습니다.");
                return false;
            }
            if (!comment) {
                alert("코멘트를 입력해주세요.");
                return false;
            }
            return true;
        }

        function saveMemory() {
            if (!validateMemoryForm()) {
                return;
            }

            const formData = new FormData();
            formData.append("petId", petId);
            formData.append("content", document.getElementById("memoryContent").value.trim());

            const file = document.getElementById("memoryImageFile").files[0];
            if (file) {
                formData.append("file", file);
            }

            fetch("/api/memories", {
                method: "POST",
                body: formData
            })
                .then(response => {
                    if (!response.ok) {
                        return response.text().then(text => {
                            throw new Error(text || "추억 저장 실패");
                        });
                    }
                    return response.text();
                })
                .then(() => {
                    clearForm();
                    setResult("");
                    loadMemoryList();
                })
                .catch(error => {
                    setResult("추억 저장 실패: " + error.message);
                });
        }

        function clearForm() {
            document.getElementById("memoryContent").value = "";
            document.getElementById("memoryImageFile").value = "";

            const previewImage = document.getElementById("memoryImagePreview");
            previewImage.style.display = "none";
            previewImage.src = "";
            document.getElementById("memoryImagePreviewText").innerText = "선택된 사진이 없습니다.";
        }

        function loadMemoryList() {
            if (!petId) {
                return;
            }

            fetch("/api/memories/pet/" + encodeURIComponent(petId))
                .then(response => {
                    if (!response.ok) {
                        throw new Error("추억 목록 조회 실패");
                    }
                    return response.json();
                })
                .then(memories => {
                    const listBox = document.getElementById("memoryList");
                    const countText = document.getElementById("memoryCountText");
                    listBox.innerHTML = "";

                    if (!memories || memories.length === 0) {
                        countText.innerText = "0개";
                        listBox.innerHTML = "<div class='memory-item'>저장된 추억이 없습니다.</div>";
                        return;
                    }

                    countText.innerText = memories.length + "개";

                    memories.forEach((memory, index) => {
                        const div = document.createElement("div");
                        div.className = "memory-item";

                        let html = "";
                        html += "<div class='order'>장면 " + (index + 1) + "</div>";
                        html += "<div class='title'>추억 " + (index + 1) + "</div>";

                        if (memory.imageUrl) {
                            html += "<img src='" + escapeHtml(memory.imageUrl) + "' alt='추억 이미지'>";
                        }

                        html += "<div class='comment'>" + escapeHtml(memory.content || "") + "</div>";

                        div.innerHTML = html;
                        listBox.appendChild(div);
                    });
                })
                .catch(error => {
                    setResult("추억 목록 조회 실패: " + error.message);
                });
        }

        function completeBook() {
            if (!bookProjectId) {
                alert("bookProjectId가 없습니다.");
                return;
            }

            fetch("/api/books/book-projects/" + encodeURIComponent(bookProjectId) + "/apply-template", {
                method: "POST"
            })
                .then(response => {
                    if (!response.ok) {
                        return response.text().then(text => {
                            throw new Error(text || "책 생성 실패");
                        });
                    }
                    return response.json();
                })
                .then(result => {
                    console.log(result);

                    alert("책 생성 완료");

                    // 핵심 정보 출력
                    setResult(
                        "bookUid: " + result.bookUid + "\n" +
                        "적용 페이지 수: " + result.appliedCount + "\n" +
                        "상태: " + result.status
                    );
                })
                .catch(error => {
                    setResult("책 생성 실패: " + error.message);
                });
        }

        function escapeHtml(text) {
            return String(text)
                .replaceAll("&", "&amp;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&#39;");
        }
    </script>
</head>
<body>

<div class="wrap">
    <div class="hero">
        <h1>추억을 하나씩 담아보세요</h1>
        <p>
            사진과 코멘트를 차곡차곡 쌓으면,<br>
            이 추억들이 나중에 한 권의 책으로 이어집니다.
        </p>
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
                <div class="hint">추억 사진을 선택해주세요.</div>
            </div>

            <div class="row">
                <label for="memoryContent">코멘트</label>
                <textarea id="memoryContent" placeholder="사진에 담긴 추억을 적어주세요."></textarea>
            </div>

            <div class="row">
                <div class="preview-box">
                    <div id="memoryImagePreviewText" class="hint">선택된 사진이 없습니다.</div>
                    <img id="memoryImagePreview" alt="추억 사진 미리보기">
                </div>
            </div>
        </div>

        <div class="btn-area">
            <button type="button" class="btn-main" onclick="saveMemory()">추억 추가</button>
            <button type="button" class="btn-sub"
                    onclick="location.href='/review?bookProjectId=' + bookProjectId">
                미리보기 보러가기
            </button>
            <button type="button" class="btn-sub" onclick="completeBook()">완성하기</button>
        </div>

        <div id="result" class="result-box"></div>
    </div>

    <div class="card">
        <h2 class="section-title">📚 저장된 추억 목록</h2>
        <div id="memoryList" class="list-box"></div>
    </div>
</div>

</body>
</html>