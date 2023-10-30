package dev.diego.server;

import dev.diego.common.models.Funkos;
import dev.diego.common.utils.ProcesadorCsv;
import dev.diego.server.repositories.funkos.CrudFunkosRepositoryImpl;
import dev.diego.server.services.database.DataBaseManager;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
public class Server {
    public static void main(String[] args) {
        ProcesadorCsv pCsv=ProcesadorCsv.getInstance();
        ExecutorService ex= Executors.newFixedThreadPool(5);
        Future<List<Funkos>> lista;
        try {
            lista=ex.submit(pCsv);
            lista.get().forEach(System.out::println);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        ex.shutdown();
        CrudFunkosRepositoryImpl base=CrudFunkosRepositoryImpl.getInstance(DataBaseManager.getInstance());

        System.out.println("Insertando Datos");
        System.out.println("*********************");

        try {
            Flux<Funkos> funkosFlux=base.insertAll(lista.get());
            log.info("Se han insertado "+ funkosFlux.count().block());
        } catch (InterruptedException | ExecutionException | SQLException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

        try(ServerSocket serverSocket = new ServerSocket(3000)) {
            log.info("ServerSocket iniciado: Escuchando en el puerto 3000");
            while (true) {
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
