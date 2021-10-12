package org.xrpl.xrpl4j.crypto.signing;

/**
 * Defines the type of signature to perform.
 *
 * @deprecated Prefer the variant found in {@link org.xrpl.xrpl4j.crypto.core} instead.
 */
@Deprecated
public enum SigningBehavior {
  /**
   * Indicates the signature was generated for a multi-signed transaction.
   *
   * @see "https://xrpl.org/sign.html"
   */
  SINGLE,
  /**
   * Indicates the signature was generated for a multi-signed transaction.
   *
   * @see "https://xrpl.org/multi-signing.html"
   */
  MULTI
}
