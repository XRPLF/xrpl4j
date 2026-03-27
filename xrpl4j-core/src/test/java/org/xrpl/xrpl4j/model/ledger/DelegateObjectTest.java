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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.transactions.AccountPermission;
import org.xrpl.xrpl4j.model.transactions.AccountPermissionWrapper;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.TransactionType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link DelegateObject} focusing on object behavior and JSON validation.
 */
public class DelegateObjectTest extends AbstractJsonTest {

  private static final Address ACCOUNT = Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8");
  private static final Address AUTHORIZE = Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de");
  private static final Hash256 PREVIOUS_TXN_ID =
    Hash256.of("3E8964D5A86B3CD6B9ECB33310D4E073D64C865A5B866200AD2B7E29F8326702");
  private static final Hash256 INDEX =
    Hash256.of("4A255038CC3ADCC1A9C91509279B59908251728D0DAADB248FFE297D0F7E068C");
  private static final UnsignedInteger PREVIOUS_TXN_LEDGER_SEQ = UnsignedInteger.valueOf(7);
  private static final String OWNER_NODE = "0";

  @Test
  void testBuilderWithRequiredFields() {
    DelegateObject delegateObject = DelegateObject.builder()
      .account(ACCOUNT)
      .authorize(AUTHORIZE)
      .permissions(Collections.singletonList(
        AccountPermissionWrapper.builder()
          .permission(AccountPermission.of(TransactionType.PAYMENT))
          .build()
      ))
      .ownerNode(OWNER_NODE)
      .previousTransactionId(PREVIOUS_TXN_ID)
      .previousTransactionLedgerSequence(PREVIOUS_TXN_LEDGER_SEQ)
      .index(INDEX)
      .build();

    assertThat(delegateObject.account()).isEqualTo(ACCOUNT);
    assertThat(delegateObject.authorize()).isEqualTo(AUTHORIZE);
    assertThat(delegateObject.permissions()).hasSize(1);
    assertThat(delegateObject.ownerNode()).isEqualTo(OWNER_NODE);
    assertThat(delegateObject.previousTransactionId()).isEqualTo(PREVIOUS_TXN_ID);
    assertThat(delegateObject.previousTransactionLedgerSequence()).isEqualTo(PREVIOUS_TXN_LEDGER_SEQ);
    assertThat(delegateObject.index()).isEqualTo(INDEX);
  }

  @Test
  void testDerivedLedgerEntryType() {
    DelegateObject delegateObject = createMinimalDelegateObject();
    assertThat(delegateObject.ledgerEntryType()).isEqualTo(LedgerObject.LedgerEntryType.DELEGATE);
  }

  @Test
  void testDerivedFlags() {
    DelegateObject delegateObject = createMinimalDelegateObject();
    assertThat(delegateObject.flags()).isEqualTo(Flags.UNSET);
  }

  @Test
  void testWithMultiplePermissions() {
    List<AccountPermissionWrapper> permissions = Arrays.asList(
      AccountPermissionWrapper.builder()
        .permission(AccountPermission.of(TransactionType.PAYMENT))
        .build(),
      AccountPermissionWrapper.builder()
        .permission(AccountPermission.of(TransactionType.TRUST_SET))
        .build(),
      AccountPermissionWrapper.builder()
        .permission(AccountPermission.of(TransactionType.OFFER_CREATE))
        .build()
    );

    DelegateObject delegateObject = DelegateObject.builder()
      .account(ACCOUNT)
      .authorize(AUTHORIZE)
      .permissions(permissions)
      .ownerNode(OWNER_NODE)
      .previousTransactionId(PREVIOUS_TXN_ID)
      .previousTransactionLedgerSequence(PREVIOUS_TXN_LEDGER_SEQ)
      .index(INDEX)
      .build();

    assertThat(delegateObject.permissions()).hasSize(3);
    String value0 = delegateObject.permissions().get(0).permission().permissionValue().map(tx -> tx.value(), g -> g.value());
    String value1 = delegateObject.permissions().get(1).permission().permissionValue().map(tx -> tx.value(), g -> g.value());
    String value2 = delegateObject.permissions().get(2).permission().permissionValue().map(tx -> tx.value(), g -> g.value());
    assertThat(value0).isEqualTo("Payment");
    assertThat(value1).isEqualTo("TrustSet");
    assertThat(value2).isEqualTo("OfferCreate");
  }

  @Test
  void testWithSinglePermission() {
    DelegateObject delegateObject = createMinimalDelegateObject();
    assertThat(delegateObject.permissions()).hasSize(1);
    String permissionValue = delegateObject.permissions().get(0).permission().permissionValue().map(tx -> tx.value(), g -> g.value());
    assertThat(permissionValue).isEqualTo("Payment");
  }

  @Test
  void testEquality() {
    DelegateObject delegateObject1 = createMinimalDelegateObject();
    DelegateObject delegateObject2 = createMinimalDelegateObject();

    assertThat(delegateObject1).isEqualTo(delegateObject2);
    assertThat(delegateObject1.hashCode()).isEqualTo(delegateObject2.hashCode());
  }

  @Test
  void testInequality() {
    DelegateObject delegateObject1 = createMinimalDelegateObject();
    DelegateObject delegateObject2 = DelegateObject.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk"))
      .authorize(AUTHORIZE)
      .permissions(Collections.singletonList(
        AccountPermissionWrapper.builder()
          .permission(AccountPermission.of(TransactionType.PAYMENT))
          .build()
      ))
      .ownerNode(OWNER_NODE)
      .previousTransactionId(PREVIOUS_TXN_ID)
      .previousTransactionLedgerSequence(PREVIOUS_TXN_LEDGER_SEQ)
      .index(INDEX)
      .build();

    assertThat(delegateObject1).isNotEqualTo(delegateObject2);
  }

  @Test
  void testWithDifferentOwnerNode() {
    DelegateObject delegateObject = DelegateObject.builder()
      .account(ACCOUNT)
      .authorize(AUTHORIZE)
      .permissions(Collections.singletonList(
        AccountPermissionWrapper.builder()
          .permission(AccountPermission.of(TransactionType.PAYMENT))
          .build()
      ))
      .ownerNode("ABCD1234")
      .previousTransactionId(PREVIOUS_TXN_ID)
      .previousTransactionLedgerSequence(PREVIOUS_TXN_LEDGER_SEQ)
      .index(INDEX)
      .build();

    assertThat(delegateObject.ownerNode()).isEqualTo("ABCD1234");
  }

  @Test
  void testWithDifferentLedgerSequence() {
    UnsignedInteger highLedgerSeq = UnsignedInteger.valueOf(1000000);
    DelegateObject delegateObject = DelegateObject.builder()
      .account(ACCOUNT)
      .authorize(AUTHORIZE)
      .permissions(Collections.singletonList(
        AccountPermissionWrapper.builder()
          .permission(AccountPermission.of(TransactionType.PAYMENT))
          .build()
      ))
      .ownerNode(OWNER_NODE)
      .previousTransactionId(PREVIOUS_TXN_ID)
      .previousTransactionLedgerSequence(highLedgerSeq)
      .index(INDEX)
      .build();

    assertThat(delegateObject.previousTransactionLedgerSequence()).isEqualTo(highLedgerSeq);
  }

  @Test
  void testImmutability() {
    List<AccountPermissionWrapper> permissions = Arrays.asList(
      AccountPermissionWrapper.builder()
        .permission(AccountPermission.of(TransactionType.PAYMENT))
        .build()
    );

    DelegateObject delegateObject = DelegateObject.builder()
      .account(ACCOUNT)
      .authorize(AUTHORIZE)
      .permissions(permissions)
      .ownerNode(OWNER_NODE)
      .previousTransactionId(PREVIOUS_TXN_ID)
      .previousTransactionLedgerSequence(PREVIOUS_TXN_LEDGER_SEQ)
      .index(INDEX)
      .build();

    // Verify that the returned list is immutable
    assertThrows(UnsupportedOperationException.class, () -> {
      delegateObject.permissions().add(
        AccountPermissionWrapper.builder()
          .permission(AccountPermission.of(TransactionType.TRUST_SET))
          .build()
      );
    });
  }

  @Test
  void testToString() {
    DelegateObject delegateObject = createMinimalDelegateObject();
    String toString = delegateObject.toString();

    assertThat(toString).contains("DelegateObject");
    assertThat(toString).contains(ACCOUNT.value());
    assertThat(toString).contains(AUTHORIZE.value());
  }

  @Test
  void testWithEmptyPermissionsList() {
    DelegateObject delegateObject = DelegateObject.builder()
      .account(ACCOUNT)
      .authorize(AUTHORIZE)
      .permissions(Collections.emptyList())
      .ownerNode(OWNER_NODE)
      .previousTransactionId(PREVIOUS_TXN_ID)
      .previousTransactionLedgerSequence(PREVIOUS_TXN_LEDGER_SEQ)
      .index(INDEX)
      .build();

    assertThat(delegateObject.permissions()).isEmpty();
  }

  @Test
  void testAccountAndAuthorizeAreDifferent() {
    DelegateObject delegateObject = createMinimalDelegateObject();
    assertThat(delegateObject.account()).isNotEqualTo(delegateObject.authorize());
  }

  private DelegateObject createMinimalDelegateObject() {
    return DelegateObject.builder()
      .account(ACCOUNT)
      .authorize(AUTHORIZE)
      .permissions(Collections.singletonList(
        AccountPermissionWrapper.builder()
          .permission(AccountPermission.of(TransactionType.PAYMENT))
          .build()
      ))
      .ownerNode(OWNER_NODE)
      .previousTransactionId(PREVIOUS_TXN_ID)
      .previousTransactionLedgerSequence(PREVIOUS_TXN_LEDGER_SEQ)
      .index(INDEX)
      .build();
  }

  // ========== JSON Validation Tests ==========

  @Test
  void testJsonWithMinimalFields() throws JsonProcessingException, JSONException {
    DelegateObject delegateObject = DelegateObject.builder()
      .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
      .authorize(Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de"))
      .ownerNode("0")
      .previousTransactionId(Hash256.of("3E8964D5A86B3CD6B9ECB33310D4E073D64C865A5B866200AD2B7E29F8326702"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(7))
      .index(Hash256.of("4A255038CC3ADCC1A9C91509279B59908251728D0DAADB248FFE297D0F7E068C"))
      .build();

    String json = "{" +
      "\"LedgerEntryType\":\"Delegate\"," +
      "\"Account\":\"rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8\"," +
      "\"Authorize\":\"rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de\"," +
      "\"Flags\":0," +
      "\"OwnerNode\":\"0\"," +
      "\"PreviousTxnID\":\"3E8964D5A86B3CD6B9ECB33310D4E073D64C865A5B866200AD2B7E29F8326702\"," +
      "\"PreviousTxnLgrSeq\":7," +
      "\"index\":\"4A255038CC3ADCC1A9C91509279B59908251728D0DAADB248FFE297D0F7E068C\"" +
      "}";

    assertCanSerializeAndDeserialize(delegateObject, json);
  }

  @Test
  void testJsonWithSinglePermission() throws JsonProcessingException, JSONException {
    DelegateObject delegateObject = DelegateObject.builder()
      .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
      .authorize(Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de"))
      .permissions(Collections.singletonList(
        AccountPermissionWrapper.builder()
          .permission(AccountPermission.of(TransactionType.PAYMENT))
          .build()
      ))
      .ownerNode("0")
      .previousTransactionId(Hash256.of("3E8964D5A86B3CD6B9ECB33310D4E073D64C865A5B866200AD2B7E29F8326702"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(7))
      .index(Hash256.of("4A255038CC3ADCC1A9C91509279B59908251728D0DAADB248FFE297D0F7E068C"))
      .build();

    String json = "{" +
      "\"LedgerEntryType\":\"Delegate\"," +
      "\"Account\":\"rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8\"," +
      "\"Authorize\":\"rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de\"," +
      "\"Permissions\":[{\"Permission\":{\"PermissionValue\":\"Payment\"}}]," +
      "\"Flags\":0," +
      "\"OwnerNode\":\"0\"," +
      "\"PreviousTxnID\":\"3E8964D5A86B3CD6B9ECB33310D4E073D64C865A5B866200AD2B7E29F8326702\"," +
      "\"PreviousTxnLgrSeq\":7," +
      "\"index\":\"4A255038CC3ADCC1A9C91509279B59908251728D0DAADB248FFE297D0F7E068C\"" +
      "}";

    assertCanSerializeAndDeserialize(delegateObject, json);
  }

  @Test
  void testJsonWithMultiplePermissions() throws JsonProcessingException, JSONException {
    DelegateObject delegateObject = DelegateObject.builder()
      .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
      .authorize(Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de"))
      .permissions(Arrays.asList(
        AccountPermissionWrapper.builder()
          .permission(AccountPermission.of(TransactionType.PAYMENT))
          .build(),
        AccountPermissionWrapper.builder()
          .permission(AccountPermission.of(TransactionType.TRUST_SET))
          .build(),
        AccountPermissionWrapper.builder()
          .permission(AccountPermission.of(TransactionType.OFFER_CREATE))
          .build()
      ))
      .ownerNode("0")
      .previousTransactionId(Hash256.of("3E8964D5A86B3CD6B9ECB33310D4E073D64C865A5B866200AD2B7E29F8326702"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(7))
      .index(Hash256.of("4A255038CC3ADCC1A9C91509279B59908251728D0DAADB248FFE297D0F7E068C"))
      .build();

    String json = "{" +
      "\"LedgerEntryType\":\"Delegate\"," +
      "\"Account\":\"rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8\"," +
      "\"Authorize\":\"rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de\"," +
      "\"Permissions\":[" +
      "{\"Permission\":{\"PermissionValue\":\"Payment\"}}," +
      "{\"Permission\":{\"PermissionValue\":\"TrustSet\"}}," +
      "{\"Permission\":{\"PermissionValue\":\"OfferCreate\"}}" +
      "]," +
      "\"Flags\":0," +
      "\"OwnerNode\":\"0\"," +
      "\"PreviousTxnID\":\"3E8964D5A86B3CD6B9ECB33310D4E073D64C865A5B866200AD2B7E29F8326702\"," +
      "\"PreviousTxnLgrSeq\":7," +
      "\"index\":\"4A255038CC3ADCC1A9C91509279B59908251728D0DAADB248FFE297D0F7E068C\"" +
      "}";

    assertCanSerializeAndDeserialize(delegateObject, json);
  }

  @Test
  void testJsonWithHighLedgerSequence() throws JsonProcessingException, JSONException {
    DelegateObject delegateObject = DelegateObject.builder()
      .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
      .authorize(Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de"))
      .ownerNode("ABCD")
      .previousTransactionId(Hash256.of("3E8964D5A86B3CD6B9ECB33310D4E073D64C865A5B866200AD2B7E29F8326702"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(999999999))
      .index(Hash256.of("4A255038CC3ADCC1A9C91509279B59908251728D0DAADB248FFE297D0F7E068C"))
      .build();

    String json = "{" +
      "\"LedgerEntryType\":\"Delegate\"," +
      "\"Account\":\"rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8\"," +
      "\"Authorize\":\"rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de\"," +
      "\"Flags\":0," +
      "\"OwnerNode\":\"ABCD\"," +
      "\"PreviousTxnID\":\"3E8964D5A86B3CD6B9ECB33310D4E073D64C865A5B866200AD2B7E29F8326702\"," +
      "\"PreviousTxnLgrSeq\":999999999," +
      "\"index\":\"4A255038CC3ADCC1A9C91509279B59908251728D0DAADB248FFE297D0F7E068C\"" +
      "}";

    assertCanSerializeAndDeserialize(delegateObject, json);
  }

  @Test
  void testJsonWithEmptyPermissions() throws JsonProcessingException, JSONException {
    DelegateObject delegateObject = DelegateObject.builder()
      .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
      .authorize(Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de"))
      .permissions(Collections.emptyList())
      .ownerNode("0")
      .previousTransactionId(Hash256.of("3E8964D5A86B3CD6B9ECB33310D4E073D64C865A5B866200AD2B7E29F8326702"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(7))
      .index(Hash256.of("4A255038CC3ADCC1A9C91509279B59908251728D0DAADB248FFE297D0F7E068C"))
      .build();

    // Note: Empty permissions list is not serialized to JSON
    String json = "{" +
      "\"LedgerEntryType\":\"Delegate\"," +
      "\"Account\":\"rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8\"," +
      "\"Authorize\":\"rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de\"," +
      "\"Flags\":0," +
      "\"OwnerNode\":\"0\"," +
      "\"PreviousTxnID\":\"3E8964D5A86B3CD6B9ECB33310D4E073D64C865A5B866200AD2B7E29F8326702\"," +
      "\"PreviousTxnLgrSeq\":7," +
      "\"index\":\"4A255038CC3ADCC1A9C91509279B59908251728D0DAADB248FFE297D0F7E068C\"" +
      "}";

    assertCanSerializeAndDeserialize(delegateObject, json);
  }
}

