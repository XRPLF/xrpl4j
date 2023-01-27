package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.ripple.cryptoconditions.CryptoConditionReader;
import com.ripple.cryptoconditions.der.DerEncodingException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.binary.XrplBinaryCodec;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.flags.OfferCreateFlags;
import org.xrpl.xrpl4j.model.flags.PaymentChannelClaimFlags;
import org.xrpl.xrpl4j.model.flags.PaymentFlags;
import org.xrpl.xrpl4j.model.flags.RippleStateFlags;
import org.xrpl.xrpl4j.model.flags.TrustSetFlags;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.ledger.RippleStateObject;
import org.xrpl.xrpl4j.model.ledger.SignerEntry;
import org.xrpl.xrpl4j.model.ledger.SignerEntryWrapper;
import org.xrpl.xrpl4j.model.transactions.AccountDelete;
import org.xrpl.xrpl4j.model.transactions.AccountSet;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CheckCancel;
import org.xrpl.xrpl4j.model.transactions.CheckCash;
import org.xrpl.xrpl4j.model.transactions.CheckCreate;
import org.xrpl.xrpl4j.model.transactions.CurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.DepositPreAuth;
import org.xrpl.xrpl4j.model.transactions.EscrowCancel;
import org.xrpl.xrpl4j.model.transactions.EscrowCreate;
import org.xrpl.xrpl4j.model.transactions.EscrowFinish;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.OfferCancel;
import org.xrpl.xrpl4j.model.transactions.OfferCreate;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.PaymentChannelClaim;
import org.xrpl.xrpl4j.model.transactions.PaymentChannelCreate;
import org.xrpl.xrpl4j.model.transactions.PaymentChannelFund;
import org.xrpl.xrpl4j.model.transactions.SetRegularKey;
import org.xrpl.xrpl4j.model.transactions.SignerListSet;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.TrustSet;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.math.BigDecimal;

public class BinarySerializationTests {

  ObjectMapper objectMapper = ObjectMapperFactory.create();
  XrplBinaryCodec binaryCodec = XrplBinaryCodec.getInstance();

  private static IssuedCurrencyAmount currencyAmount(int amount) {
    return IssuedCurrencyAmount.builder()
      .currency("WCG")
      .issuer(Address.of("rUx4xgE7bNWCCgGcXv1CCoQyTcCeZ275YG"))
      .value(amount + "")
      .build();
  }

  @Test
  public void serializeAccountSetTransaction() throws JsonProcessingException {
    AccountSet accountSet = AccountSet.builder()
      .account(Address.of("rpP2GdsQwenNnFPefbXFgiTvEgJWQpq8Rw"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(10598))
      .build();

    String expectedBinary = "1200032280000000240000296668400000000000000A730081140F3D0C7D2CFAB2EC8295451F0B3CA03" +
      "8E8E9CDCD";
    assertSerializesAndDeserializes(accountSet, expectedBinary);
  }

  @Test
  public void serializeAccountDelete() throws JsonProcessingException {
    AccountDelete accountDelete = AccountDelete.builder()
      .account(Address.of("rWYkbWkCeg8dP6rXALnjgZSjjLyih5NXm"))
      .fee(XrpCurrencyAmount.ofDrops(5000000))
      .sequence(UnsignedInteger.valueOf(2470665))
      .destination(Address.of("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe"))
      .destinationTag(UnsignedInteger.valueOf(13))
      .build();

    String expectedBinary = "1200152280000000240025B3092E0000000D6840000000004C4B40730081140596915CFDEEE" +
      "3A695B3EFD6BDA9AC788A368B7B8314F667B0CA50CC7709A220B0561B85E53A48461FA8";
    assertSerializesAndDeserializes(accountDelete, expectedBinary);
  }

  @Test
  public void serializeCheckCancel() throws JsonProcessingException {
    CheckCancel checkCancel = CheckCancel.builder()
      .account(Address.of("rUn84CUYbNjRoTQ6mSW7BVJPSVJNLb1QLo"))
      .checkId(Hash256.of("49647F0D748DC3FE26BDACBC57F251AADEFFF391403EC9BF87C97F67E9977FB0"))
      .sequence(UnsignedInteger.valueOf(12))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .build();

    String expectedBinary = "1200122280000000240000000C501849647F0D748DC3FE26BDACBC57F251AADEFFF391403EC9BF87C97" +
      "F67E9977FB068400000000000000C730081147990EC5D1D8DF69E070A968D4B186986FDF06ED0";
    assertSerializesAndDeserializes(checkCancel, expectedBinary);
  }

  @Test
  public void serializeCheckCashWithXrpAmount() throws JsonProcessingException {
    CheckCash checkCash = CheckCash.builder()
      .account(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .checkId(Hash256.of("838766BA2B995C00744175F69A1B11E32C3DBC40E64801A4056FCBD657F57334"))
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(12))
      .amount(XrpCurrencyAmount.ofDrops(100))
      .build();

    String expectedBinary = "120011228000000024000000015018838766BA2B995C00744175F69A1B11E32C3DBC40E64801A4" +
      "056FCBD657F5733461400000000000006468400000000000000C7300811449FF0C73CA6AF9733DA805F76CA2C37776B7C46B";
    assertSerializesAndDeserializes(checkCash, expectedBinary);
  }

  @Test
  public void serializeCheckCashWithXrpDeliverMin() throws JsonProcessingException {
    CheckCash checkCash = CheckCash.builder()
      .account(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .checkId(Hash256.of("838766BA2B995C00744175F69A1B11E32C3DBC40E64801A4056FCBD657F57334"))
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(12))
      .deliverMin(XrpCurrencyAmount.ofDrops(100))
      .build();

    String expectedBinary = "120011228000000024000000015018838766BA2B995C00744175F69A1B11E32C3DBC40E64801A4056" +
      "FCBD657F5733468400000000000000C6A40000000000000647300811449FF0C73CA6AF9733DA805F76CA2C37776B7C46B";
    assertSerializesAndDeserializes(checkCash, expectedBinary);
  }

  @Test
  public void serializeCheckCashWithIssuedCurrencyDeliverMin() throws JsonProcessingException {
    CheckCash checkCash = CheckCash.builder()
      .account(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .checkId(Hash256.of("838766BA2B995C00744175F69A1B11E32C3DBC40E64801A4056FCBD657F57334"))
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(12))
      .deliverMin(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
        .value("100")
        .build())
      .build();

    String expectedBinary = "120011228000000024000000015018838766BA2B995C00744175F69A1B11E32C3DBC40E64801A40" +
      "56FCBD657F5733468400000000000000C6AD5038D7EA4C68000000000000000000000000000555344000000000049FF0C73CA" +
      "6AF9733DA805F76CA2C37776B7C46B7300811449FF0C73CA6AF9733DA805F76CA2C37776B7C46B";
    assertSerializesAndDeserializes(checkCash, expectedBinary);
  }

  @Test
  void serializeCheckCreate() throws JsonProcessingException {
    CheckCreate checkCreate = CheckCreate.builder()
      .account(Address.of("rUn84CUYbNjRoTQ6mSW7BVJPSVJNLb1QLo"))
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(12))
      .destination(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .destinationTag(UnsignedInteger.ONE)
      .sendMax(XrpCurrencyAmount.ofDrops(100000000))
      .expiration(UnsignedInteger.valueOf(570113521))
      .invoiceId(Hash256.of("6F1DFD1D0FE8A32E40E1F2C05CF1C15545BAB56B617F9C6C2D63A6B704BEF59B"))
      .build();

    String expectedBinary = "120010228000000024000000012A21FB3DF12E0000000150116F1DFD1D0FE8A32E40E1F2C05CF1C" +
      "15545BAB56B617F9C6C2D63A6B704BEF59B68400000000000000C694000000005F5E100730081147990EC5D1D8DF69E070A96" +
      "8D4B186986FDF06ED0831449FF0C73CA6AF9733DA805F76CA2C37776B7C46B";
    assertSerializesAndDeserializes(checkCreate, expectedBinary);
  }

  @Test
  void serializeDepositPreAuth() throws JsonProcessingException {
    DepositPreAuth preAuth = DepositPreAuth.builder()
      .account(Address.of("rDd6FpNbeY2CrQajSmP178BmNGusmQiYMM"))
      .authorize(Address.of("rDJFnv5sEfp42LMFiX3mVQKczpFTdxYDzM"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(65))
      .build();

    String expectedBinary = "1200132280000000240000004168400000000000000A730081148A928D14A643F388AC0D26B" +
      "AF9755B07EB0A2B44851486FFE2A17E861BA0FE9A3ED8352F895D80E789E0";
    assertSerializesAndDeserializes(preAuth, expectedBinary);
  }

  @Test
  void serializeEscrowCreate() throws JsonProcessingException, DerEncodingException {
    EscrowCreate checkCreate = EscrowCreate.builder()
      .account(Address.of("r4jQDHCUvgcBAa5EzcB1D8BHGcjYP9eBC2"))
      .amount(XrpCurrencyAmount.ofDrops(100))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.ONE)
      .cancelAfter(UnsignedLong.valueOf(630000001))
      .finishAfter(UnsignedLong.valueOf(630000000))
      .destination(Address.of("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"))
      .destinationTag(UnsignedInteger.valueOf(23480))
      .condition(CryptoConditionReader.readCondition(BaseEncoding.base16().decode(
        "A0258020E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855810100"))
      )
      .build();

    String expectedBinary = "120001228000000024000000012E00005BB82024258D09812025258D0980614000000000000064684" +
      "00000000000000C7300701127A0258020E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855810100" +
      "8114EE5F7CF61504C7CF7E0C22562EB19CC7ACB0FCBA8314B5F762798A53D543A014CAF8B297CFF8F2F937E8";
    assertSerializesAndDeserializes(checkCreate, expectedBinary);
  }

  @Test
  void serializeEscrowCancel() throws JsonProcessingException {
    EscrowCancel escrowCancel = EscrowCancel.builder()
      .account(Address.of("r4jQDHCUvgcBAa5EzcB1D8BHGcjYP9eBC2"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("r4jQDHCUvgcBAa5EzcB1D8BHGcjYP9eBC2"))
      .offerSequence(UnsignedInteger.valueOf(25))
      .build();

    String expectedBinary = "1200042280000000240000000120190000001968400000000000000C73008114EE5F7CF61504C7" +
      "CF7E0C22562EB19CC7ACB0FCBA8214EE5F7CF61504C7CF7E0C22562EB19CC7ACB0FCBA";
    assertSerializesAndDeserializes(escrowCancel, expectedBinary);
  }

  @Test
  void serializeEscrowFinish() throws JsonProcessingException, DerEncodingException {
    EscrowFinish escrowFinish = EscrowFinish.builder()
      .account(Address.of("rMYPppnVNQ7crMizv8D6wF45kYuSupygyr"))
      .fee(XrpCurrencyAmount.ofDrops(330))
      .sequence(UnsignedInteger.valueOf(3))
      .owner(Address.of("rMYPppnVNQ7crMizv8D6wF45kYuSupygyr"))
      .offerSequence(UnsignedInteger.valueOf(25))
      .condition(CryptoConditionReader.readCondition(BaseEncoding.base16().decode(
        "A0258020E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855810100")
      ))
      .fulfillment(CryptoConditionReader.readFulfillment(BaseEncoding.base16().decode("A0028000")))
      .build();

    String expectedBinary = "1200022280000000240000000320190000001968400000000000014A7300701004A0028000701127A02" +
      "58020E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B8558101008114E151CA3207BAB5B91D2F0E4D35" +
      "ECDFD4551C69A18214E151CA3207BAB5B91D2F0E4D35ECDFD4551C69A1";
    assertSerializesAndDeserializes(escrowFinish, expectedBinary);
  }

  @Test
  void serializeXrpPayment() throws JsonProcessingException {
    Address source = Address.builder()
      .value("r45dBj4S3VvMMYXxr9vHX4Z4Ma6ifPMCkK")
      .build();

    Address destination = Address.builder()
      .value("rrrrrrrrrrrrrrrrrrrrBZbvji")
      .build();

    Payment payment = Payment.builder()
      .account(source)
      .destination(destination)
      .sourceTag(UnsignedInteger.valueOf(1))
      .destinationTag(UnsignedInteger.valueOf(2))
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .fee(XrpCurrencyAmount.ofDrops(789))
      .sequence(UnsignedInteger.valueOf(56565656))
      .build();

    String expectedBinary = "1200002280000000230000000124035F1F982E0000000261400000000000303968" +
      "400000000000031573008114EE39E6D05CFD6A90DAB700A1D70149ECEE29DFEC83140000000000000000000000000000000000000001";
    assertSerializesAndDeserializes(payment, expectedBinary);
  }

  @Test
  void serializeIssuedCurrencyPayment() throws JsonProcessingException {
    Address source = Address.builder()
      .value("r45dBj4S3VvMMYXxr9vHX4Z4Ma6ifPMCkK")
      .build();

    Address destination = Address.builder()
      .value("rrrrrrrrrrrrrrrrrrrrBZbvji")
      .build();

    Address issuer = Address.builder()
      .value("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw")
      .build();

    CurrencyAmount amount = IssuedCurrencyAmount.builder()
      .currency("USD")
      .issuer(issuer)
      .value("1234567890123456")
      .build();

    Payment payment = Payment.builder()
      .flags(PaymentFlags.builder()
        .tfPartialPayment(true)
        .build())
      .account(source)
      .destination(destination)
      .sourceTag(UnsignedInteger.valueOf(1))
      .destinationTag(UnsignedInteger.valueOf(2))
      .amount(amount)
      .fee(XrpCurrencyAmount.ofDrops(789))
      .sequence(UnsignedInteger.valueOf(1))
      .build();

    String expectedBinary = "1200002280020000230000000124000000012E0000000261D84462D53C8ABAC000000000000000" +
      "000000000055534400000000008B1CE810C13D6F337DAC85863B3D70265A24DF4468400000000000031573008114EE39E6D" +
      "05CFD6A90DAB700A1D70149ECEE29DFEC83140000000000000000000000000000000000000001";
    assertSerializesAndDeserializes(payment, expectedBinary);
  }

  @Test
  void serializePaymentChannelCreate() throws JsonProcessingException {
    PaymentChannelCreate create = PaymentChannelCreate.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .sourceTag(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .amount(XrpCurrencyAmount.ofDrops(10000))
      .destination(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .destinationTag(UnsignedInteger.valueOf(2))
      .settleDelay(UnsignedInteger.ONE)
      .publicKey("32D2471DB72B27E3310F355BB33E339BF26F8392D5A93D3BC0FC3B566612DA0F0A")
      .cancelAfter(UnsignedLong.valueOf(533171558))
      .build();

    String expectedBinary = "12000D2280000000230000000124000000012E0000000220241FC78D66202700000001614000" +
      "000000002710684000000000000064712132D2471DB72B27E3310F355BB33E339BF26F8392D5A93D3BC0FC3B566612DA0" +
      "F0A730081144B4E9C06F24296074F7BC48F92A97916C6DC5EA983144B4E9C06F24296074F7BC48F92A97916C6DC5EA9";
    assertSerializesAndDeserializes(create, expectedBinary);
  }

  @Test
  void serializePaymentChannelClaim() throws JsonProcessingException {
    PaymentChannelClaim claim = PaymentChannelClaim.builder()
      .account(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .flags(PaymentChannelClaimFlags.builder().tfClose(true).build())
      .channel(Hash256.of("C1AE6DDDEEC05CF2978C0BAD6FE302948E9533691DC749DCDD3B9E5992CA6198"))
      .balance(XrpCurrencyAmount.ofDrops(1000000))
      .amount(XrpCurrencyAmount.ofDrops(1000000))
      .signature("30440220718D264EF05CAED7C781FF6DE298DCAC68D002562C9BF3A07C1E721B420C0DAB02203A5A4779E" +
        "F4D2CCC7BC3EF886676D803A9981B928D3B8ACA483B80ECA3CD7B9B")
      .publicKey("32D2471DB72B27E3310F355BB33E339BF26F8392D5A93D3BC0FC3B566612DA0F0A")
      .build();

    String expectedBinary = "12000F228002000024000000015016C1AE6DDDEEC05CF2978C0BAD6FE302948E9533691DC749D" +
      "CDD3B9E5992CA61986140000000000F42406240000000000F424068400000000000000A712132D2471DB72B27E3310F355B" +
      "B33E339BF26F8392D5A93D3BC0FC3B566612DA0F0A7300764630440220718D264EF05CAED7C781FF6DE298DCAC68D002562" +
      "C9BF3A07C1E721B420C0DAB02203A5A4779EF4D2CCC7BC3EF886676D803A9981B928D3B8ACA483B80ECA3CD7B9B81142042" +
      "88D2E47F8EF6C99BCC457966320D12409711";

    assertSerializesAndDeserializes(claim, expectedBinary);
  }

  @Test
  void serializePaymentChannelFund() throws JsonProcessingException {
    PaymentChannelFund fund = PaymentChannelFund.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .channel(Hash256.of("C1AE6DDDEEC05CF2978C0BAD6FE302948E9533691DC749DCDD3B9E5992CA6198"))
      .amount(XrpCurrencyAmount.ofDrops(200000))
      .expiration(UnsignedLong.valueOf(543171558))
      .build();

    String expectedJson = "12000E228000000024000000012A206023E65016C1AE6DDDEEC05CF2978C0BAD6FE302948E9533691DC7" +
      "49DCDD3B9E5992CA6198614000000000030D4068400000000000000A730081144B4E9C06F24296074F7BC48F92A97916C6DC5EA9";

    assertSerializesAndDeserializes(fund, expectedJson);
  }

  @Test
  void serializeTrustLine() throws JsonProcessingException {
    TrustSet trustSet = TrustSet.builder()
      .account(Address.of("rJMiz2rCMjZzEMijXNH1exNBryTQEjFd9S"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .flags(TrustSetFlags.builder()
        .tfSetNoRipple()
        .build())
      .sequence(UnsignedInteger.valueOf(44))
      .limitAmount(IssuedCurrencyAmount.builder()
        .currency("WCG")
        .issuer(Address.of("rUx4xgE7bNWCCgGcXv1CCoQyTcCeZ275YG"))
        .value("10000000")
        .build())
      .build();


    String expectedBinary = "1200142280020000240000002C63D6438D7EA4C68000000000000000000000000000574347000000" +
      "0000832297BEF589D59F9C03A84F920F8D9128CC1CE468400000000000000C73008114BE6C30732AE33CF2AF3344CE8172A6B9" +
      "300183E3";
    assertSerializesAndDeserializes(trustSet, expectedBinary);
  }

  @Test
  void serializeTrustSetWithMaxValue() throws JsonProcessingException {
    TrustSet trustSet = TrustSet.builder()
      .account(Address.of("rBZwgHHifSeFmf2btuubr6YUhZ3FfW47XE"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(5))
      .limitAmount(IssuedCurrencyAmount.builder()
        .currency("7872706C346A436F696E00000000000000000000")
        .issuer(Address.of("rwk7evAoUG1YoH1k3KVps3Zm1MtHuwHadj"))
        .value(IssuedCurrencyAmount.MAX_VALUE)
        .build())
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("EDD299D60BCE7980F6082945B5597FFFD35223F1950673BFA4D4AED6FDE5097156")
      )
      .build();

    String expectedBinary = "1200142280000000240000000563EC6386F26FC0FFFF7872706C346A436F696E00000000000000000000" +
      "6AF21A4FCA7AF4F62AA88E72CA923CDAE72FBC1668400000000000000A7321EDD299D60BCE7980F6082945B5597FFFD35223F19506" +
      "73BFA4D4AED6FDE5097156811473C6A5937EB779F5CF33600DB0ADB8556A497318";
    String transactionJson = objectMapper.writeValueAsString(trustSet);
    String transactionBinary = binaryCodec.encode(transactionJson);
    assertThat(transactionBinary).isEqualTo(expectedBinary);

    String decodedBinary = binaryCodec.decode(transactionBinary);
    TrustSet deserialized = objectMapper.readValue(
      decodedBinary,
      objectMapper.getTypeFactory().constructType(TrustSet.class)
    );

    assertThat(deserialized).usingRecursiveComparison().ignoringFields("limitAmount").isEqualTo(trustSet);
    BigDecimal deserializedValue = new BigDecimal(deserialized.limitAmount().value());
    BigDecimal originalValue = new BigDecimal(trustSet.limitAmount().value());
    assertThat(deserializedValue.toPlainString()).isEqualTo(originalValue.toPlainString());
  }

  @Test
  void serializeTrustSetWithMaxValueMinusOnePowerOfTen() throws JsonProcessingException {
    TrustSet trustSet = TrustSet.builder()
      .account(Address.of("r4V5fhT5KPCdhUanXCMpPUHiM4s6bXDVDH"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(5))
      .limitAmount(IssuedCurrencyAmount.builder()
        .currency("7872706C346A436F696E00000000000000000000")
        .issuer(Address.of("rUcccYgErfgMifUHYDHed4ibTArjqfkU5R"))
        .value(new BigDecimal(IssuedCurrencyAmount.MAX_VALUE).scaleByPowerOfTen(-1).toString())
        .build())
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED930B5CBBBCA68DE43CC928E4F3860151E1E5F10D52478B38512651363F4DE8D0")
      )
      .build();

    String expectedBinary = "1200142280000000240000000563EC2386F26FC0FFFF7872706C346A436F696E00000000000000000000" +
      "7F65AB9FC20D6D3140698670A12869D6A7D9EE9568400000000000000A7321ED930B5CBBBCA68DE43CC928E4F3860151E1E5F10D52" +
      "478B38512651363F4DE8D08114EBC899C616EE2D4765C3CBBEB51E6131F701537C";
    String transactionJson = objectMapper.writeValueAsString(trustSet);
    String transactionBinary = binaryCodec.encode(transactionJson);
    assertThat(transactionBinary).isEqualTo(expectedBinary);

    String decodedBinary = binaryCodec.decode(transactionBinary);
    TrustSet deserialized = objectMapper.readValue(
      decodedBinary,
      objectMapper.getTypeFactory().constructType(TrustSet.class)
    );

    assertThat(deserialized).usingRecursiveComparison().ignoringFields("limitAmount").isEqualTo(trustSet);
    BigDecimal deserializedValue = new BigDecimal(deserialized.limitAmount().value());
    BigDecimal originalValue = new BigDecimal(trustSet.limitAmount().value());
    assertThat(deserializedValue.toPlainString()).isEqualTo(originalValue.toPlainString());
  }

  @Test
  void serializeTrustSetWithMaxValuePlusOnePowerOfTenThrows() throws JsonProcessingException {
    TrustSet trustSet = TrustSet.builder()
      .account(Address.of("rJMiz2rCMjZzEMijXNH1exNBryTQEjFd9S"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .flags(TrustSetFlags.builder()
        .tfSetNoRipple()
        .build())
      .sequence(UnsignedInteger.valueOf(44))
      .limitAmount(IssuedCurrencyAmount.builder()
        .currency("WCG")
        .issuer(Address.of("rUx4xgE7bNWCCgGcXv1CCoQyTcCeZ275YG"))
        .value(new BigDecimal(IssuedCurrencyAmount.MAX_VALUE).scaleByPowerOfTen(1).toEngineeringString())
        .build())
      .build();

    String transactionJson = objectMapper.writeValueAsString(trustSet);
    assertThatThrownBy(() -> binaryCodec.encode(transactionJson))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("exponent out of range");
  }

  @Test
  void serializeTrustSetWithMaxValuePlusOneThrows() throws JsonProcessingException {
    TrustSet trustSet = TrustSet.builder()
      .account(Address.of("rJMiz2rCMjZzEMijXNH1exNBryTQEjFd9S"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .flags(TrustSetFlags.builder()
        .tfSetNoRipple()
        .build())
      .sequence(UnsignedInteger.valueOf(44))
      .limitAmount(IssuedCurrencyAmount.builder()
        .currency("WCG")
        .issuer(Address.of("rUx4xgE7bNWCCgGcXv1CCoQyTcCeZ275YG"))
        .value(new BigDecimal(IssuedCurrencyAmount.MAX_VALUE).add(BigDecimal.ONE).toEngineeringString())
        .build())
      .build();

    String transactionJson = objectMapper.writeValueAsString(trustSet);
    assertThatThrownBy(() -> binaryCodec.encode(transactionJson))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageEndingWith("has more than 16 digits");
  }

  @Test
  void serializeTrustSetWithMinPositiveValue() throws JsonProcessingException {
    TrustSet trustSet = TrustSet.builder()
      .account(Address.of("rLmnUtinq987W2LWcqz5Ty5BXgCUFG1PX4"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(5))
      .limitAmount(IssuedCurrencyAmount.builder()
        .currency("7872706C346A436F696E00000000000000000000")
        .issuer(Address.of("rEPgyyv4xdWkKuavTmHLYPV2Ryqt7NNmH"))
        .value(IssuedCurrencyAmount.MIN_POSITIVE_VALUE)
        .build())
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED55D61E72F14EC798ABC10BC96381573717658B46F58E416EC0C74A467835AB4D")
      )
      .build();

    String expectedBinary = "1200142280000000240000000563C0438D7EA4C680007872706C346A436F696E00000000000" +
      "00000000002B8C37A261B75FC294AC84B40D249456715277D68400000000000000A7321ED55D61E72F14EC798ABC10BC9" +
      "6381573717658B46F58E416EC0C74A467835AB4D8114D8C033D20D216FD8939778C229F8BD9CF9585EAC";
    String transactionJson = objectMapper.writeValueAsString(trustSet);
    String transactionBinary = binaryCodec.encode(transactionJson);
    assertThat(transactionBinary).isEqualTo(expectedBinary);

    String decodedBinary = binaryCodec.decode(transactionBinary);
    TrustSet deserialized = objectMapper.readValue(
      decodedBinary,
      TrustSet.class
    );

    assertThat(deserialized).usingRecursiveComparison().ignoringFields("limitAmount").isEqualTo(trustSet);
    BigDecimal deserializedValue = new BigDecimal(deserialized.limitAmount().value());
    BigDecimal originalValue = new BigDecimal(trustSet.limitAmount().value());
    assertThat(deserializedValue.setScale(96).toString()).isEqualTo(originalValue.toString());
  }

  @Test
  void serializeTrustSetWithMinPositiveValuePlusOne() throws JsonProcessingException {
    BigDecimal value = new BigDecimal(IssuedCurrencyAmount.MIN_POSITIVE_VALUE)
      .add(new BigDecimal(IssuedCurrencyAmount.MIN_POSITIVE_VALUE).scaleByPowerOfTen(-1));
    TrustSet trustSet = TrustSet.builder()
      .account(Address.of("rQrbagUaGHMTvbveDB3XBHyKB4pmWzzAup"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(5))
      .limitAmount(IssuedCurrencyAmount.builder()
        .currency("7872706C346A436F696E00000000000000000000")
        .issuer(Address.of("r4e6zHaTwNK5WLtRdFiSY3UitarXqCMyoD"))
        .value(value.toString())
        .build())
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED098659726D4B0CDCD35E37135766FF2038579291351CC08DE70C32851D049A83")
      )
      .build();

    String expectedBinary = "1200142280000000240000000563C043E871B540C0007872706C346A436F696E000000000000000" +
      "00000ED7C3F2940D37A688A1D3F9AB883A8E3AB4D5FE168400000000000000A7321ED098659726D4B0CDCD35E37135766FF20" +
      "38579291351CC08DE70C32851D049A838114FC675B42929326571785885ECD4E61FA82F4AAAC";
    String transactionJson = objectMapper.writeValueAsString(trustSet);
    String transactionBinary = binaryCodec.encode(transactionJson);
    assertThat(transactionBinary).isEqualTo(expectedBinary);

    String decodedBinary = binaryCodec.decode(transactionBinary);
    TrustSet deserialized = objectMapper.readValue(
      decodedBinary,
      TrustSet.class
    );

    assertThat(deserialized).usingRecursiveComparison().ignoringFields("limitAmount").isEqualTo(trustSet);
    BigDecimal deserializedValue = new BigDecimal(deserialized.limitAmount().value());
    BigDecimal originalValue = new BigDecimal(trustSet.limitAmount().value());
    assertThat(deserializedValue.setScale(96).toString()).isEqualTo(originalValue.setScale(96).toString());
  }

  @Test
  void serializeTrustSetWithMinPositiveValueMinusOneThrows() throws JsonProcessingException {
    BigDecimal value = new BigDecimal(IssuedCurrencyAmount.MIN_POSITIVE_VALUE)
      .subtract(new BigDecimal(IssuedCurrencyAmount.MIN_POSITIVE_VALUE).scaleByPowerOfTen(-1));
    TrustSet trustSet = TrustSet.builder()
      .account(Address.of("rQrbagUaGHMTvbveDB3XBHyKB4pmWzzAup"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(5))
      .limitAmount(IssuedCurrencyAmount.builder()
        .currency("7872706C346A436F696E00000000000000000000")
        .issuer(Address.of("r4e6zHaTwNK5WLtRdFiSY3UitarXqCMyoD"))
        .value(value.toString())
        .build())
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED098659726D4B0CDCD35E37135766FF2038579291351CC08DE70C32851D049A83")
      )
      .build();

    String transactionJson = objectMapper.writeValueAsString(trustSet);
    assertThatThrownBy(() -> binaryCodec.encode(transactionJson))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("exponent out of range");
  }

  @Test
  void serializeRippleStateObjectWithMinBalanceValue() throws JsonProcessingException {
    RippleStateObject rippleStateObject = RippleStateObject.builder()
      .flags(RippleStateFlags.HIGH_NO_RIPPLE)
      .balance(
        IssuedCurrencyAmount.builder()
          .issuer(Address.of("rJMiz2rCMjZzEMijXNH1exNBryTQEjFd9S"))
          .value(IssuedCurrencyAmount.MIN_VALUE)
          .currency("USD")
          .build()
      )
      .lowLimit(
        IssuedCurrencyAmount.builder()
          .issuer(Address.of("rJMiz2rCMjZzEMijXNH1exNBryTQEjFd9S"))
          .value(IssuedCurrencyAmount.MAX_VALUE)
          .currency("USD")
          .build()
      )
      .highLimit(
        IssuedCurrencyAmount.builder()
          .issuer(Address.of("rJMiz2rCMjZzEMijXNH1exNBryTQEjFd9S"))
          .value(IssuedCurrencyAmount.MIN_VALUE)
          .currency("USD")
          .build()
      )
      .previousTransactionId(Hash256.of(Strings.repeat("0", 64)))
      .previousTransactionLedgerSequence(UnsignedInteger.ONE)
      .index(Hash256.of(Strings.repeat("0", 64)))
      .build();

    String expectedBinary = "1100722200200000250000000155000000000000000000000000000000000000000000000000000000000" +
      "000000062AC6386F26FC0FFFF0000000000000000000000005553440000000000BE6C30732AE33CF2AF3344CE8172A6B9300183E366" +
      "EC6386F26FC0FFFF0000000000000000000000005553440000000000BE6C30732AE33CF2AF3344CE8172A6B9300183E367AC6386F26" +
      "FC0FFFF0000000000000000000000005553440000000000BE6C30732AE33CF2AF3344CE8172A6B9300183E3";
    String transactionJson = objectMapper.writeValueAsString(rippleStateObject);
    String transactionBinary = binaryCodec.encode(transactionJson);
    assertThat(transactionBinary).isEqualTo(expectedBinary);
  }

  @Test
  void serializeRippleStateObjectWithMinBalanceValuePlusOne() throws JsonProcessingException {
    String value = new BigDecimal(IssuedCurrencyAmount.MIN_VALUE).subtract(
        new BigDecimal("-0.100000000000000E+95"))
      .toString();
    RippleStateObject rippleStateObject = RippleStateObject.builder()
      .flags(RippleStateFlags.HIGH_NO_RIPPLE)
      .balance(
        IssuedCurrencyAmount.builder()
          .issuer(Address.of("rJMiz2rCMjZzEMijXNH1exNBryTQEjFd9S"))
          .value(
            value
          )
          .currency("USD")
          .build()
      )
      .lowLimit(
        IssuedCurrencyAmount.builder()
          .issuer(Address.of("rJMiz2rCMjZzEMijXNH1exNBryTQEjFd9S"))
          .value(IssuedCurrencyAmount.MAX_VALUE)
          .currency("USD")
          .build()
      )
      .highLimit(
        IssuedCurrencyAmount.builder()
          .issuer(Address.of("rJMiz2rCMjZzEMijXNH1exNBryTQEjFd9S"))
          .value(IssuedCurrencyAmount.MIN_VALUE)
          .currency("USD")
          .build()
      )
      .previousTransactionId(Hash256.of(Strings.repeat("0", 64)))
      .previousTransactionLedgerSequence(UnsignedInteger.ONE)
      .index(Hash256.of(Strings.repeat("0", 64)))
      .build();

    String expectedBinary = "11007222002000002500000001550000000000000000000000000000000000000000000000000000000" +
      "00000000062AC632BFF5F46BFFF0000000000000000000000005553440000000000BE6C30732AE33CF2AF3344CE8172A6B9300183" +
      "E366EC6386F26FC0FFFF0000000000000000000000005553440000000000BE6C30732AE33CF2AF3344CE8172A6B9300183E367AC6" +
      "386F26FC0FFFF0000000000000000000000005553440000000000BE6C30732AE33CF2AF3344CE8172A6B9300183E3";
    String transactionJson = objectMapper.writeValueAsString(rippleStateObject);
    String transactionBinary = binaryCodec.encode(transactionJson);
    assertThat(transactionBinary).isEqualTo(expectedBinary);
  }

  @Test
  void serializeRippleStateObjectWithMinBalanceValueMinusOneThrows() throws JsonProcessingException {
    String value = new BigDecimal(IssuedCurrencyAmount.MIN_VALUE).subtract(new BigDecimal("1e80"))
      .toString();
    RippleStateObject rippleStateObject = RippleStateObject.builder()
      .flags(RippleStateFlags.HIGH_NO_RIPPLE)
      .balance(
        IssuedCurrencyAmount.builder()
          .issuer(Address.of("rJMiz2rCMjZzEMijXNH1exNBryTQEjFd9S"))
          .value(
            value
          )
          .currency("USD")
          .build()
      )
      .lowLimit(
        IssuedCurrencyAmount.builder()
          .issuer(Address.of("rJMiz2rCMjZzEMijXNH1exNBryTQEjFd9S"))
          .value(IssuedCurrencyAmount.MAX_VALUE)
          .currency("USD")
          .build()
      )
      .highLimit(
        IssuedCurrencyAmount.builder()
          .issuer(Address.of("rJMiz2rCMjZzEMijXNH1exNBryTQEjFd9S"))
          .value(IssuedCurrencyAmount.MIN_VALUE)
          .currency("USD")
          .build()
      )
      .previousTransactionId(Hash256.of(Strings.repeat("0", 64)))
      .previousTransactionLedgerSequence(UnsignedInteger.ONE)
      .index(Hash256.of(Strings.repeat("0", 64)))
      .build();

    String transactionJson = objectMapper.writeValueAsString(rippleStateObject);
    assertThatThrownBy(
      () -> binaryCodec.encode(transactionJson)
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("exponent out of range");
  }

  @Test
  void serializeRippleStateObjectWithMaxNegativeValue() throws JsonProcessingException {
    RippleStateObject rippleStateObject = RippleStateObject.builder()
      .flags(RippleStateFlags.HIGH_NO_RIPPLE)
      .balance(
        IssuedCurrencyAmount.builder()
          .issuer(Address.of("rJMiz2rCMjZzEMijXNH1exNBryTQEjFd9S"))
          .value(IssuedCurrencyAmount.MAX_NEGATIVE_VALUE)
          .currency("USD")
          .build()
      )
      .lowLimit(
        IssuedCurrencyAmount.builder()
          .issuer(Address.of("rJMiz2rCMjZzEMijXNH1exNBryTQEjFd9S"))
          .value(IssuedCurrencyAmount.MAX_VALUE)
          .currency("USD")
          .build()
      )
      .highLimit(
        IssuedCurrencyAmount.builder()
          .issuer(Address.of("rJMiz2rCMjZzEMijXNH1exNBryTQEjFd9S"))
          .value(IssuedCurrencyAmount.MIN_VALUE)
          .currency("USD")
          .build()
      )
      .previousTransactionId(Hash256.of(Strings.repeat("0", 64)))
      .previousTransactionLedgerSequence(UnsignedInteger.ONE)
      .index(Hash256.of(Strings.repeat("0", 64)))
      .build();

    String expectedBinary = "110072220020000025000000015500000000000000000000000000000000000000000000000000" +
      "000000000000006280438D7EA4C680000000000000000000000000005553440000000000BE6C30732AE33CF2AF3344CE8172" +
      "A6B9300183E366EC6386F26FC0FFFF0000000000000000000000005553440000000000BE6C30732AE33CF2AF3344CE8172A6" +
      "B9300183E367AC6386F26FC0FFFF0000000000000000000000005553440000000000BE6C30732AE33CF2AF3344CE8172A6B9" +
      "300183E3";
    String transactionJson = objectMapper.writeValueAsString(rippleStateObject);
    String transactionBinary = binaryCodec.encode(transactionJson);
    assertThat(transactionBinary).isEqualTo(expectedBinary);
  }

  @Test
  void serializeRippleStateObjectWithMaxNegativeValueMinusOne() throws JsonProcessingException {
    String value = new BigDecimal(IssuedCurrencyAmount.MAX_NEGATIVE_VALUE)
      .add(new BigDecimal(IssuedCurrencyAmount.MAX_NEGATIVE_VALUE).scaleByPowerOfTen(-1)).toString();
    RippleStateObject rippleStateObject = RippleStateObject.builder()
      .flags(RippleStateFlags.HIGH_NO_RIPPLE)
      .balance(
        IssuedCurrencyAmount.builder()
          .issuer(Address.of("rJMiz2rCMjZzEMijXNH1exNBryTQEjFd9S"))
          .value(value)
          .currency("USD")
          .build()
      )
      .lowLimit(
        IssuedCurrencyAmount.builder()
          .issuer(Address.of("rJMiz2rCMjZzEMijXNH1exNBryTQEjFd9S"))
          .value(IssuedCurrencyAmount.MAX_VALUE)
          .currency("USD")
          .build()
      )
      .highLimit(
        IssuedCurrencyAmount.builder()
          .issuer(Address.of("rJMiz2rCMjZzEMijXNH1exNBryTQEjFd9S"))
          .value(IssuedCurrencyAmount.MIN_VALUE)
          .currency("USD")
          .build()
      )
      .previousTransactionId(Hash256.of(Strings.repeat("0", 64)))
      .previousTransactionLedgerSequence(UnsignedInteger.ONE)
      .index(Hash256.of(Strings.repeat("0", 64)))
      .build();

    String expectedBinary = "11007222002000002500000001550000000000000000000000000000000000000000000000000000000" +
      "000000000628043E871B540C0000000000000000000000000005553440000000000BE6C30732AE33CF2AF3344CE8172A6B9300183" +
      "E366EC6386F26FC0FFFF0000000000000000000000005553440000000000BE6C30732AE33CF2AF3344CE8172A6B9300183E367AC" +
      "6386F26FC0FFFF0000000000000000000000005553440000000000BE6C30732AE33CF2AF3344CE8172A6B9300183E3";
    String transactionJson = objectMapper.writeValueAsString(rippleStateObject);
    String transactionBinary = binaryCodec.encode(transactionJson);
    assertThat(transactionBinary).isEqualTo(expectedBinary);
  }

  @Test
  void serializeRippleStateObjectWithMaxNegativeValuePlusOneThrows() throws JsonProcessingException {
    String value = new BigDecimal(IssuedCurrencyAmount.MAX_NEGATIVE_VALUE)
      .subtract(new BigDecimal(IssuedCurrencyAmount.MAX_NEGATIVE_VALUE).scaleByPowerOfTen(-1)).toString();
    RippleStateObject rippleStateObject = RippleStateObject.builder()
      .flags(RippleStateFlags.HIGH_NO_RIPPLE)
      .balance(
        IssuedCurrencyAmount.builder()
          .issuer(Address.of("rJMiz2rCMjZzEMijXNH1exNBryTQEjFd9S"))
          .value(value)
          .currency("USD")
          .build()
      )
      .lowLimit(
        IssuedCurrencyAmount.builder()
          .issuer(Address.of("rJMiz2rCMjZzEMijXNH1exNBryTQEjFd9S"))
          .value(IssuedCurrencyAmount.MAX_VALUE)
          .currency("USD")
          .build()
      )
      .highLimit(
        IssuedCurrencyAmount.builder()
          .issuer(Address.of("rJMiz2rCMjZzEMijXNH1exNBryTQEjFd9S"))
          .value(IssuedCurrencyAmount.MIN_VALUE)
          .currency("USD")
          .build()
      )
      .previousTransactionId(Hash256.of(Strings.repeat("0", 64)))
      .previousTransactionLedgerSequence(UnsignedInteger.ONE)
      .index(Hash256.of(Strings.repeat("0", 64)))
      .build();

    String transactionJson = objectMapper.writeValueAsString(rippleStateObject);
    assertThatThrownBy(() -> binaryCodec.encode(transactionJson))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("exponent out of range");
  }

  @Test
  public void serializeOfferCreate() throws JsonProcessingException {
    OfferCreate offerCreate = OfferCreate.builder()
      .takerGets(currencyAmount(100))
      .takerPays(currencyAmount(200))
      .fee(XrpCurrencyAmount.ofDrops(300))
      .account(Address.of("rUx4xgE7bNWCCgGcXv1CCoQyTcCeZ275YG"))
      .sequence(UnsignedInteger.valueOf(11223344))
      .offerSequence(UnsignedInteger.valueOf(123))
      .flags(OfferCreateFlags.builder().tfSell(true).build())
      .expiration(UnsignedInteger.valueOf(456))
      .build();

    String expectedBinary = "12000722800800002400AB41302A000001C820190000007B64D5071AFD498D0000000000000000000" +
      "0000000005743470000000000832297BEF589D59F9C03A84F920F8D9128CC1CE465D5038D7EA4C6800000000000000000000000" +
      "00005743470000000000832297BEF589D59F9C03A84F920F8D9128CC1CE468400000000000012C73008114832297BEF589D59F9" +
      "C03A84F920F8D9128CC1CE4";
    assertSerializesAndDeserializes(offerCreate, expectedBinary);
  }

  @Test
  public void serializeOfferCancel() throws JsonProcessingException {
    OfferCancel offerCreate = OfferCancel.builder()
      .fee(XrpCurrencyAmount.ofDrops(300))
      .account(Address.of("rUx4xgE7bNWCCgGcXv1CCoQyTcCeZ275YG"))
      .sequence(UnsignedInteger.valueOf(11223344))
      .offerSequence(UnsignedInteger.valueOf(123))
      .build();

    String expectedBinary = "12000822800000002400AB413020190000007B68400000000000012C73008114832297BEF589" +
      "D59F9C03A84F920F8D9128CC1CE4";
    assertSerializesAndDeserializes(offerCreate, expectedBinary);
  }

  @Test
  public void serializeSetRegularKey() throws JsonProcessingException {
    SetRegularKey setRegularKey = SetRegularKey.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.ONE)
      .regularKey(Address.of("rAR8rR8sUkBoCZFawhkWzY4Y5YoyuznwD"))
      .build();

    String expectedBinary = "1200052280000000240000000168400000000000000C730081144B4E9C06F24296074F7BC48F92" +
      "A97916C6DC5EA988140A4B24D606281E6E5A78D9F80E039F5E66FA5AC5";

    assertSerializesAndDeserializes(setRegularKey, expectedBinary);
  }

  @Test
  void serializeSignerListSet() throws JsonProcessingException {
    SignerListSet signerListSet = SignerListSet.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.ONE)
      .signerQuorum(UnsignedInteger.valueOf(3))
      .addSignerEntries(
        SignerEntryWrapper.of(
          SignerEntry.builder()
            .account(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
            .signerWeight(UnsignedInteger.valueOf(2))
            .build()
        ),
        SignerEntryWrapper.of(
          SignerEntry.builder()
            .account(Address.of("rUpy3eEg8rqjqfUoLeBnZkscbKbFsKXC3v"))
            .signerWeight(UnsignedInteger.ONE)
            .build()
        ),
        SignerEntryWrapper.of(
          SignerEntry.builder()
            .account(Address.of("raKEEVSGnKSD9Zyvxu4z6Pqpm4ABH8FS6n"))
            .signerWeight(UnsignedInteger.ONE)
            .build()
        )
      )
      .build();

    String expectedBinary = "12000C2280000000240000000120230000000368400000000000000C730081144B4E9C06F24296074" +
      "F7BC48F92A97916C6DC5EA9F4EB1300028114204288D2E47F8EF6C99BCC457966320D12409711E1EB13000181147908A7F0EDD4" +
      "8EA896C3580A399F0EE78611C8E3E1EB13000181143A4C02EA95AD6AC3BED92FA036E0BBFB712C030CE1F1";

    assertSerializesAndDeserializes(signerListSet, expectedBinary);
  }

  private <T extends Transaction> void assertSerializesAndDeserializes(
    T transaction,
    String expectedBinary
  ) throws JsonProcessingException {
    String transactionJson = objectMapper.writeValueAsString(transaction);
    String transactionBinary = binaryCodec.encode(transactionJson);
    assertThat(transactionBinary).isEqualTo(expectedBinary);

    String decodedBinary = binaryCodec.decode(transactionBinary);
    T deserialized = objectMapper.readValue(
      decodedBinary,
      objectMapper.getTypeFactory().constructType(transaction.getClass())
    );
    assertThat(deserialized).isEqualTo(transaction);
  }

}
