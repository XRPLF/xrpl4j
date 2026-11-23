package org.xrpl.xrpl4j.crypto.confidential;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.base.Strings;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Unit tests for the {@link ConfidentialMptOp} closed-union dispatch ({@link ConfidentialMptOp#handle} /
 * {@link ConfidentialMptOp#map}), verifying each of the five variants routes to the matching callback and that an
 * unrecognized implementation is rejected.
 */
class ConfidentialMptOpTest {

  private static final KeyPair EG = Seed.elGamalSecp256k1SeedFromPassphrase(Passphrase.of("op-test")).deriveKeyPair();
  private static final Address ACCOUNT =
    Seed.secp256k1SeedFromPassphrase(Passphrase.of("op-test")).deriveKeyPair().publicKey().deriveAddress();
  private static final Address HOLDER =
    Seed.secp256k1SeedFromPassphrase(Passphrase.of("op-holder")).deriveKeyPair().publicKey().deriveAddress();
  private static final MpTokenIssuanceId TOKEN = MpTokenIssuanceId.of(Strings.repeat("0", 48));
  private static final UnsignedLong AMOUNT = UnsignedLong.valueOf(10);

  private static final ConfidentialSendOp SEND = ConfidentialSendOp.builder()
    .account(ACCOUNT).destination(HOLDER).amount(AMOUNT).senderKeyPair(EG).mpTokenIssuanceId(TOKEN).build();
  private static final ConfidentialConvertOp CONVERT = ConfidentialConvertOp.builder()
    .account(ACCOUNT).amount(AMOUNT).holderKeyPair(EG).mpTokenIssuanceId(TOKEN).build();
  private static final ConfidentialConvertBackOp CONVERT_BACK = ConfidentialConvertBackOp.builder()
    .account(ACCOUNT).amount(AMOUNT).holderKeyPair(EG).mpTokenIssuanceId(TOKEN).build();
  private static final ConfidentialMergeInboxOp MERGE = ConfidentialMergeInboxOp.builder()
    .account(ACCOUNT).mpTokenIssuanceId(TOKEN).build();
  private static final ConfidentialClawbackOp CLAWBACK = ConfidentialClawbackOp.builder()
    .account(ACCOUNT).holder(HOLDER).amount(AMOUNT).issuerKeyPair(EG).mpTokenIssuanceId(TOKEN).build();

  private static final Function<ConfidentialMptOp, String> LABEL = op -> op.map(
    send -> "send", convert -> "convert", convertBack -> "convertBack", mergeInbox -> "mergeInbox",
    clawback -> "clawback"
  );

  @Test
  void mapDispatchesToTheMatchingVariant() {
    assertThat(LABEL.apply(SEND)).isEqualTo("send");
    assertThat(LABEL.apply(CONVERT)).isEqualTo("convert");
    assertThat(LABEL.apply(CONVERT_BACK)).isEqualTo("convertBack");
    assertThat(LABEL.apply(MERGE)).isEqualTo("mergeInbox");
    assertThat(LABEL.apply(CLAWBACK)).isEqualTo("clawback");
  }

  @Test
  void handleDispatchesToTheMatchingVariant() {
    assertThat(handled(SEND)).isEqualTo("send");
    assertThat(handled(CONVERT)).isEqualTo("convert");
    assertThat(handled(CONVERT_BACK)).isEqualTo("convertBack");
    assertThat(handled(MERGE)).isEqualTo("mergeInbox");
    assertThat(handled(CLAWBACK)).isEqualTo("clawback");
  }

  @Test
  void rejectsUnrecognizedVariant() {
    // An implementation that is none of the five known variants falls through to the default arm.
    ConfidentialMptOp unknown = new ConfidentialMptOp() {
      @Override
      public Address account() {
        return ACCOUNT;
      }

      @Override
      public MpTokenIssuanceId mpTokenIssuanceId() {
        return TOKEN;
      }
    };
    assertThatThrownBy(() -> LABEL.apply(unknown))
      .isInstanceOf(IllegalStateException.class).hasMessageContaining("Unsupported ConfidentialMptOp type");
    assertThatThrownBy(() -> handled(unknown))
      .isInstanceOf(IllegalStateException.class).hasMessageContaining("Unsupported ConfidentialMptOp type");
  }

  private static String handled(final ConfidentialMptOp op) {
    AtomicReference<String> result = new AtomicReference<>();
    op.handle(
      send -> result.set("send"),
      convert -> result.set("convert"),
      convertBack -> result.set("convertBack"),
      mergeInbox -> result.set("mergeInbox"),
      clawback -> result.set("clawback")
    );
    return result.get();
  }
}
