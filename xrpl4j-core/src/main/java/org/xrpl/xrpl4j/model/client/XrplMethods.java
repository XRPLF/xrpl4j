package org.xrpl.xrpl4j.model.client;

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

/**
 * A definition class for all rippled method name constants.
 */
public class XrplMethods {

  // Account methods
  /**
   * Constant for the
   * <a href="https://xrpl.org/account_channels.html">account_channels</a> rippled API method.
   */
  public static final String ACCOUNT_CHANNELS = "account_channels";

  /**
   * Constant for the <a href="https://xrpl.org/account_currencies.html">account_currencies</a> rippled API method.
   */
  public static final String ACCOUNT_CURRENCIES = "account_currencies";

  /**
   * Constant for the <a href="https://xrpl.org/account_info.html">account_info</a> rippled API method.
   */
  public static final String ACCOUNT_INFO = "account_info";

  /**
   * Constant for the <a href="https://xrpl.org/account_lines.html">account_lines</a> rippled API method.
   */
  public static final String ACCOUNT_LINES = "account_lines";

  /**
   * Constant for the <a href="https://xrpl.org/account_objects.html">account_objects</a> rippled API method.
   */
  public static final String ACCOUNT_OBJECTS = "account_objects";

  /**
   * Constant for the <a href="https://xrpl.org/account_offers.html">account_offers</a> rippled API method.
   */
  public static final String ACCOUNT_OFFERS = "account_offers";

  /**
   * Constant for the <a href="https://xrpl.org/account_tx.html">account_tx</a> rippled API method.
   */
  public static final String ACCOUNT_TX = "account_tx";

  /**
   * Constant for the <a href="https://xrpl.org/gateway_balances.html">gateway_balances</a> rippled API method.
   */
  public static final String GATEWAY_BALANCES = "gateway_balances";

  /**
   * Constant for the <a href="https://xrpl.org/noripple_check.html">noripple_check</a> rippled API method.
   */
  public static final String NORIPPLE_CHECK = "noripple_check";

  // Ledger methods
  /**
   * Constant for the <a href="https://xrpl.org/ledger.html">ledger</a> rippled API method.
   */
  public static final String LEDGER = "ledger";

  /**
   * Constant for the <a href="https://xrpl.org/ledger_closed.html">ledger_closed</a> rippled API method.
   */
  public static final String LEDGER_CLOSED = "ledger_closed";

  /**
   * Constant for the <a href="https://xrpl.org/ledger_current.html">ledger_current</a> rippled API method.
   */
  public static final String LEDGER_CURRENT = "ledger_current";

  /**
   * Constant for the <a href="https://xrpl.org/ledger_data.html">ledger_data</a> rippled API method.
   */
  public static final String LEDGER_DATA = "ledger_data";

  /**
   * Constant for the <a href="https://xrpl.org/ledger_entry.html">ledger_entry</a> rippled API method.
   */
  public static final String LEDGER_ENTRY = "ledger_entry";

  // NFT methods
  /**
   * Constant for the account_nfts rippled API method.
   */
  public static final String ACCOUNT_NFTS = "account_nfts";

  /**
   * Constant for the nft_buy_offers rippled API method.
   */
  public static final String NFT_BUY_OFFERS = "nft_buy_offers";

  /**
   * Constant for the nft_sell_offers rippled API method.
   */
  public static final String NFT_SELL_OFFERS = "nft_sell_offers";

  /**
   * Constant for the nft_info Clio API method.
   */
  public static final String NFT_INFO = "nft_info";

  // Transaction methods
  /**
   * Constant for the <a href="https://xrpl.org/sign.html">sign</a> rippled API method.
   */
  public static final String SIGN = "sign";

  /**
   * Constant for the <a href="https://xrpl.org/sign_for.html">sign_for</a> rippled API method.
   */
  public static final String SIGN_FOR = "sign_for";

  /**
   * Constant for the <a href="https://xrpl.org/submit.html">submit</a> rippled API method.
   */
  public static final String SUBMIT = "submit";

  /**
   * Constant for the <a href="https://xrpl.org/submit_multisigned.html">submit_multisigned</a> rippled API method.
   */
  public static final String SUBMIT_MULTISIGNED = "submit_multisigned";

  /**
   * Constant for the <a href="https://xrpl.org/transaction_entry.html">transaction_entry</a> rippled API method.
   */
  public static final String TRANSACTION_ENTRY = "transaction_entry";

  /**
   * Constant for the <a href="https://xrpl.org/tx.html">tx</a> rippled API method.
   */
  public static final String TX = "tx";

  /**
   * Constant for the <a href="https://xrpl.org/tx_history.html">tx_history</a> rippled API method.
   */
  public static final String TX_HISTORY = "tx_history";

  // Path and Order Book methods
  /**
   * Constant for the <a href="https://xrpl.org/book_offers.html">book_offers</a> rippled API method.
   */
  public static final String BOOK_OFFERS = "book_offers";

  /**
   * Constant for the <a href="https://xrpl.org/deposit_authorized.html">deposit_authorized</a> rippled API method.
   */
  public static final String DEPOSIT_AUTHORIZED = "deposit_authorized";

  /**
   * Constant for the <a href="https://xrpl.org/path_find.html">path_find</a> rippled API method.
   */
  public static final String PATH_FIND = "path_find";

  /**
   * Constant for the <a href="https://xrpl.org/ripple_path_find.html">ripple_path_find</a> rippled API method.
   */
  public static final String RIPPLE_PATH_FIND = "ripple_path_find";

  // Payment Channel methods
  /**
   * Constant for the <a href="https://xrpl.org/channel_authorize.html">channel_authorize</a> rippled API method.
   */
  public static final String CHANNEL_AUTHORIZE = "channel_authorize";

  /**
   * Constant for the <a href="https://xrpl.org/channel_verify.html">channel_verify</a> rippled API method.
   */
  public static final String CHANNEL_VERIFY = "channel_verify";

  // Subscription methods
  /**
   * Constant for the <a href="https://xrpl.org/subscribe.html">subscribe</a> rippled API method.
   */
  public static final String SUBSCRIBE = "subscribe";

  /**
   * Constant for the <a href="https://xrpl.org/unsubscribe.html">unsubscribe</a> rippled API method.
   */
  public static final String UNSUBSCRIBE = "unsubscribe";

  // Server info methods
  /**
   * Constant for the <a href="https://xrpl.org/fee.html">fee</a> rippled API method.
   */
  public static final String FEE = "fee";

  /**
   * Constant for the <a href="https://xrpl.org/server_info.html">server_info</a> rippled API method.
   */
  public static final String SERVER_INFO = "server_info";

  /**
   * Constant for the <a href="https://xrpl.org/server_state.html">server_state</a> rippled API method.
   */
  public static final String SERVER_STATE = "server_state";

  // Utility methods
  /**
   * Constant for the <a href="https://xrpl.org/json.html">json</a> rippled API method.
   */
  public static final String JSON = "json";

  /**
   * Constant for the <a href="https://xrpl.org/ping.html">ping</a> rippled API method.
   */
  public static final String PING = "ping";

}
