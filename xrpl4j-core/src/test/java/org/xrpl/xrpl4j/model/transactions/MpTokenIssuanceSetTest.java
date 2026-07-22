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
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceImmutableFlags;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceSetFlags;

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
  void testJsonWithImmutableFlagsAndMetadata() throws JSONException, JsonProcessingException {
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
  void testJsonWithImmutableFlagsAndTransferFee() throws JSONException, JsonProcessingException {
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
  void testJsonWithSetCanEnableFlags() throws JSONException, JsonProcessingException {
    // tfFullyCanonicalSig (0x80000000) | tfMPTSetCanLock (0x4) | tfMPTSetCanEscrow (0x10) = 2147483668
    MpTokenIssuanceSet issuanceSet = MpTokenIssuanceSet.builder()
      .account(Address.of("rBcfczVUsaQTGNVGQ63hGZHmLNNzJr3gMd"))
      .sequence(UnsignedInteger.valueOf(335))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .mpTokenIssuanceId(MpTokenIssuanceId.of("0000014D745557D1E15173E54C7A8445DA5B28C50E90C7D4"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED6EC29EF994F886D623A58B4CDB36DAFDBB7812C289E17B770EDF7E3B2F53E148")
      )
      .flags(MpTokenIssuanceSetFlags.builder()
        .tfMptSetCanLock(true)
        .tfMptSetCanEscrow(true)
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
      "  \"Flags\" : 2147483668,\n" +
      "  \"MPTokenIssuanceID\" : \"0000014D745557D1E15173E54C7A8445DA5B28C50E90C7D4\"\n" +
      "}";

    assertCanSerializeAndDeserialize(issuanceSet, json);
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
  void immutableFlagsAndHolderMutuallyExclusive() {
    assertThatThrownBy(() -> MpTokenIssuanceSet.builder()
      .account(Address.of("rBcfczVUsaQTGNVGQ63hGZHmLNNzJr3gMd"))
      .sequence(UnsignedInteger.valueOf(335))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .mpTokenIssuanceId(MpTokenIssuanceId.of("0000014D745557D1E15173E54C7A8445DA5B28C50E90C7D4"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED6EC29EF994F886D623A58B4CDB36DAFDBB7812C289E17B770EDF7E3B2F53E148")
      )
      .immutableFlags(MpTokenIssuanceImmutableFlags.builder().lsifMptCanLock(true).build())
      .holder(Address.of("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"))
      .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Holder must not be present");
  }

  @Test
  void immutableFlagsAndLockFlagsMutuallyExclusive() {
    assertThatThrownBy(() -> MpTokenIssuanceSet.builder()
      .account(Address.of("rBcfczVUsaQTGNVGQ63hGZHmLNNzJr3gMd"))
      .sequence(UnsignedInteger.valueOf(335))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .mpTokenIssuanceId(MpTokenIssuanceId.of("0000014D745557D1E15173E54C7A8445DA5B28C50E90C7D4"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED6EC29EF994F886D623A58B4CDB36DAFDBB7812C289E17B770EDF7E3B2F53E148")
      )
      .flags(MpTokenIssuanceSetFlags.LOCK)
      .immutableFlags(MpTokenIssuanceImmutableFlags.builder().lsifMptCanLock(true).build())
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
  void immutableFlagsRejectsZero() {
    assertThatThrownBy(() -> MpTokenIssuanceSet.builder()
      .account(Address.of("rBcfczVUsaQTGNVGQ63hGZHmLNNzJr3gMd"))
      .sequence(UnsignedInteger.valueOf(335))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .mpTokenIssuanceId(MpTokenIssuanceId.of("0000014D745557D1E15173E54C7A8445DA5B28C50E90C7D4"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED6EC29EF994F886D623A58B4CDB36DAFDBB7812C289E17B770EDF7E3B2F53E148")
      )
      .immutableFlags(MpTokenIssuanceImmutableFlags.of(0L))
      .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("ImmutableFlags must not be 0");
  }

  /**
   * Bit {@code 0x1} mirrors {@code lsfMPTLocked} and is not a valid {@code ImmutableFlags} bit; bits between the
   * highest capability bit ({@code 0x80}) and the {@code MPTokenMetadata}/{@code TransferFee} bits
   * ({@code 0x10000}/{@code 0x20000}) are unused and also rejected.
   */
  @ParameterizedTest
  @ValueSource(longs = {0x1L, 0x100L, 0x200L, 0x400L, 0x800L, 0x1000L, 0x8000L, 0x40000L})
  void immutableFlagsRejectsInvalidBits(long invalidBit) {
    assertThatThrownBy(() -> MpTokenIssuanceSet.builder()
      .account(Address.of("rBcfczVUsaQTGNVGQ63hGZHmLNNzJr3gMd"))
      .sequence(UnsignedInteger.valueOf(335))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .mpTokenIssuanceId(MpTokenIssuanceId.of("0000014D745557D1E15173E54C7A8445DA5B28C50E90C7D4"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED6EC29EF994F886D623A58B4CDB36DAFDBB7812C289E17B770EDF7E3B2F53E148")
      )
      .immutableFlags(MpTokenIssuanceImmutableFlags.of(invalidBit))
      .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("invalid bits");
  }

  @Test
  void immutableFlagsAndUnlockFlagsMutuallyExclusive() {
    assertThatThrownBy(() -> MpTokenIssuanceSet.builder()
      .account(Address.of("rBcfczVUsaQTGNVGQ63hGZHmLNNzJr3gMd"))
      .sequence(UnsignedInteger.valueOf(335))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .mpTokenIssuanceId(MpTokenIssuanceId.of("0000014D745557D1E15173E54C7A8445DA5B28C50E90C7D4"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED6EC29EF994F886D623A58B4CDB36DAFDBB7812C289E17B770EDF7E3B2F53E148")
      )
      .flags(MpTokenIssuanceSetFlags.UNLOCK)
      .immutableFlags(MpTokenIssuanceImmutableFlags.builder().lsifMptCanLock(true).build())
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
      .flags(MpTokenIssuanceSetFlags.builder().tfMptSetCanTransfer(true).build())
      .build();

    assertThat(issuanceSet.transferFee()).isPresent();
    assertThat(issuanceSet.flags().tfMptSetCanTransfer()).isTrue();
  }
}
