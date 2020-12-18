package org.xrpl.xrpl4j.model.ledger;

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
    property = "LedgerEntryType"
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
    @JsonSubTypes.Type(value = ImmutableOfferObject.class, name = "Offer"),
    @JsonSubTypes.Type(value = ImmutablePayChannelObject.class, name = "PayChannel"),
    @JsonSubTypes.Type(value = ImmutableRippleStateObject.class, name = "RippleState"),
    @JsonSubTypes.Type(value = ImmutableSignerListObject.class, name = "SignerList"),
})
// TODO: Uncomment subtypes as we implement
public interface LedgerObject {

  /**
   * Enum for all types of ledger objects.
   */
  enum LedgerEntryType {
    ACCOUNT_ROOT("AccountRoot"),
    AMENDMENTS("Amendments"),
    CHECK("Check"),
    DEPOSIT_PRE_AUTH("DepositPreauth"),
    DIRECTORY_NODE("DirectoryNode"),
    ESCROW("Escrow"),
    FEE_SETTINGS("FeeSettings"),
    LEDGER_HASHES("LedgerHashes"),
    NEGATIVE_UNL("NegativeUNL"),
    OFFER("Offer"),
    PAY_CHANNEL("PayChannel"),
    RIPPLE_STATE("RippleState"),
    SIGNER_LIST("SignerList");

    private final String value;

    LedgerEntryType(String value) {
      this.value = value;
    }

    @JsonCreator
    public static LedgerEntryType forValue(String value) {
      for (LedgerEntryType type : LedgerEntryType.values()) {
        if (type.value.equals(value)) {
          return type;
        }
      }

      throw new IllegalArgumentException("No matching LedgerEntryType enum value for String value " + value);
    }

    @JsonValue
    public String value() {
      return value;
    }
  }
}
