package com.jetmessage.util;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;

public interface LibC extends Library {
    LibC INSTANCE = Native.load(Platform.isWindows() ? "msvcrt" : "c", LibC.class);

    int mlock(Pointer var1, NativeLong var2);

    int munlock(Pointer var1, NativeLong var2);
}
