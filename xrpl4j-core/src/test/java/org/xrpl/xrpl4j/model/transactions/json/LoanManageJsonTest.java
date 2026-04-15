package org.xrpl.xrpl4j.model.transactions.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.LoanManageFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.LoanManage;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * Unit tests for {@link LoanManage} JSON serialization.
 */
public class LoanManageJsonTest extends AbstractJsonTest {

  @Test
  public void testLoanManageJsonWithAllFields()
    throws JsonProcessingException, JSONException {

    LoanManage loanManage = LoanManage.builder()
      .account(Address.of("rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8"))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .sequence(UnsignedInteger.valueOf(196))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey(
          "ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD"
        )
      )
      .flags(LoanManageFlags.of(
        LoanManageFlags.LOAN_DEFAULT.getValue()
      ))
      .loanId(Hash256.of(
        "A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2"
      ))
      .build();

    String json = "{" +
      "\"Account\":\"rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8\"," +
      "\"Fee\":\"15\"," +
      "\"Sequence\":196," +
      "\"Flags\":65536," +
      "\"SigningPubKey\":" +
      "\"ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD\"," +
      "\"LoanID\":" +
      "\"A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2\"," +
      "\"TransactionType\":\"LoanManage\"" +
      "}";

    assertCanSerializeAndDeserialize(loanManage, json);
  }

  @Test
  public void testLoanManageJsonWithRequiredFieldsOnly()
    throws JsonProcessingException, JSONException {

    LoanManage loanManage = LoanManage.builder()
      .account(Address.of("rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8"))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .sequence(UnsignedInteger.valueOf(196))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey(
          "ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD"
        )
      )
      .loanId(Hash256.of(
        "A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2"
      ))
      .build();

    String json = "{" +
      "\"Account\":\"rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8\"," +
      "\"Fee\":\"15\"," +
      "\"Sequence\":196," +
      "\"SigningPubKey\":" +
      "\"ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD\"," +
      "\"LoanID\":" +
      "\"A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2\"," +
      "\"TransactionType\":\"LoanManage\"" +
      "}";

    assertCanSerializeAndDeserialize(loanManage, json);
  }

  @Test
  public void testLoanManageJsonWithImpairFlag()
    throws JsonProcessingException, JSONException {

    LoanManage loanManage = LoanManage.builder()
      .account(Address.of("rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8"))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .sequence(UnsignedInteger.valueOf(196))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey(
          "ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD"
        )
      )
      .flags(LoanManageFlags.of(
        LoanManageFlags.LOAN_IMPAIR.getValue()
      ))
      .loanId(Hash256.of(
        "A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2"
      ))
      .build();

    String json = "{" +
      "\"Account\":\"rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8\"," +
      "\"Fee\":\"15\"," +
      "\"Sequence\":196," +
      "\"Flags\":131072," +
      "\"SigningPubKey\":" +
      "\"ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD\"," +
      "\"LoanID\":" +
      "\"A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2\"," +
      "\"TransactionType\":\"LoanManage\"" +
      "}";

    assertCanSerializeAndDeserialize(loanManage, json);
  }

  @Test
  public void testLoanManageJsonWithUnimpairFlag()
    throws JsonProcessingException, JSONException {

    LoanManage loanManage = LoanManage.builder()
      .account(Address.of("rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8"))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .sequence(UnsignedInteger.valueOf(196))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey(
          "ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD"
        )
      )
      .flags(LoanManageFlags.of(
        LoanManageFlags.LOAN_UNIMPAIR.getValue()
      ))
      .loanId(Hash256.of(
        "A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2"
      ))
      .build();

    String json = "{" +
      "\"Account\":\"rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8\"," +
      "\"Fee\":\"15\"," +
      "\"Sequence\":196," +
      "\"Flags\":262144," +
      "\"SigningPubKey\":" +
      "\"ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD\"," +
      "\"LoanID\":" +
      "\"A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2\"," +
      "\"TransactionType\":\"LoanManage\"" +
      "}";

    assertCanSerializeAndDeserialize(loanManage, json);
  }
}
