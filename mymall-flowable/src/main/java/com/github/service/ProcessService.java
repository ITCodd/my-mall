package com.github.service;

import com.github.utils.ResultData;

import java.util.List;

public interface ProcessService {
    /**
     * 加签
     * @param taskId
     */
    void addMultiInstance(String taskId,String assignee);

    void deleteMultiInstance(String taskId);

    void genProcessDiagram(String processId);

    void move(String proInstId, String nodeId, String toNodeId);

    void moveToParentProcess(String proInstId, String subNodeId, String parentNodeId);

    void moveToSubProcess(String proInstId, String subProcess,String subNodeId,String parentNodeId);

    void moveNodeIdsToSingle(String proInstId, List<String> nodeIds, String toNodeId);

    void moveSingleToNodeIds(String proInstId, String nodeId, List<String> toNodeIds);

    ResultData moveToPre(String taskId, String comment);
}
