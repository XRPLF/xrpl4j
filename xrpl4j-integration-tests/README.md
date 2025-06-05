# xrpl4j-integration-tests

Contains a suite of integration tests which use the xrpl4j modules to perform XRPL flows on the Testnet.

## Tests

### [AccountSetIT](./src/test/java/org/xrpl/xrpl4j/tests/AccountSetIT.java)

- Uses [`AccountSet`](../xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/transactions/AccountSet.java) transactions to
  enable and disable all [account flags](https://xrpl.org/accountroot.html#accountroot-flags) on a random Testnet
  account.

### [AccountTransactionsIT](./src/test/java/org/xrpl/xrpl4j/tests/AccountTransactionsIT.java)

- Uses [`AccountTransactionsResult`](../xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/client/accounts/AccountTransactionsResult.java)
to load data from rippled.

### [CheckIT](./src/test/java/org/xrpl/xrpl4j/tests/CheckIT.java)

- Creates [Checks](https://xrpl.org/checks.html#checks) between two accounts using
  [`CheckCreate`](../xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/transactions/CheckCreate.java) transactions
  and cancels or cashes those [Checks]()
  using [`CheckCancel`](../xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/transactions/CheckCancel.java) and
  [`CheckCash`](../xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/transactions/CheckCash.java) transactions

### [DepositPreAuth](./src/test/java/org/xrpl/xrpl4j/tests/DepositPreAuthIT.java)

- Shows how to enable [Deposit Preauthorization](https://xrpl.org/depositauth.html#deposit-authorization) and grant
  authorization to other accounts using the
  [`DepositPreAuth`](../xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/transactions/DepositPreAuth.java) transaction

### [EscrowIT](./src/test/java/org/xrpl/xrpl4j/tests/EscrowIT.java)

- Creates, cancels, and finishes time-based and condition-based [Escrows](https://xrpl.org/escrow.html#escrow) using
  [`EscrowCreate`](../xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/transactions/EscrowCreate.java),
  [`EscrowFinish`](../xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/transactions/EscrowFinish.java), and
  [`EscrowCancel`](../xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/transactions/EscrowCancel.java) transactions

### [FreezeIssuedCurrencyIT](./src/test/java/org/xrpl/xrpl4j/tests/FreezeIssuedCurrencyIT.java)

- Validates that an issued currency can be frozen at both the individual account level and the global (i.e.,
  ledger-wide) level using a [`TrustSet`](../xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/transactions/TrustSet.java)
  transaction.

### [GatewayBalancesIT](./src/test/java/org/xrpl/xrpl4j/tests/GatewayBalancesIT.java)

- Validates that the library can properly request and receive Gateway balances
  using [`GatewayBalancesRequestParams`](../xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/client/accounts/GatewayBalancesRequestParams.java)
  and [`GatewayBalancesResult`](../xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/client/accounts/GatewayBalancesResult.java)
  transactions.

### [IsFinalIT](./src/test/java/org/xrpl/xrpl4j/tests/IsFinalIT.java)

- Check if a transaction is "final" on the ledger or not,
  using [`XrplClient#isFinal`](../xrpl4j-client/src/main/java/org/xrpl/xrpl4j/client/XrplClient.java#L323).

### [IssuedCurrencyIT](./src/test/java/org/xrpl/xrpl4j/tests/IssuedCurrencyIT.java)

- Issues a currency on the Testnet
  using [Trustlines](https://xrpl.org/trust-lines-and-issuing.html#trust-lines) and shows how to send
  that issued currency across multiple hops using [rippling](https://xrpl.org/rippling.html)

### [LedgerResultIT](./src/test/java/org/xrpl/xrpl4j/tests/LedgerResultIT.java)

- Validates that the library can read `CLOSED`, `VALIDATED`, and `CURRENT` ledgers.

### [NfTokenIT](./src/test/java/org/xrpl/xrpl4j/tests/NfTokenIT.java)

- Validates all on-ledger NFT functionality including minting, burning, and trading.

### [OfferIT](./src/test/java/org/xrpl/xrpl4j/tests/OfferIT.java)

- Shows example usage of the [XRPL DEX](https://xrpl.org/decentralized-exchange.html)

### [PaymentChannelIT](./src/test/java/org/xrpl/xrpl4j/tests/PaymentChannelIT.java)

- Validates that the library can create and cloase Payment Channels, as well as claim value from them.

### [ServerInfoIT](./src/test/java/org/xrpl/xrpl4j/tests/ServerInfoIT.java)

- Validates that the library can read an XRPL node's server information.

### [SetRegularKeyIT](./src/test/java/org/xrpl/xrpl4j/tests/SetRegularKeyIT.java)

- Sets the regular key using a newly-generated keypair using
  the [`SetRegularKey`](../xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/transactions/SetRegularKey.java)
  transaction

### [SignerListSet](./src/test/java/org/xrpl/xrpl4j/tests/SignerListSetIT.java)

- Enables a [multisig account](https://xrpl.org/multi-signing.html#multi-signing) by adding signers to an account's
  [signer list](https://xrpl.org/multi-signing.html#signer-lists)

### [SubmitPaymentIT](./src/test/java/org/xrpl/xrpl4j/tests/SubmitPaymentIT.java)

- Validates that the library can send an XRP payments using both an Ed25519 and secp256k1 based accounts.

### [SubmitMultisignedIT](./src/test/java/org/xrpl/xrpl4j/tests/SubmitMultisignedIT.java)

- Validates that the library can construct and submit a [multisigned](https://xrpl.org/multi-signing.html) XRPL
  transaction.

### [TicketIT](./src/test/java/org/xrpl/xrpl4j/tests/TicketIT.java)

- Validates that the library can construct and submit a [ticket](https://xrpl.org/tickets.html#tickets) transactions.

### [TransactionWithMemoIT](./src/test/java/org/xrpl/xrpl4j/tests/TransactionWithMemoIT.java)

- Validates that the library can properly and submit transactions with
  a [memo](https://xrpl.org/transaction-common-fields.html#memos-field).

### [TransactUsingDerivedKeySignatureServiceIT](./src/test/java/org/xrpl/xrpl4j/tests/TransactUsingDerivedKeySignatureServiceIT.java)

- Validates that the library can sign and validate transactions using
  a [`SignatureService<PrivateKeyReference>`](../xrpl4j-core/src/main/java/org/xrpl/xrpl4j/crypto/signing/SignatureService.java)
  that can derive private key information from an in-memory secret combined with a supplied unique identifier for key
  derivation.

### [TransactUsingSignatureServiceIT](./src/test/java/org/xrpl/xrpl4j/tests/TransactUsingSignatureServiceIT.java)

- Validates that the library can sign and validate transactions using
  a [`SignatureService`](../xrpl4j-core/src/main/java/org/xrpl/xrpl4j/crypto/signing/SignatureService.java)
  that uses in-memory private key material.
