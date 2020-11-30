package com.ripple.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Market interface for XRP Ledger Objects.
 *
 * TODO: pull common fields up.
 */
@JsonDeserialize(using = LedgerObjectDeserializer.class)
public interface LedgerObject {

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
