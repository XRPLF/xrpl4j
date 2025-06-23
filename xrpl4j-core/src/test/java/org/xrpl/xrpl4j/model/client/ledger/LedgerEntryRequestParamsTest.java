package org.xrpl.xrpl4j.model.client.ledger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.xrpl.xrpl4j.crypto.TestConstants.EC_ADDRESS;
import static org.xrpl.xrpl4j.crypto.TestConstants.ED_ADDRESS;
import static org.xrpl.xrpl4j.crypto.TestConstants.HASH_256;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.ledger.RippleStateLedgerEntryParams.RippleStateAccounts;
import org.xrpl.xrpl4j.model.ledger.AccountRootObject;
import org.xrpl.xrpl4j.model.ledger.AmmObject;
import org.xrpl.xrpl4j.model.ledger.BridgeObject;
import org.xrpl.xrpl4j.model.ledger.CheckObject;
import org.xrpl.xrpl4j.model.ledger.CredentialObject;
import org.xrpl.xrpl4j.model.ledger.DepositPreAuthObject;
import org.xrpl.xrpl4j.model.ledger.DidObject;
import org.xrpl.xrpl4j.model.ledger.EscrowObject;
import org.xrpl.xrpl4j.model.ledger.Issue;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.ledger.MpTokenIssuanceObject;
import org.xrpl.xrpl4j.model.ledger.MpTokenObject;
import org.xrpl.xrpl4j.model.ledger.NfTokenPageObject;
import org.xrpl.xrpl4j.model.ledger.OfferObject;
import org.xrpl.xrpl4j.model.ledger.OracleObject;
import org.xrpl.xrpl4j.model.ledger.PayChannelObject;
import org.xrpl.xrpl4j.model.ledger.PermissionedDomainObject;
import org.xrpl.xrpl4j.model.ledger.RippleStateObject;
import org.xrpl.xrpl4j.model.ledger.TicketObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CredentialType;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.OracleDocumentId;
import org.xrpl.xrpl4j.model.transactions.XChainBridge;

import java.util.Collections;
import java.util.List;

class LedgerEntryRequestParamsTest extends AbstractJsonTest {

  @Test
  void testTypedIndexParams() throws JSONException, JsonProcessingException {
    LedgerEntryRequestParams<AmmObject> params = LedgerEntryRequestParams.index(HASH_256, AmmObject.class,
      LedgerSpecifier.VALIDATED);
    assertThat(params.index()).isNotEmpty().get().isEqualTo(HASH_256);
    assertThat(params.ledgerObjectClass()).isEqualTo(AmmObject.class);

    assertThat(params.accountRoot()).isEmpty();
    assertThat(params.amm()).isEmpty();
    assertThat(params.offer()).isEmpty();
    assertThat(params.rippleState()).isEmpty();
    assertThat(params.check()).isEmpty();
    assertThat(params.escrow()).isEmpty();
    assertThat(params.paymentChannel()).isEmpty();
    assertThat(params.depositPreAuth()).isEmpty();
    assertThat(params.ticket()).isEmpty();
    assertThat(params.nftPage()).isEmpty();
    assertThat(params.bridgeAccount()).isEmpty();
    assertThat(params.bridge()).isEmpty();
    assertThat(params.did()).isEmpty();
    assertThat(params.oracle()).isEmpty();
    assertThat(params.mptIssuance()).isEmpty();
    assertThat(params.mpToken()).isEmpty();

    String json = "{" +
      "  \"index\": \"" + HASH_256 + "\"," +
      "  \"binary\": false," +
      "  \"ledger_index\": \"validated\"" +
      "}";

    String serialized = objectMapper.writeValueAsString(params);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);

    // Note that when deserializing from JSON, we cannot figure out what ledgerObjectClass should be based on the JSON.
    // This is likely fine because request params should never really be getting deserialized by this library.
    XrplRequestParams deserialized = objectMapper.readValue(serialized, params.getClass());
    assertThat(deserialized).usingRecursiveComparison().ignoringFields("ledgerObjectClass")
      .isEqualTo(params);
  }

  @Test
  void testUntypedIndexParams() throws JSONException, JsonProcessingException {
    LedgerEntryRequestParams<LedgerObject> params = LedgerEntryRequestParams.index(HASH_256, LedgerSpecifier.VALIDATED);
    assertThat(params.index()).isNotEmpty().get().isEqualTo(HASH_256);
    assertThat(params.ledgerObjectClass()).isEqualTo(LedgerObject.class);

    assertThat(params.accountRoot()).isEmpty();
    assertThat(params.amm()).isEmpty();
    assertThat(params.offer()).isEmpty();
    assertThat(params.rippleState()).isEmpty();
    assertThat(params.check()).isEmpty();
    assertThat(params.escrow()).isEmpty();
    assertThat(params.paymentChannel()).isEmpty();
    assertThat(params.depositPreAuth()).isEmpty();
    assertThat(params.ticket()).isEmpty();
    assertThat(params.nftPage()).isEmpty();
    assertThat(params.did()).isEmpty();
    assertThat(params.bridgeAccount()).isEmpty();
    assertThat(params.bridge()).isEmpty();
    assertThat(params.oracle()).isEmpty();
    assertThat(params.mptIssuance()).isEmpty();
    assertThat(params.mpToken()).isEmpty();

    String json = "{" +
      "  \"index\": \"" + HASH_256 + "\"," +
      "  \"binary\": false," +
      "  \"ledger_index\": \"validated\"" +
      "}";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  void testAccountRootParams() throws JSONException, JsonProcessingException {
    LedgerEntryRequestParams<AccountRootObject> params = LedgerEntryRequestParams.accountRoot(
      ED_ADDRESS, LedgerSpecifier.VALIDATED
    );
    assertThat(params.accountRoot()).isNotEmpty().get().isEqualTo(ED_ADDRESS);
    assertThat(params.ledgerObjectClass()).isEqualTo(AccountRootObject.class);

    assertThat(params.index()).isEmpty();
    assertThat(params.amm()).isEmpty();
    assertThat(params.offer()).isEmpty();
    assertThat(params.rippleState()).isEmpty();
    assertThat(params.check()).isEmpty();
    assertThat(params.escrow()).isEmpty();
    assertThat(params.paymentChannel()).isEmpty();
    assertThat(params.depositPreAuth()).isEmpty();
    assertThat(params.ticket()).isEmpty();
    assertThat(params.nftPage()).isEmpty();
    assertThat(params.did()).isEmpty();
    assertThat(params.bridgeAccount()).isEmpty();
    assertThat(params.bridge()).isEmpty();
    assertThat(params.oracle()).isEmpty();
    assertThat(params.mptIssuance()).isEmpty();
    assertThat(params.mpToken()).isEmpty();

    String json = "{" +
      "  \"account_root\":  \"" + ED_ADDRESS + "\"," +
      "  \"binary\": false," +
      "  \"ledger_index\": \"validated\"" +
      "}";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  void testAmmParams() throws JSONException, JsonProcessingException {
    AmmLedgerEntryParams ammParams = AmmLedgerEntryParams.builder()
      .asset(Issue.XRP)
      .asset2(
        Issue.builder()
          .currency("TST")
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .build()
      )
      .build();

    LedgerEntryRequestParams<AmmObject> params = LedgerEntryRequestParams.amm(ammParams, LedgerSpecifier.VALIDATED);
    assertThat(params.amm()).isNotEmpty().get().isEqualTo(ammParams);
    assertThat(params.ledgerObjectClass()).isEqualTo(AmmObject.class);

    assertThat(params.index()).isEmpty();
    assertThat(params.accountRoot()).isEmpty();
    assertThat(params.offer()).isEmpty();
    assertThat(params.rippleState()).isEmpty();
    assertThat(params.check()).isEmpty();
    assertThat(params.escrow()).isEmpty();
    assertThat(params.paymentChannel()).isEmpty();
    assertThat(params.depositPreAuth()).isEmpty();
    assertThat(params.ticket()).isEmpty();
    assertThat(params.nftPage()).isEmpty();
    assertThat(params.did()).isEmpty();
    assertThat(params.bridgeAccount()).isEmpty();
    assertThat(params.bridge()).isEmpty();
    assertThat(params.oracle()).isEmpty();
    assertThat(params.mptIssuance()).isEmpty();
    assertThat(params.mpToken()).isEmpty();

    String json = "{" +
      "  \"amm\": {" +
      "    \"asset\": {" +
      "      \"currency\": \"XRP\"" +
      "    }," +
      "    \"asset2\": {" +
      "      \"currency\" : \"TST\"," +
      "      \"issuer\" : \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\"" +
      "    }" +
      "  }," +
      "  \"binary\": false," +
      "  \"ledger_index\": \"validated\"" +
      "}";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  void testOfferParams() throws JSONException, JsonProcessingException {
    OfferLedgerEntryParams offerParams = OfferLedgerEntryParams.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .seq(UnsignedInteger.valueOf(359))
      .build();

    LedgerEntryRequestParams<OfferObject> params = LedgerEntryRequestParams.offer(
      offerParams, LedgerSpecifier.VALIDATED
    );
    assertThat(params.offer()).isNotEmpty().get().isEqualTo(offerParams);
    assertThat(params.ledgerObjectClass()).isEqualTo(OfferObject.class);

    assertThat(params.index()).isEmpty();
    assertThat(params.accountRoot()).isEmpty();
    assertThat(params.amm()).isEmpty();
    assertThat(params.rippleState()).isEmpty();
    assertThat(params.check()).isEmpty();
    assertThat(params.escrow()).isEmpty();
    assertThat(params.paymentChannel()).isEmpty();
    assertThat(params.depositPreAuth()).isEmpty();
    assertThat(params.ticket()).isEmpty();
    assertThat(params.nftPage()).isEmpty();
    assertThat(params.did()).isEmpty();
    assertThat(params.bridgeAccount()).isEmpty();
    assertThat(params.bridge()).isEmpty();
    assertThat(params.oracle()).isEmpty();
    assertThat(params.mptIssuance()).isEmpty();
    assertThat(params.mpToken()).isEmpty();

    String json = "{" +
      "  \"offer\": {" +
      "    \"account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\"," +
      "    \"seq\": 359" +
      "  }," +
      "  \"binary\": false," +
      "  \"ledger_index\": \"validated\"" +
      "}";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  void testRippleStateParams() throws JSONException, JsonProcessingException {
    RippleStateLedgerEntryParams rippleStateParams = RippleStateLedgerEntryParams.builder()
      .accounts(RippleStateAccounts.of(
        Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"),
        Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW")
      ))
      .currency("USD")
      .build();

    LedgerEntryRequestParams<RippleStateObject> params = LedgerEntryRequestParams.rippleState(
      rippleStateParams, LedgerSpecifier.VALIDATED
    );
    assertThat(params.rippleState()).isNotEmpty().get().isEqualTo(rippleStateParams);
    assertThat(params.ledgerObjectClass()).isEqualTo(RippleStateObject.class);

    assertThat(params.index()).isEmpty();
    assertThat(params.accountRoot()).isEmpty();
    assertThat(params.amm()).isEmpty();
    assertThat(params.offer()).isEmpty();
    assertThat(params.check()).isEmpty();
    assertThat(params.escrow()).isEmpty();
    assertThat(params.paymentChannel()).isEmpty();
    assertThat(params.depositPreAuth()).isEmpty();
    assertThat(params.ticket()).isEmpty();
    assertThat(params.nftPage()).isEmpty();
    assertThat(params.did()).isEmpty();
    assertThat(params.bridgeAccount()).isEmpty();
    assertThat(params.bridge()).isEmpty();
    assertThat(params.oracle()).isEmpty();
    assertThat(params.mptIssuance()).isEmpty();
    assertThat(params.mpToken()).isEmpty();

    String json = "{" +
      "  \"ripple_state\": {" +
      "    \"accounts\": [" +
      "      \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\"," +
      "      \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\"" +
      "    ]," +
      "    \"currency\": \"USD\"" +
      "  }," +
      "  \"binary\": false," +
      "  \"ledger_index\": \"validated\"" +
      "}";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  void testCheckParams() throws JSONException, JsonProcessingException {
    LedgerEntryRequestParams<CheckObject> params = LedgerEntryRequestParams.check(HASH_256, LedgerSpecifier.VALIDATED);
    assertThat(params.check()).isNotEmpty().get().isEqualTo(HASH_256);
    assertThat(params.ledgerObjectClass()).isEqualTo(CheckObject.class);

    assertThat(params.index()).isEmpty();
    assertThat(params.accountRoot()).isEmpty();
    assertThat(params.amm()).isEmpty();
    assertThat(params.offer()).isEmpty();
    assertThat(params.rippleState()).isEmpty();
    assertThat(params.escrow()).isEmpty();
    assertThat(params.paymentChannel()).isEmpty();
    assertThat(params.depositPreAuth()).isEmpty();
    assertThat(params.ticket()).isEmpty();
    assertThat(params.nftPage()).isEmpty();
    assertThat(params.did()).isEmpty();
    assertThat(params.bridgeAccount()).isEmpty();
    assertThat(params.bridge()).isEmpty();
    assertThat(params.oracle()).isEmpty();
    assertThat(params.mptIssuance()).isEmpty();
    assertThat(params.mpToken()).isEmpty();

    String json = "{" +
      "  \"check\": \"" + HASH_256 + "\"," +
      "  \"binary\": false," +
      "  \"ledger_index\": \"validated\"" +
      "}";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  void testEscrowParams() throws JSONException, JsonProcessingException {
    EscrowLedgerEntryParams escrowParams = EscrowLedgerEntryParams.builder()
      .owner(Address.of("rL4fPHi2FWGwRGRQSH7gBcxkuo2b9NTjKK"))
      .seq(UnsignedInteger.valueOf(126))
      .build();
    LedgerEntryRequestParams<EscrowObject> params = LedgerEntryRequestParams.escrow(
      escrowParams, LedgerSpecifier.VALIDATED
    );
    assertThat(params.escrow()).isNotEmpty().get().isEqualTo(escrowParams);
    assertThat(params.ledgerObjectClass()).isEqualTo(EscrowObject.class);

    assertThat(params.index()).isEmpty();
    assertThat(params.accountRoot()).isEmpty();
    assertThat(params.amm()).isEmpty();
    assertThat(params.offer()).isEmpty();
    assertThat(params.rippleState()).isEmpty();
    assertThat(params.check()).isEmpty();
    assertThat(params.paymentChannel()).isEmpty();
    assertThat(params.depositPreAuth()).isEmpty();
    assertThat(params.ticket()).isEmpty();
    assertThat(params.nftPage()).isEmpty();
    assertThat(params.did()).isEmpty();
    assertThat(params.bridgeAccount()).isEmpty();
    assertThat(params.bridge()).isEmpty();
    assertThat(params.oracle()).isEmpty();
    assertThat(params.mptIssuance()).isEmpty();
    assertThat(params.mpToken()).isEmpty();

    String json = "{" +
      "  \"escrow\": {" +
      "    \"owner\": \"rL4fPHi2FWGwRGRQSH7gBcxkuo2b9NTjKK\"," +
      "    \"seq\": 126" +
      "  }," +
      "  \"binary\": false," +
      "  \"ledger_index\": \"validated\"" +
      "}";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  void testPaymentChannelParams() throws JSONException, JsonProcessingException {
    LedgerEntryRequestParams<PayChannelObject> params = LedgerEntryRequestParams.paymentChannel(
      HASH_256, LedgerSpecifier.VALIDATED
    );
    assertThat(params.paymentChannel()).isNotEmpty().get().isEqualTo(HASH_256);
    assertThat(params.ledgerObjectClass()).isEqualTo(PayChannelObject.class);

    assertThat(params.index()).isEmpty();
    assertThat(params.accountRoot()).isEmpty();
    assertThat(params.amm()).isEmpty();
    assertThat(params.offer()).isEmpty();
    assertThat(params.rippleState()).isEmpty();
    assertThat(params.check()).isEmpty();
    assertThat(params.escrow()).isEmpty();
    assertThat(params.depositPreAuth()).isEmpty();
    assertThat(params.ticket()).isEmpty();
    assertThat(params.nftPage()).isEmpty();
    assertThat(params.did()).isEmpty();
    assertThat(params.bridgeAccount()).isEmpty();
    assertThat(params.bridge()).isEmpty();
    assertThat(params.oracle()).isEmpty();
    assertThat(params.mptIssuance()).isEmpty();
    assertThat(params.mpToken()).isEmpty();

    String json = "{" +
      "  \"payment_channel\": \"" + HASH_256 + "\"," +
      "  \"binary\": false," +
      "  \"ledger_index\": \"validated\"" +
      "}";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  void testDepositPreAuthParams() throws JSONException, JsonProcessingException {
    DepositPreAuthLedgerEntryParams depositPreAuthParams = DepositPreAuthLedgerEntryParams.builder()
      .owner(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .authorized(Address.of("ra5nK24KXen9AHvsdFTKHSANinZseWnPcX"))
      .build();
    LedgerEntryRequestParams<DepositPreAuthObject> params = LedgerEntryRequestParams.depositPreAuth(
      depositPreAuthParams,
      LedgerSpecifier.VALIDATED
    );
    assertThat(params.depositPreAuth()).isNotEmpty().get().isEqualTo(depositPreAuthParams);
    assertThat(params.ledgerObjectClass()).isEqualTo(DepositPreAuthObject.class);

    assertThat(params.index()).isEmpty();
    assertThat(params.accountRoot()).isEmpty();
    assertThat(params.amm()).isEmpty();
    assertThat(params.offer()).isEmpty();
    assertThat(params.rippleState()).isEmpty();
    assertThat(params.check()).isEmpty();
    assertThat(params.escrow()).isEmpty();
    assertThat(params.paymentChannel()).isEmpty();
    assertThat(params.ticket()).isEmpty();
    assertThat(params.nftPage()).isEmpty();
    assertThat(params.did()).isEmpty();
    assertThat(params.bridgeAccount()).isEmpty();
    assertThat(params.bridge()).isEmpty();
    assertThat(params.oracle()).isEmpty();
    assertThat(params.mptIssuance()).isEmpty();
    assertThat(params.mpToken()).isEmpty();

    String json = "{" +
      "  \"deposit_preauth\": {" +
      "    \"owner\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\"," +
      "    \"authorized\": \"ra5nK24KXen9AHvsdFTKHSANinZseWnPcX\"" +
      "  }," +
      "  \"binary\": false," +
      "  \"ledger_index\": \"validated\"" +
      "}";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  void testDepositPreAuthParamsWithAuthorizedCredentials() throws JSONException, JsonProcessingException {
    List<DepositPreAuthCredential> credentials = Collections.singletonList(
      DepositPreAuthCredential
        .builder()
        .credentialType(CredentialType.of("6D795F63726564656E7469616C"))
        .issuer(Address.of("rK2vwKgQqXahHWUvi9VVTQsYe6gze5n1os"))
        .build()
    );
    DepositPreAuthLedgerEntryParams depositPreAuthParams = DepositPreAuthLedgerEntryParams.builder()
      .owner(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .authorizedCredentials(credentials)
      .build();
    LedgerEntryRequestParams<DepositPreAuthObject> params = LedgerEntryRequestParams.depositPreAuth(
      depositPreAuthParams,
      LedgerSpecifier.VALIDATED
    );
    assertThat(params.depositPreAuth()).isNotEmpty().get().isEqualTo(depositPreAuthParams);
    assertThat(params.ledgerObjectClass()).isEqualTo(DepositPreAuthObject.class);

    assertThat(params.index()).isEmpty();
    assertThat(params.accountRoot()).isEmpty();
    assertThat(params.amm()).isEmpty();
    assertThat(params.offer()).isEmpty();
    assertThat(params.rippleState()).isEmpty();
    assertThat(params.check()).isEmpty();
    assertThat(params.escrow()).isEmpty();
    assertThat(params.paymentChannel()).isEmpty();
    assertThat(params.ticket()).isEmpty();
    assertThat(params.nftPage()).isEmpty();
    assertThat(params.did()).isEmpty();
    assertThat(params.bridgeAccount()).isEmpty();
    assertThat(params.bridge()).isEmpty();
    assertThat(params.oracle()).isEmpty();
    assertThat(params.mptIssuance()).isEmpty();
    assertThat(params.mpToken()).isEmpty();

    String json = "{" +
      "  \"deposit_preauth\": {" +
      "    \"owner\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\"," +
      "    \"authorized_credentials\": [" +
      "      {" +
      "        \"issuer\": \"rK2vwKgQqXahHWUvi9VVTQsYe6gze5n1os\"," +
      "        \"credential_type\": \"6D795F63726564656E7469616C\"" +
      "      }" +
      "    ]" +
      "  }," +
      "  \"binary\": false," +
      "  \"ledger_index\": \"validated\"" +
      "}";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  void testTicketParams() throws JSONException, JsonProcessingException {
    TicketLedgerEntryParams ticketParams = TicketLedgerEntryParams.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .ticketSeq(UnsignedInteger.valueOf(389))
      .build();
    LedgerEntryRequestParams<TicketObject> params = LedgerEntryRequestParams.ticket(
      ticketParams,
      LedgerSpecifier.VALIDATED
    );
    assertThat(params.ticket()).isNotEmpty().get().isEqualTo(ticketParams);
    assertThat(params.ledgerObjectClass()).isEqualTo(TicketObject.class);

    assertThat(params.index()).isEmpty();
    assertThat(params.accountRoot()).isEmpty();
    assertThat(params.amm()).isEmpty();
    assertThat(params.offer()).isEmpty();
    assertThat(params.rippleState()).isEmpty();
    assertThat(params.check()).isEmpty();
    assertThat(params.escrow()).isEmpty();
    assertThat(params.paymentChannel()).isEmpty();
    assertThat(params.depositPreAuth()).isEmpty();
    assertThat(params.nftPage()).isEmpty();
    assertThat(params.did()).isEmpty();
    assertThat(params.bridgeAccount()).isEmpty();
    assertThat(params.bridge()).isEmpty();
    assertThat(params.oracle()).isEmpty();
    assertThat(params.mptIssuance()).isEmpty();
    assertThat(params.mpToken()).isEmpty();

    String json = "{" +
      "  \"ticket\": {" +
      "    \"account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\"," +
      "    \"ticket_seq\": 389" +
      "  }," +
      "  \"binary\": false," +
      "  \"ledger_index\": \"validated\"" +
      "}";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  void testNftPageParams() throws JSONException, JsonProcessingException {
    LedgerEntryRequestParams<NfTokenPageObject> params = LedgerEntryRequestParams.nftPage(
      HASH_256,
      LedgerSpecifier.VALIDATED
    );
    assertThat(params.nftPage()).isNotEmpty().get().isEqualTo(HASH_256);
    assertThat(params.ledgerObjectClass()).isEqualTo(NfTokenPageObject.class);

    assertThat(params.index()).isEmpty();
    assertThat(params.accountRoot()).isEmpty();
    assertThat(params.amm()).isEmpty();
    assertThat(params.offer()).isEmpty();
    assertThat(params.rippleState()).isEmpty();
    assertThat(params.check()).isEmpty();
    assertThat(params.escrow()).isEmpty();
    assertThat(params.paymentChannel()).isEmpty();
    assertThat(params.depositPreAuth()).isEmpty();
    assertThat(params.ticket()).isEmpty();
    assertThat(params.did()).isEmpty();
    assertThat(params.bridgeAccount()).isEmpty();
    assertThat(params.bridge()).isEmpty();
    assertThat(params.oracle()).isEmpty();
    assertThat(params.mptIssuance()).isEmpty();
    assertThat(params.mpToken()).isEmpty();

    String json = "{" +
      "  \"nft_page\": \"" + HASH_256 + "\"," +
      "  \"binary\": false," +
      "  \"ledger_index\": \"validated\"" +
      "}";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  void testDidParams() throws JSONException, JsonProcessingException {
    LedgerEntryRequestParams<DidObject> params = LedgerEntryRequestParams.did(
      ED_ADDRESS,
      LedgerSpecifier.VALIDATED
    );
    assertThat(params.did()).isNotEmpty().get().isEqualTo(ED_ADDRESS);
    assertThat(params.ledgerObjectClass()).isEqualTo(DidObject.class);

    assertThat(params.index()).isEmpty();
    assertThat(params.accountRoot()).isEmpty();
    assertThat(params.amm()).isEmpty();
    assertThat(params.offer()).isEmpty();
    assertThat(params.rippleState()).isEmpty();
    assertThat(params.check()).isEmpty();
    assertThat(params.escrow()).isEmpty();
    assertThat(params.paymentChannel()).isEmpty();
    assertThat(params.depositPreAuth()).isEmpty();
    assertThat(params.ticket()).isEmpty();
    assertThat(params.nftPage()).isEmpty();
    assertThat(params.bridgeAccount()).isEmpty();
    assertThat(params.bridge()).isEmpty();
    assertThat(params.oracle()).isEmpty();
    assertThat(params.mptIssuance()).isEmpty();
    assertThat(params.mpToken()).isEmpty();

    String json = "{" +
      "  \"did\": \"" + ED_ADDRESS + "\"," +
      "  \"binary\": false," +
      "  \"ledger_index\": \"validated\"" +
      "}";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  void testBridgeParams() throws JSONException, JsonProcessingException {
    XChainBridge bridge = XChainBridge.builder()
      .lockingChainDoor(ED_ADDRESS)
      .lockingChainIssue(Issue.XRP)
      .issuingChainDoor(ED_ADDRESS)
      .issuingChainIssue(Issue.XRP)
      .build();
    LedgerEntryRequestParams<BridgeObject> params = LedgerEntryRequestParams.bridge(
      ED_ADDRESS,
      bridge,
      LedgerSpecifier.VALIDATED
    );
    assertThat(params.bridgeAccount()).isNotEmpty().get().isEqualTo(ED_ADDRESS);
    assertThat(params.bridge()).isNotEmpty().get().isEqualTo(bridge);
    assertThat(params.ledgerObjectClass()).isEqualTo(BridgeObject.class);

    assertThat(params.index()).isEmpty();
    assertThat(params.accountRoot()).isEmpty();
    assertThat(params.amm()).isEmpty();
    assertThat(params.offer()).isEmpty();
    assertThat(params.rippleState()).isEmpty();
    assertThat(params.check()).isEmpty();
    assertThat(params.escrow()).isEmpty();
    assertThat(params.paymentChannel()).isEmpty();
    assertThat(params.depositPreAuth()).isEmpty();
    assertThat(params.ticket()).isEmpty();
    assertThat(params.nftPage()).isEmpty();
    assertThat(params.did()).isEmpty();
    assertThat(params.oracle()).isEmpty();
    assertThat(params.mptIssuance()).isEmpty();
    assertThat(params.mpToken()).isEmpty();

    String json = "{" +
      "  \"bridge_account\": \"" + ED_ADDRESS + "\"," +
      "  \"bridge\": " + objectMapper.writeValueAsString(bridge) + "," +
      "  \"binary\": false," +
      "  \"ledger_index\": \"validated\"" +
      "}";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  void testOracleParams() throws JSONException, JsonProcessingException {
    OracleLedgerEntryParams oracleParams = OracleLedgerEntryParams.builder()
      .account(ED_ADDRESS)
      .oracleDocumentId(OracleDocumentId.of(UnsignedInteger.ONE))
      .build();
    LedgerEntryRequestParams<OracleObject> params = LedgerEntryRequestParams.oracle(
      oracleParams,
      LedgerSpecifier.VALIDATED
    );
    assertThat(params.oracle()).isNotEmpty().get().isEqualTo(oracleParams);
    assertThat(params.ledgerObjectClass()).isEqualTo(OracleObject.class);

    assertThat(params.index()).isEmpty();
    assertThat(params.accountRoot()).isEmpty();
    assertThat(params.amm()).isEmpty();
    assertThat(params.offer()).isEmpty();
    assertThat(params.rippleState()).isEmpty();
    assertThat(params.check()).isEmpty();
    assertThat(params.escrow()).isEmpty();
    assertThat(params.paymentChannel()).isEmpty();
    assertThat(params.depositPreAuth()).isEmpty();
    assertThat(params.ticket()).isEmpty();
    assertThat(params.nftPage()).isEmpty();
    assertThat(params.did()).isEmpty();
    assertThat(params.bridgeAccount()).isEmpty();
    assertThat(params.mptIssuance()).isEmpty();
    assertThat(params.mpToken()).isEmpty();

    String json = "{" +
      "  \"oracle\" : " + objectMapper.writeValueAsString(oracleParams) + "," +
      "  \"binary\": false," +
      "  \"ledger_index\": \"validated\"" +
      "}";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  void testMptIssuanceParams() throws JSONException, JsonProcessingException {
    MpTokenIssuanceId issuanceId = MpTokenIssuanceId.of("ABCD");
    LedgerEntryRequestParams<MpTokenIssuanceObject> params = LedgerEntryRequestParams.mpTokenIssuance(
      issuanceId,
      LedgerSpecifier.VALIDATED
    );
    assertThat(params.oracle()).isEmpty();
    assertThat(params.ledgerObjectClass()).isEqualTo(MpTokenIssuanceObject.class);

    assertThat(params.index()).isEmpty();
    assertThat(params.accountRoot()).isEmpty();
    assertThat(params.amm()).isEmpty();
    assertThat(params.offer()).isEmpty();
    assertThat(params.rippleState()).isEmpty();
    assertThat(params.check()).isEmpty();
    assertThat(params.escrow()).isEmpty();
    assertThat(params.paymentChannel()).isEmpty();
    assertThat(params.depositPreAuth()).isEmpty();
    assertThat(params.ticket()).isEmpty();
    assertThat(params.nftPage()).isEmpty();
    assertThat(params.did()).isEmpty();
    assertThat(params.bridgeAccount()).isEmpty();
    assertThat(params.mptIssuance()).isNotEmpty().get().isEqualTo(issuanceId);
    assertThat(params.mpToken()).isEmpty();

    String json = "{" +
      "  \"mpt_issuance\" : " + issuanceId + "," +
      "  \"binary\": false," +
      "  \"ledger_index\": \"validated\"" +
      "}";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  void testMpTokenParams() throws JSONException, JsonProcessingException {
    MpTokenLedgerEntryParams mpTokenParams = MpTokenLedgerEntryParams.builder()
      .mpTokenIssuanceId(MpTokenIssuanceId.of("ABCD"))
      .account(ED_ADDRESS)
      .build();
    LedgerEntryRequestParams<MpTokenObject> params = LedgerEntryRequestParams.mpToken(
      mpTokenParams,
      LedgerSpecifier.VALIDATED
    );
    assertThat(params.oracle()).isEmpty();
    assertThat(params.ledgerObjectClass()).isEqualTo(MpTokenObject.class);

    assertThat(params.index()).isEmpty();
    assertThat(params.accountRoot()).isEmpty();
    assertThat(params.amm()).isEmpty();
    assertThat(params.offer()).isEmpty();
    assertThat(params.rippleState()).isEmpty();
    assertThat(params.check()).isEmpty();
    assertThat(params.escrow()).isEmpty();
    assertThat(params.paymentChannel()).isEmpty();
    assertThat(params.depositPreAuth()).isEmpty();
    assertThat(params.ticket()).isEmpty();
    assertThat(params.nftPage()).isEmpty();
    assertThat(params.did()).isEmpty();
    assertThat(params.bridgeAccount()).isEmpty();
    assertThat(params.mptIssuance()).isEmpty();
    assertThat(params.mpToken()).isNotEmpty().get().isEqualTo(mpTokenParams);

    String json = "{" +
      "  \"mptoken\" : " + objectMapper.writeValueAsString(mpTokenParams) + "," +
      "  \"binary\": false," +
      "  \"ledger_index\": \"validated\"" +
      "}";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  void testCredentialParams() throws JSONException, JsonProcessingException {
    CredentialLedgerEntryParams credentialParams = CredentialLedgerEntryParams.builder()
      .credentialType(CredentialType.ofPlainText("ABC"))
      .issuer(ED_ADDRESS)
      .subject(EC_ADDRESS)
      .build();
    LedgerEntryRequestParams<CredentialObject> params = LedgerEntryRequestParams.credential(
      credentialParams,
      LedgerSpecifier.VALIDATED
    );
    assertThat(params.ledgerObjectClass()).isEqualTo(CredentialObject.class);

    assertThat(params.index()).isEmpty();
    assertThat(params.accountRoot()).isEmpty();
    assertThat(params.amm()).isEmpty();
    assertThat(params.offer()).isEmpty();
    assertThat(params.rippleState()).isEmpty();
    assertThat(params.check()).isEmpty();
    assertThat(params.escrow()).isEmpty();
    assertThat(params.paymentChannel()).isEmpty();
    assertThat(params.depositPreAuth()).isEmpty();
    assertThat(params.ticket()).isEmpty();
    assertThat(params.nftPage()).isEmpty();
    assertThat(params.did()).isEmpty();
    assertThat(params.bridgeAccount()).isEmpty();
    assertThat(params.mptIssuance()).isEmpty();
    assertThat(params.credential()).isNotEmpty().get().isEqualTo(credentialParams);

    String json = "{" +
      " \"credential\" : " + objectMapper.writeValueAsString(credentialParams) + "," +
      " \"binary\": false," +
      " \"ledger_index\": \"validated\"" +
      "}";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  void testPermissionedDomainParams() throws JSONException, JsonProcessingException {
    PermissionedDomainLedgerEntryParams permissionedDomainLedgerEntryParams =
      PermissionedDomainLedgerEntryParams.builder()
        .account(ED_ADDRESS)
        .seq(UnsignedInteger.valueOf(10))
        .build();
    LedgerEntryRequestParams<PermissionedDomainObject> params = LedgerEntryRequestParams.permissionedDomain(
      permissionedDomainLedgerEntryParams,
      LedgerSpecifier.VALIDATED
    );
    assertThat(params.ledgerObjectClass()).isEqualTo(PermissionedDomainObject.class);

    assertThat(params.index()).isEmpty();
    assertThat(params.accountRoot()).isEmpty();
    assertThat(params.amm()).isEmpty();
    assertThat(params.offer()).isEmpty();
    assertThat(params.rippleState()).isEmpty();
    assertThat(params.check()).isEmpty();
    assertThat(params.escrow()).isEmpty();
    assertThat(params.paymentChannel()).isEmpty();
    assertThat(params.depositPreAuth()).isEmpty();
    assertThat(params.ticket()).isEmpty();
    assertThat(params.nftPage()).isEmpty();
    assertThat(params.did()).isEmpty();
    assertThat(params.bridgeAccount()).isEmpty();
    assertThat(params.mptIssuance()).isEmpty();
    assertThat(params.credential()).isEmpty();
    assertThat(params.permissionedDomain()).isNotEmpty().get().isEqualTo(permissionedDomainLedgerEntryParams);

    String json = "{" +
      "  \"permissioned_domain\" : " + objectMapper.writeValueAsString(permissionedDomainLedgerEntryParams) + "," +
      "  \"binary\": false," +
      "  \"ledger_index\": \"validated\"" +
      "}";

    assertCanSerializeAndDeserialize(params, json);
  }
}