<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>SweetBook 프로젝트</title>

    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>

    <style>
        body {
            font-family: Arial;
            margin: 30px;
        }
        input {
            margin: 5px;
            padding: 5px;
        }
        button {
            padding: 5px 10px;
            margin-left: 5px;
        }
        li {
            margin: 10px 0;
        }
        .box {
            border: 1px solid #ddd;
            padding: 10px;
            margin-bottom: 10px;
        }
    </style>
</head>

<body>

<h1>📚 SweetBook 프로젝트</h1>

<h3>프로젝트 생성</h3>

<input type="number" id="petId" placeholder="petId 입력">
<input type="text" id="title" placeholder="제목">
<input type="text" id="description" placeholder="설명">

<button onclick="createProject()">생성</button>

<hr>

<h3>로컬 프로젝트 목록</h3>
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

                data.forEach(function (project) {
                    html += "<li class='box'>";
                    html += "<b>제목:</b> " + project.title + "<br>";
                    html += "<b>설명:</b> " + (project.description || "없음") + "<br>";
                    html += "<b>상태:</b> " + project.status + "<br>";
                    html += "<b>petId:</b> " + (project.petId || "없음") + "<br>";

                    if (project.petId != null) {
                        html += "<button onclick='goBookPage(" + project.petId + ")'>📘 책 만들기</button>";
                    } else {
                        html += "<span style='color:red'>petId 없음 → 책 생성 불가</span>";
                    }

                    html += "<button onclick='deleteProject(" + project.bookProjectId + ")'>삭제</button>";
                    html += "</li>";
                });

                $("#projectList").html(html);
            },
            error: function () {
                alert("프로젝트 목록 불러오기 실패");
            }
        });
    }

    function goBookPage(petId) {
        localStorage.setItem("sweetbook_petId", petId);
        location.href = "/book_test.jsp?petId=" + petId;
    }

    function createProject() {
        const data = {
            memberId: 1,
            petId: $("#petId").val(),
            title: $("#title").val(),
            description: $("#description").val(),
            status: "DRAFT"
        };

        $.ajax({
            url: "/api/book-projects",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify(data),
            success: function () {
                alert("생성 완료");
                loadProjects();
            },
            error: function () {
                alert("생성 실패");
            }
        });
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