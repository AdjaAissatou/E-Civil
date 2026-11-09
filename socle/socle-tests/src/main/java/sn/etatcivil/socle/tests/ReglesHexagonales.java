package sn.etatcivil.socle.tests;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

import static com.tngtech.archunit.base.DescribedPredicate.alwaysTrue;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;

/**
 * Règles communes à tous les services, vérifiées à chaque build :
 * <ul>
 *   <li>le domaine ne dépend que de Java ;</li>
 *   <li>l'application ne dépend que du domaine (aucun framework) ;</li>
 *   <li>les adaptateurs ne se connaissent pas entre eux et passent par les ports.</li>
 * </ul>
 * Paquets attendus : {@code <racine>.domaine}, {@code <racine>.application}, {@code <racine>.adaptateur.*},
 * {@code <racine>.configuration}.
 */
public final class ReglesHexagonales {

    private ReglesHexagonales() {}

    public static void verifier(String racine) {
        JavaClasses classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(racine);

        onionArchitecture()
                .domainModels(racine + ".domaine..")
                .applicationServices(racine + ".application..")
                .adapter("entree", racine + ".adaptateur.entree..")
                .adapter("sortie", racine + ".adaptateur.sortie..")
                .withOptionalLayers(true)
                // la configuration (et la classe de démarrage) assemble toutes les couches : c'est son rôle
                .ignoreDependency(resideInAPackage(racine + ".configuration.."), alwaysTrue())
                .check(classes);

        classes().that().resideInAPackage(racine + ".domaine..")
                .should().onlyDependOnClassesThat().resideInAnyPackage(racine + ".domaine..", "java..")
                .because("le domaine est du Java pur, indépendant de toute technologie")
                .check(classes);

        classes().that().resideInAPackage(racine + ".application..")
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage(racine + ".domaine..", racine + ".application..", "java..")
                .because("les cas d'usage ne dépendent d'aucun framework : ils parlent aux ports")
                .check(classes);

        noClasses().that().resideInAnyPackage(racine + ".domaine..", racine + ".application..", racine + ".adaptateur..")
                .should().dependOnClassesThat().resideInAPackage(racine + ".configuration..")
                .because("seule la configuration assemble les pièces")
                .check(classes);
    }
}
