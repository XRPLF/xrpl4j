package org.xrpl.xrpl4j.model.transactions.json;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.Before;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.transactions.Transaction;

public class AbstractJsonTest {

  protected ObjectMapper objectMapper;

  @Before
  public void setUp() {
    objectMapper = ObjectMapperFactory.create();
  }

  protected void assertCanSerializeAndDeserialize(
      Transaction transaction,
      String json
  ) throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(transaction);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);

    Transaction deserialized = objectMapper.readValue(serialized, transaction.getClass());
    assertThat(deserialized).isEqualTo(transaction);
  }
}
