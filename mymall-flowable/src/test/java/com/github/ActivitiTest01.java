package com.github;


import com.github.utils.ActivitiUtils;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.Process;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

/**
 * @author: hjp
 * Date: 2020/6/19
 * Description:
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes={FlowableApplication.class})
public class ActivitiTest01 {
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
        BpmnModel bpmnModel=new BpmnModel();
        Process process = new Process();
        bpmnModel.addProcess(process);
        final String PROCESSID ="process01";
        final String PROCESSNAME ="动态创建流程1";
        process.setId(PROCESSID);
        process.setName(PROCESSNAME);

        process.addFlowElement(ActivitiUtils.createStartEvent("start","start"));
        process.addFlowElement(ActivitiUtils.createUserTaskAssignee("task-1","部门领导审批","zhangsan"));
        process.addFlowElement(ActivitiUtils.createUserTaskAssignee("task-2","经理审批","lisi"));
        process.addFlowElement(ActivitiUtils.createEndEvent("end","end"));
        process.addFlowElement(ActivitiUtils.createSequenceFlow("start", "task-1"));
        process.addFlowElement(ActivitiUtils.createSequenceFlow("task-1", "task-2"));
        process.addFlowElement(ActivitiUtils.createSequenceFlow("task-2", "end"));

        byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(bpmnModel);
        String processName = PROCESSNAME+".bpmn20.xml";
        repositoryService.createDeployment()
                .name(PROCESSNAME)
                .addBytes(processName,bpmnBytes)
                .deploy();
    }



    @Test
    public void t2() throws IOException {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process01");
        System.out.println("processInstance = " + processInstance.getId());
    }


    @Test
    public void t3() throws IOException {
        Task task = taskService.createTaskQuery().processDefinitionKey("process01").taskAssignee("lisi").singleResult();
        if (task != null) {
            taskService.complete(task.getId());//完成任务时，设置流程变量的值
            System.out.println("任务执行完毕");
        }
    }

}
