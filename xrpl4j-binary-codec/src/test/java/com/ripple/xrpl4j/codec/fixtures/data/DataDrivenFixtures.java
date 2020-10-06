package com.ripple.xrpl4j.codec.fixtures.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableDataDrivenFixtures.class)
@JsonDeserialize(as = ImmutableDataDrivenFixtures.class)
public interface DataDrivenFixtures {

  @JsonProperty("fields_tests")
  List<FieldTest> fieldTests();

  @JsonProperty("values_tests")
  List<ValueTest> valuesTests();

  @JsonProperty("whole_objects")
  List<WholeObject> wholeObjectTests();

}
