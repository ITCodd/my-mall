package com.github.service.impl;

import com.github.service.ProcessService;
import com.github.utils.ProcessUtils;
import com.github.utils.ResultData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.UserTask;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.impl.DefaultProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
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
        /**
         * 获得当前活动的节点
         */
        String processDefinitionId = "";
        if (isFinished(processId)) {// 如果流程已经结束，则得到结束节点
            HistoricProcessInstance pi = historyService.createHistoricProcessInstanceQuery().processInstanceId(processId).singleResult();

            processDefinitionId=pi.getProcessDefinitionId();
        } else {// 如果流程没有结束，则取当前活动节点
            // 根据流程实例ID获得当前处于活动状态的ActivityId合集
            ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();
            processDefinitionId=pi.getProcessDefinitionId();
        }
        List<String> highLightedActivitis = new ArrayList<String>();

        /**
         * 获得活动的节点
         */
        List<HistoricActivityInstance> highLightedActivitList =  historyService.createHistoricActivityInstanceQuery().processInstanceId(processId).orderByHistoricActivityInstanceStartTime().asc().list();

        for(HistoricActivityInstance tempActivity : highLightedActivitList){
            String activityId = tempActivity.getActivityId();
            highLightedActivitis.add(activityId);
        }

        List<String> flows = new ArrayList<>();
        //获取流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        DefaultProcessDiagramGenerator diagramGenerator = new  DefaultProcessDiagramGenerator();
        InputStream in = diagramGenerator.generateDiagram(bpmnModel, "png", highLightedActivitis, flows, "宋体",
                "宋体", "宋体", null, 1.0, true);
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
        // 获取全部节点列表，包含子节点
        Collection<FlowElement> allElements = ProcessUtils.getAllElements(process.getFlowElements(), null);
        // 获取当前任务节点元素
        FlowElement source = null;
        if (allElements != null) {
            for (FlowElement flowElement : allElements) {
                // 类型为用户节点
                if (flowElement.getId().equals(task.getTaskDefinitionKey())) {
                    // 获取节点信息
                    source = flowElement;
                    break;
                }
            }
        }


        // 目的获取所有跳转到的节点 targetIds
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
        List<String> lastHistoricTaskInstanceList = ProcessUtils.historicTaskInstanceClean(allElements, historicTaskInstanceList);
        // 此时历史任务实例为倒序，获取最后走的节点
        List<String> targetIds = new ArrayList<>();
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
                targetIds.add(historicTaskInstanceKey);
            }
        }


        // 目的获取所有需要被跳转的节点 currentIds
        // 取其中一个父级任务，因为后续要么存在公共网关，要么就是串行公共线路
        UserTask oneUserTask = parentUserTaskList.get(0);
        // 获取所有正常进行的任务节点 Key，这些任务不能直接使用，需要找出其中需要撤回的任务
        List<Task> runTaskList = taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).list();
        List<String> runTaskKeyList = new ArrayList<>();
        runTaskList.forEach(item -> runTaskKeyList.add(item.getTaskDefinitionKey()));
        // 需驳回任务列表
        List<String> currentIds = new ArrayList<>();
        // 通过父级网关的出口连线，结合 runTaskList 比对，获取需要撤回的任务
        List<UserTask> currentUserTaskList = ProcessUtils.iteratorFindChildUserTasks(oneUserTask, runTaskKeyList, null, null);
        currentUserTaskList.forEach(item -> currentIds.add(item.getId()));


        // 规定：并行网关之前节点必须需存在唯一用户任务节点，如果出现多个任务节点，则并行网关节点默认为结束节点，原因为不考虑多对多情况
        if (targetIds.size() > 1 && currentIds.size() > 1) {
            Set<String> currentActivityIds = new HashSet<>(currentIds);
            if(currentActivityIds.size()==1){
                //排他网关分支进入的情况
                List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().processInstanceId(task.getProcessInstanceId())
                        .taskDefinitionKeys(targetIds).desc().list();
                HistoricTaskInstance historicTaskInstance = list.get(0);
                String taskDefinitionKey = historicTaskInstance.getTaskDefinitionKey();
            }else{
                return ResultData.fail("任务出现多对多情况，无法撤回");
            }

        }

        // 循环获取那些需要被撤回的节点的ID，用来设置驳回原因
        List<String> currentTaskIds = new ArrayList<>();
        currentIds.forEach(currentId -> runTaskList.forEach(runTask -> {
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
            if (targetIds.size() == 100) {
                // 1 对 多任务跳转，currentIds 当前节点(1)，targetIds 跳转到的节点(多)
                runtimeService.createChangeActivityStateBuilder().processInstanceId(task.getProcessInstanceId()).moveSingleActivityIdToActivityIds(currentIds.iterator().next(), targetIds).changeState();
            }
            // 如果父级任务只有一个，因此当前任务可能为网关中的任务
            if (targetIds.size() == 1) {
                // 1 对 1 或 多 对 1 情况，currentIds 当前要跳转的节点列表(1或多)，targetIds.get(0) 跳转到的节点(1)
                List<String> currentActivityIds = new ArrayList<>(currentIds);
                runtimeService.createChangeActivityStateBuilder().processInstanceId(task.getProcessInstanceId()).moveActivityIdsToSingleActivityId(currentActivityIds, targetIds.get(0)).changeState();
            }
        } catch (FlowableObjectNotFoundException e) {
            return ResultData.fail("未找到流程实例，流程可能已发生变化");
        } catch (FlowableException e) {
            return ResultData.fail("无法取消或开始活动");
        }
        return ResultData.success();
    }
}
