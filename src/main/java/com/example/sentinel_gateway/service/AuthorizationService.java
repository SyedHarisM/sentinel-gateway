package com.example.sentinel_gateway.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import com.example.sentinel_gateway.properties.RbacProperties;
import com.example.sentinel_gateway.security.Role;

@Service
public class AuthorizationService {

    private RbacProperties rbacProperties;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public AuthorizationService(RbacProperties rbacProperties){
        this.rbacProperties = rbacProperties;
    }

    public boolean isAuthorized(Role role, String requestPath){

        if(!rbacProperties.isEnabled()){
            return true;
        }

        List<RbacProperties.Rule> rules = rbacProperties.getRules();

        for (RbacProperties.Rule rule : rules){
            if (pathMatcher.match(rule.getPattern(), requestPath)){
                return rule.getRoles().contains(role);
            }
        }

        return !"DENY".equalsIgnoreCase(rbacProperties.getDefaultAction());
    }

    
}
