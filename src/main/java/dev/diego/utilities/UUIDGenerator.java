package dev.diego.utilities;

import java.util.UUID;

public class UUIDGenerator {
    public static UUID generate(String uuid) {
        try{
            return UUID.fromString(uuid);// si nos da error crea uno automaticamente
        }catch (IllegalArgumentException e){
            return UUID.randomUUID();
        }
    }
}
