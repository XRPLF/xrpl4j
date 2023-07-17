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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Market interface for XRP Ledger Objects.
 * TODO: pull common fields up.
 */
@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "LedgerEntryType",
  defaultImpl = ImmutableUnknownLedgerObject.class,
  visible = true
)
@JsonSubTypes( {
  @JsonSubTypes.Type(value = ImmutableAccountRootObject.class, name = "AccountRoot"),
  //    @JsonSubTypes.Type(value = ImmutableAmendmentsObject.class, name = "Amendments"),
  @JsonSubTypes.Type(value = ImmutableCheckObject.class, name = "Check"),
  @JsonSubTypes.Type(value = ImmutableDepositPreAuthObject.class, name = "DepositPreauth"),
  //    @JsonSubTypes.Type(value = ImmutableDirectoryNodeObject.class, name = "DirectoryNode"),
  @JsonSubTypes.Type(value = ImmutableEscrowObject.class, name = "Escrow"),
  //    @JsonSubTypes.Type(value = ImmutableFeeSettingsObject.class, name = "FeeSettings"),
  //    @JsonSubTypes.Type(value = ImmutableLedgerHashesObject.class, name = "LedgerHashes"),
  //    @JsonSubTypes.Type(value = ImmutableNegativeUnlObject.class, name = "NegativeUNL"),
  @JsonSubTypes.Type(value = ImmutableNfTokenOfferObject.class, name = "NFTokenOffer"),
  @JsonSubTypes.Type(value = ImmutableOfferObject.class, name = "Offer"),
  @JsonSubTypes.Type(value = ImmutablePayChannelObject.class, name = "PayChannel"),
  @JsonSubTypes.Type(value = ImmutableRippleStateObject.class, name = "RippleState"),
  @JsonSubTypes.Type(value = ImmutableSignerListObject.class, name = "SignerList"),
  @JsonSubTypes.Type(value = ImmutableTicketObject.class, name = "Ticket"),
  @JsonSubTypes.Type(value = ImmutableAmmObject.class, name = "AMM"),
  @JsonSubTypes.Type(value = ImmutableNfTokenPageObject.class, name = "NFTokenPage"),
})
// TODO: Uncomment subtypes as we implement
public interface LedgerObject {

  /**
   * Enum for all types of ledger objects.
   */
  enum LedgerEntryType {
    /**
     * The {@link LedgerEntryType} for {@code AccountRoot} ledger objects.
     */
    ACCOUNT_ROOT("AccountRoot"),

    /**
     * The {@link LedgerEntryType} for {@code Amendments} ledger objects.
     */
    AMENDMENTS("Amendments"),

    /**
     * The {@link LedgerEntryType} for {@code Check} ledger objects.
     */
    CHECK("Check"),

    /**
     * The {@link LedgerEntryType} for {@code DepositPreauth} ledger objects.
     */
    DEPOSIT_PRE_AUTH("DepositPreauth"),

    /**
     * The {@link LedgerEntryType} for {@code DirectoryNode} ledger objects.
     */
    DIRECTORY_NODE("DirectoryNode"),

    /**
     * The {@link LedgerEntryType} for {@code Escrow} ledger objects.
     */
    ESCROW("Escrow"),

    /**
     * The {@link LedgerEntryType} for {@code FeeSettings} ledger objects.
     */
    FEE_SETTINGS("FeeSettings"),

    /**
     * The {@link LedgerEntryType} for {@code LedgerHashes} ledger objects.
     */
    LEDGER_HASHES("LedgerHashes"),

    /**
     * The {@link LedgerEntryType} for {@code NegativeUNL} ledger objects.
     */
    NEGATIVE_UNL("NegativeUNL"),

    /**
     * The {@link LedgerEntryType} for {@code NFTokenOffer} ledger objects.
     */
    NFTOKEN_OFFER("NFTokenOffer"),

    /**
     * The {@link LedgerEntryType} for {@code Offer} ledger objects.
     */
    OFFER("Offer"),

    /**
     * The {@link LedgerEntryType} for {@code PayChannel} ledger objects.
     */
    PAY_CHANNEL("PayChannel"),

    /**
     * The {@link LedgerEntryType} for {@code RippleState} ledger objects.
     */
    RIPPLE_STATE("RippleState"),

    /**
     * The {@link LedgerEntryType} for {@code SignerList} ledger objects.
     */
    SIGNER_LIST("SignerList"),

    /**
     * The {@link LedgerEntryType} for {@code TicketObject} ledger objects.
     */
    TICKET("Ticket"),

    /**
     * The {@link LedgerEntryType} for {@code NfTokenPageObject} ledger objects.
     */
    NFTOKEN_PAGE("NFTokenPage"),

    /**
     * The {@link LedgerEntryType} for {@code AmmObject} ledger objects.
     */
    AMM("AMM");

    private final String value;

    LedgerEntryType(String value) {
      this.value = value;
    }

    /**
     * Constructs the {@link LedgerEntryType} corresponding to the given value, or throws an
     * {@link IllegalArgumentException} if no corresponding {@link LedgerEntryType} exists.
     *
     * <p>Mostly used by Jackson for deserialization.
     *
     * @param value The {@link String} value of a {@link LedgerEntryType}.
     * @return A {@link LedgerEntryType}.
     */
    @JsonCreator
    public static LedgerEntryType forValue(String value) {
      for (LedgerEntryType type : LedgerEntryType.values()) {
        if (type.value.equals(value)) {
          return type;
        }
      }

      throw new IllegalArgumentException("No matching LedgerEntryType enum value for String value " + value);
    }

    /**
     * Get the underlying value of this {@link LedgerEntryType}.
     *
     * @return The {@link String} value associated with this {@link LedgerEntryType}.
     */
    @JsonValue
    public String value() {
      return value;
    }
  }
}
