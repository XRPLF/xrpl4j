package org.xrpl.xrpl4j.model.transactions.metadata;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceFlags;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceImmutableFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.AssetScale;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.MpTokenMetadata;
import org.xrpl.xrpl4j.model.transactions.MpTokenNumericAmount;
import org.xrpl.xrpl4j.model.transactions.TransferFee;

class MetaMpTokenIssuanceObjectTest extends AbstractJsonTest {

  @Test
  void testJsonWithAllFields() throws JsonProcessingException, JSONException {
    MetaMpTokenIssuanceObject object = ImmutableMetaMpTokenIssuanceObject.builder()
      .flags(MpTokenIssuanceFlags.of(66))
      .issuer(Address.of("rJo2Wu7dymuFaL3QgYaEwgAEN3VcgN8e8c"))
      .assetScale(AssetScale.of(UnsignedInteger.valueOf(2)))
      .maximumAmount(MpTokenNumericAmount.of(1000000L))
      .outstandingAmount(MpTokenNumericAmount.of(500L))
      .transferFee(TransferFee.of(UnsignedInteger.valueOf(100)))
      .mpTokenMetadata(MpTokenMetadata.of("DEADBEEF"))
      .domainId(Hash256.of("ABCDEF1234567890ABCDEF1234567890ABCDEF1234567890ABCDEF1234567890"))
      .previousTransactionId(Hash256.of("8C20A85CE9EA44CEF32C8B06209890154D8810A8409D8582884566CD24DE694F"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(420))
      .sequence(UnsignedInteger.valueOf(7))
      .ownerNode("0")
      .referenceHolding(Hash256.of("BCDEF1234567890ABCDEF1234567890ABCDEF1234567890ABCDEF1234567890A"))
      .build();

    String json = "{\n" +
      "  \"Flags\": 66,\n" +
      "  \"Issuer\": \"rJo2Wu7dymuFaL3QgYaEwgAEN3VcgN8e8c\",\n" +
      "  \"AssetScale\": 2,\n" +
      "  \"MaximumAmount\": \"1000000\",\n" +
      "  \"OutstandingAmount\": \"500\",\n" +
      "  \"TransferFee\": 100,\n" +
      "  \"MPTokenMetadata\": \"DEADBEEF\",\n" +
      "  \"DomainID\": \"ABCDEF1234567890ABCDEF1234567890ABCDEF1234567890ABCDEF1234567890\",\n" +
      "  \"PreviousTxnID\": \"8C20A85CE9EA44CEF32C8B06209890154D8810A8409D8582884566CD24DE694F\",\n" +
      "  \"PreviousTxnLgrSeq\": 420,\n" +
      "  \"Sequence\": 7,\n" +
      "  \"OwnerNode\": \"0\",\n" +
      "  \"ReferenceHolding\": \"BCDEF1234567890ABCDEF1234567890ABCDEF1234567890ABCDEF1234567890A\"\n" +
      "}";

    assertCanSerializeAndDeserialize(object, json, MetaMpTokenIssuanceObject.class);
  }

  @Test
  void testJsonWithImmutableFlags() throws JsonProcessingException, JSONException {
    MpTokenIssuanceImmutableFlags lsif = MpTokenIssuanceImmutableFlags.builder()
      .lsifMptCanLock(true)
      .lsifMptMetadata(true)
      .build();

    MetaMpTokenIssuanceObject object = ImmutableMetaMpTokenIssuanceObject.builder()
      .flags(MpTokenIssuanceFlags.of(66))
      .issuer(Address.of("rJo2Wu7dymuFaL3QgYaEwgAEN3VcgN8e8c"))
      .previousTransactionId(Hash256.of("8C20A85CE9EA44CEF32C8B06209890154D8810A8409D8582884566CD24DE694F"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(420))
      .sequence(UnsignedInteger.valueOf(7))
      .ownerNode("0")
      .immutableFlags(lsif)
      .build();

    String json = "{\n" +
      "  \"Flags\": 66,\n" +
      "  \"Issuer\": \"rJo2Wu7dymuFaL3QgYaEwgAEN3VcgN8e8c\",\n" +
      "  \"PreviousTxnID\": \"8C20A85CE9EA44CEF32C8B06209890154D8810A8409D8582884566CD24DE694F\",\n" +
      "  \"PreviousTxnLgrSeq\": 420,\n" +
      "  \"Sequence\": 7,\n" +
      "  \"OwnerNode\": \"0\",\n" +
      "  \"ImmutableFlags\": " + lsif.getValue() + "\n" +
      "}";

    assertCanSerializeAndDeserialize(object, json, MetaMpTokenIssuanceObject.class);
    assertThat(object.immutableFlags()).isPresent().get().isEqualTo(lsif);
  }

  @Test
  void testImmutableFlagsIsOptional() {
    MetaMpTokenIssuanceObject object = ImmutableMetaMpTokenIssuanceObject.builder()
      .flags(MpTokenIssuanceFlags.of(0))
      .issuer(Address.of("rJo2Wu7dymuFaL3QgYaEwgAEN3VcgN8e8c"))
      .previousTransactionId(Hash256.of("8C20A85CE9EA44CEF32C8B06209890154D8810A8409D8582884566CD24DE694F"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(1))
      .sequence(UnsignedInteger.valueOf(1))
      .ownerNode("0")
      .build();

    assertThat(object.immutableFlags()).isEmpty();
  }
}
