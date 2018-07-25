/*
 * Copyright (c) 2017. The Ontario Institute for Cancer Research. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.kf.coordinator;

import io.kf.coordinator.task.etl.ETLDockerContainer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;

//@RunWith(SpringRunner.class)
//@SpringBootTest
@Slf4j
public class ETLCoordinatorTaskMainTests {

  @Test
  public void contextLoads() {
    // Passes if application starts
    assert(true);
  }

  @Test
  @SneakyThrows
  public void testRob(){
    val dockerFilePath = "/home/rtisma/Documents/workspace/kf-portal-etl/docker/etl/Dockerfile";
    val studyId = "sdfsfd1";
    val releaseNum = "RL_00000001";
    val etl = new ETLDockerContainer(dockerFilePath,studyId,releaseNum);
    etl.createContainer();
    etl.runETL();
    etl.isComplete();
    log.info("sdf");

  }

}
