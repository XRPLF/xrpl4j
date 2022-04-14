package org.xrpl.xrpl4j.model.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class FinalityTest {

  @Test
  public void createFinalityObjectTest() {

    Finality finality = Finality.builder()
      .finalityStatus(FinalityStatus.NOT_FINAL)
      .resultCode("tesSUCCESS")
      .build();

    assertThat(finality.finalityStatus()).isEqualTo(FinalityStatus.NOT_FINAL);
    assertThat(finality.resultCode().get()).isEqualTo("tesSUCCESS");
    assertThat(finality.resultCodeSafe()).isEqualTo("tesSUCCESS");
  }

  @Test
  public void throwForMissingResultCodeTest() {

    Finality finality = Finality.builder()
      .finalityStatus(FinalityStatus.NOT_FINAL)
      .build();

    assertThat(finality.finalityStatus()).isEqualTo(FinalityStatus.NOT_FINAL);
    assertThrows(IllegalStateException.class, () -> finality.resultCodeSafe());
  }
}
