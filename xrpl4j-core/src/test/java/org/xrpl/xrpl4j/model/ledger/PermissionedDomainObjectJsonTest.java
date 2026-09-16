package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Credential;
import org.xrpl.xrpl4j.model.transactions.CredentialType;
import org.xrpl.xrpl4j.model.transactions.CredentialWrapper;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PermissionedDomainObjectJsonTest extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    List<CredentialWrapper> acceptedCredentials = IntStream.range(0, 10)
      .mapToObj(i -> CredentialWrapper.builder()
        .credential(Credential.builder()
          .issuer(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
          .credentialType(CredentialType.ofPlainText("Driver licence - " + i))
          .build())
        .build())
      .collect(Collectors.toList());

    PermissionedDomainObject permissionedDomainObject = PermissionedDomainObject.builder()
      .owner(Address.of("rPLmuwXJUEtGJ6b4wtqpjYRtxSv5tDVLUz"))
      .ownerNode("0")
      .sequence(UnsignedInteger.valueOf(10))
      .acceptedCredentials(acceptedCredentials)
      .previousTxnId(Hash256.of("FC7C6F49B7264CD1984C58E68B2F30B6580AE5EC94D215737E266C1404E3DEFF"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(3105995))
      .index(Hash256.of("5A47EEAD6C185E66C20D4A61BDE8F38181B43250FA37F22EBB604C1F97C8166E"))
      .build();

    String json = "{" +
      "  \"Owner\": \"rPLmuwXJUEtGJ6b4wtqpjYRtxSv5tDVLUz\"," +
      "  \"Flags\": 0," +
      "  \"OwnerNode\": \"0\"," +
      "  \"LedgerEntryType\": \"PermissionedDomain\"," +
      "  \"PreviousTxnID\": \"FC7C6F49B7264CD1984C58E68B2F30B6580AE5EC94D215737E266C1404E3DEFF\"," +
      "  \"PreviousTxnLgrSeq\": 3105995," +
      "  \"Sequence\": 10," +
      "  \"AcceptedCredentials\":" + objectMapper.writeValueAsString(acceptedCredentials) + "," +
      "  \"index\": \"5A47EEAD6C185E66C20D4A61BDE8F38181B43250FA37F22EBB604C1F97C8166E\"" +
      "}";

    assertCanSerializeAndDeserialize(permissionedDomainObject, json);
  }
}
