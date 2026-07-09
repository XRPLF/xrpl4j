package org.xrpl.xrpl4j.model.ledger;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.MpTokenFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.EncryptedAmount;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.MpTokenNumericAmount;

class MpTokenObjectTest extends AbstractJsonTest {

  @Test
  void testJson() throws JSONException, JsonProcessingException {
    MpTokenObject mpTokenObject = MpTokenObject.builder()
      .account(Address.of("rUKufYPXYm5SRyfAQyn49j7fkMi4A8iwZ6"))
      .flags(MpTokenFlags.UNSET)
      .mptAmount(MpTokenNumericAmount.of(100000))
      .mpTokenIssuanceId(MpTokenIssuanceId.of("0000014A338173F978C65EB486DF107E9662F2E847A70F8A"))
      .ownerNode("0")
      .previousTransactionId(Hash256.of("F74BB6C80570839BBD4B9DE198BD1A3E7C633659250BA2BE5763A58068DF3C11"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(473))
      .index(Hash256.of("0B5F0A170BA45CB6BCF4B92319D63287429F4B7E7024C82AFB4D4275C43F5A6C"))
      .build();
    String json = "{\n" +
                  "  \"Account\" : \"rUKufYPXYm5SRyfAQyn49j7fkMi4A8iwZ6\",\n" +
                  "  \"Flags\" : 0,\n" +
                  "  \"LedgerEntryType\" : \"MPToken\",\n" +
                  "  \"MPTAmount\" : \"100000\",\n" +
                  "  \"MPTokenIssuanceID\" : \"0000014A338173F978C65EB486DF107E9662F2E847A70F8A\",\n" +
                  "  \"OwnerNode\" : \"0\",\n" +
                  "  \"ConfidentialBalanceVersion\" : 0,\n" +
                  "  \"PreviousTxnID\" : \"F74BB6C80570839BBD4B9DE198BD1A3E7C633659250BA2BE5763A58068DF3C11\",\n" +
                  "  \"PreviousTxnLgrSeq\" : 473,\n" +
                  "  \"index\" : \"0B5F0A170BA45CB6BCF4B92319D63287429F4B7E7024C82AFB4D4275C43F5A6C\"\n" +
                  "}";

    assertCanSerializeAndDeserialize(mpTokenObject, json);
  }

  @Test
  void testDefaultFields() {
    MpTokenObject mpTokenObject = MpTokenObject.builder()
      .account(Address.of("rUKufYPXYm5SRyfAQyn49j7fkMi4A8iwZ6"))
      .flags(MpTokenFlags.UNSET)
      .mpTokenIssuanceId(MpTokenIssuanceId.of("0000014A338173F978C65EB486DF107E9662F2E847A70F8A"))
      .ownerNode("0")
      .previousTransactionId(Hash256.of("F74BB6C80570839BBD4B9DE198BD1A3E7C633659250BA2BE5763A58068DF3C11"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(473))
      .index(Hash256.of("0B5F0A170BA45CB6BCF4B92319D63287429F4B7E7024C82AFB4D4275C43F5A6C"))
      .build();

    assertThat(mpTokenObject.mptAmount()).isEqualTo(MpTokenNumericAmount.of(0));
    assertThat(mpTokenObject.ledgerEntryType()).isEqualTo(LedgerObject.LedgerEntryType.MP_TOKEN);
  }

  @Test
  void testJsonWithLockedAmount() throws JSONException, JsonProcessingException {
    MpTokenObject mpTokenObject = MpTokenObject.builder()
      .account(Address.of("rUKufYPXYm5SRyfAQyn49j7fkMi4A8iwZ6"))
      .flags(MpTokenFlags.UNSET)
      .mptAmount(MpTokenNumericAmount.of(100000))
      .lockedAmount(MpTokenNumericAmount.of(5000))
      .mpTokenIssuanceId(MpTokenIssuanceId.of("0000014A338173F978C65EB486DF107E9662F2E847A70F8A"))
      .ownerNode("0")
      .previousTransactionId(Hash256.of("F74BB6C80570839BBD4B9DE198BD1A3E7C633659250BA2BE5763A58068DF3C11"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(473))
      .index(Hash256.of("0B5F0A170BA45CB6BCF4B92319D63287429F4B7E7024C82AFB4D4275C43F5A6C"))
      .build();

    String json = "{\n" +
                  "  \"Account\" : \"rUKufYPXYm5SRyfAQyn49j7fkMi4A8iwZ6\",\n" +
                  "  \"Flags\" : 0,\n" +
                  "  \"LedgerEntryType\" : \"MPToken\",\n" +
                  "  \"MPTAmount\" : \"100000\",\n" +
                  "  \"LockedAmount\" : \"5000\",\n" +
                  "  \"MPTokenIssuanceID\" : \"0000014A338173F978C65EB486DF107E9662F2E847A70F8A\",\n" +
                  "  \"OwnerNode\" : \"0\",\n" +
                  "  \"ConfidentialBalanceVersion\" : 0,\n" +
                  "  \"PreviousTxnID\" : \"F74BB6C80570839BBD4B9DE198BD1A3E7C633659250BA2BE5763A58068DF3C11\",\n" +
                  "  \"PreviousTxnLgrSeq\" : 473,\n" +
                  "  \"index\" : \"0B5F0A170BA45CB6BCF4B92319D63287429F4B7E7024C82AFB4D4275C43F5A6C\"\n" +
                  "}";

    assertCanSerializeAndDeserialize(mpTokenObject, json);
  }

  @Test
  void testLockedAmountIsOptional() {
    MpTokenObject mpTokenObject = MpTokenObject.builder()
      .account(Address.of("rUKufYPXYm5SRyfAQyn49j7fkMi4A8iwZ6"))
      .flags(MpTokenFlags.UNSET)
      .mptAmount(MpTokenNumericAmount.of(100000))
      .mpTokenIssuanceId(MpTokenIssuanceId.of("0000014A338173F978C65EB486DF107E9662F2E847A70F8A"))
      .ownerNode("0")
      .previousTransactionId(Hash256.of("F74BB6C80570839BBD4B9DE198BD1A3E7C633659250BA2BE5763A58068DF3C11"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(473))
      .index(Hash256.of("0B5F0A170BA45CB6BCF4B92319D63287429F4B7E7024C82AFB4D4275C43F5A6C"))
      .build();

    assertThat(mpTokenObject.lockedAmount()).isEmpty();
  }

  @Test
  void testJsonWithConfidentialFields() throws JSONException, JsonProcessingException {
    MpTokenObject mpTokenObject = MpTokenObject.builder()
      .account(Address.of("rUKufYPXYm5SRyfAQyn49j7fkMi4A8iwZ6"))
      .flags(MpTokenFlags.UNSET)
      .mptAmount(MpTokenNumericAmount.of(100000))
      .mpTokenIssuanceId(MpTokenIssuanceId.of("0000014A338173F978C65EB486DF107E9662F2E847A70F8A"))
      .ownerNode("0")
      .holderEncryptionKey(PublicKey.fromBase16EncodedPublicKey(
        "028D7500BFCD792B487E4E51664037AB543E76CEBACF0E7E17AD4B83057E1F2B30"
      ))
      .confidentialBalanceSpending(EncryptedAmount.of(Strings.repeat("AB", 66)))
      .confidentialBalanceInbox(EncryptedAmount.of(Strings.repeat("CD", 66)))
      .issuerEncryptedBalance(EncryptedAmount.of(Strings.repeat("EF", 66)))
      .auditorEncryptedBalance(EncryptedAmount.of(Strings.repeat("11", 66)))
      .confidentialBalanceVersion(UnsignedInteger.valueOf(3))
      .previousTransactionId(Hash256.of("F74BB6C80570839BBD4B9DE198BD1A3E7C633659250BA2BE5763A58068DF3C11"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(473))
      .index(Hash256.of("0B5F0A170BA45CB6BCF4B92319D63287429F4B7E7024C82AFB4D4275C43F5A6C"))
      .build();

    String json = "{\n" +
                  "  \"Account\" : \"rUKufYPXYm5SRyfAQyn49j7fkMi4A8iwZ6\",\n" +
                  "  \"Flags\" : 0,\n" +
                  "  \"LedgerEntryType\" : \"MPToken\",\n" +
                  "  \"MPTAmount\" : \"100000\",\n" +
                  "  \"MPTokenIssuanceID\" : \"0000014A338173F978C65EB486DF107E9662F2E847A70F8A\",\n" +
                  "  \"OwnerNode\" : \"0\",\n" +
                  "  \"HolderEncryptionKey\" : " +
                  "\"028D7500BFCD792B487E4E51664037AB543E76CEBACF0E7E17AD4B83057E1F2B30\",\n" +
                  "  \"ConfidentialBalanceSpending\" : \"" + Strings.repeat("AB", 66) + "\",\n" +
                  "  \"ConfidentialBalanceInbox\" : \"" + Strings.repeat("CD", 66) + "\",\n" +
                  "  \"IssuerEncryptedBalance\" : \"" + Strings.repeat("EF", 66) + "\",\n" +
                  "  \"AuditorEncryptedBalance\" : \"" + Strings.repeat("11", 66) + "\",\n" +
                  "  \"ConfidentialBalanceVersion\" : 3,\n" +
                  "  \"PreviousTxnID\" : \"F74BB6C80570839BBD4B9DE198BD1A3E7C633659250BA2BE5763A58068DF3C11\",\n" +
                  "  \"PreviousTxnLgrSeq\" : 473,\n" +
                  "  \"index\" : \"0B5F0A170BA45CB6BCF4B92319D63287429F4B7E7024C82AFB4D4275C43F5A6C\"\n" +
                  "}";

    assertCanSerializeAndDeserialize(mpTokenObject, json);
  }
}
