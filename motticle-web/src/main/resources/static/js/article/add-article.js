function goBack() {
    window.history.back();
}

const spinner = document.getElementById('spinner');
spinner.style.display = 'none';
const isPublicCheckbox = document.getElementById('isPublic');

isPublicCheckbox.addEventListener('change', function () {
    if (this.checked) {
        console.log('Switch is ON');
    } else {
        console.log('Switch is OFF');
    }
});
let accessToken = localStorage.getItem('accessToken');
const urlParams = new URLSearchParams(window.location.search);
const selectedType = urlParams.get('type');

function navigateToAddTag() {
    window.location.href = '/add-tag';
}

function updatePlaceholderAndFileInput() {
    const linkInputContainer = document.getElementById('linkInputContainer');
    const articleContentInput = document.getElementById('articleContent');
    const articleImageInput = document.getElementById('articleImage');
    const articleContentText = document.querySelector('.articleContent');
    const linkText = document.querySelector('.link-text');

    articleContentInput.placeholder = '내용을 입력하세요';
    articleImageInput.style.display = 'none';
    articleContentInput.style.height = '280px';
    articleContentInput.style.margin = '0px 7px 0px 0px';
    articleContentInput.readOnly = false;

    if (selectedType === 'IMAGE') {
        articleContentInput.placeholder = '아래 파일 선택 버튼을 눌러 이미지를 첨부하세요.';
        articleImageInput.style.display = 'block';
        articleContentInput.style.height = '240px';
        articleContentInput.readOnly = true;
        linkInputContainer.style.display = 'none';
        linkText.style.display = 'none';
    } else if (selectedType === 'LINK') {
        articleContentInput.style.display = 'none';
        linkInputContainer.style.display = 'block';
        articleContentText.style.display = 'none';
        linkText.style.display = 'block';
    } else {
        linkInputContainer.style.display = 'none';
        linkText.style.display = 'none';
    }
}

updatePlaceholderAndFileInput();

function addImagePreview() {
    const imageInput = document.getElementById('articleImage');
    const articleContent = document.getElementById('articleContent');

    if (imageInput.files && imageInput.files[0]) {
        const reader = new FileReader();

        reader.onload = function (e) {
            articleContent.style.backgroundImage = `url(${e.target.result})`;
        };

        reader.readAsDataURL(imageInput.files[0]);
    }
    const articleContentInput = document.getElementById('articleContent');
    articleContentInput.placeholder = '';
}

function displayUserTags(tags) {
    const tagButtonsContainer = document.getElementById('tagButtonsContainer');

    if (!Array.isArray(tags)) {
        console.error('Invalid response: tags is not an array', tags);
        return;
    }

    tags.forEach(tag => {
        const tagButton = document.createElement('button');
        tagButton.type = 'button';
        tagButton.id = `tag${tag.id}`;
        tagButton.value = tag.id;
        tagButton.className = 'tag-button';
        tagButton.textContent = `#${tag.name}`;

        tagButton.addEventListener('click', function () {
            this.classList.toggle('selected');
        });

        tagButtonsContainer.appendChild(tagButton);
        tagButtonsContainer.addEventListener('click', function (event) {
            if (event.target === tagButtonsContainer) {
                navigateToAddTag();
            }
        });
    });
}

fetch('/api/tags', {
    method: 'GET',
    headers: {
        'Authorization': accessToken,
        'Cache-Control': 'no-store'
    }
})
    .then(response => {
        if (response.status === 200 || response.status === 201) {
            console.log("200ok");
            return response.json();
        } else if (response.status === 401) {
            console.log("token reissue");
            return fetch("/api/auth/reissue", {
                method: "PATCH",
                headers: {
                    'Authorization': accessToken
                }
            })
                .then((res) => {
                    if (res.ok) {
                        return res.json();
                    }
                })
                .then((result) => {
                    accessToken = result.accessToken
                    localStorage.setItem('accessToken', accessToken);
                    return fetch('/api/tags', {
                        method: 'GET',
                        headers: {
                            'Authorization': accessToken,
                            'Cache-Control': 'no-store'
                        }
                    })
                        .then(retryResponse => {
                            if (retryResponse.ok) {
                                return retryResponse.json();
                            } else {
                                throw new Error('Failed to fetch tags after token reissue');
                            }
                        });
                });
        } else {
            return Promise.reject("Failed to fetch tags: " + response.status);
        }
    })
    .then(data => {
        displayUserTags(data.tagSliceRes);
    })
    .catch(error => {
        console.error("Error fetching tags:", error);
    });


function registerArticle() {
    const title = document.getElementById('articleTitle').value;

    if (title.trim() === '') {
        document.getElementById('articleTitle').style.borderColor = 'red';
        alert('제목을 입력하세요.');
        return;
    } else {
        document.getElementById('articleTitle').style.borderColor = '#ccc';
    }

    const isLinkType = selectedType === 'LINK';
    const content = isLinkType ? document.getElementById('articleLink').value :
        document.getElementById('articleContent').value;
    const memo = document.getElementById('articleMemo').value;
    const isPublic = document.getElementById('isPublic').checked;

    const selectedTags = Array.from(document.querySelectorAll('.tag-button.selected'))
        .map(button => parseInt(button.value));

    const data = {
        type: selectedType,
        title: title,
        content: content,
        memo: memo,
        isPublic: isPublic,
        tagIds: selectedTags,

    };

    let formData = new FormData();
    formData.append('articleAddReq', new Blob([JSON.stringify(data)], {
        type: "application/json"
    }));

    if (selectedType === 'IMAGE') {
        const imageInput = document.getElementById('articleImage');
        const imageFile = imageInput.files[0];

        formData.append('image', imageFile);
    }
    fetch('/api/articles', {
        method: 'POST',
        headers: {
            'Authorization': accessToken
        },
        body: formData,
    })
        .then(response => {
            if (response.ok) {
                return response.json();
            } else if (response.status === 401) {
                return fetch("/api/auth/reissue", {
                    method: "PATCH",
                    headers: {
                        'Authorization': accessToken
                    }
                })
                    .then(reissueResponse => {
                        if (reissueResponse.ok) {
                            return reissueResponse.json().then(result => {
                                accessToken = result.accessToken;
                                localStorage.setItem('accessToken', accessToken);
                                // Retry the original request with the new token
                                return fetch('/api/articles', {
                                    method: 'POST',
                                    headers: {
                                        'Authorization': accessToken
                                    },
                                    body: formData,
                                })
                                    .then(retryResponse => {
                                        if (retryResponse.ok) {
                                            return retryResponse.json();
                                        } else {
                                            throw new Error('Failed to register article after token reissue');
                                        }
                                    });
                            });
                        } else {
                            throw new Error('Failed to reissue token');
                        }
                    });
            }
        })
        .then(articleInfoRes => {
            console.log('Article registered successfully:', articleInfoRes);
            alert("아티클이 등록되었습니다.");
            window.location.href = '/';
        })
        .catch(error => {
            console.error('An error occurred while registering article:', error);
        });
}

function validateLink() {
    const link = document.getElementById('articleLink').value;
    const spinner = document.getElementById('spinner');
    spinner.style.display = 'block';

    fetch(`/api/articles/validation?link=${link}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('서버 오류');
            }
            return response.json();
        })
        .then(openGraphResponse => {
            displayLinkPreview(openGraphResponse);
            spinner.style.display = 'none';
            // displayLinkPreview(openGraphResponse);
        })
        .catch(error => {
            console.error('링크 확인 중 오류 발생:', error);
            showToast("유효하지 않은 링크입니다. 다시 입력해주세요.", true);
        });
}

function displayLinkPreview(openGraphResponse) {
    const linkInputContainer = document.getElementById('linkInputContainer');
    const linkPreviewContainer = document.getElementById('linkPreviewContainer');

    linkInputContainer.style.display = 'none';

    const articleDetails = {openGraphResponse};
    linkPreviewContainer.innerHTML = `
<article class="article-box" style="margin-bottom: 15px; onclick=" openArticleLink(
'${articleDetails.openGraphResponse.url}')">
<section class="article-section">
    <p class="link-article-title">${articleDetails.openGraphResponse.title}</p>
    <span class="article-description" href="${articleDetails.openGraphResponse.description}" target="_blank">${articleDetails.openGraphResponse.description}</span>
    <span class="article-url" href="${articleDetails.openGraphResponse.url}" target="_blank">${articleDetails.openGraphResponse.url}</span>
</section>
<img src="${articleDetails.openGraphResponse.image}" alt="OG Image" class="link-type-image">
</article>
`;
}

function openArticleLink(url) {
    window.open(url, '_blank');
}
