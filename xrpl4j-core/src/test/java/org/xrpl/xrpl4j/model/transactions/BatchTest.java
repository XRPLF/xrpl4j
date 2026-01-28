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
      .rawTransactions(createInnerTransactions(2))
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
    List<RawTransactionWrapper> oneTransaction = createInnerTransactions(1);

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
    List<RawTransactionWrapper> nineTransactions = createInnerTransactions(9);

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
    List<RawTransactionWrapper> transactions = createInnerTransactions(2);

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
    List<RawTransactionWrapper> transactions = createInnerTransactions(2);
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
    final RawTransactionWrapper invalidInnerBatchTransaction = RawTransactionWrapper.of(
      new Batch() {
        @Override
        public Address account() {
          return ACCOUNT_OTHER;
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
    transactions.add(createInnerTransactions(1).get(0));

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
    final List<RawTransactionWrapper> innerTransactions = createInnerTransactions(2);
    assertThatThrownBy(() -> Batch.builder()
      .account(innerTransactions.get(0).rawTransaction().account()) // <-- The crux of the test
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .flags(BatchFlags.ALL_OR_NOTHING)
      .rawTransactions(innerTransactions)
      .batchSigners(Lists.newArrayList(
        BatchSignerWrapper.of(BatchSigner.builder()
          .account(innerTransactions.get(0).rawTransaction().account())
          .signingPublicKey(innerTransactions.get(0).rawTransaction().signingPublicKey())
          .transactionSignature(Signature.fromBase16("ABCD"))
          .build()
        )))
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("The Account submitting a Batch transaction must not sign any inner transactions.");
  }

  @Test
  void testBatchWithRawTransactionMultiSignedBySubmitterAccount() {
    final List<RawTransactionWrapper> innerTransactions = createInnerTransactions(2);
    assertThatThrownBy(() -> Batch.builder()
      .account(innerTransactions.get(0).rawTransaction().account()) // <-- The crux of the test
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .flags(BatchFlags.ALL_OR_NOTHING)
      .rawTransactions(innerTransactions)
      .batchSigners(Lists.newArrayList(
        BatchSignerWrapper.of(
          BatchSigner.builder()
            .account(innerTransactions.get(1).rawTransaction().account()) // <-- The crux of the test
            .signers(Lists.newArrayList(
              SignerWrapper.of(Signer.builder()
                .account(innerTransactions.get(0).rawTransaction().account()) // <-- The crux of the test
                .signingPublicKey(innerTransactions.get(0).rawTransaction().signingPublicKey())
                .transactionSignature(Signature.fromBase16("ABCD"))
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
    List<RawTransactionWrapper> eightTransactions = createInnerTransactions(8);
    Batch batch = Batch.builder()
      .account(ACCOUNT)
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .flags(BatchFlags.ALL_OR_NOTHING)
      .rawTransactions(eightTransactions)
      .build();

    assertThat(batch.rawTransactions()).hasSize(8);
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
      .rawTransactions(createInnerTransactions(2))
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

  private List<RawTransactionWrapper> createInnerTransactions(int count) {
    return IntStream.range(0, count)
      .mapToObj(i -> RawTransactionWrapper.of(
        innerTransaction(UnsignedInteger.valueOf(i + 1))
      ))
      .collect(Collectors.toList());
  }
}
