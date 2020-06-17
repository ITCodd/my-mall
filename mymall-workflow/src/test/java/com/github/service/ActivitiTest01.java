package com.github.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.github.WorkflowApplication;
import org.activiti.bpmn.BpmnAutoLayout;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.*;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @ClassName: ActivitiTest01
 * @Description: TODO(activiti 工作流程图自动生成)
 * @author liang
 * @date 2016年4月20日 上午8:32:56
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes={WorkflowApplication.class})
public class ActivitiTest01 {
 	@Autowired
	private RepositoryService repositoryService;
	@Autowired
 	private RuntimeService runtimeService;
	@Autowired
	private TaskService taskService;

	@Test
	public  void test01() throws IOException {
		System.out.println(".........start...");
		
		// 1. Build up the model from scratch
		BpmnModel model = new BpmnModel();
		Process process=new Process();
		model.addProcess(process); 
		final String PROCESSID ="process01";
		final String PROCESSNAME ="测试01";
		process.setId(PROCESSID); 
		process.setName(PROCESSNAME);
		
		process.addFlowElement(createStartEvent());  
		process.addFlowElement(createUserTask("task1", "节点01", "candidateGroup1")); 
		process.addFlowElement(createExclusiveGateway("createExclusiveGateway1")); 
		process.addFlowElement(createUserTask("task2", "节点02", "candidateGroup2")); 
		process.addFlowElement(createExclusiveGateway("createExclusiveGateway2")); 
		process.addFlowElement(createUserTask("task3", "节点03", "candidateGroup3")); 
		process.addFlowElement(createExclusiveGateway("createExclusiveGateway3")); 
		process.addFlowElement(createUserTask("task4", "节点04", "candidateGroup4"));
		process.addFlowElement(createEndEvent());  
		
		process.addFlowElement(createSequenceFlow("startEvent", "task1", "", "")); 
		process.addFlowElement(createSequenceFlow("task1", "task2", "", "")); 
		process.addFlowElement(createSequenceFlow("task2", "createExclusiveGateway1", "", ""));
		process.addFlowElement(createSequenceFlow("createExclusiveGateway1", "task1", "不通过", "${pass=='2'}"));
		process.addFlowElement(createSequenceFlow("createExclusiveGateway1", "task3", "通过", "${pass=='1'}")); 
		process.addFlowElement(createSequenceFlow("task3", "createExclusiveGateway2", "", ""));
		process.addFlowElement(createSequenceFlow("createExclusiveGateway2", "task2", "不通过", "${pass=='2'}"));
		process.addFlowElement(createSequenceFlow("createExclusiveGateway2", "task4", "通过", "${pass=='1'}"));
		process.addFlowElement(createSequenceFlow("task4", "createExclusiveGateway3", "", ""));
		process.addFlowElement(createSequenceFlow("createExclusiveGateway3", "task3", "不通过", "${pass=='2'}"));
		process.addFlowElement(createSequenceFlow("createExclusiveGateway3", "endEvent", "通过", "${pass=='1'}"));
		
		// 2. Generate graphical information  
		new BpmnAutoLayout(model).execute();
		
		// 3. Deploy the process to the engine  
		Deployment deployment = repositoryService.createDeployment().addBpmnModel(PROCESSID+".bpmn", model).name(PROCESSID+"_deployment").deploy();
		   
		// 4. Start a process instance  
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESSID);
		
		// 5. Check if task is available  
		List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
		Assert.assertEquals(1, tasks.size()); 		
		
		// 6. Save process diagram to a file    
		InputStream processDiagram = repositoryService.getProcessDiagram(processInstance.getProcessDefinitionId());
		FileUtils.copyInputStreamToFile(processDiagram, new File("/deployments/"+PROCESSID+".png"));
		   
		// 7. Save resulting BPMN xml to a file  
		InputStream processBpmn = repositoryService.getResourceAsStream(deployment.getId(), PROCESSID+".bpmn");
		FileUtils.copyInputStreamToFile(processBpmn,new File("/deployments/"+PROCESSID+".bpmn"));
		
		System.out.println(".........end...");
	}
	

 
	/*任务节点*/
	protected static UserTask createUserTask(String id, String name, String candidateGroup) {
		List<String> candidateGroups=new ArrayList<String>();
		candidateGroups.add(candidateGroup);
		UserTask userTask = new UserTask();
		userTask.setName(name);
		userTask.setId(id);
		userTask.setCandidateGroups(candidateGroups);
		return userTask;
	}
 
	/*连线*/
	protected static SequenceFlow createSequenceFlow(String from, String to, String name, String conditionExpression) {
		SequenceFlow flow = new SequenceFlow();
		flow.setSourceRef(from);
		flow.setTargetRef(to);
		flow.setName(name);
		if(StringUtils.isNotEmpty(conditionExpression)){
			flow.setConditionExpression(conditionExpression);
		}		
		return flow;
	}
	
	/*排他网关*/
	protected static ExclusiveGateway createExclusiveGateway(String id) {
		ExclusiveGateway exclusiveGateway = new ExclusiveGateway();
		exclusiveGateway.setId(id);
		return exclusiveGateway;
	}
 
	/*开始节点*/
	protected static StartEvent createStartEvent() {
		StartEvent startEvent = new StartEvent();
		startEvent.setId("startEvent");
		return startEvent;
	}
 
	/*结束节点*/
	protected static EndEvent createEndEvent() {
		EndEvent endEvent = new EndEvent();
		endEvent.setId("endEvent");
		return endEvent;
	}
	
}