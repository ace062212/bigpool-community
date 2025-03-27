// 페이지 로드 완료 시 실행
document.addEventListener('DOMContentLoaded', function() {
    // 부트스트랩 토스트 초기화
    var toastElList = [].slice.call(document.querySelectorAll('.toast'))
    var toastList = toastElList.map(function (toastEl) {
        return new bootstrap.Toast(toastEl)
    });
    
    // 토스트 메시지 표시
    toastList.forEach(toast => toast.show());
    
    // 알림 메시지 자동 닫기
    setTimeout(function() {
        const alerts = document.querySelectorAll('.alert');
        alerts.forEach(function(alert) {
            const closeButton = alert.querySelector('.btn-close');
            if (closeButton) {
                closeButton.click();
            }
        });
    }, 3000);
}); 