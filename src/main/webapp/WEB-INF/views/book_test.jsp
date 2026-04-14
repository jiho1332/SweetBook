<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>추억 책 만들기</title>

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
            padding: 36px;
            margin-bottom: 24px;
            box-shadow: 0 10px 24px rgba(0, 0, 0, 0.06);
        }

        .hero h1 {
            margin: 0 0 10px;
            font-size: 34px;
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

        .grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 16px 20px;
        }

        .row {
            display: flex;
            flex-direction: column;
        }

        .row.full {
            grid-column: 1 / -1;
        }

        label {
            margin-bottom: 8px;
            font-weight: bold;
            color: #5a463d;
        }

        input, select, textarea {
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
            min-height: 130px;
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
            background: #8b6b5c;
            color: white;
        }

        .guide-box, .preview-box, .status-box, .result-box {
            margin-top: 22px;
            padding: 16px;
            border-radius: 14px;
            background: #fff;
            border: 1px solid #eadfce;
        }

        .guide-box {
            background: #fcf8f2;
            color: #6a584f;
            line-height: 1.8;
        }

        .guide-box strong {
            color: #49362d;
        }

        .hint {
            margin-top: 6px;
            font-size: 13px;
            color: #7a675c;
        }

        .preview-box {
            background: #fcfaf6;
        }

        .preview-box img {
            max-width: 220px;
            max-height: 220px;
            border-radius: 14px;
            display: none;
            border: 1px solid #e5d8c8;
            object-fit: cover;
        }

        .status-box {
            display: none;
            background: #fff9f2;
            color: #6b4a35;
        }

        .status-box strong {
            color: #4b3328;
        }

        .result-box {
            display: none;
            white-space: pre-wrap;
            line-height: 1.6;
            overflow-x: auto;
        }

        .locked-field {
            background: #f3eee7 !important;
            color: #7b685c;
            cursor: not-allowed;
        }

        .hidden {
            display: none !important;
        }
    </style>

    <script>
        let savedProjectId = null;
        let allTemplates = [];
        let allBookSpecs = [];
        let isLockedMode = false;

        window.onload = function () {
            const fileInput = document.getElementById("profileImageFile");
            if (fileInput) {
                fileInput.addEventListener("change", previewProfileImage);
            }

            const urlParams = new URLSearchParams(window.location.search);
            isLockedMode = urlParams.get("locked") === "true";

            loadBookSpecs();
            loadTemplates();
            bindInitialValuesFromQueryString();

            setTimeout(function () {
                applyLockedMode(isLockedMode);
            }, 300);
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

        function setTemplateGuide(message) {
            document.getElementById("templateGuide").innerText = message;
        }

        function showProjectStatus(message) {
            const box = document.getElementById("projectStatusBox");
            const text = document.getElementById("projectStatusText");

            if (!message || !message.trim()) {
                box.style.display = "none";
                text.innerText = "";
                return;
            }

            text.innerText = message;
            box.style.display = "block";
        }

        function previewProfileImage(event) {
            const file = event.target.files[0];
            const previewImage = document.getElementById("profileImagePreview");
            const previewText = document.getElementById("profileImagePreviewText");

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
                previewText.innerText = "선택한 대표 사진 미리보기";
            };
            reader.readAsDataURL(file);
        }

        function loadTemplates() {
            fetch("/api/books/templates")
                .then(response => {
                    if (!response.ok) {
                        throw new Error("템플릿 목록 조회 실패");
                    }
                    return response.json();
                })
                .then(data => {
                    if (!data.data || !data.data.templates) {
                        allTemplates = [];
                        renderTemplateOptions();
                        return;
                    }

                    allTemplates = data.data.templates;
                    renderTemplateOptions();
                })
                .catch(error => {
                    setResult("템플릿 목록 불러오기 실패: " + error.message);
                });
        }

        function loadBookSpecs() {
            fetch("/api/books/book-specs")
                .then(response => {
                    if (!response.ok) {
                        throw new Error("판형 목록 조회 실패");
                    }
                    return response.json();
                })
                .then(data => {
                    const bookSpecSelect = document.getElementById("bookSpecCode");
                    const urlParams = new URLSearchParams(window.location.search);
                    const selectedBookSpecCodeFromParam = urlParams.get("bookSpecCode") || "";

                    bookSpecSelect.innerHTML = "";

                    if (!data.data || data.data.length === 0) {
                        const option = document.createElement("option");
                        option.value = "";
                        option.text = "판형을 불러오지 못했습니다";
                        bookSpecSelect.appendChild(option);
                        return;
                    }

                    allBookSpecs = data.data;

                    data.data.forEach(spec => {
                        const option = document.createElement("option");
                        option.value = spec.bookSpecUid;
                        option.text = spec.name + " (" + spec.bookSpecUid + ")";
                        if (selectedBookSpecCodeFromParam === spec.bookSpecUid) {
                            option.selected = true;
                        }
                        bookSpecSelect.appendChild(option);
                    });

                    bookSpecSelect.onchange = function () {
                        renderTemplateOptions();
                    };

                    renderTemplateOptions();
                })
                .catch(error => {
                    setResult("판형 목록 불러오기 실패: " + error.message);
                });
        }

        function isSafeTemplate(template) {
            const templateKind = (template.templateKind || "").trim();
            const templateName = (template.templateName || "").trim();

            if (templateKind !== "content") {
                return false;
            }

            if (
                templateName.includes("월시작") ||
                templateName.includes("monthHeader") ||
                templateName.includes("dateA") ||
                templateName.includes("dateB")
            ) {
                return false;
            }

            if (
                templateName.includes("gallery") ||
                templateName.includes("photo") ||
                templateName.includes("빈내지") ||
                templateName === "내지" ||
                templateName.startsWith("내지 ")
            ) {
                return true;
            }

            if (templateName.startsWith("내지")) {
                return true;
            }

            return false;
        }

        function renderTemplateOptions() {
            const templateSelect = document.getElementById("templateCode");
            const selectedBookSpecCode = document.getElementById("bookSpecCode").value;
            const urlParams = new URLSearchParams(window.location.search);
            const selectedTemplateCodeFromParam = urlParams.get("templateCode") || "";

            templateSelect.innerHTML = "";

            if (!selectedBookSpecCode) {
                const option = document.createElement("option");
                option.value = "";
                option.text = "먼저 판형을 선택해주세요";
                templateSelect.appendChild(option);
                setTemplateGuide("판형을 먼저 고르면 그 판형에 맞는 내지 템플릿만 정리해서 보여드립니다.");
                return;
            }

            if (!allTemplates || allTemplates.length === 0) {
                const option = document.createElement("option");
                option.value = "";
                option.text = "템플릿을 불러오지 못했습니다";
                templateSelect.appendChild(option);
                setTemplateGuide("템플릿 목록을 다시 불러오는 중입니다.");
                return;
            }

            const filteredTemplates = allTemplates.filter(template => {
                const sameBookSpec = (template.bookSpecUid || "") === selectedBookSpecCode;
                return sameBookSpec && isSafeTemplate(template);
            });

            if (filteredTemplates.length === 0) {
                const option = document.createElement("option");
                option.value = "";
                option.text = "사용 가능한 템플릿이 없습니다";
                templateSelect.appendChild(option);
                setTemplateGuide("현재 선택한 판형에서는 바로 사용할 수 있는 내지 템플릿이 없습니다.");
                return;
            }

            filteredTemplates.forEach(template => {
                const option = document.createElement("option");
                option.value = template.templateUid;
                option.text = template.templateName + " (" + template.bookSpecName + ")";
                if (selectedTemplateCodeFromParam === template.templateUid) {
                    option.selected = true;
                }
                templateSelect.appendChild(option);
            });

            setTemplateGuide("현재 화면에서는 바로 적용 가능한 내지 템플릿만 정리해서 보여드리고 있습니다.");
        }

        function bindInitialValuesFromQueryString() {
            const urlParams = new URLSearchParams(window.location.search);

            const bookProjectId = urlParams.get("bookProjectId") || "";
            const petName = urlParams.get("petName") || "";
            const memorialDate = urlParams.get("memorialDate") || "";
            const title = urlParams.get("title") || "";
            const coverSubtitle = urlParams.get("coverSubtitle") || "";
            const dedicationText = urlParams.get("dedicationText") || "";
            const profileImageUrl = urlParams.get("profileImageUrl") || "";

            if (bookProjectId) {
                savedProjectId = bookProjectId;
            }

            document.getElementById("petName").value = petName;
            document.getElementById("memorialDate").value = memorialDate;
            document.getElementById("title").value = title;
            document.getElementById("coverSubtitle").value = coverSubtitle;
            document.getElementById("dedicationText").value = dedicationText;

            if (profileImageUrl) {
                const previewImage = document.getElementById("profileImagePreview");
                const previewText = document.getElementById("profileImagePreviewText");
                previewImage.src = profileImageUrl;
                previewImage.style.display = "block";
                previewText.innerText = "기존 대표 사진";
            }
        }

        function applyLockedMode(flag) {
            const textIds = ["petName", "memorialDate", "title", "coverSubtitle", "dedicationText"];
            const selectIds = ["bookSpecCode", "templateCode"];
            const fileInput = document.getElementById("profileImageFile");
            const saveBtn = document.getElementById("saveBtn");
            const editBtn = document.getElementById("editBtn");

            textIds.forEach(id => {
                const element = document.getElementById(id);
                if (!element) return;

                element.readOnly = flag;
                if (flag) {
                    element.classList.add("locked-field");
                } else {
                    element.classList.remove("locked-field");
                }
            });

            selectIds.forEach(id => {
                const element = document.getElementById(id);
                if (!element) return;

                element.disabled = flag;
                if (flag) {
                    element.classList.add("locked-field");
                } else {
                    element.classList.remove("locked-field");
                }
            });

            if (fileInput) {
                fileInput.disabled = flag;
                if (flag) {
                    fileInput.classList.add("locked-field");
                } else {
                    fileInput.classList.remove("locked-field");
                }
            }

            if (saveBtn) {
                if (flag) {
                    saveBtn.classList.add("hidden");
                } else {
                    saveBtn.classList.remove("hidden");
                }
            }

            if (editBtn) {
                if (flag) {
                    editBtn.classList.remove("hidden");
                } else {
                    editBtn.classList.add("hidden");
                }
            }

            if (flag) {
                showProjectStatus("기존 책 정보가 잠겨 있습니다. 수정이 필요하면 아래 수정하기 버튼을 눌러주세요.");
            } else {
                showProjectStatus("");
            }
        }

        function enableEditMode() {
            isLockedMode = false;
            applyLockedMode(false);
        }

        function validateProjectForm() {
            const petName = document.getElementById("petName").value.trim();
            const title = document.getElementById("title").value.trim();
            const templateCode = document.getElementById("templateCode").value;
            const bookSpecCode = document.getElementById("bookSpecCode").value;

            if (!petName) {
                alert("반려견 이름을 입력해주세요.");
                return false;
            }
            if (!title) {
                alert("책 제목을 입력해주세요.");
                return false;
            }
            if (!bookSpecCode) {
                alert("판형을 선택해주세요.");
                return false;
            }
            if (!templateCode) {
                alert("템플릿을 선택해주세요.");
                return false;
            }

            return true;
        }

        function saveBookProject() {
            if (!validateProjectForm()) {
                return;
            }

            const title = document.getElementById("title").value.trim();
            const selectedTemplateUid = document.getElementById("templateCode").value;
            const selectedBookSpecUid = document.getElementById("bookSpecCode").value;

            const formData = new FormData();

            if (savedProjectId) {
                formData.append("bookProjectId", savedProjectId);
            }

            formData.append("petName", document.getElementById("petName").value.trim());
            formData.append("memorialDate", document.getElementById("memorialDate").value.trim());
            formData.append("title", title);
            formData.append("coverTitle", title);
            formData.append("coverSubtitle", document.getElementById("coverSubtitle").value.trim());
            formData.append("dedicationText", document.getElementById("dedicationText").value.trim());

            formData.append("templateCode", selectedTemplateUid);
            formData.append("contentTemplateUid", selectedTemplateUid);

            formData.append("bookSpecCode", selectedBookSpecUid);
            formData.append("bookSpecUid", selectedBookSpecUid);

            formData.append("status", "DRAFT");

            const file = document.getElementById("profileImageFile").files[0];
            if (file) {
                formData.append("file", file);
            }

            fetch("/api/book-projects", {
                method: "POST",
                body: formData
            })
                .then(response => {
                    if (!response.ok) {
                        return response.text().then(text => {
                            throw new Error(text || "책 만들기 시작 실패");
                        });
                    }
                    return response.text();
                })
                .then(bookProjectId => {
                    savedProjectId = bookProjectId;
                    location.href = "/memory-add?bookProjectId=" + encodeURIComponent(bookProjectId);
                })
                .catch(error => {
                    setResult("책 만들기 시작 실패: " + error.message);
                });
        }
    </script>
</head>
<body>

<div class="wrap">
    <div class="hero">
        <h1>반려견과의 기억을 책으로 남겨보세요</h1>
        <p>
            반려견과 함께한 시간을 한 권의 책으로 정리하는 첫 단계입니다.<br>
            기본 정보를 먼저 저장한 뒤, 다음 화면에서 추억 사진과 코멘트를 차례대로 담아가게 됩니다.
        </p>
    </div>

    <div class="card">
        <h2 class="section-title">📘 책 기본 정보</h2>

        <div id="projectStatusBox" class="status-box">
            <strong>현재 상태</strong><br>
            <span id="projectStatusText"></span>
        </div>

        <div class="guide-box">
            <strong>안내</strong><br>
            이 화면에서는 반려견 정보와 책의 기본 구성을 먼저 정리합니다.<br>
            저장이 끝나면 다음 화면으로 이동해서 추억 사진과 코멘트를 여러 장면으로 차곡차곡 추가하게 됩니다.
        </div>

        <div class="grid">
            <div class="row">
                <label for="petName">반려견 이름</label>
                <input type="text" id="petName" placeholder="반려견 이름을 입력해주세요">
            </div>

            <div class="row">
                <label for="memorialDate">추모일</label>
                <input type="date" id="memorialDate">
                <div class="hint">비워두셔도 괜찮습니다.</div>
            </div>

            <div class="row full">
                <label for="profileImageFile">대표 사진 추가</label>
                <input type="file" id="profileImageFile" accept="image/*">
                <div class="hint">책의 대표 이미지로 사용할 사진을 선택해주세요.</div>
            </div>

            <div class="row full">
                <div class="preview-box">
                    <div id="profileImagePreviewText" class="hint">선택된 사진이 없습니다.</div>
                    <img id="profileImagePreview" alt="대표 사진 미리보기">
                </div>
            </div>

            <div class="row full">
                <label for="title">책 제목</label>
                <input type="text" id="title" placeholder="책 제목을 입력해주세요">
            </div>

            <div class="row full">
                <label for="coverSubtitle">부제</label>
                <input type="text" id="coverSubtitle" placeholder="부제를 입력해주세요">
            </div>

            <div class="row full">
                <label for="dedicationText">헌정 문구</label>
                <textarea id="dedicationText" placeholder="책에 담고 싶은 마음을 적어주세요"></textarea>
            </div>

            <div class="row">
                <label for="bookSpecCode">판형 선택</label>
                <select id="bookSpecCode"></select>
                <div class="hint">먼저 판형을 고르면 그에 맞는 템플릿을 바로 이어서 고를 수 있습니다.</div>
            </div>

            <div class="row">
                <label for="templateCode">템플릿 선택</label>
                <select id="templateCode"></select>
                <div class="hint" id="templateGuide">템플릿을 준비하고 있습니다.</div>
            </div>
        </div>

        <div class="btn-area">
            <button type="button" id="saveBtn" class="btn-main" onclick="saveBookProject()">저장하기</button>
            <button type="button" id="editBtn" class="btn-sub hidden" onclick="enableEditMode()">수정하기</button>
        </div>

        <div id="result" class="result-box"></div>
    </div>
</div>

</body>
</html>