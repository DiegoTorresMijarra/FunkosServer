package dev.diego.repositories.funkos;

import dev.diego.models.Funkos;
import dev.diego.repositories.base.CrudRepository;

import java.sql.SQLException;
import java.util.List;

public interface CrudFunkosRepository extends CrudRepository <Funkos> {
    /**
     * Metodo que busca por nombres que contengan el patrón indicado.
     * @return Funkos
     */
    Funkos findByNombre() throws SQLException;
    List<Funkos>insertAll(List<Funkos> list) throws SQLException;
}
