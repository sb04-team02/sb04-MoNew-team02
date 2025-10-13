package com.sprint.team2.monew.domain.article.saga;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Getter
public class SagaContext {
    private final UUID sagaId;
    private final UUID interestId;
    private final Instant startTime;
    private Instant endTime;
    private SagaStatus status;

    private final Map<String, Object> data = new ConcurrentHashMap<>();
    private final Deque<Runnable> compensations = new ArrayDeque<>();
    private final List<String> executedSteps = new ArrayList<>();

    public SagaContext(UUID sagaId, UUID interestId) {
        this.sagaId = sagaId;
        this.interestId = interestId;
        this.startTime = Instant.now();
        this.status = SagaStatus.RUNNING;
    }

    public <T> void put(String key, T value) {
        data.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) data.get(key);
    }

    public void startStep(String stepName) {
        executedSteps.add(stepName);
        log.info("[Article] Saga Step 시작: {}", stepName);
    }

    public <T> void completeStep(String stepName, T result) {
        put(stepName + "_result", result);
        log.info("[Article] Saga Step 완료: {}", stepName);
    }

    public void registerCompensation(Runnable runnable) {
        compensations.push(runnable); // 나중에 실행될 보상 로직
    }

    public void executeCompensations() {
        this.status = SagaStatus.COMPENSATING;
        log.warn("[Article|Saga] Rollback 시작");
        while (!compensations.isEmpty()) {
            try {
                compensations.pop().run();
            } catch (Exception e) {
                log.error("[Article|Saga] 보상 중 실패: {}", e.getMessage(), e);
                throw e;
            }
        }

        this.status = SagaStatus.FAILED;
        this.endTime = Instant.now();
    }

    public void completeSaga() {
        this.status = SagaStatus.COMPLETED;
        this.endTime = Instant.now();
        log.info("[Article|Saga] 성공, sagaId = {}", sagaId);
    }
}
