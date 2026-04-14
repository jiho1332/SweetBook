<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>SweetBook 프로젝트</title>

    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>

    <style>
        body {
            font-family: Arial;
            margin: 30px;
            background: #f7f1e8;
            color: #3b2f2f;
        }
        input {
            margin: 5px;
            padding: 8px 10px;
            border: 1px solid #d8c9b8;
            border-radius: 8px;
        }
        button {
            padding: 8px 12px;
            margin-left: 5px;
            border: none;
            border-radius: 8px;
            background: #d98d52;
            color: white;
            cursor: pointer;
        }
        li {
            margin: 10px 0;
            list-style: none;
        }
        .box {
            border: 1px solid #e3d7c9;
            padding: 14px;
            margin-bottom: 12px;
            border-radius: 14px;
            background: #fffdf9;
        }
        .sub-btn {
            background: #ead7c4;
            color: #5c4638;
        }
        h1, h3 {
            margin-bottom: 14px;
        }
    </style>
</head>

<body>

<h1>📚 SweetBook 프로젝트</h1>

<h3>프로젝트 목록</h3>
<ul id="projectList"></ul>

<script>
    $(document).ready(function () {
        loadProjects();
    });

    function loadProjects() {
        $.ajax({
            url: "/api/book-projects",
            type: "GET",
            success: function (data) {
                let html = "";

                if (!data || data.length === 0) {
                    html = "<li class='box'>저장된 프로젝트가 없습니다.</li>";
                    $("#projectList").html(html);
                    return;
                }

                data.forEach(function (project) {
                    html += "<li class='box'>";
                    html += "<b>프로젝트 제목:</b> " + (project.title || "없음") + "<br>";
                    html += "<b>표지 제목:</b> " + (project.coverTitle || "없음") + "<br>";
                    html += "<b>부제:</b> " + (project.coverSubtitle || "없음") + "<br>";
                    html += "<b>헌정 문구:</b> " + (project.dedicationText || "없음") + "<br>";
                    html += "<b>템플릿:</b> " + (project.templateCode || "없음") + "<br>";
                    html += "<b>판형:</b> " + (project.bookSpecCode || "없음") + "<br>";
                    html += "<b>상태:</b> " + (project.status || "없음") + "<br>";
                    html += "<b>SweetBook Book ID:</b> " + (project.sweetbookBookId || "없음") + "<br>";

                    html += "<button onclick='goBookPage(" + project.bookProjectId + ")'>📘 프로젝트 열기</button>";
                    html += "<button class='sub-btn' onclick='deleteProject(" + project.bookProjectId + ")'>삭제</button>";
                    html += "</li>";
                });

                $("#projectList").html(html);
            },
            error: function () {
                alert("프로젝트 목록 불러오기 실패");
            }
        });
    }

    function goBookPage(bookProjectId) {
        location.href = "/book_test.jsp?bookProjectId=" + bookProjectId;
    }

    function deleteProject(bookProjectId) {
        $.ajax({
            url: "/api/book-projects/" + bookProjectId,
            type: "DELETE",
            success: function () {
                alert("삭제 완료");
                loadProjects();
            },
            error: function () {
                alert("삭제 실패");
            }
        });
    }
</script>

</body>
</html>