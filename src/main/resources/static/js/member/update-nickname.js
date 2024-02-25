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

fetch('/api/members', {
    headers: {
        'Authorization': accessToken,
        'Cache-Control': 'no-cache'
    }
})
    .then(response => {
        if (response.ok) {
            return response.json();
        } else {
            if (response.status === 401) {
                return fetch("/api/auth/reissue", {
                    method: "PATCH",
                    headers: {
                        'Authorization': accessToken
                    }
                })
                    .then(reissueResponse => {
                        if (reissueResponse.ok) {
                            return reissueResponse.json();
                        } else {
                            throw new Error('Failed to reissue token');
                        }
                    })
                    .then(result => {
                        accessToken = result.accessToken;
                        localStorage.setItem('accessToken', accessToken);
                        return fetch('/api/members', {
                            headers: {
                                'Authorization': accessToken,
                                'Cache-Control': 'no-cache'
                            }
                        });
                    })
                    .then(retryResponse => {
                        if (retryResponse.ok) {
                            return retryResponse.json();
                        } else {
                            throw new Error('Failed to reissue token and retry request');
                        }
                    });
            } else {
                throw new Error('API 호출 실패');
            }
        }
    })
    .then(data => {
        const nickname = data.nickname;
        const inputElement = document.getElementById('input-:r2:');
        inputElement.value = nickname;
    })
    .catch(error => console.error('Error:', error));

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
            console.log('서버 응답 상태 코드:', response.status);

            if (response.status === 204) {
                alert('닉네임이 수정되었습니다.');
                window.location.href = '/my-page';
            } else {
                console.error('닉네임 수정에 실패했습니다.');
            }
        })
        .catch(async error => {
            if (error.response && error.response.status === 401) {
                await handleUnauthorizedResponse();
            } else {
                console.error('API 호출 에러:', error);
            }
        });
}
