function showToast(message, isError) {
    const toastElement = document.getElementById('toast');

    if (isError) {
        toastElement.style.backgroundColor = '#e74c3c';
        toastElement.style.color = '#fff';
    } else {
        toastElement.style.backgroundColor = '#2DD197';
        toastElement.style.color = '#fff';
    }

    toastElement.textContent = message;
    toastElement.style.bottom = '15px';

    setTimeout(() => {
        toastElement.style.bottom = '-100px';
    }, 3000);
}
