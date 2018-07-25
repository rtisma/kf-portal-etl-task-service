package io.kf.coordinator.task.etl;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Maps.newHashMap;
import static java.lang.String.format;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isRegularFile;
import static java.util.stream.Collectors.toList;

public class ETLDockerContainer {

  private final DockerClient docker;
  private String id;
  private final String dockerFilePath;
  private Path dockerDirectory = Paths.get("./myDockerDir");
  private String dockerImageId;
  private final String studyId;
  private final String releaseNum;

  final private static String[] PORTS = {"9977"};

  private boolean hasStarted = false;
  private boolean hasFinished = false;

  public ETLDockerContainer(@NonNull String dockerFilePath,
      @NonNull String studyId, @NonNull String releaseNum) throws DockerCertificateException {
    docker = DefaultDockerClient.fromEnv().build();
    this.dockerFilePath = dockerFilePath;
    this.releaseNum = releaseNum;
    this.studyId = studyId;
  }


  private static void checkFileExists(String filepath){
    val file = Paths.get(filepath);
    checkArgument(exists(file), "The path '%d' does not exist", file.getFileName());
    checkArgument(isRegularFile(file), "The path '%d' is not a file", file.getFileName());
  }

  @SneakyThrows
  private void buildImage(){
    val imageIdFromMessage = new AtomicReference<String>();
    checkFileExists(dockerFilePath);
    val dockerDir = Paths.get(dockerFilePath).getParent();
    this.dockerImageId = docker.build(
        dockerDir, "test", message -> {
          final String imageId = message.buildImageId();
          if (imageId != null) {
            imageIdFromMessage.set(imageId);
          }
        });
  }

  public void createContainer() throws DockerException, InterruptedException {
    buildImage();
    // Bind container ports to host ports
    final Map<String, List<PortBinding>> portBindings = new HashMap<>();
    for (String port : PORTS) {
      List<PortBinding> hostPorts = new ArrayList<>();
      hostPorts.add(PortBinding.of("0.0.0.0", port));
      portBindings.put(port, hostPorts);
    }

    // Bind container port 443 to an automatically allocated available host port.
    List<PortBinding> randomPort = new ArrayList<>();
    randomPort.add(PortBinding.randomPort("0.0.0.0"));
    portBindings.put("443", randomPort);

    final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

    // Create container with exposed ports
    final ContainerConfig containerConfig = ContainerConfig.builder()
        .hostConfig(hostConfig)
        .image(this.dockerImageId)
        .env(buildEnvMapping(studyId,releaseNum))
        .build();

    final ContainerCreation creation = docker.createContainer(containerConfig);
    this.id = creation.id();
  }
  private static List<String> buildEnvMapping(String studyId, String releaseNum){
    return new DockerEnvBuilder()
        .put("STUDY_ID", studyId)
        .put("RELEASE", releaseNum)
        .build();
  }

  public static class DockerEnvBuilder{
    private Map<String, String> map = newHashMap();
    public DockerEnvBuilder put(@NonNull String env, @NonNull String value){
      checkArgument(!map.containsKey(env),
          "The env variable '%s' with value '%s' already exists", env, value);
      map.put(env, value);
      return this;
    }

    public List<String> build(){
      return copyOf(map.entrySet().stream()
          .map(e -> format("%s=%s", e.getKey(), e.getValue()))
          .collect(toList()));
    }


  }



  public void runETL() throws DockerException, InterruptedException {
    docker.startContainer(id);
    // Start container

    // Exec command inside running container with attached STDOUT and STDERR
//    final String[] command = {"sh", "-c", dockerTriggeredCommand};
//    final ExecCreation execCreation = docker.execCreate(
//      this.id, command, DockerClient.ExecCreateParam.attachStdout(),
//      DockerClient.ExecCreateParam.attachStderr());
//    final LogStream output = docker.execStart(execCreation.id());
//    final String execOutput = output.readFully();

    this.hasStarted = true;
  }

  public boolean isComplete() {

    if(this.hasStarted && !this.hasFinished) {
      this.hasFinished = this.checkFinished();
    }

    return this.hasFinished;
  }

  private boolean checkFinished() {
    // Inspect container
    try {

      final ContainerInfo info = docker.inspectContainer(this.id);
      return !info.state().running();

    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (DockerException e) {
      e.printStackTrace();
    }
    return false;

  }


}
