package com.test.batchsystempractice;

import com.test.batchsystempractice.BatchFileTestConfig.TestFile;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.MultiResourceItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.builder.MultiResourceItemWriterBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchMultiResourceConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;

  @Bean
  public Job multiResourceJob(
      Step multiResourceStep
      ) {
    return new JobBuilder("multiResourceJob", jobRepository)
        .start(multiResourceStep)
        .build();
  }

  @Bean
  public Step multiResourceStep(
  ) {
    return new StepBuilder("multiResourceStep", jobRepository)
        .<TestFile, TestFile>chunk(10, transactionManager)
        .reader(testFileListReader())
        .writer(multiResourceItemWriter(null))
        .build();
  }

  @Bean
  public ListItemReader<TestFile> testFileListReader() {
    List<TestFile> testFiles = new ArrayList<>();

    for (int i = 1; i <= 15; i++) {
      testFiles.add(
          new TestFile(i + "", "book title " + i, "book author " + i, LocalDate.of(2023, 10, i)));
    }
    return new ListItemReader<>(testFiles);
  }

  @Bean
  public FlatFileItemWriter<TestFile> delegateFileWriter() {
    return new FlatFileItemWriterBuilder<TestFile>()
        .name("delegateFileWriter")
        .formatted()
        .format("ID: %s | 제목: %s | 작성자: %s | 집필일: %s")
        .names("id", "bookName", "author", "date")
        .headerCallback(writer -> writer.write("===================== 책 정보 ======================"))
        .footerCallback(
            writer -> writer.write("===================================================="))
        .build();
  }

  @Bean
  @StepScope
  public MultiResourceItemWriter<TestFile> multiResourceItemWriter(
      @Value("#{jobParameters['outputDir']}") String outputDir
  ) {
    return new MultiResourceItemWriterBuilder<TestFile>()
        .name("multiResourceItemWriter")
        .resource(new FileSystemResource(outputDir + "/file-inform"))
        .itemCountLimitPerResource(10)
        .delegate(delegateFileWriter())
        .resourceSuffixCreator(index -> String.format("-%03d", index))
        .build();
  }

}
