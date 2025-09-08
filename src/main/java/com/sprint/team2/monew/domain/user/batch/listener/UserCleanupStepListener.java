package com.sprint.team2.monew.domain.user.batch.listener;

import com.sprint.team2.monew.domain.user.exception.UserNotFoundException;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class UserCleanupStepListener implements StepExecutionListener {

    private final MeterRegistry registry;
    private final Map<String, Double> failureCountMap = new HashMap<>();

    public UserCleanupStepListener(MeterRegistry registry) {
        this.registry = registry;

        // 초기 상태 0으로 설정하고 Gauge 등록
        for (String reason : new String[]{"USER_NOT_FOUND", "DB_ERROR", "UNKNOWN"}) {
            failureCountMap.put(reason, 0.0);

            Gauge.builder("batch.user_cleanup.failure", failureCountMap, map -> map.get(reason))
                    .description("User cleanup step failure count")
                    .tag("reason", reason)
                    .register(registry);
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        // 모든 reason 초기화
        failureCountMap.replaceAll((k, v) -> 0.0);

        if (stepExecution.getExitStatus().equals(ExitStatus.FAILED)) {
            // 이번 Step 실패 횟수 계산
            Map<String, Long> reasonCount = stepExecution.getFailureExceptions().stream()
                    .map(this::mapExceptionToReason)
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            // Map에 반영
            reasonCount.forEach((reason, count) -> failureCountMap.put(reason, count.doubleValue()));
        }

        return stepExecution.getExitStatus();
    }

    private String mapExceptionToReason(Throwable t) {
        if (t instanceof UserNotFoundException) return "USER_NOT_FOUND";
        if (t instanceof DataAccessException) return "DB_ERROR";
        return "UNKNOWN";
    }
}

