package com.ripple.xrpl4j.codec.addresses;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.codec.addresses.exceptions.DecodeException;
import com.ripple.xrpl4j.codec.addresses.exceptions.EncodingFormatException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.Optional;

@RunWith(Enclosed.class)
public class XAddressTest {

  @RunWith(Parameterized.class)
  public static class XAddressParameterizedTests {

    String classicAddressAccountId;
    UnsignedInteger tag;
    String mainnetXAddress;
    String testnetXAddress;

    AddressCodec addressCodec;

    public XAddressParameterizedTests(
      String classicAddressAccountId,
      UnsignedInteger tag,
      String mainnetXAddress,
      String testnetXAddress
    ) {
      this.classicAddressAccountId = classicAddressAccountId;
      this.tag = tag;
      this.mainnetXAddress = mainnetXAddress;
      this.testnetXAddress = testnetXAddress;
      this.addressCodec = new AddressCodec();
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
      return Lists.newArrayList(
        new Object[] {
          "r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59",
          null,
          "X7AcgcsBL6XDcUb289X4mJ8djcdyKaB5hJDWMArnXr61cqZ",
          "T719a5UwUCnEs54UsxG9CJYYDhwmFCqkr7wxCcNcfZ6p5GZ"
        },
        new Object[] {
          "r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59",
          UnsignedInteger.ONE,
          "X7AcgcsBL6XDcUb289X4mJ8djcdyKaGZMhc9YTE92ehJ2Fu",
          "T719a5UwUCnEs54UsxG9CJYYDhwmFCvbJNZbi37gBGkRkbE"
        },
        new Object[] {
          "r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59",
          UnsignedInteger.valueOf(14),
          "X7AcgcsBL6XDcUb289X4mJ8djcdyKaGo2K5VpXpmCqbV2gS",
          "T719a5UwUCnEs54UsxG9CJYYDhwmFCvqXVCALUGJGSbNV3x"
        },
        new Object[] {
          "r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59",
          UnsignedInteger.valueOf(11747),
          "X7AcgcsBL6XDcUb289X4mJ8djcdyKaLFuhLRuNXPrDeJd9A",
          "T719a5UwUCnEs54UsxG9CJYYDhwmFCziiNHtUukubF2Mg6t"
        },
        new Object[] {
          "rLczgQHxPhWtjkaQqn3Q6UM8AbRbbRvs5K",
          null,
          "XVZVpQj8YSVpNyiwXYSqvQoQqgBttTxAZwMcuJd4xteQHyt",
          "TVVrSWtmQQssgVcmoMBcFQZKKf56QscyWLKnUyiuZW8ALU4"
        },
        new Object[] {
          "rpZc4mVfWUif9CRoHRKKcmhu1nx2xktxBo",
          null,
          "X7YenJqxv3L66CwhBSfd3N8RzGXxYqPopMGMsCcpho79rex",
          "T77wVQzA8ntj9wvCTNiQpNYLT5hmhRsFyXDoMLqYC4BzQtV"
        },
        new Object[] {
          "rpZc4mVfWUif9CRoHRKKcmhu1nx2xktxBo",
          UnsignedInteger.valueOf(58),
          "X7YenJqxv3L66CwhBSfd3N8RzGXxYqV56ZkTCa9UCzgaao1",
          "T77wVQzA8ntj9wvCTNiQpNYLT5hmhR9kej6uxm4jGcQD7rZ"
        },
        new Object[] {
          "rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW",
          UnsignedInteger.valueOf(23480),
          "X7d3eHCXzwBeWrZec1yT24iZerQjYL8m8zCJ16ACxu1BrBY",
          "T7YChPFWifjCAXLEtg5N74c7fSAYsvSokwcmBPBUZWhxH5P"
        },
        new Object[] {
          "rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW",
          UnsignedInteger.valueOf(11747),
          "X7d3eHCXzwBeWrZec1yT24iZerQjYLo2CJf8oVC5CMWey5m",
          "T7YChPFWifjCAXLEtg5N74c7fSAYsvTcc7nEfwuEEvn5Q4w"
        },
        new Object[] {
          "rGWrZyQqhTp9Xu7G5Pkayo7bXjH4k4QYpf",
          null, // false
          "XVLhHMPHU98es4dbozjVtdWzVrDjtV5fdx1mHp98tDMoQXb",
          "TVE26TYGhfLC7tQDno7G8dGtxSkYQn49b3qD26PK7FcGSKE"
        },
        new Object[] {
          "rGWrZyQqhTp9Xu7G5Pkayo7bXjH4k4QYpf",
          UnsignedInteger.ZERO,
          "XVLhHMPHU98es4dbozjVtdWzVrDjtV8AqEL4xcZj5whKbmc",
          "TVE26TYGhfLC7tQDno7G8dGtxSkYQnSy8RHqGHoGJ59spi2"
        },
        new Object[] {
          "rGWrZyQqhTp9Xu7G5Pkayo7bXjH4k4QYpf",
          UnsignedInteger.ONE,
          "XVLhHMPHU98es4dbozjVtdWzVrDjtV8xvjGQTYPiAx6gwDC",
          "TVE26TYGhfLC7tQDno7G8dGtxSkYQnSz1uDimDdPYXzSpyw"
        },
        new Object[] {
          "rGWrZyQqhTp9Xu7G5Pkayo7bXjH4k4QYpf",
          UnsignedInteger.valueOf(2),
          "XVLhHMPHU98es4dbozjVtdWzVrDjtV8zpDURx7DzBCkrQE7",
          "TVE26TYGhfLC7tQDno7G8dGtxSkYQnTryP9tG9TW8GeMBmd"
        },
        new Object[] {
          "rGWrZyQqhTp9Xu7G5Pkayo7bXjH4k4QYpf",
          UnsignedInteger.valueOf(32),
          "XVLhHMPHU98es4dbozjVtdWzVrDjtVoYiC9UvKfjKar4LJe",
          "TVE26TYGhfLC7tQDno7G8dGtxSkYQnT2oqaCDzMEuCDAj1j"
        },
        new Object[] {
          "rGWrZyQqhTp9Xu7G5Pkayo7bXjH4k4QYpf",
          UnsignedInteger.valueOf(276),
          "XVLhHMPHU98es4dbozjVtdWzVrDjtVoKj3MnFGMXEFMnvJV",
          "TVE26TYGhfLC7tQDno7G8dGtxSkYQnTMgJJYfAbsiPsc6Zg"
        },
        new Object[] {
          "rGWrZyQqhTp9Xu7G5Pkayo7bXjH4k4QYpf",
          UnsignedInteger.valueOf(65591),
          "XVLhHMPHU98es4dbozjVtdWzVrDjtVozpjdhPQVdt3ghaWw",
          "TVE26TYGhfLC7tQDno7G8dGtxSkYQn7ryu2W6njw7mT1jmS"
        },
        new Object[] {
          "rGWrZyQqhTp9Xu7G5Pkayo7bXjH4k4QYpf",
          UnsignedInteger.valueOf(16781933),
          "XVLhHMPHU98es4dbozjVtdWzVrDjtVqrDUk2vDpkTjPsY73",
          "TVE26TYGhfLC7tQDno7G8dGtxSkYQnVsw45sDtGHhLi27Qa"
        },
        new Object[] {
          "rGWrZyQqhTp9Xu7G5Pkayo7bXjH4k4QYpf",
          UnsignedInteger.valueOf(4294967294L),
          "XVLhHMPHU98es4dbozjVtdWzVrDjtV1kAsixQTdMjbWi39u",
          "TVE26TYGhfLC7tQDno7G8dGtxSkYQnX8tDFQ53itLNqs6vU"
        },
        new Object[] {
          "rGWrZyQqhTp9Xu7G5Pkayo7bXjH4k4QYpf",
          UnsignedInteger.valueOf(4294967295L),
          "XVLhHMPHU98es4dbozjVtdWzVrDjtV18pX8yuPT7y4xaEHi",
          "TVE26TYGhfLC7tQDno7G8dGtxSkYQnXoy6kSDh6rZzApc69"
        },
        new Object[] {
          "rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY",
          null,
          "XV5sbjUmgPpvXv4ixFWZ5ptAYZ6PD2gYsjNFQLKYW33DzBm",
          "TVd2rqMkYL2AyS97NdELcpeiprNBjwLZzuUG5rZnaewsahi"
        },
        new Object[] {
          "rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY",
          UnsignedInteger.ZERO,
          "XV5sbjUmgPpvXv4ixFWZ5ptAYZ6PD2m4Er6SnvjVLpMWPjR",
          "TVd2rqMkYL2AyS97NdELcpeiprNBjwRQUBetPbyrvXSTuxU"
        },
        new Object[] {
          "rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY",
          UnsignedInteger.valueOf(13371337),
          "XV5sbjUmgPpvXv4ixFWZ5ptAYZ6PD2qwGkhgc48zzcx6Gkr",
          "TVd2rqMkYL2AyS97NdELcpeiprNBjwVUDvp3vhpXbNhLwJi"
        }
      );
    }


    @Test
    public void convertBetweenClassicAndXAddressMainnet() {
      ClassicAddress fromXAddress = addressCodec.xAddressToClassicAddress(mainnetXAddress);
      ClassicAddress classicAddress = ClassicAddress.builder()
        .classicAddress(classicAddressAccountId)
        .tag(tag == null ? UnsignedInteger.ZERO : tag)
        .test(false)
        .build();

      assertThat(fromXAddress).isEqualTo(classicAddress);

      String xAddress = addressCodec.classicAddressToXAddress(classicAddressAccountId, Optional.ofNullable(tag), false);
      assertThat(xAddress).isEqualTo(mainnetXAddress);

      assertThat(addressCodec.isValidClassicAddress(fromXAddress.classicAddress()));
      assertThat(addressCodec.isValidXAddress(xAddress));
    }

    @Test
    public void convertBetweenClassicAndXAddressTestnet() {
      ClassicAddress fromXAddress = addressCodec.xAddressToClassicAddress(testnetXAddress);
      ClassicAddress classicAddress = ClassicAddress.builder()
        .classicAddress(classicAddressAccountId)
        .tag(tag == null ? UnsignedInteger.ZERO : tag)
        .test(true)
        .build();

      assertThat(fromXAddress).isEqualTo(classicAddress);

      String xAddress = addressCodec.classicAddressToXAddress(classicAddressAccountId, Optional.ofNullable(tag), true);
      assertThat(xAddress).isEqualTo(testnetXAddress);

      assertThat(addressCodec.isValidClassicAddress(fromXAddress.classicAddress()));
      assertThat(addressCodec.isValidXAddress(xAddress));
    }
  }

  public static class XAddressTests {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    AddressCodec addressCodec;

    @Before
    public void setUp() throws Exception {
      addressCodec = new AddressCodec();
    }

    @Test
    public void xAddressWithBadChecksum() {
      String xAddress = "XVLhHMPHU98es4dbozjVtdWzVrDjtV5fdx1mHp98tDMoQXa";

      expectedException.expect(EncodingFormatException.class);
      expectedException.expectMessage("Checksum does not validate");
      addressCodec.xAddressToClassicAddress(xAddress);
    }


    @Test
    public void xAddressWithBadPrefix() {
      String xAddress = "dGzKGt8CVpWoa8aWL1k18tAdy9Won3PxynvbbpkAqp3V47g";

      expectedException.expect(DecodeException.class);
      expectedException.expectMessage("Invalid X-Address: Bad Prefix");
      addressCodec.xAddressToClassicAddress(xAddress);
    }

    @Test
    public void xAddressWith64BitTag() {
      String xAddress = "XVLhHMPHU98es4dbozjVtdWzVrDjtV18pX8zeUygYrCgrPh";

      expectedException.expect(DecodeException.class);
      expectedException.expectMessage("Unsupported X-Address: 64-bit tags are not supported");
      addressCodec.xAddressToClassicAddress(xAddress);
    }

  }
}
