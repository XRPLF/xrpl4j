package org.xrpl.xrpl4j.codec.binary.types;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.binary.BinaryCodecObjectMapperFactory;
import org.xrpl.xrpl4j.codec.binary.definitions.DefinitionsService;
import org.xrpl.xrpl4j.codec.binary.definitions.FieldInstance;

/**
 * Unit test for PermissionValue field handling in UInt32Type.
 */
public class PermissionValueTest {

  private final UInt32Type codec = new UInt32Type();
  private final ObjectMapper objectMapper = BinaryCodecObjectMapperFactory.getObjectMapper();

  @Test
  void testPermissionValueFromJson() throws Exception {
    // Get the PermissionValue field instance
    FieldInstance permissionValueField = DefinitionsService.getInstance()
      .getFieldInstance("PermissionValue")
      .orElseThrow(() -> new AssertionError("PermissionValue field not found"));

    // Test converting "Payment" string to UInt32
    TextNode paymentNode = new TextNode("Payment");
    UInt32Type result = codec.fromJson(paymentNode, permissionValueField);
    
    // Payment transaction type is 0, so permission value should be 1
    assertThat(result.toHex()).isEqualTo("00000001");
  }

  @Test
  void testPermissionValueToJson() throws Exception {
    // Get the PermissionValue field instance
    FieldInstance permissionValueField = DefinitionsService.getInstance()
      .getFieldInstance("PermissionValue")
      .orElseThrow(() -> new AssertionError("PermissionValue field not found"));

    // Create a UInt32Type with value 1 (Payment permission)
    UInt32Type paymentPermission = codec.fromJson("1");
    
    // Convert back to JSON - should return "Payment" string
    assertThat(paymentPermission.toJson(permissionValueField).asText()).isEqualTo("Payment");
  }

  @Test
  void testGranularPermissionValue() throws Exception {
    // Get the PermissionValue field instance
    FieldInstance permissionValueField = DefinitionsService.getInstance()
      .getFieldInstance("PermissionValue")
      .orElseThrow(() -> new AssertionError("PermissionValue field not found"));

    // Test converting "TrustlineAuthorize" string to UInt32
    TextNode trustlineAuthorizeNode = new TextNode("TrustlineAuthorize");
    UInt32Type result = codec.fromJson(trustlineAuthorizeNode, permissionValueField);

    // TrustlineAuthorize is 65537
    assertThat(result.toHex()).isEqualTo("00010001");
  }

  @Test
  void testMapFieldSpecialization() {
    // Test that the DefinitionsService can map "Payment" to 1
    Integer paymentValue = DefinitionsService.getInstance()
      .mapFieldSpecialization("PermissionValue", "Payment")
      .orElseThrow(() -> new AssertionError("Payment not found in PERMISSION_VALUES"));

    assertThat(paymentValue).isEqualTo(1);

    // Test TrustSet
    Integer trustSetValue = DefinitionsService.getInstance()
      .mapFieldSpecialization("PermissionValue", "TrustSet")
      .orElseThrow(() -> new AssertionError("TrustSet not found in PERMISSION_VALUES"));

    assertThat(trustSetValue).isEqualTo(21); // TrustSet transaction type is 20, so permission is 21
  }
}

