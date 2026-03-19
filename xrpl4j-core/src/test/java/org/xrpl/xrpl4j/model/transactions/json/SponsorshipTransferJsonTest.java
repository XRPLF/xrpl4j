package org.xrpl.xrpl4j.model.transactions.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.SponsorshipTransferFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.SponsorshipTransfer;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class SponsorshipTransferJsonTest extends AbstractJsonTest {

  @Test
  public void testEndSponsorshipJson() throws JsonProcessingException, JSONException {
    SponsorshipTransfer transfer = SponsorshipTransfer.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .objectId(Hash256.of("E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321"))
      .flags(SponsorshipTransferFlags.builder().tfSponsorshipEnd(true).build())
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{" +
      "  \"Account\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\"," +
      "  \"TransactionType\": \"SponsorshipTransfer\"," +
      "  \"Fee\": \"10\"," +
      "  \"Sequence\": 1," +
      "  \"ObjectID\": \"E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321\"," +
      "  \"Flags\": 2147549184," +
      "  \"SigningPubKey\": \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"" +
      "}";

    assertCanSerializeAndDeserialize(transfer, json);
  }

  @Test
  public void testCreateSponsorshipJson() throws JsonProcessingException, JSONException {
    SponsorshipTransfer transfer = SponsorshipTransfer.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .objectId(Hash256.of("E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321"))
      .sponsor(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
      .flags(SponsorshipTransferFlags.builder().tfSponsorshipCreate(true).build())
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{" +
      "  \"Account\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\"," +
      "  \"TransactionType\": \"SponsorshipTransfer\"," +
      "  \"Fee\": \"10\"," +
      "  \"Sequence\": 1," +
      "  \"ObjectID\": \"E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321\"," +
      "  \"Sponsor\": \"rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY\"," +
      "  \"Flags\": 2147614720," +
      "  \"SigningPubKey\": \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"" +
      "}";

    assertCanSerializeAndDeserialize(transfer, json);
  }

  @Test
  public void testReassignSponsorshipJson() throws JsonProcessingException, JSONException {
    SponsorshipTransfer transfer = SponsorshipTransfer.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .objectId(Hash256.of("E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321"))
      .sponsor(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
      .flags(SponsorshipTransferFlags.builder().tfSponsorshipReassign(true).build())
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{" +
      "  \"Account\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\"," +
      "  \"TransactionType\": \"SponsorshipTransfer\"," +
      "  \"Fee\": \"10\"," +
      "  \"Sequence\": 1," +
      "  \"ObjectID\": \"E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321\"," +
      "  \"Sponsor\": \"rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY\"," +
      "  \"Flags\": 2147745792," +
      "  \"SigningPubKey\": \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"" +
      "}";

    assertCanSerializeAndDeserialize(transfer, json);
  }

}

