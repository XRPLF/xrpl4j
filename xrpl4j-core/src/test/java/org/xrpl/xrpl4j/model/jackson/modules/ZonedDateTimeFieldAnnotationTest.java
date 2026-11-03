package org.xrpl.xrpl4j.model.jackson.modules;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.reflect.ClassPath;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Guards against a JSON-bound {@link ZonedDateTime} accessor being added to the model without the xrpld-specific
 * serializer and deserializer. {@link Xrpl4jModule} deliberately does not register those for {@link ZonedDateTime}
 * globally, because doing so changes date handling for every type in any mapper the module is added to. Each xrpld
 * date field must opt in with {@link JsonSerialize} and {@link JsonDeserialize} instead.
 */
class ZonedDateTimeFieldAnnotationTest {

  private static final String MODEL_PACKAGE = "org.xrpl.xrpl4j.model";

  @Test
  void everyJsonBoundZonedDateTimeAccessorUsesXrpldSerializers() throws IOException {
    List<String> violations = new ArrayList<>();

    for (ClassPath.ClassInfo classInfo : ClassPath.from(getClass().getClassLoader())
      .getTopLevelClassesRecursive(MODEL_PACKAGE)) {
      if (classInfo.url().toString().contains("test-classes")) {
        continue;
      }
      for (Method method : classInfo.load().getDeclaredMethods()) {
        if (!isJsonBoundZonedDateTimeAccessor(method)) {
          continue;
        }
        boolean optional = Optional.class.equals(method.getReturnType());
        JsonSerialize serialize = method.getAnnotation(JsonSerialize.class);
        JsonDeserialize deserialize = method.getAnnotation(JsonDeserialize.class);
        Class<?> serializer = serialize == null ? null : optional ? serialize.contentUsing() : serialize.using();
        Class<?> deserializer = deserialize == null ? null :
          optional ? deserialize.contentUsing() : deserialize.using();
        if (!ZonedDateTimeSerializer.class.equals(serializer) ||
          !ZonedDateTimeDeserializer.class.equals(deserializer)) {
          violations.add(classInfo.getName() + "#" + method.getName());
        }
      }
    }

    assertThat(violations)
      .withFailMessage(
        "ZonedDateTime accessors must be annotated with @JsonSerialize(%s = ZonedDateTimeSerializer.class) and " +
          "@JsonDeserialize(%s = ZonedDateTimeDeserializer.class), or @JsonIgnore. Violations: %s",
        "using (or contentUsing for Optional)", "using (or contentUsing for Optional)", violations
      )
      .isEmpty();
  }

  private static boolean isJsonBoundZonedDateTimeAccessor(Method method) {
    if (method.isSynthetic() || Modifier.isStatic(method.getModifiers()) || method.getParameterCount() != 0) {
      return false;
    }
    if (method.isAnnotationPresent(JsonIgnore.class)) {
      return false;
    }
    Type returnType = method.getGenericReturnType();
    if (ZonedDateTime.class.equals(returnType)) {
      return true;
    }
    if (returnType instanceof ParameterizedType) {
      ParameterizedType parameterized = (ParameterizedType) returnType;
      return Optional.class.equals(parameterized.getRawType()) &&
        ZonedDateTime.class.equals(parameterized.getActualTypeArguments()[0]);
    }
    return false;
  }
}
