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
import static org.xrpl.xrpl4j.crypto.TestConstants.EC_ADDRESS;
import static org.xrpl.xrpl4j.crypto.TestConstants.ED_ADDRESS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.jayway.jsonassert.JsonAssert;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.XrplBinaryCodec;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.flags.PaymentFlags;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.Signer;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * Unit tests for {@link MultiSignedTransaction}.
 */
class MultiSignedTransactionTest {

  private static final String HEX_32_BYTES = "0000000000000000000000000000000000000000000000000000000000000000";
  private Signer signer1;
  private Signer signer2;
  private MultiSignedTransaction<Payment> multiSignedTransaction;


  @BeforeEach
  void setUp() {
    signer1 = Signer.builder()
      .transactionSignature(Signature.builder()
        .value(UnsignedByteArray.fromHex(HEX_32_BYTES))
        .build())
      .signingPublicKey(PublicKey.fromBase16EncodedPublicKey(HEX_32_BYTES + "01"))
      .build();
    signer2 = Signer.builder()
      .transactionSignature(Signature.builder()
        .value(UnsignedByteArray.fromHex(HEX_32_BYTES))
        .build())
      .signingPublicKey(PublicKey.fromBase16EncodedPublicKey(HEX_32_BYTES + "00"))
      .build();
    multiSignedTransaction = MultiSignedTransaction.<Payment>builder()
      .unsignedTransaction(Payment.builder()
        .account(ED_ADDRESS)
        .fee(XrpCurrencyAmount.of(UnsignedLong.ONE))
        .sequence(UnsignedInteger.ONE)
        .amount(XrpCurrencyAmount.ofDrops(12345))
        .destination(EC_ADDRESS)
        .flags(PaymentFlags.of(2147483648L))
        .build())
      .addSignerSet(
        signer1,
        signer2
      )
      .build();
  }

  @Test
  void testHash() {
    assertThat(multiSignedTransaction.hash().value())
      .isEqualTo("53F05196A66B08F30D1968A38BF8A9D85C624B03CF7AD2E8A7381AF1A7152043");
  }

  @Test
  void testSignedTransactionBytes() throws JsonProcessingException {
    assertThat(multiSignedTransaction.signedTransactionBytes().hexValue()).isEqualTo(
      XrplBinaryCodec.getInstance().encode(ObjectMapperFactory.create().writeValueAsString(
        multiSignedTransaction.signedTransaction()
      ))
    );
  }

  @Test
  void testSignedTransactionConstructed() {
    Transaction signedTransaction = multiSignedTransaction.signedTransaction();
    assertThat(signedTransaction.signers()).asList().hasSize(2)
      .extracting("signer.account", "signer.signingPublicKey", "signer.transactionSignature")
      .containsExactly(Tuple.tuple(
          signer2.signingPublicKey().deriveAddress(),
          signer2.signingPublicKey(),
          signer2.transactionSignature()
        ),
        Tuple.tuple(
          signer1.signingPublicKey().deriveAddress(),
          signer1.signingPublicKey(),
          signer1.transactionSignature()
        ));
  }

  @Test
  void jsonSerializeAndDeserialize() throws JsonProcessingException {
    String json = ObjectMapperFactory.create().writeValueAsString(multiSignedTransaction);
    JsonAssert.with(json).assertNotNull("$.unsignedTransaction");
    JsonAssert.with(json).assertNotNull("$.signedTransaction");
    JsonAssert.with(json).assertNotNull("$.signedTransactionBytes");
    JsonAssert.with(json).assertNotNull("$.signerSet");

    MultiSignedTransaction<?> actual = ObjectMapperFactory.create().readValue(json, MultiSignedTransaction.class);
    assertThat(actual).isEqualTo(multiSignedTransaction);
  }
}
