// 페이지 로딩 최적화 및 인터랙션 관련 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    // 사용자 아바타의 이니셜 설정
    const userInitials = document.querySelectorAll('.user-initial');
    userInitials.forEach(element => {
        if(element.textContent) {
            element.textContent = element.textContent.charAt(0).toUpperCase();
        }
    });
    
    // 로그인/회원가입 모달 제어
    const modalBackdrop = document.querySelector('.modal-backdrop');
    const loginModal = document.getElementById('loginModal');
    const registerModal = document.getElementById('registerModal');
    const loginBtns = document.querySelectorAll('.btn-login');
    const registerBtns = document.querySelectorAll('.btn-register:not(#logoutBtn)'); // 로그아웃 버튼 제외
    const closeBtns = document.querySelectorAll('.modal-close');
    
    // 모달과 배경 완전히 닫기 함수
    function closeAllModals() {
        if (!modalBackdrop) return;
        
        // 모달 닫기
        modalBackdrop.classList.remove('show');
        if (loginModal) loginModal.classList.remove('show');
        if (registerModal) registerModal.classList.remove('show');
        
        // 기존에 생성된 다른 오버레이 요소들도 정리
        const existingOverlays = document.querySelectorAll('.login-transition-overlay:not(.active)');
        existingOverlays.forEach(overlay => {
            if (overlay && overlay.parentNode) {
                overlay.parentNode.removeChild(overlay);
            }
        });
    }
    
    // 로그인 모달 열기
    if (loginBtns) {
        loginBtns.forEach(btn => {
            if (!btn.classList.contains('profile-link')) { // 프로필 링크 제외
                btn.addEventListener('click', function(e) {
                    e.preventDefault();
                    closeAllModals(); // 먼저 모든 모달 닫기
                    modalBackdrop.classList.add('show');
                    loginModal.classList.add('show');
                });
            }
        });
    }
    
    // 회원가입 모달 열기
    if (registerBtns) {
        registerBtns.forEach(btn => {
            btn.addEventListener('click', function(e) {
                e.preventDefault();
                closeAllModals(); // 먼저 모든 모달 닫기
                modalBackdrop.classList.add('show');
                registerModal.classList.add('show');
            });
        });
    }
    
    // 모달 닫기
    if (closeBtns) {
        closeBtns.forEach(btn => {
            btn.addEventListener('click', function() {
                closeAllModals();
            });
        });
    }
    
    // 배경 클릭 시 모달 닫기
    if (modalBackdrop) {
        modalBackdrop.addEventListener('click', function() {
            closeAllModals();
        });
    }
    
    // 회원가입 모달에서 로그인 모달로 전환
    const showLoginBtn = document.querySelector('.btn-show-login');
    if (showLoginBtn) {
        showLoginBtn.addEventListener('click', function(e) {
            e.preventDefault();
            registerModal.classList.remove('show');
            loginModal.classList.add('show');
        });
    }
    
    // 로그인 모달에서 회원가입 모달로 전환
    const showRegisterBtn = document.querySelector('.btn-show-register');
    if (showRegisterBtn) {
        showRegisterBtn.addEventListener('click', function(e) {
            e.preventDefault();
            loginModal.classList.remove('show');
            registerModal.classList.add('show');
        });
    }
    
    // 페이지 로드 시 모든 모달 닫기
    closeAllModals();
    
    // ESC 키 누를 때 모달 닫기
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeAllModals();
        }
    });
    
    // 로그인 폼 제출 시 애니메이션 효과
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', function(e) {
            e.preventDefault();
            closeAllModals();
            
            // 기존 오버레이 제거
            const existingOverlays = document.querySelectorAll('.login-transition-overlay');
            existingOverlays.forEach(overlay => {
                if (overlay && overlay.parentNode) {
                    overlay.parentNode.removeChild(overlay);
                }
            });
            
            // 새 오버레이 생성
            const overlay = document.createElement('div');
            overlay.className = 'login-transition-overlay';
            document.body.appendChild(overlay);
            
            // 환영 메시지 생성
            const welcomeMsg = document.createElement('div');
            welcomeMsg.className = 'welcome-message';
            welcomeMsg.textContent = '환영합니다!';
            document.body.appendChild(welcomeMsg);
            
            setTimeout(() => {
                overlay.classList.add('active');
                
                // 파도 애니메이션이 어느 정도 진행된 후 환영 메시지 표시
                setTimeout(() => {
                    welcomeMsg.classList.add('show');
                    
                    // 환영 메시지가 표시된 후, 일정 시간 뒤에 폼 제출
                    setTimeout(() => {
                        this.submit();
                    }, 1500);
                }, 500);
            }, 10);
        });
    }
    
    // 회원가입 폼 제출 시 애니메이션 효과
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', function(e) {
            e.preventDefault();
            closeAllModals();
            
            // 기존 오버레이 제거
            const existingOverlays = document.querySelectorAll('.login-transition-overlay');
            existingOverlays.forEach(overlay => {
                if (overlay && overlay.parentNode) {
                    overlay.parentNode.removeChild(overlay);
                }
            });
            
            // 새 오버레이 생성
            const overlay = document.createElement('div');
            overlay.className = 'login-transition-overlay';
            document.body.appendChild(overlay);
            
            // 환영 메시지 생성
            const welcomeMsg = document.createElement('div');
            welcomeMsg.className = 'welcome-message';
            welcomeMsg.textContent = 'BIGPOOL에 오신 것을 환영합니다!';
            document.body.appendChild(welcomeMsg);
            
            setTimeout(() => {
                overlay.classList.add('active');
                
                // 파도 애니메이션이 어느 정도 진행된 후 환영 메시지 표시
                setTimeout(() => {
                    welcomeMsg.classList.add('show');
                    
                    // 환영 메시지가 표시된 후, 일정 시간 뒤에 폼 제출
                    setTimeout(() => {
                        this.submit();
                    }, 1500);
                }, 500);
            }, 10);
        });
    }
    
    // 로그아웃 버튼 클릭 이벤트
    const logoutBtn = document.getElementById('logoutBtn');
    const logoutForm = document.getElementById('logoutForm');
    if (logoutBtn && logoutForm) {
        logoutBtn.addEventListener('click', function(e) {
            e.preventDefault();
            closeAllModals();
            
            // 기존 오버레이 제거
            const existingOverlays = document.querySelectorAll('.login-transition-overlay');
            existingOverlays.forEach(overlay => {
                if (overlay && overlay.parentNode) {
                    overlay.parentNode.removeChild(overlay);
                }
            });
            
            // 새 오버레이 생성
            const overlay = document.createElement('div');
            overlay.className = 'login-transition-overlay';
            document.body.appendChild(overlay);
            
            // 로그아웃 메시지 생성
            const logoutMsg = document.createElement('div');
            logoutMsg.className = 'logout-message';
            logoutMsg.textContent = '다음에 또 봐요!';
            document.body.appendChild(logoutMsg);
            
            setTimeout(() => {
                overlay.classList.add('active');
                
                // 파도 애니메이션이 어느 정도 진행된 후 로그아웃 메시지 표시
                setTimeout(() => {
                    logoutMsg.classList.add('show');
                    
                    // 일정 시간 후 로그아웃 폼 제출
                    setTimeout(() => {
                        logoutForm.submit();
                    }, 1500);
                }, 500);
            }, 10);
        });
    }
    
    // 현재 활성 페이지 메뉴 강조
    const currentLocation = window.location.pathname;
    const navLinks = document.querySelectorAll('.nav-link');
    navLinks.forEach(link => {
        const href = link.getAttribute('href').replace('@{', '').replace('}', '');
        if ((href === '/' && currentLocation === '/') || 
            (href !== '/' && currentLocation.includes(href))) {
            link.classList.add('active');
        }
    });
    
    // 이미지 지연 로딩 구현
    if ('IntersectionObserver' in window) {
        const imageObserver = new IntersectionObserver((entries, observer) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const image = entry.target;
                    const src = image.getAttribute('data-src');
                    if (src) {
                        image.setAttribute('src', src);
                        image.removeAttribute('data-src');
                        image.classList.add('loaded');
                    }
                    observer.unobserve(image);
                }
            });
        });
        
        document.querySelectorAll('img[data-src]').forEach(img => {
            imageObserver.observe(img);
        });
    } else {
        // IntersectionObserver가 지원되지 않는 브라우저를 위한 폴백
        document.querySelectorAll('img[data-src]').forEach(img => {
            img.setAttribute('src', img.getAttribute('data-src'));
            img.removeAttribute('data-src');
        });
    }
}); 