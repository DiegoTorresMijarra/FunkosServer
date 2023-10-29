package dev.diego.services.database;

import dev.diego.models.Funkos;
import io.r2dbc.h2.H2ConnectionConfiguration;
import io.r2dbc.h2.H2ConnectionFactory;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

public class DataBaseManager {

    private static DataBaseManager instance;

    private final ConnectionFactory connectionFactory;

    private DataBaseManager(String propertiesFilePath) {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(propertiesFilePath)) {
            if (input == null) {
                throw new IOException("Imposible localizar " + propertiesFilePath);
            }
            properties.load(input);
        } catch (IOException ex) {
            throw new RuntimeException("Fallo al leer el fichero de propiedades desde " + propertiesFilePath, ex);
        }

        String url = properties.getProperty("database.url");
        String username = properties.getProperty("database.username");
        String password = properties.getProperty("database.password");

        H2ConnectionConfiguration configuration = H2ConnectionConfiguration.builder()
                .url(url)
                .username(username)
                .password(password)
                .build();
        this.connectionFactory = new H2ConnectionFactory(configuration);
    }

    public static synchronized DataBaseManager getInstance(String propertiesFilePath) {
        if (instance == null) {
            instance = new DataBaseManager(propertiesFilePath);
        }
        return instance;
    }

    public ConnectionFactory getConnectionFactory() {
        return this.connectionFactory;
    }
}

public class FunkoRepository {

    private final ConnectionFactory connectionFactory;

    public FunkoRepository(DataBaseManager databaseManager) {
        this.connectionFactory = databaseManager.getConnectionFactory();
    }

    public Flux<Funkos> getAllFunkos() {
        String sql = "SELECT * FROM funkos";
        return Flux.usingWhen(
                connectionFactory.create(),
                connection -> Flux.from(connection.createStatement(sql).execute())
                        .flatMap(result -> result.map((row, rowMetadata) -> new Funkos(
                                UUID.fromString(row.get("id", String.class)),
                                row.get("name", String.class),
                                row.get("price", Double.class)
                        ))),
                Connection::close
        );
    }

    public Mono<Funkos> getFunkoById(UUID id) {
        String sql = "SELECT * FROM funkos WHERE id = $1";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql)
                                .bind("$1", id.toString())
                                .execute())
                        .flatMap(result -> Mono.from(result.map((row, rowMetadata) -> new Funko(
                                UUID.fromString(row.get("id", String.class)),
                                row.get("name", String.class),
                                row.get("price", Double.class)
                        )))),
                Connection::close
        );
    }

     public Mono<Void> insertFunko(Funkos funko) {
        String sql = "INSERT INTO funkos (id, name, price) VALUES ($1, $2, $3)";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> connection.createStatement(sql)
                        .bind("$1", funko.getId().toString())
                        .bind("$2", funko.getName())
                        .bind("$3", funko.getPrice())
                        .execute()
                        .then(),
                Connection::close
        );
    }

    public Mono<Void> updateFunko(Funkos funko) {
        String sql = "UPDATE funkos SET name = $2, price = $3 WHERE id = $1";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> connection.createStatement(sql)
                        .bind("$1", funko.cod())
                        .bind("$2", funko.getName())
                        .bind("$3", funko.getPrice())
                        .execute()
                        .then(),
                Connection::close
        );
    }

    public Mono<Void> deleteFunko(UUID id) {
        String sql = "DELETE FROM funkos WHERE id = $1";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> connection.createStatement(sql)
                        .bind("$1", id.toString())
                        .execute()
                        .then(),
                Connection::close
        );
    }
}