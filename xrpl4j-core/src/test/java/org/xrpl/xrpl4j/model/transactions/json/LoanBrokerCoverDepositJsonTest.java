package org.xrpl.xrpl4j.model.transactions.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.LoanBrokerCoverDeposit;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * Unit tests for {@link LoanBrokerCoverDeposit} JSON serialization.
 */
public class LoanBrokerCoverDepositJsonTest extends AbstractJsonTest {

  @Test
  public void testLoanBrokerCoverDepositJson() throws JsonProcessingException, JSONException {
    LoanBrokerCoverDeposit deposit = LoanBrokerCoverDeposit.builder()
      .account(Address.of("rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8"))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .sequence(UnsignedInteger.valueOf(191))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey(
          "ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD"
        )
      )
      .loanBrokerId(Hash256.of("79E25403E9FC010A277D80410EED5494FDD033A09FD4C1432335A1734A1D099D"))
      .amount(
        IssuedCurrencyAmount.builder()
          .currency("USD")
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .value("50000")
          .build()
      )
      .build();

    String json = "{" +
      "  \"Account\": \"rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8\"," +
      "  \"TransactionType\": \"LoanBrokerCoverDeposit\"," +
      "  \"Fee\": \"15\"," +
      "  \"Sequence\": 191," +
      "  \"SigningPubKey\": \"ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD\"," +
      "  \"LoanBrokerID\": \"79E25403E9FC010A277D80410EED5494FDD033A09FD4C1432335A1734A1D099D\"," +
      "  \"Amount\": {" +
      "    \"currency\": \"USD\"," +
      "    \"issuer\": \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\"," +
      "    \"value\": \"50000\"" +
      "  }" +
      "}";

    assertCanSerializeAndDeserialize(deposit, json);
  }

  @Test
  public void testLoanBrokerCoverDepositJsonWithXrp() throws JsonProcessingException, JSONException {
    LoanBrokerCoverDeposit deposit = LoanBrokerCoverDeposit.builder()
      .account(Address.of("rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8"))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .sequence(UnsignedInteger.valueOf(191))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey(
          "ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD"
        )
      )
      .loanBrokerId(Hash256.of("79E25403E9FC010A277D80410EED5494FDD033A09FD4C1432335A1734A1D099D"))
      .amount(XrpCurrencyAmount.ofDrops(50000))
      .build();

    String json = "{" +
      "  \"Account\": \"rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8\"," +
      "  \"TransactionType\": \"LoanBrokerCoverDeposit\"," +
      "  \"Fee\": \"15\"," +
      "  \"Sequence\": 191," +
      "  \"SigningPubKey\": \"ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD\"," +
      "  \"LoanBrokerID\": \"79E25403E9FC010A277D80410EED5494FDD033A09FD4C1432335A1734A1D099D\"," +
      "  \"Amount\": \"50000\"" +
      "}";

    assertCanSerializeAndDeserialize(deposit, json);
  }

  @Test
  public void testLoanBrokerCoverDepositJsonWithUnsetFlags() throws JsonProcessingException, JSONException {
    LoanBrokerCoverDeposit deposit = LoanBrokerCoverDeposit.builder()
      .account(Address.of("rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8"))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .sequence(UnsignedInteger.valueOf(191))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey(
          "ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD"
        )
      )
      .loanBrokerId(Hash256.of("79E25403E9FC010A277D80410EED5494FDD033A09FD4C1432335A1734A1D099D"))
      .amount(XrpCurrencyAmount.ofDrops(50000))
      .flags(TransactionFlags.UNSET)
      .build();

    String json = "{" +
      "  \"Account\": \"rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8\"," +
      "  \"TransactionType\": \"LoanBrokerCoverDeposit\"," +
      "  \"Fee\": \"15\"," +
      "  \"Sequence\": 191," +
      "  \"Flags\": 0," +
      "  \"SigningPubKey\": \"ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD\"," +
      "  \"LoanBrokerID\": \"79E25403E9FC010A277D80410EED5494FDD033A09FD4C1432335A1734A1D099D\"," +
      "  \"Amount\": \"50000\"" +
      "}";

    assertCanSerializeAndDeserialize(deposit, json);
  }

  @Test
  public void testLoanBrokerCoverDepositJsonWithNonZeroFlags() throws JsonProcessingException, JSONException {
    LoanBrokerCoverDeposit deposit = LoanBrokerCoverDeposit.builder()
      .account(Address.of("rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8"))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .sequence(UnsignedInteger.valueOf(191))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey(
          "ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD"
        )
      )
      .loanBrokerId(Hash256.of("79E25403E9FC010A277D80410EED5494FDD033A09FD4C1432335A1734A1D099D"))
      .amount(XrpCurrencyAmount.ofDrops(50000))
      .flags(TransactionFlags.FULLY_CANONICAL_SIG)
      .build();

    String json = String.format("{" +
      "  \"Account\": \"rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8\"," +
      "  \"TransactionType\": \"LoanBrokerCoverDeposit\"," +
      "  \"Fee\": \"15\"," +
      "  \"Sequence\": 191," +
      "  \"Flags\": %s," +
      "  \"SigningPubKey\": \"ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD\"," +
      "  \"LoanBrokerID\": \"79E25403E9FC010A277D80410EED5494FDD033A09FD4C1432335A1734A1D099D\"," +
      "  \"Amount\": \"50000\"" +
      "}", TransactionFlags.FULLY_CANONICAL_SIG.getValue());

    assertCanSerializeAndDeserialize(deposit, json);
  }
}
