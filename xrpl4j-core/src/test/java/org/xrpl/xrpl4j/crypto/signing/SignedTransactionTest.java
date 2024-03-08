package org.xrpl.xrpl4j.crypto.signing;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.XrplBinaryCodec;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.flags.PaymentFlags;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Memo;
import org.xrpl.xrpl4j.model.transactions.MemoWrapper;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Arrays;
import java.util.Collections;

/**
 * Unit tests for {@link SingleSignedTransaction}.
 */
class SignedTransactionTest {

  /**
   * This test constructs the transaction with hash A7AE53FE15B02E6E2F3C610FB4BA30B12392EB110F1D5E8C20880555E8639B05 to
   * check that the hash that's on livenet matches what this library computes. The hash you see in this test is
   * different from the hash found on livenet because the real transaction did not set any flags on the transaction and
   * {@link Payment} requires a flags field (Even if you set flags to 0, it affects the hash). However, we made
   * {@link Payment#flags()} nullable during development and verified that the hashes match, so we are confident that
   * our hash calculation is accurate.
   *
   * @see "https://livenet.xrpl.org/transactions/A7AE53FE15B02E6E2F3C610FB4BA30B12392EB110F1D5E8C20880555E8639B05"
   */
  @Test
  public void computesCorrectTransactionHash() throws JsonProcessingException {
    final Payment unsignedTransaction = Payment.builder()
      .account(Address.of("rf56THCDKWb348ks9hvaD4YXq6U1qBJNsJ"))
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.valueOf(76829518))
      .destination(Address.of("rDNvjMc6LjtpR7BdfiSNvavUBjznhhmpNq"))
      .amount(XrpCurrencyAmount.ofDrops(1))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("0281E58C76A7EB8397C008CB6B6D325FF6765F008CF845AF5EB02DAB6D222C612C")
      )
      .flags(PaymentFlags.of(TransactionFlags.FULLY_CANONICAL_SIG.getValue()))
      .destinationTag(UnsignedInteger.valueOf(371969))
      .build();

    final Signature signature = Signature.fromBase16(
      "304502210093257D8E88D2A92CE55977641F72CCD235AB76B1AE189BE3377F30A69B131C49" +
        "02200B79836114069F0D331418D05818908D85DE755AE5C2DDF42E9637FE1C11754F"
    );

    final Payment signedPayment = Payment.builder().from(unsignedTransaction)
      .transactionSignature(signature)
      .build();

    SingleSignedTransaction<Payment> signedTransaction = SingleSignedTransaction.<Payment>builder()
      .signedTransaction(signedPayment)
      .signature(signature)
      .unsignedTransaction(unsignedTransaction)
      .build();

    String expectedHash = "F847C96B2EEB0609F16C9DB9D74A0CB123B5EAF5B626207977335BF0A1EF53C3";
    assertThat(signedTransaction.hash().value()).isEqualTo(expectedHash);
    assertThat(signedTransaction.unsignedTransaction()).isEqualTo(unsignedTransaction);
    assertThat(signedTransaction.signedTransaction()).isEqualTo(signedPayment);
    assertThat(signedTransaction.signedTransactionBytes().hexValue()).isEqualTo(
      XrplBinaryCodec.getInstance().encode(ObjectMapperFactory.create().writeValueAsString(signedPayment))
    );
  }

  /**
   * This test constructs the transaction with hash 1A1953AC3BA3123254AA912CE507514A6AAD05EED8981A870B45F604936F0997 to
   * check that the hash that's on livenet matches what this library computes.
   *
   * @see "https://livenet.xrpl.org/transactions/1A1953AC3BA3123254AA912CE507514A6AAD05EED8981A870B45F604936F0997"
   */
  @Test
  public void computesCorrectTransactionHashWithUnsetFlags() throws JsonProcessingException {
    final Payment unsignedTransaction = Payment.builder()
      .account(Address.of("rGWx7VAsnwVKRbPFPpvy8Lo4nFf5xjj6Zb"))
      .amount(XrpCurrencyAmount.ofDrops(1))
      .destination(Address.of("rxRpSNb1VktvzBz8JF2oJC6qaww6RZ7Lw"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .flags(PaymentFlags.of(TransactionFlags.UNSET.getValue())) // 0
      .lastLedgerSequence(UnsignedInteger.valueOf(86481544))
      .memos(Collections.singletonList(
        MemoWrapper.builder()
          .memo(Memo.builder()
            .memoData("7B226F70223A226D696E74222C22616D6F756E74223A22313030303030303030222C22677061223A2230227D")
            .build())
          .build()
      ))
      .sequence(UnsignedInteger.valueOf(84987644))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED05DC98B76FCD734BD44CDF153C34F79728485D2F24F9381CF7A284223EA258CE")
      )
      .build();

    final Signature signature = Signature.fromBase16(
      "ED6F91CCF14EE94EB072C7671A397A313E3E5CBDAFE773BB6B2F07A0E75A7E65F84B5516268DAEE12902265256" +
        "EA1EF046B200148E14FF4E720C06519FD7F40F"
    );

    final Payment signedPayment = Payment.builder().from(unsignedTransaction)
      .transactionSignature(signature)
      .build();

    SingleSignedTransaction<Payment> signedTransaction = SingleSignedTransaction.<Payment>builder()
      .signedTransaction(signedPayment)
      .signature(signature)
      .unsignedTransaction(unsignedTransaction)
      .build();

    String expectedHash = "1A1953AC3BA3123254AA912CE507514A6AAD05EED8981A870B45F604936F0997";
    assertThat(signedTransaction.hash().value()).isEqualTo(expectedHash);
    assertThat(signedTransaction.unsignedTransaction()).isEqualTo(unsignedTransaction);
    assertThat(signedTransaction.signedTransaction()).isEqualTo(signedPayment);
    assertThat(signedTransaction.signedTransactionBytes().hexValue()).isEqualTo(
      XrplBinaryCodec.getInstance().encode(ObjectMapperFactory.create().writeValueAsString(signedPayment))
    );
  }

  /**
   * This test constructs the transaction with hash 1A1953AC3BA3123254AA912CE507514A6AAD05EED8981A870B45F604936F0997 to
   * check that the hash that's on livenet does not match when the signature is supplied incorrectly (i.e., this test
   * validates that a transaction's signature is always used to compute a transaction hash).
   *
   * @see "https://livenet.xrpl.org/transactions/1A1953AC3BA3123254AA912CE507514A6AAD05EED8981A870B45F604936F0997"
   */
  @Test
  public void computesIncorrectTransactionHashWithoutSignature() throws JsonProcessingException {
    final Payment unsignedTransaction = Payment.builder()
      .account(Address.of("rGWx7VAsnwVKRbPFPpvy8Lo4nFf5xjj6Zb"))
      .amount(XrpCurrencyAmount.ofDrops(1))
      .destination(Address.of("rxRpSNb1VktvzBz8JF2oJC6qaww6RZ7Lw"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .flags(PaymentFlags.of(TransactionFlags.UNSET.getValue())) // 0
      .lastLedgerSequence(UnsignedInteger.valueOf(86481544))
      .memos(Collections.singletonList(
        MemoWrapper.builder()
          .memo(Memo.builder()
            .memoData("7B226F70223A226D696E74222C22616D6F756E74223A22313030303030303030222C22677061223A2230227D")
            .build())
          .build()
      ))
      .sequence(UnsignedInteger.valueOf(84987644))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED05DC98B76FCD734BD44CDF153C34F79728485D2F24F9381CF7A284223EA258CE")
      )
      .build();

    final Signature emptySignature = Signature.fromBase16("");

    final Payment signedPayment = Payment.builder().from(unsignedTransaction)
      .transactionSignature(emptySignature)
      .build();

    SingleSignedTransaction<Payment> signedTransaction = SingleSignedTransaction.<Payment>builder()
      .signedTransaction(signedPayment)
      .signature(emptySignature)
      .unsignedTransaction(unsignedTransaction)
      .build();

    String expectedHash = "1A1953AC3BA3123254AA912CE507514A6AAD05EED8981A870B45F604936F0997";
    assertThat(signedTransaction.hash().value()).isNotEqualTo(expectedHash);
    assertThat(signedTransaction.hash().value()).isEqualTo(
      "8E0EDE65ECE8A03ABDD7926B994B2F6F14514FDBD46714F4F511143A1F01A6D0"
    );
    assertThat(signedTransaction.unsignedTransaction()).isEqualTo(unsignedTransaction);
    assertThat(signedTransaction.signedTransaction()).isEqualTo(signedPayment);
    assertThat(signedTransaction.signedTransactionBytes().hexValue()).isEqualTo(
      XrplBinaryCodec.getInstance().encode(ObjectMapperFactory.create().writeValueAsString(signedPayment))
    );
  }
}
