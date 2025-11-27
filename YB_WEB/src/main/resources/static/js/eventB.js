document.addEventListener("DOMContentLoaded", () => {
    const boardEl = document.getElementById("cardBoard");
    const turnCountEl = document.getElementById("turnCount");
    const matchCountEl = document.getElementById("matchCount");
    const timeTextEl = document.getElementById("timeText");
    const statusDailyRemainEl = document.getElementById("dailyRemainText");
    const flipRemainEl = document.getElementById("flipRemain");
    const btnPlay = document.getElementById("btnPlay");
    const btnRestart = document.getElementById("btnRestart");
    const heroScrollBtn = document.getElementById("heroScrollBtn");

    const clearOverlay = document.getElementById("clearOverlay");
    const clearTextEl = document.getElementById("clearText");
    const clearTurnEl = document.getElementById("clearTurn");
    const clearTimeEl = document.getElementById("clearTime");
    const btnClearOk = document.getElementById("btnClearOk");

    // ----- 게임 설정값 -----
    const baseDailyRemain = 3;     // 오늘 남은 게임 횟수(표시용)
    const maxFlipsPerGame = 15;   // 카드 뒤집기 가능 횟수 (턴 기준)

    statusDailyRemainEl.textContent = baseDailyRemain;
    flipRemainEl.textContent = maxFlipsPerGame;

    // 카드 타입 (10쌍)
    const baseCards = [
        { id: "gold",     icon: "💰", text: "골드 상자" },
        { id: "crystal",  icon: "💎", text: "크리스탈" },
        { id: "weapon",   icon: "⚔️", text: "무기 강화" },
        { id: "armor",    icon: "🛡️", text: "방어구 강화" },
        { id: "pet",      icon: "🐾", text: "펫 소환" },
        { id: "cardpack", icon: "📦", text: "카드 팩" },
        { id: "title",    icon: "🏅", text: "칭호" },
        { id: "potion",   icon: "🧪", text: "포션" },
        { id: "ticket",   icon: "🎟️", text: "던전 입장" },
        { id: "stone",    icon: "💠", text: "각인 스톤" }
    ];

    let deck = [];
    let firstCard = null;
    let secondCard = null;
    let lockBoard = false;
    let turns = 0;
    let matches = 0;
    let timerId = null;
    let elapsedSeconds = 0;
    let gameStarted = false;
    let gameActive = false;        // "게임 플레이" 눌렀는지 여부
    let flipsRemain = maxFlipsPerGame;

    /* ---------- 초기화 ---------- */

    function initGame() {
        boardEl.innerHTML = "";
        firstCard = null;
        secondCard = null;
        lockBoard = false;
        turns = 0;
        matches = 0;
        elapsedSeconds = 0;
        gameStarted = false;
        gameActive = false;
        flipsRemain = maxFlipsPerGame;

        if (timerId) {
            clearInterval(timerId);
            timerId = null;
        }

        turnCountEl.textContent = "0";
        matchCountEl.textContent = `0 / ${baseCards.length}`;
        timeTextEl.textContent = "00:00";
        flipRemainEl.textContent = flipsRemain;

        btnPlay.disabled = false;

        // 덱 생성 (각 카드 2개)
        deck = shuffleArray([...baseCards, ...baseCards]).map((card, index) => {
            return { ...card, key: `${card.id}-${index}` };
        });

        deck.forEach(card => {
            const cardEl = createCardElement(card);
            boardEl.appendChild(cardEl);
        });
    }

    function createCardElement(card) {
        const cardEl = document.createElement("div");
        cardEl.className = "card";
        cardEl.dataset.id = card.id;
        cardEl.dataset.key = card.key;

        const inner = document.createElement("div");
        inner.className = "card-inner";

        // back
        const back = document.createElement("div");
        back.className = "card-face card-back";
        const backText = document.createElement("div");
        backText.className = "card-back-text";
        backText.textContent = "EVENT";
        back.appendChild(backText);

        // front
        const front = document.createElement("div");
        front.className = "card-face card-front";

        const frontContent = document.createElement("div");
        frontContent.className = "card-front-content";

        const frontIcon = document.createElement("span");
        frontIcon.className = "card-front-icon";
        frontIcon.textContent = card.icon;

        const frontText = document.createElement("span");
        frontText.className = "card-front-text";
        frontText.textContent = card.text;

        frontContent.appendChild(frontIcon);
        frontContent.appendChild(frontText);
        front.appendChild(frontContent);

        inner.appendChild(back);
        inner.appendChild(front);
        cardEl.appendChild(inner);

        cardEl.addEventListener("click", () => onCardClick(cardEl));

        return cardEl;
    }

    /* ---------- 카드 클릭 ---------- */

    function onCardClick(cardEl) {
        if (!gameActive) return;           // 게임 플레이 버튼 누르기 전엔 막기
        if (lockBoard) return;
        if (cardEl.classList.contains("flipped")) return;
        if (cardEl.classList.contains("matched")) return;
        if (flipsRemain <= 0) return;

        // 첫 클릭 시 타이머 시작
        if (!gameStarted) {
            startTimer();
            gameStarted = true;
        }

        cardEl.classList.add("flipped");

        if (!firstCard) {
            firstCard = cardEl;
            return;
        }

        secondCard = cardEl;
        lockBoard = true;

        turns += 1;
        flipUsed();
        updateStats();

        const firstId = firstCard.dataset.id;
        const secondId = secondCard.dataset.id;

        if (firstId === secondId) {
            handleMatch();
        } else {
            setTimeout(unflipSelected, 800);
        }
    }

    function handleMatch() {
        if (!firstCard || !secondCard) return;

        firstCard.classList.add("matched");
        secondCard.classList.add("matched");

        matches += 1;
        updateStats();

        setTimeout(() => {
            if (firstCard) firstCard.style.visibility = "hidden";
            if (secondCard) secondCard.style.visibility = "hidden";
        }, 380);

        resetSelection();

        if (matches === baseCards.length) {
            endGame(true);
        }
    }

    function unflipSelected() {
        if (firstCard) firstCard.classList.remove("flipped");
        if (secondCard) secondCard.classList.remove("flipped");
        resetSelection();
    }

    function resetSelection() {
        [firstCard, secondCard] = [null, null];
        lockBoard = false;
    }

    /* ---------- 통계 / 타이머 ---------- */

    function updateStats() {
        turnCountEl.textContent = String(turns);
        matchCountEl.textContent = `${matches} / ${baseCards.length}`;
    }

    function startTimer() {
        timerId = setInterval(() => {
            elapsedSeconds += 1;
            timeTextEl.textContent = formatTime(elapsedSeconds);
        }, 1000);
    }

    function stopTimer() {
        if (timerId) {
            clearInterval(timerId);
            timerId = null;
        }
    }

    function formatTime(sec) {
        const m = Math.floor(sec / 60);
        const s = sec % 60;
        return `${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
    }

    function flipUsed() {
        flipsRemain = Math.max(0, flipsRemain - 1);
        flipRemainEl.textContent = flipsRemain;

        if (flipsRemain === 0 && matches < baseCards.length) {
            // 잔여 횟수 소진: 실패 처리 (연출만)
            endGame(false);
        }
    }

    /* ---------- 게임 종료 / 팝업 ---------- */

    function endGame(success) {
        stopTimer();
        gameActive = false;

        clearTextEl.textContent = success ? "10쌍 매칭 완료!" : "잔여 횟수를 모두 사용했습니다.";
        clearTurnEl.textContent = turns;
        clearTimeEl.textContent = formatTime(elapsedSeconds);
        clearOverlay.classList.remove("hidden");
        btnPlay.disabled = true;
    }

    /* ---------- 유틸 ---------- */

    function shuffleArray(arr) {
        const a = [...arr];
        for (let i = a.length - 1; i > 0; i--) {
            const j = Math.floor(Math.random() * (i + 1));
            [a[i], a[j]] = [a[j], a[i]];
        }
        return a;
    }

    /* ---------- 이벤트 바인딩 ---------- */

    btnPlay.addEventListener("click", () => {
        if (gameActive) return;
        gameActive = true;
        btnPlay.disabled = true;
    });

    btnRestart.addEventListener("click", () => {
        initGame();
    });

    btnClearOk.addEventListener("click", () => {
        clearOverlay.classList.add("hidden");
        // 여기서 바로 다시 시작하고 싶으면:
        // initGame();
    });

    clearOverlay.addEventListener("click", (e) => {
        if (e.target === clearOverlay) {
            clearOverlay.classList.add("hidden");
        }
    });

    // 히어로 버튼 클릭 시 게임 섹션으로 스크롤
    if (heroScrollBtn) {
        heroScrollBtn.addEventListener("click", () => {
            const gameSection = document.getElementById("gameSection");
            if (gameSection) {
                gameSection.scrollIntoView({ behavior: "smooth" });
            }
        });
    }

    // 페이지 로드 시 초기화
    initGame();
});