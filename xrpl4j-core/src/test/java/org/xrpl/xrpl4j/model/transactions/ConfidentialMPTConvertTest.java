package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;

class ConfidentialMPTConvertTest extends AbstractJsonTest {

  @Test
  void testJsonWithAllFields() throws JSONException, JsonProcessingException {
    ConfidentialMPTConvert convert = ConfidentialMPTConvert.builder()
      .account(Address.of("rJbVo4xrsGN8o3vLKGXe1s1uW8mAMYHamV"))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .sequence(UnsignedInteger.valueOf(432))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED0C0B9B7D5F80868A701693D7C994385EB4DC661D9E7A2DD95E8199EDC5C211E7")
      )
      .mpTokenIssuanceId(MpTokenIssuanceId.of("00000143A58DCB491FD36A15A7D3172E6A9F088A5478BA41"))
      .mptAmount(MpTokenNumericAmount.of(1000L))
      .holderElGamalPublicKey("0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF")
      .holderEncryptedAmount("AABBCCDD0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF")
      .issuerEncryptedAmount("DDEEFF000123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF")
      .auditorEncryptedAmount("11223344556677889900AABBCCDDEEFF0123456789ABCDEF0123456789ABCDEF0123456789")
      .blindingFactor("0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF")
      .zkProof("0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF01")
      .build();

    String json =
      "{\n" +
      "  \"Account\" : \"rJbVo4xrsGN8o3vLKGXe1s1uW8mAMYHamV\",\n" +
      "  \"TransactionType\" : \"ConfidentialMPTConvert\",\n" +
      "  \"Fee\" : \"15\",\n" +
      "  \"Sequence\" : 432,\n" +
      "  \"SigningPubKey\" : \"ED0C0B9B7D5F80868A701693D7C994385EB4DC661D9E7A2DD95E8199EDC5C211E7\",\n" +
      "  \"MPTokenIssuanceID\" : \"00000143A58DCB491FD36A15A7D3172E6A9F088A5478BA41\",\n" +
      "  \"MPTAmount\" : \"1000\",\n" +
      "  \"HolderElGamalPublicKey\" : \"0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF\",\n" +
      "  \"HolderEncryptedAmount\" : \"AABBCCDD0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF\",\n" +
      "  \"IssuerEncryptedAmount\" : \"DDEEFF000123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF\",\n" +
      "  \"AuditorEncryptedAmount\" : \"11223344556677889900AABBCCDDEEFF0123456789ABCDEF0123456789ABCDEF0123456789\",\n" +
      "  \"BlindingFactor\" : \"0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF\",\n" +
      "  \"ZKProof\" : \"0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF01\"\n" +
      "}";

    assertCanSerializeAndDeserialize(convert, json);
  }

  @Test
  void testJsonWithRequiredFieldsOnly() throws JSONException, JsonProcessingException {
    ConfidentialMPTConvert convert = ConfidentialMPTConvert.builder()
      .account(Address.of("rJbVo4xrsGN8o3vLKGXe1s1uW8mAMYHamV"))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .sequence(UnsignedInteger.valueOf(432))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED0C0B9B7D5F80868A701693D7C994385EB4DC661D9E7A2DD95E8199EDC5C211E7")
      )
      .mpTokenIssuanceId(MpTokenIssuanceId.of("00000143A58DCB491FD36A15A7D3172E6A9F088A5478BA41"))
      .mptAmount(MpTokenNumericAmount.of(500L))
      .holderEncryptedAmount("AABBCCDD0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF")
      .issuerEncryptedAmount("DDEEFF000123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF")
      .blindingFactor("0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF")
      .build();

    String json =
      "{\n" +
      "  \"Account\" : \"rJbVo4xrsGN8o3vLKGXe1s1uW8mAMYHamV\",\n" +
      "  \"TransactionType\" : \"ConfidentialMPTConvert\",\n" +
      "  \"Fee\" : \"15\",\n" +
      "  \"Sequence\" : 432,\n" +
      "  \"SigningPubKey\" : \"ED0C0B9B7D5F80868A701693D7C994385EB4DC661D9E7A2DD95E8199EDC5C211E7\",\n" +
      "  \"MPTokenIssuanceID\" : \"00000143A58DCB491FD36A15A7D3172E6A9F088A5478BA41\",\n" +
      "  \"MPTAmount\" : \"500\",\n" +
      "  \"HolderEncryptedAmount\" : \"AABBCCDD0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF\",\n" +
      "  \"IssuerEncryptedAmount\" : \"DDEEFF000123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF\",\n" +
      "  \"BlindingFactor\" : \"0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF\"\n" +
      "}";

    assertCanSerializeAndDeserialize(convert, json);
  }

}

