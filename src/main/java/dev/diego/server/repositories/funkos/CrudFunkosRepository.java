package dev.diego.server.repositories.funkos;

import dev.diego.common.models.Funkos;
import dev.diego.server.repositories.base.CrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public interface CrudFunkosRepository extends CrudRepository <Funkos> {
    /**
     * Metodo que busca por nombres que contengan el patrón indicado.
     * @return Funkos
     */
    Flux<Funkos> findByNombre(String nombre) throws SQLException;
    Flux<Funkos> insertAll(List<Funkos> list) throws SQLException;
    Mono<Funkos> findbyUUID(UUID uuid) throws SQLException;

}
