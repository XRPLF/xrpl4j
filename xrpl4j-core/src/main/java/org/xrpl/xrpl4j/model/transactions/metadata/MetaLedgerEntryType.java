package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableMetaLedgerEntryType.class)
@JsonDeserialize(as = ImmutableMetaLedgerEntryType.class)
public interface MetaLedgerEntryType {

  MetaLedgerEntryType ACCOUNT_ROOT = MetaLedgerEntryType.of("AccountRoot");
  MetaLedgerEntryType AMENDMENTS = MetaLedgerEntryType.of("Amendments");
  MetaLedgerEntryType CHECK = MetaLedgerEntryType.of("Check");
  MetaLedgerEntryType DEPOSIT_PRE_AUTH = MetaLedgerEntryType.of("DepositPreauth");
  MetaLedgerEntryType DIRECTORY_NODE = MetaLedgerEntryType.of("DirectoryNode");
  MetaLedgerEntryType ESCROW = MetaLedgerEntryType.of("Escrow");
  MetaLedgerEntryType FEE_SETTINGS = MetaLedgerEntryType.of("FeeSettings");
  MetaLedgerEntryType LEDGER_HASHES = MetaLedgerEntryType.of("LedgerHashes");
  MetaLedgerEntryType NEGATIVE_UNL = MetaLedgerEntryType.of("NegativeUNL");
  MetaLedgerEntryType NFTOKEN_OFFER = MetaLedgerEntryType.of("NFTokenOffer");
  MetaLedgerEntryType OFFER = MetaLedgerEntryType.of("Offer");
  MetaLedgerEntryType PAY_CHANNEL = MetaLedgerEntryType.of("PayChannel");
  MetaLedgerEntryType RIPPLE_STATE = MetaLedgerEntryType.of("RippleState");
  MetaLedgerEntryType SIGNER_LIST = MetaLedgerEntryType.of("SignerList");
  MetaLedgerEntryType TICKET = MetaLedgerEntryType.of("Ticket");

  /**
   * Construct a new {@link MetaLedgerEntryType} from a {@link String}.
   *
   * @param value The {@link String} value.
   * @return A {@link MetaLedgerEntryType} wrapping the supplied value.
   */
  static MetaLedgerEntryType of(String value) {
    return ImmutableMetaLedgerEntryType.builder()
      .value(value)
      .build();
  }

  String value();

}
