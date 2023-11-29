package com.jetmessage.storage;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.util.concurrent.MoreExecutors;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Function;

@Service
public class DefaultTopicInfoService implements TopicInfoService {
    private Cache<String, TopicInfo> cache = Caffeine.newBuilder()
            .executor(MoreExecutors.directExecutor())
            .expireAfterAccess(Duration.ofHours(2))
            .build();

    @Override
    public synchronized void saveTopic(TopicInfo topicInfo) {
    }

    @Override
    public void deleteTopic(TopicInfo topicInfo) {

    }

    @Override
    public TopicInfo updateTopic(TopicInfo topicInfo) {
        return null;
    }

    @Override
    public Optional<TopicInfo> topicInfo(String topic) {
        return Optional.ofNullable(cache.get(topic, ignore -> null));
    }
}
