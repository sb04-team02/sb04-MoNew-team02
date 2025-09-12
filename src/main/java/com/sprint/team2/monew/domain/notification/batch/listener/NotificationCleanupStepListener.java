package com.sprint.team2.monew.domain.notification.batch.listener;

import com.sprint.team2.monew.domain.notification.exception.NotificationNotFoundException;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class NotificationCleanupStepListener implements StepExecutionListener {
    private final MeterRegistry registry;
    private final Map<String, Long> failureCountMap = new HashMap<>();

    public NotificationCleanupStepListener(MeterRegistry registry) {
        this.registry = registry;

        // 기본 실패 사유 등록
        for (String reason : new String[] {"NOTIFICATION_NOT_FOUND", "DB_ERROR", "UNKNOWN"}) {
            failureCountMap.put(reason, 0L);

            Gauge.builder("batch.notification_cleanup.failure", failureCountMap, map ->map.get(reason))
                    .description("알림 삭제 배치 실패 사유별 카운트")
                    .tag("reason", reason)
                    .register(registry);
        }
    }
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        //실패 카운트 초기화
        failureCountMap.replaceAll((k, v) -> 0L);

        if (stepExecution.getExitStatus().equals(ExitStatus.FAILED)) {
            stepExecution.getFailureExceptions().forEach(e-> {
                String reason = mapExecutionToReason(e);
                failureCountMap.merge(reason,1L, Long::sum);
            });
        }
        return stepExecution.getExitStatus();
    }
    private String mapExecutionToReason(Throwable t) {
        if (t instanceof NotificationNotFoundException) return "NOTIFICATION_NOT_FOUND";
        if (t instanceof DataAccessException) return "DB_ERROR";
        return "UNKNOWN";
    }
}
