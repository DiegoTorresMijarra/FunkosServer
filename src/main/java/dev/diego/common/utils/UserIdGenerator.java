package dev.diego.common.utils;

import java.util.concurrent.atomic.AtomicInteger;

public class UserIdGenerator {
    private static AtomicInteger id=new AtomicInteger(0);

    public static int getAndIncrement(){
        return id.getAndIncrement();
    }
}
