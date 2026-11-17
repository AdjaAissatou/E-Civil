package sn.etatcivil.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Donne un identifiant à chaque requête (en-tête X-Request-Id), transmis aux services et renvoyé au client. */
@Component
class IdentifiantRequete implements GlobalFilter, Ordered {

    static final String EN_TETE = "X-Request-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String id = exchange.getRequest().getHeaders().getFirst(EN_TETE);
        String valeur = id == null || id.isBlank() || id.length() > 64 ? UUID.randomUUID().toString() : id;
        exchange.getResponse().getHeaders().set(EN_TETE, valeur);
        return chain.filter(exchange.mutate().request(r -> r.headers(h -> h.set(EN_TETE, valeur))).build());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
