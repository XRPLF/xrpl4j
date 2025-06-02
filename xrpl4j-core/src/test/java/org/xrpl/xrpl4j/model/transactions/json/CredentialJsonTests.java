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
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CredentialAccept;
import org.xrpl.xrpl4j.model.transactions.CredentialCreate;
import org.xrpl.xrpl4j.model.transactions.CredentialDelete;
import org.xrpl.xrpl4j.model.transactions.CredentialType;
import org.xrpl.xrpl4j.model.transactions.CredentialUri;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class CredentialJsonTests extends AbstractJsonTest {

  // CredentialCreate
  @Test
  public void testCredentialCreateJson() throws JsonProcessingException, JSONException {
    CredentialCreate credentialCreate = CredentialCreate.builder()
      .account(Address.of("rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c"))
      .subject(Address.of("rffipZrgdn3ZYo4o2JN4Cymf3sP9m4rKuU"))
      .credentialType(CredentialType.ofPlainText("driver licence"))
      .uri(CredentialUri.ofPlainText("https://sample-vc.pdf"))
      .sequence(UnsignedInteger.valueOf(3195631))
      .fee(XrpCurrencyAmount.ofDrops(1))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84")
      )
      .build();

    String json = "{\n" +
                  "   \"Account\":\"rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c\",\n" +
                  "   \"CredentialType\":\"647269766572206C6963656E6365\",\n" +
                  "   \"Fee\":\"1\",\n" +
                  "   \"Sequence\":3195631,\n" +
                  "   \"SigningPubKey\":\"ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84\",\n" +
                  "   \"Subject\":\"rffipZrgdn3ZYo4o2JN4Cymf3sP9m4rKuU\",\n" +
                  "   \"TransactionType\":\"CredentialCreate\",\n" +
                  "   \"URI\":\"68747470733A2F2F73616D706C652D76632E706466\"\n" +
                  "}";

    assertCanSerializeAndDeserialize(credentialCreate, json);
  }

  @Test
  public void testCredentialCreateJsonWithUnsetFlags() throws JsonProcessingException, JSONException {
    CredentialCreate credentialCreate = CredentialCreate.builder()
      .account(Address.of("rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c"))
      .subject(Address.of("rffipZrgdn3ZYo4o2JN4Cymf3sP9m4rKuU"))
      .credentialType(CredentialType.ofPlainText("driver licence"))
      .uri(CredentialUri.ofPlainText("https://sample-vc.pdf"))
      .sequence(UnsignedInteger.valueOf(3195631))
      .fee(XrpCurrencyAmount.ofDrops(1))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84")
      )
      .flags(TransactionFlags.UNSET)
      .build();

    String json = "{\n" +
                  "   \"Account\":\"rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c\",\n" +
                  "   \"CredentialType\":\"647269766572206C6963656E6365\",\n" +
                  "   \"Fee\":\"1\",\n" +
                  "   \"Sequence\":3195631,\n" +
                  "   \"SigningPubKey\":\"ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84\",\n" +
                  "   \"Subject\":\"rffipZrgdn3ZYo4o2JN4Cymf3sP9m4rKuU\",\n" +
                  "   \"TransactionType\":\"CredentialCreate\",\n" +
                  "   \"Flags\": 0,\n" +
                  "   \"URI\":\"68747470733A2F2F73616D706C652D76632E706466\"\n" +
                  "}";

    assertCanSerializeAndDeserialize(credentialCreate, json);
  }

  @Test
  public void testCredentialCreateJsonWithNonZeroFlags() throws JsonProcessingException, JSONException {
    CredentialCreate credentialCreate = CredentialCreate.builder()
      .account(Address.of("rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c"))
      .subject(Address.of("rffipZrgdn3ZYo4o2JN4Cymf3sP9m4rKuU"))
      .credentialType(CredentialType.ofPlainText("driver licence"))
      .uri(CredentialUri.ofPlainText("https://sample-vc.pdf"))
      .sequence(UnsignedInteger.valueOf(3195631))
      .fee(XrpCurrencyAmount.ofDrops(1))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84")
      )
      .flags(TransactionFlags.FULLY_CANONICAL_SIG)
      .build();

    String json = "{\n" +
                  "   \"Account\":\"rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c\",\n" +
                  "   \"CredentialType\":\"647269766572206C6963656E6365\",\n" +
                  "   \"Fee\":\"1\",\n" +
                  "   \"Sequence\":3195631,\n" +
                  "   \"SigningPubKey\":\"ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84\",\n" +
                  "   \"Subject\":\"rffipZrgdn3ZYo4o2JN4Cymf3sP9m4rKuU\",\n" +
                  "   \"TransactionType\":\"CredentialCreate\",\n" +
                  "   \"Flags\": 2147483648,\n" +
                  "   \"URI\":\"68747470733A2F2F73616D706C652D76632E706466\"\n" +
                  "}";

    assertCanSerializeAndDeserialize(credentialCreate, json);
  }

  @Test
  public void testCredentialCreateJsonWithUnknownFields() throws JsonProcessingException, JSONException {
    CredentialCreate credentialCreate = CredentialCreate.builder()
      .account(Address.of("rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c"))
      .subject(Address.of("rffipZrgdn3ZYo4o2JN4Cymf3sP9m4rKuU"))
      .credentialType(CredentialType.ofPlainText("driver licence"))
      .uri(CredentialUri.ofPlainText("https://sample-vc.pdf"))
      .sequence(UnsignedInteger.valueOf(3195631))
      .fee(XrpCurrencyAmount.ofDrops(1))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84")
      )
      .flags(TransactionFlags.FULLY_CANONICAL_SIG)
      .putUnknownFields("Foo", "Bar")
      .build();

    String json = "{\n" +
                  "   \"Account\":\"rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c\",\n" +
                  "   \"CredentialType\":\"647269766572206C6963656E6365\",\n" +
                  "   \"Fee\":\"1\",\n" +
                  "   \"Sequence\":3195631,\n" +
                  "   \"SigningPubKey\":\"ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84\",\n" +
                  "   \"Subject\":\"rffipZrgdn3ZYo4o2JN4Cymf3sP9m4rKuU\",\n" +
                  "   \"TransactionType\":\"CredentialCreate\",\n" +
                  "   \"Flags\": 2147483648,\n" +
                  "   \"Foo\" : \"Bar\",\n" +
                  "   \"URI\":\"68747470733A2F2F73616D706C652D76632E706466\"\n" +
                  "}";

    assertCanSerializeAndDeserialize(credentialCreate, json);
  }

  // CredentialAccept
  @Test
  public void testCredentialAcceptJson() throws JsonProcessingException, JSONException {
    CredentialAccept credentialAccept = CredentialAccept.builder()
      .account(Address.of("rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c"))
      .issuer(Address.of("rffipZrgdn3ZYo4o2JN4Cymf3sP9m4rKuU"))
      .credentialType(CredentialType.ofPlainText("driver licence"))
      .sequence(UnsignedInteger.valueOf(3195631))
      .fee(XrpCurrencyAmount.ofDrops(1))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84")
      )
      .build();

    String json = "{\n" +
                  "   \"Account\":\"rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c\",\n" +
                  "   \"Issuer\":\"rffipZrgdn3ZYo4o2JN4Cymf3sP9m4rKuU\",\n" +
                  "   \"CredentialType\":\"647269766572206C6963656E6365\",\n" +
                  "   \"Fee\":\"1\",\n" +
                  "   \"Sequence\":3195631,\n" +
                  "   \"SigningPubKey\":\"ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84\",\n" +
                  "   \"TransactionType\":\"CredentialAccept\"\n" +
                  "}";

    assertCanSerializeAndDeserialize(credentialAccept, json);
  }

  @Test
  public void testCredentialAcceptJsonWithUnsetFlags() throws JsonProcessingException, JSONException {
    CredentialAccept credentialAccept = CredentialAccept.builder()
      .account(Address.of("rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c"))
      .issuer(Address.of("rffipZrgdn3ZYo4o2JN4Cymf3sP9m4rKuU"))
      .credentialType(CredentialType.ofPlainText("driver licence"))
      .sequence(UnsignedInteger.valueOf(3195631))
      .fee(XrpCurrencyAmount.ofDrops(1))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84")
      )
      .flags(TransactionFlags.UNSET)
      .build();

    String json = "{\n" +
                  "   \"Account\":\"rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c\",\n" +
                  "   \"Issuer\":\"rffipZrgdn3ZYo4o2JN4Cymf3sP9m4rKuU\",\n" +
                  "   \"CredentialType\":\"647269766572206C6963656E6365\",\n" +
                  "   \"Fee\":\"1\",\n" +
                  "   \"Sequence\":3195631,\n" +
                  "   \"SigningPubKey\":\"ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84\",\n" +
                  "   \"Flags\": 0,\n" +
                  "   \"TransactionType\":\"CredentialAccept\"\n" +
                  "}";

    assertCanSerializeAndDeserialize(credentialAccept, json);
  }

  @Test
  public void testCredentialAcceptJsonWithNonZeroFlags() throws JsonProcessingException, JSONException {
    CredentialAccept credentialAccept = CredentialAccept.builder()
      .account(Address.of("rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c"))
      .issuer(Address.of("rffipZrgdn3ZYo4o2JN4Cymf3sP9m4rKuU"))
      .credentialType(CredentialType.ofPlainText("driver licence"))
      .sequence(UnsignedInteger.valueOf(3195631))
      .fee(XrpCurrencyAmount.ofDrops(1))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84")
      )
      .flags(TransactionFlags.FULLY_CANONICAL_SIG)
      .build();

    String json = "{\n" +
                  "   \"Account\":\"rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c\",\n" +
                  "   \"Issuer\":\"rffipZrgdn3ZYo4o2JN4Cymf3sP9m4rKuU\",\n" +
                  "   \"CredentialType\":\"647269766572206C6963656E6365\",\n" +
                  "   \"Fee\":\"1\",\n" +
                  "   \"Sequence\":3195631,\n" +
                  "   \"SigningPubKey\":\"ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84\",\n" +
                  "   \"Flags\": 2147483648,\n" +
                  "   \"TransactionType\":\"CredentialAccept\"\n" +
                  "}";

    assertCanSerializeAndDeserialize(credentialAccept, json);
  }

  @Test
  public void testCredentialAcceptJsonWithUnknownFields() throws JsonProcessingException, JSONException {
    CredentialAccept credentialAccept = CredentialAccept.builder()
      .account(Address.of("rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c"))
      .issuer(Address.of("rffipZrgdn3ZYo4o2JN4Cymf3sP9m4rKuU"))
      .credentialType(CredentialType.ofPlainText("driver licence"))
      .sequence(UnsignedInteger.valueOf(3195631))
      .fee(XrpCurrencyAmount.ofDrops(1))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84")
      )
      .flags(TransactionFlags.FULLY_CANONICAL_SIG)
      .putUnknownFields("Foo", "Bar")
      .build();

    String json = "{\n" +
                  "   \"Account\":\"rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c\",\n" +
                  "   \"Issuer\":\"rffipZrgdn3ZYo4o2JN4Cymf3sP9m4rKuU\",\n" +
                  "   \"CredentialType\":\"647269766572206C6963656E6365\",\n" +
                  "   \"Fee\":\"1\",\n" +
                  "   \"Sequence\":3195631,\n" +
                  "   \"SigningPubKey\":\"ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84\",\n" +
                  "   \"Flags\": 2147483648,\n" +
                  "   \"Foo\" : \"Bar\",\n" +
                  "   \"TransactionType\":\"CredentialAccept\"\n" +
                  "}";

    assertCanSerializeAndDeserialize(credentialAccept, json);
  }

  // CredentialDelete
  @Test
  public void testCredentialDeleteJson() throws JsonProcessingException, JSONException {
    CredentialDelete credentialDelete = CredentialDelete.builder()
      .account(Address.of("rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c"))
      .issuer(Address.of("rffipZrgdn3ZYo4o2JN4Cymf3sP9m4rKuU"))
      .credentialType(CredentialType.ofPlainText("driver licence"))
      .sequence(UnsignedInteger.valueOf(3195631))
      .fee(XrpCurrencyAmount.ofDrops(1))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84")
      )
      .build();

    String json = "{\n" +
                  "   \"Account\":\"rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c\",\n" +
                  "   \"Issuer\":\"rffipZrgdn3ZYo4o2JN4Cymf3sP9m4rKuU\",\n" +
                  "   \"CredentialType\":\"647269766572206C6963656E6365\",\n" +
                  "   \"Fee\":\"1\",\n" +
                  "   \"Sequence\":3195631,\n" +
                  "   \"SigningPubKey\":\"ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84\",\n" +
                  "   \"TransactionType\":\"CredentialDelete\"\n" +
                  "}";

    assertCanSerializeAndDeserialize(credentialDelete, json);
  }

  @Test
  public void testCredentialDeleteJsonWithUnsetFlags() throws JsonProcessingException, JSONException {
    CredentialDelete credentialDelete = CredentialDelete.builder()
      .account(Address.of("rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c"))
      .issuer(Address.of("rffipZrgdn3ZYo4o2JN4Cymf3sP9m4rKuU"))
      .credentialType(CredentialType.ofPlainText("driver licence"))
      .sequence(UnsignedInteger.valueOf(3195631))
      .fee(XrpCurrencyAmount.ofDrops(1))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84")
      )
      .flags(TransactionFlags.UNSET)
      .build();

    String json = "{\n" +
                  "   \"Account\":\"rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c\",\n" +
                  "   \"Issuer\":\"rffipZrgdn3ZYo4o2JN4Cymf3sP9m4rKuU\",\n" +
                  "   \"CredentialType\":\"647269766572206C6963656E6365\",\n" +
                  "   \"Fee\":\"1\",\n" +
                  "   \"Sequence\":3195631,\n" +
                  "   \"SigningPubKey\":\"ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84\",\n" +
                  "   \"Flags\": 0,\n" +
                  "   \"TransactionType\":\"CredentialDelete\"\n" +
                  "}";

    assertCanSerializeAndDeserialize(credentialDelete, json);
  }

  @Test
  public void testCredentialDeleteJsonWithNonZeroFlags() throws JsonProcessingException, JSONException {
    CredentialDelete credentialDelete = CredentialDelete.builder()
      .account(Address.of("rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c"))
      .issuer(Address.of("rffipZrgdn3ZYo4o2JN4Cymf3sP9m4rKuU"))
      .credentialType(CredentialType.ofPlainText("driver licence"))
      .sequence(UnsignedInteger.valueOf(3195631))
      .fee(XrpCurrencyAmount.ofDrops(1))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84")
      )
      .flags(TransactionFlags.FULLY_CANONICAL_SIG)
      .build();

    String json = "{\n" +
                  "   \"Account\":\"rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c\",\n" +
                  "   \"Issuer\":\"rffipZrgdn3ZYo4o2JN4Cymf3sP9m4rKuU\",\n" +
                  "   \"CredentialType\":\"647269766572206C6963656E6365\",\n" +
                  "   \"Fee\":\"1\",\n" +
                  "   \"Sequence\":3195631,\n" +
                  "   \"SigningPubKey\":\"ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84\",\n" +
                  "   \"Flags\": 2147483648,\n" +
                  "   \"TransactionType\":\"CredentialDelete\"\n" +
                  "}";

    assertCanSerializeAndDeserialize(credentialDelete, json);
  }

  @Test
  public void testCredentialDeleteJsonWithUnknownFields() throws JsonProcessingException, JSONException {
    CredentialDelete credentialDelete = CredentialDelete.builder()
      .account(Address.of("rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c"))
      .issuer(Address.of("rffipZrgdn3ZYo4o2JN4Cymf3sP9m4rKuU"))
      .credentialType(CredentialType.ofPlainText("driver licence"))
      .sequence(UnsignedInteger.valueOf(3195631))
      .fee(XrpCurrencyAmount.ofDrops(1))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84")
      )
      .flags(TransactionFlags.FULLY_CANONICAL_SIG)
      .putUnknownFields("Foo", "Bar")
      .build();

    String json = "{\n" +
                  "   \"Account\":\"rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c\",\n" +
                  "   \"Issuer\":\"rffipZrgdn3ZYo4o2JN4Cymf3sP9m4rKuU\",\n" +
                  "   \"CredentialType\":\"647269766572206C6963656E6365\",\n" +
                  "   \"Fee\":\"1\",\n" +
                  "   \"Sequence\":3195631,\n" +
                  "   \"SigningPubKey\":\"ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84\",\n" +
                  "   \"Flags\": 2147483648,\n" +
                  "   \"Foo\" : \"Bar\",\n" +
                  "   \"TransactionType\":\"CredentialDelete\"\n" +
                  "}";

    assertCanSerializeAndDeserialize(credentialDelete, json);
  }
}
