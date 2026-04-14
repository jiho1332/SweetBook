<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>추억 책 프로젝트 작성</title>

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
        }

        textarea {
            min-height: 110px;
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

        .info-box, .result-box, .list-box, .guide-box {
            margin-top: 22px;
            padding: 16px;
            border-radius: 14px;
            background: #fff;
            border: 1px solid #eadfce;
        }

        .info-item {
            margin: 6px 0;
            color: #6e5b50;
        }

        .result-box {
            white-space: pre-wrap;
            line-height: 1.5;
            overflow-x: auto;
        }

        .guide-box {
            background: #fcf8f2;
            color: #6a584f;
            line-height: 1.7;
        }

        .guide-box strong {
            color: #49362d;
        }

        ul {
            padding-left: 20px;
            margin: 0;
        }

        li {
            margin-bottom: 10px;
        }

        .hint {
            margin-top: 6px;
            font-size: 13px;
            color: #7a675c;
        }
    </style>

    <script>
        let savedProjectId = null;
        let allTemplates = [];
        let allBookSpecs = [];

        window.onload = function () {
            loadBookSpecs();
            loadTemplates();
            loadExternalBooks();
        };

        function setResult(message) {
            document.getElementById("result").innerText = message;
        }

        function setTemplateGuide(message) {
            document.getElementById("templateGuide").innerText = message;
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
                    bookSpecSelect.innerHTML = "";

                    if (!data.data || data.data.length === 0) {
                        const option = document.createElement("option");
                        option.value = "";
                        option.text = "판형 없음";
                        bookSpecSelect.appendChild(option);
                        return;
                    }

                    allBookSpecs = data.data;

                    data.data.forEach(spec => {
                        const option = document.createElement("option");
                        option.value = spec.bookSpecUid;
                        option.text = spec.name + " (" + spec.bookSpecUid + ")";
                        bookSpecSelect.appendChild(option);
                    });

                    bookSpecSelect.addEventListener("change", function () {
                        renderTemplateOptions();
                    });

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

            templateSelect.innerHTML = "";

            if (!selectedBookSpecCode) {
                const option = document.createElement("option");
                option.value = "";
                option.text = "먼저 판형을 선택해주세요";
                templateSelect.appendChild(option);
                setTemplateGuide("판형을 먼저 선택하면, 해당 판형에서 사용 가능한 안전한 내지 템플릿만 보여드립니다.");
                return;
            }

            if (!allTemplates || allTemplates.length === 0) {
                const option = document.createElement("option");
                option.value = "";
                option.text = "템플릿 없음";
                templateSelect.appendChild(option);
                setTemplateGuide("템플릿 목록을 아직 불러오지 못했습니다.");
                return;
            }

            const filteredTemplates = allTemplates.filter(template => {
                const sameBookSpec = (template.bookSpecUid || "") === selectedBookSpecCode;
                return sameBookSpec && isSafeTemplate(template);
            });

            if (filteredTemplates.length === 0) {
                const option = document.createElement("option");
                option.value = "";
                option.text = "선택 가능한 템플릿이 없습니다";
                templateSelect.appendChild(option);
                setTemplateGuide("현재 선택한 판형에서 안전하게 사용할 수 있는 템플릿이 없습니다.");
                return;
            }

            filteredTemplates.forEach(template => {
                const option = document.createElement("option");
                option.value = template.templateUid;
                option.text = template.templateName + " (" + template.bookSpecName + ")";
                templateSelect.appendChild(option);
            });

            setTemplateGuide(
                "현재는 오류 가능성이 높은 월시작 / monthHeader / dateA / dateB 템플릿은 숨기고, " +
                "gallery / photo / 빈내지 / 내지 계열만 보여드리고 있습니다."
            );
        }

        function loadExternalBooks() {
            fetch("/api/books/list")
                .then(response => {
                    if (!response.ok) {
                        throw new Error("외부 책 목록 조회 실패");
                    }
                    return response.json();
                })
                .then(data => {
                    const list = document.getElementById("externalBookList");
                    list.innerHTML = "";

                    if (!data.data || !data.data.books || data.data.books.length === 0) {
                        list.innerHTML = "<li>외부 책이 없습니다.</li>";
                        return;
                    }

                    data.data.books.forEach(book => {
                        const li = document.createElement("li");
                        li.innerText =
                            "제목: " + (book.title || "제목 없음")
                            + " / bookUid: " + (book.bookUid || "-")
                            + " / 상태: " + (book.status || "-");
                        list.appendChild(li);
                    });
                })
                .catch(error => {
                    document.getElementById("externalBookList").innerHTML =
                        "<li>외부 책 목록 불러오기 실패: " + error.message + "</li>";
                });
        }

        function collectProjectData() {
            return {
                petName: document.getElementById("petName").value.trim(),
                profileImageUrl: document.getElementById("profileImageUrl").value.trim(),
                memorialDate: document.getElementById("memorialDate").value.trim(),
                title: document.getElementById("title").value.trim(),
                coverTitle: document.getElementById("coverTitle").value.trim(),
                coverSubtitle: document.getElementById("coverSubtitle").value.trim(),
                dedicationText: document.getElementById("dedicationText").value.trim(),
                templateCode: document.getElementById("templateCode").value,
                bookSpecCode: document.getElementById("bookSpecCode").value,
                status: "DRAFT"
            };
        }

        function validateProjectData(project) {
            if (!project.petName) {
                alert("반려견 이름을 입력해주세요.");
                return false;
            }
            if (!project.title) {
                alert("책 제목을 입력해주세요.");
                return false;
            }
            if (!project.coverTitle) {
                alert("표지 제목을 입력해주세요.");
                return false;
            }
            if (!project.bookSpecCode) {
                alert("판형을 선택해주세요.");
                return false;
            }
            if (!project.templateCode) {
                alert("템플릿을 선택해주세요.");
                return false;
            }
            return true;
        }

        function saveBookProject() {
            const project = collectProjectData();

            if (!validateProjectData(project)) {
                return;
            }

            fetch("/api/book-projects", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(project)
            })
                .then(response => {
                    if (!response.ok) {
                        return response.text().then(text => {
                            throw new Error(text || "책 프로젝트 저장 실패");
                        });
                    }
                    return response.text();
                })
                .then(bookProjectId => {
                    savedProjectId = bookProjectId;
                    document.getElementById("savedProjectId").innerText = bookProjectId;
                    setResult("책 프로젝트 저장 완료. bookProjectId = " + bookProjectId);
                })
                .catch(error => {
                    setResult("책 프로젝트 저장 실패: " + error.message);
                });
        }

        function previewBook() {
            if (!savedProjectId) {
                alert("먼저 프로젝트 저장을 해주세요.");
                return;
            }

            fetch("/api/books/book-projects/" + savedProjectId + "/preview")
                .then(response => {
                    if (!response.ok) {
                        return response.text().then(text => {
                            throw new Error(text || "미리보기 조회 실패");
                        });
                    }
                    return response.json();
                })
                .then(data => {
                    setResult(JSON.stringify(data, null, 2));
                })
                .catch(error => {
                    setResult("미리보기 실패: " + error.message);
                });
        }

        function submitBookCreate() {
            if (!savedProjectId) {
                alert("먼저 프로젝트 저장을 해주세요.");
                return;
            }

            fetch("/api/books/book-projects/" + savedProjectId + "/create", {
                method: "POST"
            })
                .then(response => {
                    if (!response.ok) {
                        return response.text().then(text => {
                            throw new Error(text || "책 생성 실패");
                        });
                    }
                    return response.text();
                })
                .then(data => {
                    setResult(data);
                    loadExternalBooks();
                })
                .catch(error => {
                    setResult("책 생성 실패: " + error.message);
                });
        }
    </script>
</head>
<body>

<div class="wrap">
    <div class="hero">
        <h1>반려견과의 기억을 책으로 남겨보세요</h1>
        <p>
            반려견과 함께한 소중한 순간을 기록하고,<br>
            표지 문구와 헌정 문구를 담아 나만의 추억 책 프로젝트를 만들어보세요.
        </p>
    </div>

    <div class="card">
        <h2 class="section-title">📘 책 프로젝트 작성</h2>

        <div class="info-box">
            <div class="info-item">저장된 bookProjectId: <span id="savedProjectId">-</span></div>
        </div>

        <div class="guide-box">
            <strong>안내</strong><br>
            현재는 자동 조립 안정성을 위해, 선택한 판형에 맞는 안전한 내지 템플릿만 표시합니다.<br>
            복잡한 템플릿은 필수 이미지 리소스를 더 요구할 수 있어 임시로 제외했습니다.
        </div>

        <div class="grid">
            <div class="row">
                <label for="petName">반려견 이름</label>
                <input type="text" id="petName" placeholder="예: 하양이">
            </div>

            <div class="row">
                <label for="memorialDate">추모일</label>
                <input type="date" id="memorialDate">
                <div class="hint">없으면 비워두셔도 됩니다.</div>
            </div>

            <div class="row full">
                <label for="profileImageUrl">대표 사진 경로</label>
                <input type="file" id="profileImageFile" accept="image/*">
                <div class="hint">지금은 파일 업로드 대신 이미지 경로 문자열 방식으로 두는 상태입니다.</div>
            </div>

            <div class="row full">
                <label for="title">책 제목</label>
                <input type="text" id="title" placeholder="예: 하양이와 함께한 시간">
            </div>

            <div class="row">
                <label for="coverTitle">표지 제목</label>
                <input type="text" id="coverTitle" placeholder="표지에 들어갈 제목">
            </div>

            <div class="row">
                <label for="coverSubtitle">부제</label>
                <input type="text" id="coverSubtitle" placeholder="부제 입력">
            </div>

            <div class="row full">
                <label for="dedicationText">헌정 문구</label>
                <textarea id="dedicationText" placeholder="예: 우리 집에 와줘서 고마워."></textarea>
            </div>

            <div class="row">
                <label for="bookSpecCode">판형 선택</label>
                <select id="bookSpecCode"></select>
                <div class="hint">판형을 먼저 선택하면 해당 판형의 안전한 내지 템플릿만 표시됩니다.</div>
            </div>

            <div class="row">
                <label for="templateCode">템플릿 선택</label>
                <select id="templateCode"></select>
                <div class="hint" id="templateGuide">템플릿을 불러오는 중입니다.</div>
            </div>
        </div>

        <div class="btn-area">
            <button type="button" class="btn-sub" onclick="saveBookProject()">저장</button>
            <button type="button" class="btn-sub" onclick="previewBook()">미리보기</button>
            <button type="button" class="btn-main" onclick="submitBookCreate()">책 생성</button>
        </div>

        <div id="result" class="result-box">여기에 결과가 표시됩니다.</div>
    </div>

    <div class="card">
        <h2 class="section-title">📚 외부 SweetBook 책 목록</h2>
        <div class="guide-box">
            이 영역은 현재 프로젝트 DB 목록이 아니라, SweetBook Sandbox에 생성된 외부 책 목록입니다.<br>
            테스트로 생성된 draft 책도 여기 계속 보일 수 있습니다.
        </div>
        <div class="list-box">
            <ul id="externalBookList"></ul>
        </div>
    </div>
</div>

</body>
</html>