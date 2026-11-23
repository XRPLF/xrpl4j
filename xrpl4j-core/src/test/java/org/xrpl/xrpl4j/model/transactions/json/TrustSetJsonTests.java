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

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.TrustSetFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.NetworkId;
import org.xrpl.xrpl4j.model.transactions.TrustSet;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class TrustSetJsonTests extends AbstractJsonTest {

  @Test
  public void testMinimalTrustSetJson() throws JsonProcessingException, JSONException {
    TrustSet trustSet = TrustSet.builder()
      .account(Address.of("ra5nK24KXen9AHvsdFTKHSANinZseWnPcX"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .flags(TrustSetFlags.builder()
        .tfClearNoRipple()
        .build())
      .sequence(UnsignedInteger.valueOf(12))
      .limitAmount(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rsP3mgGb2tcYUrxiLFiHJiQXhsziegtwBc"))
        .value("100")
        .build())
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .networkId(NetworkId.of(1024))
      .build();

    String json = "{\n" +
      "    \"TransactionType\": \"TrustSet\",\n" +
      "    \"Account\": \"ra5nK24KXen9AHvsdFTKHSANinZseWnPcX\",\n" +
      "    \"Fee\": \"12\",\n" +
      "    \"Flags\": 2147745792,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"LimitAmount\": {\n" +
      "      \"currency\": \"USD\",\n" +
      "      \"issuer\": \"rsP3mgGb2tcYUrxiLFiHJiQXhsziegtwBc\",\n" +
      "      \"value\": \"100\"\n" +
      "    },\n" +
      "    \"NetworkID\": 1024,\n" +
      "    \"Sequence\": 12\n" +
      "}";

    assertCanSerializeAndDeserialize(trustSet, json);
  }

  @Test
  public void testMinimalTrustSetJsonWithoutFlags() throws JsonProcessingException, JSONException {
    TrustSet trustSet = TrustSet.builder()
      .account(Address.of("ra5nK24KXen9AHvsdFTKHSANinZseWnPcX"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.valueOf(12))
      .limitAmount(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rsP3mgGb2tcYUrxiLFiHJiQXhsziegtwBc"))
        .value("100")
        .build())
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{\n" +
      "    \"TransactionType\": \"TrustSet\",\n" +
      "    \"Account\": \"ra5nK24KXen9AHvsdFTKHSANinZseWnPcX\",\n" +
      "    \"Fee\": \"12\",\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"LimitAmount\": {\n" +
      "      \"currency\": \"USD\",\n" +
      "      \"issuer\": \"rsP3mgGb2tcYUrxiLFiHJiQXhsziegtwBc\",\n" +
      "      \"value\": \"100\"\n" +
      "    },\n" +
      "    \"Sequence\": 12\n" +
      "}";

    assertCanSerializeAndDeserialize(trustSet, json);
  }

  @Test
  public void testTrustSetWithQualityJson() throws JsonProcessingException, JSONException {
    TrustSet trustSet = TrustSet.builder()
      .account(Address.of("ra5nK24KXen9AHvsdFTKHSANinZseWnPcX"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .flags(TrustSetFlags.builder()
        .tfClearNoRipple()
        .build())
      .sequence(UnsignedInteger.valueOf(12))
      .limitAmount(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rsP3mgGb2tcYUrxiLFiHJiQXhsziegtwBc"))
        .value("100")
        .build())
      .qualityIn(UnsignedInteger.valueOf(100))
      .qualityOut(UnsignedInteger.valueOf(100))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{\n" +
      "    \"TransactionType\": \"TrustSet\",\n" +
      "    \"Account\": \"ra5nK24KXen9AHvsdFTKHSANinZseWnPcX\",\n" +
      "    \"Fee\": \"12\",\n" +
      "    \"Flags\": 2147745792,\n" +
      "    \"LimitAmount\": {\n" +
      "      \"currency\": \"USD\",\n" +
      "      \"issuer\": \"rsP3mgGb2tcYUrxiLFiHJiQXhsziegtwBc\",\n" +
      "      \"value\": \"100\"\n" +
      "    },\n" +
      "    \"Sequence\": 12,\n" +
      "    \"QualityIn\": 100,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"QualityOut\": 100\n" +
      "}";

    assertCanSerializeAndDeserialize(trustSet, json);
  }

  @Test
  public void testJsonWithUnknownFields() throws JsonProcessingException, JSONException {
    TrustSet trustSet = TrustSet.builder()
      .account(Address.of("ra5nK24KXen9AHvsdFTKHSANinZseWnPcX"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .flags(TrustSetFlags.builder()
        .tfClearNoRipple()
        .build())
      .sequence(UnsignedInteger.valueOf(12))
      .limitAmount(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rsP3mgGb2tcYUrxiLFiHJiQXhsziegtwBc"))
        .value("100")
        .build())
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .networkId(NetworkId.of(1024))
      .putUnknownFields("Foo", "Bar")
      .build();

    String json = "{\n" +
      "    \"Foo\" : \"Bar\",\n" +
      "    \"TransactionType\": \"TrustSet\",\n" +
      "    \"Account\": \"ra5nK24KXen9AHvsdFTKHSANinZseWnPcX\",\n" +
      "    \"Fee\": \"12\",\n" +
      "    \"Flags\": 2147745792,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"LimitAmount\": {\n" +
      "      \"currency\": \"USD\",\n" +
      "      \"issuer\": \"rsP3mgGb2tcYUrxiLFiHJiQXhsziegtwBc\",\n" +
      "      \"value\": \"100\"\n" +
      "    },\n" +
      "    \"NetworkID\": 1024,\n" +
      "    \"Sequence\": 12\n" +
      "}";

    assertCanSerializeAndDeserialize(trustSet, json);
  }

  @Test
  public void transactionFlagsReturnsEmptyFlagsWhenNoFlagsSet() {
    TrustSet trustSet = TrustSet.builder()
      .account(Address.of("ra5nK24KXen9AHvsdFTKHSANinZseWnPcX"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.valueOf(12))
      .limitAmount(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rsP3mgGb2tcYUrxiLFiHJiQXhsziegtwBc"))
        .value("100")
        .build())
      .build();

    assertThat(trustSet.transactionFlags()).isEqualTo(trustSet.flags());
    assertThat(trustSet.transactionFlags().isEmpty()).isTrue();
  }

  @Test
  public void transactionFlagsReturnsCorrectFlagsWhenFlagsSet() {
    TrustSet trustSet = TrustSet.builder()
      .account(Address.of("ra5nK24KXen9AHvsdFTKHSANinZseWnPcX"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.valueOf(12))
      .limitAmount(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rsP3mgGb2tcYUrxiLFiHJiQXhsziegtwBc"))
        .value("100")
        .build())
      .flags(TrustSetFlags.builder().tfSetNoRipple().tfSetFreeze().build())
      .build();

    assertThat(trustSet.transactionFlags()).isEqualTo(trustSet.flags());
    assertThat(trustSet.transactionFlags().tfFullyCanonicalSig()).isTrue();
    assertThat(((TrustSetFlags) trustSet.transactionFlags()).tfSetNoRipple()).isTrue();
    assertThat(((TrustSetFlags) trustSet.transactionFlags()).tfSetFreeze()).isTrue();
  }

  @Test
  public void builderFromCopiesFlagsCorrectly() {
    TrustSetFlags originalFlags = TrustSetFlags.builder()
      .tfSetNoRipple()
      .tfSetFreeze()
      .build();

    TrustSet original = TrustSet.builder()
      .account(Address.of("ra5nK24KXen9AHvsdFTKHSANinZseWnPcX"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.valueOf(12))
      .limitAmount(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rsP3mgGb2tcYUrxiLFiHJiQXhsziegtwBc"))
        .value("100")
        .build())
      .flags(originalFlags)
      .build();

    TrustSet copied = TrustSet.builder()
      .from(original)
      .build();

    assertThat(copied.flags()).isEqualTo(original.flags());
    assertThat(copied.transactionFlags()).isEqualTo(original.transactionFlags());
    assertThat(((TrustSetFlags) copied.transactionFlags()).tfSetNoRipple()).isTrue();
    assertThat(((TrustSetFlags) copied.transactionFlags()).tfSetFreeze()).isTrue();
  }

  @Test
  public void testTrustSetWithDelegate() throws JsonProcessingException, JSONException {
    TrustSet trustSet = TrustSet.builder()
      .account(Address.of("ra5nK24KXen9AHvsdFTKHSANinZseWnPcX"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.valueOf(12))
      .limitAmount(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rsP3mgGb2tcYUrxiLFiHJiQXhsziegtwBc"))
        .value("100")
        .build())
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .delegate(Address.of("rDelegateAddress123456789012345678"))
      .build();

    String json = "{\n" +
      "    \"TransactionType\": \"TrustSet\",\n" +
      "    \"Account\": \"ra5nK24KXen9AHvsdFTKHSANinZseWnPcX\",\n" +
      "    \"Fee\": \"12\",\n" +
      "    \"SigningPubKey\": \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"Delegate\": \"rDelegateAddress123456789012345678\",\n" +
      "    \"LimitAmount\": {\n" +
      "        \"currency\": \"USD\",\n" +
      "        \"issuer\": \"rsP3mgGb2tcYUrxiLFiHJiQXhsziegtwBc\",\n" +
      "        \"value\": \"100\"\n" +
      "    },\n" +
      "    \"Sequence\": 12\n" +
      "}";

    assertCanSerializeAndDeserialize(trustSet, json);
  }
}
