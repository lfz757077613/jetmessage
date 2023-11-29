package com.jetmessage.conf;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "static-config")
public class StaticConfig {
    private int maxMessageBytes;
    private int maxLockedFileCount;
    private int messageFileServiceThreadCount;
    private double maxDiskUsedRatio;
}
