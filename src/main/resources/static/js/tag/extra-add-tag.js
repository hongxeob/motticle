function goBack() {
    window.history.back();
}

let accessToken = localStorage.getItem('accessToken');
const urlParams = new URLSearchParams(window.location.search);
const articleId = urlParams.get('articleId');

document.addEventListener('DOMContentLoaded', function () {
    fetchAndDisplayTags();
});

function fetchAndDisplayTags() {
    fetch('/api/tags', {
        method: 'GET',
        headers: {
            'Authorization': accessToken
        }
    })
        .then(response => {
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
                                'Authorization': result.accessToken,
                            }
                        })
                            .then(response => response.json());
                    });
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

                const tagItemWrapper = document.createElement('div');
                const tagItem = document.createElement('button');
                tagItem.classList.add("tag-list-button");
                tagItem.textContent = `${tag.name}`;
                tagItem.addEventListener('click', function () {
                    tagging(tag.id);
                });

                const deleteButtonWrapper = document.createElement('div');
                const deleteButton = document.createElement('span');
                deleteButton.classList.add("delete-tag-button")
                deleteButton.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="17" fill="none"><g clip-path="url(#a)"><path fill="#A6A6A6" d="M12.666 1.417H6.637a3.15 3.15 0 0 0-1.465.358 3.36 3.36 0 0 0-1.166 1.01L.14 8.064a.737.737 0 0 0 0 .87l3.866 5.28c.31.428.71.773 1.166 1.01.456.238.957.36 1.465.358h6.03a3.241 3.241 0 0 0 2.355-1.038A3.662 3.662 0 0 0 16 12.042V4.958a3.661 3.661 0 0 0-.978-2.503 3.241 3.241 0 0 0-2.356-1.038Zm-.862 8.707a.744.744 0 0 1 .008 1.01.665.665 0 0 1-.218.155.633.633 0 0 1-.732-.163L9.333 9.502l-1.529 1.624a.646.646 0 0 1-.469.199.648.648 0 0 1-.465-.208.731.731 0 0 1-.195-.495.733.733 0 0 1 .187-.498L8.39 8.5 6.862 6.876a.733.733 0 0 1-.187-.498.731.731 0 0 1 .195-.495.648.648 0 0 1 .465-.208.646.646 0 0 1 .47.2l1.528 1.623 1.529-1.624a.646.646 0 0 1 .469-.199.648.648 0 0 1 .465.208.731.731 0 0 1 .196.495.732.732 0 0 1-.188.498L10.276 8.5l1.528 1.624Z"/></g><defs><clipPath id="a"><path fill="#fff" d="M0 0h16v17H0z"/></clipPath></defs></svg>';
                deleteButton.addEventListener('click', function () {
                    deleteTag(tag.id);
                });

                const tagContainer = document.createElement('div');
                tagContainer.classList.add("added-tag-container");
                tagItemWrapper.appendChild(tagItem);
                hashTagWrapper.appendChild(hashTag);
                deleteButtonWrapper.appendChild(deleteButton)

                tagContainer.appendChild(hashTagWrapper);
                tagContainer.appendChild(tagItemWrapper);
                tagContainer.appendChild(deleteButtonWrapper);

                tagListContainer.appendChild(tagContainer);
            });
        })
        .catch(error => console.error('태그를 가져오는 중 에러 발생:', error));
}

function tagging(tagId) {
    const tagNameInput = document.getElementById('tagNameInput');
    const tagAlert = document.querySelector('.tagAlert');

    fetch(`/api/articles/${articleId}/tags`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': accessToken
        },
        body: JSON.stringify({
            tagId: tagId,
        }),
    })
        .then(articleResponse => {
            if (articleResponse.status === 400) {
                tagAlert.textContent = "이미 등록된 태그입니다. 아래에서 확인해주세요.";
                tagAlert.style.color = "red";
                tagNameInput.style.border = "1px solid red";

                tagNameInput.focus();
                fetchAndDisplayTags();
                tagNameInput.value = '';
                return;
            } else if (articleResponse.status === 401) {
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
                        return fetch(`/api/articles/${articleId}/tags`, {
                            method: "POST",
                            headers: {
                                'Authorization': result.accessToken,
                                'Content-Type': 'application/json'
                            },
                            body: JSON.stringify({
                                tagId: tagId,
                            }),
                        })
                            .then(response => response.json());
                    });
            }
            return articleResponse.json();
        })
        .then(articleInfoRes => {
            if (articleInfoRes) {
                console.log('아티클에 태그가 추가되었습니다.', articleInfoRes);
                fetchAndDisplayTags();
                showToast('태그 추가 및 해당 아티클에 바로 태깅되었습니다!', false);

                tagNameInput.value = '';
            }
        })
        .catch(error => console.error('아티클에 태그 추가 중 에러 발생:', error));
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
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': accessToken
        },
        body: JSON.stringify({
            name: tagName,
        }),
    }).then(response => {
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
                        method: "POST",
                        headers: {
                            'Authorization': result.accessToken,
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({
                            name: tagName,
                        }),
                    })
                        .then(response => response.json());
                });
        } else {
            return response.json();
        }
    })
        .then(tag => {
            const tagId = tag.id;

            fetch(`/api/articles/${articleId}/tags`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': accessToken
                },
                body: JSON.stringify({
                    tagId: tagId,
                }),
            })
                .then(articleResponse => {
                    if (articleResponse.status === 400) {
                        tagAlert.textContent = "이미 등록된 태그입니다. 아래에서 확인해주세요.";
                        tagAlert.style.color = "red";
                        tagNameInput.style.border = "1px solid red";
                        tagNameInput.focus();
                        fetchAndDisplayTags();
                        return;
                    } else if (articleResponse.status === 401) {
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
                                return fetch(`/api/articles/${articleId}/tags`, {
                                    method: "POST",
                                    headers: {
                                        'Authorization': accessToken,
                                        'Content-Type': 'application/json'
                                    },
                                    body: JSON.stringify({
                                        tagId: tagId,
                                    }),
                                })
                                    .then(response => response.json());
                            });
                    }
                    return articleResponse.json();
                })
                .then(articleInfoRes => {
                    if (articleInfoRes) {
                        console.log('아티클에 태그가 추가되었습니다.', articleInfoRes);
                        tagAlert.textContent = "해당 아티클에 태깅할 태그를 입력해주세요.";
                        tagAlert.style.color = "rgb(163, 179, 186)";
                        tagNameInput.style.border = "none";

                        fetchAndDisplayTags();
                        tagNameInput.value = '';
                        showToast('태그 추가 및 해당 아티클에 바로 태깅되었습니다!', false);
                    }
                })
                .catch(error => console.error('아티클에 태그 추가 중 에러 발생:', error));
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
            showToast("해당 태그가 포함된 아티클에서 태그 해제 및 삭제 되었습니다.");
            fetchAndDisplayTags();
        })
        .catch(error => console.error('An error occurred while deleting tag:', error))
}
