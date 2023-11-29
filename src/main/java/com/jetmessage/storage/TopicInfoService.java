package com.jetmessage.storage;

import java.util.Optional;

public interface TopicInfoService {
    void saveTopic(TopicInfo topicInfo);
    void deleteTopic(TopicInfo topicInfo);
    TopicInfo updateTopic(TopicInfo topicInfo);
    Optional<TopicInfo> topicInfo(String topic);
}
