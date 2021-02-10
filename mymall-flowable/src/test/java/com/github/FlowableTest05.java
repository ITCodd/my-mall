package com.github;

import com.github.service.ProcessService;
import com.github.utils.FlowElementUtils;
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
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={FlowableApplication.class})
@Slf4j
public class FlowableTest05 {
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
        final String PROCESSID ="process05";
        final String PROCESSNAME ="或签会签监听器驳回测试";
        process.setId(PROCESSID);
        process.setName(PROCESSNAME);

        process.addFlowElement(FlowElementUtils.createStartEvent("start","start"));
        List<String> orsignAssignees=new ArrayList<>();
        orsignAssignees.add("zsan");
        orsignAssignees.add("lisi");
        orsignAssignees.add("wangwu");
        process.addFlowElement(FlowElementUtils.createUserTaskSignAssignees("task1","部门领导审批","orsign",orsignAssignees));
        List<String> counterSignAssignees=new ArrayList<>();
        counterSignAssignees.add("fugui");
        counterSignAssignees.add("liubei");
        counterSignAssignees.add("zouyu");
        process.addFlowElement(FlowElementUtils.createUserTaskSignAssignees("task2","经理审批","countersign",counterSignAssignees));
        process.addFlowElement(FlowElementUtils.createUserTaskAssignee("task3","总经理审批","mayu"));
        process.addFlowElement(FlowElementUtils.createEndEvent("end","end"));
        process.addFlowElement(FlowElementUtils.createSequenceFlow("start", "task1"));
        process.addFlowElement(FlowElementUtils.createSequenceFlow("task1", "task2"));
        process.addFlowElement(FlowElementUtils.createSequenceFlow("task2", "task3"));
        process.addFlowElement(FlowElementUtils.createSequenceFlow("task3", "end"));

        String processName = PROCESSNAME+".bpmn20.xml";

        //生成自动布局
        new BpmnAutoLayout(bpmnModel).execute();

        repositoryService.createDeployment()
                .name(PROCESSNAME)
                .addBpmnModel(processName, bpmnModel)
                .deploy();

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process05");
        log.info("processInstance = " + processInstance.getId());

    }

    @Test
    public void t2() throws IOException {
        Task task = taskService.createTaskQuery().processDefinitionKey("process05").taskAssignee("mayu").singleResult();
        if (task != null) {
            taskService.complete(task.getId());//完成任务时，设置流程变量的值
            System.out.println("任务执行完毕");
        }
    }

    @Test
    public void t3() throws IOException {
        processService.genProcessDiagram("4411d347-6b4b-11eb-8bd9-005056c00008");
    }


    @Test
    public void t4() throws IOException {
        processService.move("0143f7b9-bc6d-11ea-a37a-005056c00008","task2","task1");
    }

}
