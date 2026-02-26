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
import org.xrpl.xrpl4j.model.transactions.AccountPermission;
import org.xrpl.xrpl4j.model.transactions.AccountPermissionWrapper;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.DelegateSet;
import org.xrpl.xrpl4j.model.transactions.GranularPermission;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Arrays;

/**
 * Unit tests for {@link DelegateSet} JSON serialization and deserialization.
 */
public class DelegateSetJsonTest extends AbstractJsonTest {
  @Test
  public void testDelegateSetJsonWithPermissions() throws JsonProcessingException, JSONException {
    DelegateSet delegateSet = DelegateSet.builder()
      .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
      .authorize(Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de"))
      .permissions(Arrays.asList(
        AccountPermissionWrapper.builder()
          .permission(AccountPermission.builder().permissionValue("Payment").build())
          .build(),
        AccountPermissionWrapper.builder()
          .permission(AccountPermission.builder().permissionValue("TrustSet").build())
          .build()
      ))
      .sequence(UnsignedInteger.valueOf(2))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84")
      )
      .build();

    String json = "{" +
      "  \"Account\":\"rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8\"," +
      "  \"Authorize\":\"rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de\"," +
      "  \"Permissions\":[" +
      "    {\"Permission\":{\"PermissionValue\":\"Payment\"}}," +
      "    {\"Permission\":{\"PermissionValue\":\"TrustSet\"}}" +
      "  ]," +
      "  \"Fee\":\"10\"," +
      "  \"Sequence\":2," +
      "  \"SigningPubKey\":\"ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84\"," +
      "  \"TransactionType\":\"DelegateSet\"" +
      "}";

    assertCanSerializeAndDeserialize(delegateSet, json);
  }

  @Test
  public void testDelegateSetJsonWithFlags() throws JsonProcessingException, JSONException {
    DelegateSet delegateSet = DelegateSet.builder()
      .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
      .authorize(Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de"))
      .permissions(Arrays.asList(
        AccountPermissionWrapper.builder()
          .permission(AccountPermission.builder().permissionValue("Payment").build())
          .build()
      ))
      .sequence(UnsignedInteger.valueOf(2))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84")
      )
      .flags(TransactionFlags.FULLY_CANONICAL_SIG)
      .build();

    String json = "{" +
      "  \"Account\":\"rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8\"," +
      "  \"Authorize\":\"rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de\"," +
      "  \"Permissions\":[" +
      "    {\"Permission\":{\"PermissionValue\":\"Payment\"}}" +
      "  ]," +
      "  \"Fee\":\"10\"," +
      "  \"Sequence\":2," +
      "  \"SigningPubKey\":\"ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84\"," +
      "  \"TransactionType\":\"DelegateSet\"," +
      "  \"Flags\":2147483648" +
      "}";

    assertCanSerializeAndDeserialize(delegateSet, json);
  }

  @Test
  public void testDelegateSetJsonWithNoPermissions() throws JsonProcessingException, JSONException {
    DelegateSet delegateSet = DelegateSet.builder()
            .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
            .authorize(Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de"))
            .sequence(UnsignedInteger.valueOf(2))
            .fee(XrpCurrencyAmount.ofDrops(10))
            .signingPublicKey(
                    PublicKey.fromBase16EncodedPublicKey(
                      "ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84"
                    )
            )
            .build();

    String json = "{" +
            "  \"Account\":\"rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8\"," +
            "  \"Authorize\":\"rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de\"," +
            "  \"Permissions\":[]," +
            "  \"Fee\":\"10\"," +
            "  \"Sequence\":2," +
            "  \"SigningPubKey\":\"ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84\"," +
            "  \"TransactionType\":\"DelegateSet\"" +
            "}";

    assertCanSerializeAndDeserialize(delegateSet, json);
  }

  @Test
  public void testDelegateSetJsonWithGranularPermissions() throws JsonProcessingException, JSONException {
    DelegateSet delegateSet = DelegateSet.builder()
      .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
      .authorize(Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de"))
      .permissions(Arrays.asList(
        AccountPermissionWrapper.builder()
          .permission(AccountPermission.of(GranularPermission.TRUSTLINE_AUTHORIZE))
          .build(),
        AccountPermissionWrapper.builder()
          .permission(AccountPermission.of(GranularPermission.PAYMENT_MINT))
          .build(),
        AccountPermissionWrapper.builder()
          .permission(AccountPermission.of(GranularPermission.TRUSTLINE_FREEZE))
          .build()
      ))
      .sequence(UnsignedInteger.valueOf(2))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84")
      )
      .build();

    String json = "{" +
      "  \"Account\":\"rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8\"," +
      "  \"Authorize\":\"rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de\"," +
      "  \"Permissions\":[" +
      "    {\"Permission\":{\"PermissionValue\":\"TrustlineAuthorize\"}}," +
      "    {\"Permission\":{\"PermissionValue\":\"PaymentMint\"}}," +
      "    {\"Permission\":{\"PermissionValue\":\"TrustlineFreeze\"}}" +
      "  ]," +
      "  \"Fee\":\"10\"," +
      "  \"Sequence\":2," +
      "  \"SigningPubKey\":\"ED87987410480E90474F7A02E0DA0CE4E6ABC8A1377864026A1FEE2718688B0B84\"," +
      "  \"TransactionType\":\"DelegateSet\"" +
      "}";

    assertCanSerializeAndDeserialize(delegateSet, json);
  }
}

