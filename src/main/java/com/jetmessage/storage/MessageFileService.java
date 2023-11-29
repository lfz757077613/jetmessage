package com.jetmessage.storage;

import com.jetmessage.conf.Const;
import com.jetmessage.conf.StaticConfig;
import com.jetmessage.model.JetException;
import com.jetmessage.util.CommonRunnable;
import io.netty.util.internal.PlatformDependent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MessageFileService {
    private static final File MESSAGE_DIR = new File(Const.MESSAGE_DIR);
    private static final int NAME_LENGTH = String.valueOf(Long.MAX_VALUE).length();

    @Resource
    private StaticConfig config;
    @Resource
    private TopicManager topicManager;

    private volatile boolean stopped;
    private final ConcurrentSkipListMap<Long, MessageFile> messageFileMap = new ConcurrentSkipListMap<>();
    private ByteBuffer messageTmpDirectBuffer;
    private ScheduledThreadPoolExecutor scheduledExecutor;
    private ThreadPoolExecutor executorService;
    private Future<MessageFile> addMessageFileFuture;

    @PostConstruct
    private void init() throws IOException {
        log.info("MessageFileService init ...");
        Files.createDirectories(MESSAGE_DIR.toPath());
        File[] files = MESSAGE_DIR.listFiles();
        if (files == null) {
            throw new JetException("listFiles null");
        }
        messageTmpDirectBuffer = ByteBuffer.allocateDirect(config.getMaxMessageBytes());
        List<File> messageFileList = Arrays.stream(files)
                .sorted(Comparator.comparing(File::getName))
                .collect(Collectors.toList());
        for (int i = 0; i < messageFileList.size(); i++) {
            File file = messageFileList.get(i);
            MessageFile messageFile = new MessageFile(file.getName());
            messageFile.init(i < config.getMaxLockedFileCount());
            messageFileMap.put(Long.valueOf(file.getName()), messageFile);
        }
        if (messageFileMap.isEmpty()) {
            MessageFile messageFile = new MessageFile(StringUtils.leftPad("0", NAME_LENGTH, '0'));
            messageFile.init(true);
            messageFileMap.put(0L, messageFile);
        }
        scheduledExecutor = new ScheduledThreadPoolExecutor(1, new CustomizableThreadFactory(getClass().getSimpleName()));
        scheduledExecutor.execute(new CommonRunnable(this::cleanOldMessage));
        executorService = new ThreadPoolExecutor(
                config.getMessageFileServiceThreadCount(),
                config.getMessageFileServiceThreadCount(),
                0, TimeUnit.MILLISECONDS,
                new SynchronousQueue<>(),
                new CustomizableThreadFactory(getClass().getSimpleName()),
                new ThreadPoolExecutor.CallerRunsPolicy());
        log.info("MessageFileService init finish");
    }

    @PreDestroy
    private void destroy() throws InterruptedException {
        log.info("MessageFileService shutdown ...");
        stopped = true;
        executorService.shutdown();
        while (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
            log.info("MessageFileService executorService await shutdown");
        }
        scheduledExecutor.shutdown();
        while (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
            log.info("MessageFileService scheduledExecutor await shutdown");
        }
        for (MessageFile messageFile : messageFileMap.values()) {
            messageFile.close();
        }
        PlatformDependent.freeDirectBuffer(messageTmpDirectBuffer);
        log.info("MessageFileService shutdown finish");
    }

    /**
     * https://stackoverflow.com/questions/37560121/why-using-getfreespace-gettotalspace-getusablespace-gives-different-output-fr
     * |------------- free ----------|
     * |-------usable------|----used-----|
     * |-reserve-|
     * |xxxxxxxxx|+++++++++++++++++++|=============|
     * |---------------- total --------------------|
     */
    public void cleanOldMessage() {
        try {
            long totalSpace = MESSAGE_DIR.getTotalSpace();
            long freeSpace = MESSAGE_DIR.getFreeSpace();
            long usableSpace = MESSAGE_DIR.getUsableSpace();
            double diskUsedRatio = (0.0 + totalSpace - freeSpace) / (totalSpace - freeSpace + usableSpace);
            if (diskUsedRatio > config.getMaxDiskUsedRatio()) {

            }
        } catch (Throwable t) {
            log.error("cleanOldMessageFile error", t);
        }
    }

    public synchronized void writeMessage(String topic) {
        Optional<TopicInfo> topicInfoOptional = topicManager.topicInfo(topic);
        if (!topicInfoOptional.isPresent()) {
            return;
        }
        TopicInfo topicInfo = topicInfoOptional.get();
        messageTmpDirectBuffer.clear();
//        messageTmpBuffer.put();长度
//        messageTmpBuffer.put();crc
//        messageTmpBuffer.put();内容
        messageTmpDirectBuffer.flip();
        MessageFile lastMessageFile = messageFileMap.lastEntry().getValue();
        if (lastMessageFile.remaining() >= messageTmpDirectBuffer.remaining()) {
            lastMessageFile.write(messageTmpDirectBuffer);
            return;
        }
//        lastMessageFile.write()
        if (addMessageFileFuture == null) {

        }
    }


}
