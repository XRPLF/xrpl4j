---
name: batch-deps-upgrade
description: Batch all open Dependabot dependency upgrade PRs into a single PR, plus any further upgrades needed to resolve open Semgrep JIRA tickets that a package upgrade can fix
disable-model-invocation: true
---

Batch all open Dependabot dependency upgrade PRs into a single PR for this repository, **plus any further upgrades needed to resolve the open Semgrep JIRA tickets that a package upgrade can fix**.

**Scope — only tickets a package upgrade can fix:** ones naming a vulnerable dependency and a version that fixes it.

Within that scope, the two inputs are independent. Every in-scope ticket gets fixed whether or not a Dependabot PR happens to propose that upgrade. Where no open Dependabot PR covers an impacted package, this PR adds the upgrade itself.

**Two modes — decide which before doing anything else.** The invocation argument arrives as `ARGUMENTS`:

- **empty** (`/batch-deps-upgrade`) — run Steps 1-4, then **stop**. Do not continue into "Closing run" even though it appears further down; this mode writes nothing to JIRA or GitHub, only the working tree and local files.
- **`close`** (`/batch-deps-upgrade close`) — run **only** "Closing run" at the bottom. No discovery, no branch, no build, no tests. Normally used after the batch PR merges.
- **anything else** — stop and ask which was meant. Do not guess.

Requires `gh auth login` and, for the Semgrep parts, the Atlassian MCP.

## Step 1: Discover

Run: `gh pr list --repo XRPLF/xrpl4j --label dependencies --state open --limit 500 --json number,title,headRefName,body,url`

Parse each PR to extract coordinates and versions. Maven Dependabot PRs come in two formats:
- **Single-package PRs**: title is `Bump <groupId>:<artifactId> from <old> to <new>` — parse groupId, artifactId, old version, and new version from title
- **Property PRs**: title is `Bump <propertyName> from <old> to <new>` — the property name maps to a `<properties>` entry in `pom.xml` (e.g., `jackson.version`, `feign.version`)
- **Grouped PRs**: title lists multiple packages — parse from PR body, which contains a structured list of package updates

If any PR can't be parsed from either title or body, flag it for manual review. Build a table of all proposed upgrades. Report the table to the user before proceeding.

Also note: Dependabot sometimes opens duplicate PRs for the same dependency across submodules (e.g., `xrpl4j-core/org.assertj-assertj-core-3.27.7` and `xrpl4j-integration-tests/org.assertj-assertj-core-3.27.7`). Deduplicate — if the version is managed centrally in the root `pom.xml` `<dependencyManagement>` or `<properties>`, one update covers all submodules. Mark the duplicates as No-op (managed by root pom).

Also fetch the Semgrep tickets per **Where the list comes from** under "Semgrep tickets" below, and add them to the same table.

## Step 2: Apply

1. If a target branch already exists for this quarter's upgrades, work on that branch. Otherwise create a new branch from main: `deps/batch-deps-upgrade-YYYY-QN` (use current year and quarter).
2. Inspect the root `pom.xml` to determine where each dependency's version is controlled:
   - **Version property**: a `<properties>` entry like `<jackson.version>2.14.0</jackson.version>` — update the property value
   - **Direct version in `<dependencyManagement>`**: a `<version>` element — update it directly
   - **BOM import in `<dependencyManagement>`**: a `<dependency>` with `<type>pom</type><scope>import</scope>` — update its `<version>`
   - **Maven plugin version**: in `<build><pluginManagement><plugins>` — update the `<version>` element
3. Check for **compatibility constraints** before upgrading:
   - **Java version**: read `maven.compiler.source` from `pom.xml`. If a dependency's new version requires a higher Java version than the project's compiler source, mark it as Skipped immediately — do not attempt the upgrade. Known constraints: caffeine 3.x requires Java 11+, junit-bom 6.x requires Java 17+.
   - **API changes**: major version bumps may require source-level changes (e.g., changed APIs, removed methods, renamed classes). Flag these as needing extra validation.
   - A dependency pinned at an exact `<version>` (not a range) is the one place a transitive upgrade can get stuck — if bumping it directly is itself an ordinary upgrade (not forcing an unsupported combination), try that rather than marking Skipped. Mark Skipped only when the blocker is a BOM-managed transitive version you don't declare directly, or when bumping the pin fails validation.
4. Apply each upgrade by editing `pom.xml` directly. For version properties, use `mvn versions:set-property -Dproperty=<name> -DnewVersion=<version> -DprocessAllModules` as an alternative to manual XML edits — but verify the result looks correct before proceeding.
5. Run `mvn dependency:resolve -q` to verify the dependency graph resolves without conflicts after applying all changes.
6. Classify each Dependabot PR **and each Semgrep ticket** as:
   - **Upgraded**: version changed in pom.xml
   - **No-op**: version was already current or newer, or is a duplicate submodule PR covered by a root-pom change

     For tickets, see **Picking the target and matching results** under "Semgrep tickets" below — several tickets can share one dependency, and the match is per ticket.
   - **Skipped**: compatibility issue or unresolvable conflict
7. Verify completeness: every PR and every ticket from Step 1 must have a status (Upgraded, No-op, or Skipped). If any is unaccounted for, stop and report it before proceeding.

## Step 3: Validate

Run the build and test suite in order:

1. `mvn clean install`
   - This compiles all modules, runs checkstyle, runs unit tests, and runs integration tests
2. If step 1 fails, **attempt to fix the breaking change with code modifications before rolling back**. Common patterns for Java/Maven:
   - **Removed or renamed API** (e.g., a method removed in a major version): update call sites to use the new API
   - **Changed constructor or builder pattern**: adapt usage to the new API
   - **Checkstyle violations from new code patterns**: fix the style issue
   - **Compile errors from changed generics or type parameters**: adjust the type annotations
   - **Breaking changes in BouncyCastle** (crypto library): the API often changes significantly between major versions — check migration notes
   - **JUnit 5 → 6 migration**: junit-bom 6.x requires Java 17+ (class file version 61.0) — skip unless the project has already raised its minimum Java version to 17
3. Only roll back and mark as Skipped if:
   - The fix requires a large-scale migration across many files
   - The upgrade is blocked by an incompatible transitive dependency you cannot resolve
4. If a failure persists after investigation, roll back the upgrade and mark it as Skipped. Re-run validation until green.

## Step 4: Generate Outputs

Do NOT commit or create a PR. Instead, generate the following outputs for the human to use.

**Formatting for every generated markdown file:** one line per paragraph and one line per list item — never hard-wrap prose mid-sentence. Editors soft-wrap it anyway, and mid-paragraph breaks make later diffs noisy. Blank line between blocks, no trailing whitespace.

1. **Code changes note** — write a markdown file (`.claude/skills/batch-deps-upgrade/code-changes.md`) documenting every non-pom.xml source code change: what broke, why, and the minimal fix applied.

2. **Commit message** — output a concise commit message the human can copy-paste. Format: `chore(deps): quarterly batch dependency upgrade YYYY-QN` followed by a brief summary of upgrades, skips, and no-ops.

3. **PR description** — write a markdown file (`.claude/skills/batch-deps-upgrade/pr-description.md`) containing:
   - A summary section describing the batch upgrade
   - A "Superseded Dependabot PRs" table with columns: PR (linked), Package (`groupId:artifactId`), From, Asked for, Resolved, Status, MajorVersionUpgrade. The **Semgrep tickets** table uses these same columns, with the first headed `Ticket` — one shape for both artifacts.
     - `From` is the version on `main`; `Asked for` is what the PR proposed or the ticket requires (`≥ x.y.z`); `Resolved` is what the pom actually holds after the batch. Keep them in separate columns — a PR proposing one version can resolve to a higher one once a parent or BOM is bumped, and burying the real version in Status prose makes the table unverifiable.
     - Status values: Upgraded, No-op (reason), Skipped (reason: compatibility issue / build failure: error) — a status and its reason, never a version number.
     - MajorVersionUpgrade: `No` if the major version number did not change. Otherwise `Yes` plus a link for each major version crossed — link to the package's Maven Central release notes, GitHub releases page, or CHANGELOG.md. Verify each link returns HTTP 200 and has meaningful content (e.g., `curl -sL -o /dev/null -w "%{http_code}" <url>`); if a package doesn't publish per-version GitHub releases, fall back to its CHANGELOG.md file or the closest valid release tag.
   - Closing instructions with two paragraphs:
     1. "After merging, run `/batch-deps-upgrade close` to close the superseded PRs and the resolved Semgrep tickets." Follow it with the list of Upgraded and No-op PRs (#X, #Y, #Z) as a record, so the PR documents what will be closed even if the skill isn't used.
     2. "The following PRs were Skipped and should remain open: #A (`groupId:artifactId`), #B (`groupId:artifactId`), ..." — these stay open so Dependabot keeps rebasing them.
   - Include a **"Semgrep tickets"** table with the same columns, the first headed `Ticket`. Status alone cannot be verified against `main`, so `Asked for` and `Resolved` must be their own columns. Because `close-list.md` is not committed, this table is the closing run's fallback.
   - Give ticket-driven upgrades that no Dependabot PR proposed their own table naming the motivating ticket — they are additions, not supersessions.
   - Call out separately, in the overview, any upgrade taken only to unblock a ticket with no corresponding Dependabot PR: a reviewer will not be expecting it and must see it flagged rather than buried in a table.

4. **Close list** — write a markdown file (`.claude/skills/batch-deps-upgrade/close-list.md`) recording every ticket and Dependabot PR the batch makes closable. This is the input to the closing run.

   1. **Close** — every Upgraded and No-op ticket and PR, in one shape for both so a single parser handles them:

      ```
      <ticket-key or #pr> | <groupId:artifactId> | installed <resolved> (<asked for>) | Comment: "<text>"
      ```

      The third field must carry the **resolved** version, not the proposed one — that is what step 1 of the closing run checks against `main`, and a proposed version cannot be verified.

      Every comment must reference the batch PR, which does not exist yet at this point. Write that reference as the literal token `<PR>`; the closing run substitutes the merged PR's URL. Use a URL rather than `#1234`, which JIRA renders as plain text.
   2. **Left open** — every Skipped one, with its reason. Each is a security fix that did not land, so this is worth reading. Skipped PRs stay open for Dependabot to keep rebasing.

   **Do NOT commit `close-list.md`** — local scratch, like `code-changes.md` and `pr-description.md`. That is why the PR body must carry the same lists (item 3): it is the closing run's fallback when this file is gone.

## Semgrep tickets

Treat each in-scope ticket as one more row in Step 1's table: a `groupId:artifactId` plus a target version, carried through Steps 1-4 and classified Upgraded / No-op / Skipped like any Dependabot PR. Only the differences are below.

### Where the list comes from (Step 1)

Fetch open tickets via the Atlassian MCP:

```
parent = DGE-3869 AND project = DGE
AND status IN ("To Do", "in review", Blocked, "In Progress")
AND created >= "2022-01-01"
AND textfields ~ "xrpl4j"
ORDER BY rank
```

**Take the package name from the summary, not the description** — a description may list several packages sharing one advisory, so it picks the wrong one. Take the fix version, severity and CVE/GHSA link from the description. Wording varies, so read for intent rather than matching labels literally.

If a ticket names a package but no fix version, check the GitHub Advisory Database for that package (ecosystem `maven`, package `groupId:artifactId`) and take the "patched versions" field from the matching advisory instead of dropping the ticket.

Keep tickets naming a package and (directly or via its advisory) a fix version; drop the rest. If tickets came back but none of them could be parsed, that is a parsing failure — stop. If the query returned nothing, or nothing was in scope, there is simply no Semgrep work this quarter: record zero and carry on with the Dependabot batch. A ticket becomes a row whether or not a Dependabot PR proposes that package; a ticket is reason enough on its own. A ticket naming a package with no patched release at all (the advisory lists none) is left open for a human — note why in `close-list.md`'s "Left open" section rather than marking it Skipped alongside build failures.

### Picking the target and matching results (Step 2)

Where a ticket and a PR both want the same dependency, the target is the **highest** version either wants. Three matching rules — get them wrong and you close tickets whose vulnerable version is still in use:

- Compare versions numerically, dot-segment by dot-segment, never as strings — lexically `"1.9" > "1.10"`.
- Check the version actually resolved for that coordinate (`mvn dependency:tree -Dincludes=<groupId>:<artifactId>` or `mvn help:evaluate -Dexpression=<property>`), not just what you set in `pom.xml` — a BOM or another `<dependencyManagement>` entry can still win. One `groupId:artifactId` can resolve differently across modules if a submodule overrides it; match the install the ticket means, and if a module still resolves below the fix version, that module's row is not satisfied even if the root pom's is.
- Match per ticket, not per package. Tickets sharing one dependency can want different versions. Never conclude "we upgraded X, so close all the X tickets" without checking each ticket's own required version.

Classify from the Step 2 diff and resolved versions, not from which PR did what: a BOM or parent bump can carry along a dependency it pins exactly, so a ticket can come out Upgraded with no PR naming its package.

Write `close-list.md` — the closing run's input, so keep it parseable, one item per line.

### Non-goal

Never remove a dependency to resolve a finding. A transitive dep leaves only when its parent (or BOM) stops depending on it; a direct dep with no published fix needs whatever imported it rewritten, which belongs in a human-authored PR.

## Closing run (`/batch-deps-upgrade close`)

> **Runs only when `ARGUMENTS` is `close`.** If you reached this section by reading past Step 4 during a default run, stop here — everything below comments on and closes real JIRA tickets and real public PRs.

Runs none of Steps 1-4: no ticket discovery, no bumps, and none of Step 3's build/test chain. That exclusion does **not** cover the per-item check in step 1 below — that one always runs, and it is the safeguard against closing something whose fix was reverted. Read section 1 of `close-list.md`; if it is missing, fall back to the merged PR body's lists. Identify the batch PR from an argument or `gh pr list --repo XRPLF/xrpl4j --state merged --head <branch>`, and replace the `<PR>` token in every comment with its URL. **Check that no comment still contains `<PR>` before posting anything** — if one does, the substitution failed, so stop rather than post a placeholder onto dozens of tickets.

1. **Verify each item against the current `main`** and skip anything not genuinely satisfied — a reviewer may have had an upgrade reverted. This is what makes the run safe whether or not the batch has merged.
2. **Close everything that verified.** Do not ask for approval; the engineer reviewed both lists on the PR, and step 1 is the real check.
   - **JIRA tickets** — complete the `Done` transition first (query available transitions, take the one to `Done`), then post the comment. This avoids open-but-commented tickets if transition fails.
   - **Dependabot PRs** — `gh pr close <n> --repo XRPLF/xrpl4j --comment "<comment>"`.
3. Report in this shape:

   ```
   Closed <n> JIRA tickets, <n> Dependabot PRs.

   Skipped — not satisfied on main @ <sha> (<n>):
     <TICKET-KEY>   <groupId:artifactId> needs <version>, main has <version>
     #<pr-number>   <groupId:artifactId> <version> never applied (<reason from close-list>)
   ```

   If nothing was skipped, say so rather than omitting the section.
