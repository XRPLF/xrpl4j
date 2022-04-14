package org.xrpl.xrpl4j.model.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link FinalityStatus}.
 */
public class FinalityStatusTest {

  @Test
  public void finalityStatusValuesTest() {
    FinalityStatus finalityStatus = FinalityStatus.EXPIRED;
    assertThat(finalityStatus).isEqualTo(FinalityStatus.EXPIRED);
    assertThat(finalityStatus).isNotEqualTo(FinalityStatus.NOT_FINAL);
    assertThat(finalityStatus).isNotEqualTo(FinalityStatus.VALIDATED_FAILURE);
    assertThat(finalityStatus).isNotEqualTo(FinalityStatus.VALIDATED_SUCCESS);
    assertThat(finalityStatus).isNotEqualTo(FinalityStatus.VALIDATED_UNKNOWN);
    assertThat(finalityStatus).isNotEqualTo(FinalityStatus.EXPIRED_WITH_SPENT_ACCOUNT_SEQUENCE);
  }
}
