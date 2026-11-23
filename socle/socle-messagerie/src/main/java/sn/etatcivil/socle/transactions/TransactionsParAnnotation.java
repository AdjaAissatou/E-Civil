package sn.etatcivil.socle.transactions;

import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.transaction.interceptor.MatchAlwaysTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.lang.annotation.Annotation;

/**
 * Rend transactionnelles toutes les méthodes des classes portant l'annotation donnée.
 * Permet aux cas d'usage (couche application) de rester sans aucune dépendance à Spring :
 * c'est la configuration qui leur ajoute les transactions.
 */
public final class TransactionsParAnnotation {

    private TransactionsParAnnotation() {}

    public static TransactionInterceptor intercepteur() {
        TransactionInterceptor t = new TransactionInterceptor();
        t.setTransactionAttributeSource(new MatchAlwaysTransactionAttributeSource());
        t.setTransactionManagerBeanName("transactionManager");
        return t;
    }

    public static Advisor conseiller(Class<? extends Annotation> annotation, TransactionInterceptor intercepteur) {
        return new DefaultPointcutAdvisor(new AnnotationMatchingPointcut(annotation, true), intercepteur);
    }
}
