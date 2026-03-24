package org.xrpl.xrpl4j.model.ledger;

import static org.xrpl.xrpl4j.crypto.TestConstants.HASH_256;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.VaultFlags;
import org.xrpl.xrpl4j.model.ledger.IouIssue;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.AssetAmount;
import org.xrpl.xrpl4j.model.transactions.AssetScale;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.VaultData;
import org.xrpl.xrpl4j.model.transactions.WithdrawalPolicy;

class VaultObjectTest extends AbstractJsonTest {

  @Test
  void testJsonWithAllFields() throws JSONException, JsonProcessingException {
    VaultObject vault = VaultObject.builder()
      .previousTransactionId(Hash256.of("0000000000000000000000000000000000000000000000000000000000000010"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(100))
      .sequence(UnsignedInteger.valueOf(5))
      .ownerNode("0")
      .owner(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .account(Address.of("rE54zDvgnghAoPopCgvtiqWNq3dU5y836S"))
      .data(VaultData.of("48656C6C6F"))
      .asset(
        IouIssue.builder()
          .currency("USD")
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .build()
      )
      .assetsTotal(AssetAmount.of("500000"))
      .assetsAvailable(AssetAmount.of("400000"))
      .assetsMaximum(AssetAmount.of("1000000"))
      .lossUnrealized(AssetAmount.of("100"))
      .shareMptId(MpTokenIssuanceId.of("00000005E54ZDVGNGHAOPOPCGVTIQWNQ3DU5Y836"))
      .withdrawalPolicy(WithdrawalPolicy.FIRST_COME_FIRST_SERVE)
      .scale(AssetScale.of(UnsignedInteger.valueOf(8)))
      .flags(VaultFlags.VAULT_PRIVATE)
      .index(HASH_256)
      .build();

    String json = String.format("{\n" +
      "    \"LedgerEntryType\" : \"Vault\",\n" +
      "    \"Flags\" : 65536,\n" +
      "    \"PreviousTxnID\" : \"0000000000000000000000000000000000000000000000000000000000000010\",\n" +
      "    \"PreviousTxnLgrSeq\" : 100,\n" +
      "    \"Sequence\" : 5,\n" +
      "    \"OwnerNode\" : \"0\",\n" +
      "    \"Owner\" : \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\",\n" +
      "    \"Account\" : \"rE54zDvgnghAoPopCgvtiqWNq3dU5y836S\",\n" +
      "    \"Data\" : \"48656C6C6F\",\n" +
      "    \"Asset\" : {\n" +
      "        \"currency\" : \"USD\",\n" +
      "        \"issuer\" : \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\"\n" +
      "    },\n" +
      "    \"AssetsTotal\" : \"500000\",\n" +
      "    \"AssetsAvailable\" : \"400000\",\n" +
      "    \"AssetsMaximum\" : \"1000000\",\n" +
      "    \"LossUnrealized\" : \"100\",\n" +
      "    \"ShareMPTID\" : \"00000005E54ZDVGNGHAOPOPCGVTIQWNQ3DU5Y836\",\n" +
      "    \"WithdrawalPolicy\" : 1,\n" +
      "    \"Scale\" : 8,\n" +
      "    \"index\" : %s\n" +
      "}", HASH_256);

    assertCanSerializeAndDeserialize(vault, json);
  }

  @Test
  void testJsonWithDefaultFields() throws JSONException, JsonProcessingException {
    VaultObject vault = VaultObject.builder()
      .previousTransactionId(Hash256.of("0000000000000000000000000000000000000000000000000000000000000010"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(100))
      .sequence(UnsignedInteger.valueOf(5))
      .ownerNode("0")
      .owner(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .account(Address.of("rE54zDvgnghAoPopCgvtiqWNq3dU5y836S"))
      .asset(Issue.XRP)
      .shareMptId(MpTokenIssuanceId.of("00000005E54ZDVGNGHAOPOPCGVTIQWNQ3DU5Y836"))
      .withdrawalPolicy(WithdrawalPolicy.FIRST_COME_FIRST_SERVE)
      .index(HASH_256)
      .build();

    String json = String.format("{\n" +
      "    \"LedgerEntryType\" : \"Vault\",\n" +
      "    \"Flags\" : 0,\n" +
      "    \"PreviousTxnID\" : \"0000000000000000000000000000000000000000000000000000000000000010\",\n" +
      "    \"PreviousTxnLgrSeq\" : 100,\n" +
      "    \"Sequence\" : 5,\n" +
      "    \"OwnerNode\" : \"0\",\n" +
      "    \"Owner\" : \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\",\n" +
      "    \"Account\" : \"rE54zDvgnghAoPopCgvtiqWNq3dU5y836S\",\n" +
      "    \"Asset\" : {\n" +
      "        \"currency\" : \"XRP\"\n" +
      "    },\n" +
      "    \"AssetsTotal\" : \"0\",\n" +
      "    \"AssetsAvailable\" : \"0\",\n" +
      "    \"AssetsMaximum\" : \"0\",\n" +
      "    \"LossUnrealized\" : \"0\",\n" +
      "    \"ShareMPTID\" : \"00000005E54ZDVGNGHAOPOPCGVTIQWNQ3DU5Y836\",\n" +
      "    \"WithdrawalPolicy\" : 1,\n" +
      "    \"Scale\" : 0,\n" +
      "    \"index\" : %s\n" +
      "}", HASH_256);

    assertCanSerializeAndDeserialize(vault, json);
  }
}
