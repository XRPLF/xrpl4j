package org.xrpl.xrpl4j.model.client.accounts;

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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;

public class GatewayBalancesResultsJsonTest extends AbstractJsonTest {

  @Test
  public void testWithLedgerHash() throws Exception {
    String json =
      "    {\n" +
      "        \"account\": \"rMwjYedjc7qqtKYVLiAccJSmCwih4LnE2q\",\n" +
      "        \"assets\": {\n" +
      "            \"r9F6wk8HkXrgYWoJ7fsv4VrUBVoqDVtzkH\": [\n" +
      "                {\n" +
      "                    \"currency\": \"BTC\",\n" +
      "                    \"value\": \"5444166510000000e-26\"\n" +
      "                }\n" +
      "            ],\n" +
      "            \"rPFLkxQk6xUGdGYEykqe7PR25Gr7mLHDc8\": [\n" +
      "                {\n" +
      "                    \"currency\": \"EUR\",\n" +
      "                    \"value\": \"4000000000000000e-27\"\n" +
      "                }\n" +
      "            ],\n" +
      "            \"rPU6VbckqCLW4kb51CWqZdxvYyQrQVsnSj\": [\n" +
      "                {\n" +
      "                    \"currency\": \"BTC\",\n" +
      "                    \"value\": \"1029900000000000e-26\"\n" +
      "                }\n" +
      "            ],\n" +
      "            \"rpR95n1iFkTqpoy1e878f4Z1pVHVtWKMNQ\": [\n" +
      "                {\n" +
      "                    \"currency\": \"BTC\",\n" +
      "                    \"value\": \"4000000000000000e-30\"\n" +
      "                }\n" +
      "            ],\n" +
      "            \"rwmUaXsWtXU4Z843xSYwgt1is97bgY8yj6\": [\n" +
      "                {\n" +
      "                    \"currency\": \"BTC\",\n" +
      "                    \"value\": \"8700000000000000e-30\"\n" +
      "                }\n" +
      "            ]\n" +
      "        },\n" +
      "        \"balances\": {\n" +
      "            \"rKm4uWpg9tfwbVSeATv4KxDe6mpE9yPkgJ\": [\n" +
      "                {\n" +
      "                    \"currency\": \"EUR\",\n" +
      "                    \"value\": \"29826.1965999999\"\n" +
      "                }\n" +
      "            ],\n" +
      "            \"ra7JkEzrgeKHdzKgo4EUUVBnxggY4z37kt\": [\n" +
      "                {\n" +
      "                    \"currency\": \"USD\",\n" +
      "                    \"value\": \"13857.70416\"\n" +
      "                }\n" +
      "            ]\n" +
      "        },\n" +
      "        \"ledger_hash\": \"980FECF48CA4BFDEC896692C31A50D484BDFE865EC101B00259C413AA3DBD672\",\n" +
      "        \"obligations\": {\n" +
      "            \"BTC\": \"5908.324927635318\",\n" +
      "            \"EUR\": \"992471.7419793958\",\n" +
      "            \"GBP\": \"4991.38706013193\",\n" +
      "            \"USD\": \"1997134.20229482\"\n" +
      "        },\n" +
      "        \"status\": \"success\",\n" +
      "        \"validated\": true\n" +
      "    }";

    GatewayBalancesResult result = GatewayBalancesResult
      .builder()
      .balances(hotWallets)
      .assets(assets)
      .obligations(obligations)
      .ledgerHash(Hash256.of("980FECF48CA4BFDEC896692C31A50D484BDFE865EC101B00259C413AA3DBD672"))
      .account(issuer)
      .status("success")
      .validated(true)
      .build();

    assertCanDeserialize(json, result);
  }

  @Test
  public void testWithLedgerIndex() throws Exception {
    String json =
      "    {\n" +
        "        \"account\": \"rMwjYedjc7qqtKYVLiAccJSmCwih4LnE2q\",\n" +
        "        \"assets\": {\n" +
        "            \"r9F6wk8HkXrgYWoJ7fsv4VrUBVoqDVtzkH\": [\n" +
        "                {\n" +
        "                    \"currency\": \"BTC\",\n" +
        "                    \"value\": \"5444166510000000e-26\"\n" +
        "                }\n" +
        "            ],\n" +
        "            \"rPFLkxQk6xUGdGYEykqe7PR25Gr7mLHDc8\": [\n" +
        "                {\n" +
        "                    \"currency\": \"EUR\",\n" +
        "                    \"value\": \"4000000000000000e-27\"\n" +
        "                }\n" +
        "            ],\n" +
        "            \"rPU6VbckqCLW4kb51CWqZdxvYyQrQVsnSj\": [\n" +
        "                {\n" +
        "                    \"currency\": \"BTC\",\n" +
        "                    \"value\": \"1029900000000000e-26\"\n" +
        "                }\n" +
        "            ],\n" +
        "            \"rpR95n1iFkTqpoy1e878f4Z1pVHVtWKMNQ\": [\n" +
        "                {\n" +
        "                    \"currency\": \"BTC\",\n" +
        "                    \"value\": \"4000000000000000e-30\"\n" +
        "                }\n" +
        "            ],\n" +
        "            \"rwmUaXsWtXU4Z843xSYwgt1is97bgY8yj6\": [\n" +
        "                {\n" +
        "                    \"currency\": \"BTC\",\n" +
        "                    \"value\": \"8700000000000000e-30\"\n" +
        "                }\n" +
        "            ]\n" +
        "        },\n" +
        "        \"balances\": {\n" +
        "            \"rKm4uWpg9tfwbVSeATv4KxDe6mpE9yPkgJ\": [\n" +
        "                {\n" +
        "                    \"currency\": \"EUR\",\n" +
        "                    \"value\": \"29826.1965999999\"\n" +
        "                }\n" +
        "            ],\n" +
        "            \"ra7JkEzrgeKHdzKgo4EUUVBnxggY4z37kt\": [\n" +
        "                {\n" +
        "                    \"currency\": \"USD\",\n" +
        "                    \"value\": \"13857.70416\"\n" +
        "                }\n" +
        "            ]\n" +
        "        },\n" +
        "        \"ledger_index\": 14483212,\n" +
        "        \"obligations\": {\n" +
        "            \"BTC\": \"5908.324927635318\",\n" +
        "            \"EUR\": \"992471.7419793958\",\n" +
        "            \"GBP\": \"4991.38706013193\",\n" +
        "            \"USD\": \"1997134.20229482\"\n" +
        "        },\n" +
        "        \"status\": \"success\",\n" +
        "        \"validated\": true\n" +
        "    }";

    GatewayBalancesResult result = GatewayBalancesResult
      .builder()
      .balances(hotWallets)
      .assets(assets)
      .obligations(obligations)
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(14483212)))
      .account(issuer)
      .status("success")
      .validated(true)
      .build();

    assertCanDeserialize(json, result);
  }


  @Test
  public void testNoAssets() throws Exception {
    String json =
      "    {\n" +
        "        \"account\": \"rMwjYedjc7qqtKYVLiAccJSmCwih4LnE2q\",\n" +
        "        \"balances\": {\n" +
        "            \"rKm4uWpg9tfwbVSeATv4KxDe6mpE9yPkgJ\": [\n" +
        "                {\n" +
        "                    \"currency\": \"EUR\",\n" +
        "                    \"value\": \"29826.1965999999\"\n" +
        "                }\n" +
        "            ],\n" +
        "            \"ra7JkEzrgeKHdzKgo4EUUVBnxggY4z37kt\": [\n" +
        "                {\n" +
        "                    \"currency\": \"USD\",\n" +
        "                    \"value\": \"13857.70416\"\n" +
        "                }\n" +
        "            ]\n" +
        "        },\n" +
        "        \"ledger_index\": 14483212,\n" +
        "        \"obligations\": {\n" +
        "            \"BTC\": \"5908.324927635318\",\n" +
        "            \"EUR\": \"992471.7419793958\",\n" +
        "            \"GBP\": \"4991.38706013193\",\n" +
        "            \"USD\": \"1997134.20229482\"\n" +
        "        },\n" +
        "        \"status\": \"success\",\n" +
        "        \"validated\": true\n" +
        "    }";

    GatewayBalancesResult result = GatewayBalancesResult
      .builder()
      .balances(hotWallets)
      .obligations(obligations)
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(14483212)))
      .account(issuer)
      .status("success")
      .validated(true)
      .build();

    assertCanDeserialize(json, result);
  }

  @Test
  public void testNoHotWallet() throws Exception {
    String json =
      "    {\n" +
        "        \"account\": \"rMwjYedjc7qqtKYVLiAccJSmCwih4LnE2q\",\n" +
        "        \"assets\": {\n" +
        "            \"r9F6wk8HkXrgYWoJ7fsv4VrUBVoqDVtzkH\": [\n" +
        "                {\n" +
        "                    \"currency\": \"BTC\",\n" +
        "                    \"value\": \"5444166510000000e-26\"\n" +
        "                }\n" +
        "            ],\n" +
        "            \"rPFLkxQk6xUGdGYEykqe7PR25Gr7mLHDc8\": [\n" +
        "                {\n" +
        "                    \"currency\": \"EUR\",\n" +
        "                    \"value\": \"4000000000000000e-27\"\n" +
        "                }\n" +
        "            ],\n" +
        "            \"rPU6VbckqCLW4kb51CWqZdxvYyQrQVsnSj\": [\n" +
        "                {\n" +
        "                    \"currency\": \"BTC\",\n" +
        "                    \"value\": \"1029900000000000e-26\"\n" +
        "                }\n" +
        "            ],\n" +
        "            \"rpR95n1iFkTqpoy1e878f4Z1pVHVtWKMNQ\": [\n" +
        "                {\n" +
        "                    \"currency\": \"BTC\",\n" +
        "                    \"value\": \"4000000000000000e-30\"\n" +
        "                }\n" +
        "            ],\n" +
        "            \"rwmUaXsWtXU4Z843xSYwgt1is97bgY8yj6\": [\n" +
        "                {\n" +
        "                    \"currency\": \"BTC\",\n" +
        "                    \"value\": \"8700000000000000e-30\"\n" +
        "                }\n" +
        "            ]\n" +
        "        },\n" +
        "        \"ledger_index\": 14483212,\n" +
        "        \"obligations\": {\n" +
        "            \"BTC\": \"5908.324927635318\",\n" +
        "            \"EUR\": \"992471.7419793958\",\n" +
        "            \"GBP\": \"4991.38706013193\",\n" +
        "            \"USD\": \"1997134.20229482\"\n" +
        "        },\n" +
        "        \"status\": \"success\",\n" +
        "        \"validated\": true\n" +
        "    }";

    GatewayBalancesResult result = GatewayBalancesResult
      .builder()
      .assets(assets)
      .obligations(obligations)
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(14483212)))
      .account(issuer)
      .status("success")
      .validated(true)
      .build();

    assertCanDeserialize(json, result);
  }

  @Test
  public void testNoObligations() throws Exception {
    String json =
      "    {\n" +
        "        \"account\": \"rMwjYedjc7qqtKYVLiAccJSmCwih4LnE2q\",\n" +
        "        \"assets\": {\n" +
        "            \"r9F6wk8HkXrgYWoJ7fsv4VrUBVoqDVtzkH\": [\n" +
        "                {\n" +
        "                    \"currency\": \"BTC\",\n" +
        "                    \"value\": \"5444166510000000e-26\"\n" +
        "                }\n" +
        "            ],\n" +
        "            \"rPFLkxQk6xUGdGYEykqe7PR25Gr7mLHDc8\": [\n" +
        "                {\n" +
        "                    \"currency\": \"EUR\",\n" +
        "                    \"value\": \"4000000000000000e-27\"\n" +
        "                }\n" +
        "            ],\n" +
        "            \"rPU6VbckqCLW4kb51CWqZdxvYyQrQVsnSj\": [\n" +
        "                {\n" +
        "                    \"currency\": \"BTC\",\n" +
        "                    \"value\": \"1029900000000000e-26\"\n" +
        "                }\n" +
        "            ],\n" +
        "            \"rpR95n1iFkTqpoy1e878f4Z1pVHVtWKMNQ\": [\n" +
        "                {\n" +
        "                    \"currency\": \"BTC\",\n" +
        "                    \"value\": \"4000000000000000e-30\"\n" +
        "                }\n" +
        "            ],\n" +
        "            \"rwmUaXsWtXU4Z843xSYwgt1is97bgY8yj6\": [\n" +
        "                {\n" +
        "                    \"currency\": \"BTC\",\n" +
        "                    \"value\": \"8700000000000000e-30\"\n" +
        "                }\n" +
        "            ]\n" +
        "        },\n" +
        "        \"balances\": {\n" +
        "            \"rKm4uWpg9tfwbVSeATv4KxDe6mpE9yPkgJ\": [\n" +
        "                {\n" +
        "                    \"currency\": \"EUR\",\n" +
        "                    \"value\": \"29826.1965999999\"\n" +
        "                }\n" +
        "            ],\n" +
        "            \"ra7JkEzrgeKHdzKgo4EUUVBnxggY4z37kt\": [\n" +
        "                {\n" +
        "                    \"currency\": \"USD\",\n" +
        "                    \"value\": \"13857.70416\"\n" +
        "                }\n" +
        "            ]\n" +
        "        },\n" +
        "        \"ledger_index\": 14483212,\n" +
        "        \"status\": \"success\",\n" +
        "        \"validated\": true\n" +
        "    }";

    GatewayBalancesResult result = GatewayBalancesResult
      .builder()
      .assets(assets)
      .balances(hotWallets)
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(14483212)))
      .account(issuer)
      .status("success")
      .validated(true)
      .build();

    assertCanDeserialize(json, result);
  }

  private Address issuer = Address.of("rMwjYedjc7qqtKYVLiAccJSmCwih4LnE2q");

  private GatewayBalancesObligations obligations = GatewayBalancesObligations
    .builder()
    .balances(Lists.newArrayList(
      GatewayBalancesIssuedCurrencyAmount.builder().currency("BTC").value("5908.324927635318").build(),
      GatewayBalancesIssuedCurrencyAmount.builder().currency("EUR").value("992471.7419793958").build(),
      GatewayBalancesIssuedCurrencyAmount.builder().currency("GBP").value("4991.38706013193").build(),
      GatewayBalancesIssuedCurrencyAmount.builder().currency("USD").value("1997134.20229482").build()
    ))
    .build();

  private GatewayBalancesHotWallets hotWallets = GatewayBalancesHotWallets
    .builder()
    .balancesByHolder(ImmutableMap.of(
      Address.of("rKm4uWpg9tfwbVSeATv4KxDe6mpE9yPkgJ"),
      Lists.newArrayList(
        GatewayBalancesIssuedCurrencyAmount
          .builder()
          .currency("EUR")
          .value("29826.1965999999")
          .build()
      ),
      Address.of("ra7JkEzrgeKHdzKgo4EUUVBnxggY4z37kt"),
      Lists.newArrayList(
        GatewayBalancesIssuedCurrencyAmount
          .builder()
          .currency("USD")
          .value("13857.70416")
          .build()
      )
    ))
    .build();

  private GatewayBalancesAssets assets = GatewayBalancesAssets
    .builder()
    .balancesByIssuer(ImmutableMap.of(
      Address.of("r9F6wk8HkXrgYWoJ7fsv4VrUBVoqDVtzkH"),
      Lists.newArrayList(
        GatewayBalancesIssuedCurrencyAmount
          .builder()
          .currency("BTC")
          .value("5444166510000000e-26")
          .build()
      ),
      Address.of("rPFLkxQk6xUGdGYEykqe7PR25Gr7mLHDc8"),
      Lists.newArrayList(
        GatewayBalancesIssuedCurrencyAmount
          .builder()
          .currency("EUR")
          .value("4000000000000000e-27")
          .build()
      ),
      Address.of("rPU6VbckqCLW4kb51CWqZdxvYyQrQVsnSj"),
      Lists.newArrayList(
        GatewayBalancesIssuedCurrencyAmount
          .builder()
          .currency("BTC")
          .value("1029900000000000e-26")
          .build()
      ),
      Address.of("rpR95n1iFkTqpoy1e878f4Z1pVHVtWKMNQ"),
      Lists.newArrayList(
        GatewayBalancesIssuedCurrencyAmount
          .builder()
          .currency("BTC")
          .value("4000000000000000e-30")
          .build()
      ),
      Address.of("rwmUaXsWtXU4Z843xSYwgt1is97bgY8yj6"),
      Lists.newArrayList(
        GatewayBalancesIssuedCurrencyAmount
          .builder()
          .currency("BTC")
          .value("8700000000000000e-30")
          .build()
      )
    ))
    .build();

}
