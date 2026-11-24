package sn.etatcivil.habilitation.adaptateur.entree.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sn.etatcivil.habilitation.application.port.sortie.AnnuaireIndisponibleException;
import sn.etatcivil.habilitation.domaine.RegleMetierException;

@RestControllerAdvice
class GestionErreurs {

    @ExceptionHandler(RegleMetierException.class)
    ProblemDetail regle(RegleMetierException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail invalide(MethodArgumentNotValidException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Requête invalide");
    }

    @ExceptionHandler(AnnuaireIndisponibleException.class)
    ProblemDetail annuaire(AnnuaireIndisponibleException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "L'annuaire des identités est momentanément indisponible, veuillez réessayer");
    }
}
