package dev.diego.server.services.clientServices;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.diego.common.models.Login;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class TokenService {
    private static TokenService INSTANCE = null;
    private final Logger logger = LoggerFactory.getLogger(TokenService.class);

    private TokenService() {
    }

    public synchronized static TokenService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TokenService();
        }
        return INSTANCE;
    }

    public String createToken(Login logIn, String tokenSecret, long tokenExpiration) {
        logger.debug("Creando token");
        Algorithm algorithm = Algorithm.HMAC256(tokenSecret);
        return JWT.create()
                //.withIssuer("2DAW") // Quien lo emite *
                //.withSubject("Desarrollo Web Entornos Servidor") // Para que lo emite *
                // Solo guardar el id del usuario, lo demas siempre lo podemos recuperar
                // y no damos pistas al atacante
                .withClaim("username", logIn.username()) // Datos que queremos guardar
                .withClaim("password", logIn.password()) // Datos que queremos guardar
                .withIssuedAt(new Date()) // Fecha de emision *
                .withExpiresAt(new Date(System.currentTimeMillis() + tokenExpiration)) // Fecha de expiracion *
                //.withJWTId(UUID.randomUUID().toString()) // Identificador unico del token
                //.withNotBefore(new Date(System.currentTimeMillis() + 1000L)) // Fecha de cuando se puede usar
                .sign(algorithm); // Firmamos el token
    }

    public boolean verifyToken(String token, String tokenSecret, Login logIn) {
        logger.debug("Verificando token");
        Algorithm algorithm = Algorithm.HMAC256(tokenSecret);
        try {
            JWTVerifier verifier = JWT.require(algorithm)
                    .build(); // Creamos el verificador
            DecodedJWT decodedJWT = verifier.verify(token);
            logger.debug("Token verificado");
            // Comprobamos que el token es del usuario
            // Solo compruebas los datos obligatorios, en este caso el id
            // meter mas información es dar pistas al atacante
            return decodedJWT.getClaim("username").asString().compareTo(logIn.username())==0 &&
                    decodedJWT.getClaim("password").asString().equals(logIn.password());
        } catch (Exception e) {
            logger.error("Error al verificar el token: " + e.getMessage());
            return false;
        }
    }

    public boolean verifyToken(String token, String tokenSecret) {
        logger.debug("Verificando token");
        Algorithm algorithm = Algorithm.HMAC256(tokenSecret);
        try {
            JWTVerifier verifier = JWT.require(algorithm)
                    .build(); // Creamos el verificador
            DecodedJWT decodedJWT = verifier.verify(token);
            logger.debug("Token verificado");
            return true;
        } catch (Exception e) {
            logger.error("Error al verificar el token: " + e.getMessage());
            return false;
        }
    }

    public java.util.Map<String, com.auth0.jwt.interfaces.Claim> getClaims(String token, String tokenSecret) {
        logger.debug("Verificando token");
        Algorithm algorithm = Algorithm.HMAC256(tokenSecret);
        try {
            JWTVerifier verifier = JWT.require(algorithm)
                    .build(); // Creamos el verificador
            DecodedJWT decodedJWT = verifier.verify(token);
            logger.debug("Token verificado");
            return decodedJWT.getClaims();
        } catch (Exception e) {
            logger.error("Error al verificar el token: " + e.getMessage());
            return null;
        }
    }
}