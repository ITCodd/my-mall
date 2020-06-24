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

/**
 * @author: hjp
 * Date: 2020/6/23
 * Description:
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes={WorkflowApplication.class})
public class ActivitiTest05 {
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private ActivitiService activitiService;

    @Test
    public void t1() throws IOException {
        BpmnModel model = new BpmnModel();
        Process process=new Process();
        model.addProcess(process);
        final String PROCESSID ="process06";
        final String PROCESSNAME ="或签会签监听器-驳回上一节点测试";
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
        process.addFlowElement(ActivitiUtils.createUserTaskAssignee("task3","总经理审批","mayu"));
        process.addFlowElement(ActivitiUtils.createEndEvent("end","end"));
        process.addFlowElement(ActivitiUtils.createSequenceFlow("start", "task1"));
        process.addFlowElement(ActivitiUtils.createSequenceFlow("task1", "task2"));
        process.addFlowElement(ActivitiUtils.createSequenceFlow("task2", "task3"));
        process.addFlowElement(ActivitiUtils.createSequenceFlow("task3", "end"));

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
        List<Task> tasks = taskService.createTaskQuery()
                .processDefinitionKey("process06")
                .list();
        for (Task task : tasks) {
            System.out.println("getId = " + task.getId());
            System.out.println("getProcessInstanceId = " + task.getProcessInstanceId());
            System.out.println("getProcessDefinitionId = " + task.getProcessDefinitionId());
            System.out.println("getName = " + task.getName());
            System.out.println("getTaskDefinitionKey = " + task.getTaskDefinitionKey());
            System.out.println("getAssignee = " + task.getAssignee());
        }
    }

    @Test
    public void t3() throws IOException {
        Task task = taskService.createTaskQuery().processDefinitionKey("process06").taskAssignee("lisi").singleResult();
        if (task != null) {
            taskService.complete(task.getId());//完成任务时，设置流程变量的值
            System.out.println("任务执行完毕");
        }
    }

    @Test
    public void t4() {
        activitiService.backToPreNode("2514");
    }
}
