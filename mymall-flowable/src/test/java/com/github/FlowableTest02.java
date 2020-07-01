package com.github;

import com.github.utils.FlowableUtils;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: hjp
 * Date: 2020/6/29
 * Description:
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes={FlowableApplication.class})
public class FlowableTest02 {
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
        final String PROCESSID ="process02";
        final String PROCESSNAME ="或签会签测试";
        process.setId(PROCESSID);
        process.setName(PROCESSNAME);

        Map<String,Object> map=new HashMap<>();
        process.addFlowElement(FlowableUtils.createStartEvent("start","start"));
        List<String> orsignAssignees=new ArrayList<>();
        orsignAssignees.add("zsan");
        orsignAssignees.add("lisi");
        orsignAssignees.add("wangwu");
        String key1 = FlowableUtils.getVariablesKey("orsign", "task1");
        map.put(key1,orsignAssignees);
        process.addFlowElement(FlowableUtils.createUserTaskSign("task1","部门领导审批","orsign"));
        List<String> counterSignAssignees=new ArrayList<>();
        counterSignAssignees.add("fugui");
        counterSignAssignees.add("liubei");
        counterSignAssignees.add("zouyu");
        String key2 = FlowableUtils.getVariablesKey("countersign", "task2");
        map.put(key2,counterSignAssignees);
        process.addFlowElement(FlowableUtils.createUserTaskSign("task2","经理审批","countersign"));
        process.addFlowElement(FlowableUtils.createEndEvent("end","end"));
        process.addFlowElement(FlowableUtils.createSequenceFlow("start", "task1"));
        process.addFlowElement(FlowableUtils.createSequenceFlow("task1", "task2"));
        process.addFlowElement(FlowableUtils.createSequenceFlow("task2", "end"));

        byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(bpmnModel);
        String processName = PROCESSNAME+".bpmn20.xml";
        repositoryService.createDeployment()
                .name(PROCESSNAME)
                .addBytes(processName,bpmnBytes)
                .deploy();

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process02",map);
        System.out.println("processInstance = " + processInstance.getId());
    }

    @Test
    public void t3() throws IOException {
        Task task = taskService.createTaskQuery().processDefinitionKey("process02").taskAssignee("zouyu").singleResult();
        if (task != null) {
            taskService.complete(task.getId());//完成任务时，设置流程变量的值
            System.out.println("任务执行完毕");
        }
    }
}
