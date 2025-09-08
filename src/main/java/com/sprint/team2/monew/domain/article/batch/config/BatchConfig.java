package com.sprint.team2.monew.domain.article.batch.config;

import com.sprint.team2.monew.domain.article.service.basic.BasicArticleService;
import com.sprint.team2.monew.domain.interest.entity.Interest;
import com.sprint.team2.monew.domain.interest.repository.InterestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Iterator;
import java.util.UUID;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {

    private final BasicArticleService articleService;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job newsCollectJob(Step step) {
        return new JobBuilder("NewsCollectJob", jobRepository)
                .start(step)
                .build();
    }

    @Bean
    public Step newsCollectStep(ItemReader<UUID> interestReader,
                                ItemProcessor<UUID, UUID> interestProcessor,
                                ItemWriter<UUID> interestWriter) {
        return new StepBuilder("NewsCollectStep", jobRepository)
                .<UUID, UUID>chunk(10, transactionManager)
                .reader(interestReader)
                .processor(interestProcessor)
                .writer(interestWriter)
                .build();
    }

    @Bean
    public ItemReader<UUID> interestReader(InterestRepository interestRepository) {
        return new ItemReader<UUID>() {
            private Iterator<UUID> iterator;

            @Override
            public UUID read() {
                if (iterator == null) {
                    iterator = interestRepository.findAll().stream()
                            .map(Interest::getId).iterator();
                }
                return iterator.hasNext() ? iterator.next() : null;
            }
        };
    }

    @Bean
    public ItemProcessor<UUID, UUID> interestProcessor() {
        return item -> item;
    }

    @Bean
    public ItemWriter<UUID> itemWriter() {
        return items -> {
            for (UUID interestId : items) {
                articleService.saveByInterest(interestId);
            }
        };
    }
}