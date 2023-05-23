package org.xrpl.xrpl4j.model.client.accounts;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.signing.Signature;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.OfferCancel;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmountTest;

class XrplWebSocketTransactionResultTest {

  @Test
  void testJson() throws JsonProcessingException {
    String json = "{\n" +
        "  \"status\": \"closed\",\n" +
        "  \"type\": \"transaction\",\n" +
        "  \"engine_result\": \"tesSUCCESS\",\n" +
        "  \"engine_result_code\": 0,\n" +
        "  \"engine_result_message\": \"The transaction was applied.\",\n" +
        "  \"ledger_hash\": \"989AFBFD65D820C6BD85301B740F5D592F060668A90EEF5EC1815EBA27D58FE8\",\n" +
        "  \"ledger_index\": 7125442,\n" +
        "  \"meta\": {\n" +
        "    \"AffectedNodes\": [\n" +
        "      {\n" +
        "        \"ModifiedNode\": {\n" +
        "          \"FinalFields\": {\n" +
        "            \"Flags\": 0,\n" +
        "            \"IndexPrevious\": \"0000000000000000\",\n" +
        "            \"Owner\": \"rRh634Y6QtoqkwTTrGzX66UYoCAvgE6jL\",\n" +
        "            \"RootIndex\": \"ABD8CE2D1205D0C062876E9E1F3CBDC902ED8EF4E8D3D071B962C7ED0E113E68\"\n" +
        "          },\n" +
        "          \"LedgerEntryType\": \"DirectoryNode\",\n" +
        "          \"LedgerIndex\": \"0BBDEE7D0BE120F7BF27640B5245EBFE0C5FD5281988BA823C44477A70262A4D\"\n" +
        "        }\n" +
        "      },\n" +
        "      {\n" +
        "        \"DeletedNode\": {\n" +
        "          \"FinalFields\": {\n" +
        "            \"Account\": \"rRh634Y6QtoqkwTTrGzX66UYoCAvgE6jL\",\n" +
        "            \"BookDirectory\": \"892E892DC63D8F70DCF5C9ECF29394FF7DD3DC6F47DB8EB34A03920BFC5E99BE\",\n" +
        "            \"BookNode\": \"0000000000000000\",\n" +
        "            \"Flags\": 0,\n" +
        "            \"OwnerNode\": \"000000000000006E\",\n" +
        "            \"PreviousTxnID\": \"58A17D95770F8D07E08B81A85896F4032A328B6C2BDCDEC0A00F3EF3914DCF0A\",\n" +
        "            \"PreviousTxnLgrSeq\": 7125330,\n" +
        "            \"Sequence\": 540691,\n" +
        "            \"TakerGets\": \"4401967683\",\n" +
        "            \"TakerPays\": {\n" +
        "              \"currency\": \"BTC\",\n" +
        "              \"issuer\": \"rNPRNzBB92BVpAhhZr4iXDTveCgV5Pofm9\",\n" +
        "              \"value\": \"0.04424\"\n" +
        "            }\n" +
        "          },\n" +
        "          \"LedgerEntryType\": \"Offer\",\n" +
        "          \"LedgerIndex\": \"386B7803A9210747941B0D079BB408F31ACB1CB98832184D0287A1CBF4FE6D00\"\n" +
        "        }\n" +
        "      },\n" +
        "      {\n" +
        "        \"DeletedNode\": {\n" +
        "          \"FinalFields\": {\n" +
        "            \"ExchangeRate\": \"4A03920BFC5E99BE\",\n" +
        "            \"Flags\": 0,\n" +
        "            \"RootIndex\": \"892E892DC63D8F70DCF5C9ECF29394FF7DD3DC6F47DB8EB34A03920BFC5E99BE\",\n" +
        "            \"TakerGetsCurrency\": \"0000000000000000000000000000000000000000\",\n" +
        "            \"TakerGetsIssuer\": \"0000000000000000000000000000000000000000\",\n" +
        "            \"TakerPaysCurrency\": \"0000000000000000000000004254430000000000\",\n" +
        "            \"TakerPaysIssuer\": \"92D705968936C419CE614BF264B5EEB1CEA47FF4\"\n" +
        "          },\n" +
        "          \"LedgerEntryType\": \"DirectoryNode\",\n" +
        "          \"LedgerIndex\": \"892E892DC63D8F70DCF5C9ECF29394FF7DD3DC6F47DB8EB34A03920BFC5E99BE\"\n" +
        "        }\n" +
        "      },\n" +
        "      {\n" +
        "        \"ModifiedNode\": {\n" +
        "          \"FinalFields\": {\n" +
        "            \"Account\": \"rRh634Y6QtoqkwTTrGzX66UYoCAvgE6jL\",\n" +
        "            \"Balance\": \"11133297300\",\n" +
        "            \"Flags\": 0,\n" +
        "            \"OwnerCount\": 9,\n" +
        "            \"Sequence\": 540706\n" +
        "          },\n" +
        "          \"LedgerEntryType\": \"AccountRoot\",\n" +
        "          \"LedgerIndex\": \"A6C2532E1008A513B3F822A92B8E5214BD0D413DC20AD3631C1A39AD6B36CD07\",\n" +
        "          \"PreviousFields\": {\n" +
        "            \"Balance\": \"11133297310\",\n" +
        "            \"OwnerCount\": 10,\n" +
        "            \"Sequence\": 540705\n" +
        "          },\n" +
        "          \"PreviousTxnID\": \"484D57DFC4E446DA83B4540305F0CE836D4E007361542EC12CC0FFB5F0A1BE3A\",\n" +
        "          \"PreviousTxnLgrSeq\": 7125358\n" +
        "        }\n" +
        "      }\n" +
        "    ],\n" +
        "    \"TransactionIndex\": 1,\n" +
        "    \"TransactionResult\": \"tesSUCCESS\"\n" +
        "  },\n" +
        "  \"transaction\": {\n" +
        "    \"Account\": \"rRh634Y6QtoqkwTTrGzX66UYoCAvgE6jL\",\n" +
        "    \"Fee\": \"10\",\n" +
        "    \"Flags\": 2147483648,\n" +
        "    \"OfferSequence\": 540691,\n" +
        "    \"Sequence\": 540705,\n" +
        "    \"SigningPubKey\": \"030BB49C591C9CD65C945D4B78332F27633D7771E6CF4D4B942D26BA40748BB8B4\",\n" +
        "    \"TransactionType\": \"OfferCancel\",\n" +
        "    \"TxnSignature\": \"30450221008223604A383F3AED25D53CE7C874700619893A6EEE4336508312217850A9722302205E0614366E174F2DFF78B879F310DB0B3F6DA1967E52A32F65E25DCEC622CD68\",\n" +
        "    \"date\": 455751680,\n" +
        "    \"hash\": \"94CF924C774DFDBE474A2A7E40AEA70E7E15D130C8CBEF8AF1D2BE97A8269F14\"\n" +
        "  },\n" +
        "  \"validated\": true\n" +
        "}";

    XrplWebSocketTransactionResult expected = XrplWebSocketTransactionResult.builder()
        .status("tesSUCCESS")
        .ledgerIndex(UnsignedInteger.valueOf(7125442))
        .transaction(
            XrplWebSocketTransaction.builder()
                .transaction(
                    OfferCancel.builder()
                        .account(Address.of("rRh634Y6QtoqkwTTrGzX66UYoCAvgE6jL"))
                        .fee(XrpCurrencyAmount.ofDrops(10))
                        .offerSequence(UnsignedInteger.valueOf(540691))
                        .sequence(UnsignedInteger.valueOf(540705))
                        .signingPublicKey(PublicKey.fromBase16EncodedPublicKey("030BB49C591C9CD65C945D4B78332F27633D7771E6CF4D4B942D26BA40748BB8B4"))
                        .transactionSignature(Signature.fromBase16("30450221008223604A383F3AED25D53CE7C874700619893A6EEE4336508312217850A9722302205E0614366E174F2DFF78B879F310DB0B3F6DA1967E52A32F65E25DCEC622CD68"))
                        .build()
                )
                .hash(Hash256.of("94CF924C774DFDBE474A2A7E40AEA70E7E15D130C8CBEF8AF1D2BE97A8269F14"))
                .build()
        )
        .build();

    ObjectMapper objectMapper = ObjectMapperFactory.create();
    XrplWebSocketTransactionResult deserialized = objectMapper.readValue(json, XrplWebSocketTransactionResult.class);
    assertThat(deserialized).isEqualTo(expected);
  }
}