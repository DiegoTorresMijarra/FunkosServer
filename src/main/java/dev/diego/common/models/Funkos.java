package dev.diego.common.models;

import com.google.gson.JsonObject;
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

    public Funkos(JsonObject jsonObject) {
        this.cod = UUID.fromString(jsonObject.get("cod").getAsString());
        this.nombre = jsonObject.get("nombre").getAsString();
        this.modelo = Modelo.valueOf(jsonObject.get("modelo").getAsString());
        this.precio = jsonObject.get("precio").getAsDouble();
        this.fechaLanzamiento = LocalDate.parse(jsonObject.get("fechaLanzamiento").getAsString());
        this.created_at = LocalDateTime.parse(jsonObject.get("created_at").getAsString());
        this.updated_at = LocalDateTime.parse(jsonObject.get("updated_at").getAsString());
    }
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
