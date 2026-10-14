package org.xrpl.xrpl4j.model.jackson.modules;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.example.consumer.ConsumerNamedType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.transactions.Memo;

/**
 * Unit tests for {@link Xrpl4jAnnotationIntrospector}, installed by {@link Xrpl4jModule}. The introspector gives
 * xrpl4j types the serialization inclusion and unknown-property leniency they need, without changing those behaviors
 * for any other type handled by the same mapper.
 */
class Xrpl4jAnnotationIntrospectorTest {

  /**
   * A mapper with Jackson's defaults plus the xrpl4j module. No mapper-wide inclusion or leniency is configured.
   */
  private final ObjectMapper objectMapper = JsonMapper.builder()
    .addModule(new Jdk8Module())
    .addModule(new Xrpl4jModule())
    .build();

  @Test
  void xrpl4jTypeOmitsEmptyOptionals() throws JsonProcessingException {
    Memo memo = Memo.builder().memoData("ABC").build();

    assertThat(objectMapper.writeValueAsString(memo)).isEqualTo("{\"MemoData\":\"ABC\"}");
  }

  @Test
  void xrpl4jTypeIgnoresUnknownProperties() throws JsonProcessingException {
    Memo memo = objectMapper.readValue("{\"MemoData\":\"ABC\",\"AddedByFutureAmendment\":1}", Memo.class);

    assertThat(memo).isEqualTo(Memo.builder().memoData("ABC").build());
  }

  @Test
  void otherTypeKeepsNullProperties() throws JsonProcessingException {
    ConsumerNamedType consumerType = new ConsumerNamedType();

    assertThat(objectMapper.writeValueAsString(consumerType)).isEqualTo("{\"name\":null}");
  }

  @Test
  void otherTypeFailsOnUnknownProperties() {
    assertThatThrownBy(() -> objectMapper.readValue("{\"name\":\"x\",\"typo\":1}", ConsumerNamedType.class))
      .isInstanceOf(UnrecognizedPropertyException.class);
  }
}
