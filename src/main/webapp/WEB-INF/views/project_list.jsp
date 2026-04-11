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
    </style>
</head>

<body>

<h1>📚 SweetBook 프로젝트</h1>

<!-- 프로젝트 생성 -->
<h3>프로젝트 생성</h3>

<input type="text" id="title" placeholder="제목">
<input type="text" id="description" placeholder="설명">

<button onclick="createProject()">생성</button>

<hr>

<!-- 프로젝트 목록 -->
<h3>프로젝트 목록</h3>
<ul id="projectList"></ul>

<script>

    // 페이지 로드 시 실행
    $(document).ready(function () {
        loadProjects();
    });

    // 프로젝트 목록 조회
    function loadProjects() {
        $.ajax({
            url: "/projects",
            type: "GET",
            success: function (data) {

                let html = "";

                data.forEach(function (project) {

                    html += "<li>";
                    html += "<b>" + project.title + "</b>";
                    html += " (" + (project.description || "설명 없음") + ")";
                    html += " [상태: " + project.status + "]";
                    html += " <button onclick='deleteProject(" + project.projectId + ")'>삭제</button>";
                    html += "</li>";

                });

                $("#projectList").html(html);
            },
            error: function () {
                alert("프로젝트 목록 불러오기 실패");
            }
        });
    }

    // 프로젝트 생성
    function createProject() {

        const data = {
            memberId: 1,  // 테스트용
            title: $("#title").val(),
            description: $("#description").val(),
            status: "DRAFT"
        };

        $.ajax({
            url: "/projects",
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

    // 프로젝트 삭제
    function deleteProject(projectId) {

        $.ajax({
            url: "/projects/" + projectId,
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