package org.xrpl.xrpl4j.model.transactions.metadata;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;

import com.google.common.base.Strings;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.transactions.Hash256;

class AffectedNodeTest {

  @Test
  void handleWithNullHandlers() {
    AffectedNode affectedNode = ImmutableCreatedNode.builder()
      .ledgerEntryType(MetaLedgerEntryType.ACCOUNT_ROOT)
      .newFields(mock(MetaAccountRootObject.class))
      .ledgerIndex(Hash256.of(Strings.repeat("0", 64)))
      .build();

    assertThatThrownBy(
      () -> affectedNode.handle(
        null,
        $ -> new Object(),
        $ -> new Object()
      )
    ).isInstanceOf(NullPointerException.class);

    assertThatThrownBy(
      () -> affectedNode.handle(
        $ -> new Object(),
        null,
        $ -> new Object()
      )
    ).isInstanceOf(NullPointerException.class);

    assertThatThrownBy(
      () -> affectedNode.handle(
        $ -> new Object(),
        $ -> new Object(),
        null
      )
    ).isInstanceOf(NullPointerException.class);
  }

  @Test
  void handleUnsupportedType() {
    assertThatThrownBy(
      () -> new AffectedNode() {
        @Override
        public MetaLedgerEntryType ledgerEntryType() {
          return null;
        }

        @Override
        public Hash256 ledgerIndex() {
          return null;
        }
      }.handle(
        $ -> new Object(),
        $ -> new Object(),
        $ -> new Object()
      )
    ).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void handleCreatedNode() {
    AffectedNode affectedNode = ImmutableCreatedNode.builder()
      .ledgerEntryType(MetaLedgerEntryType.ACCOUNT_ROOT)
      .newFields(mock(MetaAccountRootObject.class))
      .ledgerIndex(Hash256.of(Strings.repeat("0", 64)))
      .build();

    affectedNode.handle(
      $ -> assertThat($.ledgerEntryType()).isEqualTo(MetaLedgerEntryType.ACCOUNT_ROOT),
      $ -> fail(),
      $ -> fail()
    );
  }

  @Test
  void handleModifiedNode() {
    AffectedNode affectedNode = ImmutableModifiedNode.builder()
      .ledgerEntryType(MetaLedgerEntryType.ACCOUNT_ROOT)
      .ledgerIndex(Hash256.of(Strings.repeat("0", 64)))
      .build();

    affectedNode.handle(
      $ -> fail(),
      $ -> assertThat($.ledgerEntryType()).isEqualTo(MetaLedgerEntryType.ACCOUNT_ROOT),
      $ -> fail()
    );
  }

  @Test
  void handleDeletedNode() {
    AffectedNode affectedNode = ImmutableDeletedNode.builder()
      .ledgerEntryType(MetaLedgerEntryType.ACCOUNT_ROOT)
      .finalFields(mock(MetaAccountRootObject.class))
      .ledgerIndex(Hash256.of(Strings.repeat("0", 64)))
      .build();

    affectedNode.handle(
      $ -> fail(),
      $ -> fail(),
      $ -> assertThat($.ledgerEntryType()).isEqualTo(MetaLedgerEntryType.ACCOUNT_ROOT)
    );
  }

  @Test
  void mapWithNullHandlers() {
    AffectedNode affectedNode = ImmutableCreatedNode.builder()
      .ledgerEntryType(MetaLedgerEntryType.ACCOUNT_ROOT)
      .newFields(mock(MetaAccountRootObject.class))
      .ledgerIndex(Hash256.of(Strings.repeat("0", 64)))
      .build();

    assertThatThrownBy(
      () -> affectedNode.map(
        null,
        $ -> new Object(),
        $ -> new Object()
      )
    ).isInstanceOf(NullPointerException.class);

    assertThatThrownBy(
      () -> affectedNode.map(
        $ -> new Object(),
        null,
        $ -> new Object()
      )
    ).isInstanceOf(NullPointerException.class);

    assertThatThrownBy(
      () -> affectedNode.map(
        $ -> new Object(),
        $ -> new Object(),
        null
      )
    ).isInstanceOf(NullPointerException.class);
  }

  @Test
  void mapUnsupportedType() {
    assertThatThrownBy(
      () -> new AffectedNode() {
        @Override
        public MetaLedgerEntryType ledgerEntryType() {
          return null;
        }

        @Override
        public Hash256 ledgerIndex() {
          return null;
        }
      }.map(
        $ -> new Object(),
        $ -> new Object(),
        $ -> new Object()
      )
    ).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void mapCreatedNode() {
    AffectedNode affectedNode = ImmutableCreatedNode.builder()
      .ledgerEntryType(MetaLedgerEntryType.ACCOUNT_ROOT)
      .newFields(mock(MetaAccountRootObject.class))
      .ledgerIndex(Hash256.of(Strings.repeat("0", 64)))
      .build();

    String mapped = affectedNode.map(
      $ -> "success",
      $ -> "fail",
      $ -> "fail"
    );

    assertThat(mapped).isEqualTo("success");
  }

  @Test
  void mapModifiedNode() {
    AffectedNode affectedNode = ImmutableModifiedNode.builder()
      .ledgerEntryType(MetaLedgerEntryType.ACCOUNT_ROOT)
      .ledgerIndex(Hash256.of(Strings.repeat("0", 64)))
      .build();

    String mapped = affectedNode.map(
      $ -> "fail",
      $ -> "success",
      $ -> "fail"
    );

    assertThat(mapped).isEqualTo("success");
  }

  @Test
  void mapDeletedNode() {
    AffectedNode affectedNode = ImmutableDeletedNode.builder()
      .ledgerEntryType(MetaLedgerEntryType.ACCOUNT_ROOT)
      .finalFields(mock(MetaAccountRootObject.class))
      .ledgerIndex(Hash256.of(Strings.repeat("0", 64)))
      .build();

    String mapped = affectedNode.map(
      $ -> "fail",
      $ -> "fail",
      $ -> "success"
    );

    assertThat(mapped).isEqualTo("success");
  }
}