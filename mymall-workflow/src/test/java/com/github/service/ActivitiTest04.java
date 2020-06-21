package com.github.service;

import com.github.WorkflowApplication;
import com.github.utils.ActivitiUtils;
import org.activiti.bpmn.BpmnAutoLayout;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={WorkflowApplication.class})
public class ActivitiTest04 {
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;

    @Test
    public void t1() throws IOException {
        BpmnModel model = new BpmnModel();
        Process process=new Process();
        model.addProcess(process);
        final String PROCESSID ="process05";
        final String PROCESSNAME ="或签会签监听器测试";
        process.setId(PROCESSID);
        process.setName(PROCESSNAME);

        process.addFlowElement(ActivitiUtils.createStartEvent("start","start"));
        List<String> orsignAssignees=new ArrayList<>();
        orsignAssignees.add("zsan");
        orsignAssignees.add("lisi");
        orsignAssignees.add("wangwu");
        process.addFlowElement(ActivitiUtils.createUserTaskSignAssignees("task1","部门领导审批","orsign",orsignAssignees));
        List<String> counterSignAssignees=new ArrayList<>();
        counterSignAssignees.add("fugui");
        counterSignAssignees.add("liubei");
        counterSignAssignees.add("zouyu");
        process.addFlowElement(ActivitiUtils.createUserTaskSignAssignees("task2","经理审批","countersign",counterSignAssignees));
        process.addFlowElement(ActivitiUtils.createEndEvent("end","end"));
        process.addFlowElement(ActivitiUtils.createSequenceFlow("start", "task1"));
        process.addFlowElement(ActivitiUtils.createSequenceFlow("task1", "task2"));
        process.addFlowElement(ActivitiUtils.createSequenceFlow("task2", "end"));

        new BpmnAutoLayout(model).execute();

        Deployment deployment = repositoryService.createDeployment().addBpmnModel(PROCESSID + ".bpmn", model).name(PROCESSID + "_deployment").deploy();

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESSID);
        System.out.println("processInstance = " + processInstance.getId());
        InputStream processDiagram = repositoryService.getProcessDiagram(processInstance.getProcessDefinitionId());
        FileUtils.copyInputStreamToFile(processDiagram, new File("/deployments/"+PROCESSID+".png"));

        InputStream processBpmn = repositoryService.getResourceAsStream(deployment.getId(), PROCESSID+".bpmn");
        FileUtils.copyInputStreamToFile(processBpmn,new File("/deployments/"+PROCESSID+".bpmn"));
    }

    @Test
    public void t2() throws IOException {
        Task task = taskService.createTaskQuery().processDefinitionKey("process05").taskAssignee("zouyu").singleResult();
        if (task != null) {
            taskService.complete(task.getId());//完成任务时，设置流程变量的值
            System.out.println("任务执行完毕");
        }
    }
}
