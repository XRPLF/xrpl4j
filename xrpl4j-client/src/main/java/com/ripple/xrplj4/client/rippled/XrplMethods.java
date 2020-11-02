package com.ripple.xrplj4.client.rippled;

/**
 * A definition class for all rippled method name constants.
 */
public class XrplMethods {

  // Account methods
  public static final String ACCOUNT_CHANNELS = "account_channels";
  public static final String ACCOUNT_CURRENCIES = "account_currencies";
  public static final String ACCOUNT_INFO = "account_info";
  public static final String ACCOUNT_LINES = "account_lines";
  public static final String ACCOUNT_OBJECTS = "account_objects";
  public static final String ACCOUNT_OFFERS = "account_offers";
  public static final String ACCOUNT_TX = "account_tx";
  public static final String GATEWAY_BALANCES = "gateway_balances";
  public static final String NORIPPLE_CHECK = "noripple_check";

  // Ledger methods
  public static final String LEDGER = "ledger";
  public static final String LEDGER_CLOSED = "ledger_closed";
  public static final String LEDGER_CURRENT = "ledger_current";
  public static final String LEDGER_DATA = "ledger_data";
  public static final String LEDGER_ENTRY = "ledger_entry";

  // Transaction methods
  public static final String SIGN = "sign";
  public static final String SIGN_FOR = "sign_for";
  public static final String SUBMIT = "submit";
  public static final String SUBMIT_MULTISIGNED = "submit_multisigned";
  public static final String TRANSACTION_ENTRY = "transaction_entry";
  public static final String TX = "tx";
  public static final String TX_HISTORY = "tx_history";

  // Path and Order Book methods
  public static final String BOOK_OFFERS = "book_offers";
  public static final String DEPOSIT_AUTHORIZED = "deposit_authorized";
  public static final String PATH_FIND = "path_find";
  public static final String RIPPLE_PATH_FIND = "ripple_path_find";

  // Payment Channel methods
  public static final String CHANNEL_AUTHORIZE = "channel_authorize";
  public static final String CHANNEL_VERIFY = "channel_verify";

  // Subscription methods
  public static final String SUBSCRIBE = "subscribe";
  public static final String UNSUBSCRIBE = "unsubscribe";

  // Server info methods
  public static final String FEE = "fee";
  public static final String SERVER_INFO = "server_info";
  public static final String SERVER_STATE = "server_state";

  // Utility methods
  public static final String JSON = "json";
  public static final String PING = "ping";

}
