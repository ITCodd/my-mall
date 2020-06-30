package com.github.service;

public interface ProcessService {
    /**
     * 加签
     * @param taskId
     */
    void addMultiInstance(String taskId,String assignee);

    void deleteMultiInstance(String taskId);
}
