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
    let currentRotation = 0;

    // 남은 기회 UI 초기화
    chanceCountEl.textContent = chances;

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

        // 0~9 랜덤 인덱스
        const index = Math.floor(Math.random() * SEGMENT_COUNT);

        // 화살표가 항상 선택된 칸의 중앙을 가리키도록 각도 계산
        const extraRotations = 4; // 최소 4바퀴
        const stopAngle =
            360 - (index * SEGMENT_ANGLE + SEGMENT_ANGLE / 2); // 선택칸 중앙이 위로 오도록

        const targetRotation = extraRotations * 360 + stopAngle;

        currentRotation += targetRotation;
        wheel.style.transform = `rotate(${currentRotation}deg)`;

        const reward = rewards[index];

        // 애니메이션이 끝나면 결과 처리 (CSS transition 시간과 맞춤: 3.2s)
        setTimeout(() => {
            onSpinEnd(reward);
        }, 3300);
    }

    function onSpinEnd(reward) {
        wheel.classList.remove("spinning");

        // 꽝이 아니면 기회 1 감소
        if (reward !== "꽝 (재도전)") {
            chances = Math.max(0, chances - 1);
            chanceCountEl.textContent = chances;
        }

        // 참여 내역 추가
        addHistory(reward);

        // 결과 팝업 표시
        showResultPopup(reward);

        spinning = false;
        btnStart.disabled = false;
    }

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
        historyList.prepend(li);
    }

    function showResultPopup(reward) {
        resultTextEl.textContent = reward;
        overlay.classList.remove("hidden");
    }

    btnResultOk.addEventListener("click", () => {
        overlay.classList.add("hidden");
    });

    // 바깥(어두운 영역) 클릭시 닫기
    overlay.addEventListener("click", (e) => {
        if (e.target === overlay) {
            overlay.classList.add("hidden");
        }
    });
});