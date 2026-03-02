#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <secp256k1.h>
#include "secp256k1_mpt.h"
#include "test_utils.h"

/* Extern declaration for the run index setter */
extern void secp256k1_mpt_set_nonce_run_index(int index);

/* Fixed values for deterministic test inputs */
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

static void print_hex_json(const unsigned char* data, size_t len) {
    for (size_t i = 0; i < len; i++) {
        printf("%02X", data[i]);
    }
}

static void print_pubkey_json(const secp256k1_context* ctx, const secp256k1_pubkey* pk) {
    unsigned char buf[33];
    size_t len = 33;
    secp256k1_ec_pubkey_serialize(ctx, buf, &len, pk, SECP256K1_EC_COMPRESSED);
    print_hex_json(buf, 33);
}

/**
 * Generate test vectors with fixed inputs in JSON format.
 * Uses FIXED_NONCES[run_number] for km and all kr_i.
 * @param n_participants Number of participants (2 or 3)
 */
static void generate_test_vector_json(const secp256k1_context* ctx, int run_number, size_t n_participants, int is_last) {
    secp256k1_pubkey Pk[3], R[3], S[3];
    unsigned char sk[3][32];  // Private keys
    unsigned char r[3][32];   // Blinding factors for encryption
    unsigned char tx_id[32];
    uint64_t amount = 555666;
    size_t i;

    // Set the run index so the proof generator uses the correct nonce
    secp256k1_mpt_set_nonce_run_index(run_number);

    // Use deterministic values derived from run_number
    for (i = 0; i < n_participants; i++) {
        memcpy(sk[i], FIXED_NONCES[(run_number + 1 + i) % 10], 32);
        memcpy(r[i], FIXED_NONCES[(run_number + 1 + n_participants + i) % 10], 32);
    }
    memcpy(tx_id, FIXED_NONCES[(run_number + 1 + 2 * n_participants) % 10], 32);

    // Generate public keys from private keys
    for (i = 0; i < n_participants; i++) {
        EXPECT(secp256k1_ec_pubkey_create(ctx, &Pk[i], sk[i]) == 1);
    }

    // Encrypt the same amount with all keys
    for (i = 0; i < n_participants; i++) {
        EXPECT(secp256k1_elgamal_encrypt(ctx, &R[i], &S[i], &Pk[i], amount, r[i]) == 1);
    }

    // Generate proof
    size_t proof_len = secp256k1_mpt_prove_same_plaintext_multi_size(n_participants);
    unsigned char* proof = (unsigned char*)malloc(proof_len);
    EXPECT(proof != NULL);

    unsigned char r_flat[96];  // Max 3 * 32
    for (i = 0; i < n_participants; i++) {
        memcpy(&r_flat[i * 32], r[i], 32);
    }

    size_t out_len = proof_len;
    EXPECT(secp256k1_mpt_prove_same_plaintext_multi(
            ctx, proof, &out_len, amount, n_participants,
            R, S, Pk, r_flat, tx_id
    ) == 1);

    // Verify
    int verify_result = secp256k1_mpt_verify_same_plaintext_multi(
            ctx, proof, proof_len, n_participants, R, S, Pk, tx_id);
    EXPECT(verify_result == 1);

    // Output JSON
    printf("    {\n");
    printf("      \"amount\": %llu,\n", (unsigned long long)amount);
    printf("      \"nonce\": \""); print_hex_json(FIXED_NONCES[run_number], 32); printf("\",\n");

    // Print Pk array
    printf("      \"Pk\": [");
    for (i = 0; i < n_participants; i++) {
        printf("\""); print_pubkey_json(ctx, &Pk[i]); printf("\"");
        if (i < n_participants - 1) printf(", ");
    }
    printf("],\n");

    // Print R array
    printf("      \"R\": [");
    for (i = 0; i < n_participants; i++) {
        printf("\""); print_pubkey_json(ctx, &R[i]); printf("\"");
        if (i < n_participants - 1) printf(", ");
    }
    printf("],\n");

    // Print S array
    printf("      \"S\": [");
    for (i = 0; i < n_participants; i++) {
        printf("\""); print_pubkey_json(ctx, &S[i]); printf("\"");
        if (i < n_participants - 1) printf(", ");
    }
    printf("],\n");

    // Print rArray
    printf("      \"rArray\": [");
    for (i = 0; i < n_participants; i++) {
        printf("\""); print_hex_json(r[i], 32); printf("\"");
        if (i < n_participants - 1) printf(", ");
    }
    printf("],\n");

    printf("      \"contextId\": \""); print_hex_json(tx_id, 32); printf("\",\n");
    printf("      \"expectedProof\": \""); print_hex_json(proof, proof_len); printf("\"\n");
    printf("    }%s\n", is_last ? "" : ",");

    free(proof);
}

int main() {
    secp256k1_context* ctx = secp256k1_context_create(
            SECP256K1_CONTEXT_SIGN | SECP256K1_CONTEXT_VERIFY);
    EXPECT(ctx != NULL);

    // Output JSON header
    printf("{\n");
    printf("  \"description\": \"Same plaintext multi proof test vectors from C implementation\",\n");
    printf("  \"vectors\": [\n");

    // Generate 5 test vectors with 2 participants
    for (int i = 0; i < 5; i++) {
        generate_test_vector_json(ctx, i, 2, 0);  // not last
    }

    // Generate 5 test vectors with 3 participants
    for (int i = 5; i < 10; i++) {
        generate_test_vector_json(ctx, i, 3, i == 9);  // last one at i=9
    }

    // Close JSON
    printf("  ]\n");
    printf("}\n");

    secp256k1_context_destroy(ctx);
    return 0;
}

