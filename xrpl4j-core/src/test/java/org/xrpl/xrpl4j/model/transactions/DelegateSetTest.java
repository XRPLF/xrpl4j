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
    // Create 11 permissions (exceeds max of 10)
    List<PermissionWrapper> tooManyPermissions = IntStream.range(0, 11)
      .mapToObj(i -> PermissionWrapper.builder()
        .permission(Permission.builder().permissionValue("Permission" + i).build())
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
    // Test with exactly 10 permissions (should succeed)
    List<PermissionWrapper> maxPermissions = IntStream.range(0, 10)
      .mapToObj(i -> PermissionWrapper.builder()
        .permission(Permission.builder().permissionValue("Permission" + i).build())
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
}

