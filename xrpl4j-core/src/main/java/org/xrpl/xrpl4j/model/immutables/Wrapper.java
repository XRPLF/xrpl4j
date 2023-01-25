package org.xrpl.xrpl4j.model.immutables;

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

import org.immutables.value.Value;

/**
 * A class helper for creating type-safe wrappers using Immutables.
 *
 * @see "http://immutables.github.io/immutable.html#wrapper-types"
 */
public abstract class Wrapper<T extends Comparable<T>> implements Comparable<Wrapper<T>> {

  /**
   * The wrapped value.
   *
   * @return The wrapped value.
   */
  @Value.Parameter
  public abstract T value();

  @Override
  public int compareTo(Wrapper<T> otherWrapped) {
    return value().compareTo(otherWrapped.value());
  }

  @Override
  public boolean equals(Object obj) {

    if (obj != null && obj instanceof Wrapper) {
      Object otherValue = ((Wrapper) obj).value();
      if (otherValue != null) {
        return otherValue.equals(value());
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    return value().hashCode();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "(" + value() + ")";
  }
}
