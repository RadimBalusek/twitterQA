package com.camunda.training;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.camunda.bpm.engine.task.Task;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;

public class ProcessJUnitTest {

  @Rule
  @ClassRule
  //public ProcessEngineRule rule = new ProcessEngineRule();
  public static ProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().build();

  @Before
  public void setup() {
    init(rule.getProcessEngine());
  }

  @Test
  @Deployment(resources = "diagram_1_ManualTask.bpmn")
  public void testHappyPath() {
    // Create a HashMap to put in variables for the process instance
    Map<String, Object> variables = new HashMap<String, Object>();
   // variables.put("approved", true);
    variables.put("content", "Exercise 10 test - RADIM B");
    // Start process with Java API and variables
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("TwitterQAProcess", variables);
    // Make assertions on the process instance

    List<Job> jobList = jobQuery()
            .processInstanceId(processInstance.getId())
            .list();
    Assertions.assertThat(jobList).hasSize(1);
    Job job = jobList.get(0);
    execute(job);

    assertThat(processInstance).isWaitingAt("ReviewTweetTask");  // sucesss just if process ended

    List<Task> taskList = taskService()
            .createTaskQuery()
            .taskCandidateGroup("management")
            .processInstanceId(processInstance.getId())
            .list();

    Task task = taskList.get(0);

    Map<String, Object> approvedMap = new HashMap<String, Object>();
    approvedMap.put("approved", true);
    taskService().complete(task.getId(), approvedMap);

    Assertions.assertThat(taskList).isNotNull();
    Assertions.assertThat(taskList).hasSize(1);

  }
}
