package com.ripple.xrpl4j.tests;

import java.util.Optional;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Provides a set of Hamcrest matchers for {@code java.util.Optional}:
 * <ul>
 *     <li>{@link #isEmpty()} - matches when the examined {@code Optional}
 *     contains no value.</li>
 *     <li>{@link #isPresent()} - matches when the examined {@code Optional}
 *     contains a value.</li>
 *     <li>{@link #isPresentAndIs(Object)} - matches when the examined
 *     {@code Optional} contains a value that is logically equal to the
 *     {@code operand}.</li>
 *     <li>{@link #isPresentAnd(Matcher)} - matches when the examined
 *     {@code Optional} contains a value that satisfies the specified matcher.
 *     </li>
 * </ul>
 *
 * @author npathai, sweiler
 */
public class OptionalMatchers {

  /**
   * Creates a matcher that matches when the examined {@code Optional}
   * contains a value.
   * <pre>
   *     Optional&lt;String&gt; optionalObject = Optional.of("dummy value");
   *     assertThat(optionalObject, isPresent());
   * </pre>
   *
   * @return  a matcher that matches when the examined {@code Optional}
   * contains a value.
   */
  public static Matcher<Optional<?>> isPresent() {
    return new PresenceMatcher();
  }

  private static class PresenceMatcher extends TypeSafeMatcher<Optional<?>> {

    public void describeTo(Description description) {
      description.appendText("is <Present>");
    }

    @Override
    protected boolean matchesSafely(Optional<?> item) {
      return item.isPresent();
    }

    @Override
    protected void describeMismatchSafely(Optional<?> item, Description mismatchDescription) {
      mismatchDescription.appendText("was <Empty>");
    }
  }

  /**
   * Creates a matcher that matches when the examined {@code Optional}
   * contains no value.
   * <pre>
   *     Optional&lt;String&gt; optionalObject = Optional.empty();
   *     assertThat(optionalObject, isEmpty());
   * </pre>
   *
   * @return  a matcher that matches when the examined {@code Optional}
   * contains no value.
   */
  public static Matcher<Optional<?>> isEmpty() {
    return new EmptyMatcher();
  }

  private static class EmptyMatcher extends TypeSafeMatcher<Optional<?>> {

    public void describeTo(Description description) {
      description.appendText("is <Empty>");
    }

    @Override
    protected boolean matchesSafely(Optional<?> item) {
      return !item.isPresent();
    }

    @Override
    protected void describeMismatchSafely(Optional<?> item, Description mismatchDescription) {
      mismatchDescription.appendText("had value ");
      mismatchDescription.appendValue(item.get());
    }
  }

  /**
   * Creates a matcher that matches when the examined {@code Optional}
   * contains a value that is logically equal to the {@code operand}, as
   * determined by calling the {@code equals} method on the value.
   * <pre>
   *     Optional&lt;String&gt; optionalInt = Optional.of("dummy value");
   *     assertThat(optionalInt, isPresentAndIs("dummy value"));
   * </pre>
   *
   * @param operand the object that any examined {@code Optional} value
   * should equal
   * @param <T> the class of the value.
   * @return  a matcher that matches when the examined {@code Optional}
   * contains a value that is logically equal to the {@code operand}.
   */
  public static <T> Matcher<Optional<T>> isPresentAndIs(T operand) {
    return new HasValue<>(equalTo(operand));
  }

  /**
   * Creates a matcher that matches when the examined {@code Optional}
   * contains a value that satisfies the specified matcher.
   * <pre>
   *     Optional&lt;String&gt; optionalObject = Optional.of("dummy value");
   *     assertThat(optionalObject, isPresentAnd(startsWith("dummy")));
   * </pre>
   *
   * @param matcher a matcher for the value of the examined {@code Optional}.
   * @param <T> the class of the value.
   * @return  a matcher that matches when the examined {@code Optional}
   * contains a value that satisfies the specified matcher.
   */
  public static <T> Matcher<Optional<T>> isPresentAnd(Matcher<? super T> matcher) {
    return new HasValue<>(matcher);
  }

  private static class HasValue<T> extends TypeSafeMatcher<Optional<T>> {
    private Matcher<? super T> matcher;

    public HasValue(Matcher<? super T> matcher) {
      this.matcher = matcher;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("has value that is ");
      matcher.describeTo(description);
    }

    @Override
    protected boolean matchesSafely(Optional<T> item) {
      return item.isPresent() && matcher.matches(item.get());
    }

    @Override
    protected void describeMismatchSafely(Optional<T> item, Description mismatchDescription) {
      if (item.isPresent()) {
        mismatchDescription.appendText("value ");
        matcher.describeMismatch(item.get(), mismatchDescription);
      } else {
        mismatchDescription.appendText("was <Empty>");
      }
    }
  }

  //This is an utility class that must not be instantiated.
  private OptionalMatchers() {
  }
}
