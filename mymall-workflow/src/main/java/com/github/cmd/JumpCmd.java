package com.github.cmd;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.history.HistoryManager;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityManager;

/**
 * @author: hjp
 * Date: 2020/6/22
 * Description:
 */
public class JumpCmd implements Command<Void> {
    private  String taskId;

    private  String targetNodeId;

    private String comment;

    /**
     * @param taskId 当前任务ID
     * @param targetNodeId 目标节点定义ID
     */
    public JumpCmd(String taskId, String targetNodeId) {
        this.taskId = taskId;
        this.targetNodeId = targetNodeId;
    }

    public JumpCmd(String taskId, String targetNodeId, String comment) {
        this.taskId = taskId;
        this.targetNodeId = targetNodeId;
        this.comment = comment;
    }

    public JumpCmd(String taskId, String targetNodeId, RepositoryService repositoryService) {
        this.taskId = taskId;
        this.targetNodeId = targetNodeId;

    }

    @Override
    public Void execute(CommandContext commandContext) {
        RepositoryService repositoryService = commandContext.getProcessEngineConfiguration().getRepositoryService();
        // 获取任务实例管理类
        TaskEntityManager taskEntityManager = commandContext.getTaskEntityManager();
        // 获取当前任务实例
        TaskEntity currentTask = taskEntityManager.findById(this.taskId);

        // 获取当前节点的执行实例
        ExecutionEntity execution = currentTask.getExecution();
        String executionId = execution.getId();

        // 获取流程定义id
        String processDefinitionId = execution.getProcessDefinitionId();
        // 获取目标节点
//        Process process = ProcessDefinitionUtil.getProcess(processDefinitionId);
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        Process process = bpmnModel.getProcesses().get(0);
        FlowElement flowElement = process.getFlowElement(this.targetNodeId);
        // 获取历史管理
        HistoryManager historyManager = commandContext.getHistoryManager();
        // 通知当前活动结束(更新act_hi_actinst)
        historyManager.recordActivityEnd(execution, this.comment);
        // 通知任务节点结束(更新act_hi_taskinst)
        historyManager.recordTaskEnd(this.taskId, this.comment);
        // 删除正在执行的当前任务
        // 删除当前任务,来源任务
        taskEntityManager.deleteTask(currentTask, "jumpReason", true, true);

        // 此时设置执行实例的当前活动节点为目标节点
        execution.setCurrentFlowElement(flowElement);

        // 向operations中压入继续流程的操作类
        commandContext.getAgenda().planContinueProcessOperation(execution);
        return null;
    }
}
