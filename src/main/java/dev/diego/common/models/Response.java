package dev.diego.common.models;

import dev.diego.common.enunms.ResponseEstatus;

public record Response<T> (ResponseEstatus status, T contenido) {
}
