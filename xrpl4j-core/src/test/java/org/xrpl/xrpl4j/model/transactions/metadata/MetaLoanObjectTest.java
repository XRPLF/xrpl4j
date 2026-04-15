package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.LoanFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.AssetAmount;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.LoanData;

class MetaLoanObjectTest extends AbstractJsonTest {

  @Test
  void testMetaLoanObjectWithAllFields() throws JsonProcessingException, JSONException {
    MetaLoanObject metaLoanObject = ImmutableMetaLoanObject.builder()
      .flags(LoanFlags.UNSET)
      .previousTransactionId(Hash256.of("7E5F3FB60E1177F8AF8A9EAC7982F27FA5494FDEA871B23B4B149939A5A7A7BB"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(82357607))
      .loanSequence(UnsignedInteger.valueOf(1))
      .ownerNode("0000000000000000")
      .loanBrokerNode("0000000000000000")
      .loanBrokerId(Hash256.of("C031EFE677CDEF1C5F43475B374A16F990EE184F76015CB7548D34B500F72BFB"))
      .borrower(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .loanOriginationFee(AssetAmount.of("100"))
      .loanServiceFee(AssetAmount.of("10"))
      .latePaymentFee(AssetAmount.of("50"))
      .closePaymentFee(AssetAmount.of("25"))
      .overpaymentFee(UnsignedInteger.valueOf(5000))
      .interestRate(UnsignedInteger.valueOf(10000))
      .lateInterestRate(UnsignedInteger.valueOf(5000))
      .closeInterestRate(UnsignedInteger.valueOf(2000))
      .overpaymentInterestRate(UnsignedInteger.valueOf(1000))
      .startDate(UnsignedInteger.valueOf(784111330))
      .paymentInterval(UnsignedInteger.valueOf(2592000))
      .gracePeriod(UnsignedInteger.valueOf(86400))
      .previousPaymentDueDate(UnsignedInteger.valueOf(784111330))
      .nextPaymentDueDate(UnsignedInteger.valueOf(786703330))
      .paymentRemaining(UnsignedInteger.valueOf(11))
      .totalValueOutstanding(AssetAmount.of("1100000"))
      .principalOutstanding(AssetAmount.of("1000000"))
      .managementFeeOutstanding(AssetAmount.of("10000"))
      .periodicPayment(AssetAmount.of("100000"))
      .loanScale(6)
      .data(LoanData.of("AABBCC"))
      .build();

    String json = "{" +
      "  \"Flags\": 0," +
      "  \"PreviousTxnID\": \"7E5F3FB60E1177F8AF8A9EAC7982F27FA5494FDEA871B23B4B149939A5A7A7BB\"," +
      "  \"PreviousTxnLgrSeq\": 82357607," +
      "  \"LoanSequence\": 1," +
      "  \"OwnerNode\": \"0000000000000000\"," +
      "  \"LoanBrokerNode\": \"0000000000000000\"," +
      "  \"LoanBrokerID\": \"C031EFE677CDEF1C5F43475B374A16F990EE184F76015CB7548D34B500F72BFB\"," +
      "  \"Borrower\": \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\"," +
      "  \"LoanOriginationFee\": \"100\"," +
      "  \"LoanServiceFee\": \"10\"," +
      "  \"LatePaymentFee\": \"50\"," +
      "  \"ClosePaymentFee\": \"25\"," +
      "  \"OverpaymentFee\": 5000," +
      "  \"InterestRate\": 10000," +
      "  \"LateInterestRate\": 5000," +
      "  \"CloseInterestRate\": 2000," +
      "  \"OverpaymentInterestRate\": 1000," +
      "  \"StartDate\": 784111330," +
      "  \"PaymentInterval\": 2592000," +
      "  \"GracePeriod\": 86400," +
      "  \"PreviousPaymentDueDate\": 784111330," +
      "  \"NextPaymentDueDate\": 786703330," +
      "  \"PaymentRemaining\": 11," +
      "  \"TotalValueOutstanding\": \"1100000\"," +
      "  \"PrincipalOutstanding\": \"1000000\"," +
      "  \"ManagementFeeOutstanding\": \"10000\"," +
      "  \"PeriodicPayment\": \"100000\"," +
      "  \"LoanScale\": 6," +
      "  \"Data\": \"AABBCC\"" +
      "}";

    assertCanSerializeAndDeserialize(metaLoanObject, json, MetaLoanObject.class);
  }

  @Test
  void testMetaLoanObjectWithImpairedFlag() throws JsonProcessingException, JSONException {
    MetaLoanObject metaLoanObject = ImmutableMetaLoanObject.builder()
      .flags(LoanFlags.LOAN_IMPAIRED)
      .loanBrokerId(Hash256.of("C031EFE677CDEF1C5F43475B374A16F990EE184F76015CB7548D34B500F72BFB"))
      .borrower(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .principalOutstanding(AssetAmount.of("500000"))
      .build();

    String json = "{" +
      "  \"Flags\": 131072," +
      "  \"LoanBrokerID\": \"C031EFE677CDEF1C5F43475B374A16F990EE184F76015CB7548D34B500F72BFB\"," +
      "  \"Borrower\": \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\"," +
      "  \"PrincipalOutstanding\": \"500000\"" +
      "}";

    assertCanSerializeAndDeserialize(metaLoanObject, json, MetaLoanObject.class);
  }

  @Test
  void testMetaLoanObjectWithMinimalFields() throws JsonProcessingException, JSONException {
    MetaLoanObject metaLoanObject = ImmutableMetaLoanObject.builder()
      .totalValueOutstanding(AssetAmount.of("900000"))
      .principalOutstanding(AssetAmount.of("800000"))
      .build();

    String json = "{" +
      "  \"TotalValueOutstanding\": \"900000\"," +
      "  \"PrincipalOutstanding\": \"800000\"" +
      "}";

    assertCanSerializeAndDeserialize(metaLoanObject, json, MetaLoanObject.class);
  }
}
