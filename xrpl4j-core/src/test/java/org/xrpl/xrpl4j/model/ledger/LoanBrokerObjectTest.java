package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Amount;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.LoanBrokerData;

class LoanBrokerObjectTest extends AbstractJsonTest {

  @Test
  void testJsonWithAllFields() throws JSONException, JsonProcessingException {
    LoanBrokerObject loanBroker = LoanBrokerObject.builder()
      .previousTransactionId(
        Hash256.of("37C246E2A4957593C8E34DDA791B0380F34DFC1841FB782C9037BC9F1CA1D78B")
      )
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(487))
      .sequence(UnsignedInteger.valueOf(188))
      .loanSequence(UnsignedInteger.valueOf(1))
      .ownerNode("0")
      .vaultNode("0")
      .vaultId(
        Hash256.of("F07A0E3933CB0E1402E52F312CBBB3CE301932F5E8B8340344B79A48574529F0")
      )
      .account(Address.of("rEBcryX1YQt9XLQqARhmo55WLTfBvkLzqB"))
      .owner(Address.of("rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8"))
      .data(LoanBrokerData.of("010203"))
      .ownerCount(UnsignedInteger.valueOf(2))
      .managementFeeRate(UnsignedInteger.valueOf(10000))
      .debtTotal(Amount.of("500000"))
      .debtMaximum(Amount.of("250000"))
      .coverAvailable(Amount.of("100000"))
      .coverRateMinimum(UnsignedInteger.valueOf(50000))
      .coverRateLiquidation(UnsignedInteger.valueOf(25000))
      .index(
        Hash256.of("79E25403E9FC010A277D80410EED5494FDD033A09FD4C1432335A1734A1D099D")
      )
      .build();

    String json = "{\n" +
      "    \"LedgerEntryType\" : \"LoanBroker\",\n" +
      "    \"Flags\" : 0,\n" +
      "    \"PreviousTxnID\" : " +
      "\"37C246E2A4957593C8E34DDA791B0380F34DFC1841FB782C9037BC9F1CA1D78B\",\n" +
      "    \"PreviousTxnLgrSeq\" : 487,\n" +
      "    \"Sequence\" : 188,\n" +
      "    \"LoanSequence\" : 1,\n" +
      "    \"OwnerNode\" : \"0\",\n" +
      "    \"VaultNode\" : \"0\",\n" +
      "    \"VaultID\" : " +
      "\"F07A0E3933CB0E1402E52F312CBBB3CE301932F5E8B8340344B79A48574529F0\",\n" +
      "    \"Account\" : \"rEBcryX1YQt9XLQqARhmo55WLTfBvkLzqB\",\n" +
      "    \"Owner\" : \"rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8\",\n" +
      "    \"Data\" : \"010203\",\n" +
      "    \"OwnerCount\" : 2,\n" +
      "    \"ManagementFeeRate\" : 10000,\n" +
      "    \"DebtTotal\" : \"500000\",\n" +
      "    \"DebtMaximum\" : \"250000\",\n" +
      "    \"CoverAvailable\" : \"100000\",\n" +
      "    \"CoverRateMinimum\" : 50000,\n" +
      "    \"CoverRateLiquidation\" : 25000,\n" +
      "    \"index\" : " +
      "\"79E25403E9FC010A277D80410EED5494FDD033A09FD4C1432335A1734A1D099D\"\n" +
      "}";

    assertCanSerializeAndDeserialize(loanBroker, json);
  }

  @Test
  void testJsonWithRequiredFieldsOnly()
    throws JSONException, JsonProcessingException {
    LoanBrokerObject loanBroker = LoanBrokerObject.builder()
      .previousTransactionId(
        Hash256.of("37C246E2A4957593C8E34DDA791B0380F34DFC1841FB782C9037BC9F1CA1D78B")
      )
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(487))
      .sequence(UnsignedInteger.valueOf(188))
      .loanSequence(UnsignedInteger.valueOf(1))
      .ownerNode("0")
      .vaultNode("0")
      .vaultId(
        Hash256.of("F07A0E3933CB0E1402E52F312CBBB3CE301932F5E8B8340344B79A48574529F0")
      )
      .account(Address.of("rEBcryX1YQt9XLQqARhmo55WLTfBvkLzqB"))
      .owner(Address.of("rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8"))
      .index(
        Hash256.of("79E25403E9FC010A277D80410EED5494FDD033A09FD4C1432335A1734A1D099D")
      )
      .build();

    String json = "{\n" +
      "    \"LedgerEntryType\" : \"LoanBroker\",\n" +
      "    \"Flags\" : 0,\n" +
      "    \"PreviousTxnID\" : " +
      "\"37C246E2A4957593C8E34DDA791B0380F34DFC1841FB782C9037BC9F1CA1D78B\",\n" +
      "    \"PreviousTxnLgrSeq\" : 487,\n" +
      "    \"Sequence\" : 188,\n" +
      "    \"LoanSequence\" : 1,\n" +
      "    \"OwnerNode\" : \"0\",\n" +
      "    \"VaultNode\" : \"0\",\n" +
      "    \"VaultID\" : " +
      "\"F07A0E3933CB0E1402E52F312CBBB3CE301932F5E8B8340344B79A48574529F0\",\n" +
      "    \"Account\" : \"rEBcryX1YQt9XLQqARhmo55WLTfBvkLzqB\",\n" +
      "    \"Owner\" : \"rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8\",\n" +
      "    \"OwnerCount\" : 0,\n" +
      "    \"ManagementFeeRate\" : 0,\n" +
      "    \"DebtTotal\" : \"0\",\n" +
      "    \"CoverAvailable\" : \"0\",\n" +
      "    \"CoverRateMinimum\" : 0,\n" +
      "    \"CoverRateLiquidation\" : 0,\n" +
      "    \"index\" : " +
      "\"79E25403E9FC010A277D80410EED5494FDD033A09FD4C1432335A1734A1D099D\"\n" +
      "}";

    assertCanSerializeAndDeserialize(loanBroker, json);
  }
}
