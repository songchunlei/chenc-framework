package com.chens.admin.web.rpc;

import com.chens.admin.remote.ISysLogClient;
import com.chens.admin.service.ISysLogService;
import com.chens.core.entity.SysLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 日志服务
 *
 * @auther songchunlei@qq.com
 * @create 2018/3/24
 */
@RestController
@RequestMapping(value="/sysLogRpc")
public class SysLogRpc implements ISysLogClient {

    @Autowired
    private ISysLogService sysLogService;

    @Override
    public boolean create(@RequestBody SysLog sysLog) {
        return sysLogService.insert(sysLog);
    }
}
