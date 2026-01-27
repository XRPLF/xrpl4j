package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.assertj.core.api.Assertions;
import org.immutables.value.Value;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

/**
 * Unit tests for {@link NetworkId}.
 */
public class NetworkIdTest {

  private final NetworkId NETWORK_ID_ONE = NetworkId.of(UnsignedInteger.ONE);

  ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void testNull() {
    assertThrows(NullPointerException.class, () -> NetworkId.of(null));
  }

  @Test
  void testEquality() {
    assertThat(NETWORK_ID_ONE).isEqualTo(NETWORK_ID_ONE);
    assertThat(NETWORK_ID_ONE).isNotEqualTo(new Object());
    assertThat(NETWORK_ID_ONE.equals(null)).isFalse();
  }

  @Test
  void testBounds() {
    NetworkId networkId = NetworkId.of(UnsignedInteger.ZERO);
    assertThat(networkId.value()).isEqualTo(UnsignedInteger.ZERO);

    networkId = NetworkId.of(UnsignedInteger.MAX_VALUE);
    assertThat(networkId.value()).isEqualTo(UnsignedInteger.MAX_VALUE);
  }

  @Test
  void testToString() {
    NetworkId networkId = NetworkId.of(UnsignedInteger.valueOf(1024));
    assertThat(networkId.toString()).isEqualTo("1024");
    assertThat(networkId.equals(null)).isFalse();
  }

  @Test
  void testBoundsWithLongConstructor() {
    NetworkId networkId = NetworkId.of(0);
    assertThat(networkId.value()).isEqualTo(UnsignedInteger.ZERO);

    networkId = NetworkId.of(1);
    assertThat(networkId.value()).isEqualTo(UnsignedInteger.ONE);

    networkId = NetworkId.of(4_294_967_295L);
    assertThat(networkId.value()).isEqualTo(UnsignedInteger.MAX_VALUE);

    assertThatThrownBy(() -> NetworkId.of(4_294_967_296L))
      .isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(() -> NetworkId.of(-1))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void testJson() throws JsonProcessingException, JSONException {
    NetworkId networkId = NetworkId.of(UnsignedInteger.valueOf(1000));
    NetworkIdWrapper wrapper = NetworkIdWrapper.of(networkId);

    String json = "{\"networkId\": 1000}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  private void assertSerializesAndDeserializes(
    NetworkIdWrapper wrapper,
    String json
  ) throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    NetworkIdWrapper deserialized = objectMapper.readValue(
      serialized, NetworkIdWrapper.class
    );
    Assertions.assertThat(deserialized).isEqualTo(wrapper);
  }


  @Value.Immutable
  @JsonSerialize(as = ImmutableNetworkIdWrapper.class)
  @JsonDeserialize(as = ImmutableNetworkIdWrapper.class)
  interface NetworkIdWrapper {

    static NetworkIdWrapper of(NetworkId networkId) {
      return ImmutableNetworkIdWrapper.builder().networkId(networkId).build();
    }

    NetworkId networkId();

  }
}
