package com.chens.admin.remote;

import com.chens.admin.remote.hystrix.AuthClientHystrix;
import com.chens.core.entity.SysUser;
import com.chens.core.vo.AuthRequest;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 授权服务接口
 *
 * @auther songchunlei@qq.com
 * @create 2018/3/12
 */
@FeignClient(path = "authremote", value = "chens-admin-web",fallback = AuthClientHystrix.class)
public interface IAuthClient {

    @RequestMapping(value="/findByUserNameAndPassword")
    SysUser findByUserNameAndPassword(@RequestBody AuthRequest authRequest);

}
