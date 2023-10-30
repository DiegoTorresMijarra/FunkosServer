package dev.diego.server.repositories.funkos;

import dev.diego.common.enunms.Modelo;
import dev.diego.common.models.Funkos;
import dev.diego.common.utilities.IdGenerator;
import dev.diego.common.utilities.UUIDGenerator;
import dev.diego.server.services.database.DataBaseManager;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Result;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
public class CrudFunkosRepositoryImpl implements CrudFunkosRepository {

    private static CrudFunkosRepositoryImpl instance;
    private final ConnectionPool connectionFactory;
    private CrudFunkosRepositoryImpl(DataBaseManager db){
        connectionFactory = db.getConnectionPool();
    }
    public static CrudFunkosRepositoryImpl getInstance(DataBaseManager db){
        if(instance == null){
            instance = new CrudFunkosRepositoryImpl(db);
        }
        return instance;
    }
    @Override
    public Flux<Funkos> findAll() throws SQLException {
        log.debug("Obteniendo todos los funkos");
        String sql="SELECT * FROM FUNKOS";
        return Flux.usingWhen(
                connectionFactory.create(),
                connection -> Flux.from(connection.createStatement(sql).execute()).flatMap(
                        result -> result.map((row, metadata) ->
                            Funkos.builder()
                                .cod(UUIDGenerator.generate(row.get("cod", String.class)))
                                .nombre(row.get("nombre", String.class))
                                .precio(row.get("precio", Double.class))
                                .modelo(Modelo.valueOf(row.get("modelo", String.class)))
                                .fechaLanzamiento(LocalDate.parse(row.get("fecha_lanzamiento", String.class)))
                                .created_at(row.get("created_at", LocalDateTime.class))
                                .build())
                ),
                Connection::close
        );
    }
    @Override
    public Mono<Funkos> findById(Integer id) throws SQLException {
        log.debug("Obteniendo funko por id "+ id);
        String sql="SELECT * FROM FUNKOS WHERE ID=?";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql).bind(0,id).execute())
                        .flatMap(result -> Mono.from(result.map((row, metadata) -> Funkos.builder()
                                    .cod(UUIDGenerator.generate(row.get("cod", String.class)))
                                    .nombre(row.get("nombre", String.class))
                                    .precio(row.get("precio", Double.class))
                                    .modelo(Modelo.valueOf(row.get("modelo", String.class)))
                                    .fechaLanzamiento(LocalDate.parse(row.get("fecha_lanzamiento", String.class)))
                                    .created_at(row.get("created_at", LocalDateTime.class))
                                    .build())
                        )
                ),
                Connection::close
        );
    }

    @Override
    public Mono<Funkos> insert(Funkos entity) throws SQLException {
        log.debug("Insertando funko "+entity.getNombre());
        String sql = "INSERT INTO funkos VALUES (null, ?, ?, ?, ?, ?, ?, ?, ?)";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql)
                                .bind(0, entity.getCod())
                                .bind(1, IdGenerator.getAndIncrement())
                                .bind(2, entity.getNombre())
                                .bind(3, entity.getModelo().toString())
                                .bind(4, entity.getPrecio())
                                .bind(5, entity.getFechaLanzamiento())
                                .bind(6, LocalDate.now())
                                .bind(7, LocalDate.now())
                            .execute()
                        ).then(Mono.just(entity)),
                Connection::close
        );
    }
    @Override
    public Mono<Funkos> update(Funkos entity) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Mono<Funkos> delete(Long id) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Flux<Funkos> findByNombre(String nombre) throws SQLException {
        log.debug("Obteniendo funkos por el nombre "+nombre);
        String sql="SELECT * FROM FUNKOS WHERE nombre=?";
        return Flux.usingWhen(
                connectionFactory.create(),
                connection -> Flux.from(connection.createStatement(sql).bind(0,nombre)
                        .execute()).flatMap(
                        result -> result.map((row, metadata) ->
                                Funkos.builder()
                                        .cod(UUIDGenerator.generate(row.get("cod", String.class)))
                                        .nombre(row.get("nombre", String.class))
                                        .precio(row.get("precio", Double.class))
                                        .modelo(Modelo.valueOf(row.get("modelo", String.class)))
                                        .fechaLanzamiento(LocalDate.parse(row.get("fecha_lanzamiento", String.class)))
                                        .created_at(row.get("created_at", LocalDateTime.class))
                                        .build())
                ),
                Connection::close
        );
    }


    @Override
    public Flux<Funkos> insertAll(List<Funkos> list) throws SQLException {
        return Flux.fromIterable(list).flatMap(entity ->{
            try{
                return insert(entity);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public Mono<Funkos> findbyUUID(UUID uuid) throws SQLException {
        log.debug("Obteniendo funko por UUID "+ uuid);
        String sql="SELECT * FROM FUNKOS WHERE cod=?";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql).bind(0,uuid).execute())
                        .flatMap(result -> Mono.from(result.map((row, metadata) -> Funkos.builder()
                                        .cod(UUIDGenerator.generate(row.get("cod", String.class)))
                                        .nombre(row.get("nombre", String.class))
                                        .precio(row.get("precio", Double.class))
                                        .modelo(Modelo.valueOf(row.get("modelo", String.class)))
                                        .fechaLanzamiento(LocalDate.parse(row.get("fecha_lanzamiento", String.class)))
                                        .created_at(row.get("created_at", LocalDateTime.class))
                                        .build())
                                )
                        ),
                Connection::close
        );
    }
}
