package org.xrpl.xrpl4j.model.ledger;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.specifiers.LedgerIndex;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.EscrowCreate;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class LedgerHeaderJsonTests extends AbstractJsonTest {

  @Test
  public void deserializeLedgerHeaderWithTransactions() throws JsonProcessingException, JSONException {
    LedgerHeader ledgerHeader = LedgerHeader.builder()
      .accountHash(Hash256.of("B258A8BB4743FB74CBBD6E9F67E4A56C4432EA09E5805E4CC2DA26F2DBE8F3D1"))
      .closeTime(UnsignedLong.valueOf(638329271))
      .closeTimeHuman(ZonedDateTime.parse("2020-Mar-24 01:41:11.000000000 UTC",
        DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss.SSSSSSSSS z")).withZoneSameLocal(ZoneId.of("UTC")))
      .closeTimeResolution(UnsignedInteger.valueOf(10))
      .closed(true)
      .ledgerHash(Hash256.of("3652D7FD0576BC452C0D2E9B747BDD733075971D1A9A1D98125055DEF428721A"))
      .ledgerIndex(LedgerIndex.of(UnsignedLong.valueOf(54300940)))
      .parentCloseTime(UnsignedLong.valueOf(638329270))
      .parentHash(Hash256.of("AE996778246BC81F85D5AF051241DAA577C23BCA04C034A7074F93700194520D"))
      .totalCoins(XrpCurrencyAmount.ofDrops(99991024049618156L))
      .transactionHash(Hash256.of("FC6FFCB71B2527DDD630EE5409D38913B4D4C026AA6C3B14A3E9D4ED45CFE30D"))
      .addTransactions(
        TransactionResult.builder()
          .transaction(
            Payment.builder()
              .account(Address.of("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe"))
              .amount(XrpCurrencyAmount.ofDrops(1000000000))
              .destination(Address.of("rBkoiq4sVF5N6zu4QwZPm9iVQht4BtxtM1"))
              .fee(XrpCurrencyAmount.ofDrops(12))
              .flags(Flags.PaymentFlags.of(2147483648L))
              .lastLedgerSequence(UnsignedInteger.valueOf(13010048))
              .sequence(UnsignedInteger.valueOf(2062124))
              .signingPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
              .transactionSignature("3045022100E1F74E892839A9818D991F1E7B3D069ED499A5D412DD6C8C2634E87" +
                "D0A37D3750220141AF3DCE6DA4D134614E49C99FFB1E498C238B46FC47CF3F79A989C4A2053AC")
              .hash(Hash256.of("E22068A818EA853DD3B7B574FF58C3A84D1F664495FF6ECD11D3B03B1D2FC2F7"))
              .build()
          )
          .build(),
        TransactionResult.builder()
          .transaction(
            EscrowCreate.builder()
              .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
              .amount(XrpCurrencyAmount.ofDrops(10000))
              .destination(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
              .fee(XrpCurrencyAmount.ofDrops(12))
              .cancelAfter(UnsignedLong.valueOf(533257958))
              .finishAfter(UnsignedLong.valueOf(533171558))
              .sequence(UnsignedInteger.ONE)
              .destinationTag(UnsignedInteger.valueOf(23480))
              .sourceTag(UnsignedInteger.valueOf(11747))
              .hash(Hash256.of("E939C30F233E3E6B0A9F829BDDA258CB9DA38D11C0F66C7D60E38B9D9FA987B8"))
              .build()
          )
          .build()
      )
      .build();

    String json = "{\n" +
      "  \"ledger_index\" : 54300940,\n" +
      "  \"ledger_hash\" : \"3652D7FD0576BC452C0D2E9B747BDD733075971D1A9A1D98125055DEF428721A\",\n" +
      "  \"account_hash\" : \"B258A8BB4743FB74CBBD6E9F67E4A56C4432EA09E5805E4CC2DA26F2DBE8F3D1\",\n" +
      "  \"close_time\" : 638329271,\n" +
      "  \"close_time_human\" : \"2020-Mar-24 01:41:11.000000000 UTC\",\n" +
      "  \"closed\" : true,\n" +
      "  \"parent_hash\" : \"AE996778246BC81F85D5AF051241DAA577C23BCA04C034A7074F93700194520D\",\n" +
      "  \"parent_close_time\" : 638329270,\n" +
      "  \"total_coins\" : \"99991024049618156\",\n" +
      "  \"transaction_hash\" : \"FC6FFCB71B2527DDD630EE5409D38913B4D4C026AA6C3B14A3E9D4ED45CFE30D\",\n" +
      "  \"transactions\" : [ {\n" +
      "    \"Account\" : \"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe\",\n" +
      "    \"Fee\" : \"12\",\n" +
      "    \"Sequence\" : 2062124,\n" +
      "    \"LastLedgerSequence\" : 13010048,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"TxnSignature\" : \"3045022100E1F74E892839A9818D991F1E7B3D069ED499A5D412DD6C8C2634E87D0A37D375022014" +
      "1AF3DCE6DA4D134614E49C99FFB1E498C238B46FC47CF3F79A989C4A2053AC\",\n" +
      "    \"Flags\" : 2147483648,\n" +
      "    \"Amount\" : \"1000000000\",\n" +
      "    \"Destination\" : \"rBkoiq4sVF5N6zu4QwZPm9iVQht4BtxtM1\",\n" +
      "    \"TransactionType\" : \"Payment\",\n" +
      "    \"hash\" : \"E22068A818EA853DD3B7B574FF58C3A84D1F664495FF6ECD11D3B03B1D2FC2F7\",\n" +
      "    \"validated\" : false\n" +
      "  }, {\n" +
      "    \"Account\" : \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"Fee\" : \"12\",\n" +
      "    \"Sequence\" : 1,\n" +
      "    \"SourceTag\" : 11747,\n" +
      "    \"Flags\" : 2147483648,\n" +
      "    \"Amount\" : \"10000\",\n" +
      "    \"Destination\" : \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\",\n" +
      "    \"DestinationTag\" : 23480,\n" +
      "    \"CancelAfter\" : 533257958,\n" +
      "    \"FinishAfter\" : 533171558,\n" +
      "    \"TransactionType\" : \"EscrowCreate\",\n" +
      "    \"hash\" : \"E939C30F233E3E6B0A9F829BDDA258CB9DA38D11C0F66C7D60E38B9D9FA987B8\",\n" +
      "    \"validated\" : false\n" +
      "  } ],\n" +
      "  \"close_time_resolution\" : 10\n" +
      "}";

    String serialized = objectMapper.writeValueAsString(ledgerHeader);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);

    LedgerHeader deserialized = objectMapper.readValue(serialized, LedgerHeader.class);
    assertThat(deserialized).isEqualTo(ledgerHeader);
  }

  /**
   * This test exists in response to GH issue #14 where a reporter encountered an exception trying to deserialize
   * particular values of `closeTimeHuman`. That issue was ultimately unreproducbile, but this test ensures that remains
   * the case.
   *
   * @see "https://github.com/XRPLF/xrpl4j/issues/114"
   */
  @Test
  public void deserializeWithProblematicTimeStamp() throws JsonProcessingException, JSONException {
    LedgerHeader ledgerHeader = LedgerHeader.builder()
      .accountHash(Hash256.of("B258A8BB4743FB74CBBD6E9F67E4A56C4432EA09E5805E4CC2DA26F2DBE8F3D1"))
      .closeTime(UnsignedLong.valueOf(638329271))
      .closeTimeHuman(ZonedDateTime.parse("2021-Jun-11 09:06:10.000000000 UTC",
        DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss.SSSSSSSSS z")).withZoneSameLocal(ZoneId.of("UTC")))
      .closeTimeResolution(UnsignedInteger.valueOf(10))
      .closed(true)
      .ledgerHash(Hash256.of("3652D7FD0576BC452C0D2E9B747BDD733075971D1A9A1D98125055DEF428721A"))
      .ledgerIndex(LedgerIndex.of(UnsignedLong.valueOf(54300940)))
      .parentCloseTime(UnsignedLong.valueOf(638329270))
      .parentHash(Hash256.of("AE996778246BC81F85D5AF051241DAA577C23BCA04C034A7074F93700194520D"))
      .totalCoins(XrpCurrencyAmount.ofDrops(99991024049618156L))
      .transactionHash(Hash256.of("FC6FFCB71B2527DDD630EE5409D38913B4D4C026AA6C3B14A3E9D4ED45CFE30D"))
      .addTransactions(
        TransactionResult.builder()
          .transaction(
            Payment.builder()
              .account(Address.of("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe"))
              .amount(XrpCurrencyAmount.ofDrops(1000000000))
              .destination(Address.of("rBkoiq4sVF5N6zu4QwZPm9iVQht4BtxtM1"))
              .fee(XrpCurrencyAmount.ofDrops(12))
              .flags(Flags.PaymentFlags.of(2147483648L))
              .lastLedgerSequence(UnsignedInteger.valueOf(13010048))
              .sequence(UnsignedInteger.valueOf(2062124))
              .signingPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
              .transactionSignature("3045022100E1F74E892839A9818D991F1E7B3D069ED499A5D412DD6C8C2634E87" +
                "D0A37D3750220141AF3DCE6DA4D134614E49C99FFB1E498C238B46FC47CF3F79A989C4A2053AC")
              .hash(Hash256.of("E22068A818EA853DD3B7B574FF58C3A84D1F664495FF6ECD11D3B03B1D2FC2F7"))
              .build()
          )
          .build(),
        TransactionResult.builder()
          .transaction(
            EscrowCreate.builder()
              .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
              .amount(XrpCurrencyAmount.ofDrops(10000))
              .destination(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
              .fee(XrpCurrencyAmount.ofDrops(12))
              .cancelAfter(UnsignedLong.valueOf(533257958))
              .finishAfter(UnsignedLong.valueOf(533171558))
              .sequence(UnsignedInteger.ONE)
              .destinationTag(UnsignedInteger.valueOf(23480))
              .sourceTag(UnsignedInteger.valueOf(11747))
              .hash(Hash256.of("E939C30F233E3E6B0A9F829BDDA258CB9DA38D11C0F66C7D60E38B9D9FA987B8"))
              .build()
          )
          .build()
      )
      .build();

    String json = "{\n" +
      "  \"ledger_index\" : 54300940,\n" +
      "  \"ledger_hash\" : \"3652D7FD0576BC452C0D2E9B747BDD733075971D1A9A1D98125055DEF428721A\",\n" +
      "  \"account_hash\" : \"B258A8BB4743FB74CBBD6E9F67E4A56C4432EA09E5805E4CC2DA26F2DBE8F3D1\",\n" +
      "  \"close_time\" : 638329271,\n" +
      "  \"close_time_human\" : \"2021-Jun-11 09:06:10.000000000 UTC\",\n" +
      "  \"closed\" : true,\n" +
      "  \"parent_hash\" : \"AE996778246BC81F85D5AF051241DAA577C23BCA04C034A7074F93700194520D\",\n" +
      "  \"parent_close_time\" : 638329270,\n" +
      "  \"total_coins\" : \"99991024049618156\",\n" +
      "  \"transaction_hash\" : \"FC6FFCB71B2527DDD630EE5409D38913B4D4C026AA6C3B14A3E9D4ED45CFE30D\",\n" +
      "  \"transactions\" : [ {\n" +
      "    \"Account\" : \"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe\",\n" +
      "    \"Fee\" : \"12\",\n" +
      "    \"Sequence\" : 2062124,\n" +
      "    \"LastLedgerSequence\" : 13010048,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"TxnSignature\" : \"3045022100E1F74E892839A9818D991F1E7B3D069ED499A5D412DD6C8C2634E87D0A37D375022014" +
      "1AF3DCE6DA4D134614E49C99FFB1E498C238B46FC47CF3F79A989C4A2053AC\",\n" +
      "    \"Flags\" : 2147483648,\n" +
      "    \"Amount\" : \"1000000000\",\n" +
      "    \"Destination\" : \"rBkoiq4sVF5N6zu4QwZPm9iVQht4BtxtM1\",\n" +
      "    \"TransactionType\" : \"Payment\",\n" +
      "    \"hash\" : \"E22068A818EA853DD3B7B574FF58C3A84D1F664495FF6ECD11D3B03B1D2FC2F7\",\n" +
      "    \"validated\" : false\n" +
      "  }, {\n" +
      "    \"Account\" : \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"Fee\" : \"12\",\n" +
      "    \"Sequence\" : 1,\n" +
      "    \"SourceTag\" : 11747,\n" +
      "    \"Flags\" : 2147483648,\n" +
      "    \"Amount\" : \"10000\",\n" +
      "    \"Destination\" : \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\",\n" +
      "    \"DestinationTag\" : 23480,\n" +
      "    \"CancelAfter\" : 533257958,\n" +
      "    \"FinishAfter\" : 533171558,\n" +
      "    \"TransactionType\" : \"EscrowCreate\",\n" +
      "    \"hash\" : \"E939C30F233E3E6B0A9F829BDDA258CB9DA38D11C0F66C7D60E38B9D9FA987B8\",\n" +
      "    \"validated\" : false\n" +
      "  } ],\n" +
      "  \"close_time_resolution\" : 10\n" +
      "}";

    String serialized = objectMapper.writeValueAsString(ledgerHeader);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);

    LedgerHeader deserialized = objectMapper.readValue(serialized, LedgerHeader.class);
    assertThat(deserialized).isEqualTo(ledgerHeader);
  }
}
