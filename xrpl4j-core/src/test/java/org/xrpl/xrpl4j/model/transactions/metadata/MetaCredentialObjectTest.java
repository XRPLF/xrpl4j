package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.CredentialFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CredentialType;
import org.xrpl.xrpl4j.model.transactions.CredentialUri;
import org.xrpl.xrpl4j.model.transactions.Hash256;

class MetaCredentialObjectTest extends AbstractJsonTest {

  @Test
  public void testMetaCredentialObject() throws JsonProcessingException, JSONException {
    MetaCredentialObject object = ImmutableMetaCredentialObject.builder()
      .subject(Address.of("rB3JmRd5m292YjCsCr65tc8dwZz2WN7HQu"))
      .issuer(Address.of("rB3JmRd5m292YjCsCr65tc8dwZz2WN7HQa"))
      .flags(CredentialFlags.ACCEPTED)
      .subjectNode("0")
      .issuerNode("1")
      .uri(CredentialUri.ofPlainText("test-uri"))
      .credentialType(CredentialType.ofPlainText("test-type"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(39480038))
      .previousTxnId(Hash256.of("78D3B7A4B07BFC1F5D7EBD9844B25209F3D5885F347EBA0868FEF2672A91F9DF"))
      .expiration(UnsignedInteger.valueOf(10))
      .build();

    String json = "{" +
      "  \"Subject\": \"rB3JmRd5m292YjCsCr65tc8dwZz2WN7HQu\"," +
      "  \"Issuer\": \"rB3JmRd5m292YjCsCr65tc8dwZz2WN7HQa\"," +
      "  \"Flags\": 65536," +
      "  \"SubjectNode\": \"0\"," +
      "  \"IssuerNode\": \"1\"," +
      "  \"URI\": \"746573742D757269\"," +
      "  \"CredentialType\": \"746573742D74797065\"," +
      "  \"Expiration\": 10," +
      "  \"PreviousTxnID\": \"78D3B7A4B07BFC1F5D7EBD9844B25209F3D5885F347EBA0868FEF2672A91F9DF\"," +
      "  \"PreviousTxnLgrSeq\": 39480038" +
      "}";

    assertCanSerializeAndDeserialize(object, json, MetaCredentialObject.class);
  }

}