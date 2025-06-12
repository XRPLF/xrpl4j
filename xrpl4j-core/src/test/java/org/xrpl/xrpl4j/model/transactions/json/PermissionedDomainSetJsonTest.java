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
import org.xrpl.xrpl4j.model.transactions.Credential;
import org.xrpl.xrpl4j.model.transactions.CredentialType;
import org.xrpl.xrpl4j.model.transactions.CredentialWrapper;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.PermissionedDomainSet;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PermissionedDomainSetJsonTest extends AbstractJsonTest {

  @Test
  public void testPermissionedDomainSetJson() throws JsonProcessingException, JSONException {
    List<CredentialWrapper> acceptedCredentials = getCredentials(10);

    PermissionedDomainSet permissionedDomainSet = PermissionedDomainSet.builder()
      .account(Address.of("rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c"))
      .sequence(UnsignedInteger.valueOf(3195631))
      .fee(XrpCurrencyAmount.ofDrops(1))
      .domainId(Hash256.of("7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF0"))
      .acceptedCredentials(acceptedCredentials)
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84")
      )
      .build();

    String json = "{" +
      "  \"Account\":\"rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c\"," +
      "  \"Fee\":\"1\"," +
      "  \"Sequence\":3195631," +
      "  \"SigningPubKey\":\"ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84\"," +
      "  \"DomainID\":\"7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF0\"," +
      "  \"AcceptedCredentials\":" + objectMapper.writeValueAsString(acceptedCredentials) + "," +
      "  \"TransactionType\":\"PermissionedDomainSet\"" +
      "}";

    assertCanSerializeAndDeserialize(permissionedDomainSet, json);
  }

  @Test
  public void testPermissionedDomainSetJsonWithUnsetFlags() throws JsonProcessingException, JSONException {
    List<CredentialWrapper> acceptedCredentials = getCredentials(1);

    PermissionedDomainSet permissionedDomainSet = PermissionedDomainSet.builder()
      .account(Address.of("rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c"))
      .sequence(UnsignedInteger.valueOf(3195631))
      .fee(XrpCurrencyAmount.ofDrops(1))
      .domainId(Hash256.of("7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF0"))
      .acceptedCredentials(acceptedCredentials)
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84")
      )
      .flags(TransactionFlags.UNSET)
      .build();

    String json = "{" +
      "  \"Account\":\"rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c\"," +
      "  \"Fee\":\"1\"," +
      "  \"Sequence\":3195631," +
      "  \"SigningPubKey\":\"ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84\"," +
      "  \"DomainID\":\"7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF0\"," +
      "  \"AcceptedCredentials\":" + objectMapper.writeValueAsString(acceptedCredentials) + "," +
      "  \"Flags\": 0," +
      "  \"TransactionType\":\"PermissionedDomainSet\"" +
      "}";

    assertCanSerializeAndDeserialize(permissionedDomainSet, json);
  }

  @Test
  public void testPermissionedDomainSetJsonWithNonZeroFlags() throws JsonProcessingException, JSONException {
    List<CredentialWrapper> acceptedCredentials = getCredentials(1);

    PermissionedDomainSet permissionedDomainSet = PermissionedDomainSet.builder()
      .account(Address.of("rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c"))
      .sequence(UnsignedInteger.valueOf(3195631))
      .fee(XrpCurrencyAmount.ofDrops(1))
      .domainId(Hash256.of("7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF0"))
      .acceptedCredentials(acceptedCredentials)
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84")
      )
      .flags(TransactionFlags.FULLY_CANONICAL_SIG)
      .build();

    String json = "{" +
      "  \"Account\":\"rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c\"," +
      "  \"Fee\":\"1\"," +
      "  \"Sequence\":3195631," +
      "  \"SigningPubKey\":\"ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84\"," +
      "  \"DomainID\":\"7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF0\"," +
      "  \"AcceptedCredentials\":" + objectMapper.writeValueAsString(acceptedCredentials) + "," +
      "  \"Flags\": 2147483648," +
      "  \"TransactionType\":\"PermissionedDomainSet\"" +
      "}";

    assertCanSerializeAndDeserialize(permissionedDomainSet, json);
  }

  @Test
  public void testPermissionedDomainSetJsonWithUnknownFields() throws JsonProcessingException, JSONException {
    List<CredentialWrapper> acceptedCredentials = getCredentials(1);

    PermissionedDomainSet permissionedDomainSet = PermissionedDomainSet.builder()
      .account(Address.of("rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c"))
      .sequence(UnsignedInteger.valueOf(3195631))
      .fee(XrpCurrencyAmount.ofDrops(1))
      .domainId(Hash256.of("7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF0"))
      .acceptedCredentials(acceptedCredentials)
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84")
      )
      .flags(TransactionFlags.FULLY_CANONICAL_SIG)
      .putUnknownFields("Foo", "Bar")
      .build();

    String json = "{" +
      "  \"Account\":\"rNGBsV3xEfGyc4JXoZSFT7DYfgGCjLyh3c\"," +
      "  \"Fee\":\"1\"," +
      "  \"Sequence\":3195631," +
      "  \"SigningPubKey\":\"ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84\"," +
      "  \"DomainID\":\"7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF0\"," +
      "  \"AcceptedCredentials\":" + objectMapper.writeValueAsString(acceptedCredentials) + "," +
      "  \"Flags\": 2147483648," +
      "  \"Foo\" : \"Bar\"," +
      "  \"TransactionType\":\"PermissionedDomainSet\"" +
      "}";

    assertCanSerializeAndDeserialize(permissionedDomainSet, json);
  }

  //Private helpers

  private List<CredentialWrapper> getCredentials(int size) {
    return IntStream.range(0, size)
      .mapToObj(i -> CredentialWrapper.builder()
        .credential(Credential.builder()
          .issuer(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
          .credentialType(CredentialType.ofPlainText("Driver licence - " + i))
          .build()
        )
        .build()
      )
      .collect(Collectors.toList());
  }
}