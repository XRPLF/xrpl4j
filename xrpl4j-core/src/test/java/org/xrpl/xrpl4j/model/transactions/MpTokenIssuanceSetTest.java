package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceSetFlags;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceSetMutableFlags;

class MpTokenIssuanceSetTest extends AbstractJsonTest {

  @Test
  void testJson() throws JSONException, JsonProcessingException {
    MpTokenIssuanceSet issuanceSet = MpTokenIssuanceSet.builder()
      .account(Address.of("rBcfczVUsaQTGNVGQ63hGZHmLNNzJr3gMd"))
      .sequence(UnsignedInteger.valueOf(335))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .mpTokenIssuanceId(MpTokenIssuanceId.of("0000014D745557D1E15173E54C7A8445DA5B28C50E90C7D4"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED6EC29EF994F886D623A58B4CDB36DAFDBB7812C289E17B770EDF7E3B2F53E148")
      )
      .domainId(Hash256.of("FEDCBA0987654321FEDCBA0987654321FEDCBA0987654321FEDCBA0987654321"))
      .build();

    String json =
      "{\n" +
      "  \"Account\" : \"rBcfczVUsaQTGNVGQ63hGZHmLNNzJr3gMd\",\n" +
      "  \"TransactionType\" : \"MPTokenIssuanceSet\",\n" +
      "  \"Fee\" : \"15\",\n" +
      "  \"Sequence\" : 335,\n" +
      "  \"SigningPubKey\" : \"ED6EC29EF994F886D623A58B4CDB36DAFDBB7812C289E17B770EDF7E3B2F53E148\",\n" +
      "  \"MPTokenIssuanceID\" : \"0000014D745557D1E15173E54C7A8445DA5B28C50E90C7D4\",\n" +
      "  \"DomainID\" : \"FEDCBA0987654321FEDCBA0987654321FEDCBA0987654321FEDCBA0987654321\"\n" +
      "}";

    assertCanSerializeAndDeserialize(issuanceSet, json);
  }

  @Test
  void transactionFlagsReturnsEmptyFlags() {
    MpTokenIssuanceSet issuanceSet = MpTokenIssuanceSet.builder()
      .account(Address.of("rBcfczVUsaQTGNVGQ63hGZHmLNNzJr3gMd"))
      .sequence(UnsignedInteger.valueOf(335))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .mpTokenIssuanceId(MpTokenIssuanceId.of("0000014D745557D1E15173E54C7A8445DA5B28C50E90C7D4"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED6EC29EF994F886D623A58B4CDB36DAFDBB7812C289E17B770EDF7E3B2F53E148")
      )
      .build();

    assertThat(issuanceSet.transactionFlags()).isEqualTo(issuanceSet.flags());
    assertThat(issuanceSet.transactionFlags().isEmpty()).isTrue();
  }

  @Test
  void transactionFlagsReturnsCorrectFlagsWhenSet() {
    MpTokenIssuanceSet issuanceSet = MpTokenIssuanceSet.builder()
      .account(Address.of("rBcfczVUsaQTGNVGQ63hGZHmLNNzJr3gMd"))
      .sequence(UnsignedInteger.valueOf(335))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .mpTokenIssuanceId(MpTokenIssuanceId.of("0000014D745557D1E15173E54C7A8445DA5B28C50E90C7D4"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED6EC29EF994F886D623A58B4CDB36DAFDBB7812C289E17B770EDF7E3B2F53E148")
      )
      .flags(MpTokenIssuanceSetFlags.LOCK)
      .build();

    assertThat(issuanceSet.transactionFlags()).isEqualTo(issuanceSet.flags());
    assertThat(((MpTokenIssuanceSetFlags) issuanceSet.transactionFlags()).tfMptLock()).isTrue();
  }

  @Test
  void builderFromCopiesFlagsCorrectly() {
    MpTokenIssuanceSet original = MpTokenIssuanceSet.builder()
      .account(Address.of("rBcfczVUsaQTGNVGQ63hGZHmLNNzJr3gMd"))
      .sequence(UnsignedInteger.valueOf(335))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .mpTokenIssuanceId(MpTokenIssuanceId.of("0000014D745557D1E15173E54C7A8445DA5B28C50E90C7D4"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED6EC29EF994F886D623A58B4CDB36DAFDBB7812C289E17B770EDF7E3B2F53E148")
      )
      .flags(MpTokenIssuanceSetFlags.LOCK)
      .build();

    MpTokenIssuanceSet copied = MpTokenIssuanceSet.builder().from(original).build();

    assertThat(copied.flags()).isEqualTo(original.flags());
    assertThat(copied.transactionFlags()).isEqualTo(original.transactionFlags());
    assertThat(((MpTokenIssuanceSetFlags) copied.transactionFlags()).tfMptLock()).isTrue();
  }

  @Test
  void testJsonWithMutableFlagsAndMetadata() throws JSONException, JsonProcessingException {
    MpTokenIssuanceSet issuanceSet = MpTokenIssuanceSet.builder()
      .account(Address.of("rBcfczVUsaQTGNVGQ63hGZHmLNNzJr3gMd"))
      .sequence(UnsignedInteger.valueOf(335))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .mpTokenIssuanceId(MpTokenIssuanceId.of("0000014D745557D1E15173E54C7A8445DA5B28C50E90C7D4"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED6EC29EF994F886D623A58B4CDB36DAFDBB7812C289E17B770EDF7E3B2F53E148")
      )
      .mpTokenMetadata(MpTokenMetadata.of("575C5C"))
      .build();

    String json =
      "{\n" +
      "  \"Account\" : \"rBcfczVUsaQTGNVGQ63hGZHmLNNzJr3gMd\",\n" +
      "  \"TransactionType\" : \"MPTokenIssuanceSet\",\n" +
      "  \"Fee\" : \"15\",\n" +
      "  \"Sequence\" : 335,\n" +
      "  \"SigningPubKey\" : \"ED6EC29EF994F886D623A58B4CDB36DAFDBB7812C289E17B770EDF7E3B2F53E148\",\n" +
      "  \"MPTokenIssuanceID\" : \"0000014D745557D1E15173E54C7A8445DA5B28C50E90C7D4\",\n" +
      "  \"MPTokenMetadata\" : \"575C5C\"\n" +
      "}";

    assertCanSerializeAndDeserialize(issuanceSet, json);
  }

  @Test
  void testJsonWithMutableFlagsAndTransferFee() throws JSONException, JsonProcessingException {
    MpTokenIssuanceSet issuanceSet = MpTokenIssuanceSet.builder()
      .account(Address.of("rBcfczVUsaQTGNVGQ63hGZHmLNNzJr3gMd"))
      .sequence(UnsignedInteger.valueOf(335))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .mpTokenIssuanceId(MpTokenIssuanceId.of("0000014D745557D1E15173E54C7A8445DA5B28C50E90C7D4"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED6EC29EF994F886D623A58B4CDB36DAFDBB7812C289E17B770EDF7E3B2F53E148")
      )
      .transferFee(TransferFee.of(UnsignedInteger.valueOf(500)))
      .build();

    String json =
      "{\n" +
      "  \"Account\" : \"rBcfczVUsaQTGNVGQ63hGZHmLNNzJr3gMd\",\n" +
      "  \"TransactionType\" : \"MPTokenIssuanceSet\",\n" +
      "  \"Fee\" : \"15\",\n" +
      "  \"Sequence\" : 335,\n" +
      "  \"SigningPubKey\" : \"ED6EC29EF994F886D623A58B4CDB36DAFDBB7812C289E17B770EDF7E3B2F53E148\",\n" +
      "  \"MPTokenIssuanceID\" : \"0000014D745557D1E15173E54C7A8445DA5B28C50E90C7D4\",\n" +
      "  \"TransferFee\" : 500\n" +
      "}";

    assertCanSerializeAndDeserialize(issuanceSet, json);
  }

  @Test
  void testJsonWithMutableFlagsSettingFlags() throws JSONException, JsonProcessingException {
    // tmfMPTSetCanLock (0x1) | tmfMPTSetCanEscrow (0x4) = 0x5 = 5
    MpTokenIssuanceSet issuanceSet = MpTokenIssuanceSet.builder()
      .account(Address.of("rBcfczVUsaQTGNVGQ63hGZHmLNNzJr3gMd"))
      .sequence(UnsignedInteger.valueOf(335))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .mpTokenIssuanceId(MpTokenIssuanceId.of("0000014D745557D1E15173E54C7A8445DA5B28C50E90C7D4"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED6EC29EF994F886D623A58B4CDB36DAFDBB7812C289E17B770EDF7E3B2F53E148")
      )
      .mutableFlags(MpTokenIssuanceSetMutableFlags.builder()
        .tmfMptSetCanLock(true)
        .tmfMptSetCanEscrow(true)
        .build()
      )
      .build();

    String json =
      "{\n" +
      "  \"Account\" : \"rBcfczVUsaQTGNVGQ63hGZHmLNNzJr3gMd\",\n" +
      "  \"TransactionType\" : \"MPTokenIssuanceSet\",\n" +
      "  \"Fee\" : \"15\",\n" +
      "  \"Sequence\" : 335,\n" +
      "  \"SigningPubKey\" : \"ED6EC29EF994F886D623A58B4CDB36DAFDBB7812C289E17B770EDF7E3B2F53E148\",\n" +
      "  \"MPTokenIssuanceID\" : \"0000014D745557D1E15173E54C7A8445DA5B28C50E90C7D4\",\n" +
      "  \"MutableFlags\" : 5\n" +
      "}";

    assertCanSerializeAndDeserialize(issuanceSet, json);
  }

  @Test
  void mutableFlagsSetBuilderSetsCorrectBits() {
    MpTokenIssuanceSetMutableFlags flags = MpTokenIssuanceSetMutableFlags.builder()
      .tmfMptSetCanLock(true)
      .tmfMptSetRequireAuth(true)
      .tmfMptSetCanEscrow(true)
      .tmfMptSetCanTrade(true)
      .tmfMptSetCanTransfer(true)
      .tmfMptSetCanClawback(true)
      .build();

    assertThat(flags.tmfMptSetCanLock()).isTrue();
    assertThat(flags.tmfMptSetRequireAuth()).isTrue();
    assertThat(flags.tmfMptSetCanEscrow()).isTrue();
    assertThat(flags.tmfMptSetCanTrade()).isTrue();
    assertThat(flags.tmfMptSetCanTransfer()).isTrue();
    assertThat(flags.tmfMptSetCanClawback()).isTrue();

    // 0x1|0x2|0x4|0x8|0x10|0x20 = 0x3F = 63
    assertThat(flags.getValue()).isEqualTo(63L);
  }

  @Test
  void mutableFlagsSetOfParsesCorrectly() {
    // tmfMPTSetCanLock = 0x1
    MpTokenIssuanceSetMutableFlags flags = MpTokenIssuanceSetMutableFlags.of(1L);
    assertThat(flags.tmfMptSetCanLock()).isTrue();
    assertThat(flags.tmfMptSetRequireAuth()).isFalse();
  }

  @Test
  void testJsonWithDomainId() throws JSONException, JsonProcessingException {
    MpTokenIssuanceSet issuanceSet = MpTokenIssuanceSet.builder()
      .account(Address.of("rBcfczVUsaQTGNVGQ63hGZHmLNNzJr3gMd"))
      .sequence(UnsignedInteger.valueOf(335))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .mpTokenIssuanceId(MpTokenIssuanceId.of("0000014D745557D1E15173E54C7A8445DA5B28C50E90C7D4"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED6EC29EF994F886D623A58B4CDB36DAFDBB7812C289E17B770EDF7E3B2F53E148")
      )
      .domainId(Hash256.of("A4C9D0EB468ED7F02A63EB8F5A2D5CAEE7EDE4D8E8F202C93B0FF5D78B72E921"))
      .build();

    String json =
      "{\n" +
      "  \"Account\" : \"rBcfczVUsaQTGNVGQ63hGZHmLNNzJr3gMd\",\n" +
      "  \"TransactionType\" : \"MPTokenIssuanceSet\",\n" +
      "  \"Fee\" : \"15\",\n" +
      "  \"Sequence\" : 335,\n" +
      "  \"SigningPubKey\" : \"ED6EC29EF994F886D623A58B4CDB36DAFDBB7812C289E17B770EDF7E3B2F53E148\",\n" +
      "  \"MPTokenIssuanceID\" : \"0000014D745557D1E15173E54C7A8445DA5B28C50E90C7D4\",\n" +
      "  \"DomainID\" : \"A4C9D0EB468ED7F02A63EB8F5A2D5CAEE7EDE4D8E8F202C93B0FF5D78B72E921\"\n" +
      "}";

    assertCanSerializeAndDeserialize(issuanceSet, json);
  }

  @Test
  void mutableFlagsAndHolderMutuallyExclusive() {
    assertThatThrownBy(() -> MpTokenIssuanceSet.builder()
      .account(Address.of("rBcfczVUsaQTGNVGQ63hGZHmLNNzJr3gMd"))
      .sequence(UnsignedInteger.valueOf(335))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .mpTokenIssuanceId(MpTokenIssuanceId.of("0000014D745557D1E15173E54C7A8445DA5B28C50E90C7D4"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED6EC29EF994F886D623A58B4CDB36DAFDBB7812C289E17B770EDF7E3B2F53E148")
      )
      .mutableFlags(MpTokenIssuanceSetMutableFlags.builder().tmfMptSetCanLock(true).build())
      .holder(Address.of("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"))
      .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Holder must not be present");
  }

  @Test
  void mutableFlagsAndLockFlagsMutuallyExclusive() {
    assertThatThrownBy(() -> MpTokenIssuanceSet.builder()
      .account(Address.of("rBcfczVUsaQTGNVGQ63hGZHmLNNzJr3gMd"))
      .sequence(UnsignedInteger.valueOf(335))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .mpTokenIssuanceId(MpTokenIssuanceId.of("0000014D745557D1E15173E54C7A8445DA5B28C50E90C7D4"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED6EC29EF994F886D623A58B4CDB36DAFDBB7812C289E17B770EDF7E3B2F53E148")
      )
      .flags(MpTokenIssuanceSetFlags.LOCK)
      .mutableFlags(MpTokenIssuanceSetMutableFlags.builder().tmfMptSetCanLock(true).build())
      .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("tfMPTLock and tfMPTUnlock must not be set");
  }

  @Test
  void domainIdAndHolderMutuallyExclusive() {
    assertThatThrownBy(() -> MpTokenIssuanceSet.builder()
      .account(Address.of("rBcfczVUsaQTGNVGQ63hGZHmLNNzJr3gMd"))
      .sequence(UnsignedInteger.valueOf(335))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .mpTokenIssuanceId(MpTokenIssuanceId.of("0000014D745557D1E15173E54C7A8445DA5B28C50E90C7D4"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED6EC29EF994F886D623A58B4CDB36DAFDBB7812C289E17B770EDF7E3B2F53E148")
      )
      .domainId(Hash256.of("A4C9D0EB468ED7F02A63EB8F5A2D5CAEE7EDE4D8E8F202C93B0FF5D78B72E921"))
      .holder(Address.of("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"))
      .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("DomainID and Holder are mutually exclusive");
  }

  @Test
  void mutableFlagsRejectsZero() {
    assertThatThrownBy(() -> MpTokenIssuanceSet.builder()
      .account(Address.of("rBcfczVUsaQTGNVGQ63hGZHmLNNzJr3gMd"))
      .sequence(UnsignedInteger.valueOf(335))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .mpTokenIssuanceId(MpTokenIssuanceId.of("0000014D745557D1E15173E54C7A8445DA5B28C50E90C7D4"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED6EC29EF994F886D623A58B4CDB36DAFDBB7812C289E17B770EDF7E3B2F53E148")
      )
      .mutableFlags(MpTokenIssuanceSetMutableFlags.of(0L))
      .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("MutableFlags must not be 0");
  }

  /**
   * After removing the {@code Clear*} flags (rippled #7439 / XLS-94), {@code VALID_MASK} is {@code 0x3F}. Bits
   * {@code 0x40}–{@code 0x800} were valid {@code Set}/{@code Clear} bits under the old mask but are now rejected,
   * and {@code 0x1000} is out of range under both masks. This exercises the tightened mask rather than only a
   * bit that was already invalid before the change.
   */
  @ParameterizedTest
  @ValueSource(longs = {0x40L, 0x80L, 0x100L, 0x200L, 0x400L, 0x800L, 0x1000L})
  void mutableFlagsRejectsInvalidBits(long invalidBit) {
    assertThatThrownBy(() -> MpTokenIssuanceSet.builder()
      .account(Address.of("rBcfczVUsaQTGNVGQ63hGZHmLNNzJr3gMd"))
      .sequence(UnsignedInteger.valueOf(335))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .mpTokenIssuanceId(MpTokenIssuanceId.of("0000014D745557D1E15173E54C7A8445DA5B28C50E90C7D4"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED6EC29EF994F886D623A58B4CDB36DAFDBB7812C289E17B770EDF7E3B2F53E148")
      )
      .mutableFlags(MpTokenIssuanceSetMutableFlags.of(invalidBit))
      .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("invalid bits");
  }

  @Test
  void mutableFlagsAndUnlockFlagsMutuallyExclusive() {
    assertThatThrownBy(() -> MpTokenIssuanceSet.builder()
      .account(Address.of("rBcfczVUsaQTGNVGQ63hGZHmLNNzJr3gMd"))
      .sequence(UnsignedInteger.valueOf(335))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .mpTokenIssuanceId(MpTokenIssuanceId.of("0000014D745557D1E15173E54C7A8445DA5B28C50E90C7D4"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED6EC29EF994F886D623A58B4CDB36DAFDBB7812C289E17B770EDF7E3B2F53E148")
      )
      .flags(MpTokenIssuanceSetFlags.UNLOCK)
      .mutableFlags(MpTokenIssuanceSetMutableFlags.builder().tmfMptSetCanLock(true).build())
      .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("tfMPTLock and tfMPTUnlock must not be set");
  }

  @Test
  void transferFeeWithSetCanTransferAllowed() {
    MpTokenIssuanceSet issuanceSet = MpTokenIssuanceSet.builder()
      .account(Address.of("rBcfczVUsaQTGNVGQ63hGZHmLNNzJr3gMd"))
      .sequence(UnsignedInteger.valueOf(335))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .mpTokenIssuanceId(MpTokenIssuanceId.of("0000014D745557D1E15173E54C7A8445DA5B28C50E90C7D4"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED6EC29EF994F886D623A58B4CDB36DAFDBB7812C289E17B770EDF7E3B2F53E148")
      )
      .transferFee(TransferFee.of(UnsignedInteger.valueOf(500)))
      .mutableFlags(MpTokenIssuanceSetMutableFlags.builder().tmfMptSetCanTransfer(true).build())
      .build();

    assertThat(issuanceSet.transferFee()).isPresent();
    assertThat(issuanceSet.mutableFlags()).isPresent();
  }
}
