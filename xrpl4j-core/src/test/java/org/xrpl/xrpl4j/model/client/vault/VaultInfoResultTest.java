package org.xrpl.xrpl4j.model.client.vault;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.xrpl.xrpl4j.crypto.TestConstants.HASH_256;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceFlags;
import org.xrpl.xrpl4j.model.flags.VaultFlags;
import org.xrpl.xrpl4j.model.ledger.Issue;
import org.xrpl.xrpl4j.model.ledger.VaultObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.AssetAmount;
import org.xrpl.xrpl4j.model.transactions.AssetScale;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.MpTokenNumericAmount;
import org.xrpl.xrpl4j.model.transactions.WithdrawalPolicy;

class VaultInfoResultTest extends AbstractJsonTest {

  @Test
  void testJson() throws JSONException, JsonProcessingException {
    VaultInfoResult result = VaultInfoResult.builder()
      .vault(
        VaultObject.builder()
          .previousTransactionId(Hash256.of("0000000000000000000000000000000000000000000000000000000000000010"))
          .previousTransactionLedgerSequence(UnsignedInteger.valueOf(100))
          .sequence(UnsignedInteger.valueOf(5))
          .ownerNode("0")
          .owner(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
          .account(Address.of("rE54zDvgnghAoPopCgvtiqWNq3dU5y836S"))
          .asset(Issue.XRP)
          .assetsTotal(AssetAmount.of("1000000"))
          .assetsAvailable(AssetAmount.of("800000"))
          .assetsMaximum(AssetAmount.of("0"))
          .lossUnrealized(AssetAmount.of("0"))
          .shareMptId(MpTokenIssuanceId.of("00000005E54ZDVGNGHAOPOPCGVTIQWNQ3DU5Y836"))
          .withdrawalPolicy(WithdrawalPolicy.FIRST_COME_FIRST_SERVE)
          .scale(AssetScale.of(UnsignedInteger.valueOf(6)))
          .shares(
            VaultInfoShares.builder()
              .issuer(Address.of("rE54zDvgnghAoPopCgvtiqWNq3dU5y836S"))
              .outstandingAmount(MpTokenNumericAmount.of(1000000))
              .previousTransactionId(Hash256.of("0000000000000000000000000000000000000000000000000000000000000010"))
              .previousTransactionLedgerSequence(UnsignedInteger.valueOf(100))
              .sequence(UnsignedInteger.valueOf(5))
              .index(Hash256.of("0000000000000000000000000000000000000000000000000000000000000020"))
              .build()
          )
          .index(HASH_256)
          .build()
      )
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(50)))
      .build();

    String json = "{\n" +
      "    \"vault\" : {\n" +
      "        \"LedgerEntryType\" : \"Vault\",\n" +
      "        \"Flags\" : 0,\n" +
      "        \"PreviousTxnID\" : \"0000000000000000000000000000000000000000000000000000000000000010\",\n" +
      "        \"PreviousTxnLgrSeq\" : 100,\n" +
      "        \"Sequence\" : 5,\n" +
      "        \"OwnerNode\" : \"0\",\n" +
      "        \"Owner\" : \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\",\n" +
      "        \"Account\" : \"rE54zDvgnghAoPopCgvtiqWNq3dU5y836S\",\n" +
      "        \"Asset\" : {\n" +
      "            \"currency\" : \"XRP\"\n" +
      "        },\n" +
      "        \"AssetsTotal\" : \"1000000\",\n" +
      "        \"AssetsAvailable\" : \"800000\",\n" +
      "        \"AssetsMaximum\" : \"0\",\n" +
      "        \"LossUnrealized\" : \"0\",\n" +
      "        \"ShareMPTID\" : \"00000005E54ZDVGNGHAOPOPCGVTIQWNQ3DU5Y836\",\n" +
      "        \"WithdrawalPolicy\" : 1,\n" +
      "        \"Scale\" : 6,\n" +
      "        \"shares\" : {\n" +
      "            \"Flags\" : 0,\n" +
      "            \"Issuer\" : \"rE54zDvgnghAoPopCgvtiqWNq3dU5y836S\",\n" +
      "            \"LedgerEntryType\" : \"MPTokenIssuance\",\n" +
      "            \"OutstandingAmount\" : \"1000000\",\n" +
      "            \"PreviousTxnID\" : \"0000000000000000000000000000000000000000000000000000000000000010\",\n" +
      "            \"PreviousTxnLgrSeq\" : 100,\n" +
      "            \"Sequence\" : 5,\n" +
      "            \"index\" : \"0000000000000000000000000000000000000000000000000000000000000020\"\n" +
      "        },\n" +
      "        \"index\" : " + HASH_256 + "\n" +
      "    },\n" +
      "    \"ledger_current_index\" : 50,\n" +
      "    \"validated\" : false\n" +
      "}";

    assertCanSerializeAndDeserialize(result, json);
  }

  @Test
  void ledgerIndexSafeThrowsWhenEmpty() {
    VaultInfoResult result = VaultInfoResult.builder()
      .vault(buildMinimalVault())
      .build();

    assertThatThrownBy(result::ledgerIndexSafe)
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Result did not contain a ledgerIndex.");
  }

  @Test
  void ledgerCurrentIndexSafeThrowsWhenEmpty() {
    VaultInfoResult result = VaultInfoResult.builder()
      .vault(buildMinimalVault())
      .build();

    assertThatThrownBy(result::ledgerCurrentIndexSafe)
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Result did not contain a ledgerCurrentIndex.");
  }

  @Test
  void ledgerHashSafeThrowsWhenEmpty() {
    VaultInfoResult result = VaultInfoResult.builder()
      .vault(buildMinimalVault())
      .build();

    assertThatThrownBy(result::ledgerHashSafe)
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Result did not contain a ledgerHash.");
  }

  private VaultObject buildMinimalVault() {
    return VaultObject.builder()
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
  }
}
