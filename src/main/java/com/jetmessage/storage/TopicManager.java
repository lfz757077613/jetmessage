package com.jetmessage.storage;

import java.util.Optional;

public interface TopicManager {
    Optional<TopicInfo> topicInfo(String topic);
}
