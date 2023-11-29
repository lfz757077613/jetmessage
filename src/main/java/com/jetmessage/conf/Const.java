package com.jetmessage.conf;

import com.jetmessage.Application;
import com.sun.jna.NativeLong;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;

public final class Const {
    public static final String MDC_TRACE_ID = "trace";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss:SSS";
    public static final DataSize MESSAGE_FILE_SIZE = DataSize.of(1, DataUnit.GIGABYTES);
    public static final long MESSAGE_FILE_BYTE_SIZE = MESSAGE_FILE_SIZE.toBytes();
    public static final NativeLong NATIVE_MESSAGE_FILE_BYTE_SIZE = new NativeLong(MESSAGE_FILE_BYTE_SIZE);
    public static final String STORAGE_DIR = Application.applicationPath() + "/jetmessage";
    public static final String MESSAGE_DIR = STORAGE_DIR + "/message";
    public static final int END_FLAG = Integer.MAX_VALUE;
}
