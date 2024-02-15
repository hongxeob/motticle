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
    .then(response => response.json())
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
    localStorage.removeItem('accessToken');

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
            } else {
                console.error('로그아웃 실패');
            }
        })
        .catch(error => console.error('Error:', error));
}

function deleteCookie(name) {
    document.cookie = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;`;
}
