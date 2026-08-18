package org.xrpl.xrpl4j.crypto.confidential;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
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

import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A single Confidential MPT operation to place inside a Batch, minus the fields the {@link ConfidentialMptBatchAssembler}
 * owns (sequence, fee, inner-batch flag). This is a closed union of five variants — {@link ConfidentialSendOp},
 * {@link ConfidentialConvertOp}, {@link ConfidentialConvertBackOp}, {@link ConfidentialMergeInboxOp}, and
 * {@link ConfidentialClawbackOp}.
 *
 * <p>Following the {@code CurrencyAmount} idiom used elsewhere in this codebase (Java 8 has no sealed types or pattern
 * matching), callers dispatch on the concrete variant via the exhaustive {@link #handle} / {@link #map} callbacks rather
 * than an {@code instanceof} chain of their own.</p>
 */
public interface ConfidentialMptOp {

  /**
   * The account that submits this operation — the account whose sequence the inner consumes. For a clawback this is the
   * issuer.
   *
   * @return The submitting account's {@link Address}.
   */
  Address account();

  /**
   * The MPTokenIssuance this operation acts on.
   *
   * @return An {@link MpTokenIssuanceId}.
   */
  MpTokenIssuanceId mpTokenIssuanceId();

  /**
   * Dispatch to the consumer matching this operation's concrete variant.
   *
   * @param sendHandler        Invoked if this is a {@link ConfidentialSendOp}.
   * @param convertHandler     Invoked if this is a {@link ConfidentialConvertOp}.
   * @param convertBackHandler Invoked if this is a {@link ConfidentialConvertBackOp}.
   * @param mergeInboxHandler  Invoked if this is a {@link ConfidentialMergeInboxOp}.
   * @param clawbackHandler    Invoked if this is a {@link ConfidentialClawbackOp}.
   */
  default void handle(
    final Consumer<ConfidentialSendOp> sendHandler,
    final Consumer<ConfidentialConvertOp> convertHandler,
    final Consumer<ConfidentialConvertBackOp> convertBackHandler,
    final Consumer<ConfidentialMergeInboxOp> mergeInboxHandler,
    final Consumer<ConfidentialClawbackOp> clawbackHandler
  ) {
    if (ConfidentialSendOp.class.isAssignableFrom(this.getClass())) {
      sendHandler.accept((ConfidentialSendOp) this);
    } else if (ConfidentialConvertOp.class.isAssignableFrom(this.getClass())) {
      convertHandler.accept((ConfidentialConvertOp) this);
    } else if (ConfidentialConvertBackOp.class.isAssignableFrom(this.getClass())) {
      convertBackHandler.accept((ConfidentialConvertBackOp) this);
    } else if (ConfidentialMergeInboxOp.class.isAssignableFrom(this.getClass())) {
      mergeInboxHandler.accept((ConfidentialMergeInboxOp) this);
    } else if (ConfidentialClawbackOp.class.isAssignableFrom(this.getClass())) {
      clawbackHandler.accept((ConfidentialClawbackOp) this);
    } else {
      throw new IllegalStateException("Unsupported ConfidentialMptOp type: " + this.getClass());
    }
  }

  /**
   * Map to a value using the function matching this operation's concrete variant.
   *
   * @param sendMapper        Applied if this is a {@link ConfidentialSendOp}.
   * @param convertMapper     Applied if this is a {@link ConfidentialConvertOp}.
   * @param convertBackMapper Applied if this is a {@link ConfidentialConvertBackOp}.
   * @param mergeInboxMapper  Applied if this is a {@link ConfidentialMergeInboxOp}.
   * @param clawbackMapper    Applied if this is a {@link ConfidentialClawbackOp}.
   * @param <R>               The result type.
   *
   * @return The result of the matching function.
   */
  default <R> R map(
    final Function<ConfidentialSendOp, R> sendMapper,
    final Function<ConfidentialConvertOp, R> convertMapper,
    final Function<ConfidentialConvertBackOp, R> convertBackMapper,
    final Function<ConfidentialMergeInboxOp, R> mergeInboxMapper,
    final Function<ConfidentialClawbackOp, R> clawbackMapper
  ) {
    if (ConfidentialSendOp.class.isAssignableFrom(this.getClass())) {
      return sendMapper.apply((ConfidentialSendOp) this);
    } else if (ConfidentialConvertOp.class.isAssignableFrom(this.getClass())) {
      return convertMapper.apply((ConfidentialConvertOp) this);
    } else if (ConfidentialConvertBackOp.class.isAssignableFrom(this.getClass())) {
      return convertBackMapper.apply((ConfidentialConvertBackOp) this);
    } else if (ConfidentialMergeInboxOp.class.isAssignableFrom(this.getClass())) {
      return mergeInboxMapper.apply((ConfidentialMergeInboxOp) this);
    } else if (ConfidentialClawbackOp.class.isAssignableFrom(this.getClass())) {
      return clawbackMapper.apply((ConfidentialClawbackOp) this);
    } else {
      throw new IllegalStateException("Unsupported ConfidentialMptOp type: " + this.getClass());
    }
  }
}
