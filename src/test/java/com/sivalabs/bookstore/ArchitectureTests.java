package com.sivalabs.bookstore;

import static com.sivalabs.bookstore.ArchitectureTests.BASE_PACKAGE;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.springframework.stereotype.Service;

@AnalyzeClasses(packages = BASE_PACKAGE)
class ArchitectureTests {
    public static final String BASE_PACKAGE = "com.sivalabs.bookstore";

    // Controllers must not be called by service or repository classes
    @ArchTest
    static final ArchRule controllers_should_not_be_accessed_by_domain = noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .accessClassesThat()
            .resideInAPackage("..web..");

    // Services must not import anything from the api/web layer
    @ArchTest
    static final ArchRule services_must_not_depend_on_controllers = noClasses()
            .that()
            .haveNameMatching(".*Service")
            .should()
            .dependOnClassesThat()
            .haveNameMatching(".*Controller");

    // Repositories must not be accessed from outside their own module
    @ArchTest
    static final ArchRule repositories_are_not_public =
            noClasses().that().haveNameMatching(".*Repository").should().bePublic();

    // Entities must not be public
    @ArchTest
    static final ArchRule entities_are_not_public =
            noClasses().that().haveNameMatching(".*Entity").should().bePublic();

    // Services are public (accessible to inbound handlers in the same module),
    // but must only be accessed from within their own module — other modules must go through *API facades.
    @ArchTest
    static final ArchRule services_should_not_be_used_cross_module = classes()
            .that()
            .areAnnotatedWith(Service.class)
            .should(new ArchCondition<>("only be accessed from same module") {
                @Override
                public void check(JavaClass service, ConditionEvents events) {
                    String serviceModule = moduleOf(service);
                    for (Dependency dependency : service.getDirectDependenciesToSelf()) {
                        JavaClass origin = dependency.getOriginClass();
                        String originModule = moduleOf(origin);
                        if (!serviceModule.equals(originModule)) {
                            String message =
                                    origin.getName() + " accesses " + service.getName() + " from another module";
                            events.add(SimpleConditionEvent.violated(dependency, message));
                        }
                    }
                }
            });

    private static String moduleOf(JavaClass clazz) {
        String pkg = clazz.getPackageName();
        String[] parts = pkg.split("\\.");
        return parts[BASE_PACKAGE.split("\\.").length];
    }
}
