package com.github.pojo;

import lombok.Data;
import org.activiti.bpmn.model.Task;

/**
 * @author: hjp
 * Date: 2020/6/23
 * Description:
 */
@Data
public class FlowTaskNode {
    private String nodeId;
    private String nodeName;
    private Task task;

    public FlowTaskNode(String nodeId, String nodeName, Task task) {
        this.nodeId = nodeId;
        this.nodeName = nodeName;
        this.task = task;
    }
}
