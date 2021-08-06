package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Marker;

public class AccountLinesRequestParamsJsonTests extends AbstractJsonTest {

  public static final Hash256 HASH_256 = Hash256.of("92FA6A9FC8EA6018D5D16532D7795C91BFB0831355BDFDA177E86C8BF997985F");

  @Test
  @Deprecated
  public void oldLedgerIndexStillWorks() throws JsonProcessingException, JSONException {
    AccountLinesRequestParams params = AccountLinesRequestParams.builder()
      .account(Address.of("rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn"))
      .ledgerIndex(LedgerIndex.VALIDATED)
      .build();

    String json = "{\n" +
      "            \"account\": \"rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn\",\n" +
      "            \"ledger_index\": \"validated\"\n" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  @Deprecated
  public void oldNumericalLedgerIndexStillWorks() throws JsonProcessingException, JSONException {
    AccountLinesRequestParams params = AccountLinesRequestParams.builder()
      .account(Address.of("rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn"))
      .ledgerIndex(LedgerIndex.of(UnsignedLong.ONE))
      .build();

    String json = "{\n" +
      "            \"account\": \"rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn\",\n" +
      "            \"ledger_index\": 1\n" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  @Deprecated
  public void oldNLedgerHashStillWorks() throws JsonProcessingException, JSONException {
    AccountLinesRequestParams params = AccountLinesRequestParams.builder()
      .account(Address.of("rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn"))
      .ledgerHash(HASH_256)
      .build();

    String json = "{\n" +
      "            \"account\": \"rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn\",\n" +
      "            \"ledger_hash\": \"" + HASH_256 + "\"\n" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testWithLedgerIndex() throws JsonProcessingException, JSONException {
    AccountLinesRequestParams params = AccountLinesRequestParams.builder()
      .account(Address.of("rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn"))
      .ledgerSpecifier(LedgerSpecifier.of(LedgerIndex.of(UnsignedInteger.ONE)))
      .build();

    String json = "{\n" +
      "            \"account\": \"rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn\",\n" +
      "            \"ledger_index\": 1\n" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testMinimalJson() throws JsonProcessingException, JSONException {

    AccountLinesRequestParams params = AccountLinesRequestParams.builder()
      .account(Address.of("rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn"))
      .build();

    String json = "{\n" +
      "            \"account\": \"rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn\",\n" +
      "            \"ledger_index\": \"current\"\n" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testFullJson() throws JsonProcessingException, JSONException {

    AccountLinesRequestParams params = AccountLinesRequestParams.builder()
        .account(Address.of("rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn"))
        .ledgerSpecifier(LedgerSpecifier.of(HASH_256))
        .peer(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
        .limit(UnsignedInteger.ONE)
        .marker(Marker.of("marker"))
        .build();

    String json = "{\n" +
        "            \"account\": \"rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn\",\n" +
        "            \"ledger_hash\": \"" + HASH_256 + "\",\n" +
        "            \"peer\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
        "            \"limit\": 1,\n" +
        "            \"marker\": \"marker\"\n" +
        "        }";

    assertCanSerializeAndDeserialize(params, json);
  }
}
