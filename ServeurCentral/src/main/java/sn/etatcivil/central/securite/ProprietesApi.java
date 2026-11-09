package sn.etatcivil.central.securite;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "registre")
public record ProprietesApi(String apiKey) {
}
