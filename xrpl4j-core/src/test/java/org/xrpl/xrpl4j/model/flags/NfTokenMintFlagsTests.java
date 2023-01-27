

package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

@SuppressWarnings("abbreviationaswordinname")
public class NfTokenMintFlagsTests extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(4);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testFlagsConstructionWithIndividualFlags(
    boolean tfBurnable,
    boolean tfOnlyXRP,
    boolean tfTrustLine,
    boolean tfTransferable
  ) {
    NfTokenMintFlags flags = NfTokenMintFlags.builder()
      .tfBurnable(tfBurnable)
      .tfOnlyXRP(tfOnlyXRP)
      .tfTrustLine(tfTrustLine)
      .tfTransferable(tfTransferable)
      .build();

    assertThat(flags.getValue())
      .isEqualTo(getExpectedFlags(tfBurnable, tfOnlyXRP, tfTrustLine, tfTransferable));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testDeriveIndividualFlagsFromFlags(
    boolean tfBurnable,
    boolean tfOnlyXRP,
    boolean tfTrustLine,
    boolean tfTransferable
  ) {
    long expectedFlags = getExpectedFlags(tfBurnable, tfOnlyXRP, tfTrustLine, tfTransferable);
    NfTokenMintFlags flags = NfTokenMintFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.tfFullyCanonicalSig()).isEqualTo(true);
    assertThat(flags.tfBurnable()).isEqualTo(tfBurnable);
    assertThat(flags.tfOnlyXRP()).isEqualTo(tfOnlyXRP);
    assertThat(flags.tfTrustLine()).isEqualTo(tfTrustLine);
    assertThat(flags.tfTransferable()).isEqualTo(tfTransferable);
  }

  private long getExpectedFlags(
    boolean tfBurnable,
    boolean tfOnlyXRP,
    boolean tfTrustLine,
    boolean tfTransferable
  ) {
    return (NfTokenMintFlags.FULLY_CANONICAL_SIG.getValue()) |
      (tfBurnable ? NfTokenMintFlags.BURNABLE.getValue() : 0L) |
      (tfOnlyXRP ? NfTokenMintFlags.ONLY_XRP.getValue() : 0L) |
      (tfTrustLine ? NfTokenMintFlags.TRUSTLINE.getValue() : 0L) |
      (tfTransferable ? NfTokenMintFlags.TRANSFERABLE.getValue() : 0L);
  }
}

