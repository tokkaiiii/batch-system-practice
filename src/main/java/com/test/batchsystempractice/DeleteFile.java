package com.test.batchsystempractice;

import lombok.Getter;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@StepScope
@Component
@Getter
public class DeleteFile {

  private final String fileName;
  private final Extension extension;

  public DeleteFile(
      @Value("#{jobParameters['fileName']}") String fileName,
      @Value("#{jobParameters['extension']}") Extension extension) {
    this.fileName = fileName;
    this.extension = extension;
  }
}
