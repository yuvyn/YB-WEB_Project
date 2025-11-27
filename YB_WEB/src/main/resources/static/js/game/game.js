   /************ 기본 데이터 ************/
    const HERO = {
        name: "루나 나이트",
        maxHp: 260,
        atk: 70
    };

    // 간단한 스테이지 정보 (1-1 ~ 1-10)
    const STAGES = Array.from({length: 10}).map((_, i) => {
        const idx = i + 1;
        return {
            id: `1-${idx}`,
            name: `1-${idx} 숲의 요정들`,
            enemyName: idx < 4 ? "버섯 정령" : (idx < 8 ? "고블린 레인저" : "숲의 수호자"),
            enemyEmoji: idx < 4 ? "🍄" : (idx < 8 ? "🧝" : "🐻"),
            enemyHp: 140 + idx * 15,
            enemyAtk: 25 + idx * 4,
            goldRewardMin: 80 + idx * 10,
            goldRewardMax: 130 + idx * 12
        };
    });

    const EQUIP_POOL = [
        "철제 검", "강철 검", "마법 단검", "수정 활", "숲의 방패",
        "가죽 갑옷", "마나 반지", "체력 목걸이"
    ];

    // 게임 상태
    const gameState = {
        gold: 0,
        clearedStages: new Set(),
        equipments: []
    };

    /************ 화면 전환 ************/
    function showScreen(id) {
        document.querySelectorAll(".screen").forEach(s => s.classList.remove("active"));
        document.getElementById(id).classList.add("active");
    }

    document.getElementById("btn-title-start").addEventListener("click", () => {
        showScreen("screen-lobby");
        updateGoldUI();
    });
    document.getElementById("btn-to-title").addEventListener("click", () => {
        showScreen("screen-title");
    });

    /************ 로비 ************/
    const lobbyLog = document.getElementById("lobby-log");
    function lobbyLogAdd(msg) {
        const div = document.createElement("div");
        div.className = "news-item";
        div.textContent = `[${new Date().toLocaleTimeString()}] ${msg}`;
        lobbyLog.prepend(div);
    }

    document.querySelectorAll(".menu-btn").forEach(btn => {
        btn.addEventListener("click", () => {
            const menu = btn.dataset.menu;
            if (menu === "dungeon") {
                showScreen("screen-dungeon");
                renderStageList();
                updateGoldUI();
            } else {
                alert("데모에서는 이 메뉴는 준비중입니다. (UI 구조만 예시)");
            }
        });
    });

    /************ 던전 스테이지 리스트 ************/
    const stageListEl = document.getElementById("stage-list");
    const goldAmountSpan = document.getElementById("gold-amount");
    const goldAmountSpan2 = document.getElementById("gold-amount2");

    function updateGoldUI() {
        goldAmountSpan.textContent = gameState.gold;
        goldAmountSpan2.textContent = gameState.gold;
    }

    function renderStageList() {
        stageListEl.innerHTML = "";
        STAGES.forEach(stage => {
            const card = document.createElement("div");
            card.className = "stage-card";
            const cleared = gameState.clearedStages.has(stage.id);
            card.innerHTML = `
                <div class="stage-header">
                    <div><b>${stage.id}</b> ${stage.name}</div>
                    <span class="badge">${cleared ? "CLEARED" : "NEW"}</span>
                </div>
                <div>적: ${stage.enemyName}</div>
                <div>HP: ${stage.enemyHp} / ATK: ${stage.enemyAtk}</div>
                <div style="font-size: 11px; opacity:.8;">예상 보상: Gold ${stage.goldRewardMin} ~ ${stage.goldRewardMax}</div>
                <div style="margin-top: 6px;">
                    <button data-stage="${stage.id}">전투 시작</button>
                </div>
            `;
            const btn = card.querySelector("button");
            btn.addEventListener("click", () => startBattle(stage.id));
            stageListEl.appendChild(card);
        });
    }

    document.getElementById("btn-back-lobby").addEventListener("click", () => {
        showScreen("screen-lobby");
    });

    /************ 전투 로직 ************/
    const heroHpBar = document.getElementById("hero-hp-bar");
    const enemyHpBar = document.getElementById("enemy-hp-bar");
    const heroHpText = document.getElementById("hero-hp-text");
    const enemyHpText = document.getElementById("enemy-hp-text");
    const heroEntity = document.getElementById("hero-entity");
    const enemyEntity = document.getElementById("enemy-entity");
    const enemyAvatar = document.getElementById("enemy-avatar");
    const battlefield = document.getElementById("battlefield");
    const battleLog = document.getElementById("battle-log");

    const btnAttack = document.getElementById("btn-attack");
    const btnSkill  = document.getElementById("btn-skill");
    const btnGuard  = document.getElementById("btn-guard");

    const modalClear = document.getElementById("modal-clear");
    const modalPause = document.getElementById("modal-pause");

    const battleStageNameEl = document.getElementById("battle-stage-name");
    const battleStageLabelEl = document.getElementById("battle-stage-label");
    const clearStageLabel = document.getElementById("clear-stage-label");
    const rewardListEl = document.getElementById("reward-list");

    let currentStage = null;
    let battleState = {
        heroHp: HERO.maxHp,
        enemyHp: 0,
        guarding: false,
        inAction: false,
        battleEnd: false
    };

    function clamp(v,min,max){ return v<min?min:(v>max?max:v); }

    function updateBattleHP() {
        const heroPct = (battleState.heroHp / HERO.maxHp) * 100;
        const enemyPct = (battleState.enemyHp / currentStage.enemyHp) * 100;
        heroHpBar.style.width = clamp(heroPct,0,100) + "%";
        enemyHpBar.style.width = clamp(enemyPct,0,100) + "%";
        heroHpText.textContent = `HP ${battleState.heroHp} / ${HERO.maxHp}`;
        enemyHpText.textContent = `HP ${battleState.enemyHp} / ${currentStage.enemyHp}`;
    }

    function battleLogAdd(text, type="system") {
        const div = document.createElement("div");
        div.classList.add("log-line");
        if(type==="system") div.classList.add("log-system");
        if(type==="hero") div.classList.add("log-hero");
        if(type==="enemy") div.classList.add("log-enemy");
        div.innerHTML = text;
        battleLog.prepend(div);
    }

    function spawnDamageText(dmg, target, isHeroTarget) {
        const span = document.createElement("div");
        span.className = "damage-text " + (isHeroTarget ? "damage-hero" : "damage-enemy");
        span.textContent = dmg;

        const rect = target.getBoundingClientRect();
        const parentRect = battlefield.getBoundingClientRect();
        const x = rect.left - parentRect.left + rect.width/2;
        const y = rect.top - parentRect.top + 16;

        span.style.left = (x-10) + "px";
        span.style.top = y + "px";

        battlefield.appendChild(span);
        setTimeout(()=>span.remove(),900);
    }

    function setBattleButtons(enable) {
        btnAttack.disabled = !enable;
        btnSkill.disabled = !enable;
        btnGuard.disabled = !enable;
    }

    function checkBattleEnd() {
        if (battleState.enemyHp <= 0) {
            battleState.enemyHp = 0;
            updateBattleHP();
            battleLogAdd(`${currentStage.enemyName} 을(를) 처치했습니다!`, "system");
            battleState.battleEnd = true;
            setBattleButtons(false);
            handleStageClear();
            return true;
        }
        if (battleState.heroHp <= 0) {
            battleState.heroHp = 0;
            updateBattleHP();
            battleLogAdd(`루나 나이트가 쓰러졌습니다...`, "system");
            battleState.battleEnd = true;
            setBattleButtons(false);
            return true;
        }
        return false;
    }

    function heroAttack(isSkill=false) {
        if (battleState.inAction || battleState.battleEnd) return;
        battleState.inAction = true;
        setBattleButtons(false);
        battleState.guarding = false;

        heroEntity.classList.add("anim-hero-attack");

        const ratioMin = isSkill ? 1.3 : 0.8;
        const ratioMax = isSkill ? 1.7 : 1.2;
        const ratio = ratioMin + Math.random()*(ratioMax-ratioMin);
        const dmg = Math.floor(HERO.atk * ratio);

        setTimeout(()=>{
            battleState.enemyHp = Math.max(0, battleState.enemyHp - dmg);
            updateBattleHP();
            enemyEntity.classList.add("anim-enemy-hit");
            spawnDamageText(dmg, enemyEntity, false);
            battleLogAdd(
                isSkill
                    ? `루나 나이트의 스킬! ${currentStage.enemyName}에게 <b>${dmg}</b> 피해! 💥`
                    : `루나 나이트의 공격! ${currentStage.enemyName}에게 <b>${dmg}</b> 피해!`,
                "hero"
            );
        },150);

        setTimeout(()=>{
            heroEntity.classList.remove("anim-hero-attack");
            enemyEntity.classList.remove("anim-enemy-hit");

            if (checkBattleEnd()) {
                battleState.inAction = false;
                return;
            }
            enemyTurn();
        },420);
    }

    function enemyTurn() {
        if (battleState.battleEnd) {
            battleState.inAction = false;
            return;
        }
        enemyEntity.classList.add("anim-enemy-attack");

        const ratio = 0.8 + Math.random()*0.4;
        let dmg = Math.floor(currentStage.enemyAtk * ratio);
        if (battleState.guarding) dmg = Math.floor(dmg*0.5);

        setTimeout(()=>{
            battleState.heroHp = Math.max(0, battleState.heroHp - dmg);
            updateBattleHP();
            heroEntity.classList.add("anim-hero-hit");
            spawnDamageText(dmg, heroEntity, true);
            battleLogAdd(
                battleState.guarding
                    ? `${currentStage.enemyName}의 공격! 가드로 피해가 감소되어 <b>${dmg}</b> 피해만 입었습니다.`
                    : `${currentStage.enemyName}의 공격! 루나 나이트가 <b>${dmg}</b> 피해를 입었습니다.`,
                "enemy"
            );
        },150);

        setTimeout(()=>{
            enemyEntity.classList.remove("anim-enemy-attack");
            heroEntity.classList.remove("anim-hero-hit");
            battleState.inAction = false;
            battleState.guarding = false;
            if (!checkBattleEnd()) setBattleButtons(true);
        },420);
    }

    function guard() {
        if (battleState.inAction || battleState.battleEnd) return;
        battleState.inAction = true;
        setBattleButtons(false);
        battleState.guarding = true;
        battleLogAdd("루나 나이트가 방어 태세! 다음 공격 피해 50% 감소.", "hero");
        setTimeout(()=>enemyTurn(),250);
    }

    function resetBattle() {
        battleState.heroHp = HERO.maxHp;
        battleState.enemyHp = currentStage.enemyHp;
        battleState.guarding = false;
        battleState.inAction = false;
        battleState.battleEnd = false;
        battleLog.innerHTML = "";
        battleLogAdd(`전투 시작! 루나 나이트 vs ${currentStage.enemyName}`, "system");
        updateBattleHP();
        setBattleButtons(true);
    }

    function startBattle(stageId) {
        currentStage = STAGES.find(s=>s.id===stageId);
        if (!currentStage) return;
        showScreen("screen-battle");
        document.getElementById("hero-atk").textContent = HERO.atk;
        document.getElementById("enemy-name").textContent = currentStage.enemyName;
        enemyAvatar.textContent = currentStage.enemyEmoji;
        document.getElementById("battle-stage-name").textContent = currentStage.id;
        document.getElementById("battle-stage-label").textContent = currentStage.name;
        resetBattle();
    }

    /************ STAGE CLEAR & 보상 ************/
    function randomInt(min,max){
        return Math.floor(Math.random()*(max-min+1))+min;
    }

    function handleStageClear() {
        gameState.clearedStages.add(currentStage.id);

        const goldGain = randomInt(currentStage.goldRewardMin, currentStage.goldRewardMax);
        gameState.gold += goldGain;

        // 장비/아이템 랜덤
        const rewardEquip = Math.random() < 0.6; // 60% 확률로 장비 하나
        rewardListEl.innerHTML = "";
        const goldLine = document.createElement("div");
        goldLine.textContent = `💰 Gold +${goldGain}`;
        rewardListEl.appendChild(goldLine);

        if (rewardEquip) {
            const eq = EQUIP_POOL[randomInt(0, EQUIP_POOL.length-1)];
            gameState.equipments.push(eq);
            const eqLine = document.createElement("div");
            eqLine.textContent = `🛡 장비 획득: ${eq}`;
            rewardListEl.appendChild(eqLine);
        } else {
            const itemLine = document.createElement("div");
            itemLine.textContent = `🧪 아이템 획득: 회복 포션 x1`;
            rewardListEl.appendChild(itemLine);
        }

        clearStageLabel.textContent = `${currentStage.id} 클리어!`;
        updateGoldUI();
        lobbyLogAdd(`${currentStage.id} 스테이지를 클리어하고 보상을 획득했습니다.`);
        modalClear.classList.add("active");
    }

    // 다음 스테이지 찾기
    function getNextStageId() {
        if (!currentStage) return null;
        const idx = STAGES.findIndex(s=>s.id===currentStage.id);
        if (idx === -1 || idx === STAGES.length-1) return null;
        return STAGES[idx+1].id;
    }

    /************ 버튼 이벤트: 전투 ************/
    btnAttack.addEventListener("click", ()=>heroAttack(false));
    btnSkill.addEventListener("click",  ()=>heroAttack(true));
    btnGuard.addEventListener("click",  guard);

    document.getElementById("btn-battle-menu").addEventListener("click", ()=>{
        modalPause.classList.add("active");
    });
    document.getElementById("btn-continue").addEventListener("click", ()=>{
        modalPause.classList.remove("active");
    });
    document.getElementById("btn-restart2").addEventListener("click", ()=>{
        modalPause.classList.remove("active");
        resetBattle();
    });
    document.getElementById("btn-exit2").addEventListener("click", ()=>{
        modalPause.classList.remove("active");
        showScreen("screen-dungeon");
        renderStageList();
    });

    document.getElementById("btn-battle-exit-small").addEventListener("click", ()=>{
        showScreen("screen-dungeon");
        renderStageList();
    });

    // CLEAR 모달 버튼들
    document.getElementById("btn-exit-dungeon").addEventListener("click", ()=>{
        modalClear.classList.remove("active");
        showScreen("screen-dungeon");
        renderStageList();
    });
    document.getElementById("btn-restart-stage").addEventListener("click", ()=>{
        modalClear.classList.remove("active");
        resetBattle();
    });
    document.getElementById("btn-next-stage").addEventListener("click", ()=>{
        const nextId = getNextStageId();
        modalClear.classList.remove("active");
        if (!nextId) {
            alert("다음 스테이지가 없습니다. 던전으로 돌아갑니다.");
            showScreen("screen-dungeon");
            renderStageList();
        } else {
            startBattle(nextId);
        }
    });

    // 던전으로 나가기(상단 버튼)
    document.getElementById("btn-exit-dungeon").addEventListener("click", ()=>{
        modalClear.classList.remove("active");
        showScreen("screen-dungeon");
        renderStageList();
    });