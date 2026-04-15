package org.xrpl.xrpl4j.model.transactions.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.LoanSetFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.AssetAmount;
import org.xrpl.xrpl4j.model.transactions.CounterpartySignature;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.LoanData;
import org.xrpl.xrpl4j.model.transactions.LoanSet;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * Unit tests for {@link LoanSet} JSON serialization.
 */
public class LoanSetJsonTest extends AbstractJsonTest {

  @Test
  public void testLoanSetJsonWithAllFields()
    throws JsonProcessingException, JSONException {

    LoanSet loanSet = LoanSet.builder()
      .account(Address.of("rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8"))
      .fee(XrpCurrencyAmount.ofDrops(45))
      .sequence(UnsignedInteger.valueOf(190))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey(
          "ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD"
        )
      )
      .loanBrokerId(Hash256.of(
        "79E25403E9FC010A277D80410EED5494FDD033A09FD4C1432335A1734A1D099D"
      ))
      .counterparty(Address.of("rEePKs9pVMf91vYj1QVRPmJvCBEum9P2kE"))
      .counterpartySignature(
        CounterpartySignature.builder()
          .signingPubKey(
            "ED94BE884DB13EA26792F7DE6E8DAAEE9151BD90E0362C7D5C1898D272A7E56A00"
          )
          .txnSignature(
            "E0B7599656AF32847D4695F836FF1767F036F6E1232115E9295264C8A614FB3C" +
            "1BA3DF45D92304038032EC7BD5D6A901B041A4C74CF28838D205B7E51E08B602"
          )
          .build()
      )
      .data(LoanData.of("AABBCC"))
      .loanOriginationFee(AssetAmount.of("100"))
      .loanServiceFee(AssetAmount.of("50"))
      .latePaymentFee(AssetAmount.of("25"))
      .closePaymentFee(AssetAmount.of("75"))
      .overpaymentFee(UnsignedInteger.valueOf(500))
      .interestRate(UnsignedInteger.valueOf(5000))
      .lateInterestRate(UnsignedInteger.valueOf(7500))
      .closeInterestRate(UnsignedInteger.valueOf(3000))
      .overpaymentInterestRate(UnsignedInteger.valueOf(2000))
      .principalRequested(AssetAmount.of("50000"))
      .paymentTotal(UnsignedInteger.valueOf(3))
      .paymentInterval(UnsignedInteger.valueOf(2592000))
      .gracePeriod(UnsignedInteger.valueOf(86400))
      .build();

    String json = "{" +
      "\"Account\":\"rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8\"," +
      "\"Fee\":\"45\"," +
      "\"Sequence\":190," +
      "\"SigningPubKey\":" +
      "\"ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD\"," +
      "\"LoanBrokerID\":" +
      "\"79E25403E9FC010A277D80410EED5494FDD033A09FD4C1432335A1734A1D099D\"," +
      "\"Counterparty\":\"rEePKs9pVMf91vYj1QVRPmJvCBEum9P2kE\"," +
      "\"CounterpartySignature\":{" +
      "\"SigningPubKey\":" +
      "\"ED94BE884DB13EA26792F7DE6E8DAAEE9151BD90E0362C7D5C1898D272A7E56A00\"," +
      "\"TxnSignature\":" +
      "\"E0B7599656AF32847D4695F836FF1767F036F6E1232115E9295264C8A614FB3C" +
      "1BA3DF45D92304038032EC7BD5D6A901B041A4C74CF28838D205B7E51E08B602\"" +
      "}," +
      "\"Data\":\"AABBCC\"," +
      "\"LoanOriginationFee\":\"100\"," +
      "\"LoanServiceFee\":\"50\"," +
      "\"LatePaymentFee\":\"25\"," +
      "\"ClosePaymentFee\":\"75\"," +
      "\"OverpaymentFee\":500," +
      "\"InterestRate\":5000," +
      "\"LateInterestRate\":7500," +
      "\"CloseInterestRate\":3000," +
      "\"OverpaymentInterestRate\":2000," +
      "\"PrincipalRequested\":\"50000\"," +
      "\"PaymentTotal\":3," +
      "\"PaymentInterval\":2592000," +
      "\"GracePeriod\":86400," +
      "\"TransactionType\":\"LoanSet\"" +
      "}";

    assertCanSerializeAndDeserialize(loanSet, json);
  }

  @Test
  public void testLoanSetJsonWithRequiredFieldsOnly()
    throws JsonProcessingException, JSONException {

    LoanSet loanSet = LoanSet.builder()
      .account(Address.of("rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8"))
      .fee(XrpCurrencyAmount.ofDrops(45))
      .sequence(UnsignedInteger.valueOf(190))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey(
          "ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD"
        )
      )
      .loanBrokerId(Hash256.of(
        "79E25403E9FC010A277D80410EED5494FDD033A09FD4C1432335A1734A1D099D"
      ))
      .principalRequested(AssetAmount.of("50000"))
      .build();

    String json = "{" +
      "\"Account\":\"rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8\"," +
      "\"Fee\":\"45\"," +
      "\"Sequence\":190," +
      "\"SigningPubKey\":" +
      "\"ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD\"," +
      "\"LoanBrokerID\":" +
      "\"79E25403E9FC010A277D80410EED5494FDD033A09FD4C1432335A1734A1D099D\"," +
      "\"PrincipalRequested\":\"50000\"," +
      "\"TransactionType\":\"LoanSet\"" +
      "}";

    assertCanSerializeAndDeserialize(loanSet, json);
  }

  @Test
  public void testLoanSetJsonWithCounterpartySignature()
    throws JsonProcessingException, JSONException {

    LoanSet loanSet = LoanSet.builder()
      .account(Address.of("rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8"))
      .fee(XrpCurrencyAmount.ofDrops(45))
      .sequence(UnsignedInteger.valueOf(190))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey(
          "ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD"
        )
      )
      .loanBrokerId(Hash256.of(
        "79E25403E9FC010A277D80410EED5494FDD033A09FD4C1432335A1734A1D099D"
      ))
      .counterparty(Address.of("rEePKs9pVMf91vYj1QVRPmJvCBEum9P2kE"))
      .counterpartySignature(
        CounterpartySignature.builder()
          .signingPubKey(
            "ED94BE884DB13EA26792F7DE6E8DAAEE9151BD90E0362C7D5C1898D272A7E56A00"
          )
          .txnSignature(
            "E0B7599656AF32847D4695F836FF1767F036F6E1232115E9295264C8A614FB3C" +
            "1BA3DF45D92304038032EC7BD5D6A901B041A4C74CF28838D205B7E51E08B602"
          )
          .build()
      )
      .principalRequested(AssetAmount.of("50000"))
      .paymentTotal(UnsignedInteger.valueOf(3))
      .build();

    String json = "{" +
      "\"Account\":\"rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8\"," +
      "\"Fee\":\"45\"," +
      "\"Sequence\":190," +
      "\"SigningPubKey\":" +
      "\"ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD\"," +
      "\"LoanBrokerID\":" +
      "\"79E25403E9FC010A277D80410EED5494FDD033A09FD4C1432335A1734A1D099D\"," +
      "\"Counterparty\":\"rEePKs9pVMf91vYj1QVRPmJvCBEum9P2kE\"," +
      "\"CounterpartySignature\":{" +
      "\"SigningPubKey\":" +
      "\"ED94BE884DB13EA26792F7DE6E8DAAEE9151BD90E0362C7D5C1898D272A7E56A00\"," +
      "\"TxnSignature\":" +
      "\"E0B7599656AF32847D4695F836FF1767F036F6E1232115E9295264C8A614FB3C" +
      "1BA3DF45D92304038032EC7BD5D6A901B041A4C74CF28838D205B7E51E08B602\"" +
      "}," +
      "\"PrincipalRequested\":\"50000\"," +
      "\"PaymentTotal\":3," +
      "\"TransactionType\":\"LoanSet\"" +
      "}";

    assertCanSerializeAndDeserialize(loanSet, json);
  }

  @Test
  public void testLoanSetJsonWithFlags()
    throws JsonProcessingException, JSONException {

    LoanSet loanSet = LoanSet.builder()
      .account(Address.of("rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8"))
      .fee(XrpCurrencyAmount.ofDrops(45))
      .sequence(UnsignedInteger.valueOf(190))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey(
          "ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD"
        )
      )
      .flags(LoanSetFlags.of(
        LoanSetFlags.LOAN_OVERPAYMENT.getValue()
      ))
      .loanBrokerId(Hash256.of(
        "79E25403E9FC010A277D80410EED5494FDD033A09FD4C1432335A1734A1D099D"
      ))
      .principalRequested(AssetAmount.of("50000"))
      .build();

    String json = "{" +
      "\"Account\":\"rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8\"," +
      "\"Fee\":\"45\"," +
      "\"Sequence\":190," +
      "\"Flags\":65536," +
      "\"SigningPubKey\":" +
      "\"ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD\"," +
      "\"LoanBrokerID\":" +
      "\"79E25403E9FC010A277D80410EED5494FDD033A09FD4C1432335A1734A1D099D\"," +
      "\"PrincipalRequested\":\"50000\"," +
      "\"TransactionType\":\"LoanSet\"" +
      "}";

    assertCanSerializeAndDeserialize(loanSet, json);
  }
}
