# xrpl4j Project Instructions

## Advisor Tool

Call `advisor` proactively — before substantive work and before declaring done. No need to ask first.

## Parallel Sub-Agents and Workflows

Before spawning parallel sub-agents or workflows, ask the user first. Briefly explain what you'd do and why it would
help, then wait for approval.

## Java 1.8 Source Compatibility

`maven.compiler.source` and `maven.compiler.target` are both `1.8`. Do not use any language feature or API introduced
after Java 8:

- No `var`, records, sealed classes, text blocks, `instanceof` pattern matching
- No `List.of()`, `Map.of()`, `Set.of()`, `Map.entry()`, or other Java 9+ collection factory methods
- No `Optional.ifPresentOrElse()`, `Stream.takeWhile()`, or other Java 9+ stream/optional additions

The build will fail at compile time or produce incompatible bytecode.

## Checkstyle

Checkstyle runs on every build and violations at warning level fail the build. Run `mvn checkstyle:check -pl <module>`
to check before committing.

## Immutables

Value objects use `@Value.Immutable` (Immutables annotation processor). Do not hand-write builders, `equals()`,
`hashCode()`, or copy constructors — they are generated. New value types should follow the existing pattern.

## Running Tests

Unit tests (`*Test.java`) are run by Maven Surefire:

```
mvn -pl <module> test
```

Integration tests (`*IT.java`) are run by Maven Failsafe as part of the `verify` lifecycle — `mvn test` will **not**
pick them up. The canonical form (matching CI):

```
# All ITs against Testnet
mvn clean install -DuseTestnet

# All ITs against Devnet
mvn clean install -DuseDevnet

# All ITs against Clio
mvn clean install -DuseClioTestnet -DuseClioMainnet
```
