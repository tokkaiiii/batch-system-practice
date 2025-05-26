package com.test.batchsystempractice;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchTestConfig {


  @Bean
  public Job job1(
      JobRepository jobRepository,
      Step collectFilesToDeleteStep
  ) {
    return new JobBuilder("job1", jobRepository)
        .listener(new FileSystemListener())
        .start(collectFilesToDeleteStep)
        .build();
  }

  // 1. 삭제할 파일 수집
  @Bean
  public Step collectFilesToDeleteStep(
      JobRepository jobRepository,
      PlatformTransactionManager transactionManager
  ) {
    return new StepBuilder("collectFilesToDeleteStep", jobRepository)
        .tasklet(deleteFileTasklet(null), transactionManager)
        .build();
  }

  @Bean
  @StepScope
  public Tasklet collectFilesToDeleteTasklet(
      @Value("#{jobParameters['deleteFileBeforeDate']}") LocalDate deleteFileBeforeDate
  ) {
    return (contribution, chunkContext) -> {
      log.info("{} 일 이전 파일을 삭제합니다.",
          deleteFileBeforeDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")));
      for (int i = 0; i <= 5; i++) {
        log.info("파일 삭제 중... 삭제 대상: {} 에 생성한 파일",
            deleteFileBeforeDate.minusDays(i).format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")));
      }
      return null;
    };
  }

  @Bean
  public Tasklet deleteFileTasklet(DeleteFile deleteFile) {
    return (contribution, chunkContext) -> {
//      log.info("파일 삭제를 시작합니다.");
      log.info("삭제할 파일: {}.{}", deleteFile.getFileName(), deleteFile.getExtension().getExtension());
      // 실제 파일 삭제 로직을 여기에 추가
//      log.info("파일 삭제가 완료되었습니다.");
      return null;
    };
  }
}


