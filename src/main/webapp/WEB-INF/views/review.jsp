<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>책 미리보기</title>

    <style>
        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background: #f3efe9;
            color: #3b2f2f;
        }

        .wrap {
            max-width: 1100px;
            margin: 0 auto;
            padding: 40px 20px 80px;
        }

        .header {
            text-align: center;
            margin-bottom: 30px;
        }

        .header h1 {
            margin: 0;
            font-size: 34px;
        }

        .book-meta {
            text-align: center;
            color: #7a675c;
            margin-bottom: 26px;
            font-size: 14px;
        }

        .preview-stack {
            display: flex;
            flex-direction: column;
            gap: 28px;
        }

        .page-card {
            background: #fff;
            border-radius: 26px;
            padding: 22px;
            box-shadow: 0 10px 24px rgba(0, 0, 0, 0.06);
            border: 1px solid #eee4d8;
        }

        .page-inner {
            min-height: 560px;
            border-radius: 22px;
            overflow: hidden;
            display: grid;
        }

        .template-soft .page-inner {
            background: linear-gradient(135deg, #fcf7f1, #f7efe5);
        }

        .template-dark .page-inner {
            background: linear-gradient(135deg, #3b302c, #201916);
            color: #fff6ef;
        }

        .template-classic .page-inner {
            background: linear-gradient(135deg, #f7f2ea, #efe5d8);
        }

        .cover-layout {
            grid-template-columns: 1.1fr 0.9fr;
        }

        .memory-layout.image-left {
            grid-template-columns: 1fr 1fr;
        }

        .memory-layout.image-top {
            grid-template-columns: 1fr;
            grid-template-rows: 340px 1fr;
        }

        .dedication-layout {
            grid-template-columns: 1fr;
        }

        .page-image-box {
            position: relative;
            background: #e9ded1;
            display: flex;
            align-items: center;
            justify-content: center;
            overflow: hidden;
        }

        .page-image-box img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            display: block;
        }

        .page-text-box {
            padding: 48px 42px;
            display: flex;
            flex-direction: column;
            justify-content: center;
            box-sizing: border-box;
        }

        .page-kicker {
            font-size: 13px;
            letter-spacing: 2px;
            text-transform: uppercase;
            opacity: 0.7;
            margin-bottom: 16px;
        }

        .page-title {
            font-size: 34px;
            font-weight: bold;
            line-height: 1.3;
            margin-bottom: 16px;
            word-break: keep-all;
        }

        .page-subtitle {
            font-size: 18px;
            opacity: 0.85;
            line-height: 1.5;
            margin-bottom: 18px;
            word-break: keep-all;
        }

        .page-body {
            font-size: 17px;
            line-height: 1.9;
            white-space: pre-wrap;
            word-break: keep-all;
        }

        .page-footer {
            margin-top: 22px;
            font-size: 13px;
            opacity: 0.65;
        }

        .empty-image {
            font-size: 14px;
            color: #8c786a;
        }

        .template-dark .empty-image {
            color: #d7c2b4;
        }

        .btn-area {
            margin-top: 36px;
            display: flex;
            justify-content: center;
            gap: 10px;
            flex-wrap: wrap;
        }

        .btn-sub {
            background: #ead7c4;
            color: #5c4638;
            border: none;
            border-radius: 12px;
            padding: 12px 18px;
            cursor: pointer;
            font-size: 14px;
            font-weight: bold;
        }

        .loading {
            text-align: center;
            padding: 60px 0;
            font-size: 16px;
            color: #7a675c;
        }
    </style>

    <script>
        let bookProjectId = null;

        window.onload = function () {
            const params = new URLSearchParams(window.location.search);
            bookProjectId = params.get("bookProjectId");

            if (!bookProjectId) {
                alert("bookProjectId 없음");
                location.href = "/test/book";
                return;
            }

            loadPreview();
        };

        function loadPreview() {
            const previewStack = document.getElementById("previewStack");
            previewStack.innerHTML = "<div class='loading'>미리보기를 불러오는 중입니다.</div>";

            fetch("/api/book-preview/" + encodeURIComponent(bookProjectId))
                .then(response => {
                    if (!response.ok) {
                        throw new Error("미리보기 조회 실패");
                    }
                    return response.json();
                })
                .then(data => {
                    renderPreview(data);
                })
                .catch(error => {
                    previewStack.innerHTML = "<div class='loading'>에러: " + escapeHtml(error.message) + "</div>";
                });
        }

        function renderPreview(data) {
            const previewStack = document.getElementById("previewStack");
            const templateCode = (data.templateCode || "").toLowerCase();

            document.getElementById("metaText").innerText =
                "템플릿: " + (data.templateCode || "-") + " / 판형: " + (data.bookSpecCode || "-");

            previewStack.innerHTML = "";

            if (!data.pages || data.pages.length === 0) {
                previewStack.innerHTML = "<div class='loading'>생성된 페이지가 없습니다.</div>";
                return;
            }

            data.pages.forEach((page, index) => {
                const card = document.createElement("div");
                card.className = "page-card " + resolveTemplateClass(templateCode);

                const inner = document.createElement("div");
                inner.className = "page-inner " + resolveLayoutClass(page, index, templateCode);

                inner.innerHTML = buildPageHtml(page, index, data, templateCode);

                card.appendChild(inner);
                previewStack.appendChild(card);
            });
        }

        function resolveTemplateClass(templateCode) {
            if (templateCode.includes("dark") || templateCode.includes("night") || templateCode.includes("black")) {
                return "template-dark";
            }
            if (templateCode.includes("classic") || templateCode.includes("brown") || templateCode.includes("paper")) {
                return "template-classic";
            }
            return "template-soft";
        }

        function resolveLayoutClass(page, index, templateCode) {
            if (page.pageType === "COVER") {
                return "cover-layout";
            }
            if (page.pageType === "DEDICATION") {
                return "dedication-layout";
            }
            if (templateCode.includes("gallery") || templateCode.includes("photo")) {
                return "memory-layout image-top";
            }
            return "memory-layout image-left";
        }

        function buildPageHtml(page, index, data, templateCode) {
            if (page.pageType === "COVER") {
                return ""
                    + buildImageBox(page.imageUrl)
                    + "<div class='page-text-box'>"
                    + "<div class='page-kicker'>Memory Book</div>"
                    + "<div class='page-title'>" + escapeHtml(page.title || data.title || "") + "</div>"
                    + "<div class='page-subtitle'>" + escapeHtml(page.subtitle || data.subtitle || "") + "</div>"
                    + "<div class='page-body'>" + escapeHtml(data.petName || "") + "</div>"
                    + "<div class='page-footer'>" + escapeHtml(data.memorialDate || "") + "</div>"
                    + "</div>";
            }

            if (page.pageType === "DEDICATION") {
                return "<div class='page-text-box'>"
                    + "<div class='page-kicker'>Dedication</div>"
                    + "<div class='page-title'>" + escapeHtml(page.title || "") + "</div>"
                    + "<div class='page-body'>" + escapeHtml(page.text || "") + "</div>"
                    + "</div>";
            }

            if (resolveLayoutClass(page, index, templateCode).includes("image-top")) {
                return buildImageBox(page.imageUrl)
                    + "<div class='page-text-box'>"
                    + "<div class='page-kicker'>" + escapeHtml(page.chapterType || "MEMORY") + "</div>"
                    + "<div class='page-title'>" + escapeHtml(page.title || ("추억 " + index)) + "</div>"
                    + "<div class='page-body'>" + escapeHtml(page.text || "") + "</div>"
                    + "</div>";
            }

            return buildImageBox(page.imageUrl)
                + "<div class='page-text-box'>"
                + "<div class='page-kicker'>" + escapeHtml(page.chapterType || "MEMORY") + "</div>"
                + "<div class='page-title'>" + escapeHtml(page.title || ("추억 " + index)) + "</div>"
                + "<div class='page-body'>" + escapeHtml(page.text || "") + "</div>"
                + "</div>";
        }

        function buildImageBox(imageUrl) {
            if (imageUrl && imageUrl.trim() !== "") {
                return "<div class='page-image-box'><img src='" + escapeHtml(imageUrl) + "' alt='page image'></div>";
            }
            return "<div class='page-image-box'><div class='empty-image'>이미지가 없습니다.</div></div>";
        }

        function escapeHtml(text) {
            return String(text == null ? "" : text)
                .replaceAll("&", "&amp;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&#39;");
        }

        function goBack() {
            location.href = "/memory-add?bookProjectId=" + bookProjectId;
        }
    </script>
</head>
<body>

<div class="wrap">
    <div class="header">
        <h1>📖 책 미리보기</h1>
    </div>

    <div id="metaText" class="book-meta"></div>

    <div id="previewStack" class="preview-stack"></div>

    <div class="btn-area">
        <button type="button" class="btn-sub" onclick="goBack()">추억 추가로 돌아가기</button>
    </div>
</div>

</body>
</html>