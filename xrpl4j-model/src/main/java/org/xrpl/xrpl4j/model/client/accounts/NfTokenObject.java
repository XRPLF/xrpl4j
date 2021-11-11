package org.xrpl.xrpl4j.model.client.accounts;

//import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
//import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.transactions.NfTokenMint;

import java.util.Optional;

/**
 * Structure of an NFToken stored on the ledger.
 */
@Value.Immutable
//@JsonSerialize(as = ImmutableNfToken.class)
//@JsonDeserialize(as = ImmutableNfToken.class)
public interface NfTokenObject {

  /**
   * The unique TokenID of the token.
   *
   * @return The unique TokenID of the token.
   */
  String tokenId();

  /**
   * The URI for the data of the token.
   *
   * @return The URI for the data of the token.
   */
  Optional<String> uri();
}
