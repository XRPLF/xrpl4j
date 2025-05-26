package org.xrpl.xrpl4j.model.flags;

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

import org.xrpl.xrpl4j.model.ledger.CredentialObject;

/**
 * A set of static {@link Flags} which can be set on {@link CredentialObject}s.
 */
public class CredentialFlags extends Flags {

  /**
   * Constant {@link CredentialFlags} for the {@code lsfBuyToken} flag.
   */
  public static final CredentialFlags ACCEPTED = new CredentialFlags(0x00010000);


  private CredentialFlags(long value) {
    super(value);
  }

  /**
   * Construct {@link CredentialFlags} with a given value.
   *
   * @param value The long-number encoded flags value of this {@link CredentialFlags}.
   *
   * @return New {@link CredentialFlags}.
   */
  public static CredentialFlags of(long value) {
    return new CredentialFlags(value);
  }

  /**
   * The lsfAccepted flag represents whether the subject of the credential has accepted the credential.
   *
   * @return {@code true} if {@code lsfAccepted} is set, otherwise {@code false}.
   */
  public boolean lsfAccepted() {
    return this.isSet(ACCEPTED);
  }
}
