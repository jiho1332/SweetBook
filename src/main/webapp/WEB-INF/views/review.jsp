<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>책 미리보기</title>
    <style>
        body { margin: 0; font-family: Arial, sans-serif; background: #f3efe9; color: #3b2f2f; }
        .wrap { max-width: 1100px; margin: 0 auto; padding: 40px 20px 80px; }
        .header { text-align: center; margin-bottom: 30px; }
        .header h1 { margin: 0; font-size: 34px; }
        .book-meta { text-align: center; color: #7a675c; margin-bottom: 26px; font-size: 14px; line-height: 1.8; }
        .preview-stack { display: flex; flex-direction: column; gap: 28px; }
        .page-card { background: #fff; border-radius: 26px; padding: 22px; box-shadow: 0 10px 24px rgba(0, 0, 0, 0.06); border: 1px solid #eee4d8; }
        .page-inner { min-height: 560px; border-radius: 22px; overflow: hidden; display: grid; }
        .template-soft .page-inner { background: linear-gradient(135deg, #fcf7f1, #f7efe5); }
        .cover-layout { grid-template-columns: 1.1fr 0.9fr; }
        .memory-layout.image-left { grid-template-columns: 1fr 1fr; }
        .page-image-box { position: relative; background: #e9ded1; display: flex; align-items: center; justify-content: center; overflow: hidden; }
        .page-image-box img { width: 100%; height: 100%; object-fit: cover; display: block; }
        .page-text-box { padding: 48px 42px; display: flex; flex-direction: column; justify-content: center; box-sizing: border-box; }
        .page-kicker { font-size: 13px; letter-spacing: 2px; text-transform: uppercase; opacity: 0.7; margin-bottom: 16px; }
        .page-title { font-size: 34px; font-weight: bold; line-height: 1.3; margin-bottom: 16px; word-break: keep-all; }
        .page-body { font-size: 17px; line-height: 1.9; white-space: pre-wrap; word-break: keep-all; }
        .empty-image { font-size: 14px; color: #8c786a; }
        .btn-area { margin-top: 36px; display: flex; justify-content: center; gap: 10px; flex-wrap: wrap; }
        .btn-sub, .btn-main { border: none; border-radius: 12px; padding: 12px 18px; cursor: pointer; font-size: 14px; font-weight: bold; }
        .btn-sub { background: #ead7c4; color: #5c4638; }
        .btn-main { background: #d98d52; color: white; }
        .loading { text-align: center; padding: 60px 0; font-size: 16px; color: #7a675c; }
        .result-box { display: none; margin-top: 26px; padding: 22px; border-radius: 16px; background: #fff; border: 1px solid #eadfce; white-space: pre-wrap; line-height: 1.7; font-size: 16px; }
        .success { color: #2d6a4f; font-weight: bold; font-size: 20px; display: block; margin-bottom: 10px; }
        .error { color: #d9534f; font-weight: bold; font-size: 18px; }
    </style>

    <script>
        let bookProjectId = null;

        window.onload = function () {
            const params = new URLSearchParams(window.location.search);
            bookProjectId = params.get("bookProjectId");

            if (!bookProjectId) {
                alert("bookProjectId가 없습니다.");
                window.location.href = "/test/book";
                return;
            }

            loadPreview();
        };

        function loadPreview() {
            const previewStack = document.getElementById("previewStack");
            previewStack.innerHTML = "<div class='loading'>미리보기를 불러오는 중입니다...</div>";

            fetch("/api/books/book-projects/" + encodeURIComponent(bookProjectId) + "/preview")
                .then(function(res) {
                    return res.ok ? res.json() : res.text().then(function(t) { throw new Error(t); });
                })
                .then(function(data) {
                    renderPreview(data);
                })
                .catch(function(err) {
                    previewStack.innerHTML = "<div class='loading error'>에러: " + escapeHtml(err.message) + "</div>";
                });
        }

        function renderPreview(data) {
            const previewStack = document.getElementById("previewStack");
            document.getElementById("metaText").innerText =
                "판형: PHOTOBOOK_A4_SC / 템플릿: 58edh76I0rYa";

            previewStack.innerHTML = "";

            if (!data.pages || data.pages.length === 0) {
                previewStack.innerHTML = "<div class='loading'>생성된 페이지가 없습니다.</div>";
                return;
            }

            data.pages.forEach(function(page) {
                const card = document.createElement("div");
                card.className = "page-card template-soft";

                const inner = document.createElement("div");
                let layoutClass = "memory-layout image-left";

                if (page.chapterType === "COVER") {
                    layoutClass = "cover-layout";
                }

                inner.className = "page-inner " + layoutClass;
                inner.innerHTML = buildPageHtml(page, data);

                card.appendChild(inner);
                previewStack.appendChild(card);
            });
        }

        function buildPageHtml(page, data) {
            let html = buildImageBox(page.imageUrl);
            html += "<div class='page-text-box'>";

            if (page.chapterType === "COVER") {
                html += "<div class='page-kicker'>Memory Book</div>";
                html += "<div class='page-title'>" + escapeHtml(page.title || data.coverTitle || data.petName || "") + "</div>";
                html += "<div class='page-body'>" + escapeHtml(data.coverSubtitle || data.petName || "") + "</div>";
            } else {
                html += "<div class='page-kicker'>" + escapeHtml(page.chapterType || "MEMORY") + "</div>";
                html += "<div class='page-title'>" + escapeHtml(page.title || "추억") + "</div>";
                html += "<div class='page-body'>" + escapeHtml(page.text || "") + "</div>";
            }

            html += "</div>";
            return html;
        }

        function buildImageBox(imageUrl) {
            if (imageUrl && imageUrl.trim() !== "") {
                return "<div class='page-image-box'><img src='" + escapeHtml(imageUrl) + "' alt='image'></div>";
            }
            return "<div class='page-image-box'><div class='empty-image'>이미지가 없습니다.</div></div>";
        }

        function goBackToBookInfo() {
            if (bookProjectId) {
                window.location.href = "/test/book?bookProjectId=" + encodeURIComponent(bookProjectId) + "&locked=true";
            } else {
                window.location.href = "/test/book";
            }
        }

        function goBackToMemory() {
            if (bookProjectId) {
                window.location.href = "/memory-add?bookProjectId=" + encodeURIComponent(bookProjectId);
            } else {
                window.location.href = "/test/book";
            }
        }

        function completeBook() {
            if (!confirm("이대로 책을 완성하시겠습니까?")) {
                return;
            }

            const resultBox = document.getElementById("resultBox");
            resultBox.style.display = "block";
            resultBox.innerHTML = "<div class='loading'>스위트북 서버로 책을 연동하고 있습니다...</div>";

            document.getElementById("btnComplete").style.display = "none";

            fetch("/api/books/book-projects/" + encodeURIComponent(bookProjectId) + "/apply-template", {
                method: "POST"
            })
                .then(function(res) {
                    return res.ok ? res.json() : res.text().then(function(t) { throw new Error(t); });
                })
                .then(function(result) {
                    alert("책 제작 연동이 완료되었습니다.");
                    window.location.href =
                        "/book-completed?bookProjectId=" + encodeURIComponent(bookProjectId) +
                        "&bookUid=" + encodeURIComponent(result.bookUid || "");
                })
                .catch(function(err) {
                    document.getElementById("btnComplete").style.display = "inline-block";
                    resultBox.innerHTML = "<span class='error'>책 완성 실패</span><br><br>" + escapeHtml(err.message);
                });
        }

        function escapeHtml(text) {
            return String(text || "")
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
    <div class="header">
        <h1>📖 책 미리보기 및 완성</h1>
    </div>

    <div id="metaText" class="book-meta"></div>

    <div id="previewStack" class="preview-stack"></div>

    <div class="btn-area">
        <button type="button" class="btn-sub" onclick="goBackToBookInfo()">책 기본정보</button>
        <button type="button" class="btn-sub" onclick="goBackToMemory()">추억 추가로 돌아가기</button>
        <button type="button" id="btnComplete" class="btn-main" onclick="completeBook()">이대로 책 완성하기</button>
    </div>

    <div id="resultBox" class="result-box"></div>
</div>

</body>
</html>