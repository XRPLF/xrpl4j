package org.xrpl.xrpl4j.model.client.transactions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Signer;
import org.xrpl.xrpl4j.model.transactions.SignerWrapper;
import org.xrpl.xrpl4j.model.transactions.TrustSet;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class SubmitMultisignedResultJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    SubmitMultiSignedResult<TrustSet> result = SubmitMultiSignedResult.<TrustSet>builder()
        .engineResult("tesSUCCESS")
        .engineResultMessage("The transaction was applied. Only final in a validated ledger.")
        .status("success")
        .transactionBlob("120014220004000024000000046380000000000000000000000000000000000000005553440000000000B" +
            "5F762798A53D543A014CAF8B297CFF8F2F937E868400000000000753073008114A3780F5CB5A44D366520FC44055E8ED44" +
            "D9A2270F3E010732102B3EC4E5DD96029A647CFA20DA07FE1F85296505552CCAC114087E66B46BD77DF74473045022100C" +
            "C9C56DF51251CB04BB047E5F3B5EF01A0F4A8A549D7A20A7402BF54BA744064022061EF8EF1BCCBF144F480B32508B1D10" +
            "FD4271831D5303F920DE41C64671CB5B78114204288D2E47F8EF6C99BCC457966320D12409711E1E010732103398A4EDAE" +
            "8EE009A5879113EAA5BA15C7BB0F612A87F4103E793AC919BD1E3C174473045022100FEE8D8FA2D06CE49E9124567DCA26" +
            "5A21A9F5465F4A9279F075E4CE27E4430DE022042D5305777DA1A7801446780308897699412E4EDF0E1AEFDF3C8A0532BD" +
            "E4D0881143A4C02EA95AD6AC3BED92FA036E0BBFB712C030CE1F1")
        .transaction(TransactionResult.<TrustSet>builder()
            .transaction(
                TrustSet.builder()
                    .account(Address.of("rEuLyBCvcw4CFmzv8RepSiAoNgF8tTGJQC"))
                    .fee(XrpCurrencyAmount.ofDrops(30000))
                    .flags(Flags.TrustSetFlags.of(262144))
                    .signingPublicKey("")
                    .limitAmount(IssuedCurrencyAmount.builder()
                        .currency("USD")
                        .issuer(Address.of("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"))
                        .value("0")
                        .build())
                    .sequence(UnsignedInteger.valueOf(4))
                    .addSigners(
                        SignerWrapper.of(
                            Signer.builder()
                                .account(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
                                .signingPublicKey("02B3EC4E5DD96029A647CFA20DA07FE1F85296505552CCAC114087E66B46BD77DF")
                                .transactionSignature("3045022100CC9C56DF51251CB04BB047E5F3B5EF01A0F4A8A549D7A20A7402" +
                                    "BF54BA744064022061EF8EF1BCCBF144F480B32508B1D10FD4271831D5303F920DE41C64671CB5B7")
                                .build()
                        ),
                        SignerWrapper.of(
                            Signer.builder()
                                .account(Address.of("raKEEVSGnKSD9Zyvxu4z6Pqpm4ABH8FS6n"))
                                .signingPublicKey("03398A4EDAE8EE009A5879113EAA5BA15C7BB0F612A87F4103E793AC919BD1E3C1")
                                .transactionSignature("3045022100FEE8D8FA2D06CE49E9124567DCA265A21A9F5465F4A927" +
                                    "9F075E4CE27E4430DE022042D5305777DA1A7801446780308897699412E4EDF0E1AEFDF3C8" +
                                    "A0532BDE4D08")
                                .build()
                        )
                    )
                    .hash(Hash256.of("81A477E2A362D171BB16BE17B4120D9F809A327FA00242ABCA867283BEA2F4F8"))
                    .build()
            )
            .build())
        .build();

    String json = "{\n" +
        "        \"engine_result\": \"tesSUCCESS\",\n" +
        "        \"engine_result_message\": \"The transaction was applied. Only final in a validated ledger.\",\n" +
        "        \"status\": \"success\",\n" +
        "        \"tx_blob\": \"12001422000400002400000004638000000000000000000000000000000000000000555344000000" +
        "0000B5F762798A53D543A014CAF8B297CFF8F2F937E868400000000000753073008114A3780F5CB5A44D366520FC44055E8ED44" +
        "D9A2270F3E010732102B3EC4E5DD96029A647CFA20DA07FE1F85296505552CCAC114087E66B46BD77DF74473045022100CC9C56" +
        "DF51251CB04BB047E5F3B5EF01A0F4A8A549D7A20A7402BF54BA744064022061EF8EF1BCCBF144F480B32508B1D10FD4271831D" +
        "5303F920DE41C64671CB5B78114204288D2E47F8EF6C99BCC457966320D12409711E1E010732103398A4EDAE8EE009A5879113E" +
        "AA5BA15C7BB0F612A87F4103E793AC919BD1E3C174473045022100FEE8D8FA2D06CE49E9124567DCA265A21A9F5465F4A9279F0" +
        "75E4CE27E4430DE022042D5305777DA1A7801446780308897699412E4EDF0E1AEFDF3C8A0532BDE4D0881143A4C02EA95AD6AC3" +
        "BED92FA036E0BBFB712C030CE1F1\",\n" +
        "        \"tx_json\": {\n" +
        "            \"Account\": \"rEuLyBCvcw4CFmzv8RepSiAoNgF8tTGJQC\",\n" +
        "            \"Fee\": \"30000\",\n" +
        "            \"Flags\": 262144,\n" +
        "            \"LimitAmount\": {\n" +
        "                \"currency\": \"USD\",\n" +
        "                \"issuer\": \"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh\",\n" +
        "                \"value\": \"0\"\n" +
        "            },\n" +
        "            \"Sequence\": 4,\n" +
        "            \"Signers\": [\n" +
        "                {\n" +
        "                    \"Signer\": {\n" +
        "                        \"Account\": \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\",\n" +
        "                        \"SigningPubKey\": \"02B3EC4E5DD96029A647CFA20DA07FE1F85296505552CCAC114087E66B4" +
        "6BD77DF\",\n" +
        "                        \"TxnSignature\": \"3045022100CC9C56DF51251CB04BB047E5F3B5EF01A0F4A8A549D7A20A7402B" +
        "F54BA744064022061EF8EF1BCCBF144F480B32508B1D10FD4271831D5303F920DE41C64671CB5B7\"\n" +
        "                    }\n" +
        "                },\n" +
        "                {\n" +
        "                    \"Signer\": {\n" +
        "                        \"Account\": \"raKEEVSGnKSD9Zyvxu4z6Pqpm4ABH8FS6n\",\n" +
        "                        \"SigningPubKey\": \"03398A4EDAE8EE009A5879113EAA5BA15C7BB0F612A87F4103E793AC919BD" +
        "1E3C1\",\n" +
        "                        \"TxnSignature\": \"3045022100FEE8D8FA2D06CE49E9124567DCA265A21A9F5465F4A9279F075E" +
        "4CE27E4430DE022042D5305777DA1A7801446780308897699412E4EDF0E1AE" +
        "FDF3C8A0532BDE4D08\"\n" +
        "                    }\n" +
        "                }\n" +
        "            ],\n" +
        "            \"SigningPubKey\": \"\",\n" +
        "            \"TransactionType\": \"TrustSet\",\n" +
        "            \"validated\": false,\n" +
        "            \"hash\": \"81A477E2A362D171BB16BE17B4120D9F809A327FA00242ABCA867283BEA2F4F8\"\n" +
        "        }\n" +
        "    }";

    assertCanSerializeAndDeserialize(result, json);
  }
}
