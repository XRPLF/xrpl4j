package org.xrpl.xrpl4j.crypto.core.keys;

import org.xrpl.xrpl4j.codec.addresses.VersionType;

/**
 * The parent interface for any private key in xrpl4j.
 */
public interface PrivateKeyable {

  /**
   * The type of this key.
   *
   * @return A {@link VersionType}.
   */
  VersionType versionType();

}
