function goBack() {
    window.history.back();
}

let accessToken = localStorage.getItem('accessToken');

if (accessToken === null) {
    redirectToKakaoScreen();
}

function redirectToKakaoScreen() {
    window.location.href = '/kakao';
}

document.addEventListener('DOMContentLoaded', function () {
    fetchAndDisplayTags();
});

function fetchAndDisplayTags() {
    fetch('/api/tags', {
        method: 'GET', headers: {
            'Authorization': accessToken
        }
    })
        .then(response => {
            if (!response.ok) {
                if (response.status === 401) {
                    console.log("Token reissue");
                    return fetch("/api/auth/reissue", {
                        method: "PATCH",
                        headers: {
                            'Authorization': accessToken
                        }
                    })
                        .then(res => {
                            if (res.ok) {
                                return res.json();
                            } else {
                                throw new Error('Failed to reissue token');
                            }
                        })
                        .then(result => {
                            accessToken = result.accessToken;
                            localStorage.setItem('accessToken', accessToken);
                            return fetch('/api/tags', {
                                headers: {
                                    'Authorization': accessToken,
                                }
                            })
                                .then(response => response.json());
                        });
                } else {
                    throw new Error(`Error fetching user tags: ${response.statusText}`);
                }
            }
            return response.json();
        })
        .then(tags => {
            const tagListContainer = document.getElementById('tagList');
            tagListContainer.innerHTML = '';
            const tagSliceRes = tags.tagSliceRes;

            tagSliceRes.forEach(tag => {
                const hashTagWrapper = document.createElement('div');
                hashTagWrapper.classList.add("hash-tag-wrapper")
                const hashTag = document.createElement('span');
                hashTag.classList.add("hash-tag");
                hashTag.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="none"><path stroke="#14E69C" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 6.75h12M3 11.25h12M7.5 2.25 6 15.75M12 2.25l-1.5 13.5"/></svg>';

                const tagItem = document.createElement('button');
                tagItem.classList.add("tag-list-button");
                tagItem.textContent = `${tag.name}`;

                const deleteButtonWrapper = document.createElement('div');
                const deleteButton = document.createElement('span');
                deleteButton.classList.add("delete-tag-button")
                deleteButton.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="17" fill="none"><g clip-path="url(#a)"><path fill="#A6A6A6" d="M12.666 1.417H6.637a3.15 3.15 0 0 0-1.465.358 3.36 3.36 0 0 0-1.166 1.01L.14 8.064a.737.737 0 0 0 0 .87l3.866 5.28c.31.428.71.773 1.166 1.01.456.238.957.36 1.465.358h6.03a3.241 3.241 0 0 0 2.355-1.038A3.662 3.662 0 0 0 16 12.042V4.958a3.661 3.661 0 0 0-.978-2.503 3.241 3.241 0 0 0-2.356-1.038Zm-.862 8.707a.744.744 0 0 1 .008 1.01.665.665 0 0 1-.218.155.633.633 0 0 1-.732-.163L9.333 9.502l-1.529 1.624a.646.646 0 0 1-.469.199.648.648 0 0 1-.465-.208.731.731 0 0 1-.195-.495.733.733 0 0 1 .187-.498L8.39 8.5 6.862 6.876a.733.733 0 0 1-.187-.498.731.731 0 0 1 .195-.495.648.648 0 0 1 .465-.208.646.646 0 0 1 .47.2l1.528 1.623 1.529-1.624a.646.646 0 0 1 .469-.199.648.648 0 0 1 .465.208.731.731 0 0 1 .196.495.732.732 0 0 1-.188.498L10.276 8.5l1.528 1.624Z"/></g><defs><clipPath id="a"><path fill="#fff" d="M0 0h16v17H0z"/></clipPath></defs></svg>';
                deleteButton.addEventListener('click', function () {
                    deleteTag(tag.id);
                });

                const tagContainer = document.createElement('div');
                tagContainer.classList.add("added-tag-container");
                hashTagWrapper.appendChild(hashTag);
                hashTagWrapper.appendChild(tagItem);
                deleteButtonWrapper.appendChild(deleteButton)
                tagContainer.appendChild(hashTagWrapper);
                tagContainer.appendChild(deleteButtonWrapper);

                tagListContainer.appendChild(tagContainer);
            });
        })
        .catch(error => console.error('태그를 가져오는 중 에러 발생:', error));
}

function registerTag() {
    const tagNameInput = document.getElementById('tagNameInput');
    const tagName = tagNameInput.value;
    const tagAlert = document.querySelector('.tagAlert');

    if (tagName.trim() === '') {
        tagAlert.textContent = "태그를 입력해주세요.";
        tagAlert.style.color = "red";
        tagNameInput.style.border = "1px solid red";

        tagNameInput.focus();
        return;
    }

    fetch('/api/tags', {
        method: 'POST', headers: {
            'Content-Type': 'application/json',
            'Authorization': accessToken
        }, body: JSON.stringify({
            name: tagName,
        }),
    })
        .then(response => {
            if (response.status === 400) {
                tagAlert.textContent = "이미 등록된 태그입니다. 아래에서 확인해주세요.";
                tagAlert.style.color = "red";
                tagNameInput.style.border = "1px solid red";

                tagNameInput.focus();
                throw new Error('Duplicate tag');
            } else if (response.status === 401) {
                console.log("Token reissue");
                return fetch("/api/auth/reissue", {
                    method: "PATCH",
                    headers: {
                        'Authorization': accessToken
                    }
                })
                    .then(res => {
                        if (res.ok) {
                            return res.json();
                        } else {
                            throw new Error('Failed to reissue token');
                        }
                    })
                    .then(result => {
                        accessToken = result.accessToken;
                        localStorage.setItem('accessToken', accessToken);
                        return fetch('/api/tags', {
                            method: "POST",
                            headers: {
                                'Authorization': accessToken,
                                'Content-Type': 'application/json'
                            }, body: JSON.stringify({
                                name: tagName,
                            })
                        })
                            .then(response => response.json());
                    });
            }
            return response.json();
        })
        .then(() => {
            tagAlert.textContent = "등록할 태그를 입력하세요.";
            tagAlert.style.color = "rgb(163, 179, 186)";
            tagNameInput.style.border = "none";

            fetchAndDisplayTags();
            showToast('태그가 등록 되었습니다. 연결할 태그를 아티클 등록 화면에서 클릭해주세요!', false);
            tagNameInput.value = '';
        })
        .catch(error => {
            if (error.message !== 'Duplicate tag') {
                console.error('태그 등록 중 에러 발생:', error);
            }
        });
}

function deleteTag(tagId) {
    fetch(`/api/tags/${tagId}`, {
        method: 'DELETE',
        headers: {
            'Authorization': accessToken
        },
    })
        .then(response => {
            if (response.ok) {
                return response.text();
            } else if (response.status === 401) {
                // Handle unauthorized response
                return fetch("/api/auth/reissue", {
                    method: "PATCH",
                    headers: {
                        'Authorization': accessToken
                    }
                })
                    .then(reissueResponse => {
                        if (reissueResponse.ok) {
                            console.log("token reissued");
                            return reissueResponse.json();
                        } else {
                            throw new Error('Failed to reissue token');
                        }
                    })
                    .then(result => {
                        accessToken = result.accessToken;
                        localStorage.setItem('accessToken', accessToken);
                        return fetch(`/api/tags/${tagId}`, {
                            method: 'DELETE',
                            headers: {
                                'Authorization': accessToken
                            },
                        });
                    });
            } else {
                throw new Error(`Failed to delete tag: ${response.status} ${response.statusText}`);
            }
        })
        .then(() => {
            fetchAndDisplayTags();
        })
        .catch(error => console.error('An error occurred while deleting tag:', error))
}
