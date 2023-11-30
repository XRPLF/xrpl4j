package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.xrpl.xrpl4j.model.transactions.CurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Top level interface for all types of transaction metadata
 * <a href="https://xrpl.org/transaction-metadata.html#affectednodes">AffectedNodes</a>.
 */
public interface AffectedNode {

  /**
   * Handle this {@link AffectedNode} depending on its actual polymorphic subtype.
   *
   * @param createdNodeConsumer  A {@link Consumer} that is called if this instance is of type {@link CreatedNode}.
   * @param modifiedNodeConsumer A {@link Consumer} that is called if this instance is of type {@link ModifiedNode}.
   * @param deletedNodeConsumer  A {@link Consumer} that is called if this instance is of type {@link DeletedNode}.
   * @param <T>                  An instance that extends {@link MetaLedgerObject}.
   */
  default <T extends MetaLedgerObject> void handle(
    final Consumer<CreatedNode<T>> createdNodeConsumer,
    final Consumer<ModifiedNode<T>> modifiedNodeConsumer,
    final Consumer<DeletedNode<T>> deletedNodeConsumer
  ) {
    Objects.requireNonNull(createdNodeConsumer);
    Objects.requireNonNull(modifiedNodeConsumer);
    Objects.requireNonNull(deletedNodeConsumer);

    if (CreatedNode.class.isAssignableFrom(this.getClass())) {
      createdNodeConsumer.accept((CreatedNode<T>) this);
    } else if (ModifiedNode.class.isAssignableFrom(this.getClass())) {
      modifiedNodeConsumer.accept((ModifiedNode<T>) this);
    } else if (DeletedNode.class.isAssignableFrom(this.getClass())) {
      deletedNodeConsumer.accept((DeletedNode<T>) this);
    } else {
      throw new IllegalStateException(String.format("Unsupported AffectedNode type: %s", this.getClass()));
    }
  }

  /**
   * Map this {@link AffectedNode} to an instance of {@link R}, depending on its actual polymorphic subtype.
   *
   * @param createdNodeMapper  A {@link Function} that is called if this instance is of type {@link CreatedNode}.
   * @param modifiedNodeMapper A {@link Function} that is called if this instance is  of type {@link ModifiedNode}.
   * @param deletedNodeMapper  A {@link Function} that is called if this instance is  of type {@link DeletedNode}.
   * @param <R>                The type of object to return after mapping.
   * @param <T>                An instance that extends {@link MetaLedgerObject}.
   *
   * @return An {@link R} that is constructed by the appropriate mapper function.
   */
  default <T extends MetaLedgerObject, R> R map(
    final Function<CreatedNode<T>, R> createdNodeMapper,
    final Function<ModifiedNode<T>, R> modifiedNodeMapper,
    final Function<DeletedNode<T>, R> deletedNodeMapper
  ) {
    Objects.requireNonNull(createdNodeMapper);
    Objects.requireNonNull(modifiedNodeMapper);
    Objects.requireNonNull(deletedNodeMapper);

    if (CreatedNode.class.isAssignableFrom(this.getClass())) {
      return createdNodeMapper.apply((CreatedNode<T>) this);
    } else if (ModifiedNode.class.isAssignableFrom(this.getClass())) {
      return modifiedNodeMapper.apply((ModifiedNode<T>) this);
    } else if (DeletedNode.class.isAssignableFrom(this.getClass())) {
      return deletedNodeMapper.apply((DeletedNode<T>) this);
    } else {
      throw new IllegalStateException(String.format("Unsupported AffectedNode type: %s", this.getClass()));
    }
  }

  /**
   * The type of ledger entry this {@link AffectedNode} affected.
   *
   * @return A {@link MetaLedgerEntryType}.
   */
  @JsonProperty("LedgerEntryType")
  MetaLedgerEntryType ledgerEntryType();

  /**
   * The unique ID of the ledger object that we affected.
   *
   * @return A {@link Hash256} containing the ledger entry's ID.
   */
  @JsonProperty("LedgerIndex")
  Hash256 ledgerIndex();

}
