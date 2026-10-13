# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Purpose

A learning project for Spring Modulith, also shared as a reference with a team that works in a DDD-heavy codebase. The user writes the code themselves: suggest changes (with snippets) rather than applying them. Running builds and tests to check their work is fine. Favour clarity and short "why" comments, since readers may be new to Modulith.

## Commands

```bash
./gradlew build                      # compile + test
./gradlew test                       # all tests
./gradlew test --tests 'com.erasmos.springmodulithdemo.SomeTests'            # one class
./gradlew test --tests 'com.erasmos.springmodulithdemo.SomeTests.someMethod' # one method
./gradlew bootRun                    # run the app
```

Stack: Java 17 toolchain, Gradle 9 wrapper, Spring Boot 4.1, Spring Modulith 2.1 (BOM-managed), Spring Data JDBC on in-memory H2 (H2 console enabled), Actuator plus Modulith actuator/observability, jMolecules (events, DDD), ArchUnit 1.4.2 (`archunit-junit5`) with jMolecules' ArchUnit rules.

## Architecture

Spring Modulith treats each direct subpackage of `com.erasmos.springmodulithdemo` (the `@SpringBootApplication` package) as an application module. Only a module's top-level package is its public API; nested packages are internal unless exposed via `@NamedInterface`. Moving a type into a subpackage hides it from other modules.

The domain is library lending:

| Module | Role | `allowedDependencies` |
|---|---|---|
| `catalog` | Books; answers "does this book exist?" | none |
| `members` | Members (placeholder service) | none |
| `loans` | Checking books out; owns availability; publishes `BookCheckedOut` | `catalog`, `catalog :: ids`, `members`, `members :: ids` |
| `notifications` | Emails the member when a book is checked out (listener) | `catalog :: ids`, `loans`, `loans :: ids`, `members :: ids` |

- Modules refer to each other's aggregates by typed id (`BookId`, `MemberId`, `LoanId`), each owned by its aggregate's module and exposed through an `ids` named interface (type-level `@NamedInterface("ids")`). A type in a named interface leaves the module's unnamed interface, so plain `"catalog"` in `allowedDependencies` does not cover `BookId`.
- A dependency is any type reference (including listener parameter types), so events don't break compile-time cycles. Events belong to the publishing module.
- The root package belongs to no module and holds cross-cutting infrastructure: `ClockConfiguration` (inject `Clock`, never call `Instant.now()`) and `JdbcIdConverters`, the app's single `JdbcCustomConversions` bean (all id ⇄ `Long` converters go there; converters must be classes, not lambdas). `@ApplicationModuleTest` scans the root package, so every module test gets these. The empty `shared` package is a leftover from the shared-kernel experiment and can be deleted.

### Events and the publication registry

Because `spring-modulith-starter-jdbc` is on the classpath, published events are stored in the event publication registry. The table is only auto-created when `spring.modulith.events.jdbc.schema-initialization.enabled=true` is set.

- `@ApplicationModuleListener` runs asynchronously after commit (Modulith auto-enables `@Async`). Tests must wait for its effects (`Scenario.andWaitForStateChange`), and must not be `@Transactional` (a rollback means the listener never fires).
- A listener failure doesn't reach the publisher's caller; it leaves the row with `STATUS=FAILED`. `FailedEventPublications.resubmit(...)` retries the same row (`COMPLETION_ATTEMPTS` 1 → 2).
- The registry stores the event's class name and the listener's full signature: renaming or moving either breaks incomplete publications.
- Completion modes (`spring.modulith.events.completion-mode`): UPDATE (default, row kept), DELETE (row removed), ARCHIVE (moved to `event_publication_archive`). `deletePublicationsOlderThan(Duration.ZERO)` clears both tables; `deletePublications(predicate)` does not clear the archive.
- Typed ids serialize as `"bookId":{"value":100}` in the registry JSON.

Beans with `@ApplicationModuleListener` (or `@Transactional`/`@Async`) are CGLIB proxies: read their state through methods, never fields.

## Tests (suggested reading order)

1. `LibraryIntegrationTests`: the whole path end to end (`@SpringBootTest` + `@EnableScenarios`; only `EmailGateway` mocked).
2. `ModuleStructureTests`: `verify()` (cycles, internals, `allowedDependencies`, jMolecules DDD rules), the module printout (`+` bean dependency, `-` type-only), and `Documenter`, which writes diagrams and module canvases to `build/spring-modulith-docs/` (not committed).
3. `CatalogModuleTest`, `LoansModuleTest`: `@ApplicationModuleTest` per module; `LoansModuleTest` covers rollback (no event), unknown book, book on loan, success.
4. `NotificationsModule{Update,Delete,Archive}CompletionModeTest`: one subclass per completion mode of the abstract `NotificationsModuleTest`, each with the same three scenarios (gateway fails / fails then resubmitted / succeeds). They publish `BookCheckedOut` directly via `scenario.publish` (don't wrap `publish` in `stimulate`).
5. `CodingConventionTests`: ArchUnit conventions inside modules (package-private repositories, no field injection, no `Instant.now()`, listener methods named `on` taking one `@DomainEvent` record, naming suffixes, jMolecules DDD rules).

Notes:
- `@ApplicationModuleTest` bootstrap modes follow bean dependencies only, not event-listener ones.
- Root-package test helpers (`EventPublicationTestRepository`, `EventPublicationRow`) are available to every module test without `@Import`.
- `@DataJdbcTest` doesn't load `@Configuration` classes: `LoanRepositoryTest` imports `JdbcIdConverters`.
- Test style: `var` only in tests; literals preferred in assertions; leaf `@DisplayName`s state the expectation and end with a full stop.
- IntelliJ's Gradle runner shows only one `@ArchTest` field in its tree; use the IntelliJ test runner or the Gradle report.

## Relation to DDD

| DDD idea | Where it is |
|---|---|
| Bounded contexts | The four modules; boundaries enforced by `verify()` and `allowedDependencies` |
| Aggregates, referenced by identity | `@AggregateRoot` on `Loan` and `Book` (with `@Identity`); typed ids implementing jMolecules `Identifier`; the jMolecules rule rejects an aggregate holding another aggregate |
| One owner per invariant / information expert | `loans` owns availability (`existsByBookId`); `catalog` only knows whether a book exists. Duplicating availability in `catalog` via an async event would race (double checkout) |
| Domain events | `@DomainEvent` records, published inside the transaction, handled after commit; past-tense names, no `Event` suffix |
| Published language | The `ids` named interfaces: the only part of `catalog`/`members` other modules may use |
| Context mapping (upstream/downstream) | `catalog` and `members` are upstream leaves; `loans` is downstream of both; `notifications` is downstream of `loans` |
| Shared kernel (rejected) | Tried putting `BookCheckedOut` in a `shared` module, then chose single ownership instead |

## Possible next steps

If the team is interested (frame these around DDD):

- **Moments**: `spring-modulith-moments` publishes time events (`DayHasPassed`, …); detect overdue loans and publish `LoanBecameOverdue`. Test with `TimeMachine` and the injected `Clock`.
- **Value objects and entities**: jMolecules `@ValueObject` (e.g. a loan period) and `@Entity` inside an aggregate (e.g. renewals of a loan); returning a book (`BookReturned`) completes the loan lifecycle.
- **Members**: give `members` a real aggregate and an invariant (e.g. a member may hold at most N loans), and decide which module owns that rule.
- **Bootstrap modes**: `@ApplicationModuleTest(mode = DIRECT_DEPENDENCIES / ALL_DEPENDENCIES)` and `extraIncludes`, compared with mocking `CatalogService`.
- **Externalized events**: `@Externalized` to publish `BookCheckedOut` to a broker for other services.
- **Observability**: the Modulith actuator endpoint (`/actuator/modulith`) and per-module tracing.
- **Javadoc in the docs**: `spring-modulith-apt` extracts Javadoc for `Documenter`, but under Gradle it wrote to `~/.gradle/workers/` instead of `build/`; needs investigating.
- **`ApplicationModuleInitializer`**: per-module startup logic, e.g. seeding the catalog.
