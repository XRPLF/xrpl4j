package org.xrpl.xrpl4j.model.ledger;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.EscrowCreate;
import org.xrpl.xrpl4j.model.transactions.Flags;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class LedgerHeaderJsonTests extends AbstractJsonTest {

  @Test
  public void deserializeLedgerHeaderWithTransactions() throws JsonProcessingException, JSONException {
    LedgerHeader ledgerHeader = LedgerHeader.builder()
        .closed(false)
        .ledgerIndex(LedgerIndex.of(UnsignedLong.valueOf(13010045)))
        .parentHash(Hash256.of("573B794CE1E5164E6AD4FF47FD7DC08004D103A8C22A364670188CAD31F3311F"))
        .ledgerIndex(LedgerIndex.of(UnsignedLong.valueOf(13010045)))
        .addTransactions(
            TransactionResult.<Payment>builder()
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
                        .build()
                )
                .hash(Hash256.of("E22068A818EA853DD3B7B574FF58C3A84D1F664495FF6ECD11D3B03B1D2FC2F7"))
                .build(),
            TransactionResult.<EscrowCreate>builder()
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
                        .build()
                )
                .hash(Hash256.of("E939C30F233E3E6B0A9F829BDDA258CB9DA38D11C0F66C7D60E38B9D9FA987B8"))
                .build()
        )
        .build();
    String json = "{\n" +
        "            \"closed\": false,\n" +
        "            \"ledger_index\": 13010045,\n" +
        "            \"parent_hash\": \"573B794CE1E5164E6AD4FF47FD7DC08004D103A8C22A364670188CAD31F3311F\",\n" +
        "            \"transactions\": [\n" +
        "                {\n" +
        "                    \"Account\": \"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe\",\n" +
        "                    \"Amount\": \"1000000000\",\n" +
        "                    \"Destination\": \"rBkoiq4sVF5N6zu4QwZPm9iVQht4BtxtM1\",\n" +
        "                    \"Fee\": \"12\",\n" +
        "                    \"Flags\": 2147483648,\n" +
        "                    \"LastLedgerSequence\": 13010048,\n" +
        "                    \"Sequence\": 2062124,\n" +
        "                    \"SigningPubKey\": \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C00" +
        "18B37FC\",\n" +
        "                    \"TransactionType\": \"Payment\",\n" +
        "                    \"TxnSignature\": \"3045022100E1F74E892839A9818D991F1E7B3D069ED499A5D412DD6C8C2634E87D0" +
        "A37D3750220141AF3DCE6DA4D134614E49C99FFB1E498C238B46FC47CF3F79A989C4A2053AC\",\n" +
        "                    \"hash\": \"E22068A818EA853DD3B7B574FF58C3A84D1F664495FF6ECD11D3B03B1D2FC2F7\",\n" +
        "                    \"validated\": false\n" +
        "                },\n" +
        "                {\n" +
        "                    \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
        "                    \"TransactionType\": \"EscrowCreate\",\n" +
        "                    \"Amount\": \"10000\",\n" +
        "                    \"Destination\": \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\",\n" +
        "                    \"CancelAfter\": 533257958,\n" +
        "                    \"FinishAfter\": 533171558,\n" +
        "                   \"DestinationTag\": 23480,\n" +
        "                   \"SourceTag\": 11747,\n" +
        "                   \"Sequence\": 1,\n" +
        "                   \"Flags\": 2147483648,\n" +
        "                   \"Fee\": \"12\",\n" +
        "                   \"hash\": \"E939C30F233E3E6B0A9F829BDDA258CB9DA38D11C0F66C7D60E38B9D9FA987B8\",\n" +
        "                    \"validated\": false\n" +
        "                }\n" +
        "            ]\n" +
        "        }\n";

    String serialized = objectMapper.writeValueAsString(ledgerHeader);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);

    LedgerHeader deserialized = objectMapper.readValue(serialized, LedgerHeader.class);
    assertThat(deserialized).isEqualTo(ledgerHeader);
  }
}
