package com.github.service.impl;

import com.github.service.ProcessService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;

import java.util.Collections;


@Service
public class ProcessServiceImpl implements ProcessService {

    @Autowired
    private TaskService taskService;

    @Autowired
    private RuntimeService runtimeService;

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
}
