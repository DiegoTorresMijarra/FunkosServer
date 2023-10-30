package dev.diego.server.services.database;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.Statement;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.io.*;
import java.time.Duration;
import java.util.Properties;
import java.util.stream.Collectors;
@Slf4j
public class DataBaseManager {
    private static DataBaseManager instance;
    private final ConnectionFactory connectionFactory;
    private final ConnectionPool pool;
    private String databaseUsername;
    private String databasePassword;
    private boolean databaseInitTables;
    private String databaseUrl;

    // Constructor privado para que no se pueda instanciar Singleton
    private DataBaseManager() {
        loadProperties();
        connectionFactory = ConnectionFactories.get(databaseUrl);

        // Configuramos el pool de conexiones
        ConnectionPoolConfiguration configuration = ConnectionPoolConfiguration
                .builder(connectionFactory)
                .maxIdleTime(Duration.ofMillis(1000)) // Tiempo máximo de espera
                .maxSize(20) // Tamaño máximo del pool
                .build();

        pool = new ConnectionPool(configuration);

        // Por si hay que inicializar las tablas
        if (databaseInitTables) {
            initTables();
        }
    }

    /**
     * Método para obtener la instancia de la base de datos
     * Lo ideal e
     *
     * @return
     */
    public static synchronized DataBaseManager getInstance() {
        if (instance == null) {
            instance = new DataBaseManager();
        }
        return instance;
    }

    private synchronized void loadProperties() {
        log.debug("Cargando fichero de configuración de la base de datos");
        try {
            var file = ClassLoader.getSystemResource("database.properties").getFile();
            var props = new Properties();
            props.load(new FileReader(file));
            // Establecemos la url de la base de datos
            databaseUrl = props.getProperty("database.url", "r2dbc:h2:mem:///test?options=DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
            databaseUsername = props.getProperty("database.username", "diego");
            databasePassword = props.getProperty("database.password", "password");
            databaseInitTables = Boolean.parseBoolean(props.getProperty("database.initTables", "false"));

            log.debug("La url de la base de datos es: " + databaseUrl);
        } catch (IOException e) {
            log.error("Error al leer el fichero de configuración de la base de datos " + e.getMessage());
        }
    }


    /**
     * Método para inicializar la base de datos y las tablas
     * Esto puede ser muy complejo y mejor usar un script, ademas podemos usar datos de ejemplo en el script
     */
    public synchronized void initTables() {
        // Debes hacer un script por accion
        log.debug("Borrando tablas de la base de datos");
        executeScript("remove.sql").block(); // Bloqueamos hasta que se ejecute (no nos interesa seguir hasta que se ejecute)
        log.debug("Inicializando tablas de la base de datos");
        executeScript("init.sql").block(); // Bloqueamos hasta que se ejecute (no nos interesa seguir hasta que se ejecute)
        log.debug("Tabla de la base de datos inicializada");
    }


    public Mono<Void> executeScript(String scriptSqlFile) {
        log.debug("Ejecutando script de inicialización de la base de datos: " + scriptSqlFile);
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> {
                    log.debug("Creando conexión con la base de datos");
                    String scriptContent = null;
                    try {
                        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(scriptSqlFile)) {
                            if (inputStream == null) {
                                return Mono.error(new IOException("No se ha encontrado el fichero de script de inicialización de la base de datos"));
                            } else {
                                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                                    scriptContent = reader.lines().collect(Collectors.joining("\n"));
                                }
                            }
                        }
                        // logger.debug(scriptContent);
                        Statement statement = connection.createStatement(scriptContent);
                        return Mono.from(statement.execute());
                    } catch (IOException e) {
                        return Mono.error(e);
                    }
                },
                Connection::close
        ).then();
    }

   /* public ConnectionFactory getConnectionFactory() {
        return this.connectionFactory;
    }*/

    public ConnectionPool getConnectionPool() {
        return this.pool;
    }
}
