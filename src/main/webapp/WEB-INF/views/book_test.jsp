<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>SweetBook 생성 테스트</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 30px;
        }

        h2, h3 {
            margin-bottom: 15px;
        }

        .row {
            margin-bottom: 15px;
        }

        label {
            display: inline-block;
            width: 140px;
            font-weight: bold;
        }

        select, input {
            padding: 6px;
            min-width: 320px;
        }

        button {
            padding: 8px 14px;
            cursor: pointer;
            margin-right: 8px;
        }

        .info {
            margin-bottom: 20px;
            color: #444;
        }

        .result-box {
            margin-top: 25px;
            padding: 12px;
            border: 1px solid #ddd;
            background-color: #fafafa;
            white-space: pre-wrap;
        }

        .list-box {
            margin-top: 25px;
            padding: 12px;
            border: 1px solid #ddd;
            background-color: #fff;
        }

        ul {
            padding-left: 20px;
        }

        li {
            margin-bottom: 10px;
        }
    </style>

    <script>
        window.onload = function () {
            const petId = resolvePetId();

            document.getElementById("petId").value = petId ? petId : "";
            document.getElementById("petIdText").innerText = petId ? petId : "-";

            loadTemplates();
            loadBookSpecs();
            loadExternalBooks();

            if (!petId) {
                document.getElementById("result").innerText =
                    "petId가 없습니다. project_list.jsp에서 이동하거나 아래에 직접 입력해주세요.";
            }
        };

        function getPetIdFromUrl() {
            const params = new URLSearchParams(window.location.search);
            return params.get("petId");
        }

        function resolvePetId() {
            const urlPetId = getPetIdFromUrl();
            if (urlPetId) {
                localStorage.setItem("sweetbook_petId", urlPetId);
                return urlPetId;
            }

            const savedPetId = localStorage.getItem("sweetbook_petId");
            if (savedPetId) {
                return savedPetId;
            }

            return "";
        }

        function updatePetId() {
            const petId = document.getElementById("petId").value.trim();
            document.getElementById("petIdText").innerText = petId ? petId : "-";

            if (petId) {
                localStorage.setItem("sweetbook_petId", petId);
                const newUrl = window.location.pathname + "?petId=" + encodeURIComponent(petId);
                history.replaceState(null, "", newUrl);
                document.getElementById("result").innerText = "petId가 설정되었습니다: " + petId;
            } else {
                localStorage.removeItem("sweetbook_petId");
                history.replaceState(null, "", window.location.pathname);
                document.getElementById("result").innerText = "petId가 비어 있습니다.";
            }
        }

        function getCurrentPetId() {
            const petId = document.getElementById("petId").value.trim();
            return petId;
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
                    const templateSelect = document.getElementById("templateSelect");
                    templateSelect.innerHTML = "";

                    if (!data.data || !data.data.templates || data.data.templates.length === 0) {
                        const option = document.createElement("option");
                        option.value = "";
                        option.text = "템플릿 없음";
                        templateSelect.appendChild(option);
                        return;
                    }

                    data.data.templates.forEach(template => {
                        const option = document.createElement("option");
                        option.value = template.templateUid;
                        option.text = template.templateName + " (" + template.templateUid + ")";
                        templateSelect.appendChild(option);
                    });
                })
                .catch(error => {
                    document.getElementById("result").innerText =
                        "템플릿 목록 불러오기 실패: " + error.message;
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
                    const bookSpecSelect = document.getElementById("bookSpecSelect");
                    bookSpecSelect.innerHTML = "";

                    if (!data.data || data.data.length === 0) {
                        const option = document.createElement("option");
                        option.value = "";
                        option.text = "판형 없음";
                        bookSpecSelect.appendChild(option);
                        return;
                    }

                    data.data.forEach(spec => {
                        const option = document.createElement("option");
                        option.value = spec.bookSpecUid;
                        option.text = spec.name + " (" + spec.bookSpecUid + ")";
                        bookSpecSelect.appendChild(option);
                    });
                })
                .catch(error => {
                    document.getElementById("result").innerText =
                        "판형 목록 불러오기 실패: " + error.message;
                });
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

        function previewBook() {
            const petId = getCurrentPetId();
            const templateCode = document.getElementById("templateSelect").value;
            const bookSpecCode = document.getElementById("bookSpecSelect").value;

            if (!petId) {
                alert("petId를 입력하거나 project_list.jsp에서 이동해주세요.");
                return;
            }

            if (!templateCode || !bookSpecCode) {
                alert("템플릿과 판형을 선택해주세요.");
                return;
            }

            const url = "/api/books/projects/" + petId
                + "/preview?templateCode=" + encodeURIComponent(templateCode)
                + "&bookSpecCode=" + encodeURIComponent(bookSpecCode);

            fetch(url)
                .then(response => {
                    if (!response.ok) {
                        throw new Error("미리보기 조회 실패");
                    }
                    return response.json();
                })
                .then(data => {
                    document.getElementById("result").innerText =
                        JSON.stringify(data, null, 2);
                })
                .catch(error => {
                    document.getElementById("result").innerText =
                        "미리보기 실패: " + error.message;
                });
        }

        function submitBookCreate() {
            const petId = getCurrentPetId();
            const templateCode = document.getElementById("templateSelect").value;
            const bookSpecCode = document.getElementById("bookSpecSelect").value;

            if (!petId) {
                alert("petId를 입력하거나 project_list.jsp에서 이동해주세요.");
                return;
            }

            if (!templateCode || !bookSpecCode) {
                alert("템플릿과 판형을 선택해주세요.");
                return;
            }

            localStorage.setItem("sweetbook_petId", petId);

            const formData = new URLSearchParams();
            formData.append("templateCode", templateCode);
            formData.append("bookSpecCode", bookSpecCode);

            fetch("/api/books/projects/" + petId + "/create", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: formData.toString()
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
                    document.getElementById("result").innerText = data;
                    loadExternalBooks();
                })
                .catch(error => {
                    document.getElementById("result").innerText =
                        "책 생성 실패: " + error.message;
                });
        }
    </script>
</head>
<body>

<h2>📘 SweetBook 생성 테스트</h2>

<div class="info">
    현재 petId: <span id="petIdText">-</span>
</div>

<div class="row">
    <label for="petId">petId 입력</label>
    <input type="number" id="petId" placeholder="petId 입력">
    <button type="button" onclick="updatePetId()">petId 적용</button>
</div>

<div class="row">
    <label for="templateSelect">템플릿 선택</label>
    <select id="templateSelect"></select>
</div>

<div class="row">
    <label for="bookSpecSelect">판형 선택</label>
    <select id="bookSpecSelect"></select>
</div>

<div class="row">
    <button type="button" onclick="previewBook()">미리보기</button>
    <button type="button" onclick="submitBookCreate()">책 생성</button>
</div>

<div id="result" class="result-box">여기에 결과가 표시됩니다.</div>

<div class="list-box">
    <h3>외부 SweetBook 책 목록</h3>
    <ul id="externalBookList"></ul>
</div>

</body>
</html>