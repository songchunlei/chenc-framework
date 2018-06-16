package com.chens.bpm.demo.listener;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

/**
 * 创建任务时就会跑
 * @author songchunlei@qq.com
 * @create 2018/1/6
 */
public class MyEndExecListener implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        System.out.println("end自定义监听");
    }
}
