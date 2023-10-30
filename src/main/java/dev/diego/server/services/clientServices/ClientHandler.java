package dev.diego.server.services.clientServices;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.diego.common.models.Login;
import dev.diego.common.models.Request;
import dev.diego.common.models.Response;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
public class ClientHandler extends Thread {
    private final Socket clientSocket;
    private final Gson gson = new Gson();
    private final InputStream inStream;
    private final OutputStreamWriter outStream;
    BufferedReader in;
    PrintWriter out;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;

        try {
            this.inStream = socket.getInputStream();
            this.outStream = new OutputStreamWriter(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(inStream));
            out = new PrintWriter(outStream, true);

            log.info("Cliente conectado: " + socket.getInetAddress().getHostAddress());

        } catch (IOException e) {
            log.error("Error al iniciar el cliente: " + e.getMessage());
        }
    }
    public void closeAll() throws IOException {
        // Cerramos los streams y el socket
        log.debug("Cerrando la conexión con el cliente: " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());
        out.close();
        in.close();
        clientSocket.close();
    }

    public void run() {
        try {
            // Esto lo hacemos porque envío cosas en texto

            // Nos ponemos a escuchar hasta que nos llegue salir
            JsonObject clientInput=gson.fromJson(in.readLine(),JsonObject.class);


            String tipoPeticion=clientInput.get("type").getAsString();//esta hecho regulera, pero no se pedia mas
            switch (tipoPeticion){
                case "SALIR":
                    out.println("Vuelva Pronto");
                    break;
                case "LOGIN":{
                    JsonObject logJS= (JsonObject) clientInput.get("valor");
                    Login log=new Login(logJS.get("user").getAsString(),logJS.get("pasword").getAsString());

                    out.println(TokenService.getInstance().createToken(log,Server.CODIFICADOR,10000));
                }
                break;
                case "NUMERO":{
                    double valor=clientInput.get("valor").getAsDouble();
                    out.println(valor*2);
                }
                break;
                default:
                    out.println("No conozco esa clase de peticion");
            }

            closeAll();
            System.out.println("Cliente desconectado: " + clientSocket.getInetAddress().getHostAddress());

        } catch (IOException e) {
            System.out.println("Error al escuchar al cliente: " + e.getMessage());
        }
    }

    private void openConnection() throws IOException {
        log.debug("Conectando con el cliente  : " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    private void handleRequest(Request<String> request) throws IOException {
        // Procesamos la petición y devolvemos la respuesta, esto puede ser un método
        switch (request.type()) {
            case LOGIN ->
                    out.println(gson.toJson(new Response<>(Response.Status.OK, "Bienvenido", LocalDateTime.now().toString())));
            case FECHA ->
                    out.println(gson.toJson(new Response<>(Response.Status.OK, LocalDateTime.now().toString(), LocalDateTime.now().toString())));
            case UUID ->
                    out.println(gson.toJson(new Response<>(Response.Status.OK, UUID.randomUUID().toString(), LocalDateTime.now().toString())));
            case SALIR -> {
                out.println(gson.toJson(new Response<>(Response.Status.BYE, "Adios", LocalDateTime.now().toString())));
                closeConnection();
            }
            default ->
                    out.println(gson.toJson(new Response<>(Response.Status.ERROR, "No tengo ni idea", LocalDateTime.now().toString())));
        }
    }
}