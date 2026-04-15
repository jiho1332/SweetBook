<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>완성 성공!</title>
    <style>
    * {
        box-sizing: border-box;
    }

    body {
        margin: 0;
        min-height: 100vh;
        font-family: 'Pretendard', 'Noto Sans KR', Arial, sans-serif;
        background:
            radial-gradient(circle at top, rgba(255, 214, 170, 0.35), transparent 35%),
            linear-gradient(180deg, #f8f4ee 0%, #f3ede4 100%);
        color: #2f241f;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 32px 20px;
    }

    .box {
        width: 100%;
        max-width: 560px;
        background: rgba(255, 255, 255, 0.86);
        backdrop-filter: blur(8px);
        border: 1px solid rgba(217, 141, 82, 0.15);
        border-radius: 32px;
        box-shadow: 0 24px 60px rgba(59, 47, 47, 0.10);
        padding: 56px 48px 40px;
        text-align: center;
    }

    .icon {
        font-size: 52px;
        line-height: 1;
        margin-bottom: 12px;
    }

    h1 {
        margin: 0;
        color: #d98d52;
        font-size: 48px;
        font-weight: 800;
        letter-spacing: -1px;
    }

    .desc {
        margin: 18px 0 0;
        font-size: 18px;
        color: #5d4a41;
        line-height: 1.7;
    }

    .uid-section {
        margin-top: 34px;
        text-align: left;
        background: #fbf8f4;
        border: 1px solid #eee1d1;
        border-radius: 22px;
        padding: 22px 22px 20px;
    }

    .uid-title {
        margin: 0 0 12px;
        font-size: 15px;
        font-weight: 700;
        color: #6a554a;
    }

    .uid {
        width: 100%;
        background: #f1ece5;
        border: 1px solid #e5d8c8;
        border-radius: 16px;
        padding: 16px 18px;
        font-family: Consolas, Monaco, monospace;
        font-size: 25px;
        color: #2f241f;
        text-align: center;
        letter-spacing: 0.4px;
        word-break: break-all;
    }

    .btn-wrap {
        margin-top: 26px;
        display: flex;
        justify-content: center;
        gap: 12px;
        flex-wrap: wrap;
    }

    button {
        border: none;
        border-radius: 16px;
        padding: 16px 24px;
        min-width: 168px;
        font-size: 16px;
        font-weight: 700;
        cursor: pointer;
        transition: transform 0.16s ease, box-shadow 0.16s ease, opacity 0.16s ease;
    }

    button:hover {
        transform: translateY(-2px);
        box-shadow: 0 10px 22px rgba(59, 47, 47, 0.12);
    }

    .primary-btn {
        background: linear-gradient(135deg, #db8f55, #c97838);
        color: #fff;
    }

    .secondary-btn {
        background: #3b2f2f;
        color: #fff;
    }

    .order-box {
        margin-top: 26px;
        padding: 22px 24px;
        border-radius: 22px;
        background: #fffaf5;
        border: 1px solid #eddcca;
        box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8);
        display: none;
        text-align: left;
    }

    .order-heading {
        margin: 0 0 14px;
        font-size: 18px;
        font-weight: 800;
        color: #3b2f2f;
    }

    .row {
        margin-top: 10px;
        font-size: 16px;
        color: #453731;
        line-height: 1.6;
        word-break: break-all;
    }

    .label {
        display: inline-block;
        min-width: 88px;
        font-weight: 700;
        color: #8f5e3d;
    }

    @media (max-width: 640px) {
        .box {
            padding: 42px 20px 28px;
            border-radius: 24px;
        }

        h1 {
            font-size: 38px;
        }

        .desc {
            font-size: 16px;
        }

        .uid {
            font-size: 18px;
        }

        button {
            width: 100%;
        }

        .btn-wrap {
            gap: 10px;
        }
    }
</style>
    <script>
        let currentProjectId = null;
        let currentBookUid = null;

        window.onload = function() {
            const params = new URLSearchParams(window.location.search);
            currentProjectId = params.get("bookProjectId");
            const bookUidFromQuery = params.get("bookUid");

            if (bookUidFromQuery && bookUidFromQuery.trim() !== "") {
                currentBookUid = bookUidFromQuery;
                document.getElementById("uid").innerText = bookUidFromQuery;
                return;
            }

            if (!currentProjectId) {
                document.getElementById("uid").innerText = "bookProjectId 없음";
                return;
            }

            fetch("/api/book-projects/" + encodeURIComponent(currentProjectId))
                .then(function(r) {
                    return r.ok ? r.json() : Promise.reject(new Error("프로젝트 조회 실패"));
                })
                .then(function(project) {
                    currentBookUid = project.bookUid || "";
                    document.getElementById("uid").innerText = currentBookUid || "bookUid 없음";
                })
                .catch(function() {
                    document.getElementById("uid").innerText = "조회 실패";
                });
        };

        function createOrder() {
            if (!currentProjectId) {
                alert("bookProjectId가 없습니다.");
                return;
            }

            const orderBox = document.getElementById("orderBox");
            orderBox.style.display = "block";
            orderBox.innerHTML = "주문 생성 중입니다...";

            fetch("/api/books/book-projects/" + encodeURIComponent(currentProjectId) + "/create-order", {
                method: "POST"
            })
                .then(function(res) {
                    return res.ok ? res.json() : res.json().then(function(t) { throw new Error(t.message || "주문 생성 실패"); });
                })
                .then(function(result) {
                	orderBox.innerHTML =
                	    "<div class='order-heading'>주문 생성 결과</div>" +
                	    "<div class='row'><span class='label'>orderUid</span> " + escapeHtml(result.orderUid || "") + "</div>" +
                	    "<div class='row'><span class='label'>상태</span> " + escapeHtml(result.orderStatusDisplay || "") + "</div>" +
                	    "<div class='row'><span class='label'>총 금액</span> " + escapeHtml(String(result.totalAmount || "")) + "</div>" +
                	    "<div class='row'><span class='label'>bookUid</span> " + escapeHtml(result.bookUid || "") + "</div>";
                })
                .catch(function(err) {
                    orderBox.innerHTML = "주문 생성 실패: " + escapeHtml(err.message);
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
    <div class="box">
        <h1>🎉 책 완성!</h1>
        <p>소중한 추억이 한 권의 책으로 탄생했습니다.</p>
        <p class="uid-title">스위트북 고유 번호</p>
        <div class="uid" id="uid">불러오는 중...</div>

       <div class="btn-wrap">
    <button type="button" class="primary-btn" onclick="createOrder()">주문 생성하기</button>
    <button type="button" class="secondary-btn" onclick="location.href='/test/book'">새로운 책 만들기</button>
</div>

        <div id="orderBox" class="order-box"></div>
    </div>
</body>
</html>