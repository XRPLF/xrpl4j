package org.xrpl.xrpl4j.crypto.core;

import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.Arrays;
import java.util.Objects;

/**
 * Helper class to assemble hash values for a given transaction.
 */
public class TransactionHashUtils {

  /**
   * The hash prefix used by the XRPL to identify transaction hashes.
   */
  private static final String SIGNED_TRANSACTION_HASH_PREFIX = "54584E00";

  public static <T extends Transaction> Hash256 computeHash(final UnsignedByteArray signedTransactionBytes) {
    Objects.requireNonNull(signedTransactionBytes);

    byte[] hashBytes = Arrays.copyOfRange(
      Hashing.sha512().hashBytes(
        BaseEncoding.base16().decode(
          SIGNED_TRANSACTION_HASH_PREFIX.concat(signedTransactionBytes.hexValue()).toUpperCase()
        )).asBytes(),
      0,
      32 // <-- SHA512 Half is the first 32 bytes of the SHA512 hash.
    );
    return Hash256.of(BaseEncoding.base16().encode(hashBytes));
  }
}
