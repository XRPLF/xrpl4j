package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceCreateFlags;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceImmutableFlags;

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
        .tfMptRequireAuth(true)
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
      .domainId(Hash256.of("1234567890ABCDEF1234567890ABCDEF1234567890ABCDEF1234567890ABCDEF"))
      .build();
    String json =
      "{\n" +
      "  \"Account\" : \"rhqFECTUUqYYQouPHojLfrtjdx1WZ5jqrZ\",\n" +
      "  \"TransactionType\" : \"MPTokenIssuanceCreate\",\n" +
      "  \"Fee\" : \"15\",\n" +
      "  \"Sequence\" : 321,\n" +
      "  \"SigningPubKey\" : \"EDFE73FB561109EDCFB27C07B1870731849B4FC7718A8DCC9F9A1FB4E974874710\",\n" +
      "  \"Flags\" : 2147483774,\n" +
      "  \"AssetScale\" : 2,\n" +
      "  \"TransferFee\" : 10,\n" +
      "  \"MaximumAmount\" : \"9223372036854775807\",\n" +
      "  \"MPTokenMetadata\" : \"ABCD\",\n" +
      "  \"DomainID\" : \"1234567890ABCDEF1234567890ABCDEF1234567890ABCDEF1234567890ABCDEF\"\n" +
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
  void testJsonWithImmutableFlags() throws JSONException, JsonProcessingException {
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
      .immutableFlags(MpTokenIssuanceImmutableFlags.builder()
        .lsifMptMetadata(true)
        .lsifMptTransferFee(true)
        .build()
      )
      .mpTokenMetadata(MpTokenMetadata.of("464F4F"))
      .transferFee(TransferFee.of(UnsignedInteger.valueOf(100)))
      .build();

    // lsifMPTMetadata (0x00010000) | lsifMPTTransferFee (0x00020000) = 0x00030000 = 196608
    String json =
      "{\n" +
      "  \"Account\" : \"rhqFECTUUqYYQouPHojLfrtjdx1WZ5jqrZ\",\n" +
      "  \"TransactionType\" : \"MPTokenIssuanceCreate\",\n" +
      "  \"Fee\" : \"15\",\n" +
      "  \"Sequence\" : 321,\n" +
      "  \"SigningPubKey\" : \"EDFE73FB561109EDCFB27C07B1870731849B4FC7718A8DCC9F9A1FB4E974874710\",\n" +
      "  \"Flags\" : 2147483680,\n" +
      "  \"ImmutableFlags\" : 196608,\n" +
      "  \"MPTokenMetadata\" : \"464F4F\",\n" +
      "  \"TransferFee\" : 100\n" +
      "}";

    assertCanSerializeAndDeserialize(issuanceCreate, json);
  }

  @Test
  void immutableFlagsBuilderSetsCorrectBits() {
    MpTokenIssuanceImmutableFlags flags = MpTokenIssuanceImmutableFlags.builder()
      .lsifMptCanLock(true)
      .lsifMptRequireAuth(true)
      .lsifMptCanEscrow(true)
      .lsifMptCanTrade(true)
      .lsifMptCanTransfer(true)
      .lsifMptCanClawback(true)
      .lsifMptCanHoldConfidentialBalance(true)
      .lsifMptMetadata(true)
      .lsifMptTransferFee(true)
      .build();

    assertThat(flags.lsifMptCanLock()).isTrue();
    assertThat(flags.lsifMptRequireAuth()).isTrue();
    assertThat(flags.lsifMptCanEscrow()).isTrue();
    assertThat(flags.lsifMptCanTrade()).isTrue();
    assertThat(flags.lsifMptCanTransfer()).isTrue();
    assertThat(flags.lsifMptCanClawback()).isTrue();
    assertThat(flags.lsifMptCanHoldConfidentialBalance()).isTrue();
    assertThat(flags.lsifMptMetadata()).isTrue();
    assertThat(flags.lsifMptTransferFee()).isTrue();

    // 0x2 | 0x4 | 0x8 | 0x10 | 0x20 | 0x40 | 0x80 | 0x10000 | 0x20000 = 0x300FE = 196862
    assertThat(flags.getValue()).isEqualTo(196862L);
  }

  @Test
  void immutableFlagsOfParsesCorrectly() {
    // lsifMPTMetadata = 0x00010000 = 65536
    MpTokenIssuanceImmutableFlags flags = MpTokenIssuanceImmutableFlags.of(65536L);
    assertThat(flags.lsifMptMetadata()).isTrue();
    assertThat(flags.lsifMptTransferFee()).isFalse();
    assertThat(flags.lsifMptCanLock()).isFalse();
  }

  @Test
  void testJsonWithDomainId() throws JSONException, JsonProcessingException {
    MpTokenIssuanceCreate issuanceCreate = MpTokenIssuanceCreate.builder()
      .account(Address.of("rhqFECTUUqYYQouPHojLfrtjdx1WZ5jqrZ"))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .sequence(UnsignedInteger.valueOf(321))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("EDFE73FB561109EDCFB27C07B1870731849B4FC7718A8DCC9F9A1FB4E974874710")
      )
      .flags(MpTokenIssuanceCreateFlags.builder()
        .tfMptRequireAuth(true)
        .build()
      )
      .domainId(Hash256.of("A4C9D0EB468ED7F02A63EB8F5A2D5CAEE7EDE4D8E8F202C93B0FF5D78B72E921"))
      .build();

    String json =
      "{\n" +
      "  \"Account\" : \"rhqFECTUUqYYQouPHojLfrtjdx1WZ5jqrZ\",\n" +
      "  \"TransactionType\" : \"MPTokenIssuanceCreate\",\n" +
      "  \"Fee\" : \"15\",\n" +
      "  \"Sequence\" : 321,\n" +
      "  \"SigningPubKey\" : \"EDFE73FB561109EDCFB27C07B1870731849B4FC7718A8DCC9F9A1FB4E974874710\",\n" +
      "  \"Flags\" : 2147483652,\n" +
      "  \"DomainID\" : \"A4C9D0EB468ED7F02A63EB8F5A2D5CAEE7EDE4D8E8F202C93B0FF5D78B72E921\"\n" +
      "}";

    assertCanSerializeAndDeserialize(issuanceCreate, json);
  }

  @Test
  void immutableFlagsRejectsReservedBitOne() {
    // bit 0x1 is reserved for lsfMPTLocked and must not appear in ImmutableFlags
    assertThatThrownBy(() -> MpTokenIssuanceCreate.builder()
      .account(Address.of("rhqFECTUUqYYQouPHojLfrtjdx1WZ5jqrZ"))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .sequence(UnsignedInteger.valueOf(321))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("EDFE73FB561109EDCFB27C07B1870731849B4FC7718A8DCC9F9A1FB4E974874710")
      )
      .immutableFlags(MpTokenIssuanceImmutableFlags.of(0x1L))
      .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("reserved");
  }

  @Test
  void immutableFlagsRejectsUnknownBits() {
    // bit 0x100 is not a valid ImmutableFlags bit for MPTokenIssuanceCreate
    assertThatThrownBy(() -> MpTokenIssuanceCreate.builder()
      .account(Address.of("rhqFECTUUqYYQouPHojLfrtjdx1WZ5jqrZ"))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .sequence(UnsignedInteger.valueOf(321))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("EDFE73FB561109EDCFB27C07B1870731849B4FC7718A8DCC9F9A1FB4E974874710")
      )
      .immutableFlags(MpTokenIssuanceImmutableFlags.of(0x100L))
      .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("invalid or reserved bits");
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
