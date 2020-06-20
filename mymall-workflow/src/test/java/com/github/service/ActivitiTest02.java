package com.github.service;

import com.github.WorkflowApplication;
import com.github.model.MyBiz;
import com.github.utils.ActivitiUtils;
import org.activiti.bpmn.BpmnAutoLayout;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
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
    @Autowired
    private HistoryService historyService;

    @Test
    @Transactional
    public void t1() throws IOException {
        BpmnModel model = new BpmnModel();
        Process process=new Process();
        model.addProcess(process);
        final String PROCESSID ="process03";
        final String PROCESSNAME ="动态创建流程3";
        process.setId(PROCESSID);
        process.setName(PROCESSNAME);

        process.addFlowElement(ActivitiUtils.createStartEvent("start","start"));
        process.addFlowElement(ActivitiUtils.createUserTaskAssignee("task-1","部门领导审批","zhangsan"));
        process.addFlowElement(ActivitiUtils.createUserTaskAssignee("task-2","经理审批","lisi"));
        process.addFlowElement(ActivitiUtils.createEndEvent("end","end"));
        process.addFlowElement(ActivitiUtils.createSequenceFlow("start", "task-1"));
        process.addFlowElement(ActivitiUtils.createSequenceFlow("task-1", "task-2"));
        process.addFlowElement(ActivitiUtils.createSequenceFlow("task-2", "end"));

        new BpmnAutoLayout(model).execute();

        Deployment deployment = repositoryService.createDeployment().addBpmnModel(PROCESSID + ".bpmn", model).name(PROCESSID + "_deployment").deploy();

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESSID);

        InputStream processDiagram = repositoryService.getProcessDiagram(processInstance.getProcessDefinitionId());
        FileUtils.copyInputStreamToFile(processDiagram, new File("/deployments/"+PROCESSID+".png"));

        InputStream processBpmn = repositoryService.getResourceAsStream(deployment.getId(), PROCESSID+".bpmn");
        FileUtils.copyInputStreamToFile(processBpmn,new File("/deployments/"+PROCESSID+".bpmn"));
    }

    @Test
    public void t2() throws IOException {
        Task task = taskService.createTaskQuery().processDefinitionKey("process03").taskAssignee("lisi").singleResult();
        if (task != null) {
            taskService.complete(task.getId());//完成任务时，设置流程变量的值
            System.out.println("任务执行完毕");
        }
    }

    @Test
    public void t3() throws IOException {
        List<Task> tasks = taskService.createTaskQuery()
                .processDefinitionKey("process03")
                .list();
        for (Task task : tasks) {
            System.out.println("getId = " + task.getId());
            System.out.println("getProcessInstanceId = " + task.getProcessInstanceId());
            System.out.println("getName = " + task.getName());
            System.out.println("getAssignee = " + task.getAssignee());
            taskService.complete(task.getId());
        }
    }

    /**
     * 查询历史流程变量
     */
    @Test
    public void queryHistoricLocalVariables() {
        HistoricTaskInstanceQuery historicTaskInstanceQuery = historyService.createHistoricTaskInstanceQuery();
        List<HistoricTaskInstance> list = historicTaskInstanceQuery.includeTaskLocalVariables()
                .finished().list();

        for (HistoricTaskInstance hti : list) {
            System.out.println("============================");
            System.out.println("任务id:" + hti.getId());
            System.out.println("任务名称:" + hti.getName());
            System.out.println("任务负责人:" + hti.getAssignee());
            System.out.println("任务local变量：" + hti.getTaskLocalVariables());
        }
    }
}
