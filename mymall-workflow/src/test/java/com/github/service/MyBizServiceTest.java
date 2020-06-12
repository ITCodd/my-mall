package com.github.service;

import com.github.WorkflowApplication;
import com.github.model.MyBiz;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={WorkflowApplication.class})
public class MyBizServiceTest {

    @Autowired
    private MyBizService bizService;

    @Test
    public void t1() {
        List<MyBiz> list = bizService.list();
        for (MyBiz myBiz : list) {
            System.out.println("myBiz = " + myBiz);
        }
    }
}