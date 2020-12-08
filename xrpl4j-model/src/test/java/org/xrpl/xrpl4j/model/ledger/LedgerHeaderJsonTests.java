package org.xrpl.xrpl4j.model.ledger;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedLong;
import org.junit.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.TransactionType;

public class LedgerHeaderJsonTests extends AbstractJsonTest {

  @Test
  public void deserializeLedgerHeaderWithTransactions() throws JsonProcessingException {
    String json = "{\n" +
        "            \"closed\": false,\n" +
        "            \"ledger_index\": \"13010045\",\n" +
        "            \"parent_hash\": \"573B794CE1E5164E6AD4FF47FD7DC08004D103A8C22A364670188CAD31F3311F\",\n" +
        "            \"seqNum\": \"13010045\",\n" +
        "            \"transactions\": [\n" +
        "                {\n" +
        "                    \"Account\": \"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe\",\n" +
        "                    \"Amount\": \"1000000000\",\n" +
        "                    \"Destination\": \"rNi1ueM7a18ac9qQiDPJpKwn1YBT65zdK\",\n" +
        "                    \"Fee\": \"12\",\n" +
        "                    \"Flags\": 2147483648,\n" +
        "                    \"LastLedgerSequence\": 13010048,\n" +
        "                    \"Sequence\": 2062125,\n" +
        "                    \"SigningPubKey\": \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
        "                    \"TransactionType\": \"Payment\",\n" +
        "                    \"TxnSignature\": \"3044022014C1C530E7169D0B99E7BD72C74B565621350E4CD5BAC52F38D3E9612629DDA90220791BA52A8B879075AE6C4E69B87957D44C78E49FAF8090EDC4E4A27079CF0422\",\n" +
        "                    \"hash\": \"31FA376B21FF0E3DB731F0EFFE0BE47DC423A86E02876DB5B94C34BA7360664A\"\n" +
        "                },\n" +
        "                {\n" +
        "                    \"Account\": \"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe\",\n" +
        "                    \"Amount\": \"1000000000\",\n" +
        "                    \"Destination\": \"rBkoiq4sVF5N6zu4QwZPm9iVQht4BtxtM1\",\n" +
        "                    \"Fee\": \"12\",\n" +
        "                    \"Flags\": 2147483648,\n" +
        "                    \"LastLedgerSequence\": 13010048,\n" +
        "                    \"Sequence\": 2062124,\n" +
        "                    \"SigningPubKey\": \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
        "                    \"TransactionType\": \"Payment\",\n" +
        "                    \"TxnSignature\": \"3045022100E1F74E892839A9818D991F1E7B3D069ED499A5D412DD6C8C2634E87D0A37D3750220141AF3DCE6DA4D134614E49C99FFB1E498C238B46FC47CF3F79A989C4A2053AC\",\n" +
        "                    \"hash\": \"E22068A818EA853DD3B7B574FF58C3A84D1F664495FF6ECD11D3B03B1D2FC2F7\"\n" +
        "                },\n" +
        "                {\n" +
        "                    \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
        "                    \"TransactionType\": \"EscrowCreate\",\n" +
        "                    \"Amount\": \"10000\",\n" +
        "                    \"Destination\": \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\",\n" +
        "                    \"CancelAfter\": 533257958,\n" +
        "                    \"FinishAfter\": 533171558,\n" +
        "                   \"Condition\": \"A0258020E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855810100\",\n" +
        "                   \"DestinationTag\": 23480,\n" +
        "                   \"SourceTag\": 11747,\n" +
        "                   \"Sequence\": 1,\n" +
        "                   \"Flags\": 2147483648,\n" +
        "                   \"Fee\": \"12\",\n" +
        "                   \"hash\": \"E939C30F233E3E6B0A9F829BDDA258CB9DA38D11C0F66C7D60E38B9D9FA987B8\"\n" +
        "                }\n" +
        "            ]\n" +
        "        }\n" +
        "        \"ledger_current_index\": 13010045,\n" +
        "        \"status\": \"success\",\n" +
        "        \"validated\": false\n" +
        "    }";

    LedgerHeader ledgerHeader = objectMapper.readValue(json, LedgerHeader.class);
    assertThat(ledgerHeader.closed()).isFalse();
    assertThat(ledgerHeader.ledgerIndex()).isEqualTo(LedgerIndex.of(UnsignedLong.valueOf(13010045)));
    assertThat(ledgerHeader.parentHash()).isEqualTo(Hash256.of("573B794CE1E5164E6AD4FF47FD7DC08004D103A8C22A364670188CAD31F3311F"));
    assertThat((int) ledgerHeader.transactions().stream()
        .filter(result -> result.transaction().transactionType().equals(TransactionType.PAYMENT)).count())
        .isEqualTo(2);
    assertThat((int) ledgerHeader.transactions().stream()
        .filter(result -> result.transaction().transactionType().equals(TransactionType.ESCROW_CREATE)).count())
        .isEqualTo(1);
  }
}
