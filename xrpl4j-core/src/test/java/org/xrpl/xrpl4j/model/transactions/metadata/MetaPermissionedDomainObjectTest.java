package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CredentialType;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class MetaPermissionedDomainObjectTest extends AbstractJsonTest {

  @Test
  public void testMetaDepositPreAuthObjectTest() throws JsonProcessingException, JSONException {
    List<MetaCredentialWrapper> acceptedCredentials = IntStream.range(0, 10)
      .mapToObj(i -> ImmutableMetaCredentialWrapper.builder()
        .credential(ImmutableMetaCredential.builder()
          .issuer(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
          .credentialType(CredentialType.ofPlainText("Driver licence - " + i))
          .build())
        .build())
      .collect(Collectors.toList());

    MetaPermissionedDomainObject metaPermissionedDomainObject = ImmutableMetaPermissionedDomainObject.builder()
      .owner(Address.of("rPLmuwXJUEtGJ6b4wtqpjYRtxSv5tDVLUz"))
      .ownerNode("0")
      .sequence(UnsignedInteger.valueOf(10))
      .acceptedCredentials(acceptedCredentials)
      .previousTxnId(Hash256.of("FC7C6F49B7264CD1984C58E68B2F30B6580AE5EC94D215737E266C1404E3DEFF"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(3105995))
      .build();

    String json = "{" +
      "  \"Owner\": \"rPLmuwXJUEtGJ6b4wtqpjYRtxSv5tDVLUz\"," +
      "  \"Flags\": 0," +
      "  \"OwnerNode\": \"0\"," +
      "  \"PreviousTxnID\": \"FC7C6F49B7264CD1984C58E68B2F30B6580AE5EC94D215737E266C1404E3DEFF\"," +
      "  \"PreviousTxnLgrSeq\": 3105995," +
      "  \"Sequence\": 10," +
      "  \"AcceptedCredentials\":" + objectMapper.writeValueAsString(acceptedCredentials) +
      "}";

    assertCanSerializeAndDeserialize(metaPermissionedDomainObject, json, MetaPermissionedDomainObject.class);
  }
}