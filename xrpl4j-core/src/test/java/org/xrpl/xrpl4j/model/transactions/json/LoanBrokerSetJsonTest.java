package org.xrpl.xrpl4j.model.transactions.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.AssetAmount;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.LoanBrokerData;
import org.xrpl.xrpl4j.model.transactions.LoanBrokerSet;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * Unit tests for {@link LoanBrokerSet} JSON serialization.
 */
public class LoanBrokerSetJsonTest extends AbstractJsonTest {

  @Test
  public void testLoanBrokerSetJson() throws JsonProcessingException, JSONException {
    LoanBrokerSet loanBrokerSet = LoanBrokerSet.builder()
      .account(Address.of("rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8"))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .sequence(UnsignedInteger.valueOf(188))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey(
          "ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD"
        )
      )
      .vaultId(Hash256.of("F07A0E3933CB0E1402E52F312CBBB3CE301932F5E8B8340344B79A48574529F0"))
      .loanBrokerId(Hash256.of("A07A0E3933CB0E1402E52F312CBBB3CE301932F5E8B8340344B79A48574529F0"))
      .data(LoanBrokerData.of("010203"))
      .managementFeeRate(UnsignedInteger.valueOf(10000))
      .debtMaximum(AssetAmount.of("250000"))
      .coverRateMinimum(UnsignedInteger.valueOf(15000))
      .coverRateLiquidation(UnsignedInteger.valueOf(12000))
      .build();

    String json = "{" +
      "  \"Account\": \"rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8\"," +
      "  \"TransactionType\": \"LoanBrokerSet\"," +
      "  \"Fee\": \"15\"," +
      "  \"Sequence\": 188," +
      "  \"SigningPubKey\": \"ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD\"," +
      "  \"VaultID\": \"F07A0E3933CB0E1402E52F312CBBB3CE301932F5E8B8340344B79A48574529F0\"," +
      "  \"LoanBrokerID\": \"A07A0E3933CB0E1402E52F312CBBB3CE301932F5E8B8340344B79A48574529F0\"," +
      "  \"Data\": \"010203\"," +
      "  \"ManagementFeeRate\": 10000," +
      "  \"DebtMaximum\": \"250000\"," +
      "  \"CoverRateMinimum\": 15000," +
      "  \"CoverRateLiquidation\": 12000" +
      "}";

    assertCanSerializeAndDeserialize(loanBrokerSet, json);
  }

  @Test
  public void testLoanBrokerSetJsonWithUnsetFlags() throws JsonProcessingException, JSONException {
    LoanBrokerSet loanBrokerSet = LoanBrokerSet.builder()
      .account(Address.of("rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8"))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .sequence(UnsignedInteger.valueOf(188))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey(
          "ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD"
        )
      )
      .vaultId(Hash256.of("F07A0E3933CB0E1402E52F312CBBB3CE301932F5E8B8340344B79A48574529F0"))
      .flags(TransactionFlags.UNSET)
      .build();

    String json = "{" +
      "  \"Account\": \"rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8\"," +
      "  \"TransactionType\": \"LoanBrokerSet\"," +
      "  \"Fee\": \"15\"," +
      "  \"Sequence\": 188," +
      "  \"Flags\": 0," +
      "  \"SigningPubKey\": \"ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD\"," +
      "  \"VaultID\": \"F07A0E3933CB0E1402E52F312CBBB3CE301932F5E8B8340344B79A48574529F0\"" +
      "}";

    assertCanSerializeAndDeserialize(loanBrokerSet, json);
  }

  @Test
  public void testLoanBrokerSetJsonWithNonZeroFlags() throws JsonProcessingException, JSONException {
    LoanBrokerSet loanBrokerSet = LoanBrokerSet.builder()
      .account(Address.of("rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8"))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .sequence(UnsignedInteger.valueOf(188))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey(
          "ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD"
        )
      )
      .vaultId(Hash256.of("F07A0E3933CB0E1402E52F312CBBB3CE301932F5E8B8340344B79A48574529F0"))
      .flags(TransactionFlags.FULLY_CANONICAL_SIG)
      .build();

    String json = String.format("{" +
      "  \"Account\": \"rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8\"," +
      "  \"TransactionType\": \"LoanBrokerSet\"," +
      "  \"Fee\": \"15\"," +
      "  \"Sequence\": 188," +
      "  \"Flags\": %s," +
      "  \"SigningPubKey\": \"ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD\"," +
      "  \"VaultID\": \"F07A0E3933CB0E1402E52F312CBBB3CE301932F5E8B8340344B79A48574529F0\"" +
      "}", TransactionFlags.FULLY_CANONICAL_SIG.getValue());

    assertCanSerializeAndDeserialize(loanBrokerSet, json);
  }
}
