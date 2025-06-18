package org.xrpl.xrpl4j.model.client.ledger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.xrpl.xrpl4j.crypto.TestConstants.HASH_256;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.flags.AccountRootFlags;
import org.xrpl.xrpl4j.model.flags.CredentialFlags;
import org.xrpl.xrpl4j.model.flags.OfferFlags;
import org.xrpl.xrpl4j.model.flags.RippleStateFlags;
import org.xrpl.xrpl4j.model.ledger.AccountRootObject;
import org.xrpl.xrpl4j.model.ledger.AmmObject;
import org.xrpl.xrpl4j.model.ledger.AuctionSlot;
import org.xrpl.xrpl4j.model.ledger.CheckObject;
import org.xrpl.xrpl4j.model.ledger.CredentialObject;
import org.xrpl.xrpl4j.model.ledger.DepositPreAuthObject;
import org.xrpl.xrpl4j.model.ledger.EscrowObject;
import org.xrpl.xrpl4j.model.ledger.Issue;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.ledger.NfToken;
import org.xrpl.xrpl4j.model.ledger.NfTokenPageObject;
import org.xrpl.xrpl4j.model.ledger.NfTokenWrapper;
import org.xrpl.xrpl4j.model.ledger.OfferObject;
import org.xrpl.xrpl4j.model.ledger.PayChannelObject;
import org.xrpl.xrpl4j.model.ledger.PermissionedDomainObject;
import org.xrpl.xrpl4j.model.ledger.RippleStateObject;
import org.xrpl.xrpl4j.model.ledger.TicketObject;
import org.xrpl.xrpl4j.model.ledger.VoteEntry;
import org.xrpl.xrpl4j.model.ledger.VoteEntryWrapper;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Credential;
import org.xrpl.xrpl4j.model.transactions.CredentialType;
import org.xrpl.xrpl4j.model.transactions.CredentialWrapper;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;
import org.xrpl.xrpl4j.model.transactions.NfTokenUri;
import org.xrpl.xrpl4j.model.transactions.TradingFee;
import org.xrpl.xrpl4j.model.transactions.VoteWeight;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class LedgerEntryResultTest extends AbstractJsonTest {

  @Test
  void testAccountRootResult() throws JSONException, JsonProcessingException {
    LedgerEntryResult<AccountRootObject> result = LedgerEntryResult.<AccountRootObject>builder()
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(83125250)))
      .ledgerHash(Hash256.of("783625588CF01BD3D0E9C2719B92098A6A87649AEFF5AE970CD68B911436C1D7"))
      .validated(true)
      .index(Hash256.of("13F1A95D7AAB7108D5CE7EEAF504B2894B8C674E6D68499076441C4837282BF8"))
      .node(
        AccountRootObject.builder()
          .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
          .accountTransactionId(Hash256.of("932CC7E9BAC1F7B9FA5381679F293EEC0A646E5E7F2F6D14C85FEE2102F0E66C"))
          .balance(XrpCurrencyAmount.ofDrops(1066107694))
          .domain("6D64756F31332E636F6D")
          .emailHash("98B4375E1D753E5B91627516F6D70977")
          .flags(AccountRootFlags.of(9568256))
          .messageKey("0000000000000000000000070000000300")
          .ownerCount(UnsignedInteger.valueOf(17))
          .previousTransactionId(Hash256.of("7E5F3FB60E1177F8AF8A9EAC7982F27FA5494FDEA871B23B4B149939A5A7A7BB"))
          .previousTransactionLedgerSequence(UnsignedInteger.valueOf(82357607))
          .regularKey(Address.of("rD9iJmieYHn8jTtPjwwkW2Wm9sVDvPXLoJ"))
          .sequence(UnsignedInteger.valueOf(393))
          .ticketCount(UnsignedInteger.valueOf(5))
          .transferRate(UnsignedInteger.valueOf(4294967295L))
          .index(Hash256.of("13F1A95D7AAB7108D5CE7EEAF504B2894B8C674E6D68499076441C4837282BF8"))
          .build()
      )
      .status("success")
      .build();

    String json = "{" +
      "  \"ledger_hash\": \"783625588CF01BD3D0E9C2719B92098A6A87649AEFF5AE970CD68B911436C1D7\"," +
      "  \"ledger_index\": 83125250," +
      "  \"validated\": true," +
      "  \"index\": \"13F1A95D7AAB7108D5CE7EEAF504B2894B8C674E6D68499076441C4837282BF8\"," +
      "  \"node\": {" +
      "    \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\"," +
      "    \"AccountTxnID\": \"932CC7E9BAC1F7B9FA5381679F293EEC0A646E5E7F2F6D14C85FEE2102F0E66C\"," +
      "    \"Balance\": \"1066107694\"," +
      "    \"Domain\": \"6D64756F31332E636F6D\"," +
      "    \"EmailHash\": \"98B4375E1D753E5B91627516F6D70977\"," +
      "    \"Flags\": 9568256," +
      "    \"LedgerEntryType\": \"AccountRoot\"," +
      "    \"MessageKey\": \"0000000000000000000000070000000300\"," +
      "    \"OwnerCount\": 17," +
      "    \"PreviousTxnID\": \"7E5F3FB60E1177F8AF8A9EAC7982F27FA5494FDEA871B23B4B149939A5A7A7BB\"," +
      "    \"PreviousTxnLgrSeq\": 82357607," +
      "    \"RegularKey\": \"rD9iJmieYHn8jTtPjwwkW2Wm9sVDvPXLoJ\"," +
      "    \"Sequence\": 393," +
      "    \"TicketCount\": 5," +
      "    \"TransferRate\": 4294967295," +
      "    \"index\": \"13F1A95D7AAB7108D5CE7EEAF504B2894B8C674E6D68499076441C4837282BF8\"" +
      "  }," +
      "  \"status\": \"success\"" +
      "}";

    assertCanSerializeAndDeserialize(result, json);
  }

  @Test
  void testAmmResult() throws JSONException, JsonProcessingException {
    LedgerEntryResult<AmmObject> result = LedgerEntryResult.<AmmObject>builder()
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(607272)))
      .ledgerHash(Hash256.of("EEB650A0FD3CF0A5CE68B3DBD67C902FEC85E6AFAE1D0A7A7AF4BAD2F38557C7"))
      .validated(true)
      .index(Hash256.of("6BCD7E451DDA015FB307DAD9208A98A2DC3AC4D1448E624B42C89246DCF08692"))
      .node(
        AmmObject.builder()
          .account(Address.of("rNqXnvSYbjZeJQ6jWcf6T5mnNMRPzHXaZW"))
          .asset(Issue.XRP)
          .asset2(
            Issue.builder()
              .currency("7872706C346A436F696E00000000000000000000")
              .issuer(Address.of("rDeo7rDoYw6AUKGneWwfkHPsMJagxcGWy1"))
              .build()
          )
          .auctionSlot(
            AuctionSlot.builder()
              .account(Address.of("rDeo7rDoYw6AUKGneWwfkHPsMJagxcGWy1"))
              .discountedFee(TradingFee.of(UnsignedInteger.valueOf(77)))
              .expiration(UnsignedInteger.valueOf(750359162))
              .price(
                IssuedCurrencyAmount.builder()
                  .currency("03DCF8F3910BFE6AB56136A90BD41E0902E23C4F")
                  .value("0")
                  .issuer(Address.of("rNqXnvSYbjZeJQ6jWcf6T5mnNMRPzHXaZW"))
                  .build()
              )
              .build()
          )
          .lpTokenBalance(
            IssuedCurrencyAmount.builder()
              .currency("03DCF8F3910BFE6AB56136A90BD41E0902E23C4F")
              .issuer(Address.of("rNqXnvSYbjZeJQ6jWcf6T5mnNMRPzHXaZW"))
              .value("70606.68056410846")
              .build()
          )
          .ownerNode("0")
          .tradingFee(TradingFee.of(UnsignedInteger.valueOf(778)))
          .addVoteSlots(
            VoteEntryWrapper.of(VoteEntry.builder()
              .account(Address.of("rDeo7rDoYw6AUKGneWwfkHPsMJagxcGWy1"))
              .tradingFee(TradingFee.of(UnsignedInteger.valueOf(1000)))
              .voteWeight(VoteWeight.of(UnsignedInteger.valueOf(70815)))
              .build()),
            VoteEntryWrapper.of(VoteEntry.builder()
              .account(Address.of("rHPoJo9R3QdQjK6XdWL5hY2eTc4wUeNYzW"))
              .tradingFee(TradingFee.of(UnsignedInteger.valueOf(240)))
              .voteWeight(VoteWeight.of(UnsignedInteger.valueOf(29185)))
              .build())
          )
          .index(Hash256.of("6BCD7E451DDA015FB307DAD9208A98A2DC3AC4D1448E624B42C89246DCF08692"))
          .build()
      )
      .status("success")
      .build();

    String json = "{" +
      "  \"index\": \"6BCD7E451DDA015FB307DAD9208A98A2DC3AC4D1448E624B42C89246DCF08692\"," +
      "  \"ledger_hash\": \"EEB650A0FD3CF0A5CE68B3DBD67C902FEC85E6AFAE1D0A7A7AF4BAD2F38557C7\"," +
      "  \"ledger_index\": 607272," +
      "  \"node\": {" +
      "    \"Account\": \"rNqXnvSYbjZeJQ6jWcf6T5mnNMRPzHXaZW\"," +
      "    \"Asset\": {" +
      "      \"currency\": \"XRP\"" +
      "    }," +
      "    \"Asset2\": {" +
      "      \"currency\": \"7872706C346A436F696E00000000000000000000\"," +
      "      \"issuer\": \"rDeo7rDoYw6AUKGneWwfkHPsMJagxcGWy1\"" +
      "    }," +
      "    \"AuctionSlot\": {" +
      "      \"Account\": \"rDeo7rDoYw6AUKGneWwfkHPsMJagxcGWy1\"," +
      "      \"DiscountedFee\": 77," +
      "      \"Expiration\": 750359162," +
      "      \"Price\": {" +
      "        \"currency\": \"03DCF8F3910BFE6AB56136A90BD41E0902E23C4F\"," +
      "        \"issuer\": \"rNqXnvSYbjZeJQ6jWcf6T5mnNMRPzHXaZW\"," +
      "        \"value\": \"0\"" +
      "      }" +
      "    }," +
      "    \"Flags\": 0," +
      "    \"LPTokenBalance\": {" +
      "      \"currency\": \"03DCF8F3910BFE6AB56136A90BD41E0902E23C4F\"," +
      "      \"issuer\": \"rNqXnvSYbjZeJQ6jWcf6T5mnNMRPzHXaZW\"," +
      "      \"value\": \"70606.68056410846\"" +
      "    }," +
      "    \"LedgerEntryType\": \"AMM\"," +
      "    \"OwnerNode\": \"0\"," +
      "    \"TradingFee\": 778," +
      "    \"VoteSlots\": [" +
      "      {" +
      "        \"VoteEntry\": {" +
      "          \"Account\": \"rDeo7rDoYw6AUKGneWwfkHPsMJagxcGWy1\"," +
      "          \"TradingFee\": 1000," +
      "          \"VoteWeight\": 70815" +
      "        }" +
      "      }," +
      "      {" +
      "        \"VoteEntry\": {" +
      "          \"Account\": \"rHPoJo9R3QdQjK6XdWL5hY2eTc4wUeNYzW\"," +
      "          \"TradingFee\": 240," +
      "          \"VoteWeight\": 29185" +
      "        }" +
      "      }" +
      "    ]," +
      "    \"index\": \"6BCD7E451DDA015FB307DAD9208A98A2DC3AC4D1448E624B42C89246DCF08692\"" +
      "  }," +
      "  \"status\": \"success\"," +
      "  \"validated\": true" +
      "}";

    assertCanSerializeAndDeserialize(result, json);
  }

  @Test
  void testOfferResult() throws JSONException, JsonProcessingException {
    LedgerEntryResult<OfferObject> result = LedgerEntryResult.<OfferObject>builder()
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(41931093)))
      .ledgerHash(Hash256.of("54FE89D2FF925D623D386A03B402FDB22B2D2D058A62AEE441CA52CC9AA92BB1"))
      .validated(true)
      .index(Hash256.of("066B61CF7248A5A08672541077E3C58EAFD1FA52DDF6B4FD93595E16542C0A14"))
      .node(
        OfferObject.builder()
          .account(Address.of("rNdCZMZqHCo5VkrvsmNVt8ZtdpahT7rDKx"))
          .bookDirectory(Hash256.of("D30EF7A9BFCCEE47AF722871D91E1E21522DF5141CA29AB05B071AFD498D0000"))
          .bookNode("0")
          .flags(OfferFlags.of(131072))
          .ownerNode("0")
          .previousTransactionId(Hash256.of("9D613D7E1E34DA5BE421E06002441F1E183C0B7B3323E1898FC585144A7C13B1"))
          .previousTransactionLedgerSequence(UnsignedInteger.valueOf(41931065))
          .sequence(UnsignedInteger.valueOf(41931063))
          .takerGets(
            IssuedCurrencyAmount.builder()
              .currency("USD")
              .issuer(Address.of("rNdCZMZqHCo5VkrvsmNVt8ZtdpahT7rDKx"))
              .value("100")
              .build()
          )
          .takerPays(XrpCurrencyAmount.ofDrops(200000000))
          .index(Hash256.of("066B61CF7248A5A08672541077E3C58EAFD1FA52DDF6B4FD93595E16542C0A14"))
          .build()
      )
      .status("success")
      .build();

    String json = "{" +
      "  \"index\": \"066B61CF7248A5A08672541077E3C58EAFD1FA52DDF6B4FD93595E16542C0A14\"," +
      "  \"ledger_hash\": \"54FE89D2FF925D623D386A03B402FDB22B2D2D058A62AEE441CA52CC9AA92BB1\"," +
      "  \"ledger_index\": 41931093," +
      "  \"node\": {" +
      "    \"Account\": \"rNdCZMZqHCo5VkrvsmNVt8ZtdpahT7rDKx\"," +
      "    \"BookDirectory\": \"D30EF7A9BFCCEE47AF722871D91E1E21522DF5141CA29AB05B071AFD498D0000\"," +
      "    \"BookNode\": \"0\"," +
      "    \"Flags\": 131072," +
      "    \"LedgerEntryType\": \"Offer\"," +
      "    \"OwnerNode\": \"0\"," +
      "    \"PreviousTxnID\": \"9D613D7E1E34DA5BE421E06002441F1E183C0B7B3323E1898FC585144A7C13B1\"," +
      "    \"PreviousTxnLgrSeq\": 41931065," +
      "    \"Sequence\": 41931063," +
      "    \"TakerGets\": {" +
      "      \"currency\": \"USD\"," +
      "      \"issuer\": \"rNdCZMZqHCo5VkrvsmNVt8ZtdpahT7rDKx\"," +
      "      \"value\": \"100\"" +
      "    }," +
      "    \"TakerPays\": \"200000000\"," +
      "    \"index\": \"066B61CF7248A5A08672541077E3C58EAFD1FA52DDF6B4FD93595E16542C0A14\"" +
      "  }," +
      "  \"status\": \"success\"," +
      "  \"validated\": true" +
      "}";

    assertCanSerializeAndDeserialize(result, json);
  }

  @Test
  void testRippleStateResult() throws JSONException, JsonProcessingException {
    LedgerEntryResult<RippleStateObject> result = LedgerEntryResult.<RippleStateObject>builder()
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(83126482)))
      .ledgerHash(Hash256.of("995F5C7565065ED88C251225C15A02C95D6AADD4AC75E199A9234FA8322B5F84"))
      .validated(true)
      .index(Hash256.of("6A409D31A016227B74D6A14C307239B2BBBE0CFBFCF7C271BFAF20CAA7A1E6DA"))
      .node(
        RippleStateObject.builder()
          .balance(
            IssuedCurrencyAmount.builder()
              .currency("CNY")
              .issuer(Address.of("rrrrrrrrrrrrrrrrrrrrBZbvji"))
              .value("0")
              .build()
          )
          .flags(RippleStateFlags.of(2228224))
          .highLimit(
            IssuedCurrencyAmount.builder()
              .currency("CNY")
              .issuer(Address.of("rHzKtpcB1KC1YuU4PBhk9m2abqrf2kZsfV"))
              .value("1000000000")
              .build()
          )
          .highNode("0")
          .lowLimit(
            IssuedCurrencyAmount.builder()
              .currency("CNY")
              .issuer(Address.of("rJ1adrpGS3xsnQMb9Cw54tWJVFPuSdZHK"))
              .value("0")
              .build()
          )
          .lowNode("2")
          .previousTransactionId(Hash256.of("9A5E68C795D68665A648A5A05E5BC94AA3681400353236F75139BD102D9406FD"))
          .previousTransactionLedgerSequence(UnsignedInteger.valueOf(69746363))
          .index(Hash256.of("6A409D31A016227B74D6A14C307239B2BBBE0CFBFCF7C271BFAF20CAA7A1E6DA"))
          .build()
      )
      .status("success")
      .build();

    String json = "{" +
      "  \"ledger_hash\": \"995F5C7565065ED88C251225C15A02C95D6AADD4AC75E199A9234FA8322B5F84\"," +
      "  \"ledger_index\": 83126482," +
      "  \"validated\": true," +
      "  \"index\": \"6A409D31A016227B74D6A14C307239B2BBBE0CFBFCF7C271BFAF20CAA7A1E6DA\"," +
      "  \"node\": {" +
      "    \"Balance\": {" +
      "      \"currency\": \"CNY\"," +
      "      \"issuer\": \"rrrrrrrrrrrrrrrrrrrrBZbvji\"," +
      "      \"value\": \"0\"" +
      "    }," +
      "    \"Flags\": 2228224," +
      "    \"HighLimit\": {" +
      "      \"currency\": \"CNY\"," +
      "      \"issuer\": \"rHzKtpcB1KC1YuU4PBhk9m2abqrf2kZsfV\"," +
      "      \"value\": \"1000000000\"" +
      "    }," +
      "    \"HighNode\": \"0\"," +
      "    \"LedgerEntryType\": \"RippleState\"," +
      "    \"LowLimit\": {" +
      "      \"currency\": \"CNY\"," +
      "      \"issuer\": \"rJ1adrpGS3xsnQMb9Cw54tWJVFPuSdZHK\"," +
      "      \"value\": \"0\"" +
      "    }," +
      "    \"LowNode\": \"2\"," +
      "    \"PreviousTxnID\": \"9A5E68C795D68665A648A5A05E5BC94AA3681400353236F75139BD102D9406FD\"," +
      "    \"PreviousTxnLgrSeq\": 69746363," +
      "    \"index\": \"6A409D31A016227B74D6A14C307239B2BBBE0CFBFCF7C271BFAF20CAA7A1E6DA\"" +
      "  }," +
      "  \"status\": \"success\"" +
      "}";

    assertCanSerializeAndDeserialize(result, json);
  }

  @Test
  void testCheckResult() throws JSONException, JsonProcessingException {
    LedgerEntryResult<CheckObject> result = LedgerEntryResult.<CheckObject>builder()
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(83126482)))
      .ledgerHash(Hash256.of("995F5C7565065ED88C251225C15A02C95D6AADD4AC75E199A9234FA8322B5F84"))
      .validated(true)
      .index(Hash256.of("56B5D2CC81461E339424869D0F5A2F4F24095B74FCD6F79960EF2D5EA10FBE00"))
      .node(
        CheckObject.builder()
          .account(Address.of("rJk8P3yazgCSSvWXavKKCY5Y3tk4UGCiFF"))
          .destination(Address.of("rHr2n1zVm5nzadgtJY5G2mUYnmWcrxfTbQ"))
          .destinationNode("0")
          .invoiceId(Hash256.of("5D059E085A91283DA8F2C1B8DB973994A3250ABDEDB934799A9C3EE243D3DFBD"))
          .ownerNode("0")
          .previousTxnId(Hash256.of("7F2DB52CA2D2C600748D7B1DF060964C74BF0219B8EF055DAB151F8A23CA1B09"))
          .previousTransactionLedgerSequence(UnsignedInteger.valueOf(41931458))
          .sendMax(XrpCurrencyAmount.ofDrops(12345))
          .sequence(UnsignedInteger.valueOf(41931456))
          .index(Hash256.of("56B5D2CC81461E339424869D0F5A2F4F24095B74FCD6F79960EF2D5EA10FBE00"))
          .build()
      )
      .status("success")
      .build();

    String json = "{" +
      "  \"ledger_hash\": \"995F5C7565065ED88C251225C15A02C95D6AADD4AC75E199A9234FA8322B5F84\"," +
      "  \"ledger_index\": 83126482," +
      "  \"validated\": true," +
      "  \"index\": \"56B5D2CC81461E339424869D0F5A2F4F24095B74FCD6F79960EF2D5EA10FBE00\"," +
      "  \"node\": {" +
      "    \"Account\": \"rJk8P3yazgCSSvWXavKKCY5Y3tk4UGCiFF\"," +
      "    \"Destination\": \"rHr2n1zVm5nzadgtJY5G2mUYnmWcrxfTbQ\"," +
      "    \"DestinationNode\": \"0\"," +
      "    \"Flags\": 0," +
      "    \"InvoiceID\": \"5D059E085A91283DA8F2C1B8DB973994A3250ABDEDB934799A9C3EE243D3DFBD\"," +
      "    \"LedgerEntryType\": \"Check\"," +
      "    \"OwnerNode\": \"0\"," +
      "    \"PreviousTxnID\": \"7F2DB52CA2D2C600748D7B1DF060964C74BF0219B8EF055DAB151F8A23CA1B09\"," +
      "    \"PreviousTxnLgrSeq\": 41931458," +
      "    \"SendMax\": \"12345\"," +
      "    \"Sequence\": 41931456," +
      "    \"index\": \"56B5D2CC81461E339424869D0F5A2F4F24095B74FCD6F79960EF2D5EA10FBE00\"" +
      "  }," +
      "  \"status\": \"success\"" +
      "}";

    assertCanSerializeAndDeserialize(result, json);
  }

  @Test
  void testEscrowResult() throws JSONException, JsonProcessingException {
    LedgerEntryResult<EscrowObject> result = LedgerEntryResult.<EscrowObject>builder()
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(83126482)))
      .ledgerHash(Hash256.of("995F5C7565065ED88C251225C15A02C95D6AADD4AC75E199A9234FA8322B5F84"))
      .validated(true)
      .index(Hash256.of("ABC67054C15F79FEE9183B44D2E16CA06A1804E023E6A2EDB288F4976B1BFEC5"))
      .node(
        EscrowObject.builder()
          .account(Address.of("rEWt92vANNAghT9CC83DtnDDWZcJEL5gk1"))
          .amount(XrpCurrencyAmount.ofDrops(123456))
          .cancelAfter(UnsignedLong.valueOf(750277784))
          .destination(Address.of("rMuTP1PFEFMVhDYKNBLgmre5YrCzVoCYjm"))
          .destinationNode("0")
          .finishAfter(UnsignedLong.valueOf(750277689))
          .ownerNode("0")
          .previousTransactionId(Hash256.of("466C5F96809D62385073F6BA43F5A3C217C96C4A493B7F64F6CE5B5B64278AAA"))
          .previousTransactionLedgerSequence(UnsignedInteger.valueOf(41931760))
          .index(Hash256.of("ABC67054C15F79FEE9183B44D2E16CA06A1804E023E6A2EDB288F4976B1BFEC5"))
          .build()
      )
      .status("success")
      .build();

    String json = "{" +
      "  \"ledger_hash\": \"995F5C7565065ED88C251225C15A02C95D6AADD4AC75E199A9234FA8322B5F84\"," +
      "  \"ledger_index\": 83126482," +
      "  \"validated\": true," +
      "  \"index\": \"ABC67054C15F79FEE9183B44D2E16CA06A1804E023E6A2EDB288F4976B1BFEC5\"," +
      "  \"node\": {" +
      "    \"Account\": \"rEWt92vANNAghT9CC83DtnDDWZcJEL5gk1\"," +
      "    \"Amount\": \"123456\"," +
      "    \"CancelAfter\": 750277784," +
      "    \"Destination\": \"rMuTP1PFEFMVhDYKNBLgmre5YrCzVoCYjm\"," +
      "    \"DestinationNode\": \"0\"," +
      "    \"FinishAfter\": 750277689," +
      "    \"Flags\": 0," +
      "    \"LedgerEntryType\": \"Escrow\"," +
      "    \"OwnerNode\": \"0\"," +
      "    \"PreviousTxnID\": \"466C5F96809D62385073F6BA43F5A3C217C96C4A493B7F64F6CE5B5B64278AAA\"," +
      "    \"PreviousTxnLgrSeq\": 41931760," +
      "    \"index\": \"ABC67054C15F79FEE9183B44D2E16CA06A1804E023E6A2EDB288F4976B1BFEC5\"" +
      "  }," +
      "  \"status\": \"success\"" +
      "}";

    assertCanSerializeAndDeserialize(result, json);
  }

  @Test
  void testCredentialResult() throws JSONException, JsonProcessingException {
    LedgerEntryResult<CredentialObject> result = LedgerEntryResult.<CredentialObject>builder()
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(3110416)))
      .ledgerHash(Hash256.of("74B644AB7D1E043836A70FAFDEAFA6EF8791FE01F129351A9DA231B4E5A91FBF"))
      .validated(true)
      .index(Hash256.of("5A47EEAD6C185E66C20D4A61BDE8F38181B43250FA37F22EBB604C1F97C8166E"))
      .node(
        CredentialObject.builder()
          .credentialType(CredentialType.of("4472697665722773206C6963656E7365"))
          .issuer(Address.of("rPLmuwXJUEtGJ6b4wtqpjYRtxSv5tDVLUz"))
          .subject(Address.of("r9EKPUSDehySNoxqBNuezALVgynRBMNpYi"))
          .issuerNode("0")
          .previousTxnId(Hash256.of("FC7C6F49B7264CD1984C58E68B2F30B6580AE5EC94D215737E266C1404E3DEFF"))
          .previousTransactionLedgerSequence(UnsignedInteger.valueOf(3105995))
          .subjectNode("0")
          .index(Hash256.of("5A47EEAD6C185E66C20D4A61BDE8F38181B43250FA37F22EBB604C1F97C8166E"))
          .flags(CredentialFlags.ACCEPTED)
          .build()
      )
      .status("success")
      .build();

    String json = "{" +
      "  \"index\": \"5A47EEAD6C185E66C20D4A61BDE8F38181B43250FA37F22EBB604C1F97C8166E\"," +
      "  \"ledger_hash\": \"74B644AB7D1E043836A70FAFDEAFA6EF8791FE01F129351A9DA231B4E5A91FBF\"," +
      "  \"ledger_index\": 3110416," +
      "  \"node\": {" +
      "    \"CredentialType\": \"4472697665722773206C6963656E7365\"," +
      "    \"Flags\": 65536," +
      "    \"Issuer\": \"rPLmuwXJUEtGJ6b4wtqpjYRtxSv5tDVLUz\"," +
      "    \"IssuerNode\": \"0\"," +
      "    \"LedgerEntryType\": \"Credential\"," +
      "    \"PreviousTxnID\": \"FC7C6F49B7264CD1984C58E68B2F30B6580AE5EC94D215737E266C1404E3DEFF\"," +
      "    \"PreviousTxnLgrSeq\": 3105995," +
      "    \"Subject\": \"r9EKPUSDehySNoxqBNuezALVgynRBMNpYi\"," +
      "    \"SubjectNode\": \"0\"," +
      "    \"index\": \"5A47EEAD6C185E66C20D4A61BDE8F38181B43250FA37F22EBB604C1F97C8166E\"" +
      "  }," +
      "  \"status\": \"success\"," +
      "  \"validated\": true" +
      "}";

    assertCanSerializeAndDeserialize(result, json);
  }

  @Test
  void testPaymentChannelResult() throws JSONException, JsonProcessingException {
    LedgerEntryResult<PayChannelObject> result = LedgerEntryResult.<PayChannelObject>builder()
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(83126482)))
      .ledgerHash(Hash256.of("995F5C7565065ED88C251225C15A02C95D6AADD4AC75E199A9234FA8322B5F84"))
      .validated(true)
      .index(Hash256.of("7474D1ED2DE25B055AD3C8473DDD69553ACD7325BF2B15C83D54E743C576C615"))
      .node(
        PayChannelObject.builder()
          .account(Address.of("rtqQepGRnrvaHCDyLHcc8xY7uCTnV1aRT"))
          .amount(XrpCurrencyAmount.ofDrops(10000))
          .balance(XrpCurrencyAmount.ofDrops(0))
          .cancelAfter(UnsignedLong.valueOf(533171558))
          .destination(Address.of("r4sxKQshFFUvN8xDiP6KsnUKTyFi1un8UQ"))
          .ownerNode("0")
          .previousTransactionId(Hash256.of("987A299731B80FF14519FF28F18C14B0C55487133E086EAC895099428D57737C"))
          .previousTransactionLedgerSequence(UnsignedInteger.valueOf(41931835))
          .publicKey("EDAF1B0148D4FBB6BC0FCDA97C917C0BD831A654EBFD9B7D84FCB13ADE1BCB5C44")
          .settleDelay(UnsignedLong.ONE)
          .index(Hash256.of("7474D1ED2DE25B055AD3C8473DDD69553ACD7325BF2B15C83D54E743C576C615"))
          .build()
      )
      .status("success")
      .build();

    String json = "{" +
      "  \"ledger_hash\": \"995F5C7565065ED88C251225C15A02C95D6AADD4AC75E199A9234FA8322B5F84\"," +
      "  \"ledger_index\": 83126482," +
      "  \"validated\": true," +
      "  \"index\": \"7474D1ED2DE25B055AD3C8473DDD69553ACD7325BF2B15C83D54E743C576C615\"," +
      "  \"node\": {" +
      "    \"Account\": \"rtqQepGRnrvaHCDyLHcc8xY7uCTnV1aRT\"," +
      "    \"Amount\": \"10000\"," +
      "    \"Balance\": \"0\"," +
      "    \"CancelAfter\": 533171558," +
      "    \"Destination\": \"r4sxKQshFFUvN8xDiP6KsnUKTyFi1un8UQ\"," +
      "    \"Flags\": 0," +
      "    \"LedgerEntryType\": \"PayChannel\"," +
      "    \"OwnerNode\": \"0\"," +
      "    \"PreviousTxnID\": \"987A299731B80FF14519FF28F18C14B0C55487133E086EAC895099428D57737C\"," +
      "    \"PreviousTxnLgrSeq\": 41931835," +
      "    \"PublicKey\": \"EDAF1B0148D4FBB6BC0FCDA97C917C0BD831A654EBFD9B7D84FCB13ADE1BCB5C44\"," +
      "    \"SettleDelay\": 1," +
      "    \"index\": \"7474D1ED2DE25B055AD3C8473DDD69553ACD7325BF2B15C83D54E743C576C615\"" +
      "  }," +
      "  \"status\": \"success\"" +
      "}";

    assertCanSerializeAndDeserialize(result, json);
  }

  @Test
  void testDepositPreAuthResult() throws JSONException, JsonProcessingException {
    LedgerEntryResult<DepositPreAuthObject> result = LedgerEntryResult.<DepositPreAuthObject>builder()
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(83126482)))
      .ledgerHash(Hash256.of("995F5C7565065ED88C251225C15A02C95D6AADD4AC75E199A9234FA8322B5F84"))
      .validated(true)
      .index(Hash256.of("4CFA41F0CEB3BBECB0799BCD4E70057A80B98E762AD655D005BE90992E32CDF7"))
      .node(
        DepositPreAuthObject.builder()
          .account(Address.of("rnmLMp1znQHpSM7xKzL1rg9unXiu1o8ptU"))
          .authorize(Address.of("r4yaMT4QVKFQsyw5sLrJMETe3Wx1L5P9Pe"))
          .ownerNode("0")
          .previousTransactionId(Hash256.of("8D2D634EC7E5B4C6BCB5D4DD72575D42A60A11AD91E5A991692E525E6BF463BA"))
          .previousTransactionLedgerSequence(UnsignedInteger.valueOf(41931900))
          .index(Hash256.of("4CFA41F0CEB3BBECB0799BCD4E70057A80B98E762AD655D005BE90992E32CDF7"))
          .build()
      )
      .status("success")
      .build();

    String json = "{" +
      "  \"ledger_hash\": \"995F5C7565065ED88C251225C15A02C95D6AADD4AC75E199A9234FA8322B5F84\"," +
      "  \"ledger_index\": 83126482," +
      "  \"validated\": true," +
      "  \"index\": \"4CFA41F0CEB3BBECB0799BCD4E70057A80B98E762AD655D005BE90992E32CDF7\"," +
      "  \"node\": {" +
      "    \"Account\": \"rnmLMp1znQHpSM7xKzL1rg9unXiu1o8ptU\"," +
      "    \"Authorize\": \"r4yaMT4QVKFQsyw5sLrJMETe3Wx1L5P9Pe\"," +
      "    \"Flags\": 0," +
      "    \"LedgerEntryType\": \"DepositPreauth\"," +
      "    \"OwnerNode\": \"0\"," +
      "    \"PreviousTxnID\": \"8D2D634EC7E5B4C6BCB5D4DD72575D42A60A11AD91E5A991692E525E6BF463BA\"," +
      "    \"PreviousTxnLgrSeq\": 41931900," +
      "    \"index\": \"4CFA41F0CEB3BBECB0799BCD4E70057A80B98E762AD655D005BE90992E32CDF7\"" +
      "  }," +
      "  \"status\": \"success\"" +
      "}";

    assertCanSerializeAndDeserialize(result, json);
  }

  @Test
  void testDepositPreAuthWithAuthorizedCredResult() throws JSONException, JsonProcessingException {
    List<CredentialWrapper> credentials = Collections.singletonList(
      CredentialWrapper.builder().credential(
        Credential
          .builder()
          .credentialType(CredentialType.of("6D795F63726564656E7469616C"))
          .issuer(Address.of("rK2vwKgQqXahHWUvi9VVTQsYe6gze5n1os"))
          .build()).build()
    );

    LedgerEntryResult<DepositPreAuthObject> result = LedgerEntryResult.<DepositPreAuthObject>builder()
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(3113386)))
      .ledgerHash(Hash256.of("F51471F3AD6080FDDCD5F5E048873B3F3E208FDF16A1C5015DB9EEEEF18A64A9"))
      .validated(true)
      .index(Hash256.of("7587E9C2F7CFCD74D924614C2FFBAD6D590590052B69CCA2BA25DAEBB678067A"))
      .node(
        DepositPreAuthObject.builder()
          .account(Address.of("rK2vwKgQqXahHWUvi9VVTQsYe6gze5n1os"))
          .authorizeCredentials(credentials)
          .ownerNode("0")
          .previousTransactionId(Hash256.of("3D4665AE6874D7E4E34B45E906FF970CD820EB42B1DBD238588E845466D1CE61"))
          .previousTransactionLedgerSequence(UnsignedInteger.valueOf(3113101))
          .index(Hash256.of("7587E9C2F7CFCD74D924614C2FFBAD6D590590052B69CCA2BA25DAEBB678067A"))
          .build()
      )
      .status("success")
      .build();

    String json = "{" +
      "  \"index\": \"7587E9C2F7CFCD74D924614C2FFBAD6D590590052B69CCA2BA25DAEBB678067A\"," +
      "  \"ledger_hash\": \"F51471F3AD6080FDDCD5F5E048873B3F3E208FDF16A1C5015DB9EEEEF18A64A9\"," +
      "  \"ledger_index\": 3113386," +
      "  \"node\": {" +
      "    \"Account\": \"rK2vwKgQqXahHWUvi9VVTQsYe6gze5n1os\"," +
      "    \"AuthorizeCredentials\": [" +
      "      {" +
      "        \"Credential\": {" +
      "          \"CredentialType\": \"6D795F63726564656E7469616C\"," +
      "          \"Issuer\": \"rK2vwKgQqXahHWUvi9VVTQsYe6gze5n1os\"" +
      "        }" +
      "      }" +
      "    ]," +
      "    \"Flags\": 0," +
      "    \"LedgerEntryType\": \"DepositPreauth\"," +
      "    \"OwnerNode\": \"0\"," +
      "    \"PreviousTxnID\": \"3D4665AE6874D7E4E34B45E906FF970CD820EB42B1DBD238588E845466D1CE61\"," +
      "    \"PreviousTxnLgrSeq\": 3113101," +
      "    \"index\": \"7587E9C2F7CFCD74D924614C2FFBAD6D590590052B69CCA2BA25DAEBB678067A\"" +
      "  }," +
      "  \"status\": \"success\"," +
      "  \"validated\": true" +
      "}";

    assertCanSerializeAndDeserialize(result, json);
  }

  @Test
  void testTicketResult() throws JSONException, JsonProcessingException {
    LedgerEntryResult<TicketObject> result = LedgerEntryResult.<TicketObject>builder()
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(83126482)))
      .ledgerHash(Hash256.of("995F5C7565065ED88C251225C15A02C95D6AADD4AC75E199A9234FA8322B5F84"))
      .validated(true)
      .index(Hash256.of("8A0FB133F2D9875961990CE1F6CBB08120C7BD9B330B5D2C9718DE2A4ABCFC47"))
      .node(
        TicketObject.builder()
          .account(Address.of("rKfyHN2fbAJuHtSc1gStGDxxbq4kf9VPFQ"))
          .ownerNode("0")
          .previousTransactionId(Hash256.of("AB5B87765DABF11B9FE5B40506E355532BBE3CF0ADC8C15AF1CD82F4F68CD13D"))
          .previousTransactionLedgerSequence(UnsignedInteger.valueOf(41932010))
          .ticketSequence(UnsignedInteger.valueOf(41932009))
          .index(Hash256.of("8A0FB133F2D9875961990CE1F6CBB08120C7BD9B330B5D2C9718DE2A4ABCFC47"))
          .build()
      )
      .status("success")
      .build();

    String json = "{" +
      "  \"ledger_hash\": \"995F5C7565065ED88C251225C15A02C95D6AADD4AC75E199A9234FA8322B5F84\"," +
      "  \"ledger_index\": 83126482," +
      "  \"validated\": true," +
      "  \"index\": \"8A0FB133F2D9875961990CE1F6CBB08120C7BD9B330B5D2C9718DE2A4ABCFC47\"," +
      "  \"node\": {" +
      "    \"Account\": \"rKfyHN2fbAJuHtSc1gStGDxxbq4kf9VPFQ\"," +
      "    \"Flags\": 0," +
      "    \"LedgerEntryType\": \"Ticket\"," +
      "    \"OwnerNode\": \"0\"," +
      "    \"PreviousTxnID\": \"AB5B87765DABF11B9FE5B40506E355532BBE3CF0ADC8C15AF1CD82F4F68CD13D\"," +
      "    \"PreviousTxnLgrSeq\": 41932010," +
      "    \"TicketSequence\": 41932009," +
      "    \"index\": \"8A0FB133F2D9875961990CE1F6CBB08120C7BD9B330B5D2C9718DE2A4ABCFC47\"" +
      "  }," +
      "  \"status\": \"success\"" +
      "}";

    assertCanSerializeAndDeserialize(result, json);
  }

  @Test
  void testNftPageResult() throws JSONException, JsonProcessingException {
    LedgerEntryResult<NfTokenPageObject> result = LedgerEntryResult.<NfTokenPageObject>builder()
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(83126482)))
      .ledgerHash(Hash256.of("995F5C7565065ED88C251225C15A02C95D6AADD4AC75E199A9234FA8322B5F84"))
      .validated(true)
      .index(Hash256.of("4070656F661A60726DBB384E09F6E36B88071072FFFFFFFFFFFFFFFFFFFFFFFF"))
      .node(
        NfTokenPageObject.builder()
          .addNfTokens(
            NfTokenWrapper.of(
              NfToken.builder()
                .nfTokenId(NfTokenId.of("000000004070656F661A60726DBB384E09F6E36B880710720000099A00000000"))
                .uri(NfTokenUri.of(
                  "697066733A2F2F62616679626569676479727A74357366703775646D376875373675683779323" +
                    "66E6634646675796C71616266336F636C67747179353566627A6469")
                )
                .build()
            )
          )

          .previousTransactionId(Hash256.of("C94CF5BC9DF78A75997E93C71CDD8A2776E2DF279DD6F394BF4976045D960C4B"))
          .previousTransactionLedgerSequence(LedgerIndex.of(UnsignedInteger.valueOf(41932089)))
          .index(Hash256.of("4070656F661A60726DBB384E09F6E36B88071072FFFFFFFFFFFFFFFFFFFFFFFF"))
          .build()
      )
      .status("success")
      .build();

    String json = "{" +
      "  \"ledger_hash\": \"995F5C7565065ED88C251225C15A02C95D6AADD4AC75E199A9234FA8322B5F84\"," +
      "  \"ledger_index\": 83126482," +
      "  \"validated\": true," +
      "  \"index\": \"4070656F661A60726DBB384E09F6E36B88071072FFFFFFFFFFFFFFFFFFFFFFFF\"," +
      "  \"node\": {" +
      "    \"LedgerEntryType\": \"NFTokenPage\"," +
      "    \"NFTokens\": [" +
      "      {" +
      "        \"NFToken\": {" +
      "          \"NFTokenID\": \"000000004070656F661A60726DBB384E09F6E36B880710720000099A00000000\"," +
      "          \"URI\": \"697066733A2F2F62616679626569676479727A74357366703775646D37687537367568377932366E6634646" +
      "675796C71616266336F636C67747179353566627A6469\"" +
      "        }" +
      "      }" +
      "    ]," +
      "    \"PreviousTxnID\": \"C94CF5BC9DF78A75997E93C71CDD8A2776E2DF279DD6F394BF4976045D960C4B\"," +
      "    \"PreviousTxnLgrSeq\": 41932089," +
      "    \"index\": \"4070656F661A60726DBB384E09F6E36B88071072FFFFFFFFFFFFFFFFFFFFFFFF\"" +
      "  }," +
      "  \"status\": \"success\"" +
      "}";

    assertCanSerializeAndDeserialize(result, json);
  }

  @Test
  void testPermissionedDomainResult() throws JSONException, JsonProcessingException {
    List<CredentialWrapper> acceptedCredentials = IntStream.range(0, 10)
      .mapToObj(i -> CredentialWrapper.builder()
        .credential(Credential.builder()
          .issuer(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
          .credentialType(CredentialType.ofPlainText("Driver licence - " + i))
          .build())
        .build())
      .collect(Collectors.toList());

    LedgerEntryResult<PermissionedDomainObject> result = LedgerEntryResult.<PermissionedDomainObject>builder()
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(3581898)))
      .ledgerHash(Hash256.of("E28A994738F9CEA73C7483CD337560E500F0AADE27900BF97FD5B0FC6A26B228"))
      .validated(true)
      .index(Hash256.of("D4ACD9C1EBE3EEF9B3B1052CDFF40F87CA5AF37FEB1E35F842E6A72CB5911C74"))
      .node(
        PermissionedDomainObject.builder()
          .owner(Address.of("rhjmfJwtW5bKAbFkZuxVQdCTDVVC4mcfwB"))
          .ownerNode("0")
          .sequence(UnsignedInteger.valueOf(3581881))
          .acceptedCredentials(acceptedCredentials)
          .previousTxnId(Hash256.of("C012929FA346E9E5624A7AA23F8EAE6B9909BD616087AF99F2BF2F55B6140686"))
          .previousTransactionLedgerSequence(UnsignedInteger.valueOf(3581884))
          .index(Hash256.of("D4ACD9C1EBE3EEF9B3B1052CDFF40F87CA5AF37FEB1E35F842E6A72CB5911C74"))
          .build()
      )
      .status("success")
      .build();

    String json = "{" +
      "  \"index\": \"D4ACD9C1EBE3EEF9B3B1052CDFF40F87CA5AF37FEB1E35F842E6A72CB5911C74\"," +
      "  \"ledger_hash\": \"E28A994738F9CEA73C7483CD337560E500F0AADE27900BF97FD5B0FC6A26B228\"," +
      "  \"ledger_index\": 3581898," +
      "  \"node\": {" +
      "  \"AcceptedCredentials\": " + objectMapper.writeValueAsString(acceptedCredentials) + "," +
      "    \"Flags\": 0," +
      "    \"LedgerEntryType\": \"PermissionedDomain\"," +
      "    \"Owner\": \"rhjmfJwtW5bKAbFkZuxVQdCTDVVC4mcfwB\"," +
      "    \"OwnerNode\": \"0\"," +
      "    \"PreviousTxnID\": \"C012929FA346E9E5624A7AA23F8EAE6B9909BD616087AF99F2BF2F55B6140686\"," +
      "    \"PreviousTxnLgrSeq\": 3581884," +
      "    \"Sequence\": 3581881," +
      "    \"index\": \"D4ACD9C1EBE3EEF9B3B1052CDFF40F87CA5AF37FEB1E35F842E6A72CB5911C74\"" +
      "  }," +
      "  \"status\": \"success\"," +
      "  \"validated\": true" +
      "}";

    assertCanSerializeAndDeserialize(result, json);
  }

  @Test
  void testWithHashAndLedgerIndex() {
    LedgerEntryResult<LedgerObject> result = LedgerEntryResult.builder()
      .node(mock(LedgerObject.class))
      .ledgerHash(HASH_256)
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.ONE))
      .index(HASH_256)
      .build();

    assertThat(result.ledgerHash()).isNotEmpty().get().isEqualTo(result.ledgerHashSafe());
    assertThat(result.ledgerIndex()).isNotEmpty().get().isEqualTo(result.ledgerIndexSafe());
    assertThat(result.ledgerCurrentIndex()).isEmpty();
    assertThatThrownBy(result::ledgerCurrentIndexSafe)
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Result did not contain a ledgerCurrentIndex.");
  }

  @Test
  void testWithLedgerCurrentIndex() {
    LedgerEntryResult<LedgerObject> result = LedgerEntryResult.builder()
      .node(mock(LedgerObject.class))
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.ONE))
      .index(HASH_256)
      .build();

    assertThat(result.ledgerCurrentIndex()).isNotEmpty().get().isEqualTo(result.ledgerCurrentIndexSafe());
    assertThat(result.ledgerHash()).isEmpty();
    assertThat(result.ledgerIndex()).isEmpty();
    assertThatThrownBy(result::ledgerHashSafe)
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Result did not contain a ledgerHash.");
    assertThatThrownBy(result::ledgerIndexSafe)
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Result did not contain a ledgerIndex.");
  }
}