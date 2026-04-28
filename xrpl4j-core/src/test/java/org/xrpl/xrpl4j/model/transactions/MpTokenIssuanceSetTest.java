package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
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
      .build();

    String json =
      "{\n" +
      "  \"Account\" : \"rBcfczVUsaQTGNVGQ63hGZHmLNNzJr3gMd\",\n" +
      "  \"TransactionType\" : \"MPTokenIssuanceSet\",\n" +
      "  \"Fee\" : \"15\",\n" +
      "  \"Sequence\" : 335,\n" +
      "  \"SigningPubKey\" : \"ED6EC29EF994F886D623A58B4CDB36DAFDBB7812C289E17B770EDF7E3B2F53E148\",\n" +
      "  \"MPTokenIssuanceID\" : \"0000014D745557D1E15173E54C7A8445DA5B28C50E90C7D4\"\n" +
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
    // tmfMPTSetCanLock (0x1) | tmfMPTSetCanEscrow (0x10) = 0x11 = 17
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
      "  \"MutableFlags\" : 17\n" +
      "}";

    assertCanSerializeAndDeserialize(issuanceSet, json);
  }

  @Test
  void mutableFlagsSetBuilderSetsCorrectBits() {
    MpTokenIssuanceSetMutableFlags flags = MpTokenIssuanceSetMutableFlags.builder()
      .tmfMptSetCanLock(true)
      .tmfMptClearCanLock(true)
      .tmfMptSetRequireAuth(true)
      .tmfMptClearRequireAuth(true)
      .tmfMptSetCanEscrow(true)
      .tmfMptClearCanEscrow(true)
      .tmfMptSetCanTrade(true)
      .tmfMptClearCanTrade(true)
      .tmfMptSetCanTransfer(true)
      .tmfMptClearCanTransfer(true)
      .tmfMptSetCanClawback(true)
      .tmfMptClearCanClawback(true)
      .build();

    assertThat(flags.tmfMptSetCanLock()).isTrue();
    assertThat(flags.tmfMptClearCanLock()).isTrue();
    assertThat(flags.tmfMptSetRequireAuth()).isTrue();
    assertThat(flags.tmfMptClearRequireAuth()).isTrue();
    assertThat(flags.tmfMptSetCanEscrow()).isTrue();
    assertThat(flags.tmfMptClearCanEscrow()).isTrue();
    assertThat(flags.tmfMptSetCanTrade()).isTrue();
    assertThat(flags.tmfMptClearCanTrade()).isTrue();
    assertThat(flags.tmfMptSetCanTransfer()).isTrue();
    assertThat(flags.tmfMptClearCanTransfer()).isTrue();
    assertThat(flags.tmfMptSetCanClawback()).isTrue();
    assertThat(flags.tmfMptClearCanClawback()).isTrue();

    // 0x1|0x2|0x4|0x8|0x10|0x20|0x40|0x80|0x100|0x200|0x400|0x800 = 0xFFF = 4095
    assertThat(flags.getValue()).isEqualTo(4095L);
  }

  @Test
  void mutableFlagsSetOfParsesCorrectly() {
    // tmfMPTSetCanLock = 0x1
    MpTokenIssuanceSetMutableFlags flags = MpTokenIssuanceSetMutableFlags.of(1L);
    assertThat(flags.tmfMptSetCanLock()).isTrue();
    assertThat(flags.tmfMptClearCanLock()).isFalse();
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
  void setAndClearSameFlagRejected() {
    // tmfMPTSetCanLock (0x1) | tmfMPTClearCanLock (0x2) = 0x3
    assertThatThrownBy(() -> MpTokenIssuanceSet.builder()
      .account(Address.of("rBcfczVUsaQTGNVGQ63hGZHmLNNzJr3gMd"))
      .sequence(UnsignedInteger.valueOf(335))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .mpTokenIssuanceId(MpTokenIssuanceId.of("0000014D745557D1E15173E54C7A8445DA5B28C50E90C7D4"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED6EC29EF994F886D623A58B4CDB36DAFDBB7812C289E17B770EDF7E3B2F53E148")
      )
      .mutableFlags(MpTokenIssuanceSetMutableFlags.of(
        MpTokenIssuanceSetMutableFlags.SET_CAN_LOCK.getValue() |
          MpTokenIssuanceSetMutableFlags.CLEAR_CAN_LOCK.getValue()
      ))
      .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Cannot set and clear lsfMPTCanLock");
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
  void nonZeroTransferFeeWithClearCanTransferRejected() {
    assertThatThrownBy(() -> MpTokenIssuanceSet.builder()
      .account(Address.of("rBcfczVUsaQTGNVGQ63hGZHmLNNzJr3gMd"))
      .sequence(UnsignedInteger.valueOf(335))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .mpTokenIssuanceId(MpTokenIssuanceId.of("0000014D745557D1E15173E54C7A8445DA5B28C50E90C7D4"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED6EC29EF994F886D623A58B4CDB36DAFDBB7812C289E17B770EDF7E3B2F53E148")
      )
      .transferFee(TransferFee.of(UnsignedInteger.valueOf(100)))
      .mutableFlags(MpTokenIssuanceSetMutableFlags.builder().tmfMptClearCanTransfer(true).build())
      .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("non-zero TransferFee cannot be combined with tmfMPTClearCanTransfer");
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

  @Test
  void mutableFlagsRejectsInvalidBits() {
    // bit 0x1000 is not a valid MutableFlags bit for MPTokenIssuanceSet
    assertThatThrownBy(() -> MpTokenIssuanceSet.builder()
      .account(Address.of("rBcfczVUsaQTGNVGQ63hGZHmLNNzJr3gMd"))
      .sequence(UnsignedInteger.valueOf(335))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .mpTokenIssuanceId(MpTokenIssuanceId.of("0000014D745557D1E15173E54C7A8445DA5B28C50E90C7D4"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED6EC29EF994F886D623A58B4CDB36DAFDBB7812C289E17B770EDF7E3B2F53E148")
      )
      .mutableFlags(MpTokenIssuanceSetMutableFlags.of(0x1000L))
      .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("invalid bits");
  }
}
