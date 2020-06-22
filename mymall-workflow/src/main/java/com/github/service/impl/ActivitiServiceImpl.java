package com.github.service.impl;

import com.github.service.ActivitiService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: hjp
 * Date: 2020/6/22
 * Description:
 */
@Service
public class ActivitiServiceImpl implements ActivitiService {
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private HistoryService historyService;

}
