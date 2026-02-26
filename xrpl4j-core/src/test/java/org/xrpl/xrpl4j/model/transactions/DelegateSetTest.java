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
    List<AccountPermissionWrapper> permissions = Arrays.asList(
      AccountPermissionWrapper.builder()
        .permission(AccountPermission.builder().permissionValue("Payment").build())
        .build(),
      AccountPermissionWrapper.builder()
        .permission(AccountPermission.builder().permissionValue("TrustSet").build())
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
    List<AccountPermissionWrapper> permissions = Arrays.asList(
      AccountPermissionWrapper.builder()
        .permission(AccountPermission.builder().permissionValue("Payment").build())
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

    List<AccountPermissionWrapper> tooManyPermissions = Arrays.stream(validTypes)
      .map(txType -> AccountPermissionWrapper.builder()
        .permission(AccountPermission.of(txType))
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
    List<AccountPermissionWrapper> duplicatePermissions = Arrays.asList(
      AccountPermissionWrapper.builder()
        .permission(AccountPermission.builder().permissionValue("Payment").build())
        .build(),
      AccountPermissionWrapper.builder()
        .permission(AccountPermission.builder().permissionValue("Payment").build())
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
    // Test each non-delegatable transaction type by reusing DelegateSet.NON_DELEGABLE_TRANSACTIONS
    for (String txType : DelegateSet.NON_DELEGABLE_TRANSACTIONS) {
      List<AccountPermissionWrapper> permissions = Arrays.asList(
        AccountPermissionWrapper.builder()
          .permission(AccountPermission.builder().permissionValue(txType).build())
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

    List<AccountPermissionWrapper> maxPermissions = Arrays.stream(validTypes)
      .map(txType -> AccountPermissionWrapper.builder()
        .permission(AccountPermission.of(txType))
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
    List<AccountPermissionWrapper> permissions = Arrays.asList(
      AccountPermissionWrapper.builder()
        .permission(AccountPermission.builder().permissionValue("Payment").build())
        .build(),
      AccountPermissionWrapper.builder()
        .permission(AccountPermission.builder().permissionValue("TrustSet").build())
        .build(),
      AccountPermissionWrapper.builder()
        .permission(AccountPermission.builder().permissionValue("OfferCreate").build())
        .build(),
      AccountPermissionWrapper.builder()
        .permission(AccountPermission.builder().permissionValue("OfferCancel").build())
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
    // Test using AccountPermission.of(TransactionType) factory method
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
    // Test using AccountPermission.of(GranularPermission) factory method
    List<AccountPermissionWrapper> permissions = Arrays.asList(
      AccountPermissionWrapper.builder()
        .permission(AccountPermission.of(GranularPermission.TRUSTLINE_AUTHORIZE))
        .build(),
      AccountPermissionWrapper.builder()
        .permission(AccountPermission.of(GranularPermission.TRUSTLINE_FREEZE))
        .build(),
      AccountPermissionWrapper.builder()
        .permission(AccountPermission.of(GranularPermission.PAYMENT_MINT))
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
    List<AccountPermissionWrapper> permissions = Arrays.asList(
      AccountPermissionWrapper.builder()
        .permission(AccountPermission.of(TransactionType.PAYMENT))
        .build(),
      AccountPermissionWrapper.builder()
        .permission(AccountPermission.of(GranularPermission.TRUSTLINE_AUTHORIZE))
        .build(),
      AccountPermissionWrapper.builder()
        .permission(AccountPermission.of(TransactionType.ESCROW_CREATE))
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
    List<AccountPermissionWrapper> permissions = Arrays.asList(
      AccountPermissionWrapper.builder()
        .permission(AccountPermission.builder().permissionValue("InvalidPermission").build())
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
    // Automatically derive delegatable transaction types by removing non-delegatable ones
    // from all TransactionType values. This ensures that when new transaction types are added
    // to the enum, they are automatically tested.
    List<TransactionType> delegatableTypes = Arrays.stream(TransactionType.values())
      .filter(txType -> !DelegateSet.NON_DELEGABLE_TRANSACTIONS.contains(txType.value()))
      .filter(txType -> txType != TransactionType.UNKNOWN) // Exclude UNKNOWN
      .collect(Collectors.toList());

    // Verify we have a reasonable number of delegatable types (sanity check)
    assertThat(delegatableTypes.size()).isGreaterThan(20);

    for (TransactionType txType : delegatableTypes) {
      List<AccountPermissionWrapper> permissions = Arrays.asList(
        AccountPermissionWrapper.builder()
          .permission(AccountPermission.of(txType))
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
    // Automatically derive non-delegatable transaction types from DelegateSet.NON_DELEGABLE_TRANSACTIONS
    // This ensures that when new non-delegatable types are added, they are automatically tested
    List<TransactionType> nonDelegatableTypes = Arrays.stream(TransactionType.values())
      .filter(txType -> DelegateSet.NON_DELEGABLE_TRANSACTIONS.contains(txType.value()))
      .collect(Collectors.toList());

    // Verify we have the expected number of non-delegatable types (sanity check)
    assertThat(nonDelegatableTypes.size()).isEqualTo(DelegateSet.NON_DELEGABLE_TRANSACTIONS.size());

    for (TransactionType txType : nonDelegatableTypes) {
      List<AccountPermissionWrapper> permissions = Arrays.asList(
        AccountPermissionWrapper.builder()
          .permission(AccountPermission.of(txType))
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
    // Test all granular permission types using GranularPermission.values()
    // This ensures that when new granular permissions are added to the enum, they are automatically tested
    GranularPermission[] allGranularPermissions = GranularPermission.values();

    // Verify we have a reasonable number of granular permissions (sanity check)
    assertThat(allGranularPermissions.length).isGreaterThan(10);

    for (GranularPermission granularPerm : allGranularPermissions) {
      List<AccountPermissionWrapper> permissions = Arrays.asList(
        AccountPermissionWrapper.builder()
          .permission(AccountPermission.of(granularPerm))
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

