package org.xrpl.xrpl4j.model.transactions;

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

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class AddressTest {

  @Test
  public void addressWithBadPrefix() {

    assertThrows(
      IllegalArgumentException.class,
      () -> Address.of("c9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"),
      "Invalid Address: Bad Prefix"
    );
  }

  @Test
  public void addressOfIncorrectLength() {

    assertThrows(
      IllegalArgumentException.class,
      () -> Address.of("r9cZA1mLK5R"),
      "Classic Addresses must be (25,35) characters long inclusive."
    );
    assertThrows(
      IllegalArgumentException.class,
      () -> Address.of("rAJYB9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"),
      "Classic Addresses must be (25,35) characters long inclusive."
    );
  }
}
