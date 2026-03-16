package org.xrpl.xrpl4j.model.transactions.metadata;

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

class MetaVaultObjectTest extends AbstractJsonTest {

  @Test
  void testMetaVaultObjectWithAllFields() throws JsonProcessingException, JSONException {
    MetaVaultObject metaVaultObject = ImmutableMetaVaultObject.builder()
      .flags(VaultFlags.VAULT_PRIVATE)
      .previousTransactionId(Hash256.of("7E5F3FB60E1177F8AF8A9EAC7982F27FA5494FDEA871B23B4B149939A5A7A7BB"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(82357607))
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
      .build();

    String json = "{" +
      "  \"Flags\": 65536," +
      "  \"PreviousTxnID\": \"7E5F3FB60E1177F8AF8A9EAC7982F27FA5494FDEA871B23B4B149939A5A7A7BB\"," +
      "  \"PreviousTxnLgrSeq\": 82357607," +
      "  \"Sequence\": 5," +
      "  \"OwnerNode\": \"0\"," +
      "  \"Owner\": \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\"," +
      "  \"Account\": \"rE54zDvgnghAoPopCgvtiqWNq3dU5y836S\"," +
      "  \"Data\": \"48656C6C6F\"," +
      "  \"Asset\": {" +
      "    \"currency\": \"USD\"," +
      "    \"issuer\": \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\"" +
      "  }," +
      "  \"AssetsTotal\": \"500000\"," +
      "  \"AssetsAvailable\": \"400000\"," +
      "  \"AssetsMaximum\": \"1000000\"," +
      "  \"LossUnrealized\": \"100\"," +
      "  \"ShareMPTID\": \"00000005E54ZDVGNGHAOPOPCGVTIQWNQ3DU5Y836\"," +
      "  \"WithdrawalPolicy\": 1," +
      "  \"Scale\": 8" +
      "}";

    assertCanSerializeAndDeserialize(metaVaultObject, json, MetaVaultObject.class);
  }
}