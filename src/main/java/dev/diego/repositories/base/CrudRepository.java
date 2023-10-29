package dev.diego.repositories.base;

import java.sql.SQLException;
import java.util.List;

/**
 * @author Diego
 * @param <T> Entidad
 */
public interface CrudRepository<T> {

    /**
     * Encontrar todas las entidades
     * @return List Lista de entidades
     * @throws SQLException Excepcion
     */
    List<T> findAll() throws SQLException;

    /**
     * Encontrar una entidad por su id
     * @param id Id de la entidad
     * @return T Entidad
     * @throws SQLException - Excepcion
     */
    T findById(Integer id) throws SQLException;

    /**
     * Insertar una entidad
     * @param entity Entidad
     * @return T  Entidad insertada
     * @throws SQLException - Excepcion
     */
    T insert(T entity) throws SQLException;

    /**
     * Actualizar una entidad
     * @param entity  Entidad
     * @return T  Entidad actualizada
     * @throws SQLException  Excepcion
     */
    T update(T entity)  throws SQLException ;

    /**
     * Eliminar una entidad
     * @param entity  Entidad
     * @return T  Entidad eliminada
     * @throws SQLException  Excepcion
     */
    T delete(T entity)  throws SQLException ;

}