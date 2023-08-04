package org.xrpl.xrpl4j.model.client.nft;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.flags.NfTokenFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;
import org.xrpl.xrpl4j.model.transactions.NfTokenUri;
import org.xrpl.xrpl4j.model.transactions.TransferFee;

class NftInfoResultTest extends AbstractJsonTest {

  @Test
  void testJsonWithUri() throws JSONException, JsonProcessingException {
    NftInfoResult result = NftInfoResult.builder()
      .nftId(NfTokenId.of("00080000B4F4AFC5FBCBD76873F18006173D2193467D3EE70000099B00000000"))
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(269)))
      .owner(Address.of("rG9gdNygQ6npA9JvDFWBoeXbiUcTYJnEnk"))
      .burned(false)
      .flags(NfTokenFlags.TRANSFERABLE)
      .transferFee(TransferFee.of(UnsignedInteger.ZERO))
      .issuer(Address.of("rHVokeuSnjPjz718qdb47bGXBBHNMP3KDQ"))
      .nftTaxon(UnsignedLong.ZERO)
      .nftSerial(UnsignedInteger.ZERO)
      .uri(NfTokenUri.of("https://xrpl.org"))
      .status("success")
      .build();

    String json = "{\n" +
      "    \"nft_id\": \"00080000B4F4AFC5FBCBD76873F18006173D2193467D3EE70000099B00000000\",\n" +
      "    \"ledger_index\": 269,\n" +
      "    \"owner\": \"rG9gdNygQ6npA9JvDFWBoeXbiUcTYJnEnk\",\n" +
      "    \"is_burned\": false,\n" +
      "    \"flags\": 8,\n" +
      "    \"transfer_fee\": 0,\n" +
      "    \"issuer\": \"rHVokeuSnjPjz718qdb47bGXBBHNMP3KDQ\",\n" +
      "    \"nft_taxon\": 0,\n" +
      "    \"nft_serial\": 0,\n" +
      "    \"uri\": \"https://xrpl.org\",\n" +
      "    \"status\": \"success\"\n" +
      "  }";

    assertCanSerializeAndDeserialize(result, json);
  }

  @Test
  void testJsonWithoutUri() throws JSONException, JsonProcessingException {
    NftInfoResult result = NftInfoResult.builder()
      .nftId(NfTokenId.of("00080000B4F4AFC5FBCBD76873F18006173D2193467D3EE70000099B00000000"))
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(269)))
      .owner(Address.of("rG9gdNygQ6npA9JvDFWBoeXbiUcTYJnEnk"))
      .burned(false)
      .flags(NfTokenFlags.TRANSFERABLE)
      .transferFee(TransferFee.of(UnsignedInteger.ZERO))
      .issuer(Address.of("rHVokeuSnjPjz718qdb47bGXBBHNMP3KDQ"))
      .nftTaxon(UnsignedLong.ZERO)
      .nftSerial(UnsignedInteger.ZERO)
      .status("success")
      .build();

    String json = "{\n" +
      "    \"nft_id\": \"00080000B4F4AFC5FBCBD76873F18006173D2193467D3EE70000099B00000000\",\n" +
      "    \"ledger_index\": 269,\n" +
      "    \"owner\": \"rG9gdNygQ6npA9JvDFWBoeXbiUcTYJnEnk\",\n" +
      "    \"is_burned\": false,\n" +
      "    \"flags\": 8,\n" +
      "    \"transfer_fee\": 0,\n" +
      "    \"issuer\": \"rHVokeuSnjPjz718qdb47bGXBBHNMP3KDQ\",\n" +
      "    \"nft_taxon\": 0,\n" +
      "    \"nft_serial\": 0,\n" +
      "    \"status\": \"success\"\n" +
      "  }";

    assertCanSerializeAndDeserialize(result, json);
  }
}