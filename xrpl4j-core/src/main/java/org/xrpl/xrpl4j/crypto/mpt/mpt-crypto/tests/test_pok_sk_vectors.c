#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <secp256k1.h>
#include <openssl/rand.h>
#include "secp256k1_mpt.h"
#include "test_utils.h"

static void print_hex(const unsigned char* data, size_t len) {
    for (size_t i = 0; i < len; i++) {
        printf("%02X", data[i]);
    }
}

static void print_test_vector(const secp256k1_context* ctx, int with_context, int is_last) {
    unsigned char sk[32];
    unsigned char context_id[32];
    unsigned char proof[65];
    secp256k1_pubkey pk;
    unsigned char pk_bytes[33];
    size_t pk_len = 33;

    // Generate random sk
    do {
        EXPECT(RAND_bytes(sk, 32) == 1);
    } while (!secp256k1_ec_seckey_verify(ctx, sk));

    // Derive pk from sk
    EXPECT(secp256k1_ec_pubkey_create(ctx, &pk, sk) == 1);
    EXPECT(secp256k1_ec_pubkey_serialize(ctx, pk_bytes, &pk_len, &pk, SECP256K1_EC_COMPRESSED) == 1);

    // Generate random context_id if needed
    if (with_context) {
        EXPECT(RAND_bytes(context_id, 32) == 1);
    }

    // Generate proof - this will print NONCE_K to stderr
    EXPECT(secp256k1_mpt_pok_sk_prove(ctx, proof, &pk, sk, with_context ? context_id : NULL) == 1);

    // Print JSON
    printf("    {\n");
    printf("      \"sk\": \""); print_hex(sk, 32); printf("\",\n");
    printf("      \"pk\": \""); print_hex(pk_bytes, 33); printf("\",\n");
    if (with_context) {
        printf("      \"contextId\": \""); print_hex(context_id, 32); printf("\",\n");
    } else {
        printf("      \"contextId\": null,\n");
    }
    printf("      \"expectedProof\": \""); print_hex(proof, 65); printf("\"\n");
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
    printf("  \"description\": \"Secret key proof test vectors from C implementation\",\n");
    printf("  \"vectors\": [\n");

    // 5 vectors without context
    for (int i = 0; i < 5; i++) {
        print_test_vector(ctx, 0, 0);
    }

    // 5 vectors with context
    for (int i = 0; i < 4; i++) {
        print_test_vector(ctx, 1, 0);
    }
    print_test_vector(ctx, 1, 1);

    printf("  ]\n");
    printf("}\n");

    secp256k1_context_destroy(ctx);
    return 0;
}

