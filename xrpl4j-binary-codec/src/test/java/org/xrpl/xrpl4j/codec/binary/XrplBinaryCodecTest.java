package org.xrpl.xrpl4j.codec.binary;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: binary-codec
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.primitives.UnsignedInteger;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xrpl.xrpl4j.codec.fixtures.FixtureUtils;
import org.xrpl.xrpl4j.codec.fixtures.data.WholeObject;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.ImmutablePayment;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.Signer;
import org.xrpl.xrpl4j.model.transactions.SignerWrapper;
import org.xrpl.xrpl4j.model.transactions.TrustSet;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

class XrplBinaryCodecTest {

  public static final String SIMPLE_JSON = "{\"CloseResolution\":1,\"Method\":2}";
  public static final String SINGLE_LEVEL_OBJECT_JSON = "{\"Memo\":{\"Memo\":{\"Method\":2}}}";
  public static final String MULTI_LEVEL_OBJECT_JSON =
    "{\"Memo\":{\"Memo\":{\"CloseResolution\":1,\"Method\":2}}}";

  public static final String SIMPLE_HEX = "011001021002";
  public static final String SINGLE_OBJECT_HEX = "EAEA021002E1E1";
  public static final String MULTI_LEVEL_OBJECT_HEX = "EAEA011001021002E1E1";

  private XrplBinaryCodec encoder = XrplBinaryCodec.getInstance();
  private ObjectMapper objectMapper = ObjectMapperFactory.create();

  private static Stream<Arguments> dataDrivenFixtures() throws IOException {
    return FixtureUtils.getDataDrivenFixtures().wholeObjectTests().stream()
      .map(Arguments::of);
  }

  @Test
  void encodeDecodeSimple() throws JsonProcessingException {
    assertThat(encoder.encode(SIMPLE_JSON)).isEqualTo(SIMPLE_HEX);
    assertThat(encoder.decode(SIMPLE_HEX)).isEqualTo(SIMPLE_JSON);
  }

  @Test
  void encodeDecodeSingleChildObject() throws JsonProcessingException {
    assertThat(encoder.encode(SINGLE_LEVEL_OBJECT_JSON)).isEqualTo(SINGLE_OBJECT_HEX);
    assertThat(encoder.decode(SINGLE_OBJECT_HEX)).isEqualTo(SINGLE_LEVEL_OBJECT_JSON);
  }

  @Test
  void encodeJsonWithMultipleEmbeddedObjects() throws JsonProcessingException {
    assertThat(encoder.encode(MULTI_LEVEL_OBJECT_JSON)).isEqualTo(MULTI_LEVEL_OBJECT_HEX);
    assertThat(encoder.decode(MULTI_LEVEL_OBJECT_HEX)).isEqualTo(MULTI_LEVEL_OBJECT_JSON);
  }

  @Test
  void encodeDecodeAmount() throws JsonProcessingException {
    String json = "{\"Fee\":\"100\"}";
    String hex = "684000000000000064";
    assertThat(encoder.encode(json)).isEqualTo(hex);
    assertThat(encoder.decode(hex)).isEqualTo(json);
  }

  @Test
  void encodeDecodeHash128() throws JsonProcessingException {
    String json = "{\"EmailHash\":\"11223344556677889900AABBCCDDEEFF\"}";
    String hex = "4111223344556677889900AABBCCDDEEFF";
    assertThat(encoder.encode(json)).isEqualTo(hex);
    assertThat(encoder.decode(hex)).isEqualTo(json);
  }

  @Test
  void encodeDecodeHash160() throws JsonProcessingException {
    String json = "{\"TakerPaysCurrency\":\"11223344556677889900AABBCCDDEEFF11223344\"}";
    String hex = "011111223344556677889900AABBCCDDEEFF11223344";
    assertThat(encoder.encode(json)).isEqualTo(hex);
    assertThat(encoder.decode(hex)).isEqualTo(json);
  }

  @Test
  void encodeDecodeHash256() throws JsonProcessingException {
    String json = "{\"LedgerHash\":\"11223344556677889900AABBCCDDEEFF11223344556677889900AABBCCDDEEFF\"}";
    String hex = "5111223344556677889900AABBCCDDEEFF11223344556677889900AABBCCDDEEFF";
    assertThat(encoder.encode(json)).isEqualTo(hex);
    assertThat(encoder.decode(hex)).isEqualTo(json);
  }

  @Test
  void encodeDecodeBlob() throws JsonProcessingException {
    String json = "{\"Domain\":\"1234\"}";
    String hex = "77021234";
    assertThat(encoder.encode(json)).isEqualTo(hex);
    assertThat(encoder.decode(hex)).isEqualTo(json);
  }

  @Test
  void encodeDecodeBigBlob() throws JsonProcessingException {
    String bigValue = Strings.repeat("A", 50000);
    String json = "{\"Domain\":\"" + bigValue + "\"}";
    String blobType = "77";
    String lengthInHex = "F130E7"; // 50000 encoded in XRPL hex length encoding
    String hex = blobType + lengthInHex + bigValue;
    assertThat(encoder.encode(json)).isEqualTo(hex);
    assertThat(encoder.decode(hex)).isEqualTo(json);
  }

  @Test
  void encodeDecodeIssuedCurrency() throws JsonProcessingException {
    String json =
      "{\"Fee\":{\"currency\":\"USD\",\"value\":\"123\",\"issuer\":\"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\"}}";
    String hex = "68D5045EADB112E00000000000000000000000000055534400000000005E7B112523F68D2F5E879DB4EAC51C6698A69304";
    assertThat(encoder.encode(json)).isEqualTo(hex);
    assertThat(encoder.decode(hex)).isEqualTo(json);
  }

  @Test
  void encodeDecodeAmendments() throws JsonProcessingException {
    String amendmentHex1 = Strings.repeat("1", 64);
    String amendmentHex2 = Strings.repeat("2", 64);
    String json = "{\"Amendments\":[\"" + amendmentHex1 + "\",\"" + amendmentHex2 + "\"]}";
    String hex = "031340" + amendmentHex1 + amendmentHex2;
    assertThat(encoder.encode(json)).isEqualTo(hex);
    assertThat(encoder.decode(hex)).isEqualTo(json);
  }

  @Test
  void encodeDecodeUnlModify() throws JsonProcessingException {
    String json = "{" +
      "\"Account\":\"rrrrrrrrrrrrrrrrrrrrrhoLvTp\"," +
      "\"Fee\":\"0\"," +
      "\"LedgerSequence\":67850752," +
      "\"Sequence\":0," +
      "\"SigningPubKey\":\"\"," +
      "\"TransactionType\":\"UNLModify\"," +
      "\"UNLModifyDisabling\":1," +
      "\"UNLModifyValidator\":\"EDB6FC8E803EE8EDC2793F1EC917B2EE41D35255618DEB91D3F9B1FC89B75D4539\"}";

    String expected = "120066240000000026040B52006840000000000000007300701321EDB6FC8E803EE8EDC2793F1EC9" +
      "17B2EE41D35255618DEB91D3F9B1FC89B75D453900101101";
    assertThat(expected).isEqualTo(encoder.encode(json));

    String jsonWithoutAccount = "{" +
      "\"Fee\":\"0\"," +
      "\"LedgerSequence\":67850752," +
      "\"Sequence\":0," +
      "\"SigningPubKey\":\"\"," +
      "\"TransactionType\":\"UNLModify\"," +
      "\"UNLModifyDisabling\":1," +
      "\"UNLModifyValidator\":\"EDB6FC8E803EE8EDC2793F1EC917B2EE41D35255618DEB91D3F9B1FC89B75D4539\"}";
    // Results with and without `Account` are same since it is skipped while decoding.
    assertThat(encoder.encode(jsonWithoutAccount)).isEqualTo(expected);

    String expectedDisabledUnlModify = "120066240000000026040B52006840000000000000007300701321EDB6FC8E" +
      "803EE8EDC2793F1EC917B2EE41D35255618DEB91D3F9B1FC89B75D453900101100";

    String jsonDisablingUnlModify = "{" +
      "\"Account\":\"rrrrrrrrrrrrrrrrrrrrrhoLvTp\"," +
      "\"Fee\":\"0\"," +
      "\"LedgerSequence\":67850752," +
      "\"Sequence\":0," +
      "\"SigningPubKey\":\"\"," +
      "\"TransactionType\":\"UNLModify\"," +
      "\"UNLModifyDisabling\":0," +
      "\"UNLModifyValidator\":\"EDB6FC8E803EE8EDC2793F1EC917B2EE41D35255618DEB91D3F9B1FC89B75D4539\"}";
    assertThat(encoder.encode(jsonDisablingUnlModify)).isEqualTo(expectedDisabledUnlModify);
  }

  @Test
  void encodeDecodeEnableAmendment() throws JsonProcessingException {
    String json = "{" +
      "\"Account\":\"rrrrrrrrrrrrrrrrrrrrrhoLvTp\"," +
      "\"Fee\":\"12\"," +
      "\"LedgerSequence\":67850752," +
      "\"Sequence\":2470665," +
      "\"SigningPubKey\":\"ED5F5AC8B98974A3CA843326D9B88CEBD0560177B973EE0B149F782CFAA06DC66A\"," +
      "\"TransactionType\":\"EnableAmendment\"," +
      "\"Amendment\":\"42426C4D4F1009EE67080A9B7965B44656D7714D104A72F9B4369F97ABF044EE\"}";

    String expected = "120064240025B30926040B5200501342426C4D4F1009EE67080A9B7965B44656D7714D104A72F9B4369" +
      "F97ABF044EE68400000000000000C7321ED5F5AC8B98974A3CA843326D9B88CEBD0560177B973EE0B149F782CFAA06DC66A" +
      "81140000000000000000000000000000000000000000";
    assertThat(encoder.encode(json)).isEqualTo(expected);

  }

  @Test
  void encodeDecodeSetFee() throws JsonProcessingException {
    String json = "{" +
      "\"Account\":\"rrrrrrrrrrrrrrrrrrrrrhoLvTp\"," +
      "\"Fee\":\"12\"," +
      "\"LedgerSequence\":67850752," +
      "\"Sequence\":2470665," +
      "\"SigningPubKey\":\"ED5F5AC8B98974A3CA843326D9B88CEBD0560177B973EE0B149F782CFAA06DC66A\"," +
      "\"TransactionType\":\"SetFee\"," +
      "\"ReserveIncrement\":5000000," +
      "\"ReserveBase\":20000000," +
      "\"ReferenceFeeUnits\":10," +
      "\"BaseFee\":\"000000000000000A\"}";

    String expected = "120065240025B30926040B5200201E0000000A201F01312D002020004C4B4035000000000000000A" +
      "68400000000000000C7321ED5F5AC8B98974A3CA843326D9B88CEBD0560177B973EE0B149F782CFAA06DC66A8114" +
      "0000000000000000000000000000000000000000";
    assertThat(encoder.encode(json)).isEqualTo(expected);
  }

  @Test
  void encodeForSigning() throws JsonProcessingException {
    String json =
      "{\"Account\":\"r45dBj4S3VvMMYXxr9vHX4Z4Ma6ifPMCkK\",\"TransactionType\":\"Payment\",\"Fee\":\"789\"," +
        "\"Sequence\":1,\"Flags\":2147614720,\"SourceTag\":1," +
        "\"Amount\":{\"value\":\"1234567890123456\",\"currency\":\"USD\"," +
        "\"issuer\":\"rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw\"}," +
        "\"Destination\":\"rrrrrrrrrrrrrrrrrrrrBZbvji\",\"DestinationTag\":2," +
        "\"SigningPubKey\":\"ED5F5AC8B98974A3CA843326D9B88CEBD0560177B973EE0B149F782CFAA06DC66A\"," +
        "\"TxnSignature\": \"12345678\"}";

    // expected value obtained by calling encodeForSigning(json) from ripple-binary-codec
    String expected =
      "535458001200002280020000230000000124000000012E0000000261D84462D53C8ABAC00000000000000000000000005553440000000" +
        "0008B1CE810C13D6F337DAC85863B3D70265A24DF446840000000000003157321ED5F5AC8B98974A3CA843326D9B88CEB" +
        "D0560177B973EE0B149F782CFAA06DC66A8114EE39E6D05CFD6A90DAB700A1D70149ECEE29DFEC83140000000000000000" +
        "000000000000000000000001";
    assertThat(encoder.encode(json)).isNotEqualTo(encoder.encodeForSigning(json));
    assertThat(encoder.encodeForSigning(json)).isEqualTo(expected);
  }

  @Test
  void decodeSignedTransaction() throws JsonProcessingException {
    String signedTxHex =
      "535458001200002280020000230000000124000000012E0000000261D84462D53C8ABAC00000000000000000000000005553440000000" +
        "0008B1CE810C13D6F337DAC85863B3D70265A24DF446840000000000003157321ED5F5AC8B98974A3CA843326D9B88CEB" +
        "D0560177B973EE0B149F782CFAA06DC66A8114EE39E6D05CFD6A90DAB700A1D70149ECEE29DFEC83140000000000000000" +
        "000000000000000000000001";

    Payment unsignedPayment = Payment.builder()
      .account(Address.of("r45dBj4S3VvMMYXxr9vHX4Z4Ma6ifPMCkK"))
      .fee(XrpCurrencyAmount.ofDrops(789))
      .amount(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw"))
        .value("1234567890123456")
        .build())
      .destination(Address.of("rrrrrrrrrrrrrrrrrrrrBZbvji"))
      .signingPublicKey("ED5F5AC8B98974A3CA843326D9B88CEBD0560177B973EE0B149F782CFAA06DC66A")
      .flags(Flags.PaymentFlags.builder().tfPartialPayment(true).build())
      .destinationTag(UnsignedInteger.valueOf(2))
      .sequence(UnsignedInteger.ONE)
      .sourceTag(UnsignedInteger.ONE)
      .build();

    assertThat(objectMapper.readValue(encoder.decode(signedTxHex), Payment.class)).isEqualTo(unsignedPayment);
  }

  @Test
  void decodeMultiSignedTransaction() throws JsonProcessingException {
    String signerAccountId = "rJZdUusLDtY9NEsGea7ijqhVrXv98rYBYN";
    List<SignerWrapper> signers = Lists.newArrayList(
      SignerWrapper.of(Signer.builder()
        .account(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
        .signingPublicKey("02B3EC4E5DD96029A647CFA20DA07FE1F85296505552CCAC114087E66B46BD77DF")
        .transactionSignature("30450221009C195DBBF7967E223D8626CA19CF02073667F2B22E206727BFE848" +
          "FF42BEAC8A022048C323B0BED19A988BDBEFA974B6DE8AA9DCAE250AA82BBD1221787032A864E5")
        .build()
      ),
      SignerWrapper.of(Signer.builder()
        .account(Address.of("rUpy3eEg8rqjqfUoLeBnZkscbKbFsKXC3v"))
        .signingPublicKey("028FFB276505F9AC3F57E8D5242B386A597EF6C40A7999F37F1948636FD484E25B")
        .transactionSignature("30440220680BBD745004E9CFB6B13A137F505FB92298AD309071D16C7B982825" +
          "188FD1AE022004200B1F7E4A6A84BB0E4FC09E1E3BA2B66EBD32F0E6D121A34BA3B04AD99BC1")
        .build()
      )
    );

    TrustSet trustSet = TrustSet.builder()
      .account(Address.of("rEuLyBCvcw4CFmzv8RepSiAoNgF8tTGJQC"))
      .fee(XrpCurrencyAmount.ofDrops(30000))
      .sequence(UnsignedInteger.valueOf(2))
      .signingPublicKey("")
      .limitAmount(IssuedCurrencyAmount.builder()
        .value("100")
        .currency("USD")
        .issuer(Address.of("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"))
        .build())
      .flags(Flags.TrustSetFlags.of(262144))
      .signers(signers)
      .build();

    String multiSignedWithSignersTxHex =
      "534D54001200142200040000240000000263D5038D7EA4C680000000000000000000000000005553440000000000B5F762798A53D543" +
        "A014CAF8B297CFF8F2F937E868400000000000753073008114A3780F5CB5A44D366520FC44055E8ED44D9A2270F3E010732102B3EC" +
        "4E5DD96029A647CFA20DA07FE1F85296505552CCAC114087E66B46BD77DF744730450221009C195DBBF7967E223D8626CA19CF0207" +
        "3667F2B22E206727BFE848FF42BEAC8A022048C323B0BED19A988BDBEFA974B6DE8AA9DCAE250AA82BBD1221787032A864E5811420" +
        "4288D2E47F8EF6C99BCC457966320D12409711E1E0107321028FFB276505F9AC3F57E8D5242B386A597EF6C40A7999F37F1948636F" +
        "D484E25B744630440220680BBD745004E9CFB6B13A137F505FB92298AD309071D16C7B982825188FD1AE022004200B1F7E4A6A84BB" +
        "0E4FC09E1E3BA2B66EBD32F0E6D121A34BA3B04AD99BC181147908A7F0EDD48EA896C3580A399F0EE78611C8E3E1F1C0A5ABEF2428" +
        "02EFED4B041E8F2D4A8CC86AE3D1";

    TrustSet decodedTrustset = objectMapper.readValue(
      encoder.decode(multiSignedWithSignersTxHex), TrustSet.class
    );
    assertThat(decodedTrustset).isEqualTo(trustSet);
  }

  @Test
  void decodeMultiSignEncodedTx() throws JsonProcessingException {
    String signerAccountId = "rJZdUusLDtY9NEsGea7ijqhVrXv98rYBYN";
    List<SignerWrapper> signers = Lists.newArrayList(
      SignerWrapper.of(Signer.builder()
        .account(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
        .signingPublicKey("02B3EC4E5DD96029A647CFA20DA07FE1F85296505552CCAC114087E66B46BD77DF")
        .transactionSignature("30450221009C195DBBF7967E223D8626CA19CF02073667F2B22E206727BFE848" +
          "FF42BEAC8A022048C323B0BED19A988BDBEFA974B6DE8AA9DCAE250AA82BBD1221787032A864E5")
        .build()
      ),
      SignerWrapper.of(Signer.builder()
        .account(Address.of("rUpy3eEg8rqjqfUoLeBnZkscbKbFsKXC3v"))
        .signingPublicKey("028FFB276505F9AC3F57E8D5242B386A597EF6C40A7999F37F1948636FD484E25B")
        .transactionSignature("30440220680BBD745004E9CFB6B13A137F505FB92298AD309071D16C7B982825" +
          "188FD1AE022004200B1F7E4A6A84BB0E4FC09E1E3BA2B66EBD32F0E6D121A34BA3B04AD99BC1")
        .build()
      )
    );

    ImmutablePayment.Builder paymentBuilder = Payment.builder()
      .account(Address.of("rEuLyBCvcw4CFmzv8RepSiAoNgF8tTGJQC"))
      .fee(XrpCurrencyAmount.ofDrops(30000))
      .amount(XrpCurrencyAmount.ofDrops(1000))
      .destination(Address.of("r45dBj4S3VvMMYXxr9vHX4Z4Ma6ifPMCkK"))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey("");

    Payment paymentWithoutSigners = paymentBuilder.build();
    Payment paymentWithSigners = paymentBuilder.signers(signers).build();

    final String unsignedJson = objectMapper.writeValueAsString(paymentWithSigners);
    final String unsignedBinaryHex = encoder.encodeForMultiSigning(unsignedJson, signerAccountId);
    String decoded = encoder.decode(unsignedBinaryHex);
    Payment transaction = objectMapper.readValue(
      decoded,
      Payment.class
    );
    assertThat(transaction).isEqualTo(paymentWithoutSigners);
  }

  @Test
  void encodeForMultiSigning() throws JsonProcessingException {
    String signerAccountId = "rJZdUusLDtY9NEsGea7ijqhVrXv98rYBYN";
    String json =
      "{\"Account\":\"r45dBj4S3VvMMYXxr9vHX4Z4Ma6ifPMCkK\",\"TransactionType\":\"Payment\",\"Fee\":\"789\"," +
        "\"Sequence\":1,\"Flags\":2147614720,\"SourceTag\":1," +
        "\"Amount\":{\"value\":\"1234567890123456\",\"currency\":\"USD\"," +
        "\"issuer\":\"rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw\"}," +
        "\"Destination\":\"rrrrrrrrrrrrrrrrrrrrBZbvji\",\"DestinationTag\":2," +
        "\"SigningPubKey\":\"ED5F5AC8B98974A3CA843326D9B88CEBD0560177B973EE0B149F782CFAA06DC66A\"," +
        "\"TxnSignature\": \"12345678\"}";

    // expected value obtained by calling encodeForMultisigning(json, signerAccount) from ripple-binary-codec
    String expected =
      "534D54001200002280020000230000000124000000012E0000000261D84462D53C8ABAC000000000000000000000000" +
        "055534400000000008B1CE810C13D6F337DAC85863B3D70265A24DF4468400000000000031573008114EE39E6D05C" +
        "FD6A90DAB700A1D70149ECEE29DFEC83140000000000000000000000000000000000000001C0A5ABEF242802EFED4" +
        "B041E8F2D4A8CC86AE3D1";
    assertThat(encoder.encode(json)).isNotEqualTo(encoder.encodeForSigning(json));
    assertThat(encoder.encodeForMultiSigning(json, signerAccountId)).isEqualTo(expected);
  }

  @Test
  public void encodePaymentWithSigners() throws JsonProcessingException {
    String json = "{\"Account\":\"rGs8cFHMfJanAXVtn6e8Lz2iH8FtnGdexw\",\"Fee\":\"30\"," +
      "\"Sequence\":6," +
      "\"Signers\":" +
      "[{\"Signer\":{\"Account\":\"rGDG5dYzvaNMaNGHAYGJKGH1vPBTHeD4fy\"," +
      "\"TxnSignature\":\"F5354C2AEAE320FCE49CE18733EA6C27103989878E2C5561028292A09A0AE920792847982D26" +
      "8392B8134EF4CA35159C170C40E51F5AFB4F1400DCC9287A3709\"," +
      "\"SigningPubKey\":\"ED62267B5A9A0917D5F0D52531428294A80EEFEEB1DB595AED1C94964B35F79F2C\"}}," +
      "{\"Signer\":{\"Account\":\"rwm8zSsHG5oTrHMTkKQFKCV3QDQEG1zHvB\"," +
      "\"TxnSignature\":\"26A90616049EA684FDD4685726DF674815B48CBC1827D2F0D1DBC8537AC8508F715480AF70C426" +
      "B35C193DE49C851831E767BF4AA51880CD1F90618E74B93D0C\"," +
      "\"SigningPubKey\":\"ED9018780E2D6D454ED59E40DBEFA4681B7307940B45B67E4C8DE80DBA79626BB8\"}}]," +
      "\"SigningPubKey\":\"\",\"Flags\":2147483648,\"Amount\":\"12345\"," +
      "\"Destination\":\"rfA6dKpRbJfZo9HgAVGLJP3T2qPgQMA9QB\",\"TransactionType\":\"Payment\"}";

    String expected = "1200002280000000240000000661400000000000303968400000000000001E73008114A510CC5A6" +
      "84976D8986ADA04D2AC4A9C5B77ADD983144C29212A966F2C128FCEB5DB86F5C0A0635275B6F3E0107321ED9018780E" +
      "2D6D454ED59E40DBEFA4681B7307940B45B67E4C8DE80DBA79626BB8744026A90616049EA684FDD4685726DF674815B" +
      "48CBC1827D2F0D1DBC8537AC8508F715480AF70C426B35C193DE49C851831E767BF4AA51880CD1F90618E74B93D0C81" +
      "146B31D3372135AEEB0AA33540B2FBAD8CA4C2EBE2E1E0107321ED62267B5A9A0917D5F0D52531428294A80EEFEEB1D" +
      "B595AED1C94964B35F79F2C7440F5354C2AEAE320FCE49CE18733EA6C27103989878E2C5561028292A09A0AE9207928" +
      "47982D268392B8134EF4CA35159C170C40E51F5AFB4F1400DCC9287A37098114A6DBFFB301F614A2F7B5E6B94392E17" +
      "AAF898E9DE1F1";
    assertThat(encoder.encode(json)).isEqualTo(expected);
  }

  @ParameterizedTest
  @MethodSource("dataDrivenFixtures")
  void dataDriven(WholeObject wholeObject) throws IOException {
    assertThat(encoder.encode(wholeObject.txJson().toString())).isEqualTo(wholeObject.expectedHex());
  }

}
