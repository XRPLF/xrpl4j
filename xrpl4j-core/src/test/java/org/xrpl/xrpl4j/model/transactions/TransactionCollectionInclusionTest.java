package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.reflect.ClassPath;
import org.immutables.value.Value;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Guards against a collection-typed field being added to a {@link Transaction} type without an explicit decision
 * about how an empty value is serialized. xrpld treats empty arrays inconsistently: some must be omitted (an empty
 * {@code Paths} is rejected with {@code temRIPPLE_EMPTY}; an empty {@code Signers} fails multi-sign validation), while
 * others must be present (an empty {@code Permissions} on {@code DelegateSet} clears all permissions). Because the
 * choice also changes the canonical JSON that is signed, every such accessor must declare {@link JsonInclude}
 * explicitly, whether {@code NON_EMPTY} or {@code ALWAYS}, so that the decision is visible in review.
 */
class TransactionCollectionInclusionTest {

  private static final String TRANSACTIONS_PACKAGE = "org.xrpl.xrpl4j.model.transactions";

  @Test
  void everyCollectionAccessorOnTransactionTypesDeclaresJsonInclude() throws IOException {
    List<String> violations = new ArrayList<>();

    for (ClassPath.ClassInfo classInfo : ClassPath.from(getClass().getClassLoader())
      .getTopLevelClassesRecursive(TRANSACTIONS_PACKAGE)) {
      if (classInfo.url().toString().contains("test-classes")) {
        continue;
      }
      Class<?> clazz = classInfo.load();
      if (!clazz.isInterface() || !Transaction.class.isAssignableFrom(clazz)) {
        continue;
      }
      for (Method method : clazz.getDeclaredMethods()) {
        if (isJsonBoundCollectionAccessor(method) && !method.isAnnotationPresent(JsonInclude.class)) {
          violations.add(clazz.getSimpleName() + "#" + method.getName());
        }
      }
    }

    assertThat(violations)
      .withFailMessage(
        "Collection-typed accessors on Transaction types must declare @JsonInclude explicitly " +
          "(NON_EMPTY to omit an empty value, ALWAYS to serialize it), so that the effect on the signed JSON is a " +
          "deliberate, reviewable choice. Violations: %s",
        violations
      )
      .isEmpty();
  }

  /**
   * Whether {@code method} is an Immutables attribute that Jackson will serialize and whose type is a collection, a
   * map, or an {@link Optional} of one.
   */
  private static boolean isJsonBoundCollectionAccessor(Method method) {
    if (method.isSynthetic() || Modifier.isStatic(method.getModifiers()) || method.getParameterCount() != 0) {
      return false;
    }
    // A default method is only an attribute if it is a @Value.Default; otherwise it is a helper.
    if (method.isDefault() && !method.isAnnotationPresent(Value.Default.class)) {
      return false;
    }
    if (method.isAnnotationPresent(JsonIgnore.class) ||
      method.isAnnotationPresent(Value.Derived.class) ||
      method.isAnnotationPresent(Value.Lazy.class)) {
      return false;
    }
    return isCollectionType(method.getGenericReturnType());
  }

  private static boolean isCollectionType(Type type) {
    Class<?> raw = rawClass(type);
    if (raw == null) {
      return false;
    }
    if (Collection.class.isAssignableFrom(raw) || Map.class.isAssignableFrom(raw)) {
      return true;
    }
    if (Optional.class.equals(raw) && type instanceof ParameterizedType) {
      return isCollectionType(((ParameterizedType) type).getActualTypeArguments()[0]);
    }
    return false;
  }

  private static Class<?> rawClass(Type type) {
    if (type instanceof Class) {
      return (Class<?>) type;
    }
    if (type instanceof ParameterizedType) {
      return (Class<?>) ((ParameterizedType) type).getRawType();
    }
    return null;
  }
}
