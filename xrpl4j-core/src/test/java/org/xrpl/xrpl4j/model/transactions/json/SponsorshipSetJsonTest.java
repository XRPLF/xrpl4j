package org.xrpl.xrpl4j.model.transactions.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.SponsorshipSet;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class SponsorshipSetJsonTest extends AbstractJsonTest {

  @Test
  public void testMinimalSponsorshipSetJson() throws JsonProcessingException, JSONException {
    SponsorshipSet sponsorshipSet = SponsorshipSet.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .sponsee(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{" +
      "  \"Account\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\"," +
      "  \"TransactionType\": \"SponsorshipSet\"," +
      "  \"Fee\": \"10\"," +
      "  \"Sequence\": 1," +
      "  \"Sponsee\": \"rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY\"," +
      "  \"SigningPubKey\": \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"" +
      "}";

    assertCanSerializeAndDeserialize(sponsorshipSet, json);
  }

  @Test
  public void testSponsorshipSetWithFeeAmountJson() throws JsonProcessingException, JSONException {
    SponsorshipSet sponsorshipSet = SponsorshipSet.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .sponsee(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
      .feeAmount(XrpCurrencyAmount.ofDrops(1000000))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{" +
      "  \"Account\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\"," +
      "  \"TransactionType\": \"SponsorshipSet\"," +
      "  \"Fee\": \"10\"," +
      "  \"Sequence\": 1," +
      "  \"Sponsee\": \"rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY\"," +
      "  \"FeeAmount\": \"1000000\"," +
      "  \"SigningPubKey\": \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"" +
      "}";

    assertCanSerializeAndDeserialize(sponsorshipSet, json);
  }

  @Test
  public void testSponsorshipSetWithMaxFeeJson() throws JsonProcessingException, JSONException {
    SponsorshipSet sponsorshipSet = SponsorshipSet.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .sponsee(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
      .feeAmount(XrpCurrencyAmount.ofDrops(1000000))
      .maxFee(XrpCurrencyAmount.ofDrops(100))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{" +
      "  \"Account\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\"," +
      "  \"TransactionType\": \"SponsorshipSet\"," +
      "  \"Fee\": \"10\"," +
      "  \"Sequence\": 1," +
      "  \"Sponsee\": \"rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY\"," +
      "  \"FeeAmount\": \"1000000\"," +
      "  \"MaxFee\": \"100\"," +
      "  \"SigningPubKey\": \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"" +
      "}";

    assertCanSerializeAndDeserialize(sponsorshipSet, json);
  }

  @Test
  public void testSponsorshipSetWithReserveCountJson() throws JsonProcessingException, JSONException {
    SponsorshipSet sponsorshipSet = SponsorshipSet.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .sponsee(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
      .reserveCount(UnsignedInteger.valueOf(5))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{" +
      "  \"Account\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\"," +
      "  \"TransactionType\": \"SponsorshipSet\"," +
      "  \"Fee\": \"10\"," +
      "  \"Sequence\": 1," +
      "  \"Sponsee\": \"rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY\"," +
      "  \"ReserveCount\": 5," +
      "  \"SigningPubKey\": \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"" +
      "}";

    assertCanSerializeAndDeserialize(sponsorshipSet, json);
  }

  @Test
  public void testSponsorshipSetWithAllFieldsJson() throws JsonProcessingException, JSONException {
    SponsorshipSet sponsorshipSet = SponsorshipSet.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .sponsee(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
      .counterpartySponsor(Address.of("rU6K7V3Po4snVhBBaU29sesqs2qTQJWDw1"))
      .feeAmount(XrpCurrencyAmount.ofDrops(1000000))
      .maxFee(XrpCurrencyAmount.ofDrops(100))
      .reserveCount(UnsignedInteger.valueOf(5))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{" +
      "  \"Account\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\"," +
      "  \"TransactionType\": \"SponsorshipSet\"," +
      "  \"Fee\": \"10\"," +
      "  \"Sequence\": 1," +
      "  \"Sponsee\": \"rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY\"," +
      "  \"CounterpartySponsor\": \"rU6K7V3Po4snVhBBaU29sesqs2qTQJWDw1\"," +
      "  \"FeeAmount\": \"1000000\"," +
      "  \"MaxFee\": \"100\"," +
      "  \"ReserveCount\": 5," +
      "  \"SigningPubKey\": \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"" +
      "}";

    assertCanSerializeAndDeserialize(sponsorshipSet, json);
  }

}

