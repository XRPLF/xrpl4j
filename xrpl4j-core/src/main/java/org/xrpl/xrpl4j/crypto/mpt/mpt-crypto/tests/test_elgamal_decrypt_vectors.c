#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <inttypes.h>
#include <secp256k1.h>
#include "secp256k1_mpt.h"
#include "test_utils.h"

static void print_hex(const unsigned char* data, size_t len) {
    for (size_t i = 0; i < len; i++) {
        printf("%02X", data[i]);
    }
}

static void print_test_vector(const secp256k1_context* ctx, uint64_t amount, int is_last) {
    unsigned char privkey[32], blinding_factor[32];
    secp256k1_pubkey pubkey, c1, c2, temp_pubkey;
    unsigned char pubkey_bytes[33], c1_bytes[33], c2_bytes[33];
    size_t pubkey_len = 33, c1_len = 33, c2_len = 33;

    EXPECT(secp256k1_elgamal_generate_keypair(ctx, privkey, &pubkey) == 1);
    EXPECT(secp256k1_elgamal_generate_keypair(ctx, blinding_factor, &temp_pubkey) == 1);
    EXPECT(secp256k1_elgamal_encrypt(ctx, &c1, &c2, &pubkey, amount, blinding_factor) == 1);

    EXPECT(secp256k1_ec_pubkey_serialize(ctx, pubkey_bytes, &pubkey_len, &pubkey, SECP256K1_EC_COMPRESSED) == 1);
    EXPECT(secp256k1_ec_pubkey_serialize(ctx, c1_bytes, &c1_len, &c1, SECP256K1_EC_COMPRESSED) == 1);
    EXPECT(secp256k1_ec_pubkey_serialize(ctx, c2_bytes, &c2_len, &c2, SECP256K1_EC_COMPRESSED) == 1);

    /* Verify decryption works */
    uint64_t decrypted_amount;
    EXPECT(secp256k1_elgamal_decrypt(ctx, &decrypted_amount, &c1, &c2, privkey) == 1);
    EXPECT(decrypted_amount == amount);

    printf("    {\n");
    printf("      \"privkey\": \"");
    print_hex(privkey, 32);
    printf("\",\n");
    printf("      \"pubkeyQ\": \"");
    print_hex(pubkey_bytes, 33);
    printf("\",\n");
    printf("      \"amount\": \"%" PRIu64 "\",\n", amount);
    printf("      \"blindingFactor\": \"");
    print_hex(blinding_factor, 32);
    printf("\",\n");
    printf("      \"c1\": \"");
    print_hex(c1_bytes, 33);
    printf("\",\n");
    printf("      \"c2\": \"");
    print_hex(c2_bytes, 33);
    printf("\"\n");
    printf("    }%s\n", is_last ? "" : ",");
}

int main() {
    secp256k1_context* ctx = secp256k1_context_create(
            SECP256K1_CONTEXT_SIGN | SECP256K1_CONTEXT_VERIFY);
    EXPECT(ctx != NULL);

    unsigned char seed[32];
    EXPECT(RAND_bytes(seed, sizeof(seed)) == 1);
    EXPECT(secp256k1_context_randomize(ctx, seed) == 1);

    printf("{\n");
    printf("  \"description\": \"ElGamal decryption test vectors from C implementation\",\n");
    printf("  \"vectors\": [\n");

    /* Test case 1: amount = 0 */
    print_test_vector(ctx, 0, 0);

    /* Test case 2: amount = 1 */
    print_test_vector(ctx, 1, 0);

    /* Test case 3: amount = 750 */
    print_test_vector(ctx, 750, 0);

    /* Test case 4: amount = 12345 */
    print_test_vector(ctx, 12345, 0);

    /* Test case 5: amount = 1000000 (max default search range) */
    print_test_vector(ctx, 1000000, 0);

    print_test_vector(ctx, 78367, 1);

    printf("  ]\n");
    printf("}\n");

    secp256k1_context_destroy(ctx);
    return 0;
}

