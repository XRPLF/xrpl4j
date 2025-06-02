package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

public class CredentialFlagsTest extends AbstractFlagsTest {

  @Test
  void testFlagWithValue() {
    CredentialFlags flags = CredentialFlags.ACCEPTED;
    assertThat(flags.isEmpty()).isFalse();

    assertThat(flags.lsfAccepted()).isTrue();
    assertThat(flags.getValue()).isEqualTo(65536L);
  }

  @Test
  void testJson() throws JSONException, JsonProcessingException {
    FlagsWrapper wrapper = FlagsWrapper.of(CredentialFlags.ACCEPTED);
    String json = String.format("{\n" +
                                "               \"flags\": %s\n" +
                                "}", CredentialFlags.ACCEPTED.getValue());

    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testUnsetJson() throws JSONException, JsonProcessingException {
    FlagsWrapper wrapper = FlagsWrapper.of(CredentialFlags.UNSET);
    String json = String.format("{\n" +
                                "               \"flags\": %s\n" +
                                "}", CredentialFlags.UNSET.getValue());

    assertCanSerializeAndDeserialize(wrapper, json);
  }

}
