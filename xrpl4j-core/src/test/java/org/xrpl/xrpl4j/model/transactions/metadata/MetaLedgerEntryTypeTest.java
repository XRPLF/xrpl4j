package org.xrpl.xrpl4j.model.transactions.metadata;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;

class MetaLedgerEntryTypeTest extends AbstractJsonTest {

  @Test
  void testConstants() {
    assertThat(MetaLedgerEntryType.ACCOUNT_ROOT.value()).isEqualTo("AccountRoot");
    assertThat(MetaLedgerEntryType.AMENDMENTS.value()).isEqualTo("Amendments");
    assertThat(MetaLedgerEntryType.CHECK.value()).isEqualTo("Check");
    assertThat(MetaLedgerEntryType.CREDENTIAL.value()).isEqualTo("Credential");
    assertThat(MetaLedgerEntryType.DEPOSIT_PRE_AUTH.value()).isEqualTo("DepositPreauth");
    assertThat(MetaLedgerEntryType.DIRECTORY_NODE.value()).isEqualTo("DirectoryNode");
    assertThat(MetaLedgerEntryType.ESCROW.value()).isEqualTo("Escrow");
    assertThat(MetaLedgerEntryType.FEE_SETTINGS.value()).isEqualTo("FeeSettings");
    assertThat(MetaLedgerEntryType.LEDGER_HASHES.value()).isEqualTo("LedgerHashes");
    assertThat(MetaLedgerEntryType.NEGATIVE_UNL.value()).isEqualTo("NegativeUNL");
    assertThat(MetaLedgerEntryType.NFTOKEN_OFFER.value()).isEqualTo("NFTokenOffer");
    assertThat(MetaLedgerEntryType.OFFER.value()).isEqualTo("Offer");
    assertThat(MetaLedgerEntryType.PAY_CHANNEL.value()).isEqualTo("PayChannel");
    assertThat(MetaLedgerEntryType.RIPPLE_STATE.value()).isEqualTo("RippleState");
    assertThat(MetaLedgerEntryType.SIGNER_LIST.value()).isEqualTo("SignerList");
    assertThat(MetaLedgerEntryType.TICKET.value()).isEqualTo("Ticket");
    assertThat(MetaLedgerEntryType.NFTOKEN_PAGE.value()).isEqualTo("NFTokenPage");
    assertThat(MetaLedgerEntryType.AMM.value()).isEqualTo("AMM");
    assertThat(MetaLedgerEntryType.BRIDGE.value()).isEqualTo("Bridge");
    assertThat(MetaLedgerEntryType.XCHAIN_OWNED_CREATE_ACCOUNT_CLAIM_ID.value())
      .isEqualTo("XChainOwnedCreateAccountClaimID");
    assertThat(MetaLedgerEntryType.XCHAIN_OWNED_CLAIM_ID.value()).isEqualTo("XChainOwnedClaimID");
    assertThat(MetaLedgerEntryType.DID.value()).isEqualTo("DID");
    assertThat(MetaLedgerEntryType.ORACLE.value()).isEqualTo("Oracle");
    assertThat(MetaLedgerEntryType.MP_TOKEN.value()).isEqualTo("MPToken");
    assertThat(MetaLedgerEntryType.MP_TOKEN_ISSUANCE.value()).isEqualTo("MPTokenIssuance");
  }

  @Test
  void testLedgerObjectType() {
    assertThat(MetaLedgerEntryType.ACCOUNT_ROOT.ledgerObjectType()).isEqualTo(MetaAccountRootObject.class);
    assertThat(MetaLedgerEntryType.AMENDMENTS.ledgerObjectType()).isEqualTo(MetaUnknownObject.class);
    assertThat(MetaLedgerEntryType.CHECK.ledgerObjectType()).isEqualTo(MetaCheckObject.class);
    assertThat(MetaLedgerEntryType.CREDENTIAL.ledgerObjectType()).isEqualTo(MetaCredentialObject.class);
    assertThat(MetaLedgerEntryType.DEPOSIT_PRE_AUTH.ledgerObjectType()).isEqualTo(MetaDepositPreAuthObject.class);
    assertThat(MetaLedgerEntryType.DIRECTORY_NODE.ledgerObjectType()).isEqualTo(MetaUnknownObject.class);
    assertThat(MetaLedgerEntryType.ESCROW.ledgerObjectType()).isEqualTo(MetaEscrowObject.class);
    assertThat(MetaLedgerEntryType.FEE_SETTINGS.ledgerObjectType()).isEqualTo(MetaUnknownObject.class);
    assertThat(MetaLedgerEntryType.LEDGER_HASHES.ledgerObjectType()).isEqualTo(MetaUnknownObject.class);
    assertThat(MetaLedgerEntryType.NEGATIVE_UNL.ledgerObjectType()).isEqualTo(MetaUnknownObject.class);
    assertThat(MetaLedgerEntryType.NFTOKEN_OFFER.ledgerObjectType()).isEqualTo(MetaNfTokenOfferObject.class);
    assertThat(MetaLedgerEntryType.OFFER.ledgerObjectType()).isEqualTo(MetaOfferObject.class);
    assertThat(MetaLedgerEntryType.PAY_CHANNEL.ledgerObjectType()).isEqualTo(MetaPayChannelObject.class);
    assertThat(MetaLedgerEntryType.RIPPLE_STATE.ledgerObjectType()).isEqualTo(MetaRippleStateObject.class);
    assertThat(MetaLedgerEntryType.SIGNER_LIST.ledgerObjectType()).isEqualTo(MetaSignerListObject.class);
    assertThat(MetaLedgerEntryType.TICKET.ledgerObjectType()).isEqualTo(MetaTicketObject.class);
    assertThat(MetaLedgerEntryType.NFTOKEN_PAGE.ledgerObjectType()).isEqualTo(MetaNfTokenPageObject.class);
    assertThat(MetaLedgerEntryType.AMM.ledgerObjectType()).isEqualTo(MetaAmmObject.class);
    assertThat(MetaLedgerEntryType.BRIDGE.ledgerObjectType()).isEqualTo(MetaBridgeObject.class);
    assertThat(MetaLedgerEntryType.XCHAIN_OWNED_CREATE_ACCOUNT_CLAIM_ID.ledgerObjectType())
      .isEqualTo(MetaXChainOwnedCreateAccountClaimIdObject.class);
    assertThat(MetaLedgerEntryType.XCHAIN_OWNED_CLAIM_ID.ledgerObjectType()).isEqualTo(
      MetaXChainOwnedClaimIdObject.class
    );
    assertThat(MetaLedgerEntryType.DID.ledgerObjectType()).isEqualTo(MetaDidObject.class);
    assertThat(MetaLedgerEntryType.ORACLE.ledgerObjectType()).isEqualTo(MetaOracleObject.class);
    assertThat(MetaLedgerEntryType.MP_TOKEN.ledgerObjectType()).isEqualTo(MetaMpTokenObject.class);
    assertThat(MetaLedgerEntryType.MP_TOKEN_ISSUANCE.ledgerObjectType()).isEqualTo(MetaMpTokenIssuanceObject.class);
  }

  @Test
  void testJson() throws JsonProcessingException {
    String json = "{\"type\":\"AccountRoot\"}";
    MetaLedgerEntryTypeWrapper wrapper = objectMapper.readValue(json, MetaLedgerEntryTypeWrapper.class);
    assertThat(wrapper.type()).isEqualTo(MetaLedgerEntryType.ACCOUNT_ROOT);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableMetaLedgerEntryTypeWrapper.class)
  @JsonDeserialize(as = ImmutableMetaLedgerEntryTypeWrapper.class)
  interface MetaLedgerEntryTypeWrapper {

    MetaLedgerEntryType type();

  }
}