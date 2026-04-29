package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.flags.VaultCreateFlags;
import org.xrpl.xrpl4j.model.ledger.IouIssue;
import org.xrpl.xrpl4j.model.ledger.Issue;
import org.xrpl.xrpl4j.model.ledger.MptIssue;

/**
 * Unit tests for {@link VaultCreate} validation logic.
 */
class VaultCreateTest {

  @Test
  void flagAccessors() {
    VaultCreateFlags flags = VaultCreateFlags.of(
      VaultCreateFlags.VAULT_PRIVATE.getValue() | VaultCreateFlags.VAULT_SHARE_NON_TRANSFERABLE.getValue()
    );
    assertThat(flags.tfVaultPrivate()).isTrue();
    assertThat(flags.tfVaultShareNonTransferable()).isTrue();
    assertThat(flags.tfInnerBatchTxn()).isFalse();
  }

  @Test
  void domainIdRequiresVaultPrivateFlag() {
    assertThatThrownBy(() -> baseBuilder()
      .domainId(Hash256.of("0000000000000000000000000000000000000000000000000000000000000001"))
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("DomainID is only allowed when the tfVaultPrivate flag is set.");
  }

  @Test
  void domainIdAllowedWithVaultPrivateFlag() {
    assertDoesNotThrow(() -> baseBuilder()
      .flags(VaultCreateFlags.builder().tfVaultPrivate(true).build())
      .domainId(Hash256.of("0000000000000000000000000000000000000000000000000000000000000001"))
      .build()
    );
  }

  @Test
  void scaleNotAllowedForXrpAsset() {
    assertThatThrownBy(() -> baseBuilder()
      .asset(Issue.XRP)
      .scale(AssetScale.of(UnsignedInteger.valueOf(6)))
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Scale is only allowed for IOU assets.");
  }

  @Test
  void scaleNotAllowedForMptAsset() {
    assertThatThrownBy(() -> baseBuilder()
      .asset(MptIssue.builder()
        .mptIssuanceId(MpTokenIssuanceId.of("00000001A407AF5856CEF3379FAB85D584830DAD31")) // MPTID
        .build())
      .scale(AssetScale.of(UnsignedInteger.valueOf(6)))
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Scale is only allowed for IOU assets.");
  }

  @Test
  void scaleMustNotExceed18() {
    assertThatThrownBy(() -> baseBuilder()
      .asset(IouIssue.builder()
        .currency("USD")
        .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
        .build())
      .scale(AssetScale.of(UnsignedInteger.valueOf(19)))
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Scale must be between 0 and 18.");
  }

  @Test
  void scaleAllowedForIouAsset() {
    assertDoesNotThrow(() -> baseBuilder()
      .asset(IouIssue.builder()
        .currency("USD")
        .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
        .build())
      .scale(AssetScale.of(UnsignedInteger.valueOf(18)))
      .build()
    );
  }

  @Test
  void scaleZeroAllowedForIouAsset() {
    assertDoesNotThrow(() -> baseBuilder()
      .asset(IouIssue.builder()
        .currency("USD")
        .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
        .build())
      .scale(AssetScale.of(UnsignedInteger.valueOf(0)))
      .build()
    );
  }

  /**
   * Returns a builder pre-populated with required fields for VaultCreate.
   */
  private ImmutableVaultCreate.Builder baseBuilder() {
    return VaultCreate.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(1))
      .asset(Issue.XRP)
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      );
  }
}
