package dev.diego.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class UUIDGenerator {
    public static UUID generate(String uuid) {
        try{
            return UUID.fromString(uuid);// si nos da error crea uno automaticamente
        }catch (IllegalArgumentException e){
            log.error("No se ha podido generar un UUID "+uuid);
            return UUID.randomUUID();
        }
    }
}
