package org.xrpl.xrpl4j.crypto.mpt.port.bc;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.mpt.port.PedersenCommitmentPort;

import java.io.InputStream;

/**
 * Test for {@link BcPedersenCommitmentPort} comparing output with C implementation.
 */
class BcPedersenCommitmentPortTest {

  private static JsonNode testVectors;
  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static PedersenCommitmentPort commitmentPort;

  @BeforeAll
  static void loadTestVectors() throws Exception {
    InputStream is = BcPedersenCommitmentPortTest.class.getResourceAsStream(
      "/mpt/port/pedersen_commitment_vectors.json"
    );
    testVectors = objectMapper.readTree(is);
    commitmentPort = new BcPedersenCommitmentPort();
  }

  @Test
  void commitmentMatchesCImplementation() {
    for (JsonNode vector : testVectors.get("vectors")) {
      UnsignedLong amount = UnsignedLong.valueOf(vector.get("amount").asText());
      String rhoHex = vector.get("rho").asText();
      String expectedCommitmentHex = vector.get("expectedCommitment").asText();

      UnsignedByteArray rho = UnsignedByteArray.fromHex(rhoHex);

      // Generate commitment
      UnsignedByteArray commitment = commitmentPort.generateCommitment(amount, rho);

      // Assert commitment matches C output byte-for-byte
      assertThat(commitment.hexValue())
        .as("Commitment for amount=%s, rho=%s", amount, rhoHex)
        .isEqualToIgnoringCase(expectedCommitmentHex);
    }
  }

  @Test
  void changingOneRhoBitProducesDifferentCommitment() {
    for (JsonNode vector : testVectors.get("vectors")) {
      UnsignedLong amount = UnsignedLong.valueOf(vector.get("amount").asText());
      String rhoHex = vector.get("rho").asText();
      String expectedCommitmentHex = vector.get("expectedCommitment").asText();

      UnsignedByteArray rho = UnsignedByteArray.fromHex(rhoHex);

      // Generate original commitment
      UnsignedByteArray originalCommitment = commitmentPort.generateCommitment(amount, rho);
      assertThat(originalCommitment.hexValue()).isEqualToIgnoringCase(expectedCommitmentHex);

      // Flip one bit in rho (change last byte)
      byte[] modifiedRhoBytes = rho.toByteArray();
      modifiedRhoBytes[31] ^= 0x01; // Flip least significant bit
      UnsignedByteArray modifiedRho = UnsignedByteArray.of(modifiedRhoBytes);

      // Generate commitment with modified rho
      UnsignedByteArray modifiedCommitment = commitmentPort.generateCommitment(amount, modifiedRho);

      // Commitments should be different
      assertThat(modifiedCommitment.hexValue())
        .as("Commitment should change when rho changes for amount=%s", amount)
        .isNotEqualToIgnoringCase(originalCommitment.hexValue());
    }
  }

  @Test
  void changingAmountByOneProducesDifferentCommitment() {
    for (JsonNode vector : testVectors.get("vectors")) {
      UnsignedLong amount = UnsignedLong.valueOf(vector.get("amount").asText());
      String rhoHex = vector.get("rho").asText();
      String expectedCommitmentHex = vector.get("expectedCommitment").asText();

      UnsignedByteArray rho = UnsignedByteArray.fromHex(rhoHex);

      // Generate original commitment
      UnsignedByteArray originalCommitment = commitmentPort.generateCommitment(amount, rho);
      assertThat(originalCommitment.hexValue()).isEqualToIgnoringCase(expectedCommitmentHex);

      // Change amount by 1
      UnsignedLong modifiedAmount = amount.plus(UnsignedLong.ONE);

      // Generate commitment with modified amount
      UnsignedByteArray modifiedCommitment = commitmentPort.generateCommitment(modifiedAmount, rho);

      // Commitments should be different
      assertThat(modifiedCommitment.hexValue())
        .as("Commitment should change when amount changes from %s to %s", amount, modifiedAmount)
        .isNotEqualToIgnoringCase(originalCommitment.hexValue());
    }
  }

  @Test
  void invalidRhoThrowsException() {
    // Zero scalar is invalid - test for all amounts
    for (JsonNode vector : testVectors.get("vectors")) {
      UnsignedLong amount = UnsignedLong.valueOf(vector.get("amount").asText());
      UnsignedByteArray zeroRho = UnsignedByteArray.of(new byte[32]);

      assertThatThrownBy(() -> commitmentPort.generateCommitment(amount, zeroRho))
        .as("Zero rho should throw for amount=%s", amount)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("not a valid scalar");
    }
  }
}

