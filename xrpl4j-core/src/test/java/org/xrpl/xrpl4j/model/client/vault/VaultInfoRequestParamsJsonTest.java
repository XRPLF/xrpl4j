package org.xrpl.xrpl4j.model.client.vault;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;

/**
 * JSON tests for {@link VaultInfoRequestParams}.
 */
public class VaultInfoRequestParamsJsonTest extends AbstractJsonTest {

  @Test
  public void testJsonWithVaultId() throws JSONException, JsonProcessingException {
    VaultInfoRequestParams params = VaultInfoRequestParams.of(
      Hash256.of("1234567890ABCDEF1234567890ABCDEF1234567890ABCDEF1234567890ABCDEF")
    );

    String json = "{" +
      "  \"vault_id\": \"1234567890ABCDEF1234567890ABCDEF1234567890ABCDEF1234567890ABCDEF\"" +
      "}";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testJsonWithOwnerAndSeq() throws JSONException, JsonProcessingException {
    VaultInfoRequestParams params = VaultInfoRequestParams.of(
      Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMgk5j"),
      UnsignedInteger.valueOf(123)
    );

    String json = "{" +
      "  \"owner\": \"rN7n7otQDd6FczFgLdlqtyMVrn3HMgk5j\"," +
      "  \"seq\": 123" +
      "}";

    assertCanSerializeAndDeserialize(params, json);
  }
}

