function goBack() {
    window.history.back();
}

const isPublicCheckbox = document.getElementById('isPublic');

isPublicCheckbox.addEventListener('change', function () {
    if (this.checked) {
        console.log('Switch is ON');
    } else {
        console.log('Switch is OFF');
    }
});
const accessToken = localStorage.getItem('accessToken');
const urlParams = new URLSearchParams(window.location.search);
const selectedType = urlParams.get('type');

function navigateToAddTag() {
    window.location.href = '/addTag';
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
        // ifLinkTypeDiv.style.marginTop = '205px';
        // buttonContainer.style.marginBottom = '0px';
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
        'Authorization': accessToken
    }
})
    .then(response => {
        if (!response.ok) {
            throw new Error(`Error fetching user tags: ${response.statusText}`);
        }
        return response.json();
    })
    .then(tags => {
        displayUserTags(tags.tagSliceRes);
    })
    .catch(error => console.error(error));


function registerArticle() {

    const accessToken = localStorage.getItem('accessToken');
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
        .then(response => response.json())
        .then(articleInfoRes => {
            console.log('아티클이 성공적으로 등록되었습니다:', articleInfoRes);
            alert("아티클이 등록 되었습니다!")
            window.location.href = '/';
        })
        .catch(error => {
            console.error('아티클 등록 중 오류 발생:', error);
        });
}

function validateLink() {
    const link = document.getElementById('articleLink').value;

    fetch(`/api/articles/validation?link=${link}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('서버 오류');
            }
            return response.json();
        })
        .then(openGraphResponse => {
            displayLinkPreview(openGraphResponse);
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
