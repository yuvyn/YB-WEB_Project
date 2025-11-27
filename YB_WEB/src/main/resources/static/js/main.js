document.addEventListener('DOMContentLoaded', function () {
        const slides = document.querySelectorAll('.banner-slide');
        const tabs = document.querySelectorAll('.banner-tabs button');
        const prevBtn = document.querySelector('.banner-nav.prev');
        const nextBtn = document.querySelector('.banner-nav.next');

        let currentIndex = 0;
        let autoPlayTimer = null;
        const autoPlayDelay = 3000; // 3초

        /** 슬라이드 변경 함수 */
        function showSlide(index) {
            if (index < 0) {
                index = slides.length - 1;
            } else if (index >= slides.length) {
                index = 0;
            }

            // 슬라이드 활성화
            slides.forEach(slide => slide.classList.remove('active'));
            slides[index].classList.add('active');

            // 탭 버튼 활성화
            tabs.forEach(tab => tab.classList.remove('active'));
            const activeTab = document.querySelector(`.banner-tabs button[data-slide="${index}"]`);
            if (activeTab) activeTab.classList.add('active');

            currentIndex = index;

            // 자동 재생 다시 시작
            restartAutoPlay();
        }

        /** 자동 재생 시작 */
        function startAutoPlay() {
            autoPlayTimer = setInterval(() => {
                showSlide(currentIndex + 1);
            }, autoPlayDelay);
        }

        /** 자동 재생 초기화 */
        function restartAutoPlay() {
            if (autoPlayTimer) clearInterval(autoPlayTimer);
            startAutoPlay();
        }

        /** 버튼 이벤트 */
        prevBtn.addEventListener('click', () => showSlide(currentIndex - 1));
        nextBtn.addEventListener('click', () => showSlide(currentIndex + 1));

        /** 탭 클릭 이벤트 */
        tabs.forEach(tab => {
            tab.addEventListener('click', function () {
                const target = parseInt(this.dataset.slide, 10);
                showSlide(target);
            });
        });

        /** 초기 슬라이드 & 자동 재생 시작 */
        showSlide(0);
        startAutoPlay();
    });