package com.example.sentinel_gateway.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    private boolean enabled; //rate limit ko enable/disable
    private long maxRequests; 
    private long windowSeconds;


    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public long getMaxRequests() {
        return maxRequests;
    }
    public void setMaxRequests(long maxRequests) {
        this.maxRequests = maxRequests;
    }
    
    public long getWindowSeconds() {
        return windowSeconds;
    }
    public void setWindowSeconds(long windowSeconds) {
        this.windowSeconds = windowSeconds;
    }
}
