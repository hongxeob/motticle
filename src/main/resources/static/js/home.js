window.onload = function () {
    document.getElementById('latestButton').click();
}

function getAccessTokenFromQuery() {
    const uriParams = new URLSearchParams(location.search);
    let accessToken;

    if (uriParams.has('accessToken')) {
        accessToken = uriParams.get('accessToken');
        localStorage.setItem('accessToken', accessToken);
    } else {
        accessToken = localStorage.getItem('accessToken');
    }

    return accessToken;
}

const accessToken = getAccessTokenFromQuery();

if (accessToken === null) {
    redirectToKakaoScreen();
}

function redirectToKakaoScreen() {
    window.location.href = '/kakao';
}

let currentPage = 1;

function changeSortOrder(order) {
    const latestButton = document.getElementById('latestButton');
    const oldestButton = document.getElementById('oldestButton');

    if (order === 'oldest') {
        oldestButton.style.display = 'none';
        latestButton.style.display = 'block';
    } else {
        latestButton.style.display = 'none';
        oldestButton.style.display = 'block';
        oldestButton.style.left = '410px';
    }
    currentPage = 1;
    const sortOrder = order === 'oldest' ? 'oldest' : null;
    fetchAndRenderArticles(sortOrder);
}

async function fetchAndRenderData() {
    const response = await fetch('/api/tags', {
        headers: {
            'Authorization': accessToken
        }
    });

    if (response.status === 401) {
        alert("다시 로그인해주세요.");
        window.location.href = "/kakao";
    } else {
        const tags = await response.json().then(data => data.tagSliceRes);
        renderTags(tags);
    }
}

fetchAndRenderData();

function renderTags(tags) {
    const tagButtonsContainer = document.getElementById('home-tagButtonsContainer');

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
        tagButton.className = 'home-tag-button';
        tagButton.textContent = `#${tag.name}`;

        tagButton.addEventListener('click', function () {
            this.classList.toggle('selected');

            fetchAndRenderArticles();
        });
        tagButtonsContainer.appendChild(tagButton);
        tagButtonsContainer.addEventListener('click', function (event) {
            if (event.target === tagButtonsContainer) {
                navigateToAddTag();
            }
        });
    });
}

let hasNextPage;

async function fetchAndRenderArticles(sortOrder) {
    const spinner = document.getElementById('spinner');
    spinner.style.display = 'block';

    const articleListContainer = document.getElementById('articleListContainer');
    const viewContainer = document.querySelector('.home-view');
    articleListContainer.innerHTML = '';

    const selectedTags = Array.from(document.querySelectorAll('.home-tag-button.selected'))
        .map(button => parseInt(button.value));

    const tagIds = selectedTags.length === 0 ? '' : selectedTags.join(',');

    const articlesResponse = await fetch(`/api/articles/search?tagIds=${tagIds}&sortOrder=${sortOrder}`, {
        headers: {
            'Authorization': accessToken
        }
    });
    spinner.style.display = 'none';

    const articlesData = await articlesResponse.json();
    console.log('Filtered Articles Data:', articlesData);
    if (articlesData.articleOgResList.length === 0) {
        const noArticlesImage = `
        <img src="images/cup of water notebook and pencil.png" alt="No Articles Image" width="100%" height="100%">
    `;
        const noArticlesContainer = document.createElement('div');
        noArticlesContainer.style.position = 'absolute';
        noArticlesContainer.style.top = '302px';
        noArticlesContainer.style.right = '102px';
        noArticlesContainer.innerHTML = noArticlesImage;
        viewContainer.appendChild(noArticlesContainer);

        const latestButton = document.getElementById('latestButton');
        const oldestButton = document.getElementById('oldestButton');
        latestButton.style.display = 'none';
        oldestButton.style.display = 'none';
    } else {
        renderArticles(articlesData);
        hasNextPage = articlesData.hasNext;
    }
}

function renderArticles(articlesData) {
    const articleListContainer = document.getElementById('articleListContainer');

    articlesData.articleOgResList.forEach(article => {
        const articleCard = document.createElement('div');
        articleCard.classList.add('article-card');
        articleCard.style.cursor = 'pointer';
        articleCard.setAttribute('onclick', `moveArticlePage(${article.id})`);

        const articleImageContainer = document.createElement('div');
        articleImageContainer.classList.add('article-image-container');

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
            articleImageHTML = `<p class="type-text-content">${article.content}</p>`;
        }

        articleDetails.innerHTML = `
            <div class="article-title">${article.title}</div>

            <div class="article-tags">${articleTagName}</div>
        `;

        articleImageContainer.innerHTML = articleImageHTML;
        articleCard.appendChild(articleImageContainer);
        articleCard.appendChild(articleDetails);

        articleListContainer.appendChild(articleCard);
    });
}

function moveArticlePage(articleId) {
    window.location.href = `/article/${articleId}`;
}

let selectedArticleType;

function openModal() {
    document.getElementById('myModal').style.display = 'flex';
}

function closeModal() {
    document.getElementById('myModal').style.display = 'none';
}

function selectArticleType(type) {
    selectedArticleType = type;
    console.log('Selected Article Type:', selectedArticleType);

    closeModal();
    window.location.href = `/add?type=${selectedArticleType}`;
}

function navigateToAddTag() {
    window.location.href = '/addTag';
}

function moveSearchPage() {
    window.location.href = '/search';
}

function infiniteScroll() {
    const articleListContainer = document.getElementById('articleListContainer');

    articleListContainer.addEventListener('scroll', function () {
        const scrollTop = articleListContainer.scrollTop;
        const scrollHeight = articleListContainer.scrollHeight;
        const clientHeight = articleListContainer.clientHeight;

        if (scrollTop + clientHeight >= scrollHeight) {
            fetchMoreDataThrottled();
        }
    });

    async function fetchMoreData() {
        if (!hasNextPage) {
            showToast("마지막 아티클 이에요.", false);
            return;
        }

        const spinner = document.getElementById('spinner');
        spinner.style.marginTop = ' -390px';
        spinner.style.display = 'block';

        const selectedTags = Array.from(document.querySelectorAll('.home-tag-button.selected'))
            .map(button => parseInt(button.value));

        const tagIds = selectedTags.length === 0 ? '' : selectedTags.join(',');

        const sortOrder = getSortOrder();

        const response = await fetch(`/api/articles/search?tagIds=${tagIds}&page=${currentPage + 1}&sortOrder=${sortOrder}`, {
            headers: {
                'Authorization': accessToken
            }
        });
        const newData = await response.json();
        renderArticles(newData);
        spinner.style.display = 'none';
        currentPage++;

        hasNextPage = newData.hasNext;
    }

    let lastFetchTime = 0;
    const throttleTime = 500;

    function fetchMoreDataThrottled() {
        const now = Date.now();
        if (now - lastFetchTime >= throttleTime) {
            fetchMoreData();
            lastFetchTime = now;
        }
    }
}

infiniteScroll();

function getSortOrder() {
    const latestButton = document.getElementById('latestButton');
    const oldestButton = document.getElementById('oldestButton');

    if (latestButton.style.display !== 'none') {
        // 최신순 버튼이 보이면 최신순 반환
        return 'oldest';
    } else {
        // 아니면 오래된순 반환
        return 'latest';
    }
}
