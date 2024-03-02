function goBack() {
    window.history.back();
}

const uriParams = new URLSearchParams(location.search)
const accessToken = uriParams.get('accessToken');
console.log(accessToken);

console.log('AccessToken:', accessToken);

function startRegistration() {
    const nickname = document.getElementById('nickname').value;
    const genderType = document.querySelector('.requestGenderType').value;

    if (nickname === "") {
        alert("닉네임을 입력해주세요.");
        document.getElementById('nickname').focus();
        return;
    }

    const requestData = {
        nickname: nickname,
        genderType: genderType
    };

    fetch('/api/members', {
        method: 'PATCH',
        headers: {
            'Authorization': accessToken,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestData)
    })
        .then(response => response.json())
        .then(data => {
            console.log('API 호출 결과:', data);
            localStorage.setItem('accessToken', accessToken);
            window.location.href = '/';
        })
        .catch(error => {
            console.error('API 호출 에러:', error);
            window.location.href = '/kakao';
        });
}

function checkNickname() {
    const nickname = document.getElementById('nickname').value.trim();
    if (nickname === "") {
        alert("닉네임을 입력해주세요.");
        return;
    }

    fetch(`/api/members/nickname?nickname=${nickname}`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            alert("닉네임이 사용 가능합니다.");
        })
        .catch(error => {
            alert("닉네임이 중복됩니다. 다른 닉네임을 입력해주세요.");
        });
}
