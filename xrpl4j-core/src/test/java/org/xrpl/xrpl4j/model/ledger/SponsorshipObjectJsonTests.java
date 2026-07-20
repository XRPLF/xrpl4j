package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class SponsorshipObjectJsonTests extends AbstractJsonTest {

  @Test
  public void testMinimalSponsorshipObjectJson() throws JsonProcessingException, JSONException {
    SponsorshipObject object = SponsorshipObject.builder()
      .owner(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .sponsee(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
      .ownerNode("0")
      .sponseeNode("0")
      .previousTransactionId(Hash256.of("E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(14091160))
      .index(Hash256.of("49647F0D748DC3FE26BDACBC57F251AADEFFF391403EC9BF87C97F67E9977FB0"))
      .build();

    String json = "{\n" +
      "  \"LedgerEntryType\": \"Sponsorship\",\n" +
      "  \"Flags\": 0,\n" +
      "  \"Owner\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\",\n" +
      "  \"Sponsee\": \"rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY\",\n" +
      "  \"RemainingOwnerCount\": 0,\n" +
      "  \"OwnerNode\": \"0\",\n" +
      "  \"SponseeNode\": \"0\",\n" +
      "  \"PreviousTxnID\": \"E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321\",\n" +
      "  \"PreviousTxnLgrSeq\": 14091160,\n" +
      "  \"index\": \"49647F0D748DC3FE26BDACBC57F251AADEFFF391403EC9BF87C97F67E9977FB0\"\n" +
      "}";

    assertCanSerializeAndDeserialize(object, json);
  }

  @Test
  public void testSponsorshipObjectWithFeeAmountJson() throws JsonProcessingException, JSONException {
    SponsorshipObject object = SponsorshipObject.builder()
      .owner(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .sponsee(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
      .feeAmount(XrpCurrencyAmount.ofDrops(1000000))
      .ownerNode("0")
      .sponseeNode("0")
      .previousTransactionId(Hash256.of("E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(14091160))
      .index(Hash256.of("49647F0D748DC3FE26BDACBC57F251AADEFFF391403EC9BF87C97F67E9977FB0"))
      .build();

    String json = "{\n" +
      "  \"LedgerEntryType\": \"Sponsorship\",\n" +
      "  \"Flags\": 0,\n" +
      "  \"Owner\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\",\n" +
      "  \"Sponsee\": \"rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY\",\n" +
      "  \"FeeAmount\": \"1000000\",\n" +
      "  \"RemainingOwnerCount\": 0,\n" +
      "  \"OwnerNode\": \"0\",\n" +
      "  \"SponseeNode\": \"0\",\n" +
      "  \"PreviousTxnID\": \"E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321\",\n" +
      "  \"PreviousTxnLgrSeq\": 14091160,\n" +
      "  \"index\": \"49647F0D748DC3FE26BDACBC57F251AADEFFF391403EC9BF87C97F67E9977FB0\"\n" +
      "}";

    assertCanSerializeAndDeserialize(object, json);
  }

  @Test
  public void testSponsorshipObjectWithMaxFeeJson() throws JsonProcessingException, JSONException {
    SponsorshipObject object = SponsorshipObject.builder()
      .owner(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .sponsee(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
      .feeAmount(XrpCurrencyAmount.ofDrops(1000000))
      .maxFee(XrpCurrencyAmount.ofDrops(100))
      .ownerNode("0")
      .sponseeNode("0")
      .previousTransactionId(Hash256.of("E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(14091160))
      .index(Hash256.of("49647F0D748DC3FE26BDACBC57F251AADEFFF391403EC9BF87C97F67E9977FB0"))
      .build();

    String json = "{\n" +
      "  \"LedgerEntryType\": \"Sponsorship\",\n" +
      "  \"Flags\": 0,\n" +
      "  \"Owner\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\",\n" +
      "  \"Sponsee\": \"rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY\",\n" +
      "  \"FeeAmount\": \"1000000\",\n" +
      "  \"MaxFee\": \"100\",\n" +
      "  \"RemainingOwnerCount\": 0,\n" +
      "  \"OwnerNode\": \"0\",\n" +
      "  \"SponseeNode\": \"0\",\n" +
      "  \"PreviousTxnID\": \"E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321\",\n" +
      "  \"PreviousTxnLgrSeq\": 14091160,\n" +
      "  \"index\": \"49647F0D748DC3FE26BDACBC57F251AADEFFF391403EC9BF87C97F67E9977FB0\"\n" +
      "}";

    assertCanSerializeAndDeserialize(object, json);
  }

  @Test
  public void testSponsorshipObjectWithRemainingOwnerCountJson() throws JsonProcessingException, JSONException {
    SponsorshipObject object = SponsorshipObject.builder()
      .owner(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .sponsee(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
      .remainingOwnerCount(UnsignedInteger.valueOf(5))
      .ownerNode("0")
      .sponseeNode("0")
      .previousTransactionId(Hash256.of("E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(14091160))
      .index(Hash256.of("49647F0D748DC3FE26BDACBC57F251AADEFFF391403EC9BF87C97F67E9977FB0"))
      .build();

    String json = "{\n" +
      "  \"LedgerEntryType\": \"Sponsorship\",\n" +
      "  \"Flags\": 0,\n" +
      "  \"Owner\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\",\n" +
      "  \"Sponsee\": \"rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY\",\n" +
      "  \"RemainingOwnerCount\": 5,\n" +
      "  \"OwnerNode\": \"0\",\n" +
      "  \"SponseeNode\": \"0\",\n" +
      "  \"PreviousTxnID\": \"E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321\",\n" +
      "  \"PreviousTxnLgrSeq\": 14091160,\n" +
      "  \"index\": \"49647F0D748DC3FE26BDACBC57F251AADEFFF391403EC9BF87C97F67E9977FB0\"\n" +
      "}";

    assertCanSerializeAndDeserialize(object, json);
  }

  @Test
  public void testSponsorshipObjectWithAllFieldsJson() throws JsonProcessingException, JSONException {
    SponsorshipObject object = SponsorshipObject.builder()
      .owner(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .sponsee(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
      .feeAmount(XrpCurrencyAmount.ofDrops(1000000))
      .maxFee(XrpCurrencyAmount.ofDrops(100))
      .remainingOwnerCount(UnsignedInteger.valueOf(5))
      .ownerNode("7B")
      .sponseeNode("1C8")
      .previousTransactionId(Hash256.of("E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(14091160))
      .index(Hash256.of("49647F0D748DC3FE26BDACBC57F251AADEFFF391403EC9BF87C97F67E9977FB0"))
      .build();

    String json = "{\n" +
      "  \"LedgerEntryType\": \"Sponsorship\",\n" +
      "  \"Flags\": 0,\n" +
      "  \"Owner\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\",\n" +
      "  \"Sponsee\": \"rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY\",\n" +
      "  \"FeeAmount\": \"1000000\",\n" +
      "  \"MaxFee\": \"100\",\n" +
      "  \"RemainingOwnerCount\": 5,\n" +
      "  \"OwnerNode\": \"7B\",\n" +
      "  \"SponseeNode\": \"1C8\",\n" +
      "  \"PreviousTxnID\": \"E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321\",\n" +
      "  \"PreviousTxnLgrSeq\": 14091160,\n" +
      "  \"index\": \"49647F0D748DC3FE26BDACBC57F251AADEFFF391403EC9BF87C97F67E9977FB0\"\n" +
      "}";

    assertCanSerializeAndDeserialize(object, json);
  }

  @Test
  public void testSponsorshipObjectWithSponsorJson() throws JsonProcessingException, JSONException {
    SponsorshipObject object = SponsorshipObject.builder()
      .owner(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .sponsee(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
      .ownerNode("0")
      .sponseeNode("0")
      .previousTransactionId(Hash256.of("E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(14091160))
      .sponsor(Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXEkk"))
      .index(Hash256.of("49647F0D748DC3FE26BDACBC57F251AADEFFF391403EC9BF87C97F67E9977FB0"))
      .build();

    String json = "{\n" +
      "  \"LedgerEntryType\": \"Sponsorship\",\n" +
      "  \"Flags\": 0,\n" +
      "  \"Owner\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\",\n" +
      "  \"Sponsee\": \"rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY\",\n" +
      "  \"RemainingOwnerCount\": 0,\n" +
      "  \"OwnerNode\": \"0\",\n" +
      "  \"SponseeNode\": \"0\",\n" +
      "  \"PreviousTxnID\": \"E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321\",\n" +
      "  \"PreviousTxnLgrSeq\": 14091160,\n" +
      "  \"Sponsor\": \"rN7n7otQDd6FczFgLdlqtyMVrn3HMfXEkk\",\n" +
      "  \"index\": \"49647F0D748DC3FE26BDACBC57F251AADEFFF391403EC9BF87C97F67E9977FB0\"\n" +
      "}";

    assertCanSerializeAndDeserialize(object, json);
  }

}

