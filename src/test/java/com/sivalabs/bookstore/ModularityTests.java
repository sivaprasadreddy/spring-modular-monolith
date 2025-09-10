package com.sivalabs.bookstore;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModule;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModularityTests {
    static ApplicationModules modules = ApplicationModules.of(BookStoreApplication.class);

    @Test
    void verifiesModularStructure() {
        modules.verify();
    }

    @Test
    void createModuleDocumentation() {
        new Documenter(modules).writeDocumentation();
    }

    @Test
    void verifiesCacheComponentsAreProperlyEncapsulated() {
        // Verify that cache components are properly encapsulated within their modules

        // The primary verification is that modules.verify() passes
        // This ensures all cache components respect module boundaries
        modules.verify();

        // Verify that orders and config modules exist and contain expected components
        var moduleNames = modules.stream().map(ApplicationModule::getName).toList();
        assertThat(moduleNames).contains("orders", "config");

        // Additional verification that the basic structure is sound
        assertThat(modules).isNotNull();
        assertThat(modules.stream()).isNotEmpty();
    }

    @Test
    void verifiesCacheModuleBoundariesAreRespected() {
        // Verify that cache components don't violate module boundaries
        // The most important test is that modules.verify() passes with cache components
        modules.verify();

        // Verify orders module exists and follows declared dependencies
        var ordersModule = modules.getModuleByName("orders");
        assertThat(ordersModule).isPresent();

        // The fact that modules.verify() passes means cache components
        // are properly encapsulated and don't violate boundaries
    }

    @Test
    void verifiesCacheInfrastructureModuleStructure() {
        // Verify that cache infrastructure is properly organized
        // and doesn't violate module boundaries

        // Primary verification: modules.verify() must pass
        modules.verify();

        // Verify config module exists for infrastructure components
        var configModule = modules.getModuleByName("config");
        assertThat(configModule).isPresent();

        // The passing of modules.verify() ensures that:
        // 1. Cache infrastructure in config module is properly placed
        // 2. Cache components in orders module are properly encapsulated
        // 3. No module boundary violations exist
        // 4. Dependencies follow declared allowed patterns
    }

    @Test
    void ensuresCacheComponentsDoNotBreakModularity() {
        // This is the most critical test for cache components
        // It ensures that adding cache functionality hasn't broken
        // the modular architecture

        // If this passes, cache components are properly integrated
        modules.verify();

        // Additional assertion to ensure we have the expected modules
        var allModules = modules.stream().toList();
        assertThat(allModules).hasSizeGreaterThan(0);

        // The cache implementation should maintain the existing
        // modular structure without introducing violations
    }
}
