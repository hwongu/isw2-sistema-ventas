package pe.edu.ulima.isw2.pagos.dao;

import java.util.List;
import java.util.Optional;

public interface Dao<ID, E> {
    Optional<E> buscarPorId(ID id);

    List<E> listar();

    E insertar(E entidad);

    boolean actualizar(E entidad);

    boolean eliminar(ID id);
}
