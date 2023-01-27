package org.xrpl.xrpl4j.crypto.keys;

/**
 * Defines a reference to a private key that lives in an external system.
 */
public interface PrivateKeyReference extends PrivateKeyable {

  /**
   * The unique identifier for the private-key.
   *
   * @return A {@link String}.
   */
  String keyIdentifier();
}
