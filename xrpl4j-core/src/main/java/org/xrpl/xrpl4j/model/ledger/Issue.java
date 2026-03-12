package org.xrpl.xrpl4j.model.ledger;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.xrpl.xrpl4j.model.jackson.modules.IssueDeserializer;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents an asset on the ledger without an amount. Can be one of:
 * <ul>
 *   <li>{@link XrpIssue} — the native XRP asset</li>
 *   <li>{@link IouIssue} — an issued currency (IOU) with a currency code and issuer</li>
 *   <li>{@link MptIssue} — a multi-purpose token identified by its issuance ID</li>
 * </ul>
 */
@JsonDeserialize(using = IssueDeserializer.class)
public interface Issue {

  /**
   * Constant {@link Issue} representing XRP.
   */
  Issue XRP = XrpIssue.XRP;

  /**
   * Handle this {@link Issue} depending on its actual polymorphic subtype.
   *
   * @param xrpIssueHandler A {@link Consumer} that is called if this instance is of type {@link XrpIssue}.
   * @param iouIssueHandler A {@link Consumer} that is called if this instance is of type {@link IouIssue}.
   * @param mptIssueHandler A {@link Consumer} that is called if this instance is of type {@link MptIssue}.
   */
  default void handle(
    final Consumer<XrpIssue> xrpIssueHandler,
    final Consumer<IouIssue> iouIssueHandler,
    final Consumer<MptIssue> mptIssueHandler
  ) {
    Objects.requireNonNull(xrpIssueHandler);
    Objects.requireNonNull(iouIssueHandler);
    Objects.requireNonNull(mptIssueHandler);

    if (XrpIssue.class.isAssignableFrom(this.getClass())) {
      xrpIssueHandler.accept((XrpIssue) this);
    } else if (IouIssue.class.isAssignableFrom(this.getClass())) {
      iouIssueHandler.accept((IouIssue) this);
    } else if (MptIssue.class.isAssignableFrom(this.getClass())) {
      mptIssueHandler.accept((MptIssue) this);
    } else {
      throw new IllegalStateException(String.format("Unsupported Issue Type: %s", this.getClass()));
    }
  }

  /**
   * Map this {@link Issue} to an instance of {@link R}, depending on its actual polymorphic subtype.
   *
   * @param xrpIssueMapper A {@link Function} that is called if this instance is of type {@link XrpIssue}.
   * @param iouIssueMapper A {@link Function} that is called if this instance is of type {@link IouIssue}.
   * @param mptIssueMapper A {@link Function} that is called if this instance is of type {@link MptIssue}.
   * @param <R>            The type of object to return after mapping.
   *
   * @return A {@link R} that is constructed by the appropriate mapper function.
   */
  default <R> R map(
    final Function<XrpIssue, R> xrpIssueMapper,
    final Function<IouIssue, R> iouIssueMapper,
    final Function<MptIssue, R> mptIssueMapper
  ) {
    Objects.requireNonNull(xrpIssueMapper);
    Objects.requireNonNull(iouIssueMapper);
    Objects.requireNonNull(mptIssueMapper);

    if (XrpIssue.class.isAssignableFrom(this.getClass())) {
      return xrpIssueMapper.apply((XrpIssue) this);
    } else if (IouIssue.class.isAssignableFrom(this.getClass())) {
      return iouIssueMapper.apply((IouIssue) this);
    } else if (MptIssue.class.isAssignableFrom(this.getClass())) {
      return mptIssueMapper.apply((MptIssue) this);
    } else {
      throw new IllegalStateException(String.format("Unsupported Issue Type: %s", this.getClass()));
    }
  }

}
