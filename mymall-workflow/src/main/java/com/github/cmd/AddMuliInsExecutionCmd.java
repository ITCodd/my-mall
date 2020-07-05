package com.github.cmd;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.activiti.engine.impl.bpmn.behavior.MultiInstanceActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.activiti.engine.impl.history.HistoryManager;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;

import java.io.Serializable;
import java.util.Map;

/**
 * 加签
 * https://blog.csdn.net/zhangcongyi420/java/article/details/106971812
 */
public class AddMuliInsExecutionCmd implements Command<Void>, Serializable {

    protected final String NUMBER_OF_INSTANCES = "nrOfInstances";
    protected final String NUMBER_OF_ACTIVE_INSTANCES = "nrOfActiveInstances";
    protected final String NUMBER_OF_COMPLETED_INSTANCES = "nrOfCompletedInstances";
    protected String collectionElementIndexVariable = "loopCounter";

    private String curExecutionId;    //执行实例的父ID
    private String activityId;    //加签的节点ID

    private Map<String, Object> executionVariables; //执行变量

    public AddMuliInsExecutionCmd(String curExecutionId, String activityId, Map<String, Object> executionVariables) {
        this.curExecutionId = curExecutionId;
        this.activityId = activityId;
        this.executionVariables = executionVariables;
    }

    @Override
    public Void execute(CommandContext commandContext) {
        // 获取执行实例管理器
        ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
        //获取二级实例
        ExecutionEntity curExecution = executionEntityManager.findById(curExecutionId);
        ExecutionEntity secondExecution = executionEntityManager.findById(curExecution.getParentId());
        String processDefinitionId = secondExecution.getProcessDefinitionId();
        if (secondExecution == null) {
            throw new RuntimeException("找不到二级实例数据，无法进行加签");
        }
        // 通过二级执行实例创建三级执行实例
        ExecutionEntity childExecution = executionEntityManager.createChildExecution(secondExecution);
        childExecution.setCurrentFlowElement(secondExecution.getCurrentFlowElement());
        // 获取当前实例中的数据，因为我们要获取当前的节点，并判断是否是多实例任务节点
        BpmnModel bpmnModel = ProcessDefinitionUtil.getBpmnModel(processDefinitionId);
        Activity miActivityElement = (Activity) bpmnModel
                .getFlowElement(secondExecution.getCurrentFlowElement().getId());
        MultiInstanceLoopCharacteristics loopCharacteristics = miActivityElement.getLoopCharacteristics();
        if (loopCharacteristics == null) {
            throw new RuntimeException("没有找到loopCharacteristics，无法进行加签");
        }
        if (!(miActivityElement.getBehavior() instanceof MultiInstanceActivityBehavior)) {
            throw new RuntimeException("这个节点不是多实例节点，无法进行加签");
        }
        //获取当前节点的任务实例数
        Integer currentNumberOfInstance = (Integer) secondExecution.getVariable(NUMBER_OF_INSTANCES);
        secondExecution.setVariableLocal(NUMBER_OF_INSTANCES, currentNumberOfInstance + 1);
        if (executionVariables != null) {
            //执行实例和变量关联，因此需要为执行实例添加关联的变量数据
            childExecution.setVariables(executionVariables);
        }
        //通过historyManager制造三级树执行实例
        HistoryManager historyManager = commandContext.getHistoryManager();
        historyManager.recordActivityStart(childExecution);
        Object behavior = miActivityElement.getBehavior();
        ParallelMultiInstanceBehavior p = (ParallelMultiInstanceBehavior) behavior;
        p.getInnerActivityBehavior().execute(childExecution);
        return null;
    }
}