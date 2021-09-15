package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.transactions.Hash256;

/**
 * Represents a {@link HookObject} created as a result of setting a Hook.
 */
@Immutable
@JsonSerialize(as = ImmutableHookObject.class)
@JsonDeserialize(as = ImmutableHookObject.class)
public interface HookObject extends LedgerObject {


  static ImmutableHookObject.Builder builder() {
    return ImmutableHookObject.builder();
  }

  /**
   * Hex representation of the Hook WASM code.
   * @return hex representation.
   */
  @JsonProperty("CreateCode")
  String createCode();

  /**
   * The type of ledger object. In this case, this is always "Hook".
   * @return "Hook"
   */
  @JsonProperty("LedgerEntryType")
  @Value.Derived
  default LedgerEntryType ledgerEntryType() {
    return LedgerEntryType.HOOK;
  }

  /**
   * A set of boolean {@link Flags.RippleStateFlags} containing options
   * enabled for this object.
   * @return state flags.
   */
  @JsonProperty("Flags")
  Flags.RippleStateFlags flags();

  /**
   * The identifying hash of the transaction that most recently modified this object.
   * @return previous transaction hash.
   */
  @JsonProperty("PreviousTxnID")
  Hash256 previousTransactionId();

  /**
   * The index of the ledger that contains the transaction that most recently modified this object.
   * @return ledger sequence.
   */
  @JsonProperty("PreviousTxnLgrSeq")
  UnsignedInteger previousTransactionLedgerSequence();

  /**
   * Unique identifier for this {@link HookObject}.
   * @return index for hook.
   */
  Hash256 index();
}
