package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;

@Value.Immutable
@JsonSerialize(as = ImmutableMetaLedgerEntryType.class)
@JsonDeserialize(as = ImmutableMetaLedgerEntryType.class)
public interface MetaLedgerEntryType {

  MetaLedgerEntryType ACCOUNT_ROOT = MetaLedgerEntryType.of("AccountRoot");
  MetaLedgerEntryType AMENDMENTS = MetaLedgerEntryType.of("Amendments");
  MetaLedgerEntryType CHECK = MetaLedgerEntryType.of("Check");
  MetaLedgerEntryType CREDENTIAL = MetaLedgerEntryType.of("Credential");
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
  MetaLedgerEntryType NFTOKEN_PAGE = MetaLedgerEntryType.of("NFTokenPage");
  MetaLedgerEntryType AMM = MetaLedgerEntryType.of("AMM");

  @Beta
  MetaLedgerEntryType BRIDGE = MetaLedgerEntryType.of("Bridge");

  @Beta
  MetaLedgerEntryType XCHAIN_OWNED_CREATE_ACCOUNT_CLAIM_ID = MetaLedgerEntryType.of(
    "XChainOwnedCreateAccountClaimID"
  );

  @Beta
  MetaLedgerEntryType XCHAIN_OWNED_CLAIM_ID = MetaLedgerEntryType.of("XChainOwnedClaimID");

  @Beta
  MetaLedgerEntryType DID = MetaLedgerEntryType.of("DID");

  @Beta
  MetaLedgerEntryType ORACLE = MetaLedgerEntryType.of("Oracle");

  MetaLedgerEntryType MP_TOKEN_ISSUANCE = MetaLedgerEntryType.of("MPTokenIssuance");
  MetaLedgerEntryType MP_TOKEN = MetaLedgerEntryType.of("MPToken");


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

  /**
   * Get the {@link MetaLedgerObject} concrete type associated with this {@link MetaLedgerEntryType}.
   *
   * @return A {@link Class} of {@link MetaLedgerObject}.
   */
  @Derived
  @JsonIgnore
  default Class<? extends MetaLedgerObject> ledgerObjectType() {
    switch (this.value()) {
      case "AccountRoot":
        return MetaAccountRootObject.class;
      case "Check":
        return MetaCheckObject.class;
      case "Credential":
        return MetaCredentialObject.class;
      case "DepositPreauth":
        return MetaDepositPreAuthObject.class;
      case "Escrow":
        return MetaEscrowObject.class;
      case "NFTokenOffer":
        return MetaNfTokenOfferObject.class;
      case "Offer":
        return MetaOfferObject.class;
      case "PayChannel":
        return MetaPayChannelObject.class;
      case "RippleState":
        return MetaRippleStateObject.class;
      case "SignerList":
        return MetaSignerListObject.class;
      case "Ticket":
        return MetaTicketObject.class;
      case "NFTokenPage":
        return MetaNfTokenPageObject.class;
      case "AMM":
        return MetaAmmObject.class;
      case "Bridge":
        return MetaBridgeObject.class;
      case "XChainOwnedClaimID":
        return MetaXChainOwnedClaimIdObject.class;
      case "XChainOwnedCreateAccountClaimID":
        return MetaXChainOwnedCreateAccountClaimIdObject.class;
      case "DID":
        return MetaDidObject.class;
      case "Oracle":
        return MetaOracleObject.class;
      case "MPTokenIssuance":
        return MetaMpTokenIssuanceObject.class;
      case "MPToken":
        return MetaMpTokenObject.class;
      default:
        return MetaUnknownObject.class;
    }
  }

  String value();

}
