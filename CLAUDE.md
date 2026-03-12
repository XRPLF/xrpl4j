# CLAUDE.md — Single Asset Vault Amendment (xrpl4j)

## 🚫 Git Rules
- NEVER run `git commit` under any circumstances.
- NEVER run `git push` under any circumstances.
- You may run `git add`, `git status`, `git diff`, and `git log` freely.
- All commits are made manually by the developer.

---

## 📁 Project Exploration

Do NOT assume any folder structure. Before writing any code, explore the repository yourself:
- Use `find`, `ls`, and `cat` to understand the module layout and package structure.
- Identify where existing transactions, ledger entries, RPC models, flags, and tests live.
- Let the actual codebase — not assumptions — guide where new files are placed.

---

## 🔍 Understanding What to Build — Study Past Amendments First

Before writing any code, use `git log` to study how past amendments were implemented.
Each amendment was squashed into a single commit. Reviewing these will tell you exactly
what files to create and what patterns to follow.

```bash
# List squashed amendment commits to identify relevant ones
git log --oneline --all | grep -iE "amm|clawback|did|vault|mpt|oracle"

# See all files changed in a given commit
git show --stat <commit-hash>

# See the full diff of a past amendment commit
git show <commit-hash>
```

**Amendments to study as reference (look these up in git log):**
- **AMMCreate / AMMDeposit / AMMWithdraw / AMMVote / AMMBid / AMMClawback** — closest analog;
  AMM introduced new transactions, a new ledger object, RPC methods, flags, and codec types.
- **MPTokenIssuance / MPTokenAuthorize** — another multi-transaction amendment for reference.
- **XChainBridge** — complex ledger object with nested fields, good serialization reference.

For each reference amendment, note:
1. What new transaction types were added and where they live.
2. What new ledger entry type was added.
3. What new RPC methods / response models were added.
4. How flags enums were defined.
5. How unit tests are structured for each.
6. How integration tests are structured.
7. What codec/serialization changes were needed.

---

## 🏗️ Repository Conventions — Follow These Exactly

Study the existing codebase to internalize its conventions before writing anything. Pay attention to:

- **Immutables pattern** — All models use `@Value.Immutable` on interfaces. Find an existing transaction (e.g., an AMM transaction) and follow its structure exactly: builder method, field declarations, `@JsonProperty` annotations, `@Value.Check` validations, and Jackson serializer/deserializer annotations.
- **Field types** — Use the same types already used in the project for amounts, addresses, hashes, and identifiers. Do not invent new types.
- **Flags** — Find how existing transaction and ledger entry flags are defined as enums and follow that pattern.
- **Polymorphic deserialization** — Find where existing transaction types are registered for polymorphic JSON deserialization and register new transactions the same way.
- **Codec / Binary serialization** — Check how existing field type codes are defined. If the Single Asset Vault amendment introduces new field types, update the codec following the same pattern used for AMM or MPToken fields. Cross-reference with the rippled source at the provided path.

---

## 🔬 What to Implement

Read the XLS specification (Single Asset Vault README.md) fully before writing any code. Use it along with the rippled source 
(/Users/rajp/Documents/code/rippled-single-asset-vault)
to determine the exact list of transactions, ledger entries, RPC methods, flags, and field names.

Do not guess field names or types — the rippled source (particularly `SField.cpp` and the
transaction/ledger entry `.cpp` files) is the source of truth for names, types, and optionality.

Implement everything the amendment introduces: all transactions, the ledger entry type,
any new RPC request/response models, flags, and any codec changes required.

---

## 🧪 Unit Tests — Follow Existing Patterns Exactly

Find the unit tests for an existing amendment (AMM is a good reference) and mirror their structure precisely. For every new model:

- Write a **fully populated** round-trip test: construct an object with every field set, serialize it to JSON, assert the JSON string matches the expected output, then deserialize it back and assert equality.
- Write a **minimally populated** round-trip test: only required fields set.
- Use hardcoded JSON strings for assertions — this is what catches missing or misnamed `@JsonProperty` annotations.
- Test any `@Value.Check` preconditions explicitly with cases that should throw.

Write unit tests immediately after each model — do not batch them at the end.

---

## 🔬 Integration Tests — Exercise All Fields

Find existing integration tests for a comparable amendment and follow their structure. Integration
tests run against a local rippled node in standalone mode.

For every new transaction type:
- Submit with only required fields — assert success.
- Submit with ALL optional fields set — assert the fields appear correctly in the ledger or metadata.
- Submit invalid input — assert the correct `tec`/`tem` error code is returned.
- Test each meaningful flag combination.

For the new ledger entry:
- Fetch it after creation and assert every field deserializes correctly.

For new RPC methods:
- Call the method and assert all response fields populate and deserialize correctly.
- Test error cases (e.g., object not found).

If new field types were introduced in the codec, add an explicit binary encode/decode round-trip test.

### Writing Readable Integration Tests

Integration tests are reviewed by humans — write them accordingly. Before writing any
integration tests, scan the existing integration test classes and find the ones with the
clearest, most descriptive comments. Use those as your style reference.

Rules for every integration test method:

- **Method names** should read like a sentence describing what is being verified.
  Prefer `canCreateVaultWithAllOptionalFields` over `testVaultCreate2`.
- **Opening comment** — each test method should start with a short paragraph explaining
  what scenario is being tested and why it matters (e.g., what bug it would catch if it failed).
- **Step comments** — use inline comments to label logical phases:
  `// Step 1: Fund accounts and wait for validation`
  `// Step 2: Submit VaultCreate with all optional fields set`
  `// Step 3: Fetch the resulting Vault ledger entry and verify all fields`
- **Assertion comments** — when asserting a non-obvious field, add a brief comment explaining
  what the value represents or why that specific value is expected.
- Keep each test focused on one scenario. Do not combine multiple unrelated assertions
  into a single test method just to save lines.

The goal is that a developer unfamiliar with the Single Asset Vault spec should be able to
read an integration test and understand exactly what is being exercised and why.

---

## 🔁 Development Workflow

1. Explore the repository structure — understand module layout, packages, and conventions.
2. Read the XLS spec fully.
3. Study past amendment commits via `git log` and `git show` as described above.
4. Read the rippled source at the provided path for exact field names, types, and error codes.
5. Implement in dependency order: **transactions → ledger entry → RPC → flags → codec**.
6. Write unit tests immediately after each model and run them before moving on.
7. Write integration tests last, after all unit tests pass.
8. Do NOT commit. Leave all changes for the developer to review and commit.

## 🛠️ Running Tests — No Permission Needed

You have full autonomy to run Maven commands at any time without asking. Do not ask for
permission before running tests — just run them.

Integration tests use Testcontainers and spin up their own rippled node automatically.
They are safe to run repeatedly as many times as needed.

Run tests frequently. Fix failures before moving on to the next model.

---

## ⚠️ General Rules
- Follow the existing code style exactly — do not reformat or refactor unrelated code.
- Do not introduce new dependencies unless they are already present in the project.
- If something in the spec is ambiguous, check the rippled source before asking.
- If still unclear, stop and ask rather than guessing.
- rippled source (/Users/rajp/Documents/code/rippled-single-asset-vault) is the ultimate source of truth.
- The Javadoc comments usually comes from the XLS spec.
--

## Pass 2

## My notes

Transactions:
VaultCreate
VaultSet
VaultDelete
VaultDeposit
VaultWithdraw
VaultClawback

Ledger Object:
Vault