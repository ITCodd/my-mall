package com.github;

import com.github.service.ProcessService;
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
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;

/**
 * @author: hjp
 * Date: 2020/6/30
 * Description:
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes={FlowableApplication.class})
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
    private ProcessService processService;

    @Test
    public void t1() throws IOException {
        BpmnModel bpmnModel=new BpmnModel();
        Process process = new Process();
        bpmnModel.addProcess(process);
        final String PROCESSID ="process03";
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

        byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(bpmnModel);
        String processName = PROCESSNAME+".bpmn20.xml";
        repositoryService.createDeployment()
                .name(PROCESSNAME)
                .addBytes(processName,bpmnBytes)
                .deploy();

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process03");
        System.out.println("processInstance = " + processInstance.getId());
    }

    @Test
    public void t2() throws IOException {
        Task task = taskService.createTaskQuery().processDefinitionKey("process03").taskAssignee("zouyu").singleResult();
        if (task != null) {
            taskService.complete(task.getId());//完成任务时，设置流程变量的值
            System.out.println("任务执行完毕");
        }
    }

    @Test
    public void t3() throws IOException {
        String taskId="2fb19cd7-bad9-11ea-921a-005056c00008";
        String assignee="laoma";
        processService.addMultiInstance(taskId,assignee);
    }

    @Test
    public void t4() throws IOException {
        String taskId="dd2a2de1-bad9-11ea-ab8f-005056c00008";
        processService.deleteMultiInstance(taskId);
    }


}
