package com.github.service.impl;

import com.github.service.ProcessService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.impl.DefaultProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
}
