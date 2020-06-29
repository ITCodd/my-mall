package com.github.service;

/**
 * @author: hjp
 * Date: 2020/6/22
 * Description:
 */
public interface ActivitiService {

    public void backToPreNode(String taskId);


    public void addSign(String taskId,String variable,String assignee);

    public void addSign(String taskId,String assignee);

    public void multiSign(String taskId);

    public void multiSign(String taskId,boolean executionIsCompleted);

}
