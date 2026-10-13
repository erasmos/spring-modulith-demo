package com.erasmos.springmodulithdemo;

import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.library.GeneralCodingRules;
import org.jmolecules.archunit.JMoleculesDddRules;
import org.jmolecules.event.annotation.DomainEvent;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

@AnalyzeClasses(packagesOf = Application.class, importOptions = ImportOption.DoNotIncludeTests.class)
class CodingConventionTests {

    @ArchTest
    static final ArchRule dddRules = JMoleculesDddRules.all();

    /*
        Modules should not be able to access another's repositories directly.
     */
    @ArchTest
    static final ArchRule repositoriesArePackagePrivate =
            classes()
                    .that().areAssignableTo(Repository.class)
                    .should().notBePublic();

    @ArchTest
    static final ArchRule noFieldInjection = GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

    @ArchTest
    static final ArchRule onlyUseInjectedClocks =
            noClasses()
                    .should().callMethod(Instant.class, "now");

    @ArchTest
    static final ArchRule eventListenerMethodsNamedCorrectly =
            methods()
                    .that().areAnnotatedWith(ApplicationModuleListener.class)
                    .should().haveName("on");

    @ArchTest
    static final ArchRule eventListenerMethodsTakeSingleDomainEvent =
            methods()
                    .that().areAnnotatedWith(ApplicationModuleListener.class)
                    .should(takeASingleDomainEventRecord());

    @ArchTest
    static final ArchRule servicesHaveCorrectSuffix =
            classes()
                    .that().areAnnotatedWith(Service.class)
                    .should().haveSimpleNameEndingWith("Service");

    @ArchTest
    static final ArchRule repositoriesHaveCorrectSuffix =
            classes()
                    .that().areAssignableTo(CrudRepository.class)
                    .should().haveSimpleNameEndingWith("Repository");

    private static ArchCondition<JavaMethod> takeASingleDomainEventRecord() {
        return new ArchCondition<>("take a single @DomainEvent record") {
            @Override
            public void check(JavaMethod javaMethod, ConditionEvents conditionEvents) {
                var rawParameterTypes = javaMethod.getRawParameterTypes();
                boolean satisfied =
                        rawParameterTypes.size() == 1 &&
                                rawParameterTypes.get(0).isRecord() &&
                                rawParameterTypes.get(0).isAnnotatedWith(DomainEvent.class);
                var message = javaMethod.getFullName() + " " + (satisfied ? "takes" : "does not take") + " a single @DomainEvent record.";
                conditionEvents.add(
                        new SimpleConditionEvent(
                                javaMethod,
                                satisfied,
                                message));
            }
        };
    }

}
