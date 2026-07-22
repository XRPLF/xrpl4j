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
import org.xrpl.xrpl4j.model.transactions.XChainClaimId;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

class XChainOwnedClaimIdObjectTest extends AbstractJsonTest {

  @Test
  void testJson() throws JSONException, JsonProcessingException {
    XChainOwnedClaimIdObject object = XChainOwnedClaimIdObject.builder()
      .account(Address.of("rBW1U7J9mEhEdk6dMHEFUjqQ7HW7WpaEMi"))
      .otherChainSource(Address.of("r9oXrvBX5aDoyMGkoYvzazxDhYoWFUjz8p"))
      .ownerNode("0")
      .previousTransactionId(Hash256.of("1CFD80E9CF232B8EED62A52857DE97438D12230C06496932A81DEFA6E66070A6"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(58673))
      .signatureReward(XrpCurrencyAmount.ofDrops(100))
      .xChainBridge(
        XChainBridge.builder()
          .issuingChainDoor(Address.of("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"))
          .issuingChainIssue(Issue.XRP)
          .lockingChainDoor(Address.of("rMAXACCrp3Y8PpswXcg3bKggHX76V3F8M4"))
          .lockingChainIssue(Issue.XRP)
          .build()
      )
      .addXChainClaimAttestations(
        XChainClaimAttestation.of(
          XChainClaimProofSig.builder()
            .amount(XrpCurrencyAmount.ofDrops(1000000))
            .attestationRewardAccount(Address.of("rfgjrgEJGDxfUY2U8VEDs7BnB1jiH3ofu6"))
            .attestationSignerAccount(Address.of("rfsxNxZ6xB1nTPhTMwQajNnkCxWG8B714n"))
            .destination(Address.of("rBW1U7J9mEhEdk6dMHEFUjqQ7HW7WpaEMi"))
            .publicKey(
              PublicKey.fromBase16EncodedPublicKey("025CA526EF20567A50FEC504589F949E0E3401C13EF76DD5FD1CC2850FA485BD7B")
            )
            .wasLockingChainSend(true)
            .build()
        ),
        XChainClaimAttestation.of(
          XChainClaimProofSig.builder()
            .amount(
              IssuedCurrencyAmount.builder()
                .value("1")
                .issuer(Address.of("rESSoiapL4EmPZTos6ks9FDZ6pbf261b3g"))
                .currency("USD")
                .build()
            )
            .attestationRewardAccount(Address.of("rUUL1tP523M8KimERqVS7sxb1tLLmpndyv"))
            .attestationSignerAccount(Address.of("rEg5sHxZVTNwRL3BAdMwJatkmWDzHMmzDF"))
            .publicKey(
              PublicKey.fromBase16EncodedPublicKey("03D40434A6843638681E2F215310EBC4131AFB12EA85985DA073183B732525F7C9")
            )
            .wasLockingChainSend(false)
            .build()
        )
      )
      .xChainClaimId(XChainClaimId.of(UnsignedLong.valueOf("b5", 16)))
      .index(Hash256.of("20B136D7BF6D2E3D610E28E3E6BE09F5C8F4F0241BBF6E2D072AE1BACB1388F5"))
      .build();

    String json = "\n" +
      "{\n" +
      "  \"Account\": \"rBW1U7J9mEhEdk6dMHEFUjqQ7HW7WpaEMi\",\n" +
      "  \"Flags\": 0,\n" +
      "  \"OtherChainSource\": \"r9oXrvBX5aDoyMGkoYvzazxDhYoWFUjz8p\",\n" +
      "  \"OwnerNode\": \"0\",\n" +
      "  \"PreviousTxnID\": \"1CFD80E9CF232B8EED62A52857DE97438D12230C06496932A81DEFA6E66070A6\",\n" +
      "  \"PreviousTxnLgrSeq\": 58673,\n" +
      "  \"SignatureReward\": \"100\",\n" +
      "  \"XChainBridge\": {\n" +
      "    \"IssuingChainDoor\": \"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh\",\n" +
      "    \"IssuingChainIssue\": {\n" +
      "      \"currency\": \"XRP\"\n" +
      "    },\n" +
      "    \"LockingChainDoor\": \"rMAXACCrp3Y8PpswXcg3bKggHX76V3F8M4\",\n" +
      "    \"LockingChainIssue\": {\n" +
      "      \"currency\": \"XRP\"\n" +
      "    }\n" +
      "  },\n" +
      "  \"XChainClaimAttestations\": [\n" +
      "    {\n" +
      "      \"XChainClaimProofSig\": {\n" +
      "        \"Amount\": \"1000000\",\n" +
      "        \"AttestationRewardAccount\": \"rfgjrgEJGDxfUY2U8VEDs7BnB1jiH3ofu6\",\n" +
      "        \"AttestationSignerAccount\": \"rfsxNxZ6xB1nTPhTMwQajNnkCxWG8B714n\",\n" +
      "        \"Destination\": \"rBW1U7J9mEhEdk6dMHEFUjqQ7HW7WpaEMi\",\n" +
      "        \"PublicKey\": \"025CA526EF20567A50FEC504589F949E0E3401C13EF76DD5FD1CC2850FA485BD7B\",\n" +
      "        \"WasLockingChainSend\": 1\n" +
      "      }\n" +
      "    },\n" +
      "    {\n" +
      "      \"XChainClaimProofSig\": {\n" +
      "        \"Amount\": {\n" +
      "           \"currency\": \"USD\",\n" +
      "           \"issuer\": \"rESSoiapL4EmPZTos6ks9FDZ6pbf261b3g\",\n" +
      "           \"value\": \"1\"\n" +
      "        },\n" +
      "        \"AttestationRewardAccount\": \"rUUL1tP523M8KimERqVS7sxb1tLLmpndyv\",\n" +
      "        \"AttestationSignerAccount\": \"rEg5sHxZVTNwRL3BAdMwJatkmWDzHMmzDF\",\n" +
      "        \"PublicKey\": \"03D40434A6843638681E2F215310EBC4131AFB12EA85985DA073183B732525F7C9\",\n" +
      "        \"WasLockingChainSend\": 0\n" +
      "      }\n" +
      "    }\n" +
      "  ],\n" +
      "  \"XChainClaimID\": \"b5\",\n" +
      "  \"LedgerEntryType\": \"XChainOwnedClaimID\",\n" +
      "  \"index\": \"20B136D7BF6D2E3D610E28E3E6BE09F5C8F4F0241BBF6E2D072AE1BACB1388F5\"\n" +
      "}";

    assertCanSerializeAndDeserialize(object, json);
  }
}