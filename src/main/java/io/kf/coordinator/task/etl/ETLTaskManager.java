package io.kf.coordinator.task.etl;

import io.kf.coordinator.task.Task;
import io.kf.coordinator.task.TaskManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ETLTaskManager extends TaskManager {

  @Autowired
  private ETLDockerContainerFactory etlDockerContainerFactory;

  @Override
  protected Task createTask(String taskId, String releaseId) {

    try {
      return new ETLTask(taskId, releaseId, etlDockerContainerFactory.buildETLDockerContainer(taskId, releaseId));

    } catch (Exception e) {
      log.error(e.getMessage());
      return null;
    }
  }

}

