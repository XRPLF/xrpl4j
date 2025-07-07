package org.xrpl.xrpl4j.model.ledger;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.AssetScale;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.MpTokenMetadata;
import org.xrpl.xrpl4j.model.transactions.MpTokenNumericAmount;
import org.xrpl.xrpl4j.model.transactions.TransferFee;

class MpTokenIssuanceObjectTest extends AbstractJsonTest {

  @Test
  void testJson() throws JSONException, JsonProcessingException {
    MpTokenIssuanceObject object = MpTokenIssuanceObject.builder()
      .assetScale(AssetScale.of(UnsignedInteger.valueOf(2)))
      .flags(MpTokenIssuanceFlags.of(122))
      .issuer(Address.of("rJo2Wu7dymuFaL3QgYaEwgAEN3VcgN8e8c"))
      .mpTokenMetadata(MpTokenMetadata.of("ABCD"))
      .maximumAmount(MpTokenNumericAmount.of(9223372036854775807L))
      .outstandingAmount(MpTokenNumericAmount.of(0))
      .ownerNode("0")
      .previousTransactionId(Hash256.of("8C20A85CE9EA44CEF32C8B06209890154D8810A8409D8582884566CD24DE694F"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(420))
      .sequence(UnsignedInteger.valueOf(377))
      .transferFee(TransferFee.of(UnsignedInteger.valueOf(10)))
      .index(Hash256.of("9295A1CC8C9E8C7CA77C823F2D10B9C599E63707C7A222B306F603D4CF511301"))
      .mpTokenIssuanceId(MpTokenIssuanceId.of("00000179C3493FFEB0869853DDEC0705800595424710FA7A"))
      .build();

    String json = "{\n" +
                  "  \"AssetScale\" : 2,\n" +
                  "  \"Flags\" : 122,\n" +
                  "  \"Issuer\" : \"rJo2Wu7dymuFaL3QgYaEwgAEN3VcgN8e8c\",\n" +
                  "  \"LedgerEntryType\" : \"MPTokenIssuance\",\n" +
                  "  \"MPTokenMetadata\" : \"ABCD\",\n" +
                  "  \"MaximumAmount\" : \"9223372036854775807\",\n" +
                  "  \"OutstandingAmount\" : \"0\",\n" +
                  "  \"OwnerNode\" : \"0\",\n" +
                  "  \"PreviousTxnID\" : \"8C20A85CE9EA44CEF32C8B06209890154D8810A8409D8582884566CD24DE694F\",\n" +
                  "  \"PreviousTxnLgrSeq\" : 420,\n" +
                  "  \"Sequence\" : 377,\n" +
                  "  \"TransferFee\" : 10,\n" +
                  "  \"index\" : \"9295A1CC8C9E8C7CA77C823F2D10B9C599E63707C7A222B306F603D4CF511301\",\n" +
                  "  \"mpt_issuance_id\" : \"00000179C3493FFEB0869853DDEC0705800595424710FA7A\"\n" +
                  "}";

    assertCanSerializeAndDeserialize(object, json);
  }

  @Test
  void testTransferFeeAndAssetScaleDefaultToZero() {
    MpTokenIssuanceObject object = MpTokenIssuanceObject.builder()
      .flags(MpTokenIssuanceFlags.of(122))
      .issuer(Address.of("rJo2Wu7dymuFaL3QgYaEwgAEN3VcgN8e8c"))
      .mpTokenMetadata(MpTokenMetadata.of("ABCD"))
      .maximumAmount(MpTokenNumericAmount.of(9223372036854775807L))
      .outstandingAmount(MpTokenNumericAmount.of(0))
      .ownerNode("0")
      .previousTransactionId(Hash256.of("8C20A85CE9EA44CEF32C8B06209890154D8810A8409D8582884566CD24DE694F"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(420))
      .sequence(UnsignedInteger.valueOf(377))
      .index(Hash256.of("9295A1CC8C9E8C7CA77C823F2D10B9C599E63707C7A222B306F603D4CF511301"))
      .mpTokenIssuanceId(MpTokenIssuanceId.of("00000179C3493FFEB0869853DDEC0705800595424710FA7A"))
      .build();

    assertThat(object.assetScale()).isEqualTo(AssetScale.of(UnsignedInteger.ZERO));
    assertThat(object.transferFee()).isEqualTo(TransferFee.of(UnsignedInteger.ZERO));
  }


}