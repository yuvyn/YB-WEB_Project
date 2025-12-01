document.addEventListener("DOMContentLoaded", () => {
    const wheel = document.getElementById("rouletteWheel");
    const btnStart = document.getElementById("btnStart");
    const chanceCountEl = document.getElementById("chanceCount");
    const historyList = document.getElementById("historyList");
    const historyEmptyText = document.getElementById("historyEmptyText");

    const overlay = document.getElementById("resultOverlay");
    const resultTextEl = document.getElementById("resultText");
    const btnResultOk = document.getElementById("btnResultOk");

    // 룰렛 보상 목록 (10칸)
    const rewards = [
        "골드 100",
        "크리스탈 50",
        "강화 재료 상자",
        "아바타 조각",
        "펫 소환권",
        "실마엘 혈석 300",
        "카드 팩(희귀)",
        "칭호 상자",
        "배틀 아이템 상자",
        "꽝 (재도전)"
    ];

    const SEGMENT_COUNT = rewards.length;
    const SEGMENT_ANGLE = 360 / SEGMENT_COUNT;

    let spinning = false;
    let chances = 3; // 남은 기회
    let currentRotation = 0; // 누적 회전각

    chanceCountEl.textContent = chances;

    /* ---------------------------
       1. 라벨 위치 세팅
    --------------------------- */
    const labels = wheel.querySelectorAll(".segment-label");
    labels.forEach((label, idx) => {
        const angleCenter = idx * SEGMENT_ANGLE + SEGMENT_ANGLE / 2;
        // 중앙 기준으로 위쪽 방향으로 배치 (반지름 약 135px)
        const radius = 135;
        label.style.transform =
            `rotate(${angleCenter}deg) translate(-50%, -${radius}px)`;
    });

    /* ---------------------------
       2. 버튼 클릭 → 스핀 시작
    --------------------------- */

    btnStart.addEventListener("click", () => {
        if (spinning) return;

        if (chances <= 0) {
            alert("오늘의 참여 기회를 모두 사용하셨습니다.");
            return;
        }

        spinRoulette();
    });

    function spinRoulette() {
        spinning = true;
        btnStart.disabled = true;
        wheel.classList.add("spinning");

        // 0 ~ 9 랜덤 인덱스
        const index = Math.floor(Math.random() * SEGMENT_COUNT);

        const angleCenter = index * SEGMENT_ANGLE + SEGMENT_ANGLE / 2;
        const spins = 4; // 최소 회전 바퀴 수

        // 현재 회전각을 0~360 범위로 정리
        const normalizedCurrent = ((currentRotation % 360) + 360) % 360;

        // 최종 회전각: pointer(0deg)가 선택 칸 중앙(angleCenter)을 향하도록
        const finalRotation = spins * 360 + (360 - angleCenter) - normalizedCurrent;

        currentRotation += finalRotation;
        wheel.style.transform = `rotate(${currentRotation}deg)`;

        const reward = rewards[index];

        // transition 시간(3.4s)과 약간 여유를 둬서 결과 처리
        setTimeout(() => {
            onSpinEnd(reward);
        }, 3500);
    }

    function onSpinEnd(reward) {
        wheel.classList.remove("spinning");

        // 꽝이 아니면 기회 차감
        if (reward !== "꽝 (재도전)") {
            chances = Math.max(0, chances - 1);
            chanceCountEl.textContent = chances;
        }

        addHistory(reward);
        showResultPopup(reward);

        spinning = false;
        btnStart.disabled = false;
    }

    /* ---------------------------
       3. 참여 내역
    --------------------------- */
    function addHistory(reward) {
        if (historyEmptyText) {
            historyEmptyText.style.display = "none";
        }

        const li = document.createElement("li");

        const leftSpan = document.createElement("span");
        leftSpan.className = "history-item-reward";
        leftSpan.textContent = reward;

        const rightSpan = document.createElement("span");
        rightSpan.className = "history-item-time";

        const now = new Date();
        const timeStr = now.toLocaleTimeString("ko-KR", {
            hour: "2-digit",
            minute: "2-digit",
            second: "2-digit"
        });
        rightSpan.textContent = timeStr;

        li.appendChild(leftSpan);
        li.appendChild(rightSpan);

        // 최근 기록이 위로 오도록
        historyList.prepend(li);
    }

    /* ---------------------------
       4. 팝업
    --------------------------- */
    function showResultPopup(reward) {
        resultTextEl.textContent = reward;
        overlay.classList.remove("hidden");
    }

    btnResultOk.addEventListener("click", () => {
        overlay.classList.add("hidden");
    });

    overlay.addEventListener("click", (e) => {
        if (e.target === overlay) {
            overlay.classList.add("hidden");
        }
    });
});