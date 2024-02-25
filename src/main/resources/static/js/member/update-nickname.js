let accessToken = localStorage.getItem('accessToken');

if (accessToken === null) {
    redirectToKakaoScreen();
}

function redirectToKakaoScreen() {
    window.location.href = '/kakao';
}

function goBack() {
    window.history.back();
}

document.addEventListener('DOMContentLoaded', getNickname);

async function getNickname() {
    try {
        const response = await fetch('/api/members', {
            headers: {
                'Authorization': accessToken,
            }
        });
        if (response.ok) {
            const data = await response.json();
            const nickname = data.nickname;
            const inputElement = document.getElementById('input-:r2:');
            inputElement.value = nickname;
        } else {
            throw new Error('API 호출 실패');
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

async function checkNickname(event) {
    event.preventDefault();

    const nicknameInput = document.getElementById('input-:r2:');
    const nickname = nicknameInput.value.trim();
    const nicknameAlert = document.querySelector('.nicknameAlert');

    if (nickname === "") {
        nicknameAlert.textContent = "닉네임을 입력해주세요.";
        nicknameAlert.style.color = "red";
        nicknameInput.style.border = "1px solid red";

        nicknameInput.focus();
        return;
    }
    try {
        const response = await fetch(`/api/members/nickname?nickname=${nickname}`);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        nicknameInput.value = nickname;
        nicknameAlert.textContent = "닉네임이 사용 가능합니다.";
        nicknameAlert.style.color = "green";

        nicknameInput.style.border = "1px solid #dadada";

    } catch (error) {
        if (error.response && error.response.status === 401) {
            await handleUnauthorizedResponse(); // Assuming handleUnauthorizedResponse is a function you've defined to handle 401 errors.
        } else {
            nicknameAlert.textContent = "닉네임이 중복됩니다. 다른 닉네임을 입력해주세요.";
            nicknameAlert.style.color = "red";
            nicknameInput.style.border = "1px solid red";

            nicknameInput.focus();
        }
    }
}

function updateNickname() {
    const nickname = document.getElementById('input-:r2:').value;

    if (nickname === "") {
        alert("닉네임을 입력해주세요.");
        document.getElementById('nickname').focus();
        return;
    }

    const requestData = {
        nickname: nickname,
    };

    fetch('/api/members/modify', {
        method: 'PATCH',
        headers: {
            'Authorization': accessToken,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestData)
    })
        .then(response => {
            if (response.status === 204) {
                showToast("닉네임이 변경되었습니다.", false);
            } else {
                showToast("닉네임을 업데이트하는데 문제가 발생했습니다.", true);
            }
        });
}
