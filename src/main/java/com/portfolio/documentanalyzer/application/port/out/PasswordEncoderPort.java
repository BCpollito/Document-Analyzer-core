package com.portfolio.documentanalyzer.application.port.out;

/** Puerto de salida para el hash de contraseñas. */
public interface PasswordEncoderPort {

    String encode(String rawPassword);

}
