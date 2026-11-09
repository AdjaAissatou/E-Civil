package sn.etatcivil.demande.adaptateur.entree.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import sn.etatcivil.demande.application.port.sortie.RegistreIndisponibleException;
import sn.etatcivil.demande.domaine.DemandeIntrouvableException;
import sn.etatcivil.demande.domaine.RegleMetierException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
class GestionErreurs {

    private static final Logger log = LoggerFactory.getLogger(GestionErreurs.class);

    @ExceptionHandler(DemandeIntrouvableException.class)
    ProblemDetail introuvable(DemandeIntrouvableException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(RegleMetierException.class)
    ProblemDetail regle(RegleMetierException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
    }

    @ExceptionHandler(RegistreIndisponibleException.class)
    ProblemDetail indisponible(RegistreIndisponibleException e) {
        log.warn("Service distant indisponible : {}", e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "Un service de l'état civil ne répond pas pour le moment. Réessayez dans quelques minutes.");
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
    ProblemDetail illisible() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Requête illisible ou valeur inconnue");
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ProblemDetail conflit() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Cette demande vient d'être modifiée par quelqu'un d'autre. Vérifiez son état puis recommencez.");
    }
}
