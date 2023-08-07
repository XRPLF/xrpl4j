package org.xrpl.xrpl4j.model.client.nft;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;

class NftInfoRequestParamsTest extends AbstractJsonTest {

  @Test
  void testWithLedgerIndexShortcut() throws JSONException, JsonProcessingException {
    NftInfoRequestParams params = NftInfoRequestParams.builder()
      .nfTokenId(NfTokenId.of("000100001E962F495F07A990F4ED55ACCFEEF365DBAA76B6A048C0A200000007"))
      .ledgerSpecifier(LedgerSpecifier.VALIDATED)
      .build();

    String json = "{\n" +
      "          \"nft_id\": \"000100001E962F495F07A990F4ED55ACCFEEF365DBAA76B6A048C0A200000007\",\n" +
      "          \"ledger_index\": \"validated\"\n" +
      "      }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  void testWithLedgerIndexNumber() throws JSONException, JsonProcessingException {
    NftInfoRequestParams params = NftInfoRequestParams.builder()
      .nfTokenId(NfTokenId.of("000100001E962F495F07A990F4ED55ACCFEEF365DBAA76B6A048C0A200000007"))
      .ledgerSpecifier(LedgerSpecifier.of(100))
      .build();

    String json = "{\n" +
      "          \"nft_id\": \"000100001E962F495F07A990F4ED55ACCFEEF365DBAA76B6A048C0A200000007\",\n" +
      "          \"ledger_index\": 100\n" +
      "      }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  void testWithLedgerHash() throws JSONException, JsonProcessingException {
    NftInfoRequestParams params = NftInfoRequestParams.builder()
      .nfTokenId(NfTokenId.of("000100001E962F495F07A990F4ED55ACCFEEF365DBAA76B6A048C0A200000007"))
      .ledgerSpecifier(
        LedgerSpecifier.of(Hash256.of("C53ECF838647FA5A4C780377025FEC7999AB4182590510CA461444B207AB74A9"))
      )
      .build();

    String json = "{\n" +
      "          \"nft_id\": \"000100001E962F495F07A990F4ED55ACCFEEF365DBAA76B6A048C0A200000007\",\n" +
      "          \"ledger_hash\": \"C53ECF838647FA5A4C780377025FEC7999AB4182590510CA461444B207AB74A9\"\n" +
      "      }";

    assertCanSerializeAndDeserialize(params, json);
  }
}