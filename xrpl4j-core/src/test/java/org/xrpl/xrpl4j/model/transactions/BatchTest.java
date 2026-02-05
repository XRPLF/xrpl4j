package org.xrpl.xrpl4j.model.transactions;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.signing.Signature;
import org.xrpl.xrpl4j.model.flags.BatchFlags;
import org.xrpl.xrpl4j.model.flags.PaymentFlags;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Unit tests for {@link Batch}.
 */
public class BatchTest {

  private static final Address ACCOUNT = Seed.ed25519Seed().deriveKeyPair().publicKey().deriveAddress();
  private static final Address ACCOUNT_OTHER = Seed.ed25519Seed().deriveKeyPair().publicKey().deriveAddress();
  private static final Address DESTINATION = Seed.ed25519Seed().deriveKeyPair().publicKey().deriveAddress();

  @Test
  void testDefaultFlags() {
    Batch batch = Batch.builder()
      .account(ACCOUNT) // <-- The crux of the test
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .rawTransactions(createInnerTransactionsFromOuterSigner(2))
      .build();
    assertThat(batch.flags()).isEqualTo(BatchFlags.ALL_OR_NOTHING);
  }

  @Test
  void testBatchWithAllOrNothingMode() {
    Batch batch = createValidBatch(BatchFlags.ALL_OR_NOTHING);
    assertThat(batch.flags().tfAllOrNothing()).isTrue();
    assertThat(batch.rawTransactions()).hasSize(2);
  }

  @Test
  void testBatchWithOnlyOneMode() {
    Batch batch = createValidBatch(BatchFlags.ONLY_ONE);
    assertThat(batch.flags().tfOnlyOne()).isTrue();
  }

  @Test
  void testBatchWithUntilFailureMode() {
    Batch batch = createValidBatch(BatchFlags.UNTIL_FAILURE);
    assertThat(batch.flags().tfUntilFailure()).isTrue();
  }

  @Test
  void testBatchWithIndependentMode() {
    Batch batch = createValidBatch(BatchFlags.INDEPENDENT);
    assertThat(batch.flags().tfIndependent()).isTrue();
  }

  @Test
  void testBatchWithTooFewTransactions() {
    List<RawTransactionWrapper> oneTransaction = createInnerTransactionsFromOuterSigner(1);

    assertThatThrownBy(() -> Batch.builder()
      .account(ACCOUNT)
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .flags(BatchFlags.ALL_OR_NOTHING)
      .rawTransactions(oneTransaction)
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("RawTransactions must contain between 2 and 8 transactions");
  }

  @Test
  void testBatchWithTooManyTransactions() {
    List<RawTransactionWrapper> nineTransactions = createInnerTransactionsFromOuterSigner(9);

    assertThatThrownBy(() -> Batch.builder()
      .account(ACCOUNT)
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .flags(BatchFlags.ALL_OR_NOTHING)
      .rawTransactions(nineTransactions)
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("RawTransactions must contain between 2 and 8 transactions");
  }

  @Test
  void testBatchWithNoModeFlag() {
    List<RawTransactionWrapper> transactions = createInnerTransactionsFromOuterSigner(2);

    assertThatThrownBy(() -> Batch.builder()
      .account(ACCOUNT)
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .flags(BatchFlags.of(0L))
      .rawTransactions(transactions)
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Exactly one batch mode flag must be set");
  }

  @Test
  void testBatchWithMultipleModeFlags() {
    List<RawTransactionWrapper> transactions = createInnerTransactionsFromOuterSigner(2);
    // Combine ALL_OR_NOTHING and ONLY_ONE flags
    long combinedFlags = BatchFlags.ALL_OR_NOTHING.getValue() | BatchFlags.ONLY_ONE.getValue();

    assertThatThrownBy(() -> Batch.builder()
      .account(ACCOUNT)
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .flags(BatchFlags.of(combinedFlags))
      .rawTransactions(transactions)
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Exactly one batch mode flag must be set");
  }

  @Test
  void testBatchWithNestedBatch() {
    final Batch batch = createValidBatch(BatchFlags.ALL_OR_NOTHING);

    // Create an inner transaction (which should be rejected). To satisfy the preconditions in `RawTransactionWrapper`,
    // we need to create a transaction that has the `tfInnerBatchTxn` flag set.
    // Use ACCOUNT (the outer signer) as the nested batch's account to avoid triggering BatchSigners validation first.
    final RawTransactionWrapper invalidInnerBatchTransaction = RawTransactionWrapper.of(
      new Batch() {
        @Override
        public Address account() {
          return ACCOUNT;
        }

        @Override
        public BatchFlags flags() {
          return BatchFlags.of(BatchFlags.ALL_OR_NOTHING.getValue() | TransactionFlags.INNER_BATCH_TXN.getValue());
        }

        @Override
        public XrpCurrencyAmount fee() {
          return batch.fee();
        }

        @Override
        public Optional<UnsignedInteger> ticketSequence() {
          return Optional.empty();
        }

        @Override
        public Optional<Hash256> accountTransactionId() {
          return Optional.empty();
        }

        @Override
        public Optional<UnsignedInteger> lastLedgerSequence() {
          return Optional.empty();
        }

        @Override
        public List<MemoWrapper> memos() {
          return batch.memos();
        }

        @Override
        public List<SignerWrapper> signers() {
          return batch.signers();
        }

        @Override
        public Optional<UnsignedInteger> sourceTag() {
          return Optional.empty();
        }

        @Override
        public Optional<Signature> transactionSignature() {
          return Optional.empty();
        }

        @Override
        public Optional<NetworkId> networkId() {
          return Optional.empty();
        }

        @Override
        public Map<String, Object> unknownFields() {
          return batch.unknownFields();
        }

        @Override
        public List<RawTransactionWrapper> rawTransactions() {
          return batch.rawTransactions();
        }

        @Override
        public List<BatchSignerWrapper> batchSigners() {
          return batch.batchSigners();
        }
      }
    );

    List<RawTransactionWrapper> transactions = new ArrayList<>();
    transactions.add(invalidInnerBatchTransaction);
    transactions.add(createInnerTransactionsFromOuterSigner(1).get(0));

    assertThatThrownBy(() -> Batch.builder()
      .account(ACCOUNT)
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .flags(BatchFlags.ALL_OR_NOTHING)
      .rawTransactions(transactions)
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Batch transactions cannot be nested");
  }

  @Test
  void testBatchWithRawTransactionSignedBySubmitterAccount() {
    // Create inner transactions where both are from the same account (the outer signer)
    Address outerAccount = Seed.ed25519Seed().deriveKeyPair().publicKey().deriveAddress();
    PublicKey pubKey = Seed.ed25519Seed().deriveKeyPair().publicKey();
    final List<RawTransactionWrapper> innerTransactions = Lists.newArrayList(
      RawTransactionWrapper.of(createInnerPayment(outerAccount, UnsignedInteger.ONE)),
      RawTransactionWrapper.of(createInnerPayment(outerAccount, UnsignedInteger.valueOf(2)))
    );

    // The test checks that the outer signer cannot be in BatchSigners
    assertThatThrownBy(() -> Batch.builder()
      .account(outerAccount) // <-- The crux of the test
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .flags(BatchFlags.ALL_OR_NOTHING)
      .rawTransactions(innerTransactions)
      .batchSigners(Lists.newArrayList(
        BatchSignerWrapper.of(BatchSigner.builder()
          .account(outerAccount) // <-- Outer signer should not be in BatchSigners
          .signingPublicKey(pubKey)
          .transactionSignature(Signature.fromBase16("00112233"))
          .build()
        )))
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("The Account submitting a Batch transaction must not sign any inner transactions.");
  }

  @Test
  void testBatchWithRawTransactionMultiSignedBySubmitterAccount() {
    // Create inner transactions where both are from the same account (the outer signer)
    Address outerAccount = Seed.ed25519Seed().deriveKeyPair().publicKey().deriveAddress();
    PublicKey pubKey = Seed.ed25519Seed().deriveKeyPair().publicKey();
    final List<RawTransactionWrapper> innerTransactions = Lists.newArrayList(
      RawTransactionWrapper.of(createInnerPayment(outerAccount, UnsignedInteger.ONE)),
      RawTransactionWrapper.of(createInnerPayment(outerAccount, UnsignedInteger.valueOf(2)))
    );

    // The test checks that the outer signer cannot be in BatchSigners (even via multi-sig)
    assertThatThrownBy(() -> Batch.builder()
      .account(outerAccount) // <-- The crux of the test
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .flags(BatchFlags.ALL_OR_NOTHING)
      .rawTransactions(innerTransactions)
      .batchSigners(Lists.newArrayList(
        BatchSignerWrapper.of(
          BatchSigner.builder()
            .account(ACCOUNT_OTHER) // Different account for the BatchSigner
            .signers(Lists.newArrayList(
              SignerWrapper.of(Signer.builder()
                .account(outerAccount) // <-- Outer signer in nested Signers
                .signingPublicKey(pubKey)
                .transactionSignature(Signature.fromBase16("00112233"))
                .build()
              )
            )).build()
        )))
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("The Account submitting a Batch transaction must not sign any inner transactions.");
  }

  @Test
  void testBatchWithMaxTransactions() {
    List<RawTransactionWrapper> eightTransactions = createInnerTransactionsFromOuterSigner(8);
    Batch batch = Batch.builder()
      .account(ACCOUNT)
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .flags(BatchFlags.ALL_OR_NOTHING)
      .rawTransactions(eightTransactions)
      .build();

    assertThat(batch.rawTransactions()).hasSize(8);
  }

  @Test
  void testBatchWithMultipleAccountsAndMissingBatchSigners() {
    // Create inner transactions from two different accounts (neither is the outer signer)
    Address innerAccount1 = Seed.ed25519Seed().deriveKeyPair().publicKey().deriveAddress();
    Address innerAccount2 = Seed.ed25519Seed().deriveKeyPair().publicKey().deriveAddress();

    List<RawTransactionWrapper> transactions = Lists.newArrayList(
      RawTransactionWrapper.of(createInnerPayment(innerAccount1, UnsignedInteger.ONE)),
      RawTransactionWrapper.of(createInnerPayment(innerAccount2, UnsignedInteger.valueOf(2)))
    );

    // Should not fail because BatchSigners is empty.
    Batch batch = Batch.builder()
      .account(ACCOUNT)
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .flags(BatchFlags.ALL_OR_NOTHING)
      .rawTransactions(transactions)
      .build();
    assertThat(batch.batchSigners().isEmpty()).isTrue();
    assertThat(batch.transactionSignature().isPresent()).isFalse();
  }

  @Test
  void testBatchWithMultipleAccountsAndPartialBatchSigners() {
    // Create inner transactions from two different accounts (neither is the outer signer)
    Address innerAccount1 = Seed.ed25519Seed().deriveKeyPair().publicKey().deriveAddress();
    Address innerAccount2 = Seed.ed25519Seed().deriveKeyPair().publicKey().deriveAddress();
    PublicKey pubKey1 = Seed.ed25519Seed().deriveKeyPair().publicKey();

    List<RawTransactionWrapper> transactions = Lists.newArrayList(
      RawTransactionWrapper.of(createInnerPayment(innerAccount1, UnsignedInteger.ONE)),
      RawTransactionWrapper.of(createInnerPayment(innerAccount2, UnsignedInteger.valueOf(2)))
    );

    // Should fail because BatchSigners only contains signature from one account
    assertThatThrownBy(() -> Batch.builder()
      .account(ACCOUNT)
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .flags(BatchFlags.ALL_OR_NOTHING)
      .rawTransactions(transactions)
      .batchSigners(Lists.newArrayList(
        BatchSignerWrapper.of(BatchSigner.builder()
          .account(innerAccount1)
          .signingPublicKey(pubKey1)
          .transactionSignature(Signature.fromBase16("00112233"))
          .build()
        )))
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("BatchSigners must contain signatures from all accounts with inner transactions")
      .hasMessageContaining(innerAccount2.value());
  }

  @Test
  void testBatchWithMultipleAccountsAndAllBatchSigners() {
    // Create inner transactions from two different accounts (neither is the outer signer)
    Address innerAccount1 = Seed.ed25519Seed().deriveKeyPair().publicKey().deriveAddress();
    Address innerAccount2 = Seed.ed25519Seed().deriveKeyPair().publicKey().deriveAddress();
    PublicKey pubKey1 = Seed.ed25519Seed().deriveKeyPair().publicKey();
    PublicKey pubKey2 = Seed.ed25519Seed().deriveKeyPair().publicKey();

    List<RawTransactionWrapper> transactions = Lists.newArrayList(
      RawTransactionWrapper.of(createInnerPayment(innerAccount1, UnsignedInteger.ONE)),
      RawTransactionWrapper.of(createInnerPayment(innerAccount2, UnsignedInteger.valueOf(2)))
    );

    // Should succeed because BatchSigners contains signatures from both inner accounts
    Batch batch = Batch.builder()
      .account(ACCOUNT)
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .flags(BatchFlags.ALL_OR_NOTHING)
      .rawTransactions(transactions)
      .batchSigners(Lists.newArrayList(
        BatchSignerWrapper.of(BatchSigner.builder()
          .account(innerAccount1)
          .signingPublicKey(pubKey1)
          .transactionSignature(Signature.fromBase16("00112233"))
          .build()
        ),
        BatchSignerWrapper.of(BatchSigner.builder()
          .account(innerAccount2)
          .signingPublicKey(pubKey2)
          .transactionSignature(Signature.fromBase16("44556677"))
          .build()
        )))
      .build();

    assertThat(batch.batchSigners()).hasSize(2);
  }

  @Test
  void testBatchWithOuterSignerAsOnlyInnerAccount() {
    // Create inner transactions all from the outer signer account
    List<RawTransactionWrapper> transactions = Lists.newArrayList(
      RawTransactionWrapper.of(createInnerPayment(ACCOUNT, UnsignedInteger.ONE)),
      RawTransactionWrapper.of(createInnerPayment(ACCOUNT, UnsignedInteger.valueOf(2)))
    );

    // Should succeed because all inner transactions are from the outer signer (no BatchSigners needed)
    Batch batch = Batch.builder()
      .account(ACCOUNT)
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .flags(BatchFlags.ALL_OR_NOTHING)
      .rawTransactions(transactions)
      .build();

    assertThat(batch.batchSigners()).isEmpty();
  }

  @Test
  void testBatchWithOuterSignerAndOtherAccountWithBatchSigner() {
    // Create inner transactions from outer signer and one other account
    Address innerAccount = Seed.ed25519Seed().deriveKeyPair().publicKey().deriveAddress();
    PublicKey pubKey = Seed.ed25519Seed().deriveKeyPair().publicKey();

    List<RawTransactionWrapper> transactions = Lists.newArrayList(
      RawTransactionWrapper.of(createInnerPayment(ACCOUNT, UnsignedInteger.ONE)),
      RawTransactionWrapper.of(createInnerPayment(innerAccount, UnsignedInteger.valueOf(2)))
    );

    // Should succeed because BatchSigners contains signature from the non-outer-signer account
    Batch batch = Batch.builder()
      .account(ACCOUNT)
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .flags(BatchFlags.ALL_OR_NOTHING)
      .rawTransactions(transactions)
      .batchSigners(Lists.newArrayList(
        BatchSignerWrapper.of(BatchSigner.builder()
          .account(innerAccount)
          .signingPublicKey(pubKey)
          .transactionSignature(Signature.fromBase16("00112233"))
          .build()
        )))
      .build();

    assertThat(batch.batchSigners()).hasSize(1);
  }

  @Test
  void testBatchWithOuterSignerAndOtherAccountMissingBatchSigner() {
    // Create inner transactions from outer signer and one other account
    Address innerAccount = Seed.ed25519Seed().deriveKeyPair().publicKey().deriveAddress();

    List<RawTransactionWrapper> transactions = Lists.newArrayList(
      RawTransactionWrapper.of(createInnerPayment(ACCOUNT, UnsignedInteger.ONE)),
      RawTransactionWrapper.of(createInnerPayment(innerAccount, UnsignedInteger.valueOf(2)))
    );

    // Should fail because BatchSigners is missing for the non-outer-signer account
    Batch batch = Batch.builder()
      .account(ACCOUNT)
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .flags(BatchFlags.ALL_OR_NOTHING)
      .rawTransactions(transactions)
      .build();

    assertThat(batch.batchSigners().isEmpty()).isTrue();
    assertThat(batch.transactionSignature().isPresent()).isFalse();
  }

  @Test
  void testBatchWithExtraBatchSignerNotInInnerTransactions() {
    // Create inner transactions from one account (not the outer signer)
    Address innerAccount = Seed.ed25519Seed().deriveKeyPair().publicKey().deriveAddress();
    Address extraAccount = Seed.ed25519Seed().deriveKeyPair().publicKey().deriveAddress();
    PublicKey pubKey1 = Seed.ed25519Seed().deriveKeyPair().publicKey();
    PublicKey pubKey2 = Seed.ed25519Seed().deriveKeyPair().publicKey();

    List<RawTransactionWrapper> transactions = Lists.newArrayList(
      RawTransactionWrapper.of(createInnerPayment(innerAccount, UnsignedInteger.ONE)),
      RawTransactionWrapper.of(createInnerPayment(innerAccount, UnsignedInteger.valueOf(2)))
    );

    // Should succeed even though BatchSigners contains an extra account that has no inner transactions.
    // The validation only checks that all required accounts have signatures, not that all signers are required.
    Batch batch = Batch.builder()
      .account(ACCOUNT)
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .flags(BatchFlags.ALL_OR_NOTHING)
      .rawTransactions(transactions)
      .batchSigners(Lists.newArrayList(
        BatchSignerWrapper.of(BatchSigner.builder()
          .account(innerAccount)
          .signingPublicKey(pubKey1)
          .transactionSignature(Signature.fromBase16("00112233"))
          .build()
        ),
        // This can occur in cases where a regular key has been changed.
        BatchSignerWrapper.of(BatchSigner.builder()
          .account(extraAccount) // Extra signer not in any inner transaction
          .signingPublicKey(pubKey2)
          .transactionSignature(Signature.fromBase16("44556677"))
          .build()
        )))
      .build();

    assertThat(batch.batchSigners()).hasSize(2);
  }

  // ///////////////
  // Private Helpers
  // ///////////////

  private Batch createValidBatch(BatchFlags batchFlags) {
    return Batch.builder()
      .account(ACCOUNT)
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .flags(batchFlags)
      .rawTransactions(createInnerTransactionsFromOuterSigner(2))
      .build();
  }

  private Payment innerTransaction(UnsignedInteger sequence) {
    return Payment.builder()
      .account(Seed.ed25519Seed().deriveKeyPair().publicKey().deriveAddress())
      .destination(DESTINATION)
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(sequence)
      .amount(XrpCurrencyAmount.ofDrops(1000))
      .flags(PaymentFlags.INNER_BATCH_TXN)
      .build();
  }

  private Payment createInnerPayment(Address account, UnsignedInteger sequence) {
    return Payment.builder()
      .account(account)
      .destination(DESTINATION)
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(sequence)
      .amount(XrpCurrencyAmount.ofDrops(1000))
      .flags(PaymentFlags.INNER_BATCH_TXN)
      .build();
  }

  private List<RawTransactionWrapper> createInnerTransactions(int count) {
    return IntStream.range(0, count)
      .mapToObj(i -> RawTransactionWrapper.of(
        innerTransaction(UnsignedInteger.valueOf(i + 1))
      ))
      .collect(Collectors.toList());
  }

  private List<RawTransactionWrapper> createInnerTransactionsFromOuterSigner(int count) {
    return IntStream.range(0, count)
      .mapToObj(i -> RawTransactionWrapper.of(
        createInnerPayment(ACCOUNT, UnsignedInteger.valueOf(i + 1))
      ))
      .collect(Collectors.toList());
  }
}
