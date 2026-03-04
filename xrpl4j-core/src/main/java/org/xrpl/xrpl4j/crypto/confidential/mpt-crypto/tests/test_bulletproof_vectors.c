#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <secp256k1.h>
#include <openssl/rand.h>
#include "secp256k1_mpt.h"
#include "test_utils.h"

#define BP_VALUE_BITS 64

/* Extern declaration for the run index setter */
extern void secp256k1_mpt_set_bp_nonce_run_index(int index);

static const unsigned char FIXED_NONCES[10][32] = {
    {0x03, 0x67, 0xE5, 0xC8, 0x4F, 0x43, 0x71, 0x4E, 0x13, 0xE3, 0x4E, 0xD2, 0x12, 0xF9, 0x25, 0xB3,
     0x75, 0xCD, 0x86, 0x3C, 0xAE, 0x46, 0xEF, 0xE3, 0xD1, 0x05, 0x65, 0x92, 0xC2, 0x55, 0x9D, 0xDA},
    {0xA2, 0x68, 0xEE, 0x37, 0xE2, 0x3C, 0x65, 0x6D, 0x05, 0xF4, 0x82, 0x03, 0xFA, 0x23, 0x8D, 0x41,
     0x92, 0x4C, 0x64, 0x57, 0x6E, 0x7F, 0x13, 0xA6, 0xD7, 0x29, 0x4D, 0x22, 0xCE, 0x5E, 0x24, 0x49},
    {0x6B, 0x51, 0xA9, 0xD4, 0xD9, 0xEA, 0xD9, 0xBE, 0x6C, 0x64, 0x0D, 0x37, 0x42, 0xC3, 0x29, 0x63,
     0x0C, 0xFB, 0xC0, 0x1A, 0x2D, 0x6A, 0x6D, 0xF1, 0x2D, 0x32, 0xCF, 0x60, 0x7E, 0xAD, 0xAC, 0x18},
    {0x3F, 0x98, 0x46, 0x20, 0xB0, 0x14, 0xF5, 0xE5, 0x7F, 0xFE, 0x80, 0x92, 0x4C, 0x08, 0x95, 0x26,
     0xAD, 0xE9, 0x3B, 0x87, 0xE0, 0x80, 0xDD, 0x2C, 0x39, 0xC5, 0xB7, 0xEB, 0xD1, 0xE1, 0xDC, 0x9F},
    {0x41, 0xC3, 0x04, 0xBE, 0x98, 0xE5, 0xEB, 0x03, 0x08, 0xF4, 0xC6, 0x21, 0xFE, 0x52, 0xA3, 0xEF,
     0x0E, 0x1C, 0xAE, 0x4E, 0xFC, 0xEB, 0xAC, 0x7E, 0x0F, 0xB6, 0x62, 0x75, 0xEA, 0x08, 0x3E, 0x87},
    {0x99, 0x55, 0xB9, 0x26, 0xFA, 0xA3, 0xEA, 0x9A, 0x07, 0x74, 0x6C, 0xA5, 0xC0, 0x12, 0xF2, 0xDD,
     0x10, 0xD4, 0x4C, 0xF7, 0x32, 0x17, 0x74, 0x6E, 0x5B, 0x37, 0x16, 0x3B, 0xA9, 0xCC, 0xA6, 0x2D},
    {0xFF, 0x2F, 0xC7, 0x43, 0xE0, 0x21, 0xC6, 0xA5, 0x20, 0x6E, 0x3E, 0x3E, 0x5A, 0x5C, 0xFA, 0x0C,
     0xE5, 0x42, 0x48, 0x2F, 0xF0, 0x21, 0x3A, 0x83, 0xD3, 0x57, 0xD1, 0xF0, 0xC2, 0x0C, 0xE8, 0xB7},
    {0x72, 0x0B, 0xF9, 0xF6, 0x37, 0x76, 0x70, 0xE0, 0x00, 0x11, 0xC9, 0x01, 0x52, 0x6A, 0x40, 0x5C,
     0xC7, 0x07, 0xBC, 0x92, 0x18, 0xB0, 0xE4, 0x40, 0x2E, 0x0E, 0x18, 0x21, 0x0F, 0x95, 0x3E, 0x54},
    {0x6F, 0xEA, 0x36, 0xEA, 0xD5, 0x9A, 0x20, 0xD9, 0xBE, 0x21, 0x57, 0x30, 0xB8, 0xD2, 0x99, 0x5B,
     0x67, 0x11, 0x27, 0x21, 0xB6, 0x7E, 0x1E, 0x14, 0xE3, 0xAF, 0xA8, 0x09, 0xE8, 0x3B, 0x28, 0x58},
    {0x1C, 0x7D, 0x65, 0x5F, 0x23, 0x2B, 0x29, 0x4A, 0x87, 0xBB, 0xC6, 0x37, 0x53, 0xDA, 0x3B, 0xD6,
     0xE6, 0xB4, 0xB7, 0x16, 0xF8, 0xD5, 0x5A, 0xF4, 0x5D, 0x9F, 0xF3, 0xEE, 0x11, 0xD0, 0xC8, 0xAA}
};

/* Test amounts for m=1 vectors */
static const uint64_t AMOUNTS_M1[5] = {
    1,                          /* Minimum non-zero */
    100,                        /* Small */
    555666,                     /* Medium */
    1000000000ULL,              /* Large */
    999999999999ULL             /* Very large */
};

/* Test amounts for m=2 vectors (pairs) */
static const uint64_t AMOUNTS_M2[5][2] = {
    {100,        400},
    {5000,       123456},
    {0,          1},
    {999999999,  1},
    {42,         42}
};

static void print_hex(const unsigned char* data, size_t len) {
    for (size_t i = 0; i < len; i++) {
        printf("%02X", data[i]);
    }
}

static void print_pubkey(const secp256k1_context* ctx, const secp256k1_pubkey* pk) {
    unsigned char buf[33];
    size_t len = 33;
    secp256k1_ec_pubkey_serialize(ctx, buf, &len, pk, SECP256K1_EC_COMPRESSED);
    print_hex(buf, 33);
}

/**
 * Generate a single m=1 test vector.
 */
static void generate_m1_vector(const secp256k1_context* ctx, int index, int is_last) {
    uint64_t value = AMOUNTS_M1[index];
    unsigned char blinding[32];
    unsigned char context_id[32];
    secp256k1_pubkey commitment;
    secp256k1_pubkey pk_base;

    /* Use deterministic blinding and context_id from fixed nonces */
    memcpy(blinding, FIXED_NONCES[(index + 1) % 10], 32);
    memcpy(context_id, FIXED_NONCES[(index + 2) % 10], 32);

    /* Set the nonce run index so all internal random scalars use FIXED_NONCES[index] */
    secp256k1_mpt_set_bp_nonce_run_index(index);

    /* Get H generator */
    EXPECT(secp256k1_mpt_get_h_generator(ctx, &pk_base));

    /* Create commitment */
    EXPECT(secp256k1_bulletproof_create_commitment(ctx, &commitment, value, blinding, &pk_base));

    /* Generate proof */
    unsigned char proof[4096];
    size_t proof_len = sizeof(proof);
    EXPECT(secp256k1_bulletproof_prove_agg(
        ctx, proof, &proof_len, &value, blinding, 1, &pk_base, context_id));

    /* Reset nonce index */
    secp256k1_mpt_set_bp_nonce_run_index(-1);

    /* Verify proof */
    size_t n = BP_VALUE_BITS;
    secp256k1_pubkey* G_vec = malloc(n * sizeof(secp256k1_pubkey));
    secp256k1_pubkey* H_vec = malloc(n * sizeof(secp256k1_pubkey));
    EXPECT(G_vec && H_vec);
    EXPECT(secp256k1_mpt_get_generator_vector(ctx, G_vec, n, (const unsigned char*)"G", 1));
    EXPECT(secp256k1_mpt_get_generator_vector(ctx, H_vec, n, (const unsigned char*)"H", 1));
    EXPECT(secp256k1_bulletproof_verify_agg(
        ctx, G_vec, H_vec, proof, proof_len, &commitment, 1, &pk_base, context_id));
    free(G_vec);
    free(H_vec);

    /* Output JSON */
    printf("    {\n");
    printf("      \"m\": 1,\n");
    printf("      \"values\": [%llu],\n", (unsigned long long)value);
    printf("      \"blindings\": [\""); print_hex(blinding, 32); printf("\"],\n");
    printf("      \"nonce\": \""); print_hex(FIXED_NONCES[index], 32); printf("\",\n");
    printf("      \"contextId\": \""); print_hex(context_id, 32); printf("\",\n");
    printf("      \"pkBase\": \""); print_pubkey(ctx, &pk_base); printf("\",\n");
    printf("      \"commitments\": [\""); print_pubkey(ctx, &commitment); printf("\"],\n");
    printf("      \"expectedProof\": \""); print_hex(proof, proof_len); printf("\"\n");
    printf("    }%s\n", is_last ? "" : ",");
}

/**
 * Generate a single m=2 test vector.
 */
static void generate_m2_vector(const secp256k1_context* ctx, int index, int is_last) {
    uint64_t values[2] = {AMOUNTS_M2[index][0], AMOUNTS_M2[index][1]};
    unsigned char blindings[2][32];
    unsigned char context_id[32];
    secp256k1_pubkey commitments[2];
    secp256k1_pubkey pk_base;

    /* Use deterministic blindings and context_id - offset by 5 to avoid reusing m=1 nonces */
    int nonce_base = (index + 5) % 10;
    memcpy(blindings[0], FIXED_NONCES[(nonce_base + 1) % 10], 32);
    memcpy(blindings[1], FIXED_NONCES[(nonce_base + 2) % 10], 32);
    memcpy(context_id, FIXED_NONCES[(nonce_base + 3) % 10], 32);

    /* Set the nonce run index */
    secp256k1_mpt_set_bp_nonce_run_index(nonce_base);

    /* Get H generator */
    EXPECT(secp256k1_mpt_get_h_generator(ctx, &pk_base));

    /* Create commitments */
    for (int i = 0; i < 2; i++) {
        EXPECT(secp256k1_bulletproof_create_commitment(
            ctx, &commitments[i], values[i], blindings[i], &pk_base));
    }

    /* Generate proof */
    unsigned char proof[4096];
    size_t proof_len = sizeof(proof);
    EXPECT(secp256k1_bulletproof_prove_agg(
        ctx, proof, &proof_len, values, (const unsigned char*)blindings, 2, &pk_base, context_id));

    /* Reset nonce index */
    secp256k1_mpt_set_bp_nonce_run_index(-1);

    /* Verify proof */
    size_t n = BP_VALUE_BITS * 2;
    secp256k1_pubkey* G_vec = malloc(n * sizeof(secp256k1_pubkey));
    secp256k1_pubkey* H_vec = malloc(n * sizeof(secp256k1_pubkey));
    EXPECT(G_vec && H_vec);
    EXPECT(secp256k1_mpt_get_generator_vector(ctx, G_vec, n, (const unsigned char*)"G", 1));
    EXPECT(secp256k1_mpt_get_generator_vector(ctx, H_vec, n, (const unsigned char*)"H", 1));
    EXPECT(secp256k1_bulletproof_verify_agg(
        ctx, G_vec, H_vec, proof, proof_len, commitments, 2, &pk_base, context_id));
    free(G_vec);
    free(H_vec);

    /* Output JSON */
    printf("    {\n");
    printf("      \"m\": 2,\n");
    printf("      \"values\": [%llu, %llu],\n",
           (unsigned long long)values[0], (unsigned long long)values[1]);
    printf("      \"blindings\": [\""); print_hex(blindings[0], 32);
    printf("\", \""); print_hex(blindings[1], 32); printf("\"],\n");
    printf("      \"nonce\": \""); print_hex(FIXED_NONCES[nonce_base], 32); printf("\",\n");
    printf("      \"contextId\": \""); print_hex(context_id, 32); printf("\",\n");
    printf("      \"pkBase\": \""); print_pubkey(ctx, &pk_base); printf("\",\n");
    printf("      \"commitments\": [\""); print_pubkey(ctx, &commitments[0]);
    printf("\", \""); print_pubkey(ctx, &commitments[1]); printf("\"],\n");
    printf("      \"expectedProof\": \""); print_hex(proof, proof_len); printf("\"\n");
    printf("    }%s\n", is_last ? "" : ",");
}

int main() {
    secp256k1_context* ctx = secp256k1_context_create(
        SECP256K1_CONTEXT_SIGN | SECP256K1_CONTEXT_VERIFY);
    EXPECT(ctx != NULL);

    printf("{\n");
    printf("  \"description\": \"Bulletproof range proof test vectors from C implementation\",\n");
    printf("  \"vectors\": [\n");

    /* 5 vectors with m=1 */
    for (int i = 0; i < 5; i++) {
        generate_m1_vector(ctx, i, 0);
    }

    /* 5 vectors with m=2 */
    for (int i = 0; i < 5; i++) {
        generate_m2_vector(ctx, i, i == 4);
    }

    printf("  ]\n");
    printf("}\n");

    secp256k1_context_destroy(ctx);
    return 0;
}
