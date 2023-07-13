package org.xrpl.xrpl4j.model.client.path;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.flags.OfferFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

class BookOffersResultTest extends AbstractJsonTest {

  @Test
  void testJsonWithLedgerIndexAndHash() throws JSONException, JsonProcessingException {
    BookOffersResult result = BookOffersResult.builder()
      .ledgerHash(Hash256.of("6442396558D5EF9E64441A29A39759B52813B5E18B6AD86A602A36815037B98B"))
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(80959235)))
      .status("success")
      .addOffers(
        BookOffersOffer.builder()
          .account(Address.of("rBTwLga3i2gz3doX6Gva3MgEV8ZCD8jjah"))
          .bookDirectory(Hash256.of("DFA3B6DDAB58C7E8E5D944E736DA4B7046C30E4F460FD9DE4E10925C2010F800"))
          .bookNode("0")
          .flags(OfferFlags.of(0))
          .ownerNode("0")
          .previousTransactionId(Hash256.of("606B3F8835E2B126CA1C331B2A8BE1C7DCC72B07610AF7B8B12E841B815B2F2C"))
          .previousTransactionLedgerSequence(UnsignedInteger.valueOf(80959234))
          .sequence(UnsignedInteger.valueOf(99235111))
          .takerGets(XrpCurrencyAmount.ofDrops(28703000000L))
          .takerPays(
            IssuedCurrencyAmount.builder()
              .currency("USD")
              .issuer(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B"))
              .value("13388.5832372")
              .build()
          )
          .index(Hash256.of("E72DCD2A088C8E1843100A345D636D6A017796037E83B47F2C7E0350B53A8374"))
          .ownerFundsString("28767697713")
          .qualityString("0.0000004664524")
          .build(),
        BookOffersOffer.builder()
          .account(Address.of("rPbMHxs7vy5t6e19tYfqG7XJ6Fog8EPZLk"))
          .bookDirectory(Hash256.of("DFA3B6DDAB58C7E8E5D944E736DA4B7046C30E4F460FD9DE4E109266A03C5B00"))
          .bookNode("0")
          .flags(OfferFlags.of(0))
          .ownerNode("0")
          .previousTransactionId(Hash256.of("3E8D2F2CC7593C40FF8EC2A5316B7E308473DC90B50CC54AD4D487E4E6515545"))
          .previousTransactionLedgerSequence(UnsignedInteger.valueOf(80959227))
          .sequence(UnsignedInteger.valueOf(1400403))
          .takerGets(XrpCurrencyAmount.ofDrops(2500000000L))
          .takerPays(
            IssuedCurrencyAmount.builder()
              .currency("USD")
              .issuer(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B"))
              .value("1166.142275")
              .build()
          )
          .index(Hash256.of("D17EA19846F265BD1244F2864B85ECDB7C86D462C00C8B658A035358BDDD6DE2"))
          .ownerFundsString("3389276844")
          .qualityString("0.00000046645691")
          .build()
      )
      .build();

    String json = "{\n" +
      "        \"ledger_hash\": \"6442396558D5EF9E64441A29A39759B52813B5E18B6AD86A602A36815037B98B\",\n" +
      "        \"ledger_index\": 80959235,\n" +
      "        \"offers\": [\n" +
      "            {\n" +
      "                \"Account\": \"rBTwLga3i2gz3doX6Gva3MgEV8ZCD8jjah\",\n" +
      "                \"BookDirectory\": \"DFA3B6DDAB58C7E8E5D944E736DA4B7046C30E4F460FD9DE4E10925C2010F800\",\n" +
      "                \"BookNode\": \"0\",\n" +
      "                \"Flags\": 0,\n" +
      "                \"LedgerEntryType\": \"Offer\",\n" +
      "                \"OwnerNode\": \"0\",\n" +
      "                \"PreviousTxnID\": \"606B3F8835E2B126CA1C331B2A8BE1C7DCC72B07610AF7B8B12E841B815B2F2C\",\n" +
      "                \"PreviousTxnLgrSeq\": 80959234,\n" +
      "                \"Sequence\": 99235111,\n" +
      "                \"TakerGets\": \"28703000000\",\n" +
      "                \"TakerPays\": {\n" +
      "                    \"currency\": \"USD\",\n" +
      "                    \"issuer\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B\",\n" +
      "                    \"value\": \"13388.5832372\"\n" +
      "                },\n" +
      "                \"index\": \"E72DCD2A088C8E1843100A345D636D6A017796037E83B47F2C7E0350B53A8374\",\n" +
      "                \"owner_funds\": \"28767697713\",\n" +
      "                \"quality\": \"0.0000004664524\"\n" +
      "            },\n" +
      "            {\n" +
      "                \"Account\": \"rPbMHxs7vy5t6e19tYfqG7XJ6Fog8EPZLk\",\n" +
      "                \"BookDirectory\": \"DFA3B6DDAB58C7E8E5D944E736DA4B7046C30E4F460FD9DE4E109266A03C5B00\",\n" +
      "                \"BookNode\": \"0\",\n" +
      "                \"Flags\": 0,\n" +
      "                \"LedgerEntryType\": \"Offer\",\n" +
      "                \"OwnerNode\": \"0\",\n" +
      "                \"PreviousTxnID\": \"3E8D2F2CC7593C40FF8EC2A5316B7E308473DC90B50CC54AD4D487E4E6515545\",\n" +
      "                \"PreviousTxnLgrSeq\": 80959227,\n" +
      "                \"Sequence\": 1400403,\n" +
      "                \"TakerGets\": \"2500000000\",\n" +
      "                \"TakerPays\": {\n" +
      "                    \"currency\": \"USD\",\n" +
      "                    \"issuer\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B\",\n" +
      "                    \"value\": \"1166.142275\"\n" +
      "                },\n" +
      "                \"index\": \"D17EA19846F265BD1244F2864B85ECDB7C86D462C00C8B658A035358BDDD6DE2\",\n" +
      "                \"owner_funds\": \"3389276844\",\n" +
      "                \"quality\": \"0.00000046645691\"\n" +
      "            }\n" +
      "        ],\n" +
      "        \"status\": \"success\"\n" +
      "    }";

    assertCanSerializeAndDeserialize(result, json);
  }

  @Test
  void testJsonWithLedgerCurrentIndex() throws JSONException, JsonProcessingException {
    BookOffersResult result = BookOffersResult.builder()
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(80960755)))
      .status("success")
      .addOffers(
        BookOffersOffer.builder()
          .account(Address.of("rwsixWy8srCoeUMqABYGM3ayZX55jdVZW4"))
          .bookDirectory(Hash256.of("DFA3B6DDAB58C7E8E5D944E736DA4B7046C30E4F460FD9DE4E11C37937E08000"))
          .bookNode("0")
          .expiration(UnsignedInteger.valueOf(772482259))
          .flags(OfferFlags.of(131072))
          .ownerNode("0")
          .previousTransactionId(Hash256.of("BAC9508ECC0656A0F2B5D3B02A96F34F812DC0C37D477D14D59393A8DA6DB378"))
          .previousTransactionLedgerSequence(UnsignedInteger.valueOf(80670980))
          .sequence(UnsignedInteger.valueOf(80649129))
          .takerGets(XrpCurrencyAmount.ofDrops(37716709495L))
          .takerPays(
            IssuedCurrencyAmount.builder()
              .currency("USD")
              .issuer(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B"))
              .value("18858.3547475")
              .build()
          )
          .index(Hash256.of("B3BD47F0A8C545E7AB213938E0801078C004B5C7364E7A789DE3D83BA3B6E6B1"))
          .ownerFundsString("41272254")
          .qualityString("0.0000005")
          .takerGetsFunded(XrpCurrencyAmount.ofDrops(41272254))
          .takerPaysFunded(
            IssuedCurrencyAmount.builder()
              .currency("USD")
              .issuer(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B"))
              .value("20.636127")
              .build()
          )
          .build()
      )
      .build();

    String json = "{\n" +
      "        \"ledger_current_index\": 80960755,\n" +
      "        \"offers\": [\n" +
      "            {\n" +
      "                \"Account\": \"rwsixWy8srCoeUMqABYGM3ayZX55jdVZW4\",\n" +
      "                \"BookDirectory\": \"DFA3B6DDAB58C7E8E5D944E736DA4B7046C30E4F460FD9DE4E11C37937E08000\",\n" +
      "                \"BookNode\": \"0\",\n" +
      "                \"Expiration\": 772482259,\n" +
      "                \"Flags\": 131072,\n" +
      "                \"LedgerEntryType\": \"Offer\",\n" +
      "                \"OwnerNode\": \"0\",\n" +
      "                \"PreviousTxnID\": \"BAC9508ECC0656A0F2B5D3B02A96F34F812DC0C37D477D14D59393A8DA6DB378\",\n" +
      "                \"PreviousTxnLgrSeq\": 80670980,\n" +
      "                \"Sequence\": 80649129,\n" +
      "                \"TakerGets\": \"37716709495\",\n" +
      "                \"TakerPays\": {\n" +
      "                    \"currency\": \"USD\",\n" +
      "                    \"issuer\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B\",\n" +
      "                    \"value\": \"18858.3547475\"\n" +
      "                },\n" +
      "                \"index\": \"B3BD47F0A8C545E7AB213938E0801078C004B5C7364E7A789DE3D83BA3B6E6B1\",\n" +
      "                \"owner_funds\": \"41272254\",\n" +
      "                \"quality\": \"0.0000005\",\n" +
      "                \"taker_gets_funded\": \"41272254\",\n" +
      "                \"taker_pays_funded\": {\n" +
      "                    \"currency\": \"USD\",\n" +
      "                    \"issuer\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B\",\n" +
      "                    \"value\": \"20.636127\"\n" +
      "                }\n" +
      "            }\n" +
      "        ],\n" +
      "        \"status\": \"success\"\n" +
      "    }";

    assertCanSerializeAndDeserialize(result, json);
  }

  @Test
  void testLedgerHashSafe() {
    BookOffersResult result = BookOffersResult.builder()
      .ledgerHash(Hash256.of("6442396558D5EF9E64441A29A39759B52813B5E18B6AD86A602A36815037B98B"))
      .status("success")
      .build();

    assertThat(result.ledgerHash()).isNotEmpty().get().isEqualTo(result.ledgerHashSafe());

    BookOffersResult resultWithoutHash = BookOffersResult.builder()
      .status("success")
      .build();

    assertThatThrownBy(resultWithoutHash::ledgerHashSafe)
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Result did not contain a ledgerHash.");
  }

  @Test
  void testLedgerIndexSafe() {
    BookOffersResult result = BookOffersResult.builder()
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(80960755)))
      .status("success")
      .build();

    assertThat(result.ledgerIndex()).isNotEmpty().get().isEqualTo(result.ledgerIndexSafe());

    BookOffersResult resultWithoutLedgerIndex = BookOffersResult.builder()
      .status("success")
      .build();

    assertThatThrownBy(resultWithoutLedgerIndex::ledgerIndexSafe)
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Result did not contain a ledgerIndex.");
  }

  @Test
  void testLedgerCurrentIndexSafe() {
    BookOffersResult result = BookOffersResult.builder()
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(80960755)))
      .status("success")
      .build();

    assertThat(result.ledgerCurrentIndex()).isNotEmpty().get().isEqualTo(result.ledgerCurrentIndexSafe());

    BookOffersResult resultWithoutLedgerCurrentIndex = BookOffersResult.builder()
      .status("success")
      .build();

    assertThatThrownBy(resultWithoutLedgerCurrentIndex::ledgerCurrentIndexSafe)
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Result did not contain a ledgerCurrentIndex.");
  }
}