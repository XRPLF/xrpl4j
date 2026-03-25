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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.xrpl.xrpl4j.crypto.TestConstants.EC_ADDRESS;
import static org.xrpl.xrpl4j.crypto.TestConstants.ED_ADDRESS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.jayway.jsonassert.JsonAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.XrplBinaryCodec;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.flags.PaymentFlags;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.Signer;
import org.xrpl.xrpl4j.model.transactions.SignerWrapper;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * Unit tests for {@link SingleSignedTransaction}.
 */
class SingleSignedTransactionTest {

  private static final String HEX_32_BYTES = "0000000000000000000000000000000000000000000000000000000000000000";
  private SingleSignedTransaction<?> singleSingedTransaction;

  @BeforeEach
  void setUp() {
    singleSingedTransaction = SingleSignedTransaction.builder()
      .signedTransaction(Payment.builder()
        .account(ED_ADDRESS)
        .fee(XrpCurrencyAmount.of(UnsignedLong.ONE))
        .sequence(UnsignedInteger.ONE)
        .amount(XrpCurrencyAmount.ofDrops(12345))
        .destination(EC_ADDRESS)
        .flags(PaymentFlags.of(TransactionFlags.FULLY_CANONICAL_SIG.getValue()))
        .build())
      .unsignedTransaction(Payment.builder()
        .account(ED_ADDRESS)
        .fee(XrpCurrencyAmount.of(UnsignedLong.ONE))
        .sequence(UnsignedInteger.ONE)
        .amount(XrpCurrencyAmount.ofDrops(12345))
        .destination(EC_ADDRESS)
        .signingPublicKey(PublicKey.fromBase16EncodedPublicKey(HEX_32_BYTES + "01"))
        .build())
      .signature(Signature.builder()
        .value(UnsignedByteArray.fromHex(HEX_32_BYTES))
        .build())
      .build();
  }

  @Test
  void testHash() {
    assertThat(singleSingedTransaction.hash().value())
      .isEqualTo("3C9BE23F7820DDC3D93CF69A7E47C6A89FEB0EF27A1F25E62F87F9E1F7E6E910");
  }

  @Test
  void testUnsignedAndSignedTransactionAccessors() {
    Transaction unsigned = singleSingedTransaction.unsignedTransaction();
    Transaction signed = singleSingedTransaction.signedTransaction();

    // Verify unsigned transaction has no signature
    assertThat(unsigned.transactionSignature()).isEmpty();
    assertThat(unsigned.signers()).isEmpty();

    // Verify signed transaction has the same fields as unsigned
    assertThat(signed.account()).isEqualTo(unsigned.account());
    assertThat(signed.fee()).isEqualTo(unsigned.fee());
    assertThat(signed.sequence()).isEqualTo(unsigned.sequence());
  }

  @Test
  void testSignatureAccessor() {
    Signature signature = singleSingedTransaction.signature();
    assertThat(signature).isNotNull();
    assertThat(signature.value().hexValue()).isEqualTo(HEX_32_BYTES);
  }

  @Test
  void testSignedTransactionBytes() throws JsonProcessingException {
    assertThat(singleSingedTransaction.signedTransactionBytes().hexValue()).isEqualTo(
      XrplBinaryCodec.getInstance().encode(ObjectMapperFactory.create().writeValueAsString(
        singleSingedTransaction.signedTransaction()
      ))
    );
  }

  @Test
  void jsonSerializeAndDeserialize() throws JsonProcessingException {
    String json = ObjectMapperFactory.create().writeValueAsString(singleSingedTransaction);
    JsonAssert.with(json).assertNotNull("$.unsignedTransaction");
    JsonAssert.with(json).assertNotNull("$.signedTransaction");
    JsonAssert.with(json).assertNotNull("$.signedTransactionBytes");
    JsonAssert.with(json).assertNotNull("$.signature");

    SingleSignedTransaction<?> actual = ObjectMapperFactory.create().readValue(json, SingleSignedTransaction.class);
    assertThat(actual).isEqualTo(singleSingedTransaction);
  }

  @Test
  void buildWithTransactionSignatureThrows() {
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> SingleSignedTransaction.<Payment>builder()
        .unsignedTransaction(Payment.builder()
          .account(ED_ADDRESS)
          .fee(XrpCurrencyAmount.of(UnsignedLong.ONE))
          .sequence(UnsignedInteger.ONE)
          .amount(XrpCurrencyAmount.ofDrops(12345))
          .destination(EC_ADDRESS)
          .signingPublicKey(PublicKey.fromBase16EncodedPublicKey(HEX_32_BYTES + "01"))
          .transactionSignature(Signature.builder()
            .value(UnsignedByteArray.fromHex(HEX_32_BYTES))
            .build())
          .build())
        .signedTransaction(Payment.builder()
          .account(ED_ADDRESS)
          .fee(XrpCurrencyAmount.of(UnsignedLong.ONE))
          .sequence(UnsignedInteger.ONE)
          .amount(XrpCurrencyAmount.ofDrops(12345))
          .destination(EC_ADDRESS)
          .transactionSignature(Signature.builder()
            .value(UnsignedByteArray.fromHex(HEX_32_BYTES))
            .build())
          .build())
        .signature(Signature.builder()
          .value(UnsignedByteArray.fromHex(HEX_32_BYTES))
          .build())
        .build()
    );
    assertThat(exception.getMessage()).isEqualTo("Transactions to be single-signed must not already be signed.");
  }

  @Test
  void buildWithSignersThrows() {
    Signer signer = Signer.builder()
      .transactionSignature(Signature.builder()
        .value(UnsignedByteArray.fromHex(HEX_32_BYTES))
        .build())
      .signingPublicKey(PublicKey.fromBase16EncodedPublicKey(HEX_32_BYTES + "01"))
      .build();

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> SingleSignedTransaction.<Payment>builder()
        .unsignedTransaction(Payment.builder()
          .account(ED_ADDRESS)
          .fee(XrpCurrencyAmount.of(UnsignedLong.ONE))
          .sequence(UnsignedInteger.ONE)
          .amount(XrpCurrencyAmount.ofDrops(12345))
          .destination(EC_ADDRESS)
          .signingPublicKey(PublicKey.fromBase16EncodedPublicKey(HEX_32_BYTES + "01"))
          .addSigners(SignerWrapper.of(signer))
          .build())
        .signedTransaction(Payment.builder()
          .account(ED_ADDRESS)
          .fee(XrpCurrencyAmount.of(UnsignedLong.ONE))
          .sequence(UnsignedInteger.ONE)
          .amount(XrpCurrencyAmount.ofDrops(12345))
          .destination(EC_ADDRESS)
          .build())
        .signature(Signature.builder()
          .value(UnsignedByteArray.fromHex(HEX_32_BYTES))
          .build())
        .build()
    );
    assertThat(exception.getMessage()).isEqualTo("Transactions to be single-signed must not have Signers.");
  }

  @Test
  void buildWithMultiSignPublicKeyThrows() {
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> SingleSignedTransaction.<Payment>builder()
        .unsignedTransaction(Payment.builder()
          .account(ED_ADDRESS)
          .fee(XrpCurrencyAmount.of(UnsignedLong.ONE))
          .sequence(UnsignedInteger.ONE)
          .amount(XrpCurrencyAmount.ofDrops(12345))
          .destination(EC_ADDRESS)
          .signingPublicKey(PublicKey.MULTI_SIGN_PUBLIC_KEY)
          .build())
        .signedTransaction(Payment.builder()
          .account(ED_ADDRESS)
          .fee(XrpCurrencyAmount.of(UnsignedLong.ONE))
          .sequence(UnsignedInteger.ONE)
          .amount(XrpCurrencyAmount.ofDrops(12345))
          .destination(EC_ADDRESS)
          .build())
        .signature(Signature.builder()
          .value(UnsignedByteArray.fromHex(HEX_32_BYTES))
          .build())
        .build()
    );
    assertThat(exception.getMessage()).isEqualTo(
      "Transactions to be single-signed must not set `signingPublicKey` to the multisig (empty) public key.");
  }

}
