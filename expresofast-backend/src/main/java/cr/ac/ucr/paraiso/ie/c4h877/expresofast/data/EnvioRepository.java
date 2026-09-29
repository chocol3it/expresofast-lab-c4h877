package cr.ac.ucr.paraiso.ie.c4h877.expresofast.data;

import cr.ac.ucr.paraiso.ie.c4h877.expresofast.domain.Envio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnvioRepository extends JpaRepository<Envio, Integer> {

    @Query("SELECT e FROM Envio e JOIN FETCH e.vehiculo v JOIN FETCH v.empresa JOIN FETCH e.conductor")
    List<Envio> findAllOptimized();

    Optional<Envio> findByCodigoRastreo(String codigoRastreo);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Envio e SET e.estadoEnvio = :nuevoEstado WHERE e.vehiculo.id = :vehiculoId")
    int updateEstadoByVehiculoId(@Param("nuevoEstado") String nuevoEstado, @Param("vehiculoId") Integer vehiculoId);

    @Procedure(procedureName = "SP_OBTENER_ENVIOS_POR_ESTADO")
    List<Envio> obtenerEnviosPorEstado(@Param("pEstado") String pEstado);

    Page<Envio> findByEstadoEnvio(String estadoEnvio, Pageable pageable);

    @Query("""
            SELECT e FROM Envio e
            WHERE (:estado IS NULL OR e.estadoEnvio = :estado)
              AND (:busqueda IS NULL
                   OR LOWER(e.codigoRastreo) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                   OR LOWER(e.destinatario) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                   OR LOWER(e.direccionDestino) LIKE LOWER(CONCAT('%', :busqueda, '%')))
            """)
    Page<Envio> buscarPaginado(@Param("estado") String estado, @Param("busqueda") String busqueda, Pageable pageable);
}