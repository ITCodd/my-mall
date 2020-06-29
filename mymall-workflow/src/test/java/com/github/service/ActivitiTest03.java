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
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: hjp
 * Date: 2020/6/20
 * Description:
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes={WorkflowApplication.class})
public class ActivitiTest03 {

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
        final String PROCESSID ="process04";
        final String PROCESSNAME ="或签会签测试";
        process.setId(PROCESSID);
        process.setName(PROCESSNAME);

        Map<String, Object> variables = new HashMap<>();

        process.addFlowElement(ActivitiUtils.createStartEvent("start","start"));
        process.addFlowElement(ActivitiUtils.createUserTaskSign("task1","部门领导审批","orsign"));
        List<String> orsignAssignees=new ArrayList<>();
        orsignAssignees.add("zsan");
        orsignAssignees.add("lisi");
        orsignAssignees.add("wangwu");
        variables.put(ActivitiUtils.getVariablesKey("orsign","task1"),orsignAssignees);
        process.addFlowElement(ActivitiUtils.createUserTaskSign("task2","经理审批","countersign"));
        List<String> counterSignAssignees=new ArrayList<>();
        counterSignAssignees.add("fugui");
        counterSignAssignees.add("liubei");
        counterSignAssignees.add("zouyu");
        variables.put(ActivitiUtils.getVariablesKey("countersign","task2"),counterSignAssignees);
        process.addFlowElement(ActivitiUtils.createEndEvent("end","end"));
        process.addFlowElement(ActivitiUtils.createSequenceFlow("start", "task1"));
        process.addFlowElement(ActivitiUtils.createSequenceFlow("task1", "task2"));
        process.addFlowElement(ActivitiUtils.createSequenceFlow("task2", "end"));

        new BpmnAutoLayout(model).execute();

        Deployment deployment = repositoryService.createDeployment().addBpmnModel(PROCESSID + ".bpmn", model).name(PROCESSID + "_deployment").deploy();

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESSID,variables);
        System.out.println("processInstance = " + processInstance.getId());
        InputStream processDiagram = repositoryService.getProcessDiagram(processInstance.getProcessDefinitionId());
        FileUtils.copyInputStreamToFile(processDiagram, new File("/deployments/"+PROCESSID+".png"));

        InputStream processBpmn = repositoryService.getResourceAsStream(deployment.getId(), PROCESSID+".bpmn");
        FileUtils.copyInputStreamToFile(processBpmn,new File("/deployments/"+PROCESSID+".bpmn"));
    }

    @Test
    public void t2() throws IOException {
        Task task = taskService.createTaskQuery().processDefinitionKey("process04").taskAssignee("lisi").singleResult();
        if (task != null) {
            taskService.complete(task.getId());//完成任务时，设置流程变量的值
            System.out.println("任务执行完毕");
        }
    }

    @Test
    public void t5() {
        activitiService.addSign("2514","countersigntask2","zhaoliu");
    }

}
