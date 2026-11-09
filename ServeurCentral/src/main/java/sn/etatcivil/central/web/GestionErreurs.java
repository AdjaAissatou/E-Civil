package sn.etatcivil.central.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import sn.etatcivil.central.acte.ActeIntrouvableException;
import sn.etatcivil.central.acte.RegleMetierException;

import java.util.LinkedHashMap;
import java.util.Map;

/** Réponses d'erreur au format RFC 7807, sans jamais exposer de détail technique interne. */
@RestControllerAdvice
public class GestionErreurs {

    private static final Logger log = LoggerFactory.getLogger(GestionErreurs.class);

    @ExceptionHandler(ActeIntrouvableException.class)
    ProblemDetail introuvable(ActeIntrouvableException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(RegleMetierException.class)
    ProblemDetail regle(RegleMetierException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail invalide(MethodArgumentNotValidException e) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Données invalides");
        Map<String, String> champs = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> champs.putIfAbsent(f.getField(), f.getDefaultMessage()));
        p.setProperty("champs", champs);
        return p;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ProblemDetail illisible(Exception e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Requête illisible ou valeur inconnue");
    }

    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, DataIntegrityViolationException.class})
    ProblemDetail conflit(Exception e) {
        log.warn("Conflit d'écriture : {}", e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "L'acte a été modifié en même temps par quelqu'un d'autre, veuillez réessayer");
    }
}
