package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;

class ConfidentialMptConvertTest extends AbstractJsonTest {

  private static final String HOLDER_ELGAMAL_PUB_KEY =
    "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF" +
      "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF";
  private static final String HOLDER_ENCRYPTED_AMOUNT =
    "AABBCCDD0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF";
  private static final String ISSUER_ENCRYPTED_AMOUNT =
    "DDEEFF000123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF";
  private static final String AUDITOR_ENCRYPTED_AMOUNT =
    "11223344556677889900AABBCCDDEEFF0123456789ABCDEF0123456789ABCDEF0123456789";
  private static final String BLINDING_FACTOR =
    "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF";
  private static final String ZK_PROOF =
    "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF" +
      "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF01";

  @Test
  void testJsonWithAllFields() throws JSONException, JsonProcessingException {
    ConfidentialMptConvert convert = ConfidentialMptConvert.builder()
      .account(Address.of("rJbVo4xrsGN8o3vLKGXe1s1uW8mAMYHamV"))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .sequence(UnsignedInteger.valueOf(432))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey(
          "ED0C0B9B7D5F80868A701693D7C994385EB4DC661D9E7A2DD95E8199EDC5C211E7"
        )
      )
      .mpTokenIssuanceId(MpTokenIssuanceId.of("00000143A58DCB491FD36A15A7D3172E6A9F088A5478BA41"))
      .mptAmount(MpTokenNumericAmount.of(1000L))
      .holderEncryptionKey(HOLDER_ELGAMAL_PUB_KEY)
      .holderEncryptedAmount(HOLDER_ENCRYPTED_AMOUNT)
      .issuerEncryptedAmount(ISSUER_ENCRYPTED_AMOUNT)
      .auditorEncryptedAmount(AUDITOR_ENCRYPTED_AMOUNT)
      .blindingFactor(BLINDING_FACTOR)
      .zkProof(ZK_PROOF)
      .build();

    String json =
      "{\n" +
        "  \"Account\" : \"rJbVo4xrsGN8o3vLKGXe1s1uW8mAMYHamV\",\n" +
        "  \"TransactionType\" : \"ConfidentialMPTConvert\",\n" +
        "  \"Fee\" : \"15\",\n" +
        "  \"Sequence\" : 432,\n" +
        "  \"SigningPubKey\" : " +
        "\"ED0C0B9B7D5F80868A701693D7C994385EB4DC661D9E7A2DD95E8199EDC5C211E7\",\n" +
        "  \"MPTokenIssuanceID\" : \"00000143A58DCB491FD36A15A7D3172E6A9F088A5478BA41\",\n" +
        "  \"MPTAmount\" : \"1000\",\n" +
        "  \"HolderEncryptionKey\" : \"" + HOLDER_ELGAMAL_PUB_KEY + "\",\n" +
        "  \"HolderEncryptedAmount\" : \"" + HOLDER_ENCRYPTED_AMOUNT + "\",\n" +
        "  \"IssuerEncryptedAmount\" : \"" + ISSUER_ENCRYPTED_AMOUNT + "\",\n" +
        "  \"AuditorEncryptedAmount\" : \"" + AUDITOR_ENCRYPTED_AMOUNT + "\",\n" +
        "  \"BlindingFactor\" : \"" + BLINDING_FACTOR + "\",\n" +
        "  \"ZKProof\" : \"" + ZK_PROOF + "\"\n" +
        "}";

    assertCanSerializeAndDeserialize(convert, json);
  }

  @Test
  void testJsonWithRequiredFieldsOnly() throws JSONException, JsonProcessingException {
    ConfidentialMptConvert convert = ConfidentialMptConvert.builder()
      .account(Address.of("rJbVo4xrsGN8o3vLKGXe1s1uW8mAMYHamV"))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .sequence(UnsignedInteger.valueOf(432))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey(
          "ED0C0B9B7D5F80868A701693D7C994385EB4DC661D9E7A2DD95E8199EDC5C211E7"
        )
      )
      .mpTokenIssuanceId(MpTokenIssuanceId.of("00000143A58DCB491FD36A15A7D3172E6A9F088A5478BA41"))
      .mptAmount(MpTokenNumericAmount.of(500L))
      .holderEncryptedAmount(HOLDER_ENCRYPTED_AMOUNT)
      .issuerEncryptedAmount(ISSUER_ENCRYPTED_AMOUNT)
      .blindingFactor(BLINDING_FACTOR)
      .build();

    String json =
      "{\n" +
        "  \"Account\" : \"rJbVo4xrsGN8o3vLKGXe1s1uW8mAMYHamV\",\n" +
        "  \"TransactionType\" : \"ConfidentialMPTConvert\",\n" +
        "  \"Fee\" : \"15\",\n" +
        "  \"Sequence\" : 432,\n" +
        "  \"SigningPubKey\" : " +
        "\"ED0C0B9B7D5F80868A701693D7C994385EB4DC661D9E7A2DD95E8199EDC5C211E7\",\n" +
        "  \"MPTokenIssuanceID\" : \"00000143A58DCB491FD36A15A7D3172E6A9F088A5478BA41\",\n" +
        "  \"MPTAmount\" : \"500\",\n" +
        "  \"HolderEncryptedAmount\" : \"" + HOLDER_ENCRYPTED_AMOUNT + "\",\n" +
        "  \"IssuerEncryptedAmount\" : \"" + ISSUER_ENCRYPTED_AMOUNT + "\",\n" +
        "  \"BlindingFactor\" : \"" + BLINDING_FACTOR + "\"\n" +
        "}";

    assertCanSerializeAndDeserialize(convert, json);
  }

}

