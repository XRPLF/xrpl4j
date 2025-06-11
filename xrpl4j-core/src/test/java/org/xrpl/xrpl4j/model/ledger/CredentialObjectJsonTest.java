package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.CredentialFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CredentialType;
import org.xrpl.xrpl4j.model.transactions.Hash256;

public class CredentialObjectJsonTest extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    CredentialObject credentialObject = CredentialObject.builder()
      .credentialType(CredentialType.of("4472697665722773206C6963656E7365"))
      .issuer(Address.of("rPLmuwXJUEtGJ6b4wtqpjYRtxSv5tDVLUz"))
      .subject(Address.of("r9EKPUSDehySNoxqBNuezALVgynRBMNpYi"))
      .issuerNode("0")
      .previousTxnId(Hash256.of("FC7C6F49B7264CD1984C58E68B2F30B6580AE5EC94D215737E266C1404E3DEFF"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(3105995))
      .subjectNode("0")
      .index(Hash256.of("5A47EEAD6C185E66C20D4A61BDE8F38181B43250FA37F22EBB604C1F97C8166E"))
      .flags(CredentialFlags.ACCEPTED)
      .build();

    String json = " {" +
      "  \"CredentialType\": \"4472697665722773206C6963656E7365\"," +
      "  \"Flags\": 65536," +
      "  \"Issuer\": \"rPLmuwXJUEtGJ6b4wtqpjYRtxSv5tDVLUz\"," +
      "  \"IssuerNode\": \"0\"," +
      "  \"LedgerEntryType\": \"Credential\"," +
      "  \"PreviousTxnID\": \"FC7C6F49B7264CD1984C58E68B2F30B6580AE5EC94D215737E266C1404E3DEFF\"," +
      "  \"PreviousTxnLgrSeq\": 3105995," +
      "  \"Subject\": \"r9EKPUSDehySNoxqBNuezALVgynRBMNpYi\"," +
      "  \"SubjectNode\": \"0\"," +
      "  \"index\": \"5A47EEAD6C185E66C20D4A61BDE8F38181B43250FA37F22EBB604C1F97C8166E\"" +
      "}";

    assertCanSerializeAndDeserialize(credentialObject, json);
  }
}
