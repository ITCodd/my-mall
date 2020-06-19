package com.github.service;

import com.github.WorkflowApplication;
import com.github.model.MyBiz;
import com.github.utils.ActivitiUtils;
import org.activiti.bpmn.BpmnAutoLayout;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * @author: hjp
 * Date: 2020/6/19
 * Description:
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes={WorkflowApplication.class})
public class ActivitiTest02 {
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;

    @Test
    public void t1() throws IOException {
        BpmnModel model = new BpmnModel();
        Process process=new Process();
        model.addProcess(process);
        final String PROCESSID ="process02";
        final String PROCESSNAME ="动态创建流程";
        process.setId(PROCESSID);
        process.setName(PROCESSNAME);

        String uid = UUID.randomUUID().toString().replaceAll(" ","");
        process.addFlowElement(ActivitiUtils.createStartEvent("start-"+uid,"start"));
        process.addFlowElement(ActivitiUtils.createUserTaskAssignee("task-"+uid+"-1","部门领导审批","zhangsan"));
        process.addFlowElement(ActivitiUtils.createSequenceFlow("start-"+uid, "task-"+uid+"-1"));
        process.addFlowElement(ActivitiUtils.createUserTaskAssignee("task-"+uid+"-2","经理审批","lisi"));
        process.addFlowElement(ActivitiUtils.createSequenceFlow("task-"+uid+"-1", "task-"+uid+"-2"));
        process.addFlowElement(ActivitiUtils.createEndEvent("end-"+uid,"end"));
        process.addFlowElement(ActivitiUtils.createSequenceFlow("task-"+uid+"-2", "end-"+uid));

        new BpmnAutoLayout(model).execute();

        Deployment deployment = repositoryService.createDeployment().addBpmnModel(PROCESSID+".bpmn", model).name(PROCESSID+"_deployment").deploy();

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESSID);

        InputStream processDiagram = repositoryService.getProcessDiagram(processInstance.getProcessDefinitionId());
        FileUtils.copyInputStreamToFile(processDiagram, new File("/deployments/"+PROCESSID+".png"));
    }
}
