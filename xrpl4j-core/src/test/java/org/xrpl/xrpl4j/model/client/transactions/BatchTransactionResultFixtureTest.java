package org.xrpl.xrpl4j.model.client.transactions;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2026 XRPL Foundation and its contributors
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.flags.BatchFlags;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Batch;
import org.xrpl.xrpl4j.model.transactions.BatchSignerWrapper;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.TransactionMetadata;
import org.xrpl.xrpl4j.model.transactions.TransactionResultCodes;
import org.xrpl.xrpl4j.model.transactions.TransactionType;
import org.xrpl.xrpl4j.model.transactions.TrustSet;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaAccountRootObject;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaLedgerEntryType;

import java.io.File;
import java.io.IOException;

/**
 * Verifies that real {@code tx} RPC responses for validated {@link Batch} transactions (as emitted by xrpld on Testnet)
 * can be deserialized into a {@link TransactionResult} and round-tripped through the xrpl4j {@link ObjectMapper}.
 *
 * <p>Each fixture in {@code src/test/resources/batch-transactions} is the unmodified {@code result} object of a
 * {@code tx} call against Testnet, named {@code tx-result-<ledger_index>-<first 8 hex of hash>.json}.
 */
class BatchTransactionResultFixtureTest {

  private static final String FIXTURE_DIR = "src/test/resources/batch-transactions/";

  private static final File FIXTURE = new File(FIXTURE_DIR + "tx-result-21670276-02807423.json");

  private static final Address ACCOUNT = Address.of("rN7Evm6UwmMnbbN6USosRmJwGACsTvX359");
  private static final Address ISSUER = Address.of("rp991ugoTpwiEyVTY2mQVJkpRpDWTJkpaq");

  private final ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void deserializeBatchTransactionResultFromTxRpcResponse() throws IOException {
    TransactionResult<Batch> result = objectMapper.readValue(
      FIXTURE, new TypeReference<TransactionResult<Batch>>() {}
    );

    // Outer envelope
    assertThat(result.hash())
      .isEqualTo(Hash256.of("02807423A24CA1298A0ADCB078687C3168CE8FDAE03101B75413A8AE78A8DD90"));
    assertThat(result.ledgerIndex()).hasValue(LedgerIndex.of(UnsignedInteger.valueOf(21670276)));
    assertThat(result.closeDate()).hasValue(UnsignedLong.valueOf(845771850));
    assertThat(result.validated()).isTrue();

    // Outer Batch transaction
    Batch batch = result.transaction();
    assertThat(batch.transactionType()).isEqualTo(TransactionType.BATCH);
    assertThat(batch.account()).isEqualTo(ACCOUNT);
    assertThat(batch.fee()).isEqualTo(XrpCurrencyAmount.ofDrops(40));
    assertThat(batch.sequence()).isEqualTo(UnsignedInteger.valueOf(21670266));
    assertThat(batch.lastLedgerSequence()).hasValue(UnsignedInteger.valueOf(21670304));
    assertThat(batch.flags()).isEqualTo(BatchFlags.INDEPENDENT);
    assertThat(batch.flags().tfIndependent()).isTrue();
    assertThat(batch.signingPublicKey().base16Value())
      .isEqualTo("EDA9DDF5561CD6786C2266159E7F9B6BC7DCACD389FD1C37A014115FB6F8D327D1");
    assertThat(batch.transactionSignature()).isPresent();
    assertThat(batch.batchSigners()).isEmpty();

    // Inner transactions
    assertThat(batch.rawTransactions()).hasSize(2);
    assertInnerTrustSet(batch.rawTransactions().get(0).rawTransaction(), "RW1", 21670267);
    assertInnerTrustSet(batch.rawTransactions().get(1).rawTransaction(), "RW2", 21670268);

    // Metadata
    assertThat(result.metadata()).isPresent();
    TransactionMetadata meta = result.metadata().get();
    assertThat(meta.transactionResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(meta.transactionIndex()).isEqualTo(UnsignedInteger.valueOf(4));
    assertThat(meta.affectedNodes()).hasSize(1);
    meta.affectedNodes().get(0).handle(
      createdNode -> {
        throw new AssertionError("Expected a ModifiedNode");
      },
      modifiedNode -> {
        assertThat(modifiedNode.ledgerEntryType()).isEqualTo(MetaLedgerEntryType.ACCOUNT_ROOT);
        assertThat(modifiedNode.ledgerIndex())
          .isEqualTo(Hash256.of("414ED162D3D9378F0565EB37F454032B584DF045B68F42EB89B8D9F09BB3FE33"));
        assertThat(modifiedNode.previousTransactionsLedgerSequence())
          .hasValue(LedgerIndex.of(UnsignedInteger.valueOf(21670266)));
        assertThat(modifiedNode.previousTransactionId())
          .hasValue(Hash256.of("AFFED63B62E8991870E0874A0156683E09C3A9628E4B9A44055FCA38AA1311E6"));
        MetaAccountRootObject finalFields = (MetaAccountRootObject) modifiedNode.finalFields().get();
        assertThat(finalFields.balance()).hasValue(XrpCurrencyAmount.ofDrops(99999960));
        assertThat(finalFields.sequence()).hasValue(UnsignedInteger.valueOf(21670267));
        MetaAccountRootObject previousFields = (MetaAccountRootObject) modifiedNode.previousFields().get();
        assertThat(previousFields.balance()).hasValue(XrpCurrencyAmount.ofDrops(100000000));
        assertThat(previousFields.sequence()).hasValue(UnsignedInteger.valueOf(21670266));
      },
      deletedNode -> {
        throw new AssertionError("Expected a ModifiedNode");
      }
    );
  }

  @Test
  void deserializeBatchTransactionResultAsGenericTransaction() throws IOException {
    TransactionResult<Transaction> result = objectMapper.readValue(
      FIXTURE, new TypeReference<TransactionResult<Transaction>>() {}
    );

    assertThat(result.transaction()).isInstanceOf(Batch.class);
    assertThat(((Batch) result.transaction()).rawTransactions()).hasSize(2);
  }

  /**
   * Every captured Batch transaction deserializes (typed and generic), satisfies the structural invariants of a
   * validated Batch, and survives a serialize/deserialize round trip.
   */
  @ParameterizedTest
  @ValueSource(strings = {
    "tx-result-21670012-565E70B3.json", // AllOrNothing, 3 Payments (XRP, IOU, MPT), 1 multi-signed BatchSigner
    "tx-result-21670068-B93223CE.json", // AllOrNothing, 2 Payments, 1 BatchSigner
    "tx-result-21670074-BBA1B829.json", // AllOrNothing, 2 Payments, 1 BatchSigner
    "tx-result-21670080-798B9F5C.json", // AllOrNothing, 2 Payments, 1 BatchSigner
    "tx-result-21670126-BB84DB4F.json", // AllOrNothing, 3 Payments, 1 BatchSigner
    "tx-result-21670169-553E3A16.json", // AllOrNothing, 2 Payments, 1 BatchSigner
    "tx-result-21670175-E8BAD2C4.json", // AllOrNothing, 2 Payments, 1 BatchSigner
    "tx-result-21670181-CF6877D3.json", // AllOrNothing, 2 Payments, 1 BatchSigner
    "tx-result-21670231-02984846.json", // AllOrNothing, 3 Payments, 1 BatchSigner
    "tx-result-21670276-02807423.json", // Independent, 2 TrustSets, no BatchSigners
    "tx-result-21670276-C82F6B33.json", // AllOrNothing, 3 Payments, 1 BatchSigner
    "tx-result-21670284-3FB70CCC.json"  // Independent, 2 Payments, 1 BatchSigner
  })
  void deserializeEveryCapturedBatchTransactionResult(String fixtureName) throws IOException {
    File fixture = new File(FIXTURE_DIR + fixtureName);
    String[] nameParts = fixtureName.replace(".json", "").split("-");
    UnsignedInteger expectedLedgerIndex = UnsignedInteger.valueOf(nameParts[2]);
    String expectedHashPrefix = nameParts[3];

    TransactionResult<Batch> result = objectMapper.readValue(
      fixture, new TypeReference<TransactionResult<Batch>>() {}
    );

    assertThat(result.hash().value()).startsWith(expectedHashPrefix);
    assertThat(result.ledgerIndex()).hasValue(LedgerIndex.of(expectedLedgerIndex));
    assertThat(result.validated()).isTrue();
    assertThat(result.closeDate()).isPresent();
    assertThat(result.metadata()).isPresent();
    assertThat(result.metadata().get().transactionResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(result.metadata().get().affectedNodes()).isNotEmpty();

    Batch batch = result.transaction();
    assertThat(batch.transactionType()).isEqualTo(TransactionType.BATCH);
    assertThat(batch.flags().tfAllOrNothing() || batch.flags().tfIndependent()).isTrue();
    assertThat(batch.transactionSignature()).isPresent();
    assertThat(batch.rawTransactions()).hasSizeBetween(2, 8);
    batch.rawTransactions().forEach(wrapper -> {
      Transaction inner = wrapper.rawTransaction();
      assertThat(inner).isInstanceOfAny(Payment.class, TrustSet.class);
      assertThat(inner.fee()).isEqualTo(XrpCurrencyAmount.ofDrops(0));
      assertThat(inner.transactionFlags().tfInnerBatchTxn()).isTrue();
      assertThat(inner.signingPublicKey().base16Value()).isEmpty();
      assertThat(inner.transactionSignature()).isEmpty();
    });

    // Every inner-transaction account other than the outer account must appear in BatchSigners, and every
    // BatchSigner must carry either a single signature or a multi-signature (never neither).
    batch.batchSigners().stream().map(BatchSignerWrapper::batchSigner).forEach(signer -> {
      assertThat(signer.account()).isNotEqualTo(batch.account());
      assertThat(signer.transactionSignature().isPresent() || !signer.signers().isEmpty()).isTrue();
    });

    // Generic (client-style) deserialization resolves to a Batch.
    TransactionResult<Transaction> generic = objectMapper.readValue(
      fixture, new TypeReference<TransactionResult<Transaction>>() {}
    );
    assertThat(generic.transaction()).isInstanceOf(Batch.class);
    assertThat(generic.transaction()).isEqualTo(batch);

    // Round trip.
    String json = objectMapper.writeValueAsString(result);
    TransactionResult<Batch> roundTripped = objectMapper.readValue(
      json, new TypeReference<TransactionResult<Batch>>() {}
    );
    assertThat(roundTripped).isEqualTo(result);
  }

  @Test
  void batchTransactionResultRoundTrips() throws IOException {
    TransactionResult<Batch> original = objectMapper.readValue(
      FIXTURE, new TypeReference<TransactionResult<Batch>>() {}
    );

    String json = objectMapper.writeValueAsString(original);
    TransactionResult<Batch> roundTripped = objectMapper.readValue(
      json, new TypeReference<TransactionResult<Batch>>() {}
    );

    assertThat(roundTripped).isEqualTo(original);
  }

  private static void assertInnerTrustSet(Transaction inner, String currency, long sequence) {
    assertThat(inner).isInstanceOf(TrustSet.class);
    TrustSet trustSet = (TrustSet) inner;
    assertThat(trustSet.account()).isEqualTo(ACCOUNT);
    assertThat(trustSet.fee()).isEqualTo(XrpCurrencyAmount.ofDrops(0));
    assertThat(trustSet.sequence()).isEqualTo(UnsignedInteger.valueOf(sequence));
    assertThat(trustSet.flags().getValue()).isEqualTo(1073872896L);
    assertThat(trustSet.flags().tfInnerBatchTxn()).isTrue();
    assertThat(trustSet.flags().tfSetNoRipple()).isTrue();
    assertThat(trustSet.signingPublicKey().base16Value()).isEmpty();
    assertThat(trustSet.transactionSignature()).isEmpty();
    assertThat(trustSet.limitAmount()).isEqualTo(
      IssuedCurrencyAmount.builder()
        .currency(currency)
        .issuer(ISSUER)
        .value("1000000000")
        .build()
    );
  }
}
