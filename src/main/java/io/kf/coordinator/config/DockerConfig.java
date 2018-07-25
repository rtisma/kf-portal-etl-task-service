package io.kf.coordinator.config;

import io.kf.coordinator.task.etl.ETLDockerContainerFactory;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class DockerConfig {

  @Value("${docker.filePath")
  private String dockerFilePath;

  @Bean
  public ETLDockerContainerFactory etlDockerContainerFactory(){
    return new ETLDockerContainerFactory(dockerFilePath);
  }

}
