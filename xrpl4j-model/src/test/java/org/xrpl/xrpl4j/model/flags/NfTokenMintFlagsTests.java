package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

@SuppressWarnings("abbreviationaswordinname")
public class NfTokenMintFlagsTests extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(5);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testFlagsConstructionWithIndividualFlags(
    boolean tfFullyCanonicalSig,
    boolean tfBurnable,
    boolean tfOnlyXRP,
    boolean tfTrustLine,
    boolean tfTransferable
  ) {
    Flags.NfTokenMintFlags flags = Flags.NfTokenMintFlags.builder()
      .tfFullyCanonicalSig(tfFullyCanonicalSig)
      .tfBurnable(tfBurnable)
      .tfOnlyXRP(tfOnlyXRP)
      .tfTrustLine(tfTrustLine)
      .tfTransferable(tfTransferable)
      .build();

    assertThat(flags.getValue())
      .isEqualTo(getExpectedFlags(tfFullyCanonicalSig,tfBurnable, tfOnlyXRP, tfTrustLine, tfTransferable));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testDeriveIndividualFlagsFromFlags(
    boolean tfFullyCanonicalSig,
    boolean tfBurnable,
    boolean tfOnlyXRP,
    boolean tfTrustLine,
    boolean tfTransferable
  ) {
    long expectedFlags = getExpectedFlags(tfFullyCanonicalSig,tfBurnable, tfOnlyXRP, tfTrustLine, tfTransferable);
    Flags.NfTokenMintFlags flags = Flags.NfTokenMintFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.tfFullyCanonicalSig()).isEqualTo(tfFullyCanonicalSig);
    assertThat(flags.tfBurnable()).isEqualTo(tfBurnable);
    assertThat(flags.tfOnlyXRP()).isEqualTo(tfOnlyXRP);
    assertThat(flags.tfTrustLine()).isEqualTo(tfTrustLine);
    assertThat(flags.tfTransferable()).isEqualTo(tfTransferable);
  }

  private long getExpectedFlags(
    boolean tfFullyCanonicalSig,
    boolean tfBurnable,
    boolean tfOnlyXRP,
    boolean tfTrustLine,
    boolean tfTransferable
  ) {
    return (tfFullyCanonicalSig ? Flags.NfTokenMintFlags.FULLY_CANONICAL_SIG.getValue() : 0L) |
      (tfBurnable ? Flags.NfTokenMintFlags.BURNABLE.getValue() : 0L) |
      (tfOnlyXRP ? Flags.NfTokenMintFlags.ONLY_XRP.getValue() : 0L) |
      (tfTrustLine ? Flags.NfTokenMintFlags.TRUSTLINE.getValue() : 0L) |
      (tfTransferable ? Flags.NfTokenMintFlags.TRANSFERABLE.getValue() : 0L);
  }
}
