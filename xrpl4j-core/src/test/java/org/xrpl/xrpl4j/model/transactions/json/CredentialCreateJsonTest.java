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
import org.xrpl.xrpl4j.model.transactions.CredentialCreate;
import org.xrpl.xrpl4j.model.transactions.CredentialType;
import org.xrpl.xrpl4j.model.transactions.CredentialUri;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class CredentialCreateJsonTest extends AbstractJsonTest {

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

    String json = "{" +
      "  \"Account\":\"rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c\"," +
      "  \"CredentialType\":\"647269766572206C6963656E6365\"," +
      "  \"Fee\":\"1\"," +
      "  \"Sequence\":3195631," +
      "  \"SigningPubKey\":\"ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84\"," +
      "  \"Subject\":\"rffipZrgdn3ZYo4o2JN4Cymf3sP9m4rKuU\"," +
      "  \"TransactionType\":\"CredentialCreate\"," +
      "  \"URI\":\"68747470733A2F2F73616D706C652D76632E706466\"" +
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

    String json = "{" +
      "  \"Account\":\"rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c\"," +
      "  \"CredentialType\":\"647269766572206C6963656E6365\"," +
      "  \"Fee\":\"1\"," +
      "  \"Sequence\":3195631," +
      "  \"SigningPubKey\":\"ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84\"," +
      "  \"Subject\":\"rffipZrgdn3ZYo4o2JN4Cymf3sP9m4rKuU\"," +
      "  \"TransactionType\":\"CredentialCreate\"," +
      "  \"Flags\": 0," +
      "  \"URI\":\"68747470733A2F2F73616D706C652D76632E706466\"" +
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

    String json = "{" +
      "  \"Account\":\"rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c\"," +
      "  \"CredentialType\":\"647269766572206C6963656E6365\"," +
      "  \"Fee\":\"1\"," +
      "  \"Sequence\":3195631," +
      "  \"SigningPubKey\":\"ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84\"," +
      "  \"Subject\":\"rffipZrgdn3ZYo4o2JN4Cymf3sP9m4rKuU\"," +
      "  \"TransactionType\":\"CredentialCreate\"," +
      "  \"Flags\": 2147483648," +
      "  \"URI\":\"68747470733A2F2F73616D706C652D76632E706466\"" +
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

    String json = "{" +
      "  \"Account\":\"rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c\"," +
      "  \"CredentialType\":\"647269766572206C6963656E6365\"," +
      "  \"Fee\":\"1\"," +
      "  \"Sequence\":3195631," +
      "  \"SigningPubKey\":\"ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84\"," +
      "  \"Subject\":\"rffipZrgdn3ZYo4o2JN4Cymf3sP9m4rKuU\"," +
      "  \"TransactionType\":\"CredentialCreate\"," +
      "  \"Flags\": 2147483648," +
      "  \"Foo\" : \"Bar\"," +
      "  \"URI\":\"68747470733A2F2F73616D706C652D76632E706466\"" +
      "}";

    assertCanSerializeAndDeserialize(credentialCreate, json);
  }

}
