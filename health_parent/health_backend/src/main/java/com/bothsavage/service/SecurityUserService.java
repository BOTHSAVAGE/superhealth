package com.bothsavage.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.bothsavage.pojo.Permission;
import com.bothsavage.pojo.Role;
import com.bothsavage.pojo.User;
import com.bothsavage.controller.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * 这个服务是由安全框架去调用的
 */

@Component
@Slf4j
public class SecurityUserService implements UserDetailsService {
    //使用dubbo通过网络远程调用服务提供方获取数据库中的用户信息
    //去zookeeper的服务中心去查找服务
    @Reference
    private UserService userService;

    @Autowired
    EncodePasswordUtils encodePasswordUtils;

    //根据用户名查询数据库获取用户信息
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String a = encodePasswordUtils.encodePassword("admin");
        log.info(a);

        User user = userService.findByUsername(username);
        if(user == null){
            //用户名不存在
            return null;
        }

        List<GrantedAuthority> list = new ArrayList<>();

        //动态为当前用户授权
        Set<Role> roles = user.getRoles();
        for (Role role : roles) {
            //遍历角色集合，为用户授予角色
            list.add(new SimpleGrantedAuthority(role.getKeyword()));
            Set<Permission> permissions = role.getPermissions();
            for (Permission permission : permissions) {
                //遍历权限集合，为用户授权
                list.add(new SimpleGrantedAuthority(permission.getKeyword()));//这个权限是 用户包含的所有角色 角色包含的所有权限
            }
        }

        //返回的是UserDetails的实现类
        org.springframework.security.core.userdetails.User securityUser = new org.springframework.security.core.userdetails.User(username,user.getPassword(),list);
        return securityUser;
    }
}
