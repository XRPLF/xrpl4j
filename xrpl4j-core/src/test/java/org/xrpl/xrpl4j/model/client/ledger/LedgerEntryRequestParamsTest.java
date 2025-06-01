package org.xrpl.xrpl4j.model.client.ledger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.xrpl.xrpl4j.crypto.TestConstants.EC_ADDRESS;
import static org.xrpl.xrpl4j.crypto.TestConstants.ED_ADDRESS;
import static org.xrpl.xrpl4j.crypto.TestConstants.HASH_256;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.io.BaseEncoding;
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
import org.xrpl.xrpl4j.model.ledger.RippleStateObject;
import org.xrpl.xrpl4j.model.ledger.TicketObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CredentialType;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.OracleDocumentId;
import org.xrpl.xrpl4j.model.transactions.XChainBridge;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    String json =
      "{\n" +
      "  \"index\": \"" + HASH_256 + "\",\n" +
      "  \"binary\": false,\n" +
      "  \"ledger_index\": \"validated\"\n" +
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

    String json =
      "{\n" +
      "  \"index\": \"" + HASH_256 + "\",\n" +
      "  \"binary\": false,\n" +
      "  \"ledger_index\": \"validated\"\n" +
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

    String json =
      "{\n" +
      "  \"account_root\":  \"" + ED_ADDRESS + "\",\n" +
      "  \"binary\": false,\n" +
      "  \"ledger_index\": \"validated\"\n" +
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

    String json =
      "{\n" +
      "  \"amm\": {\n" +
      "    \"asset\": {\n" +
      "      \"currency\": \"XRP\"\n" +
      "    },\n" +
      "    \"asset2\": {\n" +
      "      \"currency\" : \"TST\",\n" +
      "      \"issuer\" : \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\"\n" +
      "    }\n" +
      "  },\n" +
      "  \"binary\": false,\n" +
      "  \"ledger_index\": \"validated\"\n" +
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

    String json =
      "{\n" +
      "  \"offer\": {\n" +
      "    \"account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"seq\": 359\n" +
      "  },\n" +
      "  \"binary\": false,\n" +
      "  \"ledger_index\": \"validated\"\n" +
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

    String json =
      "{\n" +
      "  \"ripple_state\": {\n" +
      "    \"accounts\": [\n" +
      "      \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "      \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\"\n" +
      "    ],\n" +
      "    \"currency\": \"USD\"\n" +
      "  },\n" +
      "  \"binary\": false,\n" +
      "  \"ledger_index\": \"validated\"\n" +
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

    String json =
      "{\n" +
      "  \"check\": \"" + HASH_256 + "\",\n" +
      "  \"binary\": false,\n" +
      "  \"ledger_index\": \"validated\"\n" +
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

    String json =
      "{\n" +
      "  \"escrow\": {\n" +
      "    \"owner\": \"rL4fPHi2FWGwRGRQSH7gBcxkuo2b9NTjKK\",\n" +
      "    \"seq\": 126\n" +
      "  },\n" +
      "  \"binary\": false,\n" +
      "  \"ledger_index\": \"validated\"\n" +
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

    String json =
      "{\n" +
      "  \"payment_channel\": \"" + HASH_256 + "\",\n" +
      "  \"binary\": false,\n" +
      "  \"ledger_index\": \"validated\"\n" +
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

    String json =
      "{\n" +
      "  \"deposit_preauth\": {\n" +
      "    \"owner\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"authorized\": \"ra5nK24KXen9AHvsdFTKHSANinZseWnPcX\"\n" +
      "  },\n" +
      "  \"binary\": false,\n" +
      "  \"ledger_index\": \"validated\"\n" +
      "}";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  void testDepositPreAuthParamsWithAuthorizedCredentials() throws JSONException, JsonProcessingException {
    List<Credential> credentials = Collections.singletonList(
      Credential
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

    String json = "{\n" +
                  "  \"deposit_preauth\": {\n" +
                  "    \"owner\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
                  "    \"authorized_credentials\": [\n" +
                  "      {\n" +
                  "        \"issuer\": \"rK2vwKgQqXahHWUvi9VVTQsYe6gze5n1os\",\n" +
                  "        \"credential_type\": \"6D795F63726564656E7469616C\"\n" +
                  "      }\n" +
                  "    ]\n" +
                  "  },\n" +
                  "  \"binary\": false,\n" +
                  "  \"ledger_index\": \"validated\"\n" +
                  "}";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testDepositPreAuthParamsMoreThanEightAuthorizedCredentials() {
    List<Credential> moreThanEight = IntStream.range(0, 9)
      .mapToObj(i ->
        Credential.builder()
          .issuer(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
          .credentialType(CredentialType.of("7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF" + i))
          .build()
      ).collect(Collectors.toList());

    assertThrows(
      IllegalArgumentException.class,
      () -> DepositPreAuthLedgerEntryParams.builder()
        .owner(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
        .authorizedCredentials(moreThanEight)
        .build(),
      "authorizedCredentials shouldn't be empty and must have less than or equal to 8 items."
    );

  }

  @Test
  public void testDepositPreAuthParamsEmptyAuthorizedCredentials() {
    assertThrows(
      IllegalArgumentException.class,
      () -> DepositPreAuthLedgerEntryParams.builder()
        .owner(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
        .authorizedCredentials(Collections.emptyList())
        .build(),
      "authorizedCredentials shouldn't be empty and must have less than or equal to 8 items."
    );
  }

  @Test
  public void testDepositPreAuthParamsDuplicateAuthorizedCredentials() {
    List<Credential> randomCredentials = IntStream.range(0, 8)
      .mapToObj(i ->
        Credential.builder()
          .issuer(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
          .credentialType(CredentialType.of("7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF" + i))
          .build()
      ).collect(Collectors.toList());

    randomCredentials.set(0, randomCredentials.get(1));

    assertThrows(
      IllegalArgumentException.class,
      () -> DepositPreAuthLedgerEntryParams.builder()
        .owner(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
        .authorizedCredentials(randomCredentials)
        .build(),
      "authorizedCredentials should have unique values."
    );
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

    String json =
      "{\n" +
      "  \"ticket\": {\n" +
      "    \"account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"ticket_seq\": 389\n" +
      "  },\n" +
      "  \"binary\": false,\n" +
      "  \"ledger_index\": \"validated\"\n" +
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

    String json =
      "{\n" +
      "  \"nft_page\": \"" + HASH_256 + "\",\n" +
      "  \"binary\": false,\n" +
      "  \"ledger_index\": \"validated\"\n" +
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

    String json =
      "{\n" +
      "  \"did\": \"" + ED_ADDRESS + "\",\n" +
      "  \"binary\": false,\n" +
      "  \"ledger_index\": \"validated\"\n" +
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

    String json =
      "{\n" +
      "  \"bridge_account\": \"" + ED_ADDRESS + "\",\n" +
      "  \"bridge\": " + objectMapper.writeValueAsString(bridge) + ",\n" +
      "  \"binary\": false,\n" +
      "  \"ledger_index\": \"validated\"\n" +
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

    String json =
      "{\n" +
      "  \"oracle\" : " + objectMapper.writeValueAsString(oracleParams) + ",\n" +
      "  \"binary\": false,\n" +
      "  \"ledger_index\": \"validated\"\n" +
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

    String json = "{\n" +
                  "  \"mpt_issuance\" : " + issuanceId + ",\n" +
                  "  \"binary\": false,\n" +
                  "  \"ledger_index\": \"validated\"\n" +
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

    String json =
      "{\n" +
      "  \"mptoken\" : " + objectMapper.writeValueAsString(mpTokenParams) + ",\n" +
      "  \"binary\": false,\n" +
      "  \"ledger_index\": \"validated\"\n" +
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

    String json =
      "{\n" +
      " \"credential\" : " + objectMapper.writeValueAsString(credentialParams) + ",\n" +
      " \"binary\": false,\n" +
      " \"ledger_index\": \"validated\"\n" +
      "}";

    assertCanSerializeAndDeserialize(params, json);
  }
}
