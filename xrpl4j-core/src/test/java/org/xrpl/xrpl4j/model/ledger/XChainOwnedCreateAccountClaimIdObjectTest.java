package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.XChainBridge;
import org.xrpl.xrpl4j.model.transactions.XChainCount;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

class XChainOwnedCreateAccountClaimIdObjectTest extends AbstractJsonTest {

  @Test
  void testJson() throws JSONException, JsonProcessingException {
    XChainOwnedCreateAccountClaimIdObject object = XChainOwnedCreateAccountClaimIdObject.builder()
      .account(Address.of("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"))
      .ownerNode("0")
      .previousTransactionId(Hash256.of("D6451F989A89E58C5E52C081D5C2DB34AE73035588968A6166151113A3B09E9A"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(751272))
      .xChainAccountCreateCount(XChainCount.of(UnsignedLong.valueOf("2cf", 16)))
      .xChainBridge(
        XChainBridge.builder()
          .issuingChainDoor(Address.of("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"))
          .issuingChainIssue(Issue.XRP)
          .lockingChainDoor(Address.of("rnQAXXWoFNN6PEqwqsdTngCtFPCrmfuqFJ"))
          .lockingChainIssue(Issue.XRP)
          .build()
      )
      .addXChainCreateAccountAttestations(
        XChainCreateAccountAttestation.of(
          XChainCreateAccountProofSig.builder()
            .amount(XrpCurrencyAmount.ofDrops(20000000))
            .attestationRewardAccount(Address.of("rDUQ7WUgMZ6V75v3CFb1tqm1XPizmkWtTm"))
            .attestationSignerAccount(Address.of("rUNdUjNcQde1Ye3823hn4RWjBYJEZYye3x"))
            .destination(Address.of("rESSoiapL4EmPZTos6ks9FDZ6pbf261b3g"))
            .publicKey(PublicKey.fromBase16EncodedPublicKey("0300C9F746EF04811BB5529F7E58ACECA6DC5CFD5FDFB42C55C8630FC981D37A4E"))
            .signatureReward(XrpCurrencyAmount.ofDrops(100))
            .wasLockingChainSend(true)
            .build()
        ),
        XChainCreateAccountAttestation.of(
          XChainCreateAccountProofSig.builder()
            .amount(
              IssuedCurrencyAmount.builder()
                .value("1")
                .issuer(Address.of("rESSoiapL4EmPZTos6ks9FDZ6pbf261b3g"))
                .currency("USD")
                .build()
            )
            .attestationRewardAccount(Address.of("rLsS3B2m23Ms4oydi1bzNEp4R4EVxTFMrU"))
            .attestationSignerAccount(Address.of("rJMQeMMRjsKmSwJ4ewMhVMVq3mbxTBwT3a"))
            .destination(Address.of("rESSoiapL4EmPZTos6ks9FDZ6pbf261b3g"))
            .publicKey(PublicKey.fromBase16EncodedPublicKey("02C39C1AD5DBE3702D7D6A4A115618F5A0105EA394A0BD52FFA0C4787C3CB626CD"))
            .signatureReward(XrpCurrencyAmount.ofDrops(100))
            .wasLockingChainSend(false)
            .build()
        )
      )
      .index(Hash256.of("36AF95B8F602D97D3028968FAACEB5343435694990F1A0892BBB81DDCC033141"))
      .build();

    String json = "{\n" +
      "  \"Account\": \"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh\",\n" +
      "  \"Flags\": 0,\n" +
      "  \"OwnerNode\": \"0\",\n" +
      "  \"PreviousTxnID\": \"D6451F989A89E58C5E52C081D5C2DB34AE73035588968A6166151113A3B09E9A\",\n" +
      "  \"PreviousTxnLgrSeq\": 751272,\n" +
      "  \"XChainAccountCreateCount\": \"2cf\",\n" +
      "  \"XChainBridge\": {\n" +
      "    \"IssuingChainDoor\": \"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh\",\n" +
      "    \"IssuingChainIssue\": {\n" +
      "      \"currency\": \"XRP\"\n" +
      "    },\n" +
      "    \"LockingChainDoor\": \"rnQAXXWoFNN6PEqwqsdTngCtFPCrmfuqFJ\",\n" +
      "    \"LockingChainIssue\": {\n" +
      "      \"currency\": \"XRP\"\n" +
      "    }\n" +
      "  },\n" +
      "  \"XChainCreateAccountAttestations\": [\n" +
      "    {\n" +
      "      \"XChainCreateAccountProofSig\": {\n" +
      "        \"Amount\": \"20000000\",\n" +
      "        \"AttestationRewardAccount\": \"rDUQ7WUgMZ6V75v3CFb1tqm1XPizmkWtTm\",\n" +
      "        \"AttestationSignerAccount\": \"rUNdUjNcQde1Ye3823hn4RWjBYJEZYye3x\",\n" +
      "        \"Destination\": \"rESSoiapL4EmPZTos6ks9FDZ6pbf261b3g\",\n" +
      "        \"PublicKey\": \"0300C9F746EF04811BB5529F7E58ACECA6DC5CFD5FDFB42C55C8630FC981D37A4E\",\n" +
      "        \"SignatureReward\": \"100\",\n" +
      "        \"WasLockingChainSend\": 1\n" +
      "      }\n" +
      "    },\n" +
      "    {\n" +
      "      \"XChainCreateAccountProofSig\": {\n" +
      "        \"Amount\": {\n" +
      "           \"currency\": \"USD\"," +
      "           \"issuer\": \"rESSoiapL4EmPZTos6ks9FDZ6pbf261b3g\"," +
      "           \"value\": \"1\"" +
      "        },\n" +
      "        \"AttestationRewardAccount\": \"rLsS3B2m23Ms4oydi1bzNEp4R4EVxTFMrU\",\n" +
      "        \"AttestationSignerAccount\": \"rJMQeMMRjsKmSwJ4ewMhVMVq3mbxTBwT3a\",\n" +
      "        \"Destination\": \"rESSoiapL4EmPZTos6ks9FDZ6pbf261b3g\",\n" +
      "        \"PublicKey\": \"02C39C1AD5DBE3702D7D6A4A115618F5A0105EA394A0BD52FFA0C4787C3CB626CD\",\n" +
      "        \"SignatureReward\": \"100\",\n" +
      "        \"WasLockingChainSend\": 0\n" +
      "      }\n" +
      "    }\n" +
      "  ]," +
      "  \"LedgerEntryType\": \"XChainOwnedCreateAccountClaimID\",\n" +
      "  \"index\": \"36AF95B8F602D97D3028968FAACEB5343435694990F1A0892BBB81DDCC033141\"\n" +
      "}";

    assertCanSerializeAndDeserialize(object, json);
  }
}