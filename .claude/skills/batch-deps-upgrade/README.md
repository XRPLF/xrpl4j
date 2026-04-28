# Batch Dependency Upgrade

Batches all open Dependabot PRs into a single upgrade PR.

## Prerequisites

- `gh auth login` — needed to list open Dependabot PRs
- Maven 3.x and JDK 8+ in `$PATH`

## Usage

From the xrpl4j repo root, start a new Claude Code session and run:

```
/batch-deps-upgrade
```

## What it does

1. Discovers all open Dependabot PRs via `gh pr list`
2. Applies version upgrades to `pom.xml` (properties, dependency versions, plugin versions)
3. Validates with `mvn clean install -DskipITs` (unit tests only — integration tests require live Testnet access)
4. Generates output files and a commit message for the human to use

## After it finishes

1. Review the changes and generated files. Ask Claude questions about specific changes if they don't make sense.
2. Stage and commit using the suggested commit message (the skill already creates a branch)
3. Push and open a PR using the generated PR description
4. After merge, close the superseded Dependabot PRs listed in the description
