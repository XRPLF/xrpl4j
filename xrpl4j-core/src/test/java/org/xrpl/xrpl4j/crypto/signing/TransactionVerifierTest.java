package org.xrpl.xrpl4j.crypto.signing;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.Signer;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Unit test for {@link TransactionVerifier}.
 */
class TransactionVerifierTest {

  private AtomicBoolean verify1Called;
  private AtomicBoolean verify2Called;

  private TransactionVerifier transactionVerifier;

  @BeforeEach
  void setUp() {
    this.verify1Called = new AtomicBoolean();
    this.verify2Called = new AtomicBoolean();

    transactionVerifier = new TransactionVerifier() {
      @Override
      public <T extends Transaction> boolean verify(
        Signer signer, T unsignedTransaction
      ) {
        verify1Called.set(true);
        return true;
      }

      @Override
      public <T extends Transaction> boolean verifyMultiSigned(
        Set<Signer> signerSet, T unsignedTransaction, int minSigners
      ) {
        verify2Called.set(true);
        return true;
      }
    };
  }

  @Test
  void verify() {
    transactionVerifier.verifyMultiSigned(mock(Set.class), mock(Payment.class));
    assertThat(verify1Called.get()).isFalse();
    assertThat(verify2Called.get()).isTrue();
  }

}
