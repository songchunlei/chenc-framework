package com.chens.admin.mapper;

import com.baomidou.mybatisplus.annotations.SqlParser;
import com.chens.core.entity.SysUser;
import com.baomidou.mybatisplus.mapper.BaseMapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author chunlei.song@live.com123
 * @since 2018-03-04
 */
//取消租户限制
@SqlParser(filter = true)
public interface SysUserMapper extends BaseMapper<SysUser> {
    /**通过username查找用户信息;*/
    SysUser findByUsername(String username);
}