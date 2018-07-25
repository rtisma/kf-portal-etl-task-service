package io.kf.coordinator.task.etl;

import lombok.NonNull;
import lombok.SneakyThrows;

@lombok.Value
public class ETLDockerContainerFactory {
  @NonNull private final String dockerFilePath;

  @SneakyThrows
  public ETLDockerContainer buildETLDockerContainer(@NonNull String studyId, @NonNull String releaseNum){
    return new ETLDockerContainer(dockerFilePath, studyId, releaseNum);
  }

}
