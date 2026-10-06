package com.loris.onebrain.coupon;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleName;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleNameEndingWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.loris.onebrain.coupon", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule domainDependsOnNothingOutsideItself = noClasses()
            .that().resideInAPackage("..coupon.domain..")
            .should().dependOnClassesThat(resideInAnyPackage(
                    "org.springframework..",
                    "jakarta..",
                    "..coupon.application..",
                    "..coupon.infrastructure.."))
            .because("business rules live in a framework-free domain");

    @ArchTest
    static final ArchRule applicationIsFrameworkAgnostic = noClasses()
            .that().resideInAPackage("..coupon.application..")
            .should().dependOnClassesThat(resideInAnyPackage(
                    "org.springframework..",
                    "jakarta..",
                    "..coupon.infrastructure.."))
            .because("the application layer must not know if it runs on web, CLI or a queue");

    @ArchTest
    static final ArchRule webTalksToApplicationNotToDomain = noClasses()
            .that().resideInAPackage("..coupon.infrastructure.web..")
            .should().dependOnClassesThat(resideInAPackage("..coupon.domain..")
                    .and(not(simpleNameEndingWith("Exception")))
                    .and(not(simpleName("CouponErrorCode"))))
            .because("the web adapter only sees use cases and their results (domain exceptions are mapped to HTTP)");

    @ArchTest
    static final ArchRule useCasesExposeOnlyExecute = methods()
            .that().areDeclaredInClassesThat().haveSimpleNameEndingWith("UseCase")
            .and().arePublic()
            .should().haveName("execute")
            .because("a use case represents a single user intention");

    @ArchTest
    static final ArchRule useCasesLiveInApplication = noClasses()
            .that().haveSimpleNameEndingWith("UseCase")
            .should().resideOutsideOfPackage("..coupon.application..");

    @ArchTest
    static final ArchRule noGenericServices = noClasses()
            .should().haveSimpleNameEndingWith("Service")
            .because("generic *Service classes hide many intentions behind one name");
}
