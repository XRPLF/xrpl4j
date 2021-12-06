package org.xrpl.xrpl4j.codec.addresses;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.checkerframework.checker.nullness.Opt;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.xrpl.xrpl4j.codec.addresses.exceptions.DecodeException;
import org.xrpl.xrpl4j.codec.addresses.exceptions.EncodingFormatException;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.XAddress;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Optional;

@SuppressWarnings( {"ParameterName", "MethodName", "LocalVariableName"})
public class AddressTest {

  private final AddressCodec addressCodec = new AddressCodec();

  /**
   * Construct the test cases for this parameterized test.
   *
   * @return A {@link Collection} of {@link Object} arrays.
   */
  public static Collection<Object[]> data() {
    return Lists.newArrayList(
      new Object[] {
        Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"),
        null,
        XAddress.of("X7AcgcsBL6XDcUb289X4mJ8djcdyKaB5hJDWMArnXr61cqZ"),
        XAddress.of("T719a5UwUCnEs54UsxG9CJYYDhwmFCqkr7wxCcNcfZ6p5GZ")
      },
      new Object[] {
        Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"),
        UnsignedInteger.ONE,
        XAddress.of("X7AcgcsBL6XDcUb289X4mJ8djcdyKaGZMhc9YTE92ehJ2Fu"),
        XAddress.of("T719a5UwUCnEs54UsxG9CJYYDhwmFCvbJNZbi37gBGkRkbE")
      },
      new Object[] {
        Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"),
        UnsignedInteger.valueOf(14),
        XAddress.of("X7AcgcsBL6XDcUb289X4mJ8djcdyKaGo2K5VpXpmCqbV2gS"),
        XAddress.of("T719a5UwUCnEs54UsxG9CJYYDhwmFCvqXVCALUGJGSbNV3x")
      },
      new Object[] {
        Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"),
        UnsignedInteger.valueOf(11747),
        XAddress.of("X7AcgcsBL6XDcUb289X4mJ8djcdyKaLFuhLRuNXPrDeJd9A"),
        XAddress.of("T719a5UwUCnEs54UsxG9CJYYDhwmFCziiNHtUukubF2Mg6t")
      },
      new Object[] {
        Address.of("rLczgQHxPhWtjkaQqn3Q6UM8AbRbbRvs5K"),
        null,
        XAddress.of("XVZVpQj8YSVpNyiwXYSqvQoQqgBttTxAZwMcuJd4xteQHyt"),
        XAddress.of("TVVrSWtmQQssgVcmoMBcFQZKKf56QscyWLKnUyiuZW8ALU4")
      },
      new Object[] {
        Address.of("rpZc4mVfWUif9CRoHRKKcmhu1nx2xktxBo"),
        null,
        XAddress.of("X7YenJqxv3L66CwhBSfd3N8RzGXxYqPopMGMsCcpho79rex"),
        XAddress.of("T77wVQzA8ntj9wvCTNiQpNYLT5hmhRsFyXDoMLqYC4BzQtV")
      },
      new Object[] {
        Address.of("rpZc4mVfWUif9CRoHRKKcmhu1nx2xktxBo"),
        UnsignedInteger.valueOf(58),
        XAddress.of("X7YenJqxv3L66CwhBSfd3N8RzGXxYqV56ZkTCa9UCzgaao1"),
        XAddress.of("T77wVQzA8ntj9wvCTNiQpNYLT5hmhR9kej6uxm4jGcQD7rZ")
      },
      new Object[] {
        Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"),
        UnsignedInteger.valueOf(23480),
        XAddress.of("X7d3eHCXzwBeWrZec1yT24iZerQjYL8m8zCJ16ACxu1BrBY"),
        XAddress.of("T7YChPFWifjCAXLEtg5N74c7fSAYsvSokwcmBPBUZWhxH5P")
      },
      new Object[] {
        Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"),
        UnsignedInteger.valueOf(11747),
        XAddress.of("X7d3eHCXzwBeWrZec1yT24iZerQjYLo2CJf8oVC5CMWey5m"),
        XAddress.of("T7YChPFWifjCAXLEtg5N74c7fSAYsvTcc7nEfwuEEvn5Q4w")
      },
      new Object[] {
        Address.of("rGWrZyQqhTp9Xu7G5Pkayo7bXjH4k4QYpf"),
        null, // false
        XAddress.of("XVLhHMPHU98es4dbozjVtdWzVrDjtV5fdx1mHp98tDMoQXb"),
        XAddress.of("TVE26TYGhfLC7tQDno7G8dGtxSkYQn49b3qD26PK7FcGSKE")
      },
      new Object[] {
        Address.of("rGWrZyQqhTp9Xu7G5Pkayo7bXjH4k4QYpf"),
        UnsignedInteger.ZERO,
        XAddress.of("XVLhHMPHU98es4dbozjVtdWzVrDjtV8AqEL4xcZj5whKbmc"),
        XAddress.of("TVE26TYGhfLC7tQDno7G8dGtxSkYQnSy8RHqGHoGJ59spi2")
      },
      new Object[] {
        Address.of("rGWrZyQqhTp9Xu7G5Pkayo7bXjH4k4QYpf"),
        UnsignedInteger.ONE,
        XAddress.of("XVLhHMPHU98es4dbozjVtdWzVrDjtV8xvjGQTYPiAx6gwDC"),
        XAddress.of("TVE26TYGhfLC7tQDno7G8dGtxSkYQnSz1uDimDdPYXzSpyw")
      },
      new Object[] {
        Address.of("rGWrZyQqhTp9Xu7G5Pkayo7bXjH4k4QYpf"),
        UnsignedInteger.valueOf(2),
        XAddress.of("XVLhHMPHU98es4dbozjVtdWzVrDjtV8zpDURx7DzBCkrQE7"),
        XAddress.of("TVE26TYGhfLC7tQDno7G8dGtxSkYQnTryP9tG9TW8GeMBmd")
      },
      new Object[] {
        Address.of("rGWrZyQqhTp9Xu7G5Pkayo7bXjH4k4QYpf"),
        UnsignedInteger.valueOf(32),
        XAddress.of("XVLhHMPHU98es4dbozjVtdWzVrDjtVoYiC9UvKfjKar4LJe"),
        XAddress.of("TVE26TYGhfLC7tQDno7G8dGtxSkYQnT2oqaCDzMEuCDAj1j")
      },
      new Object[] {
        Address.of("rGWrZyQqhTp9Xu7G5Pkayo7bXjH4k4QYpf"),
        UnsignedInteger.valueOf(276),
        XAddress.of("XVLhHMPHU98es4dbozjVtdWzVrDjtVoKj3MnFGMXEFMnvJV"),
        XAddress.of("TVE26TYGhfLC7tQDno7G8dGtxSkYQnTMgJJYfAbsiPsc6Zg")
      },
      new Object[] {
        Address.of("rGWrZyQqhTp9Xu7G5Pkayo7bXjH4k4QYpf"),
        UnsignedInteger.valueOf(65591),
        XAddress.of("XVLhHMPHU98es4dbozjVtdWzVrDjtVozpjdhPQVdt3ghaWw"),
        XAddress.of("TVE26TYGhfLC7tQDno7G8dGtxSkYQn7ryu2W6njw7mT1jmS")
      },
      new Object[] {
        Address.of("rGWrZyQqhTp9Xu7G5Pkayo7bXjH4k4QYpf"),
        UnsignedInteger.valueOf(16781933),
        XAddress.of("XVLhHMPHU98es4dbozjVtdWzVrDjtVqrDUk2vDpkTjPsY73"),
        XAddress.of("TVE26TYGhfLC7tQDno7G8dGtxSkYQnVsw45sDtGHhLi27Qa")
      },
      new Object[] {
        Address.of("rGWrZyQqhTp9Xu7G5Pkayo7bXjH4k4QYpf"),
        UnsignedInteger.valueOf(4294967294L),
        XAddress.of("XVLhHMPHU98es4dbozjVtdWzVrDjtV1kAsixQTdMjbWi39u"),
        XAddress.of("TVE26TYGhfLC7tQDno7G8dGtxSkYQnX8tDFQ53itLNqs6vU")
      },
      new Object[] {
        Address.of("rGWrZyQqhTp9Xu7G5Pkayo7bXjH4k4QYpf"),
        UnsignedInteger.valueOf(4294967295L),
        XAddress.of("XVLhHMPHU98es4dbozjVtdWzVrDjtV18pX8yuPT7y4xaEHi"),
        XAddress.of("TVE26TYGhfLC7tQDno7G8dGtxSkYQnXoy6kSDh6rZzApc69")
      },
      new Object[] {
        Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"),
        null,
        XAddress.of("XV5sbjUmgPpvXv4ixFWZ5ptAYZ6PD2gYsjNFQLKYW33DzBm"),
        XAddress.of("TVd2rqMkYL2AyS97NdELcpeiprNBjwLZzuUG5rZnaewsahi")
      },
      new Object[] {
        Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"),
        UnsignedInteger.ZERO,
        XAddress.of("XV5sbjUmgPpvXv4ixFWZ5ptAYZ6PD2m4Er6SnvjVLpMWPjR"),
        XAddress.of("TVd2rqMkYL2AyS97NdELcpeiprNBjwRQUBetPbyrvXSTuxU")
      },
      new Object[] {
        Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"),
        UnsignedInteger.valueOf(13371337),
        XAddress.of("XV5sbjUmgPpvXv4ixFWZ5ptAYZ6PD2qwGkhgc48zzcx6Gkr"),
        XAddress.of("TVd2rqMkYL2AyS97NdELcpeiprNBjwVUDvp3vhpXbNhLwJi")
      }
    );
  }

  @ParameterizedTest
  @MethodSource("data")
  public void convertBetweenClassicAndXAddressMainnet(
    Address classicAddressAccountId,
    UnsignedInteger tag,
    XAddress mainnetXAddress,
    XAddress testnetXAddress
  ) {
    ClassicAddress fromXAddress = addressCodec.xAddressToClassicAddress(mainnetXAddress);
    ClassicAddress classicAddress = ClassicAddress.builder()
      .classicAddress(classicAddressAccountId)
      .tag(tag == null ? UnsignedInteger.ZERO : tag)
      .test(false)
      .build();

    assertThat(fromXAddress).isEqualTo(classicAddress);

    XAddress xAddress = addressCodec.classicAddressToXAddress(
      classicAddressAccountId,
      Optional.ofNullable(tag),
      false
    );
    assertThat(xAddress).isEqualTo(mainnetXAddress);

    assertThat(addressCodec.isValidClassicAddress(fromXAddress.classicAddress()));
    assertThat(addressCodec.isValidXAddress(xAddress));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void convertBetweenClassicAndXAddressTestnet(
    Address classicAddressAccountId,
    UnsignedInteger tag,
    XAddress mainnetXAddress,
    XAddress testnetXAddress
  ) {
    ClassicAddress fromXAddress = addressCodec.xAddressToClassicAddress(testnetXAddress);
    ClassicAddress classicAddress = ClassicAddress.builder()
      .classicAddress(classicAddressAccountId)
      .tag(tag == null ? UnsignedInteger.ZERO : tag)
      .test(true)
      .build();

    assertThat(fromXAddress).isEqualTo(classicAddress);

    XAddress xAddress = addressCodec.classicAddressToXAddress(
      classicAddressAccountId,
      Optional.ofNullable(tag),
      true
    );
    assertThat(xAddress).isEqualTo(testnetXAddress);

    assertThat(addressCodec.isValidClassicAddress(fromXAddress.classicAddress()));
    assertThat(addressCodec.isValidXAddress(xAddress));
  }

  @Test
  public void xAddressWithBadChecksum() {
    XAddress xAddress = XAddress.of("XVLhHMPHU98es4dbozjVtdWzVrDjtV5fdx1mHp98tDMoQXa");

    assertThrows(
      EncodingFormatException.class,
      () -> addressCodec.xAddressToClassicAddress(xAddress),
      "Checksum does not validate"
    );
  }


  @Test
  public void xAddressWithBadPrefix() {
    XAddress xAddress = XAddress.of("dGzKGt8CVpWoa8aWL1k18tAdy9Won3PxynvbbpkAqp3V47g");

    assertThrows(
      DecodeException.class,
      () -> addressCodec.xAddressToClassicAddress(xAddress),
      "Invalid X-Address: Bad Prefix"
    );
  }

  @Test
  public void xAddressWith64BitTag() {
    XAddress xAddress = XAddress.of("XVLhHMPHU98es4dbozjVtdWzVrDjtV18pX8zeUygYrCgrPh");

    assertThrows(
      DecodeException.class,
      () -> addressCodec.xAddressToClassicAddress(xAddress),
      "Unsupported X-Address: 64-bit tags are not supported"
    );
  }


  @Test
  public void addressWithBadChecksum() {
    Address address = Address.of("r9cZA1mLK5R5am25ArfXFmqgNwjZgnfk59");

    assertThrows(
      EncodingFormatException.class,
      () -> addressCodec.classicAddressToXAddress(address, true),
      "Checksum does not validate"
    );
  }


  @Test
  public void addressWithBadPrefix() {

    assertThrows(
      IllegalArgumentException.class,
      () -> {
        Address address = Address.of("c9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59");
        addressCodec.classicAddressToXAddress(address, true);
      },
      "Invalid Address: Bad Prefix"
    );
  }

}
