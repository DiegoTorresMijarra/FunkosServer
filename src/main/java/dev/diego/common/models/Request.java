package dev.diego.common.models;

import dev.diego.common.enunms.TipoRequest;

public record Request <T>(TipoRequest type, T valor){

}
