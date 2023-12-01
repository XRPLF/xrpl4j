package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.XChainBridge;
import org.xrpl.xrpl4j.model.transactions.XChainClaimId;
import org.xrpl.xrpl4j.model.transactions.XChainCount;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

class BridgeObjectTest extends AbstractJsonTest {

  @Test
  void testFullyPopulatedJson() throws JSONException, JsonProcessingException {
    BridgeObject bridge = BridgeObject.builder()
      .account(Address.of("r3nCVTbZGGYoWvZ58BcxDmiMUU7ChMa1eC"))
      .minAccountCreateAmount(XrpCurrencyAmount.ofDrops(2000000000))
      .ownerNode("0")
      .previousTransactionId(Hash256.of("67A8A1B36C1B97BE3AAB6B19CB3A3069034877DE917FD1A71919EAE7548E5636"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(102))
      .signatureReward(XrpCurrencyAmount.ofDrops(204))
      .xChainAccountClaimCount(XChainCount.of(UnsignedLong.ZERO))
      .xChainAccountCreateCount(XChainCount.of(UnsignedLong.ZERO))
      .xChainBridge(
        XChainBridge.builder()
          .issuingChainDoor(Address.of("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"))
          .issuingChainIssue(Issue.XRP)
          .lockingChainDoor(Address.of("r3nCVTbZGGYoWvZ58BcxDmiMUU7ChMa1eC"))
          .lockingChainIssue(Issue.XRP)
          .build()
      )
      .xChainClaimId(XChainClaimId.of(UnsignedLong.ONE))
      .index(Hash256.of("9F2C9E23343852036AFD323025A8506018ABF9D4DBAA746D61BF1CFB5C297D10"))
      .build();

    String json = "\n" +
      "{\n" +
      "  \"Account\": \"r3nCVTbZGGYoWvZ58BcxDmiMUU7ChMa1eC\",\n" +
      "  \"Flags\": 0,\n" +
      "  \"LedgerEntryType\": \"Bridge\",\n" +
      "  \"MinAccountCreateAmount\": \"2000000000\",\n" +
      "  \"OwnerNode\": \"0\",\n" +
      "  \"PreviousTxnID\": \"67A8A1B36C1B97BE3AAB6B19CB3A3069034877DE917FD1A71919EAE7548E5636\",\n" +
      "  \"PreviousTxnLgrSeq\": 102,\n" +
      "  \"SignatureReward\": \"204\",\n" +
      "  \"XChainAccountClaimCount\": \"0\",\n" +
      "  \"XChainAccountCreateCount\": \"0\",\n" +
      "  \"XChainBridge\": {\n" +
      "    \"IssuingChainDoor\": \"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh\",\n" +
      "    \"IssuingChainIssue\": {\n" +
      "      \"currency\": \"XRP\"\n" +
      "    },\n" +
      "    \"LockingChainDoor\": \"r3nCVTbZGGYoWvZ58BcxDmiMUU7ChMa1eC\",\n" +
      "    \"LockingChainIssue\": {\n" +
      "      \"currency\": \"XRP\"\n" +
      "    }\n" +
      "  },\n" +
      "  \"XChainClaimID\": \"1\",\n" +
      "  \"index\": \"9F2C9E23343852036AFD323025A8506018ABF9D4DBAA746D61BF1CFB5C297D10\"\n" +
      "}";

    assertCanSerializeAndDeserialize(bridge, json);
  }

  @Test
  void testJsonWithEmptyAccountCreateAmount() throws JSONException, JsonProcessingException {
    BridgeObject bridge = BridgeObject.builder()
      .account(Address.of("r3nCVTbZGGYoWvZ58BcxDmiMUU7ChMa1eC"))
      .ownerNode("0")
      .previousTransactionId(Hash256.of("67A8A1B36C1B97BE3AAB6B19CB3A3069034877DE917FD1A71919EAE7548E5636"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(102))
      .signatureReward(XrpCurrencyAmount.ofDrops(204))
      .xChainAccountClaimCount(XChainCount.of(UnsignedLong.ZERO))
      .xChainAccountCreateCount(XChainCount.of(UnsignedLong.ZERO))
      .xChainBridge(
        XChainBridge.builder()
          .issuingChainDoor(Address.of("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"))
          .issuingChainIssue(Issue.XRP)
          .lockingChainDoor(Address.of("r3nCVTbZGGYoWvZ58BcxDmiMUU7ChMa1eC"))
          .lockingChainIssue(Issue.XRP)
          .build()
      )
      .xChainClaimId(XChainClaimId.of(UnsignedLong.ONE))
      .index(Hash256.of("9F2C9E23343852036AFD323025A8506018ABF9D4DBAA746D61BF1CFB5C297D10"))
      .build();

    String json = "\n" +
      "{\n" +
      "  \"Account\": \"r3nCVTbZGGYoWvZ58BcxDmiMUU7ChMa1eC\",\n" +
      "  \"Flags\": 0,\n" +
      "  \"LedgerEntryType\": \"Bridge\",\n" +
      "  \"OwnerNode\": \"0\",\n" +
      "  \"PreviousTxnID\": \"67A8A1B36C1B97BE3AAB6B19CB3A3069034877DE917FD1A71919EAE7548E5636\",\n" +
      "  \"PreviousTxnLgrSeq\": 102,\n" +
      "  \"SignatureReward\": \"204\",\n" +
      "  \"XChainAccountClaimCount\": \"0\",\n" +
      "  \"XChainAccountCreateCount\": \"0\",\n" +
      "  \"XChainBridge\": {\n" +
      "    \"IssuingChainDoor\": \"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh\",\n" +
      "    \"IssuingChainIssue\": {\n" +
      "      \"currency\": \"XRP\"\n" +
      "    },\n" +
      "    \"LockingChainDoor\": \"r3nCVTbZGGYoWvZ58BcxDmiMUU7ChMa1eC\",\n" +
      "    \"LockingChainIssue\": {\n" +
      "      \"currency\": \"XRP\"\n" +
      "    }\n" +
      "  },\n" +
      "  \"XChainClaimID\": \"1\",\n" +
      "  \"index\": \"9F2C9E23343852036AFD323025A8506018ABF9D4DBAA746D61BF1CFB5C297D10\"\n" +
      "}";

    assertCanSerializeAndDeserialize(bridge, json);
  }
}