package com.github.listener;

import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component("signTaskListener")
public class SignTaskListener implements ExecutionListener {

    @Override
    public void notify(DelegateExecution delegateExecution) {
        log.info("任务监听类开始执行");
        FlowElement currentFlowElement = delegateExecution.getCurrentFlowElement();
        if(currentFlowElement instanceof UserTask){
            UserTask userTask = (UserTask) currentFlowElement;
            List<String> candidateUsers = userTask.getCandidateUsers();
            //设为本地变量(节点所有)
            delegateExecution.setVariableLocal("assigneeList",candidateUsers);
        }
    }
}
