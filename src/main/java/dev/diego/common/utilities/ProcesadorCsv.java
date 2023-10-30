package dev.diego.common.utilities;

import dev.diego.common.enunms.Modelo;
import dev.diego.common.models.Funkos;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class ProcesadorCsv implements Callable<List<Funkos>> {
    /**
     * instancia Procesador
     */
    private static ProcesadorCsv instance;

    /**
     * path a la carpeta foco del procesador
     */
    private static final String DATA_PATH = Paths.get("").toAbsolutePath()+ File.separator + "data";
    /**
     * nombre del archivo csv creado
     */
    private static final String CSV_FILE="funkos.csv";

    /**
     * devuelve la instancia del procesador, si esta es null la inicia
     * @return ProcesadorCsv
     */
    public static ProcesadorCsv getInstance() {
        if (instance == null) {
            instance = new ProcesadorCsv();
        }
        return instance;
    }
    @Override
    public List<Funkos> call() throws Exception {
        String archivoCsv=DATA_PATH+File.separator+CSV_FILE;
        File f=new File(archivoCsv);

        List<Funkos> res=new ArrayList<>();

        ExecutorService executorService= Executors.newFixedThreadPool(5);

        if (f.exists())
            log.info("Leyendo el archivo csv");

        try(BufferedReader fr= new BufferedReader(new FileReader(f))){
            fr.readLine();//leer linea encabezados
            while(fr.ready()){
                String[] procesar =fr.readLine().split(",");
                res.add(Funkos.builder()
                        .cod(UUIDGenerator.generate(procesar[0]))
                        .nombre(procesar[1])//si diera NullPointerException la lanza y la procesaremos despues
                        .modelo(Modelo.valueOf(procesar[2]))
                        .precio(Double.parseDouble(procesar[3]))
                        .fechaLanzamiento(LocalDate.parse(procesar[4], DateTimeFormatter.ISO_LOCAL_DATE))
                        .build());
            }
        } catch (IOException e) {
            log.error("No se ha podido crear la lista de funkos "+e.getMessage());
            throw new RuntimeException("No se ha podido crear la lista de funkos "+e.getMessage());
        }
        return res;
    }
}
