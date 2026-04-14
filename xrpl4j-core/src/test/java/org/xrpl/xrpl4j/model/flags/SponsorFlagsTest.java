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

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

public class SponsorFlagsTest extends AbstractFlagsTest {

  @Test
  void testUnsetFlags() {
    SponsorFlags flags = SponsorFlags.UNSET;

    assertThat(flags.spfSponsorFee()).isFalse();
    assertThat(flags.spfSponsorReserve()).isFalse();
    assertThat(flags.isValid()).isFalse();
    assertThat(flags.getValue()).isEqualTo(0L);
  }

  @Test
  void testSponsorFeeFlag() {
    SponsorFlags flags = SponsorFlags.SPONSOR_FEE;
    assertThat(flags.isEmpty()).isFalse();

    assertThat(flags.spfSponsorFee()).isTrue();
    assertThat(flags.spfSponsorReserve()).isFalse();
    assertThat(flags.isValid()).isTrue();
    assertThat(flags.getValue()).isEqualTo(1L);
  }

  @Test
  void testSponsorReserveFlag() {
    SponsorFlags flags = SponsorFlags.SPONSOR_RESERVE;
    assertThat(flags.isEmpty()).isFalse();

    assertThat(flags.spfSponsorFee()).isFalse();
    assertThat(flags.spfSponsorReserve()).isTrue();
    assertThat(flags.isValid()).isTrue();
    assertThat(flags.getValue()).isEqualTo(2L);
  }

  @Test
  void testOfWithCombinedFlags() {
    SponsorFlags flags = SponsorFlags.of(0x00000001L | 0x00000002L);
    assertThat(flags.spfSponsorFee()).isTrue();
    assertThat(flags.spfSponsorReserve()).isTrue();
    assertThat(flags.isValid()).isTrue();
    assertThat(flags.getValue()).isEqualTo(3L);
  }

  @Test
  void testOfWithZero() {
    SponsorFlags flags = SponsorFlags.of(0);
    assertThat(flags.spfSponsorFee()).isFalse();
    assertThat(flags.spfSponsorReserve()).isFalse();
    assertThat(flags.isValid()).isFalse();
    assertThat(flags.getValue()).isEqualTo(0L);
  }

  @Test
  void testSponsorFeeJson() throws JSONException, JsonProcessingException {
    FlagsWrapper wrapper = FlagsWrapper.of(SponsorFlags.SPONSOR_FEE);
    String json = String.format("{" +
      "  \"flags\": %s" +
      "}", SponsorFlags.SPONSOR_FEE.getValue());

    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testSponsorReserveJson() throws JSONException, JsonProcessingException {
    FlagsWrapper wrapper = FlagsWrapper.of(SponsorFlags.SPONSOR_RESERVE);
    String json = String.format("{" +
      "  \"flags\": %s" +
      "}", SponsorFlags.SPONSOR_RESERVE.getValue());

    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testUnsetJson() throws JSONException, JsonProcessingException {
    FlagsWrapper wrapper = FlagsWrapper.of(SponsorFlags.UNSET);
    String json = String.format("{" +
      "  \"flags\": %s" +
      "}", SponsorFlags.UNSET.getValue());

    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testCombinedFlagsJson() throws JSONException, JsonProcessingException {
    SponsorFlags flags = SponsorFlags.of(0x00000001L | 0x00000002L);
    FlagsWrapper wrapper = FlagsWrapper.of(flags);
    String json = String.format("{" +
      "  \"flags\": %s" +
      "}", flags.getValue());

    assertCanSerializeAndDeserialize(wrapper, json);
  }

}
