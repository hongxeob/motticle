let accessToken = localStorage.getItem('accessToken');
const path = window.location.pathname;
const articleId = path.substring(path.lastIndexOf('/') + 1);
let selectedType;
fetchArticleDetails();

const isPublicCheckbox = document.getElementById('isPublic');
isPublicCheckbox.addEventListener('change', updateIsPublic);

function moveUpdatePage() {
    window.location.href = `/article/update/${articleId}`;
}

function fetchArticleDetails() {
    const spinner = document.getElementById('spinner');
    spinner.style.display = 'block';

    const isPublicCheckbox = document.getElementById('isPublic');
    const tagButtonsContainer = document.getElementById('tagButtonsContainer');
    const updateArticleButton = document.getElementById('updateArticleButton');
    const deleteArticleButton = document.getElementById('deleteArticleButton');
    const memoLabel = document.querySelector('.memo-text');
    const tagLabel = document.querySelector('.tag-text');
    const goBack = document.querySelector('.go-back');
    const tagInfoAlert = document.querySelector('.tagInfoAlert');
    const redirectHome = document.querySelector('.redirect-home');
    const isPublic = document.getElementById('isPublicContainer');

    fetch(`/api/articles/${articleId}`, {
        method: 'GET',
        headers: {
            'Authorization': accessToken,
            'Cache-Control': 'no-store'
        }
    })
        .then(response => {
            if (!response.ok) {
                if (response.status === 401) {
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
                            accessToken = result.accessToken;
                            localStorage.setItem('accessToken', accessToken);
                            window.location.href = "/article/" + articleId;
                        });
                } else {
                    throw new Error(`Error fetching article details: ${response.statusText}`);
                }
            }
            return response.json();
        })
        .then(articleDetails => {
            tagButtonsContainer.style.display = 'block';
            memoLabel.style.display = 'block';
            tagLabel.style.display = 'block';
            goBack.style.display = 'block';
            deleteArticleButton.style.display = 'block';
            updateArticleButton.style.display = 'block';
            isPublic.style.display = 'block';
            tagInfoAlert.style.display = 'block';
            redirectHome.style.display = 'block';

            displayArticleDetails(articleDetails);
            isPublicCheckbox.checked = articleDetails.isPublic;
            selectedType = articleDetails.type;
        })
        .catch(error => console.error(error))
        .finally(() => {
            spinner.style.display = 'none';
        });
}

function displayArticleDetails(articleDetails) {

    const articleTitleElement = document.getElementById('articleTitle');
    const articleDetailsElement = document.getElementById('articleDetails');
    const articleMemoElement = document.getElementById('articleMemo');

    articleTitleElement.textContent = articleDetails.title;
    articleDetailsElement.textContent = articleDetails.content;

    let articleHTML = '';

    if (articleDetails.type === 'IMAGE' && articleDetails.content) {
        articleHTML = `<img src="${articleDetails.content}" alt="Article Image" class="image-type-image">`;

    } else if (articleDetails.type === 'TEXT') {
        articleHTML =
            `<div class="text-div1">
                    <div class="text-div2">
                        <textarea class="text-details">${articleDetails.content}</textarea>
                    </div>
                </div>
                `
    } else {
        articleHTML = `
            <article class="article-box" onclick="openArticleLink('${articleDetails.openGraphResponse.url}')">
            <section class="article-section">
                    <p class="link-article-title">${articleDetails.openGraphResponse.title}</p>
                    <span class="article-description" href="${articleDetails.openGraphResponse.description}" target="_blank">${articleDetails.openGraphResponse.description}</span>
                    <span class="article-url" href="${articleDetails.openGraphResponse.url}" target="_blank">${articleDetails.openGraphResponse.url}</span>
                </section>
                <img src="${articleDetails.openGraphResponse.image}" alt="OG Image" class="link-type-image">
            </article>
        `;
    }
    const tagContainer = document.querySelector('#tagButtonsContainer');
    tagContainer.innerHTML = '';
    const addTagButton = document.createElement('button');
    addTagButton.className = 'tag-button';
    addTagButton.textContent = '+태그등록';
    addTagButton.style.border = '1px solid rgb(205, 205, 205)';
    addTagButton.style.color = 'rgb(129, 129, 129)';
    addTagButton.style.marginRight = '2px';
    addTagButton.style.backgroundColor = '#f7f7f7';
    addTagButton.onclick = navigateToAddTag;

    tagContainer.appendChild(addTagButton);
    displayUserTags(articleDetails.tagsRes.tagRes);
    articleDetailsElement.innerHTML = articleHTML;

    articleMemoElement.textContent = articleDetails.memo;
}

function goBack() {
    window.history.back();
}

function openArticleLink(url) {
    window.open(url, '_blank');
}

function displayUserTags(tags) {
    const tagButtonsContainer = document.getElementById('tagButtonsContainer');


    if (!Array.isArray(tags)) {
        console.error('Invalid response: tags is not an array', tags);
        return;
    }
    if (tags.length === 0) {
        tagButtonsContainer.addEventListener('click', function () {
            navigateToAddTag();
        });

        return;
    }

    tags.forEach(tag => {
        const tagButton = document.createElement('button');
        tagButton.type = 'button';
        tagButton.id = `tag${tag.id}`;
        tagButton.value = tag.id;
        tagButton.className = 'added-tag-button';
        tagButton.textContent = `#${tag.name}`;

        tagButtonsContainer.appendChild(tagButton);
        tagButtonsContainer.addEventListener('click', function (event) {
            // 클릭이 태그 버튼이 아닌 빈 공간에서 발생했는지 확인
            if (event.target === tagButtonsContainer) {
                navigateToAddTag();
            }
        });
    });
}

function navigateToAddTag() {
    window.location.href = `/tagging?articleId=${articleId}`;
}

function confirmDelete() {
    if (confirm("정말 삭제하시겠습니까?")) {
        deleteArticle();
    }
}

function deleteArticle() {
    fetch(`/api/articles/${articleId}`, {
        method: 'DELETE',
        headers: {
            'Authorization': accessToken
        }
    })
        .then(response => {
            if (response.ok) {
                window.location.href = '/';
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
                        return fetch(`/api/articles/${articleId}`, {
                            method: 'DELETE',
                            headers: {
                                'Authorization': accessToken
                            },
                        });
                    })
                    .then(secondDeleteResponse => {
                        if (secondDeleteResponse.ok) {
                            window.location.href = '/';
                        } else {
                            throw new Error('Failed to delete article after token reissue');
                        }
                    });
            } else {
                throw new Error(`Error deleting article: ${response.statusText}`);
            }
        })
        .catch(error => console.error(error));
}

function updateIsPublic() {
    const isPublic = document.getElementById('isPublic').checked;
    console.log("type", selectedType);
    const isLinkType = selectedType === 'LINK';
    const isImageType = selectedType === 'IMAGE';
    let content;
    if (isLinkType) {
        const linkSection = document.querySelector('.article-section');
        content = linkSection.querySelector('.article-url').innerText;
        console.log("call isLinkType", content);
    } else if (isImageType) {
        const contentImage = document.querySelector('.image-type-image');
        const imageUrl = contentImage.src;

        content = imageUrl;
    } else {
        content = document.querySelector('.text-details').value;
    }
    const title = document.getElementById('articleTitle').textContent;
    const memo = document.getElementById('articleMemo').textContent;
    console.log("call isLinkType final", content);
    const data = {
        title: title,
        memo: memo,
        content: content,
        isPublic: isPublic,
    };

    fetch(`/api/articles/${articleId}`, {
        method: 'PATCH',
        headers: {
            'Authorization': accessToken,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data),
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
                            return reissueResponse.json();
                        } else {
                            throw new Error('Failed to reissue token');
                        }
                    })
                    .then(result => {
                        accessToken = result.accessToken;
                        localStorage.setItem('accessToken', accessToken);
                        return fetch(`/api/articles/${articleId}`, {
                            method: 'PATCH',
                            headers: {
                                'Authorization': accessToken,
                                'Content-Type': 'application/json'
                            },
                            body: JSON.stringify(data),
                        });
                    });
            } else {
                throw new Error('Error updating isPublic');
            }
        })
        .then(articleInfoRes => {
            console.log('공개 여부 수정 완료:', articleInfoRes.isPublic);
            showToast("공개 여부가 수정되었습니다.", false);
        })
        .catch(error => {
            console.error('아티클 수정 중 오류 발생:', error);
            showToast("오류가 발생했습니다. 다시 시도해주세요.", true);
        });
}
