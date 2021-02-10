package org.xrpl.xrpl4j.model.client.transactions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.json.JSONException;
import org.junit.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class SubmitResultJsonTests extends AbstractJsonTest {

  @Test
  public void testPaymentTransactionResultJson() throws JsonProcessingException, JSONException {
    SubmitResult<Payment> paymentResult = SubmitResult.<Payment>builder()
        .accepted(true)
        .accountSequenceAvailable(UnsignedInteger.valueOf(362))
        .accountSequenceNext(UnsignedInteger.valueOf(362))
        .applied(true)
        .broadcast(true)
        .engineResult("tesSUCCESS")
        .engineResultMessage("The transaction was applied. Only final in a validated ledger.")
        .status("success")
        .kept(true)
        .openLedgerCost(XrpCurrencyAmount.ofDrops(10))
        .queued(false)
        .transactionBlob("1200002280000000240000016961D4838D7EA4C6800000000000000000000000000055534400000000004" +
            "B4E9C06F24296074F7BC48F92A97916C6DC5EA9684000000000002710732103AB40A0490F9B7ED8DF29D246BF2D6269820" +
            "A0EE7742ACDD457BEA7C7D0931EDB74473045022100A7CCD11455E47547FF617D5BFC15D120D9053DFD0536B044F10CA36" +
            "31CD609E502203B61DEE4AC027C5743A1B56AF568D1E2B8E79BB9E9E14744AC87F38375C3C2F181144B4E9C06F24296074" +
            "F7BC48F92A97916C6DC5EA983143E9D4A2B8AA0780F682D136F7A56D6724EF53754")
        .transactionResult(TransactionResult.<Payment>builder()
            .transaction(Payment.builder()
                .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
                .amount(IssuedCurrencyAmount.builder()
                    .currency("USD")
                    .issuer(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
                    .value("1")
                    .build())
                .destination(Address.of("ra5nK24KXen9AHvsdFTKHSANinZseWnPcX"))
                .fee(XrpCurrencyAmount.ofDrops(10000))
                .sequence(UnsignedInteger.valueOf(361))
                .signingPublicKey("03AB40A0490F9B7ED8DF29D246BF2D6269820A0EE7742ACDD457BEA7C7D0931EDB")
                .transactionSignature("3045022100A7CCD11455E47547FF617D5BFC15D120D9053DFD0536B044F10CA3631CD609E5" +
                    "02203B61DEE4AC027C5743A1B56AF568D1E2B8E79BB9E9E14744AC87F38375C3C2F1")
                .hash(Hash256.of("5B31A7518DC304D5327B4887CD1F7DC2C38D5F684170097020C7C9758B973847"))
                .build())
//            .hash(Hash256.of("5B31A7518DC304D5327B4887CD1F7DC2C38D5F684170097020C7C9758B973847"))
            .build())
        .validatedLedgerIndex(LedgerIndex.of(UnsignedLong.valueOf(21184416)))
        .build();

    String json = "{\n" +
        "        \"accepted\" : true,\n" +
        "        \"account_sequence_available\" : 362,\n" +
        "        \"account_sequence_next\" : 362,\n" +
        "        \"applied\" : true,\n" +
        "        \"broadcast\" : true,\n" +
        "        \"engine_result\": \"tesSUCCESS\",\n" +
        "        \"engine_result_message\": \"The transaction was applied. Only final in a validated ledger.\",\n" +
        "        \"status\": \"success\",\n" +
        "        \"kept\" : true,\n" +
        "        \"open_ledger_cost\": \"10\",\n" +
        "        \"queued\" : false,\n" +
        "        \"tx_blob\": \"1200002280000000240000016961D4838D7EA4C6800000000000000000000000000055534400000000004" +
        "B4E9C06F24296074F7BC48F92A97916C6DC5EA9684000000000002710732103AB40A0490F9B7ED8DF29D246BF2D6269820A0EE7742AC" +
        "DD457BEA7C7D0931EDB74473045022100A7CCD11455E47547FF617D5BFC15D120D9053DFD0536B044F10CA3631CD609E502203B61DEE" +
        "4AC027C5743A1B56AF568D1E2B8E79BB9E9E14744AC87F38375C3C2F181144B4E9C06F24296074F7BC48F92A97916C6DC5EA983143E9" +
        "D4A2B8AA0780F682D136F7A56D6724EF53754\",\n" +
        "        \"tx_json\": {\n" +
        "            \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
        "            \"Amount\": {\n" +
        "                \"currency\": \"USD\",\n" +
        "                \"issuer\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
        "                \"value\": \"1\"\n" +
        "            },\n" +
        "            \"Destination\": \"ra5nK24KXen9AHvsdFTKHSANinZseWnPcX\",\n" +
        "            \"Fee\": \"10000\",\n" +
        "            \"Flags\": 2147483648,\n" +
        "            \"Sequence\": 361,\n" +
        "            \"SigningPubKey\": \"03AB40A0490F9B7ED8DF29D246BF2D6269820A0EE7742ACDD457BEA7C7D0931EDB\",\n" +
        "            \"TransactionType\": \"Payment\",\n" +
        "            \"TxnSignature\": \"3045022100A7CCD11455E47547FF617D5BFC15D120D9053DFD0536B044F10CA3631CD609E5" +
        "02203B61DEE4AC027C5743A1B56AF568D1E2B8E79BB9E9E14744AC87F38375C3C2F1\",\n" +
        "            \"validated\" : false,\n" +
        "            \"hash\": \"5B31A7518DC304D5327B4887CD1F7DC2C38D5F684170097020C7C9758B973847\"\n" +
        "        },\n" +
        "       \"validated_ledger_index\" : 21184416" +
        "    }";

    assertCanSerializeAndDeserialize(paymentResult, json);
  }
}
