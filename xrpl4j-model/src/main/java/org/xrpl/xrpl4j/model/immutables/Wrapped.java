package org.xrpl.xrpl4j.model.immutables;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

/**
 * Custom immutables annotation which can be used to generate immutable wrapper classes.
 *
 * @see "http://immutables.github.io/immutable.html#wrapper-types"
 */
@Value.Style(
  // Detect names starting with underscore
  typeAbstract = "_*",
  // Generate without any suffix, just raw detected name
  typeImmutable = "*",
  // Make generated public, leave underscored as package private
  visibility = ImplementationVisibility.PUBLIC,
  // Seems unnecessary to have builder or superfluous copy method
  defaults = @Value.Immutable(builder = false, copy = false))
public @interface Wrapped {

}
