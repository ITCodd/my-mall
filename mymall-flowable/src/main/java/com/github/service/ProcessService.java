package com.github.service;

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
}
