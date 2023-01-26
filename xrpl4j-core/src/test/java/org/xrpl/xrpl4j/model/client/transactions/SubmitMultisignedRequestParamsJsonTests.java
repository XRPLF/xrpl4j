package org.xrpl.xrpl4j.model.client.transactions;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Signer;
import org.xrpl.xrpl4j.model.transactions.SignerWrapper;
import org.xrpl.xrpl4j.model.transactions.TrustSet;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class SubmitMultisignedRequestParamsJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {

    SubmitMultiSignedRequestParams params = SubmitMultiSignedRequestParams.of(
      TrustSet.builder()
        .account(Address.of("rEuLyBCvcw4CFmzv8RepSiAoNgF8tTGJQC"))
        .fee(XrpCurrencyAmount.ofDrops(30000))
        .flags(Flags.TrustSetFlags.of(262144))
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
              .signingPublicKey(
                PublicKey.fromBase16EncodedPublicKey(
                  "02B3EC4E5DD96029A647CFA20DA07FE1F85296505552CCAC114087E66B46BD77DF"
                )
              )
              .transactionSignature("3045022100CC9C56DF51251CB04BB047E5F3B5EF01A0F4A8A549D7A20A7402BF" +
                "54BA744064022061EF8EF1BCCBF144F480B32508B1D10FD4271831D5303F920DE41C64671CB5B7")
              .build()
          ),
          SignerWrapper.of(
            Signer.builder()
              .account(Address.of("raKEEVSGnKSD9Zyvxu4z6Pqpm4ABH8FS6n"))
              .signingPublicKey(
                PublicKey.fromBase16EncodedPublicKey(
                  "03398A4EDAE8EE009A5879113EAA5BA15C7BB0F612A87F4103E793AC919BD1E3C1"
                )
              )
              .transactionSignature("3045022100FEE8D8FA2D06CE49E9124567DCA265A21A9F5465F4A927" +
                "9F075E4CE27E4430DE022042D5305777DA1A7801446780308897699412E4EDF0E1AEFDF3C8A0532BDE4D08")
              .build()
          )
        )
        .build()
    );

    String json = "{\n" +
      "            \"tx_json\": {\n" +
      "                \"Account\": \"rEuLyBCvcw4CFmzv8RepSiAoNgF8tTGJQC\",\n" +
      "                \"Fee\": \"30000\",\n" +
      "                \"Flags\": 262144,\n" +
      "                \"LimitAmount\": {\n" +
      "                    \"currency\": \"USD\",\n" +
      "                    \"issuer\": \"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh\",\n" +
      "                    \"value\": \"0\"\n" +
      "                },\n" +
      "                \"Sequence\": 4,\n" +
      "                \"Signers\": [\n" +
      "                    {\n" +
      "                        \"Signer\": {\n" +
      "                            \"Account\": \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\",\n" +
      "                            \"SigningPubKey\": \"02B3EC4E5DD96029A647CFA20DA07FE1F85296505552CCAC114087E6" +
      "6B46BD77DF\",\n" +
      "                            \"TxnSignature\": \"3045022100CC9C56DF51251CB04BB047E5F3B5EF01A0F4A8A549D7A20" +
      "A7402BF54BA744064022061EF8EF1BCCBF144F480B32508B1D10FD4271831D5303F920DE41C64671CB5B7\"\n" +
      "                        }\n" +
      "                    },\n" +
      "                    {\n" +
      "                        \"Signer\": {\n" +
      "                            \"Account\": \"raKEEVSGnKSD9Zyvxu4z6Pqpm4ABH8FS6n\",\n" +
      "                            \"SigningPubKey\": \"03398A4EDAE8EE009A5879113EAA5BA15C7BB0F612A87F4103E793A" +
      "C919BD1E3C1\",\n" +
      "                            \"TxnSignature\": \"3045022100FEE8D8FA2D06CE49E9124567DCA265A21A9F5465F4A927" +
      "9F075E4CE27E4430DE022042D5305777DA1A7801446780308897699412E4EDF0E1AEFDF3C8A0532BDE4D08\"\n" +
      "                        }\n" +
      "                    }\n" +
      "                ],\n" +
      "                \"SigningPubKey\": \"\",\n" +
      "                \"TransactionType\": \"TrustSet\"\n" +
      "            }\n" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }
}
