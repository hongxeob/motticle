const accessToken = localStorage.getItem('accessToken');
const urlParams = new URLSearchParams(window.location.search);
const articleId = urlParams.get('id');
fetchArticleDetails();

function fetchArticleDetails() {
    const spinner = document.getElementById('spinner');
    spinner.style.display = 'block';

    const tagButtonsContainer = document.getElementById('tagButtonsContainer');

    const tagLabel = document.querySelector('.tag-text');
    const goBack = document.querySelector('.go-back');

    const url = `/api/articles/${articleId}/details`;
    fetch(url, {
        method: 'GET'
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
                            localStorage.setItem('accessToken', result.accessToken);
                            window.location.href = "/article?id=" + articleId;
                        });
                }
            }
            return response.json();
        })
        .then(articleDetails => {
            tagButtonsContainer.style.display = 'block';
            tagLabel.style.display = 'block';
            goBack.style.display = 'block';

            displayArticleDetails(articleDetails);
        })
        .catch(error => console.error(error))
        .finally(() => {
            spinner.style.display = 'none';
        });
}

function displayArticleDetails(articleDetails) {

    const articleTitleElement = document.getElementById('articleTitle');
    const articleDetailsElement = document.getElementById('articleDetails');

    articleTitleElement.textContent = articleDetails.title;
    articleDetailsElement.textContent = articleDetails.content;

    let articleHTML = '';
    const createdAtDiv = document.querySelector('.created-at');
    const createdAt = new Date(articleDetails.createdDatetime);
    // const formattedDate = createdAt.toLocaleString();
    const formattedDate = `${createdAt.getFullYear()}/${createdAt.getMonth() + 1}/${createdAt.getDate()}`;
    createdAtDiv.textContent = `작성일: ${formattedDate}`;

    if (articleDetails.type === 'IMAGE' && articleDetails.content) {
        articleHTML = `<img src="${articleDetails.content}" alt="Article Image" class="image-type-image">`;

    } else if (articleDetails.type === 'TEXT') {
        articleHTML =
            `<div class="text-div1">
                    <div class="text-div2">
                        <textarea class="text-details" readOnly>${articleDetails.content}</textarea>
                    </div>
                </div>
                `
    } else {
        articleHTML = `
            <article class="article-box" onclick="openArticleLink('${articleDetails.openGraphResponse.url}')">
            <section class="article-section">
                    <p class="link-article-title">${articleDetails.openGraphResponse.title}</p>
                    <span class="article-url" href="${articleDetails.openGraphResponse.description}" target="_blank">${articleDetails.openGraphResponse.description}</span>
                    <span class="article-url" href="${articleDetails.openGraphResponse.url}" target="_blank">${articleDetails.openGraphResponse.url}</span>
                </section>
                <img src="${articleDetails.openGraphResponse.image}" alt="OG Image" class="link-type-image">
            </article>
        `;
    }
    const tagContainer = document.querySelector('#tagButtonsContainer');
    tagContainer.innerHTML = '';
    displayUserTags(articleDetails.tagsRes.tagRes);
    articleDetailsElement.innerHTML = articleHTML;
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
        const placeholderText = document.createElement('div');
        placeholderText.style.color = "#777";
        placeholderText.style.padding = "10px";
        tagButtonsContainer.appendChild(placeholderText);
        return;
    }

    tags.forEach(tag => {
        const tagButton = document.createElement('button');
        tagButton.type = 'button';
        tagButton.id = `tag${tag.id}`;
        tagButton.value = tag.id;
        tagButton.className = 'others-tag';
        tagButton.textContent = `#${tag.name}`;

        tagButtonsContainer.appendChild(tagButton);
    });
}
