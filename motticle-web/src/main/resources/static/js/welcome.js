window.onload = function () {
    const homeButton = document.querySelector('.nav-home-button');
    homeButton.addEventListener('click', function () {
        showToast("로그인 후 이용해주세요.", false);
    });

    const myPageButton = document.querySelector('.welcome_my_page_button');
    myPageButton.addEventListener('click', function () {
        showToast("로그인 후 이용해주세요.", false);
    });
    const homeTagButton = document.querySelector('.home-tag-add-button');
    homeTagButton.addEventListener('click', function () {
        showToast("로그인 후 이용해주세요.", false);
    });
}

const viewContainer = document.querySelector('.home-view');
const noArticlesImage = `
        <img src="/static/images/cup of water notebook and pencil.png" alt="No Articles Image" width="100%" height="100%">
    `;
let noArticlesContainer = document.createElement('div')
noArticlesContainer.className = 'noArticleContainer';
noArticlesContainer.style.position = 'fixed';
noArticlesContainer.style.top = '50%';
noArticlesContainer.style.left = '50%';
noArticlesContainer.style.transform = 'translate(-50%, -50%)';
noArticlesContainer.innerHTML = noArticlesImage;
viewContainer.appendChild(noArticlesContainer);
noArticlesContainer.style.display = 'none';

let currentPage = 1;

function changeSortOrder(order) {
    currentPage = 1;

    const sortOrder = order === 'oldest' ? 'oldest' : (order === 'scrap-count' ? 'scrap-count' : null);
    fetchAndRenderArticles(sortOrder);
}

async function fetchAndRenderData() {
    try {
        const response = await fetch('/api/tags/welcome');
        if (response.ok) {
            const data = await response.json();
            const tags = data.tagRes;
            renderTags(tags);
        } else {
            console.error('Error fetching tags:', response.statusText);
        }
    } catch (error) {
        console.error('Error fetching tags:', error);
    }
}

fetchAndRenderData().then(() => {
    fetchAndRenderArticles();
});

function renderTags(tags) {
    const tagButtonsContainer = document.getElementById('home-tagButtonsContainer');

    if (tags.length === 0) {
        noArticlesContainer.style.display = 'block';

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

    noArticlesContainer.style.display = 'none';

    const spinner = document.getElementById('spinner');

    spinner.style.display = 'block';
    const articleListContainer = document.querySelector('.explore-article-section');
    articleListContainer.innerHTML = '';

    let tagNames = '';

    if (selectedTagsBeforeAlert.length > 0) {
        const selectedTagsName = Array.from(document.querySelectorAll('.home-tag-button.selected'))
            .map(button => button.value);
        const selectedTags = selectedTagsName.map(tagName => encodeURIComponent(tagName));
        tagNames = selectedTags.join(',');
    }

    try {
        const articlesResponse = await fetch(`/api/articles/welcome?tagNames=${tagNames}&sortOrder=${sortOrder}`);

        if (articlesResponse.ok) {
            const articlesData = await articlesResponse.json();
            console.log('Filtered Articles Data:', articlesData);

            if (articlesData.articleOgResList.length === 0) {
                noArticlesContainer.style.display = 'block';
                sortDropdown.style.display = 'none';
            } else {
                sortDropdown.style.display = 'block';
                renderArticles(articlesData);
                hasNextPage = articlesData.hasNext;
            }
        } else {
            console.error('Error fetching articles:', articlesResponse.statusText);
        }
    } catch (error) {
        console.error('Error fetching articles:', error);
    } finally {
        spinner.style.display = 'none';
    }
}

async function renderArticles(articlesData) {
    const exploreArticleSection = document.querySelector('.explore-article-section');

    await Promise.all(articlesData.articleOgResList.map(async (article) => {
                const articleId = article.id;
                const nickname = article.memberInfoRes.nickname;
                const profileImage = article.memberInfoRes.image;
                const title = article.title;
                const content = article.content;
                const type = article.type;
                const tags = article.tagsRes.tagRes || [];

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

                const nameWrapper = document.createElement('div');
                nameWrapper.className = 'explore-tag-wrapper';
                const nameElement = document.createElement('small');

                nameElement.className = 'explore-article-name';
                nameElement.textContent = title;
                nameElement.onclick = function () {
                    moveArticlePage(article.id);
                };
                nameWrapper.appendChild(nameElement);

                const tagWrapper = document.createElement('div');
                tagWrapper.className = 'explore-article-tag-wrapper';

                nameTagWrapper.style.display = 'flex';
                nameTagWrapper.style.justifyContent = 'space-between';

                if (tags.length > 0) {
                    tags.forEach(tag => {
                        const tagElement = document.createElement('small');
                        tagElement.className = 'explore-article-tag';
                        tagElement.textContent = `#${tag.name}`;
                        tagWrapper.appendChild(tagElement);
                    });
                }

                exploreArticleDetails.appendChild(nameTagWrapper);
                exploreArticleDetails.appendChild(nameWrapper);
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
    window.location.href = '/add-tag';
}

function moveSearchPage() {
    window.location.href = '/welcome/search';
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

        const response = await fetch(`/api/articles/welcome?tagNames=${tagNames}&sortOrder=${sortOrder}&page=${currentPage + 1}`);
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
    const sortDropdown = document.getElementById('sortDropdown');
    const selectedOption = sortDropdown.value;

    if (selectedOption === 'latest') {
        return 'latest';
    } else if (selectedOption === 'oldest') {
        return 'oldest';
    } else if (selectedOption === 'scrap-count') {
        return 'scrap-count';
    } else {
        return 'latest';
    }
}
