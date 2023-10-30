package dev.diego.common.models;

import dev.diego.common.enunms.NivelesSeguridad;
import dev.diego.common.enunms.TipoRequest;

import java.util.ArrayList;
import java.util.List;

public record User(int id, String name, String password, NivelesSeguridad level) {
    public static final ArrayList<TipoRequest> PERMISOS_JUNIOR =new ArrayList<>(
            List.of(TipoRequest.LOGIN, TipoRequest.SALIR, TipoRequest.FINDBYID));

    public boolean checkPermiso(TipoRequest tipo){
        if(level==NivelesSeguridad.ADMIN){
            return true;
        }
        else return PERMISOS_JUNIOR.contains(tipo);
    }
}
