package org.xrpl.xrpl4j.model.ledger;

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
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Permission;
import org.xrpl.xrpl4j.model.transactions.PermissionWrapper;

import java.util.Arrays;
import java.util.Collections;

/**
 * Unit tests for {@link DelegateObject}.
 */
public class DelegateObjectJsonTest extends AbstractJsonTest {

  @Test
  public void testDelegateObjectJsonWithPermissions() throws JsonProcessingException, JSONException {
    DelegateObject delegateObject = DelegateObject.builder()
      .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
      .authorize(Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de"))
      .permissions(Arrays.asList(
        PermissionWrapper.builder()
          .permission(Permission.builder().permissionValue("Payment").build())
          .build(),
        PermissionWrapper.builder()
          .permission(Permission.builder().permissionValue("TrustSet").build())
          .build()
      ))
      .ownerNode("0")
      .previousTransactionId(Hash256.of("3E8964D5A86B3CD6B9ECB33310D4E073D64C865A5B866200AD2B7E29F8326702"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(7))
      .index(Hash256.of("4A255038CC3ADCC1A9C91509279B59908251728D0DAADB248FFE297D0F7E068C"))
      .build();

    String json = "{" +
      "  \"LedgerEntryType\":\"Delegate\"," +
      "  \"Account\":\"rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8\"," +
      "  \"Authorize\":\"rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de\"," +
      "  \"Permissions\":[" +
      "    {\"Permission\":{\"PermissionValue\":\"Payment\"}}," +
      "    {\"Permission\":{\"PermissionValue\":\"TrustSet\"}}" +
      "  ]," +
      "  \"Flags\":0," +
      "  \"OwnerNode\":\"0\"," +
      "  \"PreviousTxnID\":\"3E8964D5A86B3CD6B9ECB33310D4E073D64C865A5B866200AD2B7E29F8326702\"," +
      "  \"PreviousTxnLgrSeq\":7," +
      "  \"index\":\"4A255038CC3ADCC1A9C91509279B59908251728D0DAADB248FFE297D0F7E068C\"" +
      "}";

    assertCanSerializeAndDeserialize(delegateObject, json);
  }

  @Test
  public void testDelegateObjectJsonWithMultiplePermissions() throws JsonProcessingException, JSONException {
    DelegateObject delegateObject = DelegateObject.builder()
      .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
      .authorize(Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de"))
      .permissions(Arrays.asList(
        PermissionWrapper.builder()
          .permission(Permission.builder().permissionValue("Payment").build())
          .build(),
        PermissionWrapper.builder()
          .permission(Permission.builder().permissionValue("TrustSet").build())
          .build(),
        PermissionWrapper.builder()
          .permission(Permission.builder().permissionValue("OfferCreate").build())
          .build(),
        PermissionWrapper.builder()
          .permission(Permission.builder().permissionValue("OfferCancel").build())
          .build()
      ))
      .ownerNode("0")
      .previousTransactionId(Hash256.of("3E8964D5A86B3CD6B9ECB33310D4E073D64C865A5B866200AD2B7E29F8326702"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(7))
      .index(Hash256.of("4A255038CC3ADCC1A9C91509279B59908251728D0DAADB248FFE297D0F7E068C"))
      .build();

    String json = "{" +
      "  \"LedgerEntryType\":\"Delegate\"," +
      "  \"Account\":\"rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8\"," +
      "  \"Authorize\":\"rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de\"," +
      "  \"Permissions\":[" +
      "    {\"Permission\":{\"PermissionValue\":\"Payment\"}}," +
      "    {\"Permission\":{\"PermissionValue\":\"TrustSet\"}}," +
      "    {\"Permission\":{\"PermissionValue\":\"OfferCreate\"}}," +
      "    {\"Permission\":{\"PermissionValue\":\"OfferCancel\"}}" +
      "  ]," +
      "  \"Flags\":0," +
      "  \"OwnerNode\":\"0\"," +
      "  \"PreviousTxnID\":\"3E8964D5A86B3CD6B9ECB33310D4E073D64C865A5B866200AD2B7E29F8326702\"," +
      "  \"PreviousTxnLgrSeq\":7," +
      "  \"index\":\"4A255038CC3ADCC1A9C91509279B59908251728D0DAADB248FFE297D0F7E068C\"" +
      "}";

    assertCanSerializeAndDeserialize(delegateObject, json);
  }
}

