<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>완성 성공!</title>
    <style>
        body {
            text-align: center;
            font-family: Arial, sans-serif;
            background: #f7f1e8;
            padding-top: 100px;
            color: #3b2f2f;
        }
        .box {
            background: white;
            display: inline-block;
            padding: 50px;
            border-radius: 30px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.1);
            min-width: 420px;
        }
        h1 {
            color: #d98d52;
            font-size: 50px;
            margin-bottom: 16px;
        }
        p {
            font-size: 16px;
            line-height: 1.7;
        }
        .uid-title {
            margin-top: 24px;
            margin-bottom: 12px;
            font-weight: bold;
        }
        .uid {
            background: #eee;
            padding: 10px 20px;
            border-radius: 10px;
            font-family: monospace;
            font-size: 20px;
            display: inline-block;
            min-width: 280px;
        }
        .btn-wrap {
            margin-top: 30px;
            display: flex;
            gap: 12px;
            justify-content: center;
            flex-wrap: wrap;
        }
        button {
            padding: 15px 30px;
            background: #3b2f2f;
            color: white;
            border: none;
            border-radius: 10px;
            cursor: pointer;
            font-size: 15px;
        }
        .sub-btn {
            background: #d98d52;
        }
        .order-box {
            margin-top: 28px;
            padding: 20px;
            border-radius: 16px;
            background: #faf7f2;
            border: 1px solid #eadfce;
            display: none;
            text-align: left;
        }
        .row {
            margin-top: 8px;
        }
        .label {
            font-weight: bold;
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
                        "<div class='row'><span class='label'>orderUid:</span> " + escapeHtml(result.orderUid || "") + "</div>" +
                        "<div class='row'><span class='label'>상태:</span> " + escapeHtml(result.orderStatusDisplay || "") + "</div>" +
                        "<div class='row'><span class='label'>총 금액:</span> " + escapeHtml(String(result.totalAmount || "")) + "</div>" +
                        "<div class='row'><span class='label'>bookUid:</span> " + escapeHtml(result.bookUid || "") + "</div>";
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
            <button class="sub-btn" onclick="createOrder()">주문 생성하기</button>
            <button onclick="location.href='/test/book'">새로운 책 만들기</button>
        </div>

        <div id="orderBox" class="order-box"></div>
    </div>
</body>
</html>