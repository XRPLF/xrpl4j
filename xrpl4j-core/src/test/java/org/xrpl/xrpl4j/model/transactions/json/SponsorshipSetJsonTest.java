package org.xrpl.xrpl4j.model.transactions.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.SponsorshipSetFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.SponsorshipSet;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class SponsorshipSetJsonTest extends AbstractJsonTest {

  @Test
  public void testMinimalSponsorshipSetJson() throws JsonProcessingException, JSONException {
    // A SponsorshipSet must include at least one modification field or RequireSignFor*/ClearRequireSignFor*
    // flag (otherwise the transaction is a no-op and rippled rejects it with temREDUNDANT), so the "minimal"
    // fixture here sets only a flag and no amount/count fields.
    SponsorshipSet sponsorshipSet = SponsorshipSet.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .sponsee(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
      .flags(SponsorshipSetFlags.builder().tfRequireSignForFee().build())
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
      "  \"Flags\": 2147549184," +
      "  \"SigningPubKey\": \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"" +
      "}";

    assertCanSerializeAndDeserialize(sponsorshipSet, json);
  }

  @Test
  public void testSponsorshipSetWithFeeAmountDeltaJson() throws JsonProcessingException, JSONException {
    SponsorshipSet sponsorshipSet = SponsorshipSet.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .sponsee(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
      .feeAmountDelta(XrpCurrencyAmount.ofDrops(1000000))
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
      "  \"FeeAmountDelta\": \"1000000\"," +
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
      .feeAmountDelta(XrpCurrencyAmount.ofDrops(1000000))
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
      "  \"FeeAmountDelta\": \"1000000\"," +
      "  \"MaxFee\": \"100\"," +
      "  \"SigningPubKey\": \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"" +
      "}";

    assertCanSerializeAndDeserialize(sponsorshipSet, json);
  }

  @Test
  public void testSponsorshipSetWithRemainingOwnerCountDeltaJson() throws JsonProcessingException, JSONException {
    SponsorshipSet sponsorshipSet = SponsorshipSet.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .sponsee(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
      .remainingOwnerCountDelta(5)
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
      "  \"RemainingOwnerCountDelta\": 5," +
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
      .feeAmountDelta(XrpCurrencyAmount.ofDrops(1000000))
      .maxFee(XrpCurrencyAmount.ofDrops(100))
      .remainingOwnerCountDelta(5)
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
      "  \"FeeAmountDelta\": \"1000000\"," +
      "  \"MaxFee\": \"100\"," +
      "  \"RemainingOwnerCountDelta\": 5," +
      "  \"SigningPubKey\": \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"" +
      "}";

    assertCanSerializeAndDeserialize(sponsorshipSet, json);
  }

  @Test
  public void testSponsorshipSetDeleteWithCounterpartySponsorJson() throws JsonProcessingException, JSONException {
    // Only the sponsor can create/update a Sponsorship object, so when the sponsee (identified here via
    // Account, naming its sponsor via CounterpartySponsor) submits the transaction, tfDeleteObject must be set.
    SponsorshipSet sponsorshipSet = SponsorshipSet.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .counterpartySponsor(Address.of("rU6K7V3Po4snVhBBaU29sesqs2qTQJWDw1"))
      .flags(SponsorshipSetFlags.builder().tfDeleteObject().build())
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{" +
      "  \"Account\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\"," +
      "  \"TransactionType\": \"SponsorshipSet\"," +
      "  \"Fee\": \"10\"," +
      "  \"Sequence\": 1," +
      "  \"CounterpartySponsor\": \"rU6K7V3Po4snVhBBaU29sesqs2qTQJWDw1\"," +
      "  \"Flags\": 2148532224," +
      "  \"SigningPubKey\": \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"" +
      "}";

    assertCanSerializeAndDeserialize(sponsorshipSet, json);
  }

}

