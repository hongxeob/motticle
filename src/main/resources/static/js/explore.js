accessToken = localStorage.getItem('accessToken');

window.onload = function () {
    const selectedTags = document.querySelectorAll('.home-tag-button.selected');
    selectedTags.forEach(tag => tag.classList.remove('selected'));
    selectRandomTag();
}

if (accessToken === null) {
    redirectToKakaoScreen();
}

function redirectToKakaoScreen() {
    window.location.href = '/kakao';
}

const viewContainer = document.querySelector('.home-view');
const noArticlesImage = `
        <img src="/static/images/cup of water notebook and pencil.png" alt="No Articles Image" width="100%" height="100%">
    `;
let noArticlesContainer = document.createElement('div')
noArticlesContainer.className = 'noArticleContainer';
noArticlesContainer.style.position = 'absolute';
noArticlesContainer.style.top = '302px';
noArticlesContainer.style.right = '102px';
noArticlesContainer.innerHTML = noArticlesImage;
viewContainer.appendChild(noArticlesContainer);
noArticlesContainer.style.display = 'none';

function selectRandomTag() {
    const tagButtons = document.querySelectorAll('.home-tag-button');

    if (tagButtons.length > 0) {
        const randomIndex = Math.floor(Math.random() * tagButtons.length);

        tagButtons[randomIndex].click();
    }
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
    try {
        const latestButton = document.getElementById('latestButton');
        const oldestButton = document.getElementById('oldestButton');

        latestButton.style.display = 'none';
        oldestButton.style.display = 'none';

        const response = await fetch('/api/tags', {
            headers: {
                'Authorization': accessToken
            }
        });

        if (response.ok) {
            const data = await response.json();
            const tags = data.tagSliceRes;
            renderTags(tags);
            selectRandomTag();
        } else if (response.status === 401) {
            await handleUnauthorizedResponse();
        } else {
            console.error('Error fetching tags:', response.statusText);
        }
    } catch (error) {
        console.error('Error fetching tags:', error);
    }
}

async function handleUnauthorizedResponse() {
    try {
        const reissueResponse = await fetch("/api/auth/reissue", {
            method: "PATCH",
            headers: {
                'Authorization': accessToken
            }
        });

        if (reissueResponse.ok) {
            const result = await reissueResponse.json();
            accessToken = result.accessToken;
            localStorage.setItem('accessToken', accessToken);
            fetchAndRenderData();
        } else {
            console.error('Failed to reissue token:', reissueResponse.statusText);

        }
    } catch (error) {
        console.error('Error handling unauthorized response:', error);
    }
}

fetchAndRenderData();

async function handleUnauthorizedResponseWithArticles() {
    try {
        const reissueResponse = await fetch("/api/auth/reissue", {
            method: "PATCH",
            headers: {
                'Authorization': accessToken
            }
        });

        if (reissueResponse.ok) {
            const result = await reissueResponse.json();
            accessToken = result.accessToken;
            localStorage.setItem('accessToken', accessToken);
            fetchAndRenderArticles();
        } else {
            console.error('Failed to reissue token:', reissueResponse.statusText);

        }
    } catch (error) {
        console.error('Error handling unauthorized response:', error);

    }
}

function renderTags(tags) {
    const tagButtonsContainer = document.getElementById('home-tagButtonsContainer');

    if (tags.length === 0) {
        noArticlesContainer.style.display = 'block';

        const latestButton = document.getElementById('latestButton');
        const oldestButton = document.getElementById('oldestButton');
        latestButton.style.display = 'none';
        oldestButton.style.display = 'none';

        const placeholderText = document.createElement('div');
        placeholderText.textContent = "여기를 눌러 태그를 추가하세요";
        placeholderText.style.color = "#777";
        placeholderText.style.padding = "10px";
        placeholderText.style.cursor = "pointer";
        tagButtonsContainer.appendChild(placeholderText);

        noArticlesContainer.style.display = 'block';

        showToast("관심 있는 태그를 등록 후 사용해보세요.", false);
        const spinner = document.getElementById('spinner');
        spinner.style.display = 'none';
        return;
    } else {
        noArticlesContainer.style.display = 'none';
        tags.forEach(tag => {
            const tagButton = document.createElement('button');
            tagButton.type = 'button';
            tagButton.id = `tag${tag.id}`;
            tagButton.value = tag.name;
            tagButton.className = 'home-tag-button';
            tagButton.textContent = `#${tag.name}`;

            tagButton.addEventListener('click', function () {
                this.classList.toggle('selected');

                fetchAndRenderArticles();
            });
            tagButtonsContainer.appendChild(tagButton);
        });
    }
}

let hasNextPage;

async function fetchAndRenderArticles(sortOrder) {
    sortOrder = sortOrder || 'latest';
    const selectedTagsBeforeAlert = Array.from(document.querySelectorAll('.home-tag-button.selected'))
        .map(button => button.id);

    if (selectedTagsBeforeAlert.length === 0) {
        showToast("적어도 하나의 태그를 선택해주세요.", true);

        selectedTagsBeforeAlert.forEach(tagId => {
            const tagButton = document.getElementById(`tag${tagId}`);
            if (tagButton) {
                tagButton.classList.add('selected');
            }
        });

        return;
    }
    noArticlesContainer.style.display = 'none';

    const spinner = document.getElementById('spinner');
    const latestButton = document.getElementById('latestButton');
    const oldestButton = document.getElementById('oldestButton');

    latestButton.style.display = 'none';
    oldestButton.style.display = 'none';

    spinner.style.display = 'block';
    const articleListContainer = document.querySelector('.explore-article-section');
    articleListContainer.innerHTML = '';

    const selectedTagsName = Array.from(document.querySelectorAll('.home-tag-button.selected'))
        .map(button => button.value);

    const selectedTags = selectedTagsName.map(tagName => encodeURIComponent(tagName));
    const tagNames = selectedTags.join(',');

    try {
        const articlesResponse = await fetch(`/api/articles/explore?tagNames=${tagNames}&sortOrder=${sortOrder}`, {
            headers: {
                'Authorization': accessToken
            }
        });

        if (articlesResponse.ok) {
            const articlesData = await articlesResponse.json();
            console.log('Filtered Articles Data:', articlesData);

            if (articlesData.articleOgResList.length === 0) {
                noArticlesContainer.style.display = 'block';
                latestButton.style.display = 'none';
                oldestButton.style.display = 'none';
            } else {
                noArticlesContainer.style.display = 'none';
                if (sortOrder === 'latest') {
                    latestButton.style.display = 'none';
                    oldestButton.style.display = 'block';
                } else if (sortOrder === 'oldest') {
                    oldestButton.style.display = 'none';
                    latestButton.style.display = 'block';
                }
                renderArticles(articlesData);
                hasNextPage = articlesData.hasNext;
            }
        } else if (articlesResponse.status === 401) {
            await handleUnauthorizedResponseWithArticles();
        } else {
            console.error('Error fetching articles:', articlesResponse.statusText);
        }
    } catch (error) {
        console.error('Error fetching articles:', error);
    } finally {
        spinner.style.display = 'none';
    }
}

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
    } else if (response.status === 401) {
        const reissueResponse = await fetch("/api/auth/reissue", {
            method: "PATCH",
            headers: {
                'Authorization': accessToken
            }
        });
        if (reissueResponse.ok) {
            const result = await reissueResponse.json();
            accessToken = result.accessToken;
            localStorage.setItem('accessToken', accessToken);
            checkScrapedArticle(articleId);
        } else {
            console.error('Failed to reissue token:', reissueResponse.statusText);

        }
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
                    moveArticlePage(article.id);
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
                    exploreArticleCard.style.border = '1px solid #E6E6E6';
                    const textContentElement = document.createElement('p');
                    textContentElement.style.padding = '0 5px 0 5px';
                    textContentElement.style.margin = '3px 0 0 0px';
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
                            } else if (response.status === 401) {
                                const reissueResponse = await fetch("/api/auth/reissue", {
                                    method: "PATCH",
                                    headers: {
                                        'Authorization': accessToken
                                    }
                                });
                                if (reissueResponse.ok) {
                                    const result = await reissueResponse.json();
                                    accessToken = result.accessToken;
                                    localStorage.setItem('accessToken', accessToken);
                                    const retryResponse = await fetch(`/api/scraps`, {
                                        method: 'POST',
                                        headers: {
                                            'Authorization': accessToken,
                                            'Content-Type': 'application/json'
                                        },
                                        body: JSON.stringify({articleId: articleId})
                                    });
                                    if (retryResponse.ok) {
                                        pathElement.setAttribute('d', filledD);
                                        showToast("스크랩 되었습니다.", false);
                                    }
                                }
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
                            } else if (response.status === 401) {
                                const reissueResponse = await fetch("/api/auth/reissue", {
                                    method: "PATCH",
                                    headers: {
                                        'Authorization': accessToken
                                    }
                                });
                                if (reissueResponse.ok) {
                                    const result = await reissueResponse.json();
                                    accessToken = result.accessToken;
                                    localStorage.setItem('accessToken', accessToken);
                                    const response = await fetch(`/api/scraps/${articleId}`, {
                                        method: 'DELETE',
                                        headers: {
                                            'Authorization': accessToken
                                        }
                                    });

                                    if (response.ok) {
                                        pathElement.setAttribute('d', unfilledD);
                                    }
                                }
                            }
                        }

                    }
                )
                ;
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
            }
        )
    )
    ;
}

function moveArticlePage(articleId) {
    const queryParams = new URLSearchParams({id: articleId});
    const url = `/article?${queryParams.toString()}`;
    window.location.href = url;
}

function navigateToAddTag() {
    window.location.href = '/addTag';
}

function moveSearchPage() {
    window.location.href = '/explore/search';
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

function getSortOrder() {
    const latestButton = document.getElementById('latestButton');
    const oldestButton = document.getElementById('oldestButton');

    if (latestButton.style.display !== 'none') {
        return 'oldest';
    } else {
        return 'latest';
    }
}
