package com.github.utils;

import org.activiti.bpmn.model.*;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * @author: hjp
 * Date: 2020/6/15
 * Description:
 */
public class ActivitiUtils {

    /**
     *
     * @param userTask 任务节点
     * @param nodeId  节点id
     * @param signType
     *          countersign  会签
     *          orsign       或签
     */
    public static void buildSign(UserTask userTask,String nodeId,String signType){
        MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics = new MultiInstanceLoopCharacteristics();
        multiInstanceLoopCharacteristics.setInputDataItem("${" + getVariablesKey(signType, nodeId)+ "}");
        multiInstanceLoopCharacteristics.setElementVariable(signType + nodeId );
        if (!StringUtils.equals(signType,"countersign")) {
            multiInstanceLoopCharacteristics.setCompletionCondition("${nrOfCompletedInstances/nrOfInstances > 0}");
        }
        userTask.setLoopCharacteristics(multiInstanceLoopCharacteristics);
        userTask.setAssignee("${" + signType + nodeId + "}");
    }

    public static String getVariablesKey(String signType,String nodeId){
        return signType + "assignees" + nodeId;
    }

    //包容网关
    public static InclusiveGateway createInclusiveGateway(String id, String name) {
        InclusiveGateway inclusiveGateway = new InclusiveGateway();
        inclusiveGateway.setId(id);
        inclusiveGateway.setName(name);
        return inclusiveGateway;
    }

    // 排他网关
    public static ExclusiveGateway createExclusiveGateway(String id, String name) {
        ExclusiveGateway exclusiveGateway = new ExclusiveGateway();
        exclusiveGateway.setId(id);
        exclusiveGateway.setName(name);
        return exclusiveGateway;
    }

    // 并行网关
    public static ParallelGateway createParallelGateway(String id, String name) {
        ParallelGateway gateway = new ParallelGateway();
        gateway.setId(id);
        gateway.setName(name);
        return gateway;
    }

    // 连接线
    public static SequenceFlow createSequenceFlow(String sourceRef, String targetRef) {
        SequenceFlow sequenceFlow = new SequenceFlow(sourceRef, targetRef);
        return sequenceFlow;
    }

    /*连线*/
    public static SequenceFlow createSequenceFlow(String from, String to, String name, String conditionExpression) {
        SequenceFlow flow = new SequenceFlow();
        flow.setSourceRef(from);
        flow.setTargetRef(to);
        flow.setName(name);
        if(StringUtils.isNotEmpty(conditionExpression)){
            flow.setConditionExpression(conditionExpression);
        }
        return flow;
    }

    /*任务节点*/
    public static UserTask createUserTaskSign(String id, String name,String signType) {
        UserTask userTask = new UserTask();
        userTask.setId(id);
        userTask.setName(name);
        buildSign(userTask,id,signType);
        return userTask;
    }

    /*任务节点*/
    public static UserTask createUserTaskAssignee(String id, String name, String assignee) {
        UserTask userTask = new UserTask();
        userTask.setId(id);
        userTask.setName(name);
        userTask.setAssignee(assignee);
        return userTask;
    }

    /*任务节点*/
    public static UserTask createUserTaskCandidateGroup(String id, String name, List<String> candidateGroups) {
        UserTask userTask = new UserTask();
        userTask.setName(name);
        userTask.setId(id);
        userTask.setCandidateGroups(candidateGroups);
        return userTask;
    }

    /*任务节点*/
    public static UserTask createUserTaskCandidateUsers(String id, String name,List<String> candidateUsers) {
        UserTask userTask = new UserTask();
        userTask.setName(name);
        userTask.setId(id);
        userTask.setCandidateUsers(candidateUsers);
        return userTask;
    }

    /*开始节点*/
    public static StartEvent createStartEvent(String id, String name) {
        StartEvent startEvent = new StartEvent();
        startEvent.setId(id);
        startEvent.setName(name);
        return startEvent;
    }

    /*开始节点*/
    public static StartEvent createStartEvent() {
        StartEvent startEvent = new StartEvent();
        startEvent.setId("startEvent");
        return startEvent;
    }

    /*结束节点*/
    public static FlowElement createEndEvent(String id, String name) {
        EndEvent endEvent = new EndEvent();
        endEvent.setId(id);
        endEvent.setName(name);
        return endEvent;
    }


    /*结束节点*/
    public static EndEvent createEndEvent() {
        EndEvent endEvent = new EndEvent();
        endEvent.setId("endEvent");
        return endEvent;
    }
}
