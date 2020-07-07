package com.github.service.impl;

import com.github.draw.FlowProcessDiagramGenerator;
import com.github.service.ProcessService;
import com.github.utils.ProcessUtils;
import com.github.utils.ResultData;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.flowable.bpmn.constants.BpmnXMLConstants;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ActivityInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProcessServiceImpl implements ProcessService {

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private FlowProcessDiagramGenerator flowProcessDiagramGenerator;

    @Override
    public void addMultiInstance(String taskId,String assignee) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        runtimeService.addMultiInstanceExecution(task.getTaskDefinitionKey(), task.getProcessInstanceId(), Collections.singletonMap("assignee", assignee));
    }

    @Override
    public void deleteMultiInstance(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        runtimeService.deleteMultiInstanceExecution(task.getExecutionId(), true);
    }

    /**
     * 流程是否已经结束
     *
     * @param processInstanceId 流程实例ID
     * @return
     */
    public boolean isFinished(String processInstanceId) {
        return historyService.createHistoricProcessInstanceQuery().finished()
                .processInstanceId(processInstanceId).count() > 0;
    }


    @Override
    public void genProcessDiagram(String processId) {
        //1.获取当前的流程实例
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();
        String processDefinitionId = null;
        List<String> activeActivityIds = new ArrayList<>();
        List<String> highLightedFlows = new ArrayList<>();
        //2.获取所有的历史轨迹线对象
        List<HistoricActivityInstance> historicSquenceFlows = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processId).activityType(BpmnXMLConstants.ELEMENT_SEQUENCE_FLOW).list();
        historicSquenceFlows.forEach(historicActivityInstance -> highLightedFlows.add(historicActivityInstance.getActivityId()));
        //3. 获取流程定义id和高亮的节点id
        if (processInstance != null) {
            //3.1. 正在运行的流程实例
            processDefinitionId = processInstance.getProcessDefinitionId();
            activeActivityIds = runtimeService.getActiveActivityIds(processId);
        } else {
            //3.2. 已经结束的流程实例
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processId).singleResult();
            processDefinitionId = historicProcessInstance.getProcessDefinitionId();
            //3.3. 获取结束节点列表
            List<HistoricActivityInstance> historicEnds = historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(processId).activityType(BpmnXMLConstants.ELEMENT_EVENT_END).list();
            List<String> finalActiveActivityIds = activeActivityIds;
            historicEnds.forEach(historicActivityInstance -> finalActiveActivityIds.add(historicActivityInstance.getActivityId()));
        }
        //4. 获取bpmnModel对象
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        //5. 生成图片流
        InputStream in = flowProcessDiagramGenerator.generateDiagram(bpmnModel, activeActivityIds, highLightedFlows);
        try(OutputStream out=new FileOutputStream(new File("F:\\var",processId+".png"))){
            IOUtils.copy(in,out);
        }catch (Exception e){
            log.info("写入文件失败",e);
        }
        IOUtils.closeQuietly(in);
    }

    /**
     * 驳回（或移动节点），不支持并行网关，支持从父流程退回子流程SubProcess
     * @param proInstId  流程实例id
     * @param nodeId     流程id定义名称
     * @param toNodeId   跳转或驳回的流程节点id定义名称
     */
    @Override
    public void move(String proInstId, String nodeId, String toNodeId) {
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(proInstId)
                .moveActivityIdTo(nodeId, toNodeId)
                .changeState();
    }

    /**
     * 驳回（或移动节点）到父流程，不支持并行网关
     * @param proInstId     流程实例id
     * @param subNodeId     子流程id定义名称
     * @param parentNodeId  父流程的流程节点定义id
     */
    @Override
    public void moveToParentProcess(String proInstId, String subNodeId, String parentNodeId) {
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(proInstId)
                .moveActivityIdToParentActivityId(subNodeId, parentNodeId)
                .changeState();
    }

    /**
     * 驳回（或移动节点）到子流程，不支持并行网关，只支持调用子流程 CallActivity
     * @param proInstId  流程实例id
     * @param subProcess 子流程id定义名称
     * @param subNodeId  子流程里的流程节点定义id
     * @param parentNodeId 父流程的流程节点定义id
     */
    @Override
    public void moveToSubProcess(String proInstId, String subProcess, String subNodeId, String parentNodeId) {
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(proInstId)
                .moveActivityIdToSubProcessInstanceActivityId(parentNodeId, subNodeId, subProcess)
                .changeState();
    }

    /**
     * 驳回（或移动节点），支持并行网关，支持从父流程退回子流程SubProcess
     * @param proInstId   流程实例id
     * @param nodeIds     流程节点定义id集合
     * @param toNodeId    流程节点定义id
     */
    @Override
    public void moveNodeIdsToSingle(String proInstId, List<String> nodeIds, String toNodeId) {
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(proInstId)
                .moveActivityIdsToSingleActivityId(nodeIds, toNodeId)
                .changeState();
    }

    /**
     * 驳回（或移动节点），支持并行网关，支持从父流程退回子流程SubProcess
     * @param proInstId    流程实例id
     * @param nodeId       流程节点定义id
     * @param toNodeIds    流程节点定义id集合
     */
    @Override
    public void moveSingleToNodeIds(String proInstId, String nodeId, List<String> toNodeIds) {
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(proInstId)
                .moveSingleActivityIdToActivityIds(nodeId, toNodeIds)
                .changeState();
    }

    @Override
    public ResultData moveToPre(String taskId, String comment) {
        // 当前任务 task
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if(task.isSuspended()){
            return ResultData.fail("任务处于挂起状态");
        }
        // 获取流程定义信息
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(task.getProcessDefinitionId()).singleResult();
        // 获取所有节点信息
        Process process = repositoryService.getBpmnModel(processDefinition.getId()).getProcesses().get(0);
        // 获取当前任务节点元素
        FlowElement source = process.getFlowElement(task.getTaskDefinitionKey(), true);

        // 目的获取所有跳转到的节点 targetNodeIds
        // 获取当前节点的所有父级用户任务节点
        // 深度优先算法思想：延边迭代深入
        List<UserTask> parentUserTaskList = ProcessUtils.iteratorFindParentUserTasks(source, null, null);
        if (parentUserTaskList == null || parentUserTaskList.size() == 0) {
            return ResultData.fail("当前节点为初始任务节点，不能驳回");
        }
        // 获取活动 ID 即节点 Key
        List<String> parentUserTaskKeyList = new ArrayList<>();
        parentUserTaskList.forEach(item -> parentUserTaskKeyList.add(item.getId()));
        // 获取全部历史节点活动实例，即已经走过的节点历史，数据采用开始时间升序
        List<HistoricTaskInstance> historicTaskInstanceList = historyService.createHistoricTaskInstanceQuery().processInstanceId(task.getProcessInstanceId()).orderByHistoricTaskInstanceStartTime().asc().list();
        // 数据清洗，将回滚导致的脏数据清洗掉
        List<String> lastHistoricTaskInstanceList = ProcessUtils.historicTaskInstanceClean(process, historicTaskInstanceList);
        // 此时历史任务实例为倒序，获取最后走的节点
        List<String> targetNodeIds = new ArrayList<>();
        // 循环结束标识，遇到当前目标节点的次数
        int number = 0;
        StringBuilder parentHistoricTaskKey = new StringBuilder();
        for (String historicTaskInstanceKey : lastHistoricTaskInstanceList) {
            // 当会签时候会出现特殊的，连续都是同一个节点历史数据的情况，这种时候跳过
            if (parentHistoricTaskKey.toString().equals(historicTaskInstanceKey)) {
                continue;
            }
            parentHistoricTaskKey = new StringBuilder(historicTaskInstanceKey);
            if (historicTaskInstanceKey.equals(task.getTaskDefinitionKey())) {
                number++;
            }
            // 在数据清洗后，历史节点就是唯一一条从起始到当前节点的历史记录，理论上每个点只会出现一次
            // 在流程中如果出现循环，那么每次循环中间的点也只会出现一次，再出现就是下次循环
            // number == 1，第一次遇到当前节点
            // number == 2，第二次遇到，代表最后一次的循环范围
            if (number == 2) {
                break;
            }
            // 如果当前历史节点，属于父级的节点，说明最后一次经过了这个点，需要退回这个点
            if (parentUserTaskKeyList.contains(historicTaskInstanceKey)) {
                targetNodeIds.add(historicTaskInstanceKey);
            }
        }


        // 目的获取所有需要被跳转的节点 currentNodeIds
        // 取其中一个父级任务，因为后续要么存在公共网关，要么就是串行公共线路
        UserTask oneUserTask = parentUserTaskList.get(0);
        // 获取所有正常进行的任务节点 Key，这些任务不能直接使用，需要找出其中需要撤回的任务
        List<Task> runTaskList = taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).list();
        List<String> runTaskKeyList = new ArrayList<>();
        runTaskList.forEach(item -> runTaskKeyList.add(item.getTaskDefinitionKey()));
        // 需驳回任务列表
        List<String> currentNodeIds = new ArrayList<>();
        // 通过父级网关的出口连线，结合 runTaskList 比对，获取需要撤回的任务
        List<UserTask> currentUserTaskList = ProcessUtils.iteratorFindChildUserTasks(oneUserTask, runTaskKeyList, null, null);
        currentUserTaskList.forEach(item -> currentNodeIds.add(item.getId()));


        // 规定：并行网关之前节点必须需存在唯一用户任务节点，如果出现多个任务节点，则并行网关节点默认为结束节点，原因为不考虑多对多情况
        if (targetNodeIds.size() > 1 && currentNodeIds.size() > 1) {
            //会签一个nodeId有多个task
            Set<String> currentActivityIds = new HashSet<>(currentNodeIds);
            if(currentActivityIds.size()==1){
                //排他网关分支进入的情况
                List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().processInstanceId(task.getProcessInstanceId())
                        .taskDefinitionKeys(targetNodeIds).orderByHistoricTaskInstanceEndTime().desc().list();
                HistoricTaskInstance historicTaskInstance = list.get(0);
                String taskDefinitionKey = historicTaskInstance.getTaskDefinitionKey();
                targetNodeIds.clear();
                targetNodeIds.add(taskDefinitionKey);
            }else{
                return ResultData.fail("任务出现多对多情况，无法撤回");
            }

        }

        // 循环获取那些需要被撤回的节点的ID，用来设置驳回原因
        List<String> currentTaskIds = new ArrayList<>();
        currentNodeIds.forEach(currentId -> runTaskList.forEach(runTask -> {
            if (currentId.equals(runTask.getTaskDefinitionKey())) {
                currentTaskIds.add(runTask.getId());
            }
        }));
        // 设置驳回信息
        currentTaskIds.forEach(item -> {
            taskService.addComment(item, task.getProcessInstanceId(), "taskStatus", "reject");
            taskService.addComment(item, task.getProcessInstanceId(), "taskMessage", "已驳回");
            taskService.addComment(item, task.getProcessInstanceId(), "taskComment", comment);
        });

        try {
            // 如果父级任务多于 1 个，说明当前节点不是并行节点，原因为不考虑多对多情况
            if (targetNodeIds.size() > 1) {
                // 1 对 多任务跳转，currentNodeIds 当前节点(1)，targetNodeIds 跳转到的节点(多)
                this.moveSingleToNodeIds(task.getProcessInstanceId(),currentNodeIds.get(0),targetNodeIds);
            }
            // 如果父级任务只有一个，因此当前任务可能为网关中的任务
            if (targetNodeIds.size() == 1) {
                // 1 对 1 或 多 对 1 情况，currentNodeIds 当前要跳转的节点列表(1或多)，targetNodeIds.get(0) 跳转到的节点(1)
                this.moveNodeIdsToSingle(task.getProcessInstanceId(),currentNodeIds,targetNodeIds.get(0));
            }
        } catch (FlowableObjectNotFoundException e) {
            return ResultData.fail("未找到流程实例，流程可能已发生变化");
        } catch (FlowableException e) {
            return ResultData.fail("无法取消或开始活动");
        }
        return ResultData.success();
    }

    @Override
    public void findPre(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        Process process = repositoryService.getBpmnModel(task.getProcessDefinitionId()).getProcesses().get(0);
        FlowNode currentFlowElement = (FlowNode) process.getFlowElement(task.getTaskDefinitionKey(), true);
        log.info("currentFlowElement:"+currentFlowElement.getId());
        List<ActivityInstance> activitys = runtimeService.createActivityInstanceQuery()
                .processInstanceId(task.getProcessInstanceId()).finished().orderByActivityInstanceStartTime().asc().list();
        for (ActivityInstance activity : activitys) {
            log.info("activity:{}",activity);
        }
        List<String> activityIds = activitys.stream()
                .filter(activity -> activity.getActivityType().equals(BpmnXMLConstants.ELEMENT_TASK_USER))
                .filter(activity -> !activity.getActivityId().equals(task.getTaskDefinitionKey())).map(ActivityInstance::getActivityId)
                .distinct().collect(Collectors.toList());
        for (String activityId : activityIds) {
            System.out.println("activityId = " + activityId);
            FlowNode toBackFlowElement = (FlowNode) process.getFlowElement(activityId, true);
            if (isReachable(process, toBackFlowElement, currentFlowElement)) {
                log.info("BackFlowElement:{}",toBackFlowElement.getId());
            }
        }
    }

    public static boolean isReachable(Process process, FlowNode sourceElement, FlowNode targetElement) {
        return isReachable(process, sourceElement, targetElement, Sets.newHashSet());
    }

    public static boolean isReachable(Process process, String sourceElementId, String targetElementId) {
        FlowElement sourceFlowElement = process.getFlowElement(sourceElementId, true);
        FlowNode sourceElement = null;
        if (sourceFlowElement instanceof FlowNode) {
            sourceElement = (FlowNode) sourceFlowElement;
        } else if (sourceFlowElement instanceof SequenceFlow) {
            sourceElement = (FlowNode) ((SequenceFlow) sourceFlowElement).getTargetFlowElement();
        }
        FlowElement targetFlowElement = process.getFlowElement(targetElementId, true);
        FlowNode targetElement = null;
        if (targetFlowElement instanceof FlowNode) {
            targetElement = (FlowNode) targetFlowElement;
        } else if (targetFlowElement instanceof SequenceFlow) {
            targetElement = (FlowNode) ((SequenceFlow) targetFlowElement).getTargetFlowElement();
        }
        if (sourceElement == null) {
            throw new FlowableException("Invalid sourceElementId '" + sourceElementId
                    + "': no element found for this id n process definition '" + process.getId() + "'");
        }
        if (targetElement == null) {
            throw new FlowableException("Invalid targetElementId '" + targetElementId
                    + "': no element found for this id n process definition '" + process.getId() + "'");
        }
        Set<String> visitedElements = new HashSet<>();
        return isReachable(process, sourceElement, targetElement, visitedElements);
    }

    public static boolean isReachable(Process process, FlowNode sourceElement, FlowNode targetElement,
                                      Set<String> visitedElements) {

        if (sourceElement instanceof StartEvent && isInEventSubprocess(sourceElement)) {
            return false;
        }
        if (sourceElement.getOutgoingFlows().size() == 0) {
            visitedElements.add(sourceElement.getId());
            FlowElementsContainer parentElement = process.findParent(sourceElement);
            if (parentElement instanceof SubProcess) {
                sourceElement = (SubProcess) parentElement;
                // 子流程的结束节点，若目标节点在该子流程中，说明无法到达，返回false
                if (((SubProcess) sourceElement).getFlowElement(targetElement.getId()) != null) {
                    return false;
                }
            } else {
                return false;
            }
        }
        if (sourceElement.getId().equals(targetElement.getId())) {
            return true;
        }
        // To avoid infinite looping, we must capture every node we visit
        // and check before going further in the graph if we have already
        // visited the node.
        visitedElements.add(sourceElement.getId());
        // 当前节点能够到达子流程，且目标节点在子流程中，说明可以到达，返回true
        if (sourceElement instanceof SubProcess
                && ((SubProcess) sourceElement).getFlowElement(targetElement.getId()) != null) {
            return true;
        }
        List<SequenceFlow> sequenceFlows = sourceElement.getOutgoingFlows();
        if (sequenceFlows != null && sequenceFlows.size() > 0) {
            for (SequenceFlow sequenceFlow : sequenceFlows) {
                String targetRef = sequenceFlow.getTargetRef();
                FlowNode sequenceFlowTarget = (FlowNode) process.getFlowElement(targetRef, true);
                if (sequenceFlowTarget != null && !visitedElements.contains(sequenceFlowTarget.getId())) {
                    boolean reachable = isReachable(process, sequenceFlowTarget, targetElement, visitedElements);
                    if (reachable) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected static boolean isInEventSubprocess(FlowNode flowNode) {
        FlowElementsContainer flowElementsContainer = flowNode.getParentContainer();
        while (flowElementsContainer != null) {
            if (flowElementsContainer instanceof EventSubProcess) {
                return true;
            }
            if (flowElementsContainer instanceof FlowElement) {
                flowElementsContainer = ((FlowElement) flowElementsContainer).getParentContainer();
            } else {
                flowElementsContainer = null;
            }
        }
        return false;
    }

    @Override
    public void findPreNodes(String taskId) {
        // 当前任务 task
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        // 获取所有节点信息，暂不考虑子流程情况
        Process process = repositoryService.getBpmnModel(task.getProcessDefinitionId()).getProcesses().get(0);
        Collection<FlowElement> flowElements = process.getFlowElements();
        // 获取当前任务节点元素
        UserTask source = (UserTask) process.getFlowElement(task.getTaskDefinitionKey(), true);
        // 获取节点的所有路线
        List<List<UserTask>> roads = ProcessUtils.findRoad(source, null, null, null);
        // 可回退的节点列表
        List<UserTask> userTaskList = new ArrayList<>();
        for (List<UserTask> road : roads) {
            if (userTaskList.size() == 0) {
                // 还没有可回退节点直接添加
                userTaskList = road;
            } else {
                // 如果已有回退节点，则比对取交集部分
                userTaskList.retainAll(road);
            }
        }
        System.out.println("--------------------- = -------------------------" );
        for (UserTask userTask : userTaskList) {
            System.out.println("userTask = " + userTask.getId()+","+userTask.getName());
        }
        System.out.println("--------------------- = -------------------------" );
    }
}
