<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>추억 책 만들기</title>
    <style>
        body { font-family: 'Pretendard', Arial, sans-serif; margin: 0; background: #f7f1e8; color: #3b2f2f; }
        .wrap { max-width: 980px; margin: 0 auto; padding: 40px 20px 60px; }
        .hero { background: linear-gradient(135deg, #f5e6d3, #f8f3ec); border-radius: 24px; padding: 36px; margin-bottom: 24px; box-shadow: 0 10px 24px rgba(0,0,0,0.06); }
        .hero h1 { margin: 0 0 10px; font-size: 34px; }
        .hero p { margin: 0; color: #6b5b53; line-height: 1.6; }
        .card { background: #fffdf9; border-radius: 20px; padding: 28px; margin-bottom: 24px; box-shadow: 0 8px 20px rgba(0,0,0,0.05); }
        .section-title { margin: 0 0 20px; font-size: 22px; border-left: 5px solid #d98d52; padding-left: 12px; }
        .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px 20px; }
        .row { display: flex; flex-direction: column; }
        .row.full { grid-column: 1 / -1; }
        label { margin-bottom: 8px; font-weight: bold; color: #5a463d; }
        input, select, textarea { padding: 12px 14px; border: 1px solid #dbcfc2; border-radius: 12px; background: white; font-size: 14px; outline: none; box-sizing: border-box; width: 100%; }
        textarea { min-height: 130px; resize: vertical; }
        .btn-area { margin-top: 22px; display: flex; gap: 10px; }
        button { border: none; border-radius: 12px; padding: 14px 24px; cursor: pointer; font-size: 15px; font-weight: bold; transition: 0.2s; }
        .btn-main { background: #d98d52; color: white; }
        .btn-sub { background: #8b6b5c; color: white; }
        .preview-box { margin-top: 22px; padding: 16px; border-radius: 14px; background: #fff; border: 1px solid #eadfce; }
        .hint { margin-top: 6px; font-size: 13px; color: #7a675c; }
        .preview-box img { max-width: 220px; max-height: 220px; border-radius: 14px; display: none; border: 1px solid #e5d8c8; object-fit: cover; }
        .locked-field { background: #f3eee7 !important; color: #7b685c; cursor: not-allowed; }
        .hidden { display: none !important; }
    </style>

    <script>
        let savedProjectId = null;

        window.onload = function () {
            const fileInput = document.getElementById("profileImageFile");
            if (fileInput) fileInput.addEventListener("change", previewProfileImage);

            const urlParams = new URLSearchParams(window.location.search);
            const isLockedMode = urlParams.get("locked") === "true";
            savedProjectId = urlParams.get("bookProjectId");

            document.getElementById("bookSpecCode").innerHTML =
                '<option value="PHOTOBOOK_A4_SC" selected>A4 소프트커버 포토북 (PHOTOBOOK_A4_SC)</option>';

            document.getElementById("templateCode").innerHTML =
                '<option value="58edh76I0rYa" selected>내지 고정 템플릿</option>';

            loadProjectIfNeeded()
                .then(function() { applyLockedMode(isLockedMode); })
                .catch(function(e) { console.error("초기 로드 에러:", e); });
        };

        function previewProfileImage(event) {
            const file = event.target.files[0];
            const previewImage = document.getElementById("profileImagePreview");
            if (!file) {
                previewImage.style.display = "none";
                return;
            }
            const reader = new FileReader();
            reader.onload = function(e) {
                previewImage.src = e.target.result;
                previewImage.style.display = "block";
            };
            reader.readAsDataURL(file);
        }

        async function loadProjectIfNeeded() {
            if (!savedProjectId) return;

            const response = await fetch("/api/book-projects/" + encodeURIComponent(savedProjectId));
            if (!response.ok) return;

            const project = await response.json();
            document.getElementById("title").value = project.title || "";
            document.getElementById("coverSubtitle").value = project.coverSubtitle || "";
            document.getElementById("dedicationText").value = project.dedicationText || "";

            if (project.petId) {
                const petRes = await fetch("/api/pets/" + encodeURIComponent(project.petId));
                if (petRes.ok) {
                    const pet = await petRes.json();
                    document.getElementById("petName").value = pet.name || "";
                    document.getElementById("memorialDate").value = pet.memorialDate || "";
                    if (pet.profileImageUrl) {
                        const img = document.getElementById("profileImagePreview");
                        img.src = pet.profileImageUrl;
                        img.style.display = "block";
                    }
                }
            }
        }

        function saveBookProject() {
            const petName = document.getElementById("petName").value.trim();
            const title = document.getElementById("title").value.trim();

            if (!petName || !title) {
                alert("필수 항목(반려견 이름, 책 제목)을 모두 입력해주세요.");
                return;
            }

            const formData = new FormData();
            if (savedProjectId) formData.append("bookProjectId", savedProjectId);

            formData.append("petName", petName);
            formData.append("memorialDate", document.getElementById("memorialDate").value);
            formData.append("title", title);
            formData.append("coverTitle", title);
            formData.append("coverSubtitle", document.getElementById("coverSubtitle").value);
            formData.append("dedicationText", document.getElementById("dedicationText").value);
            formData.append("bookSpecUid", "PHOTOBOOK_A4_SC");
            formData.append("contentTemplateUid", "58edh76I0rYa");
            formData.append("status", "DRAFT");

            const file = document.getElementById("profileImageFile").files[0];
            if (file) formData.append("file", file);

            fetch("/api/book-projects", { method: "POST", body: formData })
                .then(function(res) {
                    return res.ok ? res.text() : res.text().then(function(t) { throw new Error(t); });
                })
                .then(function(id) {
                    window.location.href = "/memory-add?bookProjectId=" + encodeURIComponent(id);
                })
                .catch(function(err) {
                    alert("저장 실패: " + err.message);
                });
        }

        function applyLockedMode(flag) {
            if (!flag) return;
            document.querySelectorAll("input, select, textarea").forEach(function(el) {
                if (el.id !== "profileImageFile") {
                    el.readOnly = true;
                    if (el.tagName === "SELECT") el.disabled = true;
                    el.classList.add("locked-field");
                }
            });
            document.getElementById("saveBtn").classList.add("hidden");
            document.getElementById("editBtn").classList.remove("hidden");
        }

        function unlockEditMode() {
            if (!savedProjectId) return;
            window.location.href = "/test/book?bookProjectId=" + encodeURIComponent(savedProjectId);
        }
    </script>
</head>
<body>
<div class="wrap">
    <div class="hero">
        <h1>반려견과의 기억을 책으로 남겨보세요</h1>
        <p>기본 정보를 입력하고 저장하면 사진과 추억을 담는 다음 단계로 이동합니다.</p>
    </div>

    <div class="card">
        <h2 class="section-title">📘 책 기본 정보</h2>
        <div class="grid">
            <div class="row">
                <label>반려견 이름 *</label>
                <input type="text" id="petName" placeholder="이름을 입력하세요">
            </div>
            <div class="row">
                <label>추모일</label>
                <input type="date" id="memorialDate">
            </div>
            <div class="row full">
                <label>대표 사진</label>
                <input type="file" id="profileImageFile" accept="image/*">
                <div class="preview-box">
                    <img id="profileImagePreview">
                </div>
                <div class="hint">대표 사진이 자동으로 표지에 사용됩니다.</div>
            </div>
            <div class="row full">
                <label>책 제목 *</label>
                <input type="text" id="title" placeholder="책의 제목을 적어주세요">
            </div>
            <div class="row full">
                <label>부제</label>
                <input type="text" id="coverSubtitle" placeholder="표지에 들어갈 부제">
            </div>
            <div class="row full">
                <label>헌정 문구</label>
                <textarea id="dedicationText" placeholder="마지막으로 전하고 싶은 메시지"></textarea>
            </div>
            <div class="row">
                <label>판형 선택 *</label>
                <select id="bookSpecCode">
                    <option value="PHOTOBOOK_A4_SC" selected>A4 소프트커버 포토북 (PHOTOBOOK_A4_SC)</option>
                </select>
                <div class="hint">A4 소프트커버 포토북으로 고정됩니다.</div>
            </div>
            <div class="row hidden">
                <label>템플릿 선택 *</label>
                <select id="templateCode">
                    <option value="58edh76I0rYa" selected>내지 고정 템플릿</option>
                </select>
            </div>
        </div>

        <div class="btn-area">
            <button type="button" id="saveBtn" class="btn-main" onclick="saveBookProject()">저장하고 추억 추가하기</button>
            <button type="button" id="editBtn" class="btn-sub hidden" onclick="unlockEditMode()">수정하기</button>
        </div>
    </div>
</div>
</body>
</html>