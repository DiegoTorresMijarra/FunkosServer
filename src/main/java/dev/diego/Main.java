package dev.diego;

import dev.diego.controllers.ProcesadorCsv;
import dev.diego.models.Funkos;
import dev.diego.repositories.funkos.CrudFunkosRepositoryImpl;
import dev.diego.services.database.DataBaseManager;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.*;

public class Main {
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
        DataBaseManager dbManager = DataBaseManager.getInstance();

        // Ejecutar script SQL desde un archivo
        try {
            dbManager.initData("src/main/resources/init.sql",true);
        } catch (FileNotFoundException e) {
            System.out.println("aa");
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Insertando Datos");
        System.out.println("*********************");
        CrudFunkosRepositoryImpl base=new CrudFunkosRepositoryImpl(dbManager);

        try {
            base.insertAll(lista.get());
            base.findAll().forEach(System.out::println);
        } catch (InterruptedException | ExecutionException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
}