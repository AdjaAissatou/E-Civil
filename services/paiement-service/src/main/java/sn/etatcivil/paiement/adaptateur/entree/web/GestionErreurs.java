package sn.etatcivil.paiement.adaptateur.entree.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import sn.etatcivil.paiement.application.port.sortie.OperateurIndisponibleException;
import sn.etatcivil.paiement.domaine.RegleMetierException;

@RestControllerAdvice
class GestionErreurs {

    @ExceptionHandler(RegleMetierException.class)
    ProblemDetail regle(RegleMetierException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
    }

    @ExceptionHandler(OperateurIndisponibleException.class)
    ProblemDetail indisponible() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "Le service de paiement est momentanément indisponible, veuillez réessayer dans quelques minutes");
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ProblemDetail conflit() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Paiement en cours de traitement, réessayez");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail illisible() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Requête invalide");
    }
}
