# xrpl4j-integration-tests
Contains a suite of integration tests which use the xrpl4j modules to perform XRPL flows on the Testnet.

## Tests
### [AccountSetIT](./src/test/java/org/xrpl/xrpl4j/tests/AccountSetIT.java)
- Uses [`AccountSet`](../xrpl4j-core/src/main/java/model/transactions/AccountSet.java) transactions to enable and disable all 
[account flags](https://xrpl.org/accountroot.html#accountroot-flags) on a random Testnet account

### [CheckIT](./src/test/java/org/xrpl/xrpl4j/tests/CheckIT.java)
- Creates [Checks](https://xrpl.org/checks.html#checks) between two accounts using 
[`CheckCreate`](../xrpl4j-core/src/main/java/model/transactions/CheckCreate.java) transactions 
and cancels or cashes those [Checks]() using [`CheckCancel`](../xrpl4j-core/src/main/java/model/transactions/CheckCancel.java) and 
[`CheckCash`](../xrpl4j-core/src/main/java/model/transactions/CheckCash.java) transactions

### [DepositPreAuth](./src/test/java/org/xrpl/xrpl4j/tests/DepositPreAuthIT.java)
- Shows how to enable [Deposit Preauthorization](https://xrpl.org/depositauth.html#deposit-authorization) and grant authorization to other accounts using the 
[`DepositPreAuth`](../xrpl4j-core/src/main/java/model/transactions/DepositPreAuth.java) transaction

### [EscrowIT](./src/test/java/org/xrpl/xrpl4j/tests/EscrowIT.java)
- Creates, cancels, and finishes time-based and condition-based [Escrows](https://xrpl.org/escrow.html#escrow) using 
[`EscrowCreate`](../xrpl4j-core/src/main/java/model/transactions/EscrowCreate.java), 
[`EscrowFinish`](../xrpl4j-core/src/main/java/model/transactions/EscrowFinish.java), and 
[`EscrowCancel`](../xrpl4j-core/src/main/java/model/transactions/EscrowCancel.java) transactions

### [IssuedCurrencyIT](./src/test/java/org/xrpl/xrpl4j/tests/IssuedCurrencyIT.java)
- Issues a currency on the Testnet using [Trustlines](https://xrpl.org/trust-lines-and-issuing.html#trust-lines-and-issuing) and shows how to send 
that issued currency across multiple hops using [rippling](https://xrpl.org/rippling.html)

### [OfferIT](./src/test/java/org/xrpl/xrpl4j/tests/OfferIT.java)
- Shows example usage of the [XRPL DEX](https://xrpl.org/decentralized-exchange.html)

### [SetRegularKeyIT](./src/test/java/org/xrpl/xrpl4j/tests/SetRegularKeyIT.java)
- Sets the regular key using a newly-generated keypair using the [`SetRegularKey`](../xrpl4j-core/src/main/java/model/transactions/SetRegularKey.java)
transaction

### [SignerListSet](./src/test/java/org/xrpl/xrpl4j/tests/SignerListSetIT.java)
- Enables a [multisig account](https://xrpl.org/multi-signing.html#multi-signing) by adding signers to an account's 
[signer list](https://xrpl.org/multi-signing.html#signer-lists)

### [SubmitPaymentIT](./src/test/java/org/xrpl/xrpl4j/tests/SubmitPaymentIT.java)
- Shows how to send an XRP payment using both an Ed25519 and secp256k1 wallet
