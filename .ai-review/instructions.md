# Reviewing xrpl4j

xrpl4j is the canonical **Java SDK for the XRP Ledger** (modules `xrpl4j-core`, `-client`,
`-bom`, `-integration-tests`; built on Immutables + Jackson). It is a **financial primitive**:
amount, serialization, and signing bugs corrupt transactions or break consensus compatibility,
surfacing only on-ledger. **rippled is the source of truth** — verify wire shapes, optionality,
and flag values against it, not the XLS draft (drafts lag rippled).

## Amounts & numbers
- XRP drops are `UnsignedLong` + a separate `isNegative` flag (negative metadata deltas are
  real). `plus()`/`minus()` via signed long is intentional (the 1e17 cap is far below
  Long.MAX; results rebuild via `ofDrops()`, re-running the `@Value.Check` cap) — DO flag drops
  math that bypasses the `ofDrops`/`of` factories.
- Decimal conversion is exact: `ofXrp` uses `toBigIntegerExact()` — sub-drop values **throw,
  never round**; `toXrp` divides with `MathContext.DECIMAL128`. Flag double/float in amount
  paths, and `BigDecimal.divide` lacking a MathContext/RoundingMode.
- `IssuedCurrencyAmount`/`MptCurrencyAmount` `value()` is deliberately a raw String (IOU:
  16-digit mantissa, e−96..e80); range is validated by the codec at serialization, NOT the model. Don't
  demand BigDecimal typing or model validation; DO flag parsing these into double/float.

## Binary codec
- Canonical field sort happens ONLY in `STObjectType.fromJson` (by field ordinal); `fromParser`
  deliberately preserves wire order — flag encode paths using JSON/Map iteration order; don't
  flag `fromParser` for not sorting.
- UInt64 JSON radix is field-name-driven: hex by default; only names in
  `UInt64Type.BASE_10_UINT64_FIELD_NAMES` (MaximumAmount, OutstandingAmount, MPTAmount,
  LockedAmount) are base-10 — a new base-10 u64 field must be added there or misparses as hex.
- `BinaryParser` has no per-field bounds checks: a `fromParser` under-reading its width (or the VL
  sizeHint) silently misaligns every later field; the ObjectEndMarker is CALLER-emitted on both
  paths — new container code must replicate it.
- Base58 is the XRPL alphabet (`rpshnaf39w…`, zero byte = `r`), double-SHA256 checksum — never
  substitute a Bitcoin base58 library; the broad catch→false in `isValid*` is intentional.

## definitions.json
- A vendored snapshot (rippled `--get_definitions` / xrpl.js) — **no
  in-repo generator**; regenerate whole per `resources/README.md`, never hand-edit. A NEW
  top-level key breaks the whole codec at load — the same PR must extend `Definitions.java`.
- A serialized type name must be registered in `SerializedType.typeMap` — an unregistered
  `isSerialized:true` type throws only at first use; pseudo/`isSerialized:false` names are
  legitimately unmapped.
- `isSigningField` drives signing payloads (`encodeForSigning*` drops `false` fields,
  fail-closed); signature-bearing fields stay `false` — verify changes against rippled's sfields.

## Transaction models
- A new tx type = three sites: model interface, `TransactionType` constant with the exact rippled
  PascalCase wire string, and a `.put(ImmutableX.class, …)` in `Transaction.typeMap` (its
  inverse drives Jackson dispatch). Nothing fails at compile — missing typeMap = runtime NPE;
  missing enum = silent `UnknownTransaction`.
- Every model: `@Value.Immutable` + `@JsonSerialize`/`@JsonDeserialize(as = ImmutableX.class)` +
  static `builder()`. Required = bare abstract methods; optional = `Optional<T>`; lists = bare
  `List<T>` (defaults empty — not nullable); `@JsonProperty` = exact rippled casing. `fee()`
  is required with NO default; `signingPublicKey()` defaults to the empty multi-sign key.
- `flags()` is found by REFLECTION on that exact name — a misnamed accessor silently returns
  EMPTY; declare it `@JsonProperty("Flags") @Value.Default`. `@Value.Derived` on any JSON-mapped
  Transaction field corrupts `unknownFields()` round-trips — use `@Value.Default` (Derived is
  safe only with `@JsonIgnore`).
- Forward-compat: `TransactionType.forValue` → UNKNOWN, `LedgerObject` defaults to
  `UnknownLedgerObject` — never make these throw. Prefer typed wrappers in new APIs
  (`PublicKey`, `Unsigned*`, Flags subclasses).

## Flags
- Bit-31 constants need the `L` suffix: an unsuffixed `0x80000000` sign-extends to 32 phantom
  high bits and compiles. In `of(boolean...)` factories verify each ternary pairs its OWN
  constant — a wrong constant ships a validly-signed wrong transaction.
- `empty()` ≠ `of(0)`: EMPTY omits the Flags field from the wire; `of(0)`/UNSET serializes
  `Flags: 0`. Verify new flag values against XLS/rippled — low-bit values are legitimate in
  newer amendments.

## Immutables & Jackson
- TWO ObjectMappers with opposite strictness — never harmonize: the model mapper tolerates
  unknown fields (forward-compat); the binary-codec mapper fails on them (a dropped
  field changes signed bytes). Flag rippled-response parsing via the strict mapper.

## Keys
- A seed's algorithm comes ONLY from its base58 version prefix (`sEd…` vs `s…`); entropy bytes carry
  none; `Secp256k1KeyPairService` has no version guard — a mismatched seed silently
  derives a wrong-curve keypair. SHA-512(passphrase)→16 bytes is the standard derivation.
- Secret holders (Seed, PrivateKey, Entropy, Passphrase) are `Destroyable` with defensive copies
  and `[redacted]` toString — flag new secret classes or error paths exposing raw bytes. Key
  randomness comes only from `SecureRandomUtils.secureRandom()` — flag any other RNG there.

## Signing
- Each payload type maps to a DIFFERENT encoder in SignatureUtils — never unify: Transaction →
  `encodeForSigning`/`encodeForMultiSigning`; UnsignedClaim → `encodeForSigningClaim`;
  Attestation → plain `encode` (intentional); Batch inner → `encodeForBatchInner*`; LoanSet
  counterparty → `encodeForMultiSigningWithSigningPubKey` (keeps the pubkey).
- Multisign: signers sort by NUMERIC decoded AccountID (deliberately duplicated across sites —
  don't unify or use string compare). The per-signer suffix derives from the signer's
  SIGNING-key address, never `Account`/`Signer.account()`. An empty `SigningPubKey` is the multisig
  marker and the default — never a missing field.
- secp256k1 needs low-S normalization (`EcDsaSignature`'s `@Check` rejects s > N/2) and
  deterministic nonces via `HMacDSAKCalculator` — a bare `new ECDSASigner()` compiles.
  sha512-half is applied INSIDE `ecDsaSign` (pre-hashing double-hashes); ed25519 signs raw.

## Client
- Clients never sign. `submit()` binary-encodes to `tx_blob` (flag raw-JSON blobs);
  `submitMultisigned` sends `tx_json` by design. `JsonRpcClientErrorException` throws on
  `result.status == "error"`; tec/tem/ter results return in `SubmitResult` — don't flag callers
  inspecting the result instead of catching.
- Every client-module mapper comes from `ObjectMapperFactory.create()` — flag a bare
  `new ObjectMapper()` outside tests.

## Test conventions (do NOT flag)
- `snoPBrXtMeMyMHUVTgbuqAfg1SUTb` is the standalone genesis seed; keystore password "password"
  + `crypto.p12` is a documented public fixture. Codec tests are fixture-driven from the JS
  corpus (`codec-fixtures.json`, `data-driven-tests.json`) — vectors are canonical, never
  regenerate; new SerializedTypes and tx-serialization changes add cases there.
- ITs hit a Testcontainers rippled (env-gated via `-DuseTestnet` etc.; hardcoded mainnet
  addresses/counts are that mechanism); Awaitility polling ignores RuntimeException transients
  deliberately; no account cleanup by design. One `<Feature>IT` file per amendment — extend it.

## Contributor conventions
- JDK 1.8 floor: Java 9+ APIs compile locally but fail CI. Breaking public-API changes need a
  `V*_MIGRATION.md` entry (Before/After). Amendment-gated APIs carry `@Beta` + a javadoc naming
  the amendment, removed only at mainnet enablement; don't flag @Beta APIs as unstable.
