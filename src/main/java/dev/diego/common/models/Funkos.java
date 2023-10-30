package dev.diego.common.models;

import dev.diego.common.enunms.Modelo;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Setter
public class Funkos {
    private UUID cod;
    private String nombre;
    private Modelo modelo;
    private Double precio;
    private LocalDate fechaLanzamiento;
    @Builder.Default
    private LocalDateTime created_at=LocalDateTime.now();
    @Builder.Default
    private LocalDateTime updated_at=LocalDateTime.now();
    @Override
    public String toString() {
        return "Funkos{" +
                "cod=" + cod +
                ", nombre='" + nombre + '\'' +
                ", modelo=" + modelo +
                ", precio=" + precio +
                ", FechaLanzamiento=" + fechaLanzamiento +
                ", created_at=" + created_at +
                ", updated_at=" + updated_at +
                '}';
    }
}
