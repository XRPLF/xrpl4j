package org.xrpl.xrpl4j.model.transactions.json;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.assertj.core.util.Lists;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.PaymentFlags;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.NetworkId;
import org.xrpl.xrpl4j.model.transactions.PathStep;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Collections;

// FIXME: These tests should probably be replaced with a parameterized test that loads in payment json examples from
//  a file.  Will do this after merging with Neil's initial codec pass.
public class PaymentJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    Payment payment = Payment.builder()
      .account(Address.of("r9TeThyi5xiuUUrFjtPKZiHcDxs7K9H6Rb"))
      .destination(Address.of("r4BPgS7DHebQiU31xWELvZawwSG2fSPJ7C"))
      .amount(XrpCurrencyAmount.ofDrops(25000000))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .flags(PaymentFlags.UNSET)
      .sequence(UnsignedInteger.valueOf(2))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .networkId(NetworkId.of(1024))
      .build();

    String json = "{" +
      "  \"Account\": \"r9TeThyi5xiuUUrFjtPKZiHcDxs7K9H6Rb\"," +
      "  \"Destination\": \"r4BPgS7DHebQiU31xWELvZawwSG2fSPJ7C\"," +
      "  \"TransactionType\": \"Payment\"," +
      "  \"Amount\": \"25000000\"," +
      "  \"Fee\": \"10\"," +
      "  \"Flags\": 0," +
      "  \"NetworkID\": 1024," +
      "  \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "  \"Sequence\": 2" +
      "}";

    assertCanSerializeAndDeserialize(payment, json);
  }

  @Test
  public void testJsonWithFlags() throws JsonProcessingException, JSONException {

    Payment payment = Payment.builder()
      .account(Address.of("r9TeThyi5xiuUUrFjtPKZiHcDxs7K9H6Rb"))
      .destination(Address.of("r4BPgS7DHebQiU31xWELvZawwSG2fSPJ7C"))
      .amount(XrpCurrencyAmount.ofDrops(25000000))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(2))
      .flags(PaymentFlags.of(TransactionFlags.FULLY_CANONICAL_SIG.getValue()))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{" +
      "  \"Account\": \"r9TeThyi5xiuUUrFjtPKZiHcDxs7K9H6Rb\"," +
      "  \"Destination\": \"r4BPgS7DHebQiU31xWELvZawwSG2fSPJ7C\"," +
      "  \"TransactionType\": \"Payment\"," +
      "  \"Amount\": \"25000000\"," +
      "  \"Fee\": \"10\"," +
      "  \"Flags\": 2147483648," +
      "  \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "  \"Sequence\": 2" +
      "}";

    assertCanSerializeAndDeserialize(payment, json);
  }

  @Test
  public void testComplicatedJson() throws JsonProcessingException, JSONException {
    Payment payment = Payment.builder()
      .account(Address.of("rHXUjUtk5eiPFYpg27izxHeZ1t4x835Ecn"))
      .destination(Address.of("r45dBj4S3VvMMYXxr9vHX4Z4Ma6ifPMCkK"))
      .amount(IssuedCurrencyAmount.builder()
        .currency("CNY")
        .value("5000")
        .issuer(Address.of("r45dBj4S3VvMMYXxr9vHX4Z4Ma6ifPMCkK"))
        .build()
      )
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sendMax(IssuedCurrencyAmount.builder()
        .currency("CNY")
        .value("5050")
        .issuer(Address.of("rHXUjUtk5eiPFYpg27izxHeZ1t4x835Ecn"))
        .build()
      )
      .sequence(UnsignedInteger.valueOf(6))
      .addPaths(Lists.newArrayList(
        PathStep.builder()
          .account(Address.of("razqQKzJRdB4UxFPWf5NEpEG3WMkmwgcXA"))
          .build()
      ))
      .flags(PaymentFlags.of(TransactionFlags.FULLY_CANONICAL_SIG.getValue()))
      .destinationTag(UnsignedInteger.valueOf(736049272))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .credentialIds(Collections.singletonList(
        Hash256.of("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37"))
      )
      .domainId(Hash256.of("A2356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B3F"))
      .build();

    String json = "{" +
      "  \"Account\": \"rHXUjUtk5eiPFYpg27izxHeZ1t4x835Ecn\"," +
      "  \"Destination\": \"r45dBj4S3VvMMYXxr9vHX4Z4Ma6ifPMCkK\"," +
      "  \"TransactionType\": \"Payment\"," +
      "  \"Amount\": {" +
      "    \"currency\": \"CNY\"," +
      "    \"value\": \"5000\"," +
      "    \"issuer\": \"r45dBj4S3VvMMYXxr9vHX4Z4Ma6ifPMCkK\"" +
      "  }," +
      "  \"Fee\": \"12\"," +
      "  \"SendMax\": {" +
      "    \"currency\": \"CNY\"," +
      "    \"value\": \"5050\"," +
      "    \"issuer\": \"rHXUjUtk5eiPFYpg27izxHeZ1t4x835Ecn\"" +
      "  }," +
      "  \"Flags\": 2147483648," +
      "  \"Sequence\": 6," +
      "  \"Paths\": [[{" +
      "    \"account\": \"razqQKzJRdB4UxFPWf5NEpEG3WMkmwgcXA\"" +
      "  }]]," +
      "  \"SigningPubKey\": \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "  \"CredentialIDs\": [\"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37\"]," +
      "  \"DestinationTag\": 736049272," +
      "  \"DomainID\": \"A2356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B3F\"" +
      "}";

    assertCanSerializeAndDeserialize(payment, json);
  }

  @Test
  public void testJsonWithUnknownFields() throws JsonProcessingException, JSONException {
    Payment payment = Payment.builder()
      .account(Address.of("r9TeThyi5xiuUUrFjtPKZiHcDxs7K9H6Rb"))
      .destination(Address.of("r4BPgS7DHebQiU31xWELvZawwSG2fSPJ7C"))
      .amount(XrpCurrencyAmount.ofDrops(25000000))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .flags(PaymentFlags.UNSET)
      .sequence(UnsignedInteger.valueOf(2))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .networkId(NetworkId.of(1024))
      .putUnknownFields("Foo", "Bar")
      .build();

    String json = "{" +
      "  \"Foo\" : \"Bar\"," +
      "  \"Account\": \"r9TeThyi5xiuUUrFjtPKZiHcDxs7K9H6Rb\"," +
      "  \"Destination\": \"r4BPgS7DHebQiU31xWELvZawwSG2fSPJ7C\"," +
      "  \"TransactionType\": \"Payment\"," +
      "  \"Amount\": \"25000000\"," +
      "  \"Fee\": \"10\"," +
      "  \"Flags\": 0," +
      "  \"NetworkID\": 1024," +
      "  \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "  \"Sequence\": 2" +
      "}";

    assertCanSerializeAndDeserialize(payment, json);
  }

  @Test
  public void testJsonWithDelegate() throws JsonProcessingException, JSONException {
    Payment payment = Payment.builder()
      .account(Address.of("r9TeThyi5xiuUUrFjtPKZiHcDxs7K9H6Rb"))
      .destination(Address.of("r4BPgS7DHebQiU31xWELvZawwSG2fSPJ7C"))
      .amount(XrpCurrencyAmount.ofDrops(25000000))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .flags(PaymentFlags.UNSET)
      .sequence(UnsignedInteger.valueOf(2))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .delegate(Address.of("rDelegateAddress123456789012345678"))
      .build();

    String json = "{" +
      "  \"Account\": \"r9TeThyi5xiuUUrFjtPKZiHcDxs7K9H6Rb\"," +
      "  \"Destination\": \"r4BPgS7DHebQiU31xWELvZawwSG2fSPJ7C\"," +
      "  \"TransactionType\": \"Payment\"," +
      "  \"Amount\": \"25000000\"," +
      "  \"Fee\": \"10\"," +
      "  \"Flags\": 0," +
      "  \"Delegate\": \"rDelegateAddress123456789012345678\"," +
      "  \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"," +
      "  \"Sequence\": 2" +
      "}";

    assertCanSerializeAndDeserialize(payment, json);
  }
}
