package org.xrpl.xrpl4j.model.transactions;

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

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Unit tests for {@link DelegateSet}.
 */
public class DelegateSetTest {

  @Test
  public void testDelegateSetWithAuthorize() {
    Address authorize = Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de");
    List<PermissionWrapper> permissions = Arrays.asList(
      PermissionWrapper.builder()
        .permission(Permission.builder().permissionValue("Payment").build())
        .build(),
      PermissionWrapper.builder()
        .permission(Permission.builder().permissionValue("TrustSet").build())
        .build()
    );

    DelegateSet delegateSet = DelegateSet.builder()
      .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(2))
      .authorize(authorize)
      .permissions(permissions)
      .build();

    assertThat(delegateSet.transactionType()).isEqualTo(TransactionType.DELEGATE_SET);
    assertThat(delegateSet.account()).isEqualTo(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"));
    assertThat(delegateSet.authorize()).isEqualTo(authorize);
    assertThat(delegateSet.permissions()).hasSize(2);
    assertThat(delegateSet.permissions().get(0).permission().permissionValue()).isEqualTo("Payment");
    assertThat(delegateSet.permissions().get(1).permission().permissionValue()).isEqualTo("TrustSet");
  }

  @Test
  public void testDelegateSetWithoutPermissions() {
    // DelegateSet without permissions should work (removes delegation)
    Address authorize = Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de");

    DelegateSet delegateSet = DelegateSet.builder()
      .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(2))
      .authorize(authorize)
      .permissions(Collections.emptyList())
      .build();

    assertThat(delegateSet.authorize()).isEqualTo(authorize);
    assertThat(delegateSet.permissions()).isEmpty();
  }

  @Test
  public void testDelegateSetValidationAuthorizeEqualsAccount() {
    Address sameAddress = Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8");
    List<PermissionWrapper> permissions = Arrays.asList(
      PermissionWrapper.builder()
        .permission(Permission.builder().permissionValue("Payment").build())
        .build()
    );

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      DelegateSet.builder()
        .account(sameAddress)
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.valueOf(2))
        .authorize(sameAddress)
        .permissions(permissions)
        .build();
    });

    assertThat(exception.getMessage()).contains("Authorize and Account must be different");
  }

  @Test
  public void testDelegateSetValidationTooManyPermissions() {
    // Create 11 permissions (exceeds max of 10) using valid transaction types
    TransactionType[] validTypes = {
      TransactionType.PAYMENT,
      TransactionType.TRUST_SET,
      TransactionType.OFFER_CREATE,
      TransactionType.OFFER_CANCEL,
      TransactionType.CHECK_CREATE,
      TransactionType.CHECK_CASH,
      TransactionType.CHECK_CANCEL,
      TransactionType.ESCROW_CREATE,
      TransactionType.ESCROW_FINISH,
      TransactionType.ESCROW_CANCEL,
      TransactionType.PAYMENT_CHANNEL_CREATE
    };

    List<PermissionWrapper> tooManyPermissions = Arrays.stream(validTypes)
      .map(txType -> PermissionWrapper.builder()
        .permission(Permission.of(txType))
        .build())
      .collect(Collectors.toList());

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      DelegateSet.builder()
        .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.valueOf(2))
        .authorize(Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de"))
        .permissions(tooManyPermissions)
        .build();
    });

    assertThat(exception.getMessage()).contains("Permissions array length cannot be greater than 10");
  }

  @Test
  public void testDelegateSetValidationDuplicatePermissions() {
    List<PermissionWrapper> duplicatePermissions = Arrays.asList(
      PermissionWrapper.builder()
        .permission(Permission.builder().permissionValue("Payment").build())
        .build(),
      PermissionWrapper.builder()
        .permission(Permission.builder().permissionValue("Payment").build())
        .build()
    );

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      DelegateSet.builder()
        .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.valueOf(2))
        .authorize(Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de"))
        .permissions(duplicatePermissions)
        .build();
    });

    assertThat(exception.getMessage()).contains("Permissions array cannot contain duplicate values");
  }

  @Test
  public void testDelegateSetValidationNonDelegatableTransactions() {
    // Test each non-delegatable transaction type
    String[] nonDelegatableTypes = {
      "AccountSet", "SetRegularKey", "SignerListSet", "DelegateSet",
      "AccountDelete", "Batch", "EnableAmendment", "SetFee", "UNLModify"
    };

    for (String txType : nonDelegatableTypes) {
      List<PermissionWrapper> permissions = Arrays.asList(
        PermissionWrapper.builder()
          .permission(Permission.builder().permissionValue(txType).build())
          .build()
      );

      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
        DelegateSet.builder()
          .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
          .fee(XrpCurrencyAmount.ofDrops(10))
          .sequence(UnsignedInteger.valueOf(2))
          .authorize(Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de"))
          .permissions(permissions)
          .build();
      });

      assertThat(exception.getMessage())
        .contains("PermissionValue contains a non-delegatable transaction");
    }
  }

  @Test
  public void testDelegateSetWithMaxPermissions() {
    // Test with exactly 10 permissions (should succeed) using valid transaction types
    TransactionType[] validTypes = {
      TransactionType.PAYMENT,
      TransactionType.TRUST_SET,
      TransactionType.OFFER_CREATE,
      TransactionType.OFFER_CANCEL,
      TransactionType.CHECK_CREATE,
      TransactionType.CHECK_CASH,
      TransactionType.CHECK_CANCEL,
      TransactionType.ESCROW_CREATE,
      TransactionType.ESCROW_FINISH,
      TransactionType.ESCROW_CANCEL
    };

    List<PermissionWrapper> maxPermissions = Arrays.stream(validTypes)
      .map(txType -> PermissionWrapper.builder()
        .permission(Permission.of(txType))
        .build())
      .collect(Collectors.toList());

    DelegateSet delegateSet = DelegateSet.builder()
      .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(2))
      .authorize(Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de"))
      .permissions(maxPermissions)
      .build();

    assertThat(delegateSet.permissions()).hasSize(10);
  }

  @Test
  public void testDelegateSetWithDelegatableTransactions() {
    // Test with valid delegatable transaction types
    List<PermissionWrapper> permissions = Arrays.asList(
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
    );

    DelegateSet delegateSet = DelegateSet.builder()
      .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(2))
      .authorize(Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de"))
      .permissions(permissions)
      .build();

    assertThat(delegateSet.permissions()).hasSize(4);
    assertThat(delegateSet.permissions().get(0).permission().permissionValue()).isEqualTo("Payment");
    assertThat(delegateSet.permissions().get(1).permission().permissionValue()).isEqualTo("TrustSet");
    assertThat(delegateSet.permissions().get(2).permission().permissionValue()).isEqualTo("OfferCreate");
    assertThat(delegateSet.permissions().get(3).permission().permissionValue()).isEqualTo("OfferCancel");
  }

  @Test
  public void testDelegateSetWithTransactionTypeEnum() {
    // Test using Permission.of(TransactionType) factory method
    List<PermissionWrapper> permissions = Arrays.asList(
      PermissionWrapper.builder()
        .permission(Permission.of(TransactionType.PAYMENT))
        .build(),
      PermissionWrapper.builder()
        .permission(Permission.of(TransactionType.TRUST_SET))
        .build(),
      PermissionWrapper.builder()
        .permission(Permission.of(TransactionType.OFFER_CREATE))
        .build()
    );

    DelegateSet delegateSet = DelegateSet.builder()
      .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(2))
      .authorize(Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de"))
      .permissions(permissions)
      .build();

    assertThat(delegateSet.permissions()).hasSize(3);
    assertThat(delegateSet.permissions().get(0).permission().permissionValue()).isEqualTo("Payment");
    assertThat(delegateSet.permissions().get(1).permission().permissionValue()).isEqualTo("TrustSet");
    assertThat(delegateSet.permissions().get(2).permission().permissionValue()).isEqualTo("OfferCreate");
  }

  @Test
  public void testDelegateSetWithGranularPermissionEnum() {
    // Test using Permission.of(GranularPermission) factory method
    List<PermissionWrapper> permissions = Arrays.asList(
      PermissionWrapper.builder()
        .permission(Permission.of(GranularPermission.TRUSTLINE_AUTHORIZE))
        .build(),
      PermissionWrapper.builder()
        .permission(Permission.of(GranularPermission.TRUSTLINE_FREEZE))
        .build(),
      PermissionWrapper.builder()
        .permission(Permission.of(GranularPermission.PAYMENT_MINT))
        .build()
    );

    DelegateSet delegateSet = DelegateSet.builder()
      .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(2))
      .authorize(Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de"))
      .permissions(permissions)
      .build();

    assertThat(delegateSet.permissions()).hasSize(3);
    assertThat(delegateSet.permissions().get(0).permission().permissionValue()).isEqualTo("TrustlineAuthorize");
    assertThat(delegateSet.permissions().get(1).permission().permissionValue()).isEqualTo("TrustlineFreeze");
    assertThat(delegateSet.permissions().get(2).permission().permissionValue()).isEqualTo("PaymentMint");
  }

  @Test
  public void testDelegateSetWithMixedPermissions() {
    // Test with both TransactionType and GranularPermission
    List<PermissionWrapper> permissions = Arrays.asList(
      PermissionWrapper.builder()
        .permission(Permission.of(TransactionType.PAYMENT))
        .build(),
      PermissionWrapper.builder()
        .permission(Permission.of(GranularPermission.TRUSTLINE_AUTHORIZE))
        .build(),
      PermissionWrapper.builder()
        .permission(Permission.of(TransactionType.ESCROW_CREATE))
        .build()
    );

    DelegateSet delegateSet = DelegateSet.builder()
      .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(2))
      .authorize(Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de"))
      .permissions(permissions)
      .build();

    assertThat(delegateSet.permissions()).hasSize(3);
    assertThat(delegateSet.permissions().get(0).permission().permissionValue()).isEqualTo("Payment");
    assertThat(delegateSet.permissions().get(1).permission().permissionValue()).isEqualTo("TrustlineAuthorize");
    assertThat(delegateSet.permissions().get(2).permission().permissionValue()).isEqualTo("EscrowCreate");
  }

  @Test
  public void testDelegateSetWithInvalidPermissionValue() {
    // Test with an invalid permission value that is neither TransactionType nor GranularPermission
    List<PermissionWrapper> permissions = Arrays.asList(
      PermissionWrapper.builder()
        .permission(Permission.builder().permissionValue("InvalidPermission").build())
        .build()
    );

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      DelegateSet.builder()
        .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.valueOf(2))
        .authorize(Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de"))
        .permissions(permissions)
        .build();
    });

    assertThat(exception.getMessage())
      .contains("PermissionValue 'InvalidPermission' is not a valid TransactionType or GranularPermission");
  }

  @Test
  public void testAllDelegatableTransactionTypes() {
    // Test all delegatable transaction types (non-pseudo, non-restricted)
    TransactionType[] delegatableTypes = {
      TransactionType.CHECK_CANCEL,
      TransactionType.CHECK_CASH,
      TransactionType.CHECK_CREATE,
      TransactionType.CREDENTIAL_ACCEPT,
      TransactionType.CREDENTIAL_CREATE,
      TransactionType.CREDENTIAL_DELETE,
      TransactionType.DEPOSIT_PRE_AUTH,
      TransactionType.ESCROW_CANCEL,
      TransactionType.ESCROW_CREATE,
      TransactionType.ESCROW_FINISH,
      TransactionType.NFTOKEN_MINT,
      TransactionType.NFTOKEN_BURN,
      TransactionType.NFTOKEN_ACCEPT_OFFER,
      TransactionType.NFTOKEN_CANCEL_OFFER,
      TransactionType.NFTOKEN_CREATE_OFFER,
      TransactionType.OFFER_CANCEL,
      TransactionType.OFFER_CREATE,
      TransactionType.PAYMENT,
      TransactionType.PAYMENT_CHANNEL_CLAIM,
      TransactionType.PAYMENT_CHANNEL_CREATE,
      TransactionType.PAYMENT_CHANNEL_FUND,
      TransactionType.TRUST_SET,
      TransactionType.TICKET_CREATE,
      TransactionType.CLAWBACK,
      TransactionType.AMM_BID,
      TransactionType.AMM_CREATE,
      TransactionType.AMM_DEPOSIT,
      TransactionType.AMM_VOTE,
      TransactionType.AMM_WITHDRAW,
      TransactionType.AMM_DELETE,
      TransactionType.AMM_CLAWBACK
    };

    for (TransactionType txType : delegatableTypes) {
      List<PermissionWrapper> permissions = Arrays.asList(
        PermissionWrapper.builder()
          .permission(Permission.of(txType))
          .build()
      );

      DelegateSet delegateSet = DelegateSet.builder()
        .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.valueOf(2))
        .authorize(Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de"))
        .permissions(permissions)
        .build();

      assertThat(delegateSet.permissions()).hasSize(1);
      assertThat(delegateSet.permissions().get(0).permission().permissionValue()).isEqualTo(txType.value());
    }
  }

  @Test
  public void testAllNonDelegatableTransactionTypesWithEnum() {
    // Test all non-delegatable transaction types using TransactionType enum
    TransactionType[] nonDelegatableTypes = {
      TransactionType.ACCOUNT_SET,
      TransactionType.SET_REGULAR_KEY,
      TransactionType.SIGNER_LIST_SET,
      TransactionType.DELEGATE_SET,
      TransactionType.ACCOUNT_DELETE,
      TransactionType.BATCH,
      TransactionType.ENABLE_AMENDMENT,
      TransactionType.SET_FEE,
      TransactionType.UNL_MODIFY
    };

    for (TransactionType txType : nonDelegatableTypes) {
      List<PermissionWrapper> permissions = Arrays.asList(
        PermissionWrapper.builder()
          .permission(Permission.of(txType))
          .build()
      );

      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
        DelegateSet.builder()
          .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
          .fee(XrpCurrencyAmount.ofDrops(10))
          .sequence(UnsignedInteger.valueOf(2))
          .authorize(Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de"))
          .permissions(permissions)
          .build();
      });

      assertThat(exception.getMessage())
        .contains("PermissionValue contains a non-delegatable transaction");
    }
  }

  @Test
  public void testAllGranularPermissions() {
    // Test all granular permission types
    GranularPermission[] allGranularPermissions = {
      GranularPermission.TRUSTLINE_AUTHORIZE,
      GranularPermission.TRUSTLINE_FREEZE,
      GranularPermission.TRUSTLINE_UNFREEZE,
      GranularPermission.ACCOUNT_DOMAIN_SET,
      GranularPermission.ACCOUNT_EMAIL_HASH_SET,
      GranularPermission.ACCOUNT_MESSAGE_KEY_SET,
      GranularPermission.ACCOUNT_TRANSFER_RATE_SET,
      GranularPermission.ACCOUNT_TICK_SIZE_SET,
      GranularPermission.PAYMENT_MINT,
      GranularPermission.PAYMENT_BURN,
      GranularPermission.MPTOKEN_ISSUANCE_LOCK,
      GranularPermission.MPTOKEN_ISSUANCE_UNLOCK
    };

    for (GranularPermission granularPerm : allGranularPermissions) {
      List<PermissionWrapper> permissions = Arrays.asList(
        PermissionWrapper.builder()
          .permission(Permission.of(granularPerm))
          .build()
      );

      DelegateSet delegateSet = DelegateSet.builder()
        .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.valueOf(2))
        .authorize(Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de"))
        .permissions(permissions)
        .build();

      assertThat(delegateSet.permissions()).hasSize(1);
      assertThat(delegateSet.permissions().get(0).permission().permissionValue()).isEqualTo(granularPerm.value());
    }
  }
}

