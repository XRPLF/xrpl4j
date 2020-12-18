package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;

public class SignerListObjectJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    SignerListObject object = SignerListObject.builder()
        .flags(Flags.SignerListFlags.UNSET)
        .ownerNode("0000000000000000")
        .previousTransactionId(Hash256.of("5904C0DC72C58A83AEFED2FFC5386356AA83FCA6A88C89D00646E51E687CDBE4"))
        .previousTransactionLedgerSequence(UnsignedInteger.valueOf(16061435))
        .addSignerEntries(
            SignerEntryWrapper.of(
                SignerEntry.builder()
                    .account(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
                    .signerWeight(UnsignedInteger.valueOf(2))
                    .build()
            ),
            SignerEntryWrapper.of(
                SignerEntry.builder()
                    .account(Address.of("raKEEVSGnKSD9Zyvxu4z6Pqpm4ABH8FS6n"))
                    .signerWeight(UnsignedInteger.valueOf(1))
                    .build()
            ),
            SignerEntryWrapper.of(
                SignerEntry.builder()
                    .account(Address.of("rUpy3eEg8rqjqfUoLeBnZkscbKbFsKXC3v"))
                    .signerWeight(UnsignedInteger.valueOf(1))
                    .build()
            )
        )
        .signerListId(UnsignedInteger.ZERO)
        .signerQuorum(UnsignedInteger.valueOf(3))
        .index(Hash256.of("A9C28A28B85CD533217F5C0A0C7767666B093FA58A0F2D80026FCC4CD932DDC7"))
        .build();

    String json = "{\n" +
        "    \"Flags\": 0,\n" +
        "    \"LedgerEntryType\": \"SignerList\",\n" +
        "    \"OwnerNode\": \"0000000000000000\",\n" +
        "    \"PreviousTxnID\": \"5904C0DC72C58A83AEFED2FFC5386356AA83FCA6A88C89D00646E51E687CDBE4\",\n" +
        "    \"PreviousTxnLgrSeq\": 16061435,\n" +
        "    \"SignerEntries\": [\n" +
        "        {\n" +
        "            \"SignerEntry\": {\n" +
        "                \"Account\": \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\",\n" +
        "                \"SignerWeight\": 2\n" +
        "            }\n" +
        "        },\n" +
        "        {\n" +
        "            \"SignerEntry\": {\n" +
        "                \"Account\": \"raKEEVSGnKSD9Zyvxu4z6Pqpm4ABH8FS6n\",\n" +
        "                \"SignerWeight\": 1\n" +
        "            }\n" +
        "        },\n" +
        "        {\n" +
        "            \"SignerEntry\": {\n" +
        "                \"Account\": \"rUpy3eEg8rqjqfUoLeBnZkscbKbFsKXC3v\",\n" +
        "                \"SignerWeight\": 1\n" +
        "            }\n" +
        "        }\n" +
        "    ],\n" +
        "    \"SignerListID\": 0,\n" +
        "    \"SignerQuorum\": 3,\n" +
        "    \"index\": \"A9C28A28B85CD533217F5C0A0C7767666B093FA58A0F2D80026FCC4CD932DDC7\"\n" +
        "}";

    assertCanSerializeAndDeserialize(object, json);
  }
}
