package dev.diego.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.reflect.TypeToken;
import dev.diego.client.exeptions.ClientException;
import dev.diego.common.enunms.Modelo;
import dev.diego.common.enunms.ResponseEstatus;
import dev.diego.common.enunms.TipoRequest;
import dev.diego.common.models.Funkos;
import dev.diego.common.models.Login;
import dev.diego.common.models.Request;
import dev.diego.common.models.Response;
import dev.diego.common.utils.PropertiesReader;
import dev.diego.common.utils.UUIDGenerator;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static dev.diego.common.enunms.TipoRequest.SALIR;
import static java.lang.Character.getType;

@Slf4j
public class Cliente {
    private static final String HOST = "localhost";
    private static final int PORT = 3000;
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, TypeAdapters.CALENDAR_FACTORY)
            .registerTypeAdapter(LocalDateTime.class, TypeAdapters.CALENDAR_FACTORY)
            .create();
    private SSLSocket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String token;


    public static void main(String[] args) {
        Cliente client = new Cliente();
        try {
            client.start();
        } catch (IOException e) {
            log.error("Error: " + e.getMessage());
        }
    }

    public void start() throws IOException {
    }

    private void closeConnection() throws IOException {
        log.debug("Cerrando la conexión con el servidor: " + HOST + ":" + PORT);
        System.out.println(" Cerrando Cliente");
        if (in != null)
            in.close();
        if (out != null)
            out.close();
        if (socket != null)
            socket.close();
    }

    private void openConnection() throws IOException {
        log.debug("Iniciando Cliente");
        Map<String, String> myConfig = readConfigFile();

        log.debug("Cargando fichero de propiedades");
        // System.setProperty("javax.net.debug", "ssl, keymanager, handshake"); // Debug
        System.setProperty("javax.net.ssl.trustStore", myConfig.get("keyFile")); // llavero cliente
        System.setProperty("javax.net.ssl.trustStorePassword", myConfig.get("keyPassword")); // clave

        SSLSocketFactory clientFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket socket = (SSLSocket) clientFactory.createSocket(HOST, PORT);

        // Opcionalmente podemos forzar el tipo de protocolo -> Poner el mismo que el cliente
        log.debug("Protocolos soportados: " + Arrays.toString(socket.getSupportedProtocols()));
        socket.setEnabledCipherSuites(new String[]{"TLS_AES_128_GCM_SHA256"});
        socket.setEnabledProtocols(new String[]{"TLSv1.3"});

        log.debug("Conectando al servidor: " + HOST + ":" + PORT);

        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println(" Cliente conectado a " + HOST + ":" + PORT);

        infoSession(socket);

    }

    private String sendRequestLogin() throws ClientException {
        String myToken = null;
        var loginJson = gson.toJson(new Login("pepe", "pepe1234"));
        Request request = new Request(TipoRequest.LOGIN, loginJson);
        System.out.println("Petición enviada de tipo: " + TipoRequest.LOGIN);
        log.debug("Petición enviada: " + request);
        // Enviamos la petición
        out.println(gson.toJson(request));
        // Recibimos la respuesta
        try {
            // Es estring porque el content es String no datos
            Response response = gson.fromJson(in.readLine(), new TypeToken<Response>() {
            }.getType());

            log.debug("Respuesta recibida: " + response.toString());
            // Ahora podríamos implementar un switch para cada tipo de respuesta
            // y hacer lo que queramos con ella...
            System.out.println("Respuesta recibida de tipo: " + response.status());

            if (Objects.requireNonNull(response.status()) == ResponseEstatus.TOKEN) {
                System.out.println(" Mi token es: " + response.contenido());
                myToken = response.contenido().toString();
            } else {
                throw new ClientException("Tipo de respuesta no esperado: " + response.contenido());
            }
        } catch (IOException e) {
            log.error("Error: " + e.getMessage());
        }
        return myToken;
    }

    private void sedRequestGetAllFunkos(String token) throws ClientException, IOException {
        // Al usar el toString me ahorro el problem ade las fechas con Gson
        Request request = new Request(TipoRequest.FINDALL, token);
        log.info("Petición enviada de tipo: " + TipoRequest.FINDALL);

        // Enviamos la petición
        out.println(gson.toJson(request));

        // Recibimos la respuesta
        Response response = gson.fromJson(in.readLine(), new TypeToken<Response>() {
        }.getType());
        log.debug("Respuesta recibida: " + response.toString());
        // Ahora podríamos implementar un switch para cada tipo de respuesta
        // y hacer lo que queramos con ella...
        System.out.println("Respuesta recibida de tipo: " + response.status());

        switch (response.status()) {
            case OK -> {
                List<JsonObject> responseContent = gson.fromJson(response.contenido().toString(), new TypeToken<List<JsonObject>>() {}.getType());
                List<Funkos> listado=responseContent.stream().map(Funkos::new).toList();
                log.info(" Los funkos son: ");
                listado.forEach(funk->log.info(funk.toString()));
            }
            case NO_AUTORIZADO ->
                    log.error("Usuario no autorizado para realizar esta accion: No se ha podido obtener los funkos " + response.contenido());
            case ERROR -> System.err.println(" Error: " + response.contenido()); // No se ha encontrado
        }
    }

    private void sendRequestGetFunkoById(String token, String id) throws IOException, ClientException {
        // Al usar el toString me ahorro el problem ade las fechas con Gson
        Request request = new Request(TipoRequest.FINDBYID,token);
        log.info("Petición enviada de tipo: " + TipoRequest.FINDBYID);

        // Enviamos la petición
        out.println(gson.toJson(request));

        // Recibimos la respuesta que puede ser de distintos tipos y contenido
        Response response = gson.fromJson(in.readLine(), new TypeToken<Response>() {
        }.getType());
        log.debug("Respuesta recibida: " + response.toString());

        System.out.println("Respuesta recibida de tipo: " + response.status());

        switch (response.status()) {
            case OK -> {
                JsonObject responseContent = gson.fromJson(response.contenido().toString(), new TypeToken<JsonObject>() {}.getType());
                System.out.println("El funko solicitado es: " + new Funkos(responseContent).toString());
            }
            case ERROR ->
                    System.err.println(" Error: funko no encontrado con id: " + id + ". " + response.contenido()); // No se ha encontrado
            default -> throw new ClientException("Error no esperado al obtener el funko");
        }
    }

    private void sendRequestGetFunkoByUuid(String token, String uuid) throws IOException, ClientException {
        // Al usar el toString me ahorro el problem ade las fechas con Gson
        Request request = new Request(TipoRequest.FINDBYUUID, token);
        System.out.println("Petición enviada de tipo: " + TipoRequest.FINDBYUUID);
        log.debug("Petición enviada: " + request);

        // Enviamos la petición
        out.println(gson.toJson(request));

        // Recibimos la respuesta que puede ser de distintos tipos y contenido
        Response response = gson.fromJson(in.readLine(), new TypeToken<Response>() {
        }.getType());
        log.debug("Respuesta recibida: " + response.toString());
        // Ahora podríamos implementar un switch para cada tipo de respuesta
        // y hacer lo que queramos con ella...
        System.out.println("Respuesta recibida de tipo: " + response.status());
        switch (response.status()) {
            case OK -> {
                JsonObject responseContent = gson.fromJson(response.contenido().toString(), new TypeToken<JsonObject>() {
                }.getType());
                System.out.println(" El Funko solicitado es: " + new Funkos(responseContent).toString());
            }
            case NO_AUTORIZADO ->
                    log.error("Usuario no autorizado para realizar esta accion: No se ha podido obtener el Funko con uuid: " + uuid + ". " + response.contenido());
            case ERROR ->
                    System.err.println("Error: Funko no encontrado con uuid: " + uuid + ". " + response.contenido()); // No se ha encontrado
            default -> throw new ClientException("Error no esperado al obtener el funko");
        }
    }

    private void sendRequestPostFunko(String token, Funkos funko) throws IOException, ClientException {
        // Al usar el toString me ahorro el problem ade las fechas con Gson
        var funkoJson = gson.toJson(funko);
        Request request = new Request(TipoRequest.INSERT, token);
        log.debug("Petición enviada de tipo: " + TipoRequest.INSERT);

        // Enviamos la petición
        out.println(gson.toJson(request));

        // Recibimos la respuesta que puede ser de distintos tipos y contenido
        Response response = gson.fromJson(in.readLine(), new TypeToken<Response>() {
        }.getType());
        log.debug("Respuesta recibida: " + response.toString());
        // Ahora podríamos implementar un switch para cada tipo de respuesta
        // y hacer lo que queramos con ella...
        System.out.println("Respuesta recibida de tipo: " + response.status());

        switch (response.status()) {
            case OK -> {
                JsonObject responseContent = gson.fromJson(response.contenido().toString(), new TypeToken<JsonObject>() {
                }.getType());
                System.out.println("El funko insertado es: " + new Funkos(responseContent).toString());
            }
            case ERROR ->
                    System.err.println(" Error: No se ha podido insertar el funko: " + response.contenido()); // No se ha encontrado
            default -> throw new ClientException("Error no esperado al insertar el funko");
        }
    }

    private void sendRequestPutFunkos(String token, Funkos funko) throws IOException, ClientException {
        // Al usar el toString me ahorro el problem ade las fechas con Gson

        var funkoJson = gson.toJson(funko);
        Request request = new Request(TipoRequest.UPDATE, token);
        System.out.println("Petición enviada de tipo: " + TipoRequest.UPDATE);
        log.debug("Petición enviada: " + request);

        // Enviamos la petición
        out.println(gson.toJson(request));

        // Recibimos la respuesta que puede ser de distintos tipos y contenido
        Response response = gson.fromJson(in.readLine(), new TypeToken<Response>() {
        }.getType());
        log.debug("Respuesta recibida: " + response.toString());
        // Ahora podríamos implementar un switch para cada tipo de respuesta
        // y hacer lo que queramos con ella...
        System.out.println("Respuesta recibida de tipo: " + response.status());

        switch (response.status()) {
            case OK -> {
                JsonObject responseContent = gson.fromJson(response.contenido().toString(), new TypeToken<JsonObject>() {
                }.getType());
                System.out.println(" El funko actualizado es: " + new Funkos(responseContent).toString());
            }
            case ERROR ->
                    System.err.println(" Error: No se ha podido actualizar el FUNKO: " + response.contenido()); // No se ha encontrado
            case NO_AUTORIZADO ->
                    log.error("Usuario no autorizado para realizar esta accion: No se ha podido actualizar el Funko con id: " + funko.getCod() + ". " + response.contenido());
            default -> throw new ClientException("Error no esperado al actualizar el funko");
        }
    }

    private void sendRequestDeleteFunko(String token, String id) throws IOException, ClientException {
        // Al usar el toString me ahorro el problem ade las fechas con Gson
        Request<String> request = new Request<>(TipoRequest.DELETE, token);
        System.out.println("Petición enviada de tipo: " + TipoRequest.DELETE);
        log.debug("Petición enviada: " + request);

        // Enviamos la petición
        out.println(gson.toJson(request));

        // Recibimos la respuesta que puede ser de distintos tipos y contenido
        Response response = gson.fromJson(in.readLine(), new TypeToken<Response>() {
        }.getType());
        log.debug("Respuesta recibida: " + response.toString());
        // Ahora podríamos implementar un switch para cada tipo de respuesta
        // y hacer lo que queramos con ella...
        System.out.println("Respuesta recibida de tipo: " + response.status());

        switch (response.status()) {
            case OK -> {
                JsonObject respuesta=gson.fromJson(response.contenido().toString(),new TypeToken<JsonObject>(){}.getType());
                Funkos responseContent=new Funkos(respuesta);

                log.info("El Funko eliminado es: " + responseContent.toString());
            }
            case NO_AUTORIZADO ->
                    log.error("Usuario no autorizado para realizar esta accion: No se ha podido eliminar el Funko con id: " + id + ". " + response.contenido());
            // No se ha encontrado
            default -> throw new ClientException("Error no esperado al eliminar el Funko");
        }
    }


    private void sendRequestSalir() throws IOException, ClientException {
        // Al usar el toString me ahorro el problem ade las fechas con Gson
        Request request = new Request(TipoRequest.SALIR,token);
        System.out.println("Petición enviada de tipo: " + SALIR);
        log.debug("Petición enviada: " + request);

        // Enviamos la petición
        out.println(gson.toJson(request));

        // Recibimos la respuesta
        Response response = gson.fromJson(in.readLine(), new TypeToken<Response>() {
        }.getType());
        log.debug("Respuesta recibida: " + response.toString());
        // Ahora podríamos implementar un switch para cada tipo de respuesta
        // y hacer lo que queramos con ella...
        System.out.println("Respuesta recibida de tipo: " + response.status());

        switch (response.status()) {
            case ERROR -> System.err.println(" Error: " + response.contenido());
            case BYE -> {
                System.out.println("Vamos a cerrar la conexión " + response.contenido());
                closeConnection();
            }
            default -> throw new ClientException(response.status().toString());
        }
    }

    public Map<String, String> readConfigFile() {
        try {
            log.debug("Leyendo el fichero de configuracion");
            PropertiesReader properties = new PropertiesReader("client.properties");

            String keyFile = properties.getProperty("keyFile");
            String keyPassword = properties.getProperty("keyPassword");

            // Comprobamos que no estén vacías
            if (keyFile.isEmpty() || keyPassword.isEmpty()) {
                throw new IllegalStateException("Hay errores al procesar el fichero de propiedades o una de ellas está vacía");
            }

            // Comprobamos el fichero de la clave
            if (!Files.exists(Path.of(keyFile))) {
                throw new FileNotFoundException("No se encuentra el fichero de la clave");
            }

            Map<String, String> configMap = new HashMap<>();
            configMap.put("keyFile", keyFile);
            configMap.put("keyPassword", keyPassword);

            return configMap;
        } catch (FileNotFoundException e) {
            log.error("Error en clave: " + e.getLocalizedMessage());
            System.exit(1);
            return null; // Este retorno nunca se ejecutará debido a System.exit(1)
        } catch (IOException e) {
            log.error("Error al leer el fichero de configuracion: " + e.getLocalizedMessage());
            return null;
        }
    }


    private void infoSession(SSLSocket socket) {
        log.debug("Información de la sesión");
        System.out.println("Información de la sesión");
        try {
            SSLSession session = socket.getSession();
            System.out.println("Servidor: " + session.getPeerHost());
            System.out.println("Cifrado: " + session.getCipherSuite());
            System.out.println("Protocolo: " + session.getProtocol());
            System.out.println("Identificador:" + new BigInteger(session.getId()));
            System.out.println("Creación de la sesión: " + session.getCreationTime());
            X509Certificate certificado = (X509Certificate) session.getPeerCertificates()[0];
            System.out.println("Propietario : " + certificado.getSubjectX500Principal());
            System.out.println("Algoritmo: " + certificado.getSigAlgName());
            System.out.println("Tipo: " + certificado.getType());
            System.out.println("Número Serie: " + certificado.getSerialNumber());
            // expiración del certificado
            System.out.println("Válido hasta: " + certificado.getNotAfter());
        } catch (SSLPeerUnverifiedException ex) {
            log.error("Error en la sesión: " + ex.getLocalizedMessage());
        }
    }

}
