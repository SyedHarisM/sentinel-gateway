package com.example.sentinel_gateway.properties;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.example.sentinel_gateway.security.Role;

@ConfigurationProperties(prefix = "rbac")
public class RbacProperties {

    /**
     * Enables or disables RBAC.
     */
    private boolean enabled;

    /**
     * Default action when no rule matches.
     * Recommended values:
     * ALLOW
     * DENY
     */
    private String defaultAction = "DENY";

    /**
     * RBAC rules loaded from application.yml
     */
    private List<Rule> rules = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDefaultAction() {
        return defaultAction;
    }

    public void setDefaultAction(String defaultAction) {
        this.defaultAction = defaultAction;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    /**
     * Represents one RBAC rule.
     */
    public static class Rule {

        /**
         * Endpoint pattern.
         * Example:
         * /admin/**
         */
        private String pattern;

        /**
         * Allowed roles.
         */
        private List<Role> roles = new ArrayList<>();

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        public List<Role> getRoles() {
            return roles;
        }

        public void setRoles(List<Role> roles) {
            this.roles = roles;
        }
    }
}