function goBack() {
    window.history.back();
}

const accessToken = localStorage.getItem('accessToken');

if (accessToken === null) {
    redirectToKakaoScreen();
}

function redirectToKakaoScreen() {
    window.location.href = '/kakao';
}

fetch('/api/members', {
    headers: {
        'Authorization': accessToken
    }
})
    .then(response => {
        if (response.ok) {
            return response.json();
        } else {
            if (response.status === 401) {
                console.log("Token reissue");
                return fetch("/api/auth/reissue", {
                    method: "PATCH",
                    headers: {
                        'Authorization': accessToken
                    }
                })
                    .then(res => {
                        if (res.ok) {
                            return res.json();
                        } else {
                            throw new Error('Failed to reissue token');
                        }
                    })
                    .then(result => {
                        localStorage.setItem('accessToken', result.accessToken);
                        return fetch('/api/members', {
                            headers: {
                                'Authorization': result.accessToken,
                            }
                        })
                            .then(response => response.json());
                    });
            }
        }
    })
    .then(data => {
        const nickname = data.nickname;
        const email = data.email;
        let genderType = data.genderType;

        if (genderType === 'MALE') {
            genderType = '남성';
        } else if (genderType === 'FEMALE') {
            genderType = '여성';
        }

        document.getElementById('nicknameInfo').querySelector('.account-value').textContent = nickname;
        document.getElementById('emailInfo').querySelector('.account-value').textContent = email;
        document.getElementById('genderInfo').querySelector('.account-value').textContent = genderType;
    })
    .catch(error => console.error('Error:', error));
