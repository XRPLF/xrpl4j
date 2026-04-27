---
name: batch-deps-upgrade
description: Batch all open Dependabot dependency upgrade PRs into a single PR
disable-model-invocation: true
---

Batch all open Dependabot dependency upgrade PRs into a single PR for this repository.

## Step 1: Discover

Run: `gh pr list --repo XRPLF/xrpl4j --label dependencies --state open --limit 500 --json number,title,headRefName,body,url`

Parse each PR to extract coordinates and versions. Maven Dependabot PRs come in two formats:
- **Single-package PRs**: title is `Bump <groupId>:<artifactId> from <old> to <new>` — parse groupId, artifactId, old version, and new version from title
- **Property PRs**: title is `Bump <propertyName> from <old> to <new>` — the property name maps to a `<properties>` entry in `pom.xml` (e.g., `jackson.version`, `feign.version`)
- **Grouped PRs**: title lists multiple packages — parse from PR body, which contains a structured list of package updates

If any PR can't be parsed from either title or body, flag it for manual review. Build a table of all proposed upgrades. Report the table to the user before proceeding.

Also note: Dependabot sometimes opens duplicate PRs for the same dependency across submodules (e.g., `xrpl4j-core/org.assertj-assertj-core-3.27.7` and `xrpl4j-integration-tests/org.assertj-assertj-core-3.27.7`). Deduplicate — if the version is managed centrally in the root `pom.xml` `<dependencyManagement>` or `<properties>`, one update covers all submodules. Mark the duplicates as No-op (managed by root pom).

## Step 2: Apply

1. Create a branch from main: `deps/batch-deps-upgrade-YYYY-QN` (use current year and quarter)
2. Inspect the root `pom.xml` to determine where each dependency's version is controlled:
   - **Version property**: a `<properties>` entry like `<jackson.version>2.14.0</jackson.version>` — update the property value
   - **Direct version in `<dependencyManagement>`**: a `<version>` element — update it directly
   - **BOM import in `<dependencyManagement>`**: a `<dependency>` with `<type>pom</type><scope>import</scope>` — update its `<version>`
   - **Maven plugin version**: in `<build><pluginManagement><plugins>` — update the `<version>` element
3. Check for **compatibility constraints** before upgrading. Major version bumps may require source-level changes (e.g., changed APIs, removed methods, renamed classes). Flag these as needing extra validation.
4. Apply each upgrade by editing `pom.xml` directly. For version properties, use `mvn versions:set-property -Dproperty=<name> -DnewVersion=<version> -DprocessAllModules` as an alternative to manual XML edits — but verify the result looks correct before proceeding.
5. Run `mvn dependency:resolve -q` to verify the dependency graph resolves without conflicts after applying all changes.
6. Classify each Dependabot PR as:
   - **Upgraded**: version changed in pom.xml
   - **No-op**: version was already current or newer, or is a duplicate submodule PR covered by a root-pom change
   - **Skipped**: compatibility issue or unresolvable conflict

## Step 3: Validate

Run the build and test suite in order:

1. `mvn clean install -DskipITs`
   - This compiles all modules, runs checkstyle, and runs unit tests
   - Integration tests (`*IT.java`) are excluded — they require live XRPL Testnet access
2. If step 1 fails, **attempt to fix the breaking change with code modifications before rolling back**. Common patterns for Java/Maven:
   - **Removed or renamed API** (e.g., a method removed in a major version): update call sites to use the new API
   - **Changed constructor or builder pattern**: adapt usage to the new API
   - **Checkstyle violations from new code patterns**: fix the style issue
   - **Compile errors from changed generics or type parameters**: adjust the type annotations
   - **Breaking changes in BouncyCastle** (crypto library): the API often changes significantly between major versions — check migration notes
   - **JUnit 5 → 6 migration**: JUnit 6 has a new package (`org.junit.jupiter.api.v2` or similar) — update imports if needed
3. Only roll back and mark as Skipped if:
   - The fix requires a large-scale migration across many files
   - The upgrade is blocked by an incompatible transitive dependency you cannot resolve
4. If a failure persists after investigation, roll back the upgrade and mark it as Skipped. Re-run validation until green.

## Step 4: Generate Outputs

Do NOT commit or create a PR. Instead, generate the following outputs for the human to use:

1. **Code changes note** — write a markdown file (`.claude/skills/batch-deps-upgrade/code-changes.md`) documenting every non-pom.xml source code change: what broke, why, and the minimal fix applied.

2. **Commit message** — output a concise commit message the human can copy-paste. Format: `chore(deps): quarterly batch dependency upgrade YYYY-QN` followed by a brief summary of upgrades, skips, and no-ops.

3. **PR description** — write a markdown file (`.claude/skills/batch-deps-upgrade/pr-description.md`) containing:
   - A summary section describing the batch upgrade
   - A "Superseded Dependabot PRs" table with columns: PR (linked), Package (`groupId:artifactId`), From, To, Status, MajorVersionUpgrade
     - Status values: Upgraded, No-op (reason), Skipped (reason: compatibility issue / build failure: error)
     - MajorVersionUpgrade: `No` if the major version number did not change. Otherwise `Yes` plus a link for each major version crossed — link to the package's Maven Central release notes, GitHub releases page, or CHANGELOG.md. Verify each link returns HTTP 200 before including it.
   - Closing instructions with two paragraphs:
     1. "After merging, close the following superseded PRs (Skipped ones remain open for future handling): #X, #Y, #Z" — list only Upgraded and No-op PRs.
     2. "The following PRs were Skipped and should remain open: #A (`groupId:artifactId`), #B (`groupId:artifactId`), ..." — these stay open so Dependabot keeps rebasing them.
