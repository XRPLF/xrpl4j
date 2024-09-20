package org.xrpl.xrpl4j.model.client.oracle;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.oracle.ImmutableGetAggregatePriceResult.Builder;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.math.BigDecimal;
import java.math.BigInteger;

class GetAggregatePriceResultTest extends AbstractJsonTest {

  @Test
  void testJson() throws JSONException, JsonProcessingException {
    GetAggregatePriceResult result = baseBuilder()
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(25)))
      .build();

    String json = "{\n" +
      "  \"entire_set\" : {\n" +
      "    \"mean\" : \"74.75\",\n" +
      "    \"size\" : 10,\n" +
      "    \"standard_deviation\" : \"0.1290994448735806\"\n" +
      "  },\n" +
      "  \"ledger_current_index\" : 25,\n" +
      "  \"median\" : \"74.75\",\n" +
      "  \"status\" : \"success\",\n" +
      "  \"trimmed_set\" : {\n" +
      "    \"mean\" : \"74.75\",\n" +
      "    \"size\" : 6,\n" +
      "    \"standard_deviation\" : \"0.1290994448735806\"\n" +
      "  },\n" +
      "  \"validated\" : false,\n" +
      "  \"time\" : 78937648\n" +
      "}";

    assertCanSerializeAndDeserialize(result, json);
  }

  @Test
  void testWithLedgerCurrentIndex() {
    GetAggregatePriceResult result = baseBuilder()
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(25)))
      .build();

    assertThat(result.ledgerCurrentIndex()).isNotEmpty().get().isEqualTo(result.ledgerCurrentIndexSafe());
    assertThatThrownBy(result::ledgerIndexSafe).isInstanceOf(IllegalStateException.class);
    assertThatThrownBy(result::ledgerHashSafe).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void testWithLedgerIndex() {
    GetAggregatePriceResult result = baseBuilder()
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(25)))
      .build();

    assertThat(result.ledgerIndex()).isNotEmpty().get().isEqualTo(result.ledgerIndexSafe());
    assertThatThrownBy(result::ledgerCurrentIndexSafe).isInstanceOf(IllegalStateException.class);
    assertThatThrownBy(result::ledgerHashSafe).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void testWithLedgerHash() {
    GetAggregatePriceResult result = baseBuilder()
      .ledgerHash(Hash256.of("B9D3D80EDF4083A06B2D51202E0BFB63C46FC0985E015D06767C21A62853BF6D"))
      .build();

    assertThat(result.ledgerHash()).isNotEmpty().get().isEqualTo(result.ledgerHashSafe());
    assertThatThrownBy(result::ledgerCurrentIndexSafe).isInstanceOf(IllegalStateException.class);
    assertThatThrownBy(result::ledgerIndexSafe).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void testMedian() {
    GetAggregatePriceResult result = baseBuilder().build();
    assertThat(result.median()).isEqualTo(BigDecimal.valueOf(74.75));
  }

  private static Builder baseBuilder() {
    return GetAggregatePriceResult.builder()
      .entireSet(
        AggregatePriceSet.builder()
          .meanString("74.75")
          .size(UnsignedLong.valueOf(10))
          .standardDeviationString("0.1290994448735806")
          .build()
      )
      .medianString("74.75")
      .status("success")
      .trimmedSet(
        AggregatePriceSet.builder()
          .meanString("74.75")
          .size(UnsignedLong.valueOf(6))
          .standardDeviationString("0.1290994448735806")
          .build()
      )
      .time(UnsignedInteger.valueOf(78937648));
  }
}