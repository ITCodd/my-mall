package com.github;

import com.github.service.ProcessService;
import com.github.utils.FlowElementUtils;
import lombok.extern.slf4j.Slf4j;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: hjp
 * Date: 2020/7/3
 * Description:
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes={FlowableApplication.class})
@Slf4j
public class FlowableTest07 {
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
        final String PROCESSID ="process07";
        final String PROCESSNAME ="排他并行网关或签会签监听器驳回测试";
        process.setId(PROCESSID);
        process.setName(PROCESSNAME);

        process.addFlowElement(FlowElementUtils.createStartEvent("start","start"));
        List<String> orsignAssignees=new ArrayList<>();
        orsignAssignees.add("zsan");
        orsignAssignees.add("lisi");
        orsignAssignees.add("wangwu");
        process.addFlowElement(FlowElementUtils.createUserTaskSignAssignees("task1","部门领导审批","orsign",orsignAssignees));
        process.addFlowElement(FlowElementUtils.createExclusiveGateway("exclusiveGateway","排他网关"));
        process.addFlowElement(FlowElementUtils.createUserTaskAssignee("task2","人事审批","xiaomei"));
        List<String> counterSignAssignees=new ArrayList<>();
        counterSignAssignees.add("fugui");
        counterSignAssignees.add("liubei");
        counterSignAssignees.add("zouyu");
        process.addFlowElement(FlowElementUtils.createUserTaskSignAssignees("task3","经理审批","countersign",counterSignAssignees));
        process.addFlowElement(FlowElementUtils.createParallelGateway("parallelGateway1","并行网关开始"));
        process.addFlowElement(FlowElementUtils.createUserTaskAssignee("task4","总裁办审批","laoli"));
        process.addFlowElement(FlowElementUtils.createUserTaskAssignee("task5","人事主管审批","laowan"));
        process.addFlowElement(FlowElementUtils.createParallelGateway("parallelGateway2","并行网关结束"));
        process.addFlowElement(FlowElementUtils.createUserTaskAssignee("task6","总经理审批","zhaoyun"));
        process.addFlowElement(FlowElementUtils.createUserTaskAssignee("task7","CEO","mayu"));
        process.addFlowElement(FlowElementUtils.createEndEvent("end","end"));
        process.addFlowElement(FlowElementUtils.createSequenceFlow("start", "task1"));
        process.addFlowElement(FlowElementUtils.createSequenceFlow("task1", "exclusiveGateway"));
        process.addFlowElement(FlowElementUtils.createSequenceFlow("exclusiveGateway", "task3","${personNum < 3}"));
        process.addFlowElement(FlowElementUtils.createSequenceFlow("exclusiveGateway", "task2","${personNum >= 3}"));
        process.addFlowElement(FlowElementUtils.createSequenceFlow("task2", "task3"));
        process.addFlowElement(FlowElementUtils.createSequenceFlow("task3", "parallelGateway1"));
        process.addFlowElement(FlowElementUtils.createSequenceFlow("parallelGateway1", "task4"));
        process.addFlowElement(FlowElementUtils.createSequenceFlow("parallelGateway1", "task5"));
        process.addFlowElement(FlowElementUtils.createSequenceFlow("task4", "parallelGateway2"));
        process.addFlowElement(FlowElementUtils.createSequenceFlow("task5", "parallelGateway2"));
        process.addFlowElement(FlowElementUtils.createSequenceFlow("parallelGateway2", "task6"));
        process.addFlowElement(FlowElementUtils.createSequenceFlow("task6", "task7"));
        process.addFlowElement(FlowElementUtils.createSequenceFlow("task7", "end"));
        String processName = PROCESSNAME+".bpmn20.xml";

        //生成自动布局
//        new BpmnAutoLayout(bpmnModel).execute();

        repositoryService.createDeployment()
                .name(PROCESSNAME)
                .addBpmnModel(processName, bpmnModel)
                .deploy();

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESSID);
        log.info("processInstance = " + processInstance.getId());

    }

    @Test
    public void t2() throws IOException {
        Task task = taskService.createTaskQuery().processDefinitionKey("process07").taskAssignee("lisi").singleResult();
        if (task != null) {
            Map<String, Object> map=new HashMap<>();
            map.put("personNum",3);
            taskService.complete(task.getId(),map);//完成任务时，设置流程变量的值
            System.out.println("任务执行完毕");
        }
    }

    @Test
    public void t3() throws IOException {
        Task task = taskService.createTaskQuery().processDefinitionKey("process07").taskAssignee("laowan").singleResult();
        if (task != null) {
            taskService.complete(task.getId());//完成任务时，设置流程变量的值
            System.out.println("任务执行完毕");
        }
    }

    @Test
    public void t4() throws IOException {
        List<String> nodeIds=new ArrayList<>();
        nodeIds.add("task4");
//        nodeIds.add("task5");
        processService.moveNodeIdsToSingle("f951f0a1-c0bd-11ea-93b4-005056c00008",nodeIds,"task3");
    }

    @Test
    public void t5() throws IOException {
        List<String> nodeIds=new ArrayList<>();
        nodeIds.add("task4");
        nodeIds.add("task5");
        processService.moveSingleToNodeIds("f951f0a1-c0bd-11ea-93b4-005056c00008","task6",nodeIds);
    }

    @Test
    public void t6() throws IOException {
        processService.moveToPre("aade874f-c0c2-11ea-a41f-005056c00008","驳回意见");
    }



    @Test
    public void t7() throws IOException {
        processService.findPreNodes("1726d6a6-c0bb-11ea-8ecd-005056c00008");
    }

}
