package com.test.batchsystempractice;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchFileTestConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;

  @Bean
  public Job fileTestJob(
      Step fileTestStep
  ) {
    return new JobBuilder("fileTestJob", jobRepository)
        .start(fileTestStep)
        .build();
  }

  @Bean
  public Step fileTestStep(
      JobRepository jobRepository
  ) {
    return new StepBuilder("fileTestStep", jobRepository)
        .<TestFile, TestOutputFile>chunk(10, transactionManager)
        .reader(fileReader(null))
        .processor(itemProcessor())
        .writer(itemWriter(null))
        .build();
  }

  @StepScope
  @Bean
  public FlatFileItemReader<TestFile> fileReader(
      @Value("#{jobParameters['inputDir']}") String inputDir
  ) {
    return new FlatFileItemReaderBuilder<TestFile>()
        .name("fileReader")
        .resource(new FileSystemResource(inputDir))
        .delimited()
        .delimiter(",")
        .names("id", "bookName", "author", "date")
        .targetType(TestFile.class)
        .customEditors(Map.of(LocalDate.class, dateTimeEditor()))
        .linesToSkip(1)
        .build();
  }

  private PropertyEditor dateTimeEditor() {
    return new PropertyEditorSupport() {
      @Override
      public void setAsText(String text) throws IllegalArgumentException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        setValue(LocalDate.parse(text, formatter));
      }
    };
  }

  @Bean
  @StepScope
  public FlatFileItemWriter<TestOutputFile> itemWriter(
      @Value("#{jobParameters['outputDir']}") String outputDir
  ){
    return new FlatFileItemWriterBuilder<TestOutputFile>()
        .name("itemWriter")
        .resource(new FileSystemResource(outputDir + "/test_output.csv"))
        .delimited()
        .delimiter(",")
        .names("id", "bookName", "author", "date", "recordDate")
        .headerCallback(writer -> writer.write("ID,책이름,저자,집필 날짜,작성일"))
        .build();
  }

  @Bean
  public ItemProcessor<TestFile, TestOutputFile> itemProcessor(){
    return item -> {
      TestOutputFile outputFile = new TestOutputFile();
      // 레코드 생성 날짜를 현재 날짜로 설정
      outputFile.setId(item.getId());
      outputFile.setBookName(item.getBookName());
      outputFile.setAuthor(item.getAuthor());
      outputFile.setDate(item.getDate().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")));
      outputFile.setRecordDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분 ss초")));
      return outputFile;
    };
  }

  @Data
  public static class TestOutputFile {

    private String id;
    private String bookName;
    private String author;
    private String date; // 날짜는 문자열로 처리
    private String recordDate; // 레코드 생성 날짜
  }

  @Data
  public static class TestFile {

    private String id;
    private String bookName;
    private String author;
    private LocalDate date;
  }

}
