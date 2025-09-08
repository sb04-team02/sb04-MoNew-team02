package com.sprint.team2.monew.domain.user.batch.listener;

import com.sprint.team2.monew.domain.user.exception.UserNotFoundException;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Component
public class UserCleanupStepListener implements StepExecutionListener {

    private final MeterRegistry registry;

    public UserCleanupStepListener(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        if (stepExecution.getExitStatus().equals(ExitStatus.FAILED)) {
            for (Throwable t : stepExecution.getFailureExceptions()) {
                String reason = mapExceptionToReason(t);
                registry.counter("batch.user_cleanup.failure", "reason", reason).increment();
            }
        }
        return stepExecution.getExitStatus();
    }

    private String mapExceptionToReason(Throwable t) {
        if (t instanceof UserNotFoundException) {
            return "USER_NOT_FOUND";
        } else if (t instanceof DataAccessException) {
            return "DB_ERROR";
        } else {
            return "UNKNOWN";
        }
    }
}

