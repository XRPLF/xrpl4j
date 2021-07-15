package org.xrpl.xrpl4j.model.transactions.json;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.transactions.LedgerIndex;

public class LedgerIndexJsonTests extends AbstractJsonTest {

  @Test
  void testJsonValueIsNumber() throws JsonProcessingException, JSONException {
    LedgerIndex ledgerIndex = LedgerIndex.of(UnsignedLong.ONE);
    LedgerIndexWrapper ledgerIndexWrapper = LedgerIndexWrapper.of(ledgerIndex);

    String json = "{\"ledgerIndex\": 1}";
    String serialized = objectMapper.writeValueAsString(ledgerIndexWrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    LedgerIndexWrapper deserialized = objectMapper.readValue(serialized, LedgerIndexWrapper.class);
    assertThat(deserialized).isEqualTo(ledgerIndexWrapper);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableLedgerIndexWrapper.class)
  @JsonDeserialize(as = ImmutableLedgerIndexWrapper.class)
  interface LedgerIndexWrapper {

    static LedgerIndexWrapper of(LedgerIndex ledgerIndex) {
      return ImmutableLedgerIndexWrapper.builder().ledgerIndex(ledgerIndex).build();
    }

    LedgerIndex ledgerIndex();

  }
}
