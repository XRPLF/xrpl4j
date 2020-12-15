package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

@RunWith(Parameterized.class)
public class SignerListObjectFlagsTests extends AbstractFlagsTest {

  boolean lsfOneOwnerCount;

  long expectedFlags;

  public SignerListObjectFlagsTests(boolean lsfOneOwnerCount) {
    this.lsfOneOwnerCount = lsfOneOwnerCount;
    this.expectedFlags = lsfOneOwnerCount ? Flags.SignerListFlags.ONE_OWNER_COUNT.getValue() : 0L;
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return getBooleanCombinations(1);
  }

  @Test
  public void testFlagsConstructionWithIndividualFlags() {
    Flags.SignerListFlags flags = Flags.SignerListFlags.builder()
        .lsfOneOwnerCount(lsfOneOwnerCount)
        .build();

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
  }

  @Test
  public void testDeriveIndividualFlagsFromFlags() {
    Flags.SignerListFlags flags = Flags.SignerListFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.lsfOneOwnerCount()).isEqualTo(lsfOneOwnerCount);
  }
}
