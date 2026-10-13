package com.erasmos.springmodulithdemo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

@SpringBootTest
class ModuleStructureTests {

    private final ApplicationModules applicationModules = ApplicationModules.of(Application.class);

    @Test
    void verify() {
        applicationModules.verify();
    }

    @Test
    void printout() {
        applicationModules.forEach(module -> System.out.println(module.toString(applicationModules)));
    }

    @Test
    void writeDocumentation() {
        new Documenter(applicationModules).writeDocumentation();
    }
}
