package org.xrpl.xrpl4j.model.jackson.modules;

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

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.xrpl.xrpl4j.model.client.accounts.AccountTransactionsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountTransactionsTransaction;
import org.xrpl.xrpl4j.model.client.accounts.GatewayBalancesAssets;
import org.xrpl.xrpl4j.model.client.accounts.ImmutableGatewayBalancesAssets;
import org.xrpl.xrpl4j.model.client.accounts.ImmutableGatewayBalancesHotWallets;
import org.xrpl.xrpl4j.model.client.accounts.ImmutableGatewayBalancesObligations;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerIndexBound;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Marker;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;
import org.xrpl.xrpl4j.model.transactions.NfTokenUri;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.TransferFee;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * Jackson module for the xrpl4j-model project.
 */
public class Xrpl4jModule extends SimpleModule {

  private static final String NAME = "Xrpl4jModule";

  /**
   * No-arg constructor.
   */
  public Xrpl4jModule() {
    super(
      NAME,
      new Version(
        1,
        0,
        0,
        null,
        "org.xrpl.xrpl4j",
        "xrpl4j"
      )
    );

    addSerializer(Address.class, new AddressSerializer());
    addDeserializer(Address.class, new AddressDeserializer());

    addSerializer(Hash256.class, new Hash256Serializer());
    addDeserializer(Hash256.class, new Hash256Deserializer());

    addSerializer(NfTokenId.class, new NfTokenIdSerializer());
    addDeserializer(NfTokenId.class, new NfTokenIdDeserializer());

    addSerializer(TransferFee.class, new TransferFeeSerializer());
    addDeserializer(TransferFee.class, new TransferFeeDeserializer());

    addSerializer(NfTokenUri.class, new NfTokenUriSerializer());

    addSerializer(XrpCurrencyAmount.class, new XrpCurrencyAmountSerializer());
    addDeserializer(XrpCurrencyAmount.class, new XrpCurrencyAmountDeserializer());

    addDeserializer(CurrencyAmount.class, new CurrencyAmountDeserializer());

    addSerializer(LedgerIndex.class, new LedgerIndexSerializer());
    addDeserializer(LedgerIndex.class, new LedgerIndexDeserializer());

    addSerializer(LedgerIndexBound.class, new LedgerIndexBoundSerializer());
    addDeserializer(LedgerIndexBound.class, new LedgerIndexBoundDeserializer());

    addSerializer(LedgerSpecifier.class, new LedgerSpecifierSerializer());
    addDeserializer(LedgerSpecifier.class, new LedgerSpecifierDeserializer());

    addDeserializer(Transaction.class, new TransactionDeserializer());

    addSerializer(Marker.class, new MarkerSerializer());
    addDeserializer(Marker.class, new MarkerDeserializer());

    addDeserializer(AccountTransactionsRequestParams.class, new AccountTransactionsRequestParamsDeserializer());
    addDeserializer(AccountTransactionsTransaction.class, new AccountTransactionsTransactionDeserializer());

    addDeserializer(GatewayBalancesAssets.class, new GatewayBalancesAssetsDeserializer());
    addDeserializer(ImmutableGatewayBalancesAssets.class, new GatewayBalancesAssetsDeserializer());
    addDeserializer(ImmutableGatewayBalancesHotWallets.class, new GatewayBalancesHotWalletsDeserializer());
    addDeserializer(ImmutableGatewayBalancesObligations.class, new GatewayBalancesObligationsDeserializer());
  }
}
