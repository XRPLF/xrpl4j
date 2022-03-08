package org.xrpl.xrpl4j.codec.binary;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xrpl.xrpl4j.codec.binary.serdes.BinaryParser;
import org.xrpl.xrpl4j.codec.binary.types.STObjectType;
import org.xrpl.xrpl4j.codec.fixtures.FixtureUtils;
import org.xrpl.xrpl4j.codec.fixtures.data.WholeObject;

import java.io.IOException;
import java.util.stream.Stream;

class XrplBinaryCodecTest {

  public static final String SIMPLE_JSON = "{\"CloseResolution\":1,\"Method\":2}";
  public static final String SINGLE_LEVEL_OBJECT_JSON = "{\"Memo\":{\"Memo\":{\"Method\":2}}}";
  public static final String MULTI_LEVEL_OBJECT_JSON =
    "{\"Memo\":{\"Memo\":{\"CloseResolution\":1,\"Method\":2}}}";

  public static final String SIMPLE_HEX = "011001021002";
  public static final String SINGLE_OBJECT_HEX = "EAEA021002E1E1";
  public static final String MULTI_LEVEL_OBJECT_HEX = "EAEA011001021002E1E1";

  private XrplBinaryCodec encoder = new XrplBinaryCodec();

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
  void encodeForSigning() throws JsonProcessingException {
    String json =
      "{\"Account\":\"r45dBj4S3VvMMYXxr9vHX4Z4Ma6ifPMCkK\",\"TransactionType\":\"Payment\",\"Fee\":\"789\"," +
        "\"Sequence\":1,\"Flags\":2147614720,\"SourceTag\":1," +
        "\"Amount\":{\"value\":\"1234567890123456\",\"currency\":\"USD\"," +
        "\"issuer\":\"rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw\"}," +
        "\"Destination\":\"rrrrrrrrrrrrrrrrrrrrBZbvji\",\"DestinationTag\":2," +
        "\"SigningPubKey\":\"ED5F5AC8B98974A3CA843326D9B88CEBD0560177B973EE0B149F782CFAA06DC66A\"," +
        "\"Signature\": \"12345678\"," +
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
  void encodeForMultiSigning() throws JsonProcessingException {
    String signerAccountId = "rJZdUusLDtY9NEsGea7ijqhVrXv98rYBYN";
    String json =
      "{\"Account\":\"r45dBj4S3VvMMYXxr9vHX4Z4Ma6ifPMCkK\",\"TransactionType\":\"Payment\",\"Fee\":\"789\"," +
        "\"Sequence\":1,\"Flags\":2147614720,\"SourceTag\":1," +
        "\"Amount\":{\"value\":\"1234567890123456\",\"currency\":\"USD\"," +
        "\"issuer\":\"rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw\"}," +
        "\"Destination\":\"rrrrrrrrrrrrrrrrrrrrBZbvji\",\"DestinationTag\":2," +
        "\"SigningPubKey\":\"ED5F5AC8B98974A3CA843326D9B88CEBD0560177B973EE0B149F782CFAA06DC66A\"," +
        "\"Signature\": \"12345678\"," +
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

  @ParameterizedTest
  @MethodSource("dataDrivenFixtures")
  void dataDriven(WholeObject wholeObject) throws IOException {
    assertThat(encoder.encode(wholeObject.txJson().toString())).isEqualTo(wholeObject.expectedHex());
  }

}
