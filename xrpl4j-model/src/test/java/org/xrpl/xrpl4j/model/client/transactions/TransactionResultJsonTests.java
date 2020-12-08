package org.xrpl.xrpl4j.model.client.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.ImmutablePayment;

public class TransactionResultJsonTests extends AbstractJsonTest {

  @Test
  public void deserializePaymentTransactionResult() throws JsonProcessingException {
    String json = "{\n" +
        "                    \"Account\": \"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe\",\n" +
        "                    \"Amount\": \"1000000000\",\n" +
        "                    \"Destination\": \"r3ubyDp4gPGKH5bJx9KMmzpTSTW7EtRixS\",\n" +
        "                    \"Fee\": \"12\",\n" +
        "                    \"Flags\": 2147483648,\n" +
        "                    \"LastLedgerSequence\": 13010048,\n" +
        "                    \"Sequence\": 2062126,\n" +
        "                    \"SigningPubKey\": \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
        "                    \"TransactionType\": \"Payment\",\n" +
        "                    \"TxnSignature\": \"3045022100AA15E1F82455712B7D3CE138F6B913238CFBFF56DCB3E2DE39624EE4C639F190022003A04CE739D93DF23BB7F646E274191F550AC73975737FA5436BCF8FEF29E4DD\",\n" +
        "                    \"hash\": \"E939C30F233E3E6B0A9F829BDDA258CB9DA38D11C0F66C7D60E38B9D9FA987B8\"\n" +
        "                }";
    TransactionResult transactionResult = objectMapper.readValue(json, TransactionResult.class);
    assertThat(transactionResult.transaction()).isExactlyInstanceOf(ImmutablePayment.class);
    assertThat(transactionResult.hash()).isEqualTo(Hash256.of("E939C30F233E3E6B0A9F829BDDA258CB9DA38D11C0F66C7D60E38B9D9FA987B8"));
  }
}
