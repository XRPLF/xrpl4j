# XLS-0068 Extracted Field Codes from rippled Fork

**Source**: `XRPL-fork-sponsor/rippled`  
**Date Extracted**: 2026-03-18

## Transaction Types

| Name | Code | Source |
|------|------|--------|
| SponsorshipTransfer | 85 | `transactions.macro` line 1083 |
| SponsorshipSet | 86 | `transactions.macro` line 1096 |

## Ledger Entry Types

| Name | Code (Hex) | Code (Dec) | Source |
|------|------------|------------|--------|
| Sponsorship | 0x0090 | 144 | `ledger_entries.macro` line 614 |

## Fields - Common Transaction Fields

| Field Name | Type | nth | isSigningField | Source |
|------------|------|-----|----------------|--------|
| Sponsor | AccountID (8) | 27 | true | `sfields.macro` line 335 |
| SponsorFlags | UInt32 (2) | 73 | true | `sfields.macro` line 121 |
| SponsorSignature | STObject (14) | 38 | **false** | `sfields.macro` line 404 |
| ObjectID | Hash256 (5) | 39 | true | `sfields.macro` line 213 |

## Fields - AccountRoot

| Field Name | Type | nth | Source |
|------------|------|-----|--------|
| SponsoredOwnerCount | UInt32 (2) | 69 | `sfields.macro` line 117 |
| SponsoringOwnerCount | UInt32 (2) | 70 | `sfields.macro` line 118 |
| SponsoringAccountCount | UInt32 (2) | 71 | `sfields.macro` line 119 |

## Fields - RippleState

| Field Name | Type | nth | Source |
|------------|------|-----|--------|
| HighSponsor | AccountID (8) | 28 | `sfields.macro` line 336 |
| LowSponsor | AccountID (8) | 29 | `sfields.macro` line 337 |

## Fields - Sponsorship Object

| Field Name | Type | nth | Source |
|------------|------|-----|--------|
| Owner | AccountID (8) | 2 | `sfields.macro` line 312 |
| Sponsee | AccountID (8) | 31 | `sfields.macro` line 339 |
| CounterpartySponsor | AccountID (8) | 30 | `sfields.macro` line 338 |
| FeeAmount | Amount (6) | 32 | `sfields.macro` line 273 |
| MaxFee | Amount (6) | 33 | `sfields.macro` line 274 |
| ReserveCount | UInt32 (2) | 72 | `sfields.macro` line 120 |
| OwnerNode | UInt64 (3) | 4 | `sfields.macro` line 127 |
| SponseeNode | UInt64 (3) | 32 | `sfields.macro` line 155 |

## Type Codes Reference

For `definitions.json`, the type codes are:
- UInt32 = 2
- UInt64 = 3
- Hash256 = 5
- Amount = 6
- AccountID = 8
- STObject = 14

## Important Notes

1. **SponsorSignature has `isSigningField: false`** - This is because it's marked with `SField::notSigning` in the rippled source.

2. **Full Spec Implemented** - The rippled fork implements BOTH sponsorship models:
   - Co-signing model (SponsorshipTransfer)
   - Pre-funded model (SponsorshipSet + Sponsorship ledger object)

3. **Current xrpl4j Gap** - The current xrpl4j implementation only has the co-signing model. To match the rippled fork, we would need to add:
   - `SponsorshipSet` transaction class
   - `Sponsorship` ledger object class
   - Additional fields: `CounterpartySponsor`, `FeeAmount`, `MaxFee`, `ReserveCount`, `OwnerNode`, `SponseeNode`
   - AccountRoot fields: `SponsoredOwnerCount`, `SponsoringOwnerCount`, `SponsoringAccountCount`
   - RippleState fields: `HighSponsor`, `LowSponsor`

4. **Immediate Action** - At minimum, update `definitions.json` with the fields currently used by xrpl4j:
   - Transaction types: SponsorshipTransfer (85)
   - Fields: Sponsor (27), SponsorFlags (73), SponsorSignature (38), ObjectID (39)

5. **Future Consideration** - Decide whether to implement the full XLS-0068 spec to match the rippled fork capabilities.

