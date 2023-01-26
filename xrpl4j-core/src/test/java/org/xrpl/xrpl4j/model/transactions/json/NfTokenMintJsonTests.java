package org.xrpl.xrpl4j.model.transactions.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.NfTokenMint;
import org.xrpl.xrpl4j.model.transactions.NfTokenUri;
import org.xrpl.xrpl4j.model.transactions.TransferFee;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class NfTokenMintJsonTests extends AbstractJsonTest {

  @Test
  public void testMinimalNfTokenMintJson() throws JsonProcessingException, JSONException {
    NfTokenMint nfTokenMint = NfTokenMint.builder()
      .fee(XrpCurrencyAmount.ofDrops(12))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .tokenTaxon(UnsignedLong.valueOf(146999694L))
      .sequence(UnsignedInteger.valueOf(12))
      .transferFee(TransferFee.of(UnsignedInteger.valueOf(1000)))
      .flags(Flags.NfTokenMintFlags.builder()
        .tfTransferable(true)
        .build())
      .signingPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      .build();

    String json = "{\n" +
      "    \"TransactionType\": \"NFTokenMint\",\n" +
      "    \"Account\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba\",\n" +
      "    \"Fee\": \"12\",\n" +
      "    \"Flags\": 2147483656,\n" +
      "    \"Sequence\": 12,\n" +
      "    \"TransferFee\": \"1000\",\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"NFTokenTaxon\": 146999694\n" +
      "}";

    assertCanSerializeAndDeserialize(nfTokenMint, json);
  }

  @Test
  public void testMinimalNfTokenMintWithUriJson() throws JsonProcessingException, JSONException {

    String uri = "ipfs://bafybeigdyrzt5sfp7udm7hu76uh7y26nf4dfuylqabf3oclgtqy55fbzdi";
    NfTokenMint nfTokenMint = NfTokenMint.builder()
      .fee(XrpCurrencyAmount.ofDrops(12))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .tokenTaxon(UnsignedLong.valueOf(146999694L))
      .sequence(UnsignedInteger.valueOf(12))
      .transferFee(TransferFee.of(UnsignedInteger.valueOf(1000)))
      .uri(NfTokenUri.ofPlainText(uri))
      .flags(Flags.NfTokenMintFlags.builder()
        .tfTransferable(true)
        .build())
      .signingPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      .build();

    String json = "{\n" +
      "    \"TransactionType\": \"NFTokenMint\",\n" +
      "    \"Account\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba\",\n" +
      "    \"Fee\": \"12\",\n" +
      "    \"Flags\": 2147483656,\n" +
      "    \"Sequence\": 12,\n" +
      "    \"TransferFee\": \"1000\",\n" +
      "    \"URI\": \"697066733A2F2F62616679626569676479727A74357366703775646D3768753736756837" +
      "7932366E6634646675796C71616266336F636C67747179353566627A6469\",\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"NFTokenTaxon\": 146999694\n" +
      "}";

    assertCanSerializeAndDeserialize(nfTokenMint, json);
  }
}
