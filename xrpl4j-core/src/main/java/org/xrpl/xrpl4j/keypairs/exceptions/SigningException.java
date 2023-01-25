package org.xrpl.xrpl4j.keypairs.exceptions;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: keypairs
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

/**
 * A {@link RuntimeException} thrown when an error occurs during signing.
 *
 * @deprecated use the version from xrpl4j-signing instead.
 */
@Deprecated
public class SigningException extends RuntimeException {

  /**
   * No-arg constructor.
   */
  public SigningException() {
    super();
  }

  /**
   * Required-args constructor.
   *
   * @param message A detail message {@link String}.
   */
  public SigningException(String message) {
    super(message);
  }

  /**
   * Required-args constructor.
   *
   * @param message A detail message {@link String}.
   * @param cause   The {@link Throwable} that caused this exception.
   */
  public SigningException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Required-args constructor.
   *
   * @param cause The {@link Throwable} that caused this exception.
   */
  public SigningException(Throwable cause) {
    super(cause);
  }
}
