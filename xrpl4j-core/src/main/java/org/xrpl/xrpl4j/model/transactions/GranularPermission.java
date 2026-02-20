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

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.annotations.Beta;

/**
 * Enumeration of granular permission types for delegated credentials.
 * These permissions support control over specific portions of transactions,
 * rather than entire transaction types.
 *
 * <p>Granular permission values are always greater than 65536 (UINT16_MAX)
 * to avoid conflicts with transaction type permission values.</p>
 *
 * <p>This enum will be marked {@link Beta} until the featurePermissionDelegation amendment is enabled on mainnet.
 * Its API is subject to change.</p>
 */
@Beta
public enum GranularPermission {

  /**
   * Permission to authorize a trustline.
   */
  TRUSTLINE_AUTHORIZE("TrustlineAuthorize", 65537),

  /**
   * Permission to freeze a trustline.
   */
  TRUSTLINE_FREEZE("TrustlineFreeze", 65538),

  /**
   * Permission to unfreeze a trustline.
   */
  TRUSTLINE_UNFREEZE("TrustlineUnfreeze", 65539),

  /**
   * Permission to modify the domain of an account.
   */
  ACCOUNT_DOMAIN_SET("AccountDomainSet", 65540),

  /**
   * Permission to modify the EmailHash of an account.
   */
  ACCOUNT_EMAIL_HASH_SET("AccountEmailHashSet", 65541),

  /**
   * Permission to modify the MessageKey of an account.
   */
  ACCOUNT_MESSAGE_KEY_SET("AccountMessageKeySet", 65542),

  /**
   * Permission to modify the TransferRate of an account.
   */
  ACCOUNT_TRANSFER_RATE_SET("AccountTransferRateSet", 65543),

  /**
   * Permission to modify the TickSize of an account.
   */
  ACCOUNT_TICK_SIZE_SET("AccountTickSizeSet", 65544),

  /**
   * Permission to mint tokens via Payment transaction.
   */
  PAYMENT_MINT("PaymentMint", 65545),

  /**
   * Permission to burn tokens via Payment transaction.
   */
  PAYMENT_BURN("PaymentBurn", 65546),

  /**
   * Permission to lock an MPToken issuance.
   */
  MPTOKEN_ISSUANCE_LOCK("MPTokenIssuanceLock", 65547),

  /**
   * Permission to unlock an MPToken issuance.
   */
  MPTOKEN_ISSUANCE_UNLOCK("MPTokenIssuanceUnlock", 65548);

  private final String value;
  private final int numericValue;

  GranularPermission(String value, int numericValue) {
    this.value = value;
    this.numericValue = numericValue;
  }

  /**
   * Gets an instance of {@link GranularPermission} for the given string value.
   *
   * @param value The {@link String} value corresponding to a {@link GranularPermission}.
   *
   * @return The {@link GranularPermission} with the corresponding value, or null if not found.
   */
  public static GranularPermission forValue(String value) {
    for (GranularPermission permission : GranularPermission.values()) {
      if (permission.value.equals(value)) {
        return permission;
      }
    }
    return null;
  }

  /**
   * Get the underlying string value of this {@link GranularPermission}.
   *
   * @return The {@link String} value.
   */
  @JsonValue
  public String value() {
    return value;
  }

  /**
   * Get the numeric value of this {@link GranularPermission}.
   *
   * @return The numeric value as an int.
   */
  public int numericValue() {
    return numericValue;
  }
}

