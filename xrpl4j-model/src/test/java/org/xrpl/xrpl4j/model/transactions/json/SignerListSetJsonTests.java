package org.xrpl.xrpl4j.model.transactions.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.Test;
import org.xrpl.xrpl4j.model.ledger.SignerEntry;
import org.xrpl.xrpl4j.model.ledger.SignerEntryWrapper;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.SignerListSet;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class SignerListSetJsonTests extends AbstractJsonTest {

  @Test
  public void testSignerListSetJson() throws JsonProcessingException, JSONException {
    SignerListSet signerListSet = SignerListSet.builder()
        .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
        .fee(XrpCurrencyAmount.ofDrops(12))
        .sequence(UnsignedInteger.ONE)
        .signerQuorum(UnsignedInteger.valueOf(3))
        .addSignerEntries(
            SignerEntryWrapper.of(
                SignerEntry.builder()
                    .account(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
                    .signerWeight(UnsignedInteger.valueOf(2))
                    .build()
            ),
            SignerEntryWrapper.of(
                SignerEntry.builder()
                    .account(Address.of("rUpy3eEg8rqjqfUoLeBnZkscbKbFsKXC3v"))
                    .signerWeight(UnsignedInteger.ONE)
                    .build()
            ),
            SignerEntryWrapper.of(
                SignerEntry.builder()
                    .account(Address.of("raKEEVSGnKSD9Zyvxu4z6Pqpm4ABH8FS6n"))
                    .signerWeight(UnsignedInteger.ONE)
                    .build()
            )
        )
        .build();

    String json = "{\n" +
        "    \"Flags\": 2147483648,\n" +
        "    \"Sequence\": 1,\n" +
        "    \"TransactionType\": \"SignerListSet\",\n" +
        "    \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
        "    \"Fee\": \"12\",\n" +
        "    \"SignerQuorum\": 3,\n" +
        "    \"SignerEntries\": [\n" +
        "        {\n" +
        "            \"SignerEntry\": {\n" +
        "                \"Account\": \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\",\n" +
        "                \"SignerWeight\": 2\n" +
        "            }\n" +
        "        },\n" +
        "        {\n" +
        "            \"SignerEntry\": {\n" +
        "                \"Account\": \"rUpy3eEg8rqjqfUoLeBnZkscbKbFsKXC3v\",\n" +
        "                \"SignerWeight\": 1\n" +
        "            }\n" +
        "        },\n" +
        "        {\n" +
        "            \"SignerEntry\": {\n" +
        "                \"Account\": \"raKEEVSGnKSD9Zyvxu4z6Pqpm4ABH8FS6n\",\n" +
        "                \"SignerWeight\": 1\n" +
        "            }\n" +
        "        }\n" +
        "    ]\n" +
        "}";

    assertCanSerializeAndDeserialize(signerListSet, json);
  }

  @Test
  public void testSignerListSetForDeleteJson() throws JsonProcessingException, JSONException {
    SignerListSet signerListSet = SignerListSet.builder()
        .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
        .fee(XrpCurrencyAmount.ofDrops(12))
        .sequence(UnsignedInteger.ONE)
        .signerQuorum(UnsignedInteger.ZERO)
        .build();

    String json = "{\n" +
        "    \"Flags\": 2147483648,\n" +
        "    \"Sequence\": 1,\n" +
        "    \"TransactionType\": \"SignerListSet\",\n" +
        "    \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
        "    \"Fee\": \"12\",\n" +
        "    \"SignerQuorum\": 0\n" +
        "}";

    assertCanSerializeAndDeserialize(signerListSet, json);
  }
}
