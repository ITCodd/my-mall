package com.github;

import com.github.service.ProcessService;
import com.github.utils.FlowableUtils;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.BpmnAutoLayout;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.SubProcess;
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
public class FlowableTest06 {
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
        final String PROCESSID ="process06";
        final String PROCESSNAME ="或签会签监听器-子流程驳回测试";
        process.setId(PROCESSID);
        process.setName(PROCESSNAME);

        process.addFlowElement(FlowableUtils.createStartEvent("start","start"));
        List<String> orsignAssignees=new ArrayList<>();
        orsignAssignees.add("zsan");
        orsignAssignees.add("lisi");
        orsignAssignees.add("wangwu");
        process.addFlowElement(FlowableUtils.createUserTaskSignAssignees("task1","部门领导审批","orsign",orsignAssignees));
        List<String> counterSignAssignees=new ArrayList<>();
        counterSignAssignees.add("fugui");
        counterSignAssignees.add("liubei");
        counterSignAssignees.add("zouyu");
        process.addFlowElement(FlowableUtils.createUserTaskSignAssignees("task2","经理审批","countersign",counterSignAssignees));
        //采购子流程
        SubProcess subProcess = FlowableUtils.createSubProcess("subProcessTask", "采购部门采购");
        //子流程开始------------------------------
        subProcess.addFlowElement(FlowableUtils.createStartEvent("subStart","start"));
        subProcess.addFlowElement(FlowableUtils.createUserTaskAssignee("subTask1","采购审核员审批","xiaoli"));
        subProcess.addFlowElement(FlowableUtils.createUserTaskAssignee("subTask2","采购经理审批","laowan"));
        subProcess.addFlowElement(FlowableUtils.createEndEvent("subEnd","end"));
        subProcess.addFlowElement(FlowableUtils.createSequenceFlow("subStart", "subTask1"));
        subProcess.addFlowElement(FlowableUtils.createSequenceFlow("subTask1", "subTask2"));
        subProcess.addFlowElement(FlowableUtils.createSequenceFlow("subTask2", "subEnd"));
        //子流程结束------------------------------
        process.addFlowElement(subProcess);

        process.addFlowElement(FlowableUtils.createUserTaskAssignee("task3","总经理审批","mayu"));
        process.addFlowElement(FlowableUtils.createEndEvent("end","end"));
        process.addFlowElement(FlowableUtils.createSequenceFlow("start", "task1"));
        process.addFlowElement(FlowableUtils.createSequenceFlow("task1", "task2"));
        process.addFlowElement(FlowableUtils.createSequenceFlow("task2", "subProcessTask"));
        process.addFlowElement(FlowableUtils.createSequenceFlow("subProcessTask", "task3"));
        process.addFlowElement(FlowableUtils.createSequenceFlow("task3", "end"));

        String processName = PROCESSNAME+".bpmn20.xml";

        //生成自动布局
        new BpmnAutoLayout(bpmnModel).execute();

        repositoryService.createDeployment()
                .name(PROCESSNAME)
                .addBpmnModel(processName, bpmnModel)
                .deploy();

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process06");
        log.info("processInstance = " + processInstance.getId());

    }

    @Test
    public void t2() throws IOException {
        Task task = taskService.createTaskQuery().processDefinitionKey("process06").taskAssignee("xiaoli").singleResult();
        if (task != null) {
            taskService.complete(task.getId());//完成任务时，设置流程变量的值
            System.out.println("任务执行完毕");
        }
    }

    @Test
    public void t3() throws IOException {
        processService.genProcessDiagram("f262ede9-bc70-11ea-842c-005056c00008");
    }


    @Test
    public void t4() throws IOException {
        processService.move("f262ede9-bc70-11ea-842c-005056c00008","subTask1","task2");
    }

    @Test
    public void t5() throws IOException {
        processService.moveToParentProcess("f262ede9-bc70-11ea-842c-005056c00008","subTask1","task2");
    }

}
