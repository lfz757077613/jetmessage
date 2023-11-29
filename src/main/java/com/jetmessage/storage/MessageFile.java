package com.jetmessage.storage;

import com.jetmessage.model.JetException;
import com.jetmessage.util.LibC;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import io.netty.util.internal.PlatformDependent;
import lombok.Getter;

import java.io.Closeable;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import static com.jetmessage.conf.Const.MESSAGE_FILE_BYTE_SIZE;
import static com.jetmessage.conf.Const.MESSAGE_DIR;
import static com.jetmessage.conf.Const.NATIVE_MESSAGE_FILE_BYTE_SIZE;

final class MessageFile implements Closeable {
    @Getter
    private final long initialOffset;
    @Getter
    private final String fileName;
    @Getter
    private final String filePath;
    private FileChannel fileChannel;
    private MappedByteBuffer mappedByteBuffer;
    private Pointer addressPointer;

    public MessageFile(String fileName) {
        this.initialOffset = Long.parseLong(fileName);
        this.fileName = fileName;
        this.filePath = MESSAGE_DIR + "/" + fileName;
    }

    public void init(boolean lock) {
        try {
            this.fileChannel = new RandomAccessFile(filePath, "rw").getChannel();
            this.mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, MESSAGE_FILE_BYTE_SIZE);
            this.addressPointer = new Pointer(PlatformDependent.directBufferAddress(mappedByteBuffer));
            if (lock) {
                mLock();
            }
        } catch (Exception e) {
            close();
            throw new JetException("MappedFile init error", e);
        }
    }

    @Override
    public void close() {
        try {
            if (mappedByteBuffer != null) {
                PlatformDependent.freeDirectBuffer(mappedByteBuffer);
            }
            if (fileChannel != null) {
                fileChannel.close();
            }
        } catch (Exception e) {
            throw new JetException("MappedFile close error", e);
        }
    }

    public byte[] read(int position, int length) {
        byte[] dst = new byte[length];
        ByteBuffer sliceBuffer = mappedByteBuffer.slice();
        sliceBuffer.position(position);
        sliceBuffer.get(dst);
        return dst;
    }

    public int readInt(int position) {
        return mappedByteBuffer.getInt(position);
    }

    public int write(ByteBuffer src) {
        int position = mappedByteBuffer.position();
        mappedByteBuffer.put(src);
        return position;
    }

    public void mLock() {
        if (Platform.isWindows()) {
            return;
        }
        if (LibC.INSTANCE.mlock(addressPointer, NATIVE_MESSAGE_FILE_BYTE_SIZE) != 0) {
            throw new JetException("mLock error " + fileName);
        }
    }

    public void mUnlock() {
        if (Platform.isWindows()) {
            return;
        }
        if (LibC.INSTANCE.munlock(addressPointer, NATIVE_MESSAGE_FILE_BYTE_SIZE) != 0) {
            throw new JetException("mUnlock error " + fileName);
        }
    }

    public int remaining() {
        return mappedByteBuffer.remaining();
    }
}
