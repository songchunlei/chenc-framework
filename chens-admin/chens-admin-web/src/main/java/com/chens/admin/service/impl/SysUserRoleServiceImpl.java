package com.chens.admin.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.chens.admin.entity.SysUserRole;
import com.chens.admin.mapper.SysUserRoleMapper;
import com.chens.admin.service.ISysUserRoleService;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.chens.admin.vo.QueryRolesByUserId;
import com.chens.core.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 *  服务实现类
 *
 * @author chunlei.song@live.com
 * @create 2018-03-17
 */
@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements ISysUserRoleService {

    private final String DEFAULT_USERS_SPLIT_FLG=",";
    private final String DEFAULT_ROLES_SPLIT_FLG=",";


    @Transactional
    @Override
    public boolean saveUserRoleListByUserId(QueryRolesByUserId queryRolesByUserId) {
        List<SysUserRole> sysUserRoles = new ArrayList<>();
        String sysRoles = queryRolesByUserId.getSysRoles();
        if (StringUtils.isNotEmpty(sysRoles)) {
            String[] checkedStr = sysRoles.split(DEFAULT_ROLES_SPLIT_FLG);
            for (String s : checkedStr) {
                if(StringUtils.isNotEmpty(s)){
                    sysUserRoles.add(new SysUserRole(queryRolesByUserId.getUserId(),s));
                }
            }
            return this.insertBatch(sysUserRoles);
        }

        return false;
    }

    @Transactional
    @Override
    public boolean AddUsersInRole(String roleId, String userIds) {
        boolean  flagUserRole = false;
        if (StringUtils.isNotEmpty(userIds)) {
            List<SysUserRole> userRoleList = new ArrayList<>();
            String[] checkedStr = userIds.split(DEFAULT_USERS_SPLIT_FLG);
            for (String s : checkedStr) {
                SysUserRole r = new SysUserRole(s,roleId);
                userRoleList.add(r);
            }
            // 将选中角色id进行保存处理
            flagUserRole = this.insertBatch(userRoleList);
        }
        return flagUserRole;
    }

    @Transactional
    @Override
    public boolean DeleteUsersInRole(String roleId, String userIds) {
        if (StringUtils.isNotEmpty(userIds)) {
            String[] checkedStr = userIds.split(DEFAULT_USERS_SPLIT_FLG);
            for (String s : checkedStr) {
                SysUserRole r = new SysUserRole(s,roleId);
                this.delete(new EntityWrapper<>(r));
            }
        }
        return true;
    }
}
