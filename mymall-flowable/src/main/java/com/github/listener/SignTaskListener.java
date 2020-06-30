package com.github.listener;

import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.spring.integration.Flowable;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component("signTaskListener")
public class SignTaskListener implements JavaDelegate {
    @Override
    public void execute(DelegateExecution delegateExecution) {
//        delegateExecution.setVariable("assigneeList", ((UserTask) delegateExecution.getCurrentFlowElement()).getCandidateUsers());
        FlowElement currentFlowElement = delegateExecution.getCurrentFlowElement();
        if(currentFlowElement instanceof UserTask){
            UserTask userTask = (UserTask) currentFlowElement;
            List<String> candidateUsers = userTask.getCandidateUsers();
            delegateExecution.setVariable("assigneeList",candidateUsers);
        }
    }


}
