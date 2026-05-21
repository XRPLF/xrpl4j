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
import org.xrpl.xrpl4j.model.transactions.LoanBrokerDelete;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * Unit tests for {@link LoanBrokerDelete} JSON serialization.
 */
public class LoanBrokerDeleteJsonTest extends AbstractJsonTest {

  @Test
  public void testLoanBrokerDeleteJson() throws JsonProcessingException, JSONException {
    LoanBrokerDelete loanBrokerDelete = LoanBrokerDelete.builder()
      .account(Address.of("rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8"))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .sequence(UnsignedInteger.valueOf(189))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey(
          "ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD"
        )
      )
      .loanBrokerId(Hash256.of("79E25403E9FC010A277D80410EED5494FDD033A09FD4C1432335A1734A1D099D"))
      .build();

    String json = "{" +
      "  \"Account\": \"rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8\"," +
      "  \"TransactionType\": \"LoanBrokerDelete\"," +
      "  \"Fee\": \"15\"," +
      "  \"Sequence\": 189," +
      "  \"SigningPubKey\": \"ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD\"," +
      "  \"LoanBrokerID\": \"79E25403E9FC010A277D80410EED5494FDD033A09FD4C1432335A1734A1D099D\"" +
      "}";

    assertCanSerializeAndDeserialize(loanBrokerDelete, json);
  }

  @Test
  public void testLoanBrokerDeleteJsonWithUnsetFlags() throws JsonProcessingException, JSONException {
    LoanBrokerDelete loanBrokerDelete = LoanBrokerDelete.builder()
      .account(Address.of("rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8"))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .sequence(UnsignedInteger.valueOf(189))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey(
          "ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD"
        )
      )
      .loanBrokerId(Hash256.of("79E25403E9FC010A277D80410EED5494FDD033A09FD4C1432335A1734A1D099D"))
      .flags(TransactionFlags.UNSET)
      .build();

    String json = "{" +
      "  \"Account\": \"rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8\"," +
      "  \"TransactionType\": \"LoanBrokerDelete\"," +
      "  \"Fee\": \"15\"," +
      "  \"Sequence\": 189," +
      "  \"Flags\": 0," +
      "  \"SigningPubKey\": \"ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD\"," +
      "  \"LoanBrokerID\": \"79E25403E9FC010A277D80410EED5494FDD033A09FD4C1432335A1734A1D099D\"" +
      "}";

    assertCanSerializeAndDeserialize(loanBrokerDelete, json);
  }

  @Test
  public void testLoanBrokerDeleteJsonWithNonZeroFlags() throws JsonProcessingException, JSONException {
    LoanBrokerDelete loanBrokerDelete = LoanBrokerDelete.builder()
      .account(Address.of("rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8"))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .sequence(UnsignedInteger.valueOf(189))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey(
          "ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD"
        )
      )
      .loanBrokerId(Hash256.of("79E25403E9FC010A277D80410EED5494FDD033A09FD4C1432335A1734A1D099D"))
      .flags(TransactionFlags.FULLY_CANONICAL_SIG)
      .build();

    String json = String.format("{" +
      "  \"Account\": \"rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8\"," +
      "  \"TransactionType\": \"LoanBrokerDelete\"," +
      "  \"Fee\": \"15\"," +
      "  \"Sequence\": 189," +
      "  \"Flags\": %s," +
      "  \"SigningPubKey\": \"ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD\"," +
      "  \"LoanBrokerID\": \"79E25403E9FC010A277D80410EED5494FDD033A09FD4C1432335A1734A1D099D\"" +
      "}", TransactionFlags.FULLY_CANONICAL_SIG.getValue());

    assertCanSerializeAndDeserialize(loanBrokerDelete, json);
  }
}
