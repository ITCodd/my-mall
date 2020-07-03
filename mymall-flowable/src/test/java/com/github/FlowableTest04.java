package com.github;

import com.github.service.ProcessService;
import com.github.utils.FlowableUtils;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.BpmnAutoLayout;
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

@RunWith(SpringRunner.class)
@SpringBootTest(classes={FlowableApplication.class})
@Slf4j
public class FlowableTest04 {
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private ProcessService processService;


    @Test
    public void t1() throws IOException {
        BpmnModel bpmnModel=new BpmnModel();
        Process process = new Process();
        bpmnModel.addProcess(process);
        final String PROCESSID ="process04";
        final String PROCESSNAME ="驳回测试";
        process.setId(PROCESSID);
        process.setName(PROCESSNAME);

        process.addFlowElement(FlowableUtils.createStartEvent("start","start"));
        process.addFlowElement(FlowableUtils.createUserTaskAssignee("task1","部门领导审批","zsan"));
        process.addFlowElement(FlowableUtils.createUserTaskAssignee("task2","经理审批","fugui"));
        process.addFlowElement(FlowableUtils.createUserTaskAssignee("task3","总经理审批","mayu"));
        process.addFlowElement(FlowableUtils.createEndEvent("end","end"));
        process.addFlowElement(FlowableUtils.createSequenceFlow("start", "task1"));
        process.addFlowElement(FlowableUtils.createSequenceFlow("task1", "task2"));
        process.addFlowElement(FlowableUtils.createSequenceFlow("task2", "task3"));
        process.addFlowElement(FlowableUtils.createSequenceFlow("task3", "end"));

        String processName = PROCESSNAME+".bpmn20.xml";

        //生成自动布局
        new BpmnAutoLayout(bpmnModel).execute();

        repositoryService.createDeployment()
                .name(PROCESSNAME)
                .addBpmnModel(processName, bpmnModel)
                .deploy();

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process04");
        log.info("processInstance = " + processInstance.getId());

    }

    @Test
    public void t2() throws IOException {
        Task task = taskService.createTaskQuery().processDefinitionKey("process04").taskAssignee("mayu").singleResult();
        if (task != null) {
            taskService.complete(task.getId());//完成任务时，设置流程变量的值
            System.out.println("任务执行完毕");
        }
    }

    @Test
    public void t3() throws IOException {
        processService.genProcessDiagram("9cb1fc13-bc10-11ea-b05e-005056c00008");
    }


    @Test
    public void t4() throws IOException {
        processService.move("6d6f0921-bc6a-11ea-8521-005056c00008","task2","task1");
    }

}
