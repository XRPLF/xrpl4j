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
import org.xrpl.xrpl4j.model.transactions.TransferFee;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class NfTokenMintJsonTests extends AbstractJsonTest {

  @Test
  public void testMinimalTrustSetJson() throws JsonProcessingException, JSONException {
    NfTokenMint nfTokenMint = NfTokenMint.builder()
      .fee(XrpCurrencyAmount.ofDrops(12))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .tokenTaxon(UnsignedLong.valueOf(146999694L))
      .sequence(UnsignedInteger.valueOf(12))
      .transferFee(TransferFee.of(UnsignedInteger.valueOf(1000)))
      .flags(Flags.NfTokenMintFlags.builder()
        .tfTransferable(true)
        .build())
      .build();

    String json = "{\n" +
      "    \"TransactionType\": \"NFTokenMint\",\n" +
      "    \"Account\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba\",\n" +
      "    \"Fee\": \"12\",\n" +
      "    \"Flags\": 2147483656,\n" +
      "    \"Sequence\": 12,\n" +
      "    \"TransferFee\": \"1000\",\n" +
      "    \"TokenTaxon\": 146999694\n" +
      "}";

    assertCanSerializeAndDeserialize(nfTokenMint, json);
  }
}
