function goBack() {
    window.history.back();
}

let accessToken = localStorage.getItem('accessToken');

function searchByKeyword() {
    const keywordInput = document.getElementById('keywordInput').value;
    const accessToken = localStorage.getItem('accessToken'); // Ensure you get the latest token

    fetch(`/api/articles/search?keyword=${keywordInput}`, {
        method: 'GET',
        headers: {
            'Authorization': accessToken
        }
    })
        .then(response => {
            if (response.ok) {
                return response.json();
            } else if (response.status === 401) {
                return handleTokenReissue().then(() => {
                    const newAccessToken = localStorage.getItem('accessToken');
                    return fetch(`/api/articles/search?keyword=${keywordInput}`, {
                        method: 'GET',
                        headers: {
                            'Authorization': newAccessToken
                        }
                    });
                })
                    .then(newResponse => {
                        if (!newResponse.ok) {
                            throw new Error(`Failed to fetch with new token: ${newResponse.statusText}`);
                        }
                        return newResponse.json();
                    });
            } else {
                throw new Error(`검색 결과를 가져오는 중 오류 발생: ${response.statusText}`);
            }
        })
        .then(searchResults => {
            renderArticles(searchResults);
        })
        .catch(error => console.error(error));
}

function handleTokenReissue() {
    return fetch("/api/auth/reissue", {
        method: "PATCH",
        method: "PATCH",
        headers: {
            'Authorization': accessToken
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to reissue token');
            }
            return response.json();
        })
        .then(data => {
            accessToken = data.accessToken
            localStorage.setItem('accessToken', accessToken);
        });
}

function renderArticles(articlesData) {
    const articleListContainer = document.getElementById('articleListContainer');
    articleListContainer.innerHTML = '';
    console.log(articlesData);

    if (articlesData.articleOgResList.length === 0) {
        showToast("검색 결과가 없습니다.", false);
    } else {
        articlesData.articleOgResList.forEach(article => {
            const articleCard = document.createElement('div');
            articleCard.classList.add('article-card');

            const articleImageContainer = document.createElement('div');
            articleImageContainer.classList.add('article-image-container');
            articleImageContainer.style.borderRadius = '4px';
            const articleDetails = document.createElement('div');
            articleDetails.classList.add('article-details');

            const articleTagName = article.tagsRes.tagRes.map(tag => `
            <div class="article-tag-card">#${tag.name}</div>
        `).join('');

            let articleImageHTML = '';

            if (article.type === 'IMAGE' && article.content) {
                articleImageHTML = `<img src="${article.content}" alt="Article Image" class="home-article-image">`;
            } else if (article.type === 'LINK' && article.openGraphResponse.image) {
                articleImageHTML = `<img src="${article.openGraphResponse.image}" alt="OG Image" class="home-article-image">`;
            } else {
                articleImageContainer.style.border = '1px solid #E6E6E6';
                articleImageHTML = `<p class="type-text-content">${article.content}</p>`;
            }

            articleDetails.innerHTML = `
            <div class="article-title" onclick="moveArticlePage(${article.id})">${article.title}</div>

            <div class="article-tags">${articleTagName}</div>
        `;

            articleImageContainer.innerHTML = articleImageHTML;
            articleCard.appendChild(articleImageContainer);
            articleCard.appendChild(articleDetails);

            articleListContainer.appendChild(articleCard);
        });
    }


}

function moveArticlePage(articleId) {
    window.location.href = `/article/${articleId}`;
}
