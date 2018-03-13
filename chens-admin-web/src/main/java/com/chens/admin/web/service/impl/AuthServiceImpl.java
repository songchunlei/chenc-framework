package com.chens.admin.web.service.impl;

import com.chens.core.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.chens.admin.web.service.IAuthService;
import com.chens.admin.web.service.ISysRoleService;
import com.chens.admin.web.service.ISysUserService;
import com.chens.core.entity.SysUser;
import com.chens.core.exception.BaseException;
import com.chens.core.exception.BaseExceptionEnum;
import com.chens.core.vo.AuthRequest;

/**
 * 权限控制实现
 *
 * @auther songchunlei@qq.com
 * @create 2018/3/4
 */
@Service
public class AuthServiceImpl implements IAuthService{

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private ISysRoleService sysRoleService;

    @Override
    public SysUser findByUsernameAndPassword(AuthRequest authRequest) {
        SysUser sysUser = sysUserService.findByUsername(authRequest);
        sysUser.setRoles(sysRoleService.findRoleListByUserId(sysUser.getId()));
        return sysUser;
    }

    @Override
    public boolean Validate(AuthRequest authRequest) {

        if(authRequest==null)
        {
            throw new BaseException(BaseExceptionEnum.REQUEST_NULL);
        }

        if(StringUtils.isEmpty(authRequest.getUserName()))
        {
            throw new BaseException(BaseExceptionEnum.AUTH_REQUEST_NO_USERNAME);
        }

        if(StringUtils.isEmpty(authRequest.getPassword()))
        {
            throw new BaseException(BaseExceptionEnum.AUTH_REQUEST_NO_PASSWORD);
        }

        SysUser query = new SysUser();
        query.setUsername(authRequest.getUserName());
        query.setPassword(authRequest.getPassword());
        int count = sysUserService.selectCount(new EntityWrapper<>(query));
        if(count>0)
        {
            return true;
        }
        else
        {
            throw new BaseException(BaseExceptionEnum.AUTH_REQUEST_ERROR);
        }
    }
}
