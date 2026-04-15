package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.AssetAmount;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.LoanBrokerData;

class MetaLoanBrokerObjectTest extends AbstractJsonTest {

  @Test
  void testMetaLoanBrokerObjectWithAllFields() throws JsonProcessingException, JSONException {
    MetaLoanBrokerObject metaLoanBrokerObject = ImmutableMetaLoanBrokerObject.builder()
      .previousTransactionId(Hash256.of("7E5F3FB60E1177F8AF8A9EAC7982F27FA5494FDEA871B23B4B149939A5A7A7BB"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(82357607))
      .sequence(UnsignedInteger.valueOf(5))
      .loanSequence(UnsignedInteger.valueOf(3))
      .ownerNode("0000000000000000")
      .ownerCount(UnsignedInteger.valueOf(2))
      .vaultNode("0000000000000000")
      .vaultId(Hash256.of("D70384C6A81A5375B1DF840FAD6E7B5672780BC1583CEAB7B2247B8D456B28CB"))
      .account(Address.of("rE54zDvgnghAoPopCgvtiqWNq3dU5y836S"))
      .owner(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .data(LoanBrokerData.of("010203"))
      .managementFeeRate(UnsignedInteger.valueOf(10000))
      .debtTotal(AssetAmount.of("500000"))
      .debtMaximum(AssetAmount.of("5000000"))
      .coverAvailable(AssetAmount.of("100000"))
      .coverRateMinimum(UnsignedInteger.valueOf(50000))
      .coverRateLiquidation(UnsignedInteger.valueOf(25000))
      .build();

    String json = "{" +
      "  \"Flags\": 0," +
      "  \"PreviousTxnID\": \"7E5F3FB60E1177F8AF8A9EAC7982F27FA5494FDEA871B23B4B149939A5A7A7BB\"," +
      "  \"PreviousTxnLgrSeq\": 82357607," +
      "  \"Sequence\": 5," +
      "  \"LoanSequence\": 3," +
      "  \"OwnerNode\": \"0000000000000000\"," +
      "  \"OwnerCount\": 2," +
      "  \"VaultNode\": \"0000000000000000\"," +
      "  \"VaultID\": \"D70384C6A81A5375B1DF840FAD6E7B5672780BC1583CEAB7B2247B8D456B28CB\"," +
      "  \"Account\": \"rE54zDvgnghAoPopCgvtiqWNq3dU5y836S\"," +
      "  \"Owner\": \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\"," +
      "  \"Data\": \"010203\"," +
      "  \"ManagementFeeRate\": 10000," +
      "  \"DebtTotal\": \"500000\"," +
      "  \"DebtMaximum\": \"5000000\"," +
      "  \"CoverAvailable\": \"100000\"," +
      "  \"CoverRateMinimum\": 50000," +
      "  \"CoverRateLiquidation\": 25000" +
      "}";

    assertCanSerializeAndDeserialize(metaLoanBrokerObject, json, MetaLoanBrokerObject.class);
  }

  @Test
  void testMetaLoanBrokerObjectWithMinimalFields() throws JsonProcessingException, JSONException {
    MetaLoanBrokerObject metaLoanBrokerObject = ImmutableMetaLoanBrokerObject.builder()
      .debtTotal(AssetAmount.of("250000"))
      .coverAvailable(AssetAmount.of("50000"))
      .build();

    String json = "{" +
      "  \"Flags\": 0," +
      "  \"DebtTotal\": \"250000\"," +
      "  \"CoverAvailable\": \"50000\"" +
      "}";

    assertCanSerializeAndDeserialize(metaLoanBrokerObject, json, MetaLoanBrokerObject.class);
  }
}
