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
import org.xrpl.xrpl4j.model.transactions.AccountDelete;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.NetworkId;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Collections;

public class AccountDeleteJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    AccountDelete accountDelete = AccountDelete.builder()
      .account(Address.of("rWYkbWkCeg8dP6rXALnjgZSjjLyih5NXm"))
      .fee(XrpCurrencyAmount.ofDrops(5000000))
      .sequence(UnsignedInteger.valueOf(2470665))
      .destination(Address.of("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe"))
      .destinationTag(UnsignedInteger.valueOf(13))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .credentialIds(Collections.singletonList(
        Hash256.of("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37"))
      )
      .networkId(NetworkId.of(1024))
      .build();

    String json = "{" +
      "  \"TransactionType\": \"AccountDelete\"," +
      "  \"Account\": \"rWYkbWkCeg8dP6rXALnjgZSjjLyih5NXm\"," +
      "  \"Destination\": \"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe\"," +
      "  \"DestinationTag\": 13," +
      "  \"Fee\": \"5000000\"," +
      "  \"Sequence\": 2470665," +
      "  \"NetworkID\": 1024," +
      "  \"CredentialIDs\": [02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37]," +
      "  \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"" +
      "}";

    assertCanSerializeAndDeserialize(accountDelete, json);
  }

  @Test
  public void testJsonWithUnsetFlags() throws JsonProcessingException, JSONException {
    AccountDelete accountDelete = AccountDelete.builder()
      .account(Address.of("rWYkbWkCeg8dP6rXALnjgZSjjLyih5NXm"))
      .fee(XrpCurrencyAmount.ofDrops(5000000))
      .sequence(UnsignedInteger.valueOf(2470665))
      .destination(Address.of("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe"))
      .destinationTag(UnsignedInteger.valueOf(13))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .flags(TransactionFlags.UNSET)
      .build();

    String json = "{" +
      "  \"TransactionType\": \"AccountDelete\"," +
      "  \"Account\": \"rWYkbWkCeg8dP6rXALnjgZSjjLyih5NXm\"," +
      "  \"Destination\": \"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe\"," +
      "  \"DestinationTag\": 13," +
      "  \"Fee\": \"5000000\"," +
      "  \"Flags\": 0," +
      "  \"Sequence\": 2470665," +
      "  \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"" +
      "}";

    assertCanSerializeAndDeserialize(accountDelete, json);
  }

  @Test
  public void testJsonWithNonZeroFlags() throws JsonProcessingException, JSONException {
    AccountDelete accountDelete = AccountDelete.builder()
      .account(Address.of("rWYkbWkCeg8dP6rXALnjgZSjjLyih5NXm"))
      .fee(XrpCurrencyAmount.ofDrops(5000000))
      .sequence(UnsignedInteger.valueOf(2470665))
      .destination(Address.of("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe"))
      .destinationTag(UnsignedInteger.valueOf(13))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .flags(TransactionFlags.FULLY_CANONICAL_SIG)
      .build();

    String json = String.format("{" +
      "  \"TransactionType\": \"AccountDelete\"," +
      "  \"Account\": \"rWYkbWkCeg8dP6rXALnjgZSjjLyih5NXm\"," +
      "  \"Destination\": \"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe\"," +
      "  \"DestinationTag\": 13," +
      "  \"Fee\": \"5000000\"," +
      "  \"Flags\": %s," +
      "  \"Sequence\": 2470665," +
      "  \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"" +
      "}", TransactionFlags.FULLY_CANONICAL_SIG.getValue());

    assertCanSerializeAndDeserialize(accountDelete, json);
  }

  @Test
  public void testJsonWithUnknownFields() throws JsonProcessingException, JSONException {
    AccountDelete accountDelete = AccountDelete.builder()
      .account(Address.of("rWYkbWkCeg8dP6rXALnjgZSjjLyih5NXm"))
      .fee(XrpCurrencyAmount.ofDrops(5000000))
      .sequence(UnsignedInteger.valueOf(2470665))
      .destination(Address.of("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe"))
      .destinationTag(UnsignedInteger.valueOf(13))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .networkId(NetworkId.of(1024))
      .putUnknownFields("Foo", "Bar")
      .build();

    String json = "{" +
      "  \"Foo\" : \"Bar\"," +
      "  \"TransactionType\": \"AccountDelete\"," +
      "  \"Account\": \"rWYkbWkCeg8dP6rXALnjgZSjjLyih5NXm\"," +
      "  \"Destination\": \"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe\"," +
      "  \"DestinationTag\": 13," +
      "  \"Fee\": \"5000000\"," +
      "  \"Sequence\": 2470665," +
      "  \"NetworkID\": 1024," +
      "  \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"" +
      "}";

    assertCanSerializeAndDeserialize(accountDelete, json);
  }
}
