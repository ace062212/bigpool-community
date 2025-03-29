/**
 * 재사용 가능한 UI 컴포넌트 함수 모음
 */

/**
 * 토스트 메시지를 보여주는 함수
 * @param {string} message - 표시할 메시지
 * @param {string} type - 메시지 타입(success, error, warning, info)
 * @param {number} duration - 토스트가 표시될 시간(ms)
 */
function showToast(message, type = 'success', duration = 3000) {
    // 기존 토스트 제거
    const existingToasts = document.querySelectorAll('.toast-message');
    existingToasts.forEach(toast => {
        if (toast && toast.parentNode) {
            toast.parentNode.removeChild(toast);
        }
    });
    
    // 새 토스트 생성
    const toast = document.createElement('div');
    toast.className = `toast-message toast-${type}`;
    toast.innerHTML = `
        <div class="toast-content">
            <i class="fas ${getIconForType(type)}"></i>
            <div class="toast-text">${message}</div>
            <button class="toast-close"><i class="fas fa-times"></i></button>
        </div>
        <div class="toast-progress"></div>
    `;
    
    document.body.appendChild(toast);
    
    // 프로그레스 바 애니메이션 설정
    const progress = toast.querySelector('.toast-progress');
    progress.style.animation = `toast-progress ${duration/1000}s linear forwards`;
    
    // 토스트 표시
    setTimeout(() => {
        toast.classList.add('show');
    }, 10);
    
    // 닫기 버튼 이벤트
    const closeBtn = toast.querySelector('.toast-close');
    closeBtn.addEventListener('click', () => {
        hideToast(toast);
    });
    
    // 자동 숨김
    setTimeout(() => {
        hideToast(toast);
    }, duration);
    
    return toast;
}

/**
 * 토스트 메시지 숨김 함수
 * @param {HTMLElement} toast - 숨길 토스트 요소
 */
function hideToast(toast) {
    toast.classList.remove('show');
    setTimeout(() => {
        if (toast && toast.parentNode) {
            toast.parentNode.removeChild(toast);
        }
    }, 300);
}

/**
 * 토스트 타입에 따른 아이콘 클래스를 반환하는 함수
 * @param {string} type - 메시지 타입
 * @return {string} - Font Awesome 아이콘 클래스
 */
function getIconForType(type) {
    switch (type) {
        case 'success':
            return 'fa-check-circle';
        case 'error':
            return 'fa-exclamation-circle';
        case 'warning':
            return 'fa-exclamation-triangle';
        case 'info':
            return 'fa-info-circle';
        default:
            return 'fa-bell';
    }
}

/**
 * 확인 대화상자를 표시하는 함수
 * @param {string} message - 확인 메시지
 * @param {Function} onConfirm - 확인 시 콜백 함수
 * @param {Function} onCancel - 취소 시 콜백 함수
 */
function showConfirm(message, onConfirm, onCancel) {
    // 기존 모달 백드롭 활용
    const modalBackdrop = document.querySelector('.modal-backdrop');
    if (!modalBackdrop) return;
    
    // 확인 대화상자 생성
    const confirmDialog = document.createElement('div');
    confirmDialog.className = 'confirm-dialog';
    confirmDialog.innerHTML = `
        <div class="confirm-content">
            <div class="confirm-message">${message}</div>
            <div class="confirm-buttons">
                <button class="btn btn-secondary btn-cancel">취소</button>
                <button class="btn btn-primary btn-confirm">확인</button>
            </div>
        </div>
    `;
    
    // 모달 배경 표시
    modalBackdrop.classList.add('show');
    document.body.appendChild(confirmDialog);
    
    // 대화상자 표시
    setTimeout(() => {
        confirmDialog.classList.add('show');
    }, 10);
    
    // 이벤트 핸들러
    const confirmBtn = confirmDialog.querySelector('.btn-confirm');
    const cancelBtn = confirmDialog.querySelector('.btn-cancel');
    
    // 확인 버튼 클릭
    confirmBtn.addEventListener('click', () => {
        hideConfirm(confirmDialog, modalBackdrop);
        if (typeof onConfirm === 'function') {
            onConfirm();
        }
    });
    
    // 취소 버튼 클릭
    cancelBtn.addEventListener('click', () => {
        hideConfirm(confirmDialog, modalBackdrop);
        if (typeof onCancel === 'function') {
            onCancel();
        }
    });
    
    // 배경 클릭 시 취소
    modalBackdrop.addEventListener('click', function(e) {
        if (e.target === modalBackdrop) {
            hideConfirm(confirmDialog, modalBackdrop);
            if (typeof onCancel === 'function') {
                onCancel();
            }
        }
    });
    
    // ESC 키 누를 때 취소
    const escHandler = function(e) {
        if (e.key === 'Escape') {
            hideConfirm(confirmDialog, modalBackdrop);
            if (typeof onCancel === 'function') {
                onCancel();
            }
            document.removeEventListener('keydown', escHandler);
        }
    };
    document.addEventListener('keydown', escHandler);
    
    return confirmDialog;
}

/**
 * 확인 대화상자를 숨기는 함수
 * @param {HTMLElement} dialog - 숨길 대화상자 요소
 * @param {HTMLElement} backdrop - 모달 배경 요소
 */
function hideConfirm(dialog, backdrop) {
    dialog.classList.remove('show');
    backdrop.classList.remove('show');
    setTimeout(() => {
        if (dialog && dialog.parentNode) {
            dialog.parentNode.removeChild(dialog);
        }
    }, 300);
}

/**
 * 이미지 모달을 표시하는 함수
 * @param {string} src - 이미지 URL
 * @param {string} alt - 이미지 대체 텍스트
 */
function showImageModal(src, alt = '') {
    // 기존 모달 백드롭 활용
    const modalBackdrop = document.querySelector('.modal-backdrop');
    if (!modalBackdrop) return;
    
    // 이미지 모달 생성
    const imageModal = document.createElement('div');
    imageModal.className = 'image-modal';
    imageModal.innerHTML = `
        <div class="image-modal-content">
            <button class="image-modal-close">&times;</button>
            <img src="${src}" alt="${alt}" class="image-modal-img">
        </div>
    `;
    
    // 모달 배경 표시
    modalBackdrop.classList.add('show');
    document.body.appendChild(imageModal);
    
    // 모달 표시
    setTimeout(() => {
        imageModal.classList.add('show');
    }, 10);
    
    // 닫기 버튼 이벤트
    const closeBtn = imageModal.querySelector('.image-modal-close');
    closeBtn.addEventListener('click', () => {
        hideImageModal(imageModal, modalBackdrop);
    });
    
    // 배경 클릭 시 닫기
    modalBackdrop.addEventListener('click', function(e) {
        if (e.target === modalBackdrop) {
            hideImageModal(imageModal, modalBackdrop);
        }
    });
    
    // ESC 키 누를 때 닫기
    const escHandler = function(e) {
        if (e.key === 'Escape') {
            hideImageModal(imageModal, modalBackdrop);
            document.removeEventListener('keydown', escHandler);
        }
    };
    document.addEventListener('keydown', escHandler);
    
    return imageModal;
}

/**
 * 이미지 모달을 숨기는 함수
 * @param {HTMLElement} modal - 숨길 모달 요소
 * @param {HTMLElement} backdrop - 모달 배경 요소
 */
function hideImageModal(modal, backdrop) {
    modal.classList.remove('show');
    backdrop.classList.remove('show');
    setTimeout(() => {
        if (modal && modal.parentNode) {
            modal.parentNode.removeChild(modal);
        }
    }, 300);
}

// 필요한 경우 전역 객체에 노출
window.UI = {
    showToast,
    hideToast,
    showConfirm,
    hideConfirm,
    showImageModal,
    hideImageModal
}; 