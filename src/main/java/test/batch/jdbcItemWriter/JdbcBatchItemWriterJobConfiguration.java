package test.batch.jdbcItemWriter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import test.batch.JdbcItemReader.Pay;

import javax.sql.DataSource;

@Slf4j
@RequiredArgsConstructor
//@Configuration
public class JdbcBatchItemWriterJobConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    private static final int chunkSize = 10;

    @Bean
    public Job jdbcBatchItemWriterJob() {
        return jobBuilderFactory.get("jdbcBatchItemWriterJob")
                .start(jdbcBatchItemWriterStep())
                .build();
    }

    @Bean
    public Step jdbcBatchItemWriterStep() {
        return stepBuilderFactory.get("jdbcBatchItemWriterStep")
                .<Pay, Pay>chunk(chunkSize)
                .reader(jdbcBatchItemWriterReader())
                .writer(jdbcBatchItemWriter())
                .build();
    }

    @Bean
    public JdbcCursorItemReader<Pay> jdbcBatchItemWriterReader() {
        return new JdbcCursorItemReaderBuilder<Pay>()
                .fetchSize(chunkSize)
                .dataSource(dataSource)
                .rowMapper(new BeanPropertyRowMapper<>(Pay.class))
                .sql("SELECT id, amount, tx_name, tx_data_time FROM pay")
                .name("jdbcBatchItemWriter")
                .build();
    }

    /**
     * reader에서 넘어온 데이터를 하나씩 출력하는 writer
     */
    @Bean //beanMapped()을 사용할 때는 필수
    public JdbcBatchItemWriter<Pay> jdbcBatchItemWriter() {
        //beanMapped 이용할 때 : Pay.class와 같은 타입일 때
        return new JdbcBatchItemWriterBuilder<Pay>()
                .dataSource(dataSource)
                .sql("insert into pay2(amount, tx_name, tx_date_time) values (:amount, :txName, :txDateTime)")
                .beanMapped().build();

        /*
        columnMapped 이용할 때 : Map<String, Object> 타입일 때
        return new JdbcBatchItemWriterBuilder<Map<String, Object>>()
                .columnMapped()
                .dataSource(this.dataSource)
                .sql("insert into pay2(amount, tx_name, tx_data_time) values (:amount, :txName, :txDateTime)")
                .build();
         */
    }

}
