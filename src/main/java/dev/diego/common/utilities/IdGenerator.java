package dev.diego.common.utilities;

import java.util.concurrent.atomic.AtomicInteger;

public class IdGenerator {
    private static AtomicInteger id=new AtomicInteger(0);

    public static int getAndIncrement(){
        return id.getAndIncrement();
    }
}
