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
import org.xrpl.xrpl4j.model.transactions.CredentialType;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class CredentialAcceptJsonTest extends AbstractJsonTest {

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

    String json = "{" +
      "  \"Account\":\"rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c\"," +
      "  \"Issuer\":\"rffipZrgdn3ZYo4o2JN4Cymf3sP9m4rKuU\"," +
      "  \"CredentialType\":\"647269766572206C6963656E6365\"," +
      "  \"Fee\":\"1\"," +
      "  \"Sequence\":3195631," +
      "  \"SigningPubKey\":\"ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84\"," +
      "  \"TransactionType\":\"CredentialAccept\"" +
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

    String json = "{" +
      "  \"Account\":\"rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c\"," +
      "  \"Issuer\":\"rffipZrgdn3ZYo4o2JN4Cymf3sP9m4rKuU\"," +
      "  \"CredentialType\":\"647269766572206C6963656E6365\"," +
      "  \"Fee\":\"1\"," +
      "  \"Sequence\":3195631," +
      "  \"SigningPubKey\":\"ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84\"," +
      "  \"Flags\": 0," +
      "  \"TransactionType\":\"CredentialAccept\"" +
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

    String json = "{" +
      "  \"Account\":\"rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c\"," +
      "  \"Issuer\":\"rffipZrgdn3ZYo4o2JN4Cymf3sP9m4rKuU\"," +
      "  \"CredentialType\":\"647269766572206C6963656E6365\"," +
      "  \"Fee\":\"1\"," +
      "  \"Sequence\":3195631," +
      "  \"SigningPubKey\":\"ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84\"," +
      "  \"Flags\": 2147483648," +
      "  \"TransactionType\":\"CredentialAccept\"" +
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

    String json = "{" +
      "  \"Account\":\"rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c\"," +
      "  \"Issuer\":\"rffipZrgdn3ZYo4o2JN4Cymf3sP9m4rKuU\"," +
      "  \"CredentialType\":\"647269766572206C6963656E6365\"," +
      "  \"Fee\":\"1\"," +
      "  \"Sequence\":3195631," +
      "  \"SigningPubKey\":\"ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84\"," +
      "  \"Flags\": 2147483648," +
      "  \"Foo\" : \"Bar\"," +
      "  \"TransactionType\":\"CredentialAccept\"" +
      "}";

    assertCanSerializeAndDeserialize(credentialAccept, json);
  }

}
