package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.LoanFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Amount;
import org.xrpl.xrpl4j.model.transactions.Hash256;

class LoanObjectTest extends AbstractJsonTest {

  @Test
  void testJsonWithAllFields()
    throws JSONException, JsonProcessingException {
    LoanObject loan = LoanObject.builder()
      .previousTransactionId(
        Hash256.of(
          "30A4FCA255A4F5F420005A4CC4EF3DFCCAEE748AADFA6497226D55604A97F4EC"
        )
      )
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(553))
      .loanSequence(UnsignedInteger.valueOf(1))
      .ownerNode("0")
      .loanBrokerNode("0")
      .loanBrokerId(
        Hash256.of(
          "79E25403E9FC010A277D80410EED5494FDD033A09FD4C1432335A1734A1D099D"
        )
      )
      .borrower(
        Address.of("rEePKs9pVMf91vYj1QVRPmJvCBEum9P2kE")
      )
      .startDate(UnsignedInteger.valueOf(827248750))
      .paymentInterval(UnsignedInteger.valueOf(60))
      .gracePeriod(UnsignedInteger.valueOf(60))
      .nextPaymentDueDate(UnsignedInteger.valueOf(827248810))
      .paymentRemaining(UnsignedInteger.valueOf(3))
      .totalValueOutstanding(Amount.of("50000"))
      .principalOutstanding(Amount.of("50000"))
      .periodicPayment(Amount.of("16666.66666666666667"))
      .loanScale(-10)
      .index(
        Hash256.of(
          "569EE4B52FF7F903C835E8470400900DC493C97B3BE1D41C7EA4FF3407953D73"
        )
      )
      .build();

    String json = "{\n" +
      "    \"LedgerEntryType\" : \"Loan\",\n" +
      "    \"Flags\" : 0,\n" +
      "    \"PreviousTxnID\" : " +
      "\"30A4FCA255A4F5F420005A4CC4EF3DFCCAEE748AADFA6497226D55604A97F4EC\",\n" +
      "    \"PreviousTxnLgrSeq\" : 553,\n" +
      "    \"LoanSequence\" : 1,\n" +
      "    \"OwnerNode\" : \"0\",\n" +
      "    \"LoanBrokerNode\" : \"0\",\n" +
      "    \"LoanBrokerID\" : " +
      "\"79E25403E9FC010A277D80410EED5494FDD033A09FD4C1432335A1734A1D099D\",\n" +
      "    \"Borrower\" : \"rEePKs9pVMf91vYj1QVRPmJvCBEum9P2kE\",\n" +
      "    \"StartDate\" : 827248750,\n" +
      "    \"PaymentInterval\" : 60,\n" +
      "    \"GracePeriod\" : 60,\n" +
      "    \"NextPaymentDueDate\" : 827248810,\n" +
      "    \"PaymentRemaining\" : 3,\n" +
      "    \"TotalValueOutstanding\" : \"50000\",\n" +
      "    \"PrincipalOutstanding\" : \"50000\",\n" +
      "    \"PeriodicPayment\" : \"16666.66666666666667\",\n" +
      "    \"LoanScale\" : -10,\n" +
      "    \"index\" : " +
      "\"569EE4B52FF7F903C835E8470400900DC493C97B3BE1D41C7EA4FF3407953D73\"\n" +
      "}";

    assertCanSerializeAndDeserialize(loan, json);
  }

  @Test
  void testJsonWithImpairedFlags()
    throws JSONException, JsonProcessingException {
    LoanObject loan = LoanObject.builder()
      .flags(LoanFlags.of(131072))
      .previousTransactionId(
        Hash256.of(
          "C1101E9E41243D01AA78B1FA8B71AF869D17247B44225DB12EDECAB062D16DCB"
        )
      )
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(619))
      .loanSequence(UnsignedInteger.valueOf(1))
      .ownerNode("0")
      .loanBrokerNode("0")
      .loanBrokerId(
        Hash256.of(
          "79E25403E9FC010A277D80410EED5494FDD033A09FD4C1432335A1734A1D099D"
        )
      )
      .borrower(
        Address.of("rEePKs9pVMf91vYj1QVRPmJvCBEum9P2kE")
      )
      .startDate(UnsignedInteger.valueOf(827248750))
      .paymentInterval(UnsignedInteger.valueOf(60))
      .gracePeriod(UnsignedInteger.valueOf(60))
      .previousPaymentDueDate(UnsignedInteger.valueOf(827248810))
      .nextPaymentDueDate(UnsignedInteger.valueOf(827248806))
      .paymentRemaining(UnsignedInteger.valueOf(2))
      .totalValueOutstanding(Amount.of("33333.3333333333"))
      .principalOutstanding(Amount.of("33333.3333333333"))
      .periodicPayment(Amount.of("16666.66666666666667"))
      .loanScale(-10)
      .index(
        Hash256.of(
          "569EE4B52FF7F903C835E8470400900DC493C97B3BE1D41C7EA4FF3407953D73"
        )
      )
      .build();

    String json = "{\n" +
      "    \"LedgerEntryType\" : \"Loan\",\n" +
      "    \"Flags\" : 131072,\n" +
      "    \"PreviousTxnID\" : " +
      "\"C1101E9E41243D01AA78B1FA8B71AF869D17247B44225DB12EDECAB062D16DCB\",\n" +
      "    \"PreviousTxnLgrSeq\" : 619,\n" +
      "    \"LoanSequence\" : 1,\n" +
      "    \"OwnerNode\" : \"0\",\n" +
      "    \"LoanBrokerNode\" : \"0\",\n" +
      "    \"LoanBrokerID\" : " +
      "\"79E25403E9FC010A277D80410EED5494FDD033A09FD4C1432335A1734A1D099D\",\n" +
      "    \"Borrower\" : \"rEePKs9pVMf91vYj1QVRPmJvCBEum9P2kE\",\n" +
      "    \"StartDate\" : 827248750,\n" +
      "    \"PaymentInterval\" : 60,\n" +
      "    \"GracePeriod\" : 60,\n" +
      "    \"PreviousPaymentDueDate\" : 827248810,\n" +
      "    \"NextPaymentDueDate\" : 827248806,\n" +
      "    \"PaymentRemaining\" : 2,\n" +
      "    \"TotalValueOutstanding\" : \"33333.3333333333\",\n" +
      "    \"PrincipalOutstanding\" : \"33333.3333333333\",\n" +
      "    \"PeriodicPayment\" : \"16666.66666666666667\",\n" +
      "    \"LoanScale\" : -10,\n" +
      "    \"index\" : " +
      "\"569EE4B52FF7F903C835E8470400900DC493C97B3BE1D41C7EA4FF3407953D73\"\n" +
      "}";

    assertCanSerializeAndDeserialize(loan, json);
  }

  @Test
  void testJsonWithMinimalFields()
    throws JSONException, JsonProcessingException {
    LoanObject loan = LoanObject.builder()
      .previousTransactionId(
        Hash256.of(
          "30A4FCA255A4F5F420005A4CC4EF3DFCCAEE748AADFA6497226D55604A97F4EC"
        )
      )
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(553))
      .loanSequence(UnsignedInteger.valueOf(1))
      .ownerNode("0")
      .loanBrokerNode("0")
      .loanBrokerId(
        Hash256.of(
          "79E25403E9FC010A277D80410EED5494FDD033A09FD4C1432335A1734A1D099D"
        )
      )
      .borrower(
        Address.of("rEePKs9pVMf91vYj1QVRPmJvCBEum9P2kE")
      )
      .startDate(UnsignedInteger.valueOf(827248750))
      .paymentInterval(UnsignedInteger.valueOf(60))
      .periodicPayment(Amount.of("16666.66666666666667"))
      .index(
        Hash256.of(
          "569EE4B52FF7F903C835E8470400900DC493C97B3BE1D41C7EA4FF3407953D73"
        )
      )
      .build();

    String json = "{\n" +
      "    \"LedgerEntryType\" : \"Loan\",\n" +
      "    \"Flags\" : 0,\n" +
      "    \"PreviousTxnID\" : " +
      "\"30A4FCA255A4F5F420005A4CC4EF3DFCCAEE748AADFA6497226D55604A97F4EC\",\n" +
      "    \"PreviousTxnLgrSeq\" : 553,\n" +
      "    \"LoanSequence\" : 1,\n" +
      "    \"OwnerNode\" : \"0\",\n" +
      "    \"LoanBrokerNode\" : \"0\",\n" +
      "    \"LoanBrokerID\" : " +
      "\"79E25403E9FC010A277D80410EED5494FDD033A09FD4C1432335A1734A1D099D\",\n" +
      "    \"Borrower\" : \"rEePKs9pVMf91vYj1QVRPmJvCBEum9P2kE\",\n" +
      "    \"StartDate\" : 827248750,\n" +
      "    \"PaymentInterval\" : 60,\n" +
      "    \"PeriodicPayment\" : \"16666.66666666666667\",\n" +
      "    \"index\" : " +
      "\"569EE4B52FF7F903C835E8470400900DC493C97B3BE1D41C7EA4FF3407953D73\"\n" +
      "}";

    assertCanSerializeAndDeserialize(loan, json);
  }
}
