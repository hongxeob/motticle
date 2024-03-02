function goBack() {
    window.history.back();
}

let accessToken = localStorage.getItem('accessToken');

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
        } else {
            return response.json();
        }
    })
    .then(data => {
        const profileImageContainer = document.querySelector('.profile-image-container');
        const profileImage = document.createElement('img');
        profileImage.src = data.image;
        profileImage.alt = 'Profile Image';
        profileImage.style.width = '100%';
        profileImage.style.height = '100%';
        profileImage.style.objectFit = 'cover';

        profileImageContainer.appendChild(profileImage);
        document.querySelector('.name').innerText = data.nickname;
        document.querySelector('.email').innerText = data.email;
    })
    .catch(error => console.error('Error:', error));

async function fetchAndDisplayMemberProfile(accessToken) {
    try {
        const response = await fetch('/api/members', {
            headers: {
                'Authorization': accessToken
            }
        });

        if (response.status === 401) {
            const res = await fetch("/api/auth/reissue", {
                method: "PATCH",
                headers: {
                    'Authorization': accessToken
                }
            });

            if (res.ok) {
                const result = await res.json();
                localStorage.setItem('accessToken', result.accessToken);
                return await fetchAndDisplayMemberProfile(result.accessToken); // 재발급 후 다시 호출
            } else {
                throw new Error('Failed to reissue token');
            }
        }

        const data = await response.json();

        // 데이터 처리
        const profileImageContainer = document.querySelector('.profile-image-container');
        const profileImage = document.createElement('img');
        profileImage.src = data.image;
        profileImage.alt = 'Profile Image';
        profileImage.style.width = '100%';
        profileImage.style.height = '100%';
        profileImage.style.objectFit = 'cover';

        profileImageContainer.appendChild(profileImage);
        document.querySelector('.name').innerText = data.nickname;
        document.querySelector('.email').innerText = data.email;

    } catch (error) {
        console.error('Error:', error);
        // 오류 처리
    }
}

function changeImage() {
    const inputElement = document.createElement('input');
    inputElement.type = 'file';
    inputElement.accept = 'image/*';
    inputElement.addEventListener('change', handleImageChange);
    inputElement.click();
}

function handleImageChange(event) {
    const file = event.target.files[0];

    if (file) {
        const formData = new FormData();
        formData.append('image', file);


        fetch(`/api/members/modify/image`, {
            method: 'PATCH',
            headers: {
                'Authorization': accessToken,
            },
            body: formData,
        })
            .then(response => response.json())
            .then(data => {
                const profileImage = document.querySelector('.profile-image-container img');
                profileImage.src = URL.createObjectURL(file);
            })
            .catch(error => console.error('Error:', error));
    }
}

function logout() {
    fetch('/api/auth/logout', {
        method: 'DELETE',
        headers: {
            'Authorization': accessToken,
        },
    })
        .then(response => {
            if (response.ok) {
                deleteCookie('refreshToken');
                window.location.href = '/kakao';
            } else if (response.status === 401) {
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
                        return fetch('/api/auth/logout', {
                            method: 'DELETE',
                            headers: {
                                'Authorization': accessToken,
                            },
                        });
                    })
                    .then(secondLogoutResponse => {
                        if (secondLogoutResponse.ok) {
                            deleteCookie('refreshToken');
                            window.location.href = '/kakao';
                        } else {
                            console.error('Failed to logout after token reissue');
                        }
                    });
            } else {
                console.error('로그아웃 실패');
            }
        })
        .catch(error => console.error('Error:', error));
}

function deleteCookie(name) {
    document.cookie = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;`;
}
