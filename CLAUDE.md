# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Always Keep In Mind (Implementation / Bug Fix / Iteration Rules)

1. **Must** — Use JavaDoc comments from `Lending Protocol Spec.md` for everything. Follow the existing JavaDoc comment style in the codebase.
2. **Must** — For any transaction, ledger object, flags, signing etc, the rippled codebase is the source of truth. It can be found at `/Users/rajp/Documents/code/rippled-lending-protocol`.
3. **Must** - Write source code, write integration test that talks to Devnet for that source code (no need to run 
   entire test suite). Get the response and write JSON serialization/deserialization unit tests. So that we don't 
   make up anything. How to run a single integration test on Devnet is there in CLAUDE.md.

## Project Overview

xrpl4j is a 100% Java SDK for the XRP Ledger. Multi-module Maven project targeting JDK 1.8+.


The source of truth for the xrpl4j project is the README.md file in the root of the repository.

## Build Commands

```bash
mvn clean install                          # Full build with unit + integration tests
mvn clean install -DskipITs                # Skip integration tests (fastest useful build)
mvn clean install -DskipITs -DskipTests    # Skip all tests
```

Integration tests against specific networks:
```bash
mvn clean install -DuseDevnet              # Run ITs against Devnet
mvn clean install -DuseTestnet             # Run ITs against Testnet
```

Run a single test:
```bash
mvn test -pl xrpl4j-core -Dtest=PaymentTest
mvn verify -pl xrpl4j-integration-tests -Dit.test=SubmitPaymentIT -DskipTests
```

Checkstyle runs automatically during build. Line length limit is 120 characters.

## Module Structure

- **xrpl4j-core** — Core models, transaction types, binary codec, crypto (signing/verification), address codec. This is where most domain logic lives.
- **xrpl4j-client** — JSON-RPC client (`XrplClient`) for communicating with rippled nodes. Uses OkHttp3 + Feign.
- **xrpl4j-integration-tests** — Integration tests against live XRPL networks. Base class: `AbstractIT`. Test classes end with `IT`.
- **xrpl4j-bom** — Bill of Materials POM for downstream dependency management.

## Key Patterns

**Immutable models with Immutables library**: All transaction and ledger object types are interfaces annotated with `@Value.Immutable`. The annotation processor generates `ImmutableXxx` classes with builders. Always define a static `builder()` method returning `ImmutableXxx.Builder`.

```java
@Value.Immutable
@JsonSerialize(as = ImmutablePayment.class)
@JsonDeserialize(as = ImmutablePayment.class)
public interface Payment extends Transaction {
  static ImmutablePayment.Builder builder() {
    return ImmutablePayment.builder();
  }
}
```

**Jackson serialization**: JSON field names use PascalCase (XRPL convention). Use `@JsonProperty` annotations. Custom serializers/deserializers live in `org.xrpl.xrpl4j.model.jackson`.

**Transaction type hierarchy**: All transactions implement `Transaction`. Each has a corresponding `Flags` class and `TransactionType` enum entry. New transaction types need entries in `TransactionType`, the binary codec definitions, and Jackson polymorphic deserialization config.

**Crypto**: BouncyCastle handles Ed25519 and secp256k1 signing. Two modes: in-memory `PrivateKey` (`BcSignatureService`) or remote reference for HSM/KMS (`BcDerivedKeySignatureService`).

## Testing

- **Unit tests**: JUnit 5 + AssertJ + Mockito. Run via Surefire plugin.
- **Integration tests**: JUnit 5 + Failsafe plugin. Classes match `*IT` pattern. Get 2 automatic retries for flakiness. Use Testcontainers where needed.
- **Coverage**: JaCoCo with separate unit and integration reports.

# XRPL Protocol Definition Files

## 1. Transaction Fields + Required/Optional Status
**File:** `/Users/rajp/Documents/code/rippled-lending-protocol/include/xrpl/protocol/detail/transactions.macro`

Each transaction is defined using:
`TRANSACTION(tag, value, name, delegatable, amendments, privileges, fields)`

Fields use: `soeREQUIRED`, `soeOPTIONAL`, or `soeDEFAULT`

**Common fields shared by ALL transactions:**
`/Users/rajp/Documents/code/rippled-lending-protocol/src/libxrpl/protocol/TxFormats.cpp`

---

## 2. Data Types of Transaction Fields
**File:** `/Users/rajp/Documents/code/rippled-lending-protocol/include/xrpl/protocol/detail/sfields.macro`

Each field is defined using:
`TYPED_SFIELD(sfFieldName, TYPE, fieldCode)`

Available types: `UINT8`, `UINT16`, `UINT32`, `UINT64`, `UINT128`, `UINT256`,
`INT32`, `AMOUNT`, `NUMBER`, `ACCOUNT`, `VL`, `VECTOR256`, `OBJECT`, `ARRAY`,
`PATHSET`, `ISSUE`, `XCHAIN_BRIDGE`, `CURRENCY`

---

## 3. Ledger Object Fields + Required/Optional Status
**File:** `/Users/rajp/Documents/code/rippled-lending-protocol/include/xrpl/protocol/detail/ledger_entries.macro`

Each ledger object is defined using:
`LEDGER_ENTRY(type, typeCode, name, jsonName, fields)`

Fields use: `soeREQUIRED`, `soeOPTIONAL`, or `soeDEFAULT`

---

## 4. Data Types of Ledger Object Fields
**Same file as #2:**
`/Users/rajp/Documents/code/rippled-lending-protocol/include/xrpl/protocol/detail/sfields.macro`

---

## 5. Ledger Entry RPC Request Params
**File:** `/Users/rajp/Documents/code/rippled-lending-protocol/src/xrpld/rpc/handlers/LedgerEntry.cpp`

Contains the JSON parsing logic for each ledger entry type query,
showing which fields can be used to look up each ledger object via the `ledger_entry` RPC.

---

## Notes on Field Requirement Levels
| Value | Meaning |
|---|---|
| `soeREQUIRED` | Field must always be present |
| `soeOPTIONAL` | Field may be present; omitted if not set |
| `soeDEFAULT` | Field is optional; omitted from ledger when equal to its default (zero) value |

