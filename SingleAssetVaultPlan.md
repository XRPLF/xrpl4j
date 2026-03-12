# Single Asset Vault — Implementation Plan

*This document is a detailed, field-by-field plan for implementing the Single Asset Vault (XLS-65) amendment in xrpl4j.*

---

## General Rules

1. **Javadoc source**: All Javadoc comments on transaction interfaces, ledger object interfaces, and their fields MUST come directly from the XLS-65 specification text. Do not paraphrase or invent descriptions — copy the relevant sentence from the spec table's "Description" column.

2. **rippled is the source of truth for types**: The XLS spec defines the logical model; the rippled source 
   (`sfields.macro`, `transactions.macro`, `ledger_entries.macro`) defines the exact field names, serialized types, 
   and optionality. When in doubt, rippled wins. (/Users/rajp/Documents/code/rippled-single-asset-vault)

3. **Existing conventions**: Follow the Immutables pattern (`@Value.Immutable` on interfaces), `@JsonProperty` annotations matching the rippled field name exactly, and the same wrapper types already used in the codebase.

---

## Current Implementation Status

Some pieces are already implemented. If any of them do not conform to the detailed plan sections below (wrong types, 
wrong Javadoc, missing fields, etc.), they must be updated to match.

## VaultCreate
Let's write plan for VaultCreate transaction. Here are my thoughts:
1. We should create VaultData wrapper class for Data and perform similar checks as CredentialUri but for 256 bytes. 
   Add tests for it. Look into the existing tests for CredentialUri for inspiration.
2. Create a child Transaction Flag class for VaultCreateFlags. Add tests for it. Look at other transaction flags for 
   inspiration. We cannot batch VaultCreate so we won't have INNER_BATCH_TXN flag.
3. Asset is of type Issue. We should crate IouIssue, MptIssue and XrpIssue interfaces that extend Issue interface. 
   We will remove CurrencyIssue interface. It will be a breaking change. Fix the tests that arise due to this 
   breaking change. We might have to write deserializer for the Issue interface, since we want to create a concrete 
   type based on the contents of the JSON. (Correct me if I am wrong).
4. The IssueType in codec now can accept MPT issuance id. So we should fix that as well. rippled codebase is the 
   source of truth for serialization and deserialization.
5. AssetsMaximum is of type Number and we will have to add a new encoder and decoder for it in codec. rippled is the 
   source of truth for serialization and deserialization.
6. Now we want to test the codec changes that we made. How should we test it? We can call the sing and submit method 
   of rippled and then check if we get the correct tx blob and use it in our unit tests. But suggest me how to test 
   the binary serialization part.
7. Let's add the Preconditions tests for VaultCreate transaction based on the spec. We will only add those checks 
   that are possible without accessing the ledger. You can refer other transactions for inspiration.

We want to first get VaultCreate transaction right since it involves many binary codec changes. Once that is done we 
can move on to other transactions. Our focus should be on getting the codec right since it is the most important part of 
this implementation.
