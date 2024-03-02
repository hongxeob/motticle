const accessToken = localStorage.getItem('accessToken');
const urlParams = new URLSearchParams(window.location.search);
const articleId = urlParams.get('id');
fetchArticleDetails();

function fetchArticleDetails() {
    const spinner = document.getElementById('spinner');
    spinner.style.display = 'block';

    const tagButtonsContainer = document.getElementById('tagButtonsContainer');
    const reportButton = document.querySelector('.report-button');
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
            reportButton.style.display = 'block';

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
    const scrapCount = document.querySelector('.details-scrap-count');

    articleTitleElement.textContent = articleDetails.title;
    articleDetailsElement.textContent = articleDetails.content;

    let articleHTML = '';
    const createdAtDiv = document.querySelector('.created-at');
    const createdAt = new Date(articleDetails.createdDatetime);
    const formattedDate = `${createdAt.getFullYear()}/${createdAt.getMonth() + 1}/${createdAt.getDate()}`;
    createdAtDiv.textContent = `작성일: ${formattedDate}`;
    const scrapCountValue = articleDetails.scrapCount !== null ? articleDetails.scrapCount : 0;
    scrapCount.textContent = `스크랩 ` + scrapCountValue;

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

document.addEventListener("DOMContentLoaded", function () {
    const reportButton = document.querySelector('.report-button');
    const modal = document.getElementById('myModal');
    const closeBtn = document.querySelector('.close');

    reportButton.addEventListener('click', function () {
        modal.style.display = "block";
    });

    closeBtn.addEventListener('click', function () {
        modal.style.display = "none";
    });

    window.addEventListener('click', function (event) {
        if (event.target === modal) {
            modal.style.display = "none";
        }
    });

    const submitReportBtn = document.getElementById('submitReport');
    submitReportBtn.addEventListener('click', function () {
        const reportContent = document.getElementById('reportContent').value;

        modal.style.display = "none";

        const requestBody = {
            articleId: articleId,
            content: reportContent
        }

        fetch('/api/reports', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': accessToken
            },
            body: JSON.stringify(requestBody)
        })
            .then(response => {
                if (!response.ok) {
                    if (response.status === 400) {
                        response.json().then(data => {
                            if (data.code === 'R002') {
                                showToast("이미 신고한 아티클이에요. 관리자가 확인중이에요.", true);
                            } else if (data.code === 'R001') {
                                showToast("자신의 아티클은 신고할 수 없어요. 아티클을 확인해주세요.", true);
                            }
                        });
                    }
                    showToast("일시적인 문제로 신고에 실패했어요. 잠시 후 다시 시도해주세요.", true)
                }
                return response.json();
            })
            .then(data => {
                console.log('Report submitted successfully:', data);
                showToast("신고가 정상 제출 되었어요. 신속하게 확인 후 조치할게요.", false);
            })
            .catch(error => {
                console.error('Error submitting report:', error);
                showToast("일시적인 문제로 신고에 실패했어요. 잠시 후 다시 시도해주세요.", true)
            });
    });
});
