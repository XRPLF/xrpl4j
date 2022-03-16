package org.xrpl.xrpl4j.model.transactions;

/**
 * The rippled server summarizes transaction results with result codes, which appear
 * in fields such as engine_result and meta.TransactionResult.
 */
public class TransactionResultCode {

  // tec codes
  public static String TEC = "tec";
  public static String TEC_CLAIM = TEC + "CLAIM";
  public static String TEC_CRYPTOCONDITION_ERROR = TEC + "CRYPTOCONDITION_ERROR";
  public static String TEC_DIR_FULL = TEC + "DIR_FULL";
  public static String TEC_DUPLICATE = TEC + "DUPLICATE";
  public static String TEC_DST_TAG_NEEDED = TEC + "DST_TAG_NEEDED";
  public static String TEC_EXPIRED = TEC + "EXPIRED";
  public static String TEC_FAILED_PROCESSING = TEC + "FAILED_PROCESSING";
  public static String TEC_FROZEN = TEC + "FROZEN";
  public static String TEC_HAS_OBLIGATIONS = TEC + "HAS_OBLIGATIONS";
  public static String TEC_INSUF_RESERVE_LINE = TEC + "INSUF_RESERVE_LINE";
  public static String TEC_INSUF_RESERVE_OFFER = TEC + "INSUF_RESERVE_OFFER";
  public static String TEC_INSUFF_FEE = TEC + "INSUFF_FEE";
  public static String TEC_INSUFFICIENT_RESERVE = TEC + "INSUFFICIENT_RESERVE";
  public static String TEC_INTERNAL = TEC + "INTERNAL";
  public static String TEC_INVARIANT_FAILED = TEC + "INVARIANT_FAILED";
  public static String TEC_KILLED = TEC + "KILLED";
  public static String TEC_NEED_MASTER_KEY = TEC + "NEED_MASTER_KEY";
  public static String TEC_NO_ALTERNATIVE_KEY = TEC + "NO_ALTERNATIVE_KEY";
  public static String TEC_NO_AUTH = TEC + "NO_AUTH";
  public static String TEC_NO_DST = TEC + "NO_DST";
  public static String TEC_NO_DST_INSUF_XRP = TEC + "NO_DST_INSUF_XRP";
  public static String TEC_NO_ENTRY = TEC + "NO_ENTRY";
  public static String TEC_NO_ISSUER = TEC + "NO_ISSUER";
  public static String TEC_NO_LINE = TEC + "NO_LINE";
  public static String TEC_NO_LINE_INSUF_RESERVE = TEC + "NO_LINE_INSUF_RESERVE";
  public static String TEC_NO_LINE_REDUNDANT = TEC + "NO_LINE_REDUNDANT";
  public static String TEC_NO_PERMISSION = TEC + "NO_PERMISSION";
  public static String TEC_NO_REGULAR_KEY = TEC + "NO_REGULAR_KEY";
  public static String TEC_NO_TARGET = TEC + "NO_TARGET";
  public static String TEC_OVERSIZE = TEC + "OVERSIZE";
  public static String TEC_OWNERS = TEC + "OWNERS";
  public static String TEC_PATH_DRY = TEC + "PATH_DRY";
  public static String TEC_PATH_PARTIAL = TEC + "PATH_PARTIAL";
  public static String TEC_TOO_SOON = TEC + "TOO_SOON";
  public static String TEC_UNFUNDED = TEC + "UNFUNDED";
  public static String TEC_UNFUNDED_ADD = TEC + "UNFUNDED_ADD";
  public static String TEC_UNFUNDED_PAYMENT = TEC + "UNFUNDED_PAYMENT";
  public static String TEC_UNFUNDED_OFFER = TEC + "UNFUNDED_OFFER";

  // tef codes
  public static String TEF = "tef";
  public static String TEF_ALREADY = TEF + "ALREADY";
  public static String TEF_BAD_ADD_AUTH = TEF + "BAD_ADD_AUTH";
  public static String TEF_BAD_AUTH = TEF + "BAD_AUTH";
  public static String TEF_BAD_AUTH_MASTER = TEF + "BAD_AUTH_MASTER";
  public static String TEF_BAD_LEDGER = TEF + "BAD_LEDGER";
  public static String TEF_BAD_QUORUM = TEF + "BAD_QUORUM";
  public static String TEF_BAD_SIGNATURE = TEF + "BAD_SIGNATURE";
  public static String TEF_CREATED = TEF + "CREATED";
  public static String TEF_EXCEPTION = TEF + "EXCEPTION";
  public static String TEF_FAILURE = TEF + "FAILURE";
  public static String TEF_INTERNAL = TEF + "INTERNAL";
  public static String TEF_INVARIANT_FAILED = TEF + "INVARIANT_FAILED";
  public static String TEF_MASTER_DISABLED = TEF + "MASTER_DISABLED";
  public static String TEF_MAX_LEDGER = TEF + "MAX_LEDGER";
  public static String TEF_NO_AUTH_REQUIRED = TEF + "NO_AUTH_REQUIRED";
  public static String TEF_NO_TICKET = TEF + "NO_TICKET";
  public static String TEF_NOT_MULTI_SIGNING = TEF + "NOT_MULTI_SIGNING";
  public static String TEF_PAST_SEQ = TEF + "PAST_SEQ";
  public static String TEF_TOO_BIG = TEF + "TOO_BIG";
  public static String TEF_WRONG_PRIOR = TEF + "WRONG_PRIOR";

  // tel codes
  public static String TEL = "tel";
  public static String TEL_BAD_DOMAIN = TEF + "BAD_DOMAIN";
  public static String TEL_BAD_PATH_COUNT = TEF + "BAD_PATH_COUNT";
  public static String TEL_BAD_PUBLIC_KEY = TEF + "BAD_PUBLIC_KEY";
  public static String TEL_CAN_NOT_QUEUE = TEF + "CAN_NOT_QUEUE";
  public static String TEL_CAN_NOT_QUEUE_BALANCE = TEF + "CAN_NOT_QUEUE_BALANCE";
  public static String TEL_CAN_NOT_QUEUE_BLOCKS = TEF + "CAN_NOT_QUEUE_BLOCKS";
  public static String TEL_CAN_NOT_QUEUE_BLOCKED = TEF + "CAN_NOT_QUEUE_BLOCKED";
  public static String TEL_CAN_NOT_QUEUE_FEE = TEF + "CAN_NOT_QUEUE_FEE";
  public static String TEL_CAN_NOT_QUEUE_FULL = TEF + "CAN_NOT_QUEUE_FULL";
  public static String TEL_FAILED_PROCESSING = TEF + "FAILED_PROCESSING";
  public static String TEL_INSUF_FEE_P = TEF + "INSUF_FEE_P";
  public static String TEL_LOCAL_ERROR = TEF + "LOCAL_ERROR";
  public static String TEL_NO_DST_PARTIAL = TEF + "NO_DST_PARTIAL";

  //tem codes
  public static String TEM = "tem";
  public static String TEM_BAD_AMOUNT = TEM + "BAD_AMOUNT";
  public static String TEM_BAD_AUTH_MASTER = TEM + "BAD_AUTH_MASTER";
  public static String TEM_BAD_CURRENCY = TEM + "BAD_CURRENCY";
  public static String TEM_BAD_EXPIRATION = TEM + "BAD_EXPIRATION";
  public static String TEM_BAD_FEE = TEM + "BAD_FEE";
  public static String TEM_BAD_ISSUER = TEM + "BAD_ISSUER";
  public static String TEM_BAD_LIMIT = TEM + "BAD_LIMIT";
  public static String TEM_BAD_OFFER = TEM + "BAD_OFFER";
  public static String TEM_BAD_PATH = TEM + "BAD_PATH";
  public static String TEM_BAD_PATH_LOOP = TEM + "BAD_PATH_LOOP";
  public static String TEM_BAD_SEND_XRP_LIMIT = TEM + "BAD_SEND_XRP_LIMIT";
  public static String TEM_BAD_SEND_XRP_MAX = TEM + "BAD_SEND_XRP_MAX";
  public static String TEM_BAD_SEND_XRP_NO_DIRECT = TEM + "BAD_SEND_XRP_NO_DIRECT";
  public static String TEM_BAD_SEND_XRP_PARTIAL = TEM + "BAD_SEND_XRP_PARTIAL";
  public static String TEM_BAD_SEND_XRP_PATHS = TEM + "BAD_SEND_XRP_PATHS";
  public static String TEM_BAD_SEQUENCE = TEM + "BAD_SEQUENCE";
  public static String TEM_BAD_SIGNATURE = TEM + "BAD_SIGNATURE";
  public static String TEM_BAD_SRC_ACCOUNT = TEM + "BAD_SRC_ACCOUNT";
  public static String TEM_BAD_TRANSFER_RATE = TEM + "BAD_TRANSFER_RATE";
  public static String TEM_CANNOT_PREAUTH_SELF = TEM + "CANNOT_PREAUTH_SELF";
  public static String TEM_DST_IS_SRC = TEM + "DST_IS_SRC";
  public static String TEM_DST_NEEDED = TEM + "DST_NEEDED";
  public static String TEM_INVALID = TEM + "INVALID";
  public static String TEM_INVALID_COUNT = TEM + "INVALID_COUNT";
  public static String TEM_INVALID_FLAG = TEM + "INVALID_FLAG";
  public static String TEM_MALFORMED = TEM + "MALFORMED";
  public static String TEM_REDUNDANT = TEM + "REDUNDANT";
  public static String TEM_REDUNDANT_SEND_MAX = TEM + "REDUNDANT_SEND_MAX";
  public static String TEM_RIPPLE_EMPTY = TEM + "RIPPLE_EMPTY";
  public static String TEM_BAD_WEIGHT = TEM + "BAD_WEIGHT";
  public static String TEM_BAD_SIGNER = TEM + "BAD_SIGNER";
  public static String TEM_BAD_QUORUM = TEM + "BAD_QUORUM";
  public static String TEM_UNCERTAIN = TEM + "UNCERTAIN";
  public static String TEM_UNKNOWN = TEM + "UNKNOWN";
  public static String TEM_DISABLED = TEM + "DISABLED";

  // ter codes
  public static String TER = "ter";
  @Deprecated
  public static String TER_FUNDS_SPENT = TER + "FUNDS_SPENT";
  public static String TER_INSUF_FEE_B = TER + "INSUF_FEE_B";
  public static String TER_LAST = TER + "LAST";
  public static String TER_NO_ACCOUNT = TER + "NO_ACCOUNT";
  public static String TER_NO_AUTH = TER + "NO_AUTH";
  public static String TER_NO_LINE = TER + "NO_LINE";
  public static String TER_NO_RIPPLE = TER + "NO_RIPPLE";
  public static String TER_OWNERS = TER + "OWNERS";
  public static String TER_PRE_SEQ = TER + "PRE_SEQ";
  public static String TER_PRE_TICKET = TER + "PRE_TICKET";
  public static String TER_RETRY = TER + "RETRY";
  public static String TER_QUEUED = TER + "QUEUED";

  // tes codes
  public static String TES = "tes";
  public static String TES_SUCCESS = TES + "SUCCESS";
}