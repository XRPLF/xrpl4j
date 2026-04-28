package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceCreateFlags;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceCreateMutableFlags;

class MpTokenIssuanceCreateTest extends AbstractJsonTest {

  @Test
  void testJson() throws JSONException, JsonProcessingException {
    MpTokenIssuanceCreate issuanceCreate = MpTokenIssuanceCreate.builder()
      .account(Address.of("rhqFECTUUqYYQouPHojLfrtjdx1WZ5jqrZ"))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .sequence(UnsignedInteger.valueOf(321))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("EDFE73FB561109EDCFB27C07B1870731849B4FC7718A8DCC9F9A1FB4E974874710")
      )
      .flags(MpTokenIssuanceCreateFlags.builder()
        .tfMptCanLock(true)
        .tfMptCanEscrow(true)
        .tfMptCanTrade(true)
        .tfMptCanTransfer(true)
        .tfMptCanClawback(true)
        .build()
      )
      .assetScale(AssetScale.of(UnsignedInteger.valueOf(2)))
      .transferFee(TransferFee.of(UnsignedInteger.valueOf(10)))
      .maximumAmount(MpTokenNumericAmount.of(Long.MAX_VALUE))
      .mpTokenMetadata(MpTokenMetadata.of("ABCD"))
      .build();
    String json =
      "{\n" +
      "  \"Account\" : \"rhqFECTUUqYYQouPHojLfrtjdx1WZ5jqrZ\",\n" +
      "  \"TransactionType\" : \"MPTokenIssuanceCreate\",\n" +
      "  \"Fee\" : \"15\",\n" +
      "  \"Sequence\" : 321,\n" +
      "  \"SigningPubKey\" : \"EDFE73FB561109EDCFB27C07B1870731849B4FC7718A8DCC9F9A1FB4E974874710\",\n" +
      "  \"Flags\" : 2147483770,\n" +
      "  \"AssetScale\" : 2,\n" +
      "  \"TransferFee\" : 10,\n" +
      "  \"MaximumAmount\" : \"9223372036854775807\",\n" +
      "  \"MPTokenMetadata\" : \"ABCD\"\n" +
      "}";

    assertCanSerializeAndDeserialize(issuanceCreate, json);
  }

  @Test
  void transactionFlagsReturnsEmptyFlags() {
    MpTokenIssuanceCreate issuanceCreate = MpTokenIssuanceCreate.builder()
      .account(Address.of("rhqFECTUUqYYQouPHojLfrtjdx1WZ5jqrZ"))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .sequence(UnsignedInteger.valueOf(321))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("EDFE73FB561109EDCFB27C07B1870731849B4FC7718A8DCC9F9A1FB4E974874710")
      )
      .build();

    assertThat(issuanceCreate.transactionFlags()).isEqualTo(issuanceCreate.flags());
    assertThat(issuanceCreate.transactionFlags().isEmpty()).isTrue();
  }

  @Test
  void transactionFlagsReturnsCorrectFlagsWhenSet() {
    MpTokenIssuanceCreate issuanceCreate = MpTokenIssuanceCreate.builder()
      .account(Address.of("rhqFECTUUqYYQouPHojLfrtjdx1WZ5jqrZ"))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .sequence(UnsignedInteger.valueOf(321))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("EDFE73FB561109EDCFB27C07B1870731849B4FC7718A8DCC9F9A1FB4E974874710")
      )
      .flags(MpTokenIssuanceCreateFlags.builder()
        .tfMptCanTransfer(true)
        .build()
      )
      .build();

    assertThat(issuanceCreate.transactionFlags()).isEqualTo(issuanceCreate.flags());
    assertThat(((MpTokenIssuanceCreateFlags) issuanceCreate.transactionFlags()).tfMptCanTransfer()).isTrue();
  }

  @Test
  void testJsonWithMutableFlags() throws JSONException, JsonProcessingException {
    MpTokenIssuanceCreate issuanceCreate = MpTokenIssuanceCreate.builder()
      .account(Address.of("rhqFECTUUqYYQouPHojLfrtjdx1WZ5jqrZ"))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .sequence(UnsignedInteger.valueOf(321))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("EDFE73FB561109EDCFB27C07B1870731849B4FC7718A8DCC9F9A1FB4E974874710")
      )
      .flags(MpTokenIssuanceCreateFlags.builder()
        .tfMptCanTransfer(true)
        .build()
      )
      .mutableFlags(MpTokenIssuanceCreateMutableFlags.builder()
        .tmfMptCanMutateMetadata(true)
        .tmfMptCanMutateTransferFee(true)
        .build()
      )
      .mpTokenMetadata(MpTokenMetadata.of("464F4F"))
      .transferFee(TransferFee.of(UnsignedInteger.valueOf(100)))
      .build();

    // tmfMPTCanMutateMetadata (0x00010000) | tmfMPTCanMutateTransferFee (0x00020000) = 0x00030000 = 196608
    String json =
      "{\n" +
      "  \"Account\" : \"rhqFECTUUqYYQouPHojLfrtjdx1WZ5jqrZ\",\n" +
      "  \"TransactionType\" : \"MPTokenIssuanceCreate\",\n" +
      "  \"Fee\" : \"15\",\n" +
      "  \"Sequence\" : 321,\n" +
      "  \"SigningPubKey\" : \"EDFE73FB561109EDCFB27C07B1870731849B4FC7718A8DCC9F9A1FB4E974874710\",\n" +
      "  \"Flags\" : 2147483680,\n" +
      "  \"MutableFlags\" : 196608,\n" +
      "  \"MPTokenMetadata\" : \"464F4F\",\n" +
      "  \"TransferFee\" : 100\n" +
      "}";

    assertCanSerializeAndDeserialize(issuanceCreate, json);
  }

  @Test
  void mutableFlagsBuilderSetsCorrectBits() {
    MpTokenIssuanceCreateMutableFlags flags = MpTokenIssuanceCreateMutableFlags.builder()
      .tmfMptCanMutateCanLock(true)
      .tmfMptCanMutateRequireAuth(true)
      .tmfMptCanMutateCanEscrow(true)
      .tmfMptCanMutateCanTrade(true)
      .tmfMptCanMutateCanTransfer(true)
      .tmfMptCanMutateCanClawback(true)
      .tmfMptCanMutateMetadata(true)
      .tmfMptCanMutateTransferFee(true)
      .build();

    assertThat(flags.tmfMptCanMutateCanLock()).isTrue();
    assertThat(flags.tmfMptCanMutateRequireAuth()).isTrue();
    assertThat(flags.tmfMptCanMutateCanEscrow()).isTrue();
    assertThat(flags.tmfMptCanMutateCanTrade()).isTrue();
    assertThat(flags.tmfMptCanMutateCanTransfer()).isTrue();
    assertThat(flags.tmfMptCanMutateCanClawback()).isTrue();
    assertThat(flags.tmfMptCanMutateMetadata()).isTrue();
    assertThat(flags.tmfMptCanMutateTransferFee()).isTrue();

    // 0x2 | 0x4 | 0x8 | 0x10 | 0x20 | 0x40 | 0x10000 | 0x20000 = 0x3007E = 196734
    assertThat(flags.getValue()).isEqualTo(196734L);
  }

  @Test
  void mutableFlagsOfParsesCorrectly() {
    // tmfMPTCanMutateMetadata = 0x00010000 = 65536
    MpTokenIssuanceCreateMutableFlags flags = MpTokenIssuanceCreateMutableFlags.of(65536L);
    assertThat(flags.tmfMptCanMutateMetadata()).isTrue();
    assertThat(flags.tmfMptCanMutateTransferFee()).isFalse();
    assertThat(flags.tmfMptCanMutateCanLock()).isFalse();
  }

  @Test
  void builderFromCopiesFlagsCorrectly() {
    MpTokenIssuanceCreate original = MpTokenIssuanceCreate.builder()
      .account(Address.of("rhqFECTUUqYYQouPHojLfrtjdx1WZ5jqrZ"))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .sequence(UnsignedInteger.valueOf(321))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("EDFE73FB561109EDCFB27C07B1870731849B4FC7718A8DCC9F9A1FB4E974874710")
      )
      .flags(MpTokenIssuanceCreateFlags.builder()
        .tfMptCanTransfer(true)
        .build()
      )
      .build();

    MpTokenIssuanceCreate copied = MpTokenIssuanceCreate.builder().from(original).build();

    assertThat(copied.flags()).isEqualTo(original.flags());
    assertThat(copied.transactionFlags()).isEqualTo(original.transactionFlags());
    assertThat(((MpTokenIssuanceCreateFlags) copied.transactionFlags()).tfMptCanTransfer()).isTrue();
  }
}
