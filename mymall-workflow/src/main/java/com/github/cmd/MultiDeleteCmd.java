package com.github.cmd;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.activiti.engine.ActivitiEngineAgenda;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.bpmn.behavior.MultiInstanceActivityBehavior;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.persistence.entity.TaskEntityManager;
import org.activiti.engine.impl.util.Activiti5Util;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.activiti.engine.repository.ProcessDefinition;

import java.io.Serializable;

/**
 * 减签业务实现类
 * https://blog.csdn.net/zhangcongyi420/java/article/details/106969611
 */
public class MultiDeleteCmd implements Command<Void>, Serializable {

    /**
     * 变量表中和实例数量相关的参数
     */
    protected final String NUMBER_OF_INSTANCES = "nrOfInstances";   //实例个数
    protected final String NUMBER_OF_ACTIVE_INSTANCES = "nrOfActiveInstances";  //活跃的实例个数
    protected final String NUMBER_OF_COMPLETED_INSTANCES = "nrOfCompletedInstances";    //已完成的实例个数
    protected String collectionElementIndexVariable = "loopCounter";    //循环次数，节点有多少个任务实例，就有多少个

    private String executionId; //执行动作ID
    private String taskId;  //任务ID
    private boolean executionIsCompleted;   //是否完成

    public MultiDeleteCmd(String executionId, boolean executionIsCompleted, String taskId) {
        this.executionId = executionId;
        this.executionIsCompleted = executionIsCompleted;
        this.taskId = taskId;
    }

    @Override
    public Void execute(CommandContext commandContext) {
        //实例任务对象管理器
        TaskEntityManager taskEntityManager = commandContext.getTaskEntityManager();
        // 获取执行实例管理器
        ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
        // 获取执行实例 三级树
        ExecutionEntity executionThirdEntity = executionEntityManager.findById(executionId);
        String processDefinitionId = executionThirdEntity.getProcessDefinitionId();
        // 获取当前实例的数据，因为我们要获取当前节点，并判断是否是多实例任务节点
        BpmnModel bpmnModel = ProcessDefinitionUtil.getBpmnModel(processDefinitionId);
        Activity miActivityElement = (Activity) bpmnModel.getFlowElement(executionThirdEntity.getCurrentActivityId());
        MultiInstanceLoopCharacteristics loopCharacteristics = miActivityElement.getLoopCharacteristics();
        if (loopCharacteristics == null) {
            throw new RuntimeException("没有找到loopCharacteristics，无法进行减签");
        }
        if (!(miActivityElement.getBehavior() instanceof MultiInstanceActivityBehavior)) {
            throw new RuntimeException("这个节点不是多实例节点，无法进行减签");
        }
        DeploymentManager deploymentManager = commandContext.getProcessEngineConfiguration().getDeploymentManager();
        ProcessDefinition processDefinition = null;
        if (processDefinitionId != null) {
            processDefinition = deploymentManager.findDeployedProcessDefinitionById(processDefinitionId);
            if (processDefinition == null) {
                throw new RuntimeException("找不到流程定义，无法进行减签");
            }
        }
        if (Activiti5Util.isActiviti5ProcessDefinition(commandContext, processDefinition)) {
            throw new RuntimeException("当前的减签类不支持5版本，无法进行减签");
        }
        // 获取执行实例 二级树
        ExecutionEntity secondExecutionEntity = executionThirdEntity.getParent();
        if (loopCharacteristics.isSequential()) {
            //如果是顺序会签，由于当前的执行实例已经是3级树，没有子实例了，无法再进行执行实例的删除了
        } else {
            executionEntityManager.deleteChildExecutions(executionThirdEntity, "delete multi sequtial instance", false);
            executionEntityManager.deleteExecutionAndRelatedData(executionThirdEntity, "delete multi sequtial instance", true);
        }
        // 更新变量表数据
        int loopCounter = 0;
        // 串行多实例
        if (loopCharacteristics.isSequential()) {
            //获取执行实例的循环次数
            loopCounter = getLoopVariable(executionThirdEntity, collectionElementIndexVariable);
        }
        if (!loopCharacteristics.isSequential()) {
            Integer variable = (Integer) secondExecutionEntity.getVariable(NUMBER_OF_ACTIVE_INSTANCES);
            secondExecutionEntity.setVariableLocal(NUMBER_OF_ACTIVE_INSTANCES, variable - 1);
        }
        //触发实例完成
        if (executionIsCompleted) {
            // 获取这个 NUMBER_OF_COMPLETED_INSTANCES ，即当前循环的实例中已完成的个数
            Integer variable = (Integer) secondExecutionEntity.getVariable(NUMBER_OF_COMPLETED_INSTANCES);
            secondExecutionEntity.setVariableLocal(NUMBER_OF_COMPLETED_INSTANCES, variable + 1);
            loopCounter++;
            executionThirdEntity.setVariableLocal(collectionElementIndexVariable, loopCounter);
        } else {
            Integer variable = (Integer) secondExecutionEntity.getVariable(NUMBER_OF_INSTANCES);
            secondExecutionEntity.setVariableLocal(NUMBER_OF_INSTANCES, variable - 1);
        }
        if (taskId != null) {
            taskEntityManager.delete(taskId);
        }
        // 触发实例运转
        ActivitiEngineAgenda agenda = commandContext.getAgenda();
        agenda.planContinueProcessInCompensation(executionThirdEntity);
        return null;
    }

    protected Integer getLoopVariable(DelegateExecution execution, String variableName) {
        Object value = execution.getVariableLocal(variableName);
        DelegateExecution parent = execution.getParent();
        while (value == null && parent != null) {
            value = parent.getVariableLocal(variableName);
            parent = parent.getParent();
        }
        return (Integer) (value != null ? value : 0);
    }
    
}
