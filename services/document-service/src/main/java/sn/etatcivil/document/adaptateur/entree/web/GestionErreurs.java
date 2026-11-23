package sn.etatcivil.document.adaptateur.entree.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import sn.etatcivil.document.domaine.DocumentIntrouvableException;
import sn.etatcivil.document.domaine.RegleMetierException;

@RestControllerAdvice
class GestionErreurs {

    @ExceptionHandler(DocumentIntrouvableException.class)
    ProblemDetail introuvable(DocumentIntrouvableException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(RegleMetierException.class)
    ProblemDetail regle(RegleMetierException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail invalide() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Données invalides");
    }

    @ExceptionHandler(RestClientException.class)
    ProblemDetail registre() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, "Le registre national ne répond pas");
    }
}
