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

        process.addFlowElement(FlowableUtils.createStartEvent("start","start"));
        List<String> orsignAssignees=new ArrayList<>();
        orsignAssignees.add("zsan");
        orsignAssignees.add("lisi");
        orsignAssignees.add("wangwu");
        process.addFlowElement(FlowableUtils.createUserTaskSignAssignees("task1","部门领导审批","orsign",orsignAssignees));
        process.addFlowElement(FlowableUtils.createExclusiveGateway("exclusiveGateway","排他网关"));
        process.addFlowElement(FlowableUtils.createUserTaskAssignee("task2","人事审批","xiaomei"));
        List<String> counterSignAssignees=new ArrayList<>();
        counterSignAssignees.add("fugui");
        counterSignAssignees.add("liubei");
        counterSignAssignees.add("zouyu");
        process.addFlowElement(FlowableUtils.createUserTaskSignAssignees("task3","经理审批","countersign",counterSignAssignees));
        process.addFlowElement(FlowableUtils.createParallelGateway("parallelGateway1","并行网关开始"));
        process.addFlowElement(FlowableUtils.createUserTaskAssignee("task4","总裁办审批","laoli"));
        process.addFlowElement(FlowableUtils.createUserTaskAssignee("task5","人事主管审批","laowan"));
        process.addFlowElement(FlowableUtils.createParallelGateway("parallelGateway2","并行网关结束"));
        process.addFlowElement(FlowableUtils.createUserTaskAssignee("task6","总经理审批","zhaoyun"));
        process.addFlowElement(FlowableUtils.createUserTaskAssignee("task7","CEO","mayu"));
        process.addFlowElement(FlowableUtils.createEndEvent("end","end"));
        process.addFlowElement(FlowableUtils.createSequenceFlow("start", "task1"));
        process.addFlowElement(FlowableUtils.createSequenceFlow("task1", "exclusiveGateway"));
        process.addFlowElement(FlowableUtils.createSequenceFlow("exclusiveGateway", "task3","${personNum < 3}"));
        process.addFlowElement(FlowableUtils.createSequenceFlow("exclusiveGateway", "task2","${personNum >= 3}"));
        process.addFlowElement(FlowableUtils.createSequenceFlow("task2", "task3"));
        process.addFlowElement(FlowableUtils.createSequenceFlow("task3", "parallelGateway1"));
        process.addFlowElement(FlowableUtils.createSequenceFlow("parallelGateway1", "task4"));
        process.addFlowElement(FlowableUtils.createSequenceFlow("parallelGateway1", "task5"));
        process.addFlowElement(FlowableUtils.createSequenceFlow("task4", "parallelGateway2"));
        process.addFlowElement(FlowableUtils.createSequenceFlow("task5", "parallelGateway2"));
        process.addFlowElement(FlowableUtils.createSequenceFlow("parallelGateway2", "task6"));
        process.addFlowElement(FlowableUtils.createSequenceFlow("task6", "task7"));
        process.addFlowElement(FlowableUtils.createSequenceFlow("task7", "end"));
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
        Task task = taskService.createTaskQuery().processDefinitionKey("process07").taskAssignee("zhaoyun").singleResult();
        if (task != null) {
            Map<String, Object> map=new HashMap<>();
//            map.put("personNum",1);
            taskService.complete(task.getId(),map);//完成任务时，设置流程变量的值
            System.out.println("任务执行完毕");
        }
    }

}
