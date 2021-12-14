package org.xrpl.xrpl4j.crypto.bc.wallet;

import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.codec.addresses.VersionType;
import org.xrpl.xrpl4j.crypto.bc.BcAddressUtils;
import org.xrpl.xrpl4j.crypto.bc.keys.Ed25519KeyPairService;
import org.xrpl.xrpl4j.crypto.bc.keys.Secp256k1KeyPairService;
import org.xrpl.xrpl4j.crypto.core.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.core.keys.KeyPairService;
import org.xrpl.xrpl4j.crypto.core.keys.Seed;
import org.xrpl.xrpl4j.crypto.core.wallet.SeedWalletGenerationResult;
import org.xrpl.xrpl4j.crypto.core.wallet.Wallet;
import org.xrpl.xrpl4j.crypto.core.wallet.WalletFactory;

import java.util.Objects;

/**
 * Default implementation of {@link WalletFactory}.
 */
public class BcWalletFactory implements WalletFactory {

  private static final WalletFactory INSTANCE = new BcWalletFactory(
    Ed25519KeyPairService.getInstance(), Secp256k1KeyPairService.getInstance()
  );

  private final Ed25519KeyPairService ed25519KeyPairService;
  private final Secp256k1KeyPairService secp256k1KeyPairService;

  /**
   * Construct a {@link BcWalletFactory} from a {@link KeyPairService} and an {@link AddressCodec}.
   *
   * @param ed25519KeyPairService   An {@link Ed25519KeyPairService}.
   * @param secp256k1KeyPairService A {@link Secp256k1KeyPairService}.
   */
  public BcWalletFactory(final Ed25519KeyPairService ed25519KeyPairService,
    final Secp256k1KeyPairService secp256k1KeyPairService) {
    this.ed25519KeyPairService = Objects.requireNonNull(ed25519KeyPairService);
    this.secp256k1KeyPairService = Objects.requireNonNull(secp256k1KeyPairService);
  }

  /**
   * Get a JVM wide {@link WalletFactory} instance.
   *
   * @return A static {@link BcWalletFactory} instance.
   */
  public static WalletFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public SeedWalletGenerationResult randomWalletEd25519() {
    Seed seed = ed25519KeyPairService.generateSeed();
    Wallet wallet = this.fromSeed(seed);

    return SeedWalletGenerationResult.builder().seed(seed).wallet(wallet).build();
  }

  @Override
  public SeedWalletGenerationResult randomWalletSecp256k1() {
    Seed seed = secp256k1KeyPairService.generateSeed();
    Wallet wallet = this.fromSeed(seed);

    return SeedWalletGenerationResult.builder().seed(seed).wallet(wallet).build();
  }

  @Override
  public Wallet fromSeed(final Seed seed) {
    Objects.requireNonNull(seed);

    return seed.decodedSeed().type()
      .map(versionType -> versionType.equals(VersionType.ED25519) ?
        this.ed25519KeyPairService : this.secp256k1KeyPairService
      )
      .map(service -> service.deriveKeyPair(seed))
      .map(this::fromKeyPair)
      .orElseThrow(() -> new IllegalArgumentException("Unsupported seed type."));
  }

  @Override
  public Wallet fromKeyPair(final KeyPair keyPair) {
    Objects.requireNonNull(keyPair);

    return Wallet.builder()
      .privateKey(keyPair.privateKey())
      .publicKey(keyPair.publicKey())
      .address(BcAddressUtils.getInstance().deriveAddress(keyPair.publicKey()))
      .build();
  }

}
