package com.ripple.xrpl4j.model.transactions.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ripple.xrpl4j.model.jackson.ObjectMapperFactory;
import org.junit.Before;

public class AbstractJsonTest {

  protected ObjectMapper objectMapper;

  @Before
  public void setUp() {
    objectMapper = ObjectMapperFactory.create();
  }
}
