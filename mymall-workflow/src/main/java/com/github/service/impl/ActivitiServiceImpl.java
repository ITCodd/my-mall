package com.github.service.impl;

import com.github.cmd.JumpCmd;
import com.github.pojo.FlowTaskNode;
import com.github.service.ActivitiService;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.TaskServiceImpl;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: hjp
 * Date: 2020/6/22
 * Description:
 */
@Service
public class ActivitiServiceImpl implements ActivitiService {
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private TaskService taskService;

    @Override
    public void backToPreNode(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        FlowTaskNode preTaskNode = findPreNode(task);
        if (preTaskNode == null) {
            return;
        }
        nodeJumpTo(taskId, preTaskNode.getNodeId(), "驳回上环节");
    }

    public  void nodeJumpTo(String taskId, String targetNodeId, String comment) {
        CommandExecutor commandExecutor = ((TaskServiceImpl) taskService).getCommandExecutor();
        JumpCmd jumpCmd = new JumpCmd(taskId, targetNodeId, comment);
        commandExecutor.execute(jumpCmd);
    }

    private FlowTaskNode findPreNode(Task task) {
        Process process = getProcess(task.getProcessDefinitionId());
        List<FlowTaskNode> preNodes=new ArrayList<>();
        //获取当前节点执行的上级节点
        findIncomeNodes(process,task.getTaskDefinitionKey(),preNodes);
        for (int i = 0; i < preNodes.size(); i++) {
            List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery()
                    .processDefinitionId(task.getProcessDefinitionId()).activityId(preNodes.get(i).getNodeId()).finished().list();
            if (CollectionUtil.isEmpty(historicActivityInstances)) {
                preNodes.remove(i);
                i--;
            }
        }
        if (CollectionUtil.isEmpty(preNodes)) {
            return null;
        }
        return preNodes.get(0);
    }

    private void findIncomeNodes(Process process, String currentNodeId, List<FlowTaskNode> preNodes) {
        FlowElement currentFlowElement = process.getFlowElement(currentNodeId);
        List<SequenceFlow> incomingFlows = null;
        if (currentFlowElement instanceof UserTask) {
            incomingFlows = ((UserTask) currentFlowElement).getIncomingFlows();
        } else if (currentFlowElement instanceof Gateway) {
            incomingFlows = ((Gateway) currentFlowElement).getIncomingFlows();
        } else if (currentFlowElement instanceof StartEvent) {
            incomingFlows = ((StartEvent) currentFlowElement).getIncomingFlows();
        }
        if(!CollectionUtils.isEmpty(incomingFlows)){
            for (SequenceFlow incomingFlow : incomingFlows) {
                // 出线的上一节点
                String sourceFlowElementID = incomingFlow.getSourceRef();
                // 查询上一节点的信息
                FlowElement preFlowElement = process.getFlowElement(sourceFlowElementID);

                //用户任务
                if (preFlowElement instanceof UserTask) {
                    preNodes.add(new FlowTaskNode(preFlowElement.getId(), preFlowElement.getName(),(org.activiti.bpmn.model.Task)preFlowElement));
//                    if (isAll) {
//                       //如果需要此处可以继续findIncomeNodes
//                    }
                }
                //排他网关
                else if (preFlowElement instanceof ExclusiveGateway) {
                    findIncomeNodes(process,preFlowElement.getId(), preNodes);
                }
                //并行网关
                else if (preFlowElement instanceof ParallelGateway) {
                    findIncomeNodes(process,preFlowElement.getId(), preNodes);
                }
            }
        }
    }


    public  Process getProcess(String processDefId) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefId);
        return bpmnModel.getProcesses().get(0);
    }
}
