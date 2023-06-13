package test.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
//@Configuration
@RequiredArgsConstructor
public class ScopeConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job scopeJob() {
        return jobBuilderFactory.get("scopeJob")
                .start(scopeStep1(null))
                .next(scopeStep2())
                .build();
    }

    @Bean
    @JobScope //Step 선언문에서 사용 가능
    public Step scopeStep1(@Value("#{jobParameters[requestDate]}") String requestDate){
        return stepBuilderFactory.get("scopeStep1")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>>>>>> This is scopeStep1");
                    log.info(">>>>>>>>> requestDate = {}", requestDate);
                    return RepeatStatus.FINISHED; // Step, Tasklet : RepeatStatus.FINISHED 처리 성공 여부 관계없이 태스크릿을 완료하고 다음 처리를 이어서 하겠다는 것
                }).build();
    }

    @Bean
    public Step scopeStep2() {
        return stepBuilderFactory.get("scopeStep2")
                .tasklet(scopeStep2Tasklet(null))
                .build();
    }

    @Bean
    @StepScope // Tasklet이나 ItemReader, ItemWriter, ItemProcessor 에서 사용가능
    public Tasklet scopeStep2Tasklet(@Value("#{jobParameters[requestDate]}") String requestDate) {
        return (contribution, chunkContext) -> {
            log.info(">>>>>>>>> This is scopeStep2");
            log.info(">>>>>>>>> requestDate = {}", requestDate);
            return RepeatStatus.FINISHED;
        };
    }
}
