# Spring Modulith Demo

In order to get acquainted with [Spring Modulith](https://spring.io/projects/spring-modulith), I started by following
[this tutorial](https://www.baeldung.com/spring-modulith) which - though a decent enough introduction - barely scratched
the surface (and didn't include any mention of testing).

So I asked Claude (Opus 5.5) to teach me the framework. It gave me a few domain options, and I went with a (book) library.

In order to get a sense of the domain, I'd recommend running [`ModuleStructureTests`](src/test/java/com/erasmos/springmodulithdemo/ModuleStructureTests.java); there's a method there which
will write docs into `build/spring-modulith-docs/`. For viewing, you could install the [AsciiDoc plugin](https://plugins.jetbrains.com/plugin/7391-asciidoc); note that it will prompt you to install another plugin for viewing the diagrams. Best to open `all-docs.adoc`, first.

I'd recommend starting with the tests, specifically [`LibraryIntegrationTests`](src/test/java/com/erasmos/springmodulithdemo/LibraryIntegrationTests.java), which gives an overview.

As you probably already know, this framework uses [ArchUnit](https://www.archunit.org/) to verify the module structure, so I took the liberty
of adding a few coding conventions, as a secondary though superficial demo: [`CodingConventionTests`](src/test/java/com/erasmos/springmodulithdemo/CodingConventionTests.java).

There's much here – and I'm sure, much missing – but I hope that there's something useful.


