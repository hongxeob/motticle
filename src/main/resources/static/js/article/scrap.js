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

let hasNextPage;
const spinner = document.getElementById('spinner');
spinner.style.display = 'none';

async function fetchAndRenderArticles() {

    const articlesResponse = await fetch('/api/scraps', {
        headers: {
            'Authorization': accessToken
        }
    });
    if (articlesResponse.status === 401) {
        console.log("token reissue");
        const res = await fetch("/api/auth/reissue", {
            method: "PATCH",
            headers: {
                'Authorization': accessToken
            }
        });

        if (res.ok) {
            const result = await res.json();
            localStorage.setItem('accessToken', result.accessToken);
            window.location.href = "/scrap";
            return;
        }
    }
    const articlesData = await articlesResponse.json();

    renderArticles(articlesData);
    hasNextPage = articlesData.hasNext;
}

fetchAndRenderArticles();

async function checkScrapedArticle(articleId) {
    const response = await fetch(`/api/scraps/${articleId}`, {
        headers: {
            'Authorization': accessToken
        }
    });
    if (response.ok) {
        const isScraped = await response.json();
        console.log(isScraped)
        return isScraped;
    } else {
        return false;
    }
}

async function renderArticles(articlesData) {
    const exploreArticleSection = document.querySelector('.explore-article-section');

    await Promise.all(articlesData.articleOgResList.map(async (article) => {
        const articleId = article.id;
        const isScrapped = await checkScrapedArticle(articleId);
        const nickname = article.memberInfoRes.nickname;
        const profileImage = article.memberInfoRes.image;
        const title = article.title;
        const content = article.content;
        const type = article.type;
        const tags = article.tagsRes.tagRes || [];
        const svgElement = document.createElementNS("http://www.w3.org/2000/svg", "svg");
        svgElement.setAttribute("xmlns", "http://www.w3.org/2000/svg");
        svgElement.setAttribute("width", "20");
        svgElement.setAttribute("height", "20");
        svgElement.setAttribute("fill", "none");
        svgElement.setAttribute("viewBox", "0 0 20 20");

        const pathElement = document.createElementNS("http://www.w3.org/2000/svg", "path");

        pathElement.setAttribute("fill", "#F8BD7E");
        const unfilledD = "M16.78 20a2.333 2.333 0 0 1-1.655-.696L10 14.21l-5.125 5.099a2.333 2.333 0 0 1-2.58.507 2.333 2.333 0 0 1-1.462-2.187V4.167A4.167 4.167 0 0 1 5 0h10a4.166 4.166 0 0 1 4.166 4.167v13.461a2.333 2.333 0 0 1-1.459 2.187 2.391 2.391 0 0 1-.926.185ZM5 1.667a2.5 2.5 0 0 0-2.5 2.5v13.461a.703.703 0 0 0 1.197.5l5.72-5.684a.833.833 0 0 1 1.174 0l5.713 5.683a.703.703 0 0 0 1.197-.5V4.167a2.5 2.5 0 0 0-2.5-2.5H5Z";
        const filledD = "M1.962 19.427a2.46 2.46 0 0 0 2.722-.536l4.904-4.878 4.904 4.878a2.463 2.463 0 0 0 2.725.537 2.46 2.46 0 0 0 1.537-2.31V3.97a4.172 4.172 0 0 0-4.166-4.167h-10A4.172 4.172 0 0 0 .42 3.97v13.15a2.46 2.46 0 0 0 1.54 2.308Z";

        pathElement.setAttribute("fill", "#F8BD7E");
        pathElement.setAttribute("d", isScrapped ? filledD : unfilledD);

        const exploreArticleArticle = document.createElement('article');
        exploreArticleArticle.className = 'explore-article-article';
        exploreArticleArticle.dataset.projectionId = '2';

        const exploreArticleDetails = document.createElement('div');
        exploreArticleDetails.className = 'explore-article-details';

        const exploreArticleCard = document.createElement('div');
        exploreArticleCard.className = 'explore-article-card';
        exploreArticleCard.onclick = function () {
            if (article.isPublic) {
                moveArticlePage(article.id);
            } else {
                showToast("해당 아티클은 작성자가 비공개로 전환 하였습니다.", false)
            }
        };

        if (type === 'IMAGE') {
            const imgElement = document.createElement('img');
            imgElement.className = 'explore-image';
            imgElement.src = content;
            exploreArticleCard.appendChild(imgElement);
        } else if (type === 'LINK') {
            const linkImage = article.openGraphResponse.image;
            if (linkImage) {
                const imgElement = document.createElement('img');
                imgElement.className = 'explore-image';
                imgElement.src = linkImage;
                exploreArticleCard.appendChild(imgElement);
            }
        } else if (type === 'TEXT') {
            const textContentElement = document.createElement('p');
            textContentElement.textContent = content;
            exploreArticleCard.appendChild(textContentElement);
        }

        exploreArticleDetails.appendChild(exploreArticleCard);

        const profileImageElement = document.createElement('img');
        profileImageElement.className = 'explore-profile-image';
        profileImageElement.src = profileImage;
        profileImageElement.style.width = '25px';
        profileImageElement.style.height = '25px';
        profileImageElement.style.objectFit = 'cover';


        const nicknameElement = document.createElement('small');
        nicknameElement.className = 'explore-article-nickname';
        nicknameElement.textContent = nickname;

        const nameTagWrapper = document.createElement('div');
        nameTagWrapper.className = 'explore-article-info';
        nameTagWrapper.appendChild(profileImageElement);
        nameTagWrapper.appendChild(nicknameElement);

        const nameElement = document.createElement('small');
        nameElement.className = 'explore-article-name';
        nameElement.textContent = title;
        nameElement.onclick = function () {
            moveArticlePage(article.id);
        };

        svgElement.setAttribute("xmlns", "http://www.w3.org/2000/svg");
        svgElement.setAttribute("width", "20");
        svgElement.setAttribute("height", "20");
        svgElement.setAttribute("fill", "none");
        svgElement.setAttribute("viewBox", "0 0 20 20");

        svgElement.appendChild(pathElement);
        svgElement.addEventListener('click', async function () {
            const d = pathElement.getAttribute('d');

            const articleId = article.id;

            if (d === unfilledD) {
                const response = await fetch(`/api/scraps`, {
                    method: 'POST',
                    headers: {
                        'Authorization': accessToken,
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({articleId: articleId})
                });

                if (response.ok) {
                    pathElement.setAttribute('d', filledD);
                    showToast("스크랩 되었습니다.", false);
                }
            } else {
                const response = await fetch(`/api/scraps/${articleId}`, {
                    method: 'DELETE',
                    headers: {
                        'Authorization': accessToken
                    }
                });

                if (response.ok) {
                    pathElement.setAttribute('d', unfilledD);
                    showToast("스크랩이 취소되었습니다.", false);
                }
            }
        });
        nameTagWrapper.appendChild(svgElement);

        const tagWrapper = document.createElement('div');
        tagWrapper.className = 'explore-article-tag-wrapper';

        nameTagWrapper.style.display = 'flex';
        nameTagWrapper.style.justifyContent = 'space-between';

        svgElement.style.marginLeft = 'auto';

        if (tags.length > 0) {
            tags.forEach(tag => {
                const tagElement = document.createElement('small');
                tagElement.className = 'explore-article-tag';
                tagElement.textContent = `#${tag.name}`;
                tagWrapper.appendChild(tagElement);
            });
        }

        exploreArticleDetails.appendChild(nameTagWrapper);
        exploreArticleDetails.appendChild(nameElement);
        exploreArticleDetails.appendChild(tagWrapper);

        exploreArticleArticle.appendChild(exploreArticleDetails);
        exploreArticleSection.appendChild(exploreArticleArticle);
    }));
}

function moveArticlePage(articleId) {
    const queryParams = new URLSearchParams({id: articleId});
    const url = `/article?${queryParams.toString()}`;
    window.location.href = url;
}

function infiniteScroll() {
    const articleListContainer = document.querySelector('.explore-article-section');
    currentPage = 1;

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

        const selectedTagsName = Array.from(document.querySelectorAll('.home-tag-button.selected'))
            .map(button => button.value);
        const selectedTags = selectedTagsName.map(tagName => encodeURIComponent(tagName));
        const tagNames = selectedTags.join(',');
        const sortOrder = getSortOrder();

        const response = await fetch(`/api/articles/explore?tagNames=${tagNames}&sortOrder=${sortOrder}&page=${currentPage + 1}`, {
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
