#include "secp256k1_mpt.h"
#include <openssl/sha.h>
#include <openssl/rand.h>
#include <string.h>
#include <assert.h>

/**
 * Generates a cryptographically secure 32-byte scalar (private key).
 * Returns 1 on success, 0 on failure.
 */
static int generate_random_scalar(
        const secp256k1_context* ctx,
        unsigned char* scalar_bytes)
{
    do {
        if (RAND_bytes(scalar_bytes, 32) != 1) {
            return 0; // Randomness failure
        }
    } while (secp256k1_ec_seckey_verify(ctx, scalar_bytes) != 1);
    return 1;
}

/**
 * Computes the point M = amount * G.
 * Returns 1 on success, 0 on failure. Assumes amount > 0.
 */
static int compute_amount_point(
        const secp256k1_context* ctx,
        secp256k1_pubkey* mG,
        uint64_t amount)
{
    unsigned char amount_scalar[32] = {0};
    assert(amount != 0); // This proof is for non-zero amounts

    /* Convert amount to 32-byte BIG-ENDIAN scalar */
    for (int i = 0; i < 8; ++i) {
        amount_scalar[31 - i] = (amount >> (i * 8)) & 0xFF;
    }
    return secp256k1_ec_pubkey_create(ctx, mG, amount_scalar);
}

/**
 * Builds the challenge hash input.
 * Format: DomainSep || 6 points (33*6) || 5 points (33*5) || TxID (32)
 * Total size = 30 + 198 + 165 + 32 = 425 bytes
 */
static void build_same_plaintext_hash_input(
        unsigned char hash_input[423],
        const secp256k1_pubkey* R1, const secp256k1_pubkey* S1, const secp256k1_pubkey* P1,
        const secp256k1_pubkey* R2, const secp256k1_pubkey* S2, const secp256k1_pubkey* P2,
        const secp256k1_pubkey* T_m, const secp256k1_pubkey* T_r1_G,
        const secp256k1_pubkey* T_r1_P1, const secp256k1_pubkey* T_r2_G,
        const secp256k1_pubkey* T_r2_P2,
        const unsigned char* tx_context_id)
{
    const char* domain_sep = "MPT_POK_SAME_PLAINTEXT_PROOF"; // 30 bytes
    size_t offset = 0;
    size_t len = 33; // All points are 33 bytes compressed
    secp256k1_context* ser_ctx = secp256k1_context_create(SECP256K1_CONTEXT_NONE);

    memcpy(hash_input + offset, domain_sep, strlen(domain_sep));
    offset += strlen(domain_sep);

    // 6 Public Inputs
    len = 33; secp256k1_ec_pubkey_serialize(ser_ctx, hash_input + offset, &len, R1, SECP256K1_EC_COMPRESSED); offset += len;
    len = 33; secp256k1_ec_pubkey_serialize(ser_ctx, hash_input + offset, &len, S1, SECP256K1_EC_COMPRESSED); offset += len;
    len = 33; secp256k1_ec_pubkey_serialize(ser_ctx, hash_input + offset, &len, P1, SECP256K1_EC_COMPRESSED); offset += len;
    len = 33; secp256k1_ec_pubkey_serialize(ser_ctx, hash_input + offset, &len, R2, SECP256K1_EC_COMPRESSED); offset += len;
    len = 33; secp256k1_ec_pubkey_serialize(ser_ctx, hash_input + offset, &len, S2, SECP256K1_EC_COMPRESSED); offset += len;
    len = 33; secp256k1_ec_pubkey_serialize(ser_ctx, hash_input + offset, &len, P2, SECP256K1_EC_COMPRESSED); offset += len;

    // 5 Commitment Points
    len = 33; secp256k1_ec_pubkey_serialize(ser_ctx, hash_input + offset, &len, T_m, SECP256K1_EC_COMPRESSED); offset += len;
    len = 33; secp256k1_ec_pubkey_serialize(ser_ctx, hash_input + offset, &len, T_r1_G, SECP256K1_EC_COMPRESSED); offset += len;
    len = 33; secp256k1_ec_pubkey_serialize(ser_ctx, hash_input + offset, &len, T_r1_P1, SECP256K1_EC_COMPRESSED); offset += len;
    len = 33; secp256k1_ec_pubkey_serialize(ser_ctx, hash_input + offset, &len, T_r2_G, SECP256K1_EC_COMPRESSED); offset += len;
    len = 33; secp256k1_ec_pubkey_serialize(ser_ctx, hash_input + offset, &len, T_r2_P2, SECP256K1_EC_COMPRESSED); offset += len;

    // Transaction Context
    memcpy(hash_input + offset, tx_context_id, 32); offset += 32;

    assert(offset == 423);
    secp256k1_context_destroy(ser_ctx);
}


int secp256k1_mpt_prove_same_plaintext(
        const secp256k1_context* ctx,
        unsigned char* proof_out,          // Output: 261 bytes
        const secp256k1_pubkey* R1, const secp256k1_pubkey* S1, const secp256k1_pubkey* P1,
        const secp256k1_pubkey* R2, const secp256k1_pubkey* S2, const secp256k1_pubkey* P2,
        uint64_t amount_m,
        const unsigned char* randomness_r1,
        const unsigned char* randomness_r2,
        const unsigned char* tx_context_id
) {

    unsigned char k_m[32], k_r1[32], k_r2[32];
    unsigned char m_scalar[32] = {0};
    unsigned char e_scalar[32];
    unsigned char s_m[32], s_r1[32], s_r2[32];
    unsigned char em[32], er1[32], er2[32];
    secp256k1_pubkey T_m, T_r1_G, T_r1_P1, T_r2_G, T_r2_P2;
    unsigned char hash_input[423];
    size_t len;
    int all_ok = 1;

    /* 1. Generate random blinding scalars */
    if (!generate_random_scalar(ctx, k_m)) all_ok = 0;
    if (all_ok && !generate_random_scalar(ctx, k_r1)) all_ok = 0;
    if (all_ok && !generate_random_scalar(ctx, k_r2)) all_ok = 0;

    /* 2. Compute Commitments */
    if (all_ok) {
        if (!secp256k1_ec_pubkey_create(ctx, &T_m, k_m)) all_ok = 0;
        if (!secp256k1_ec_pubkey_create(ctx, &T_r1_G, k_r1)) all_ok = 0;
        if (!secp256k1_ec_pubkey_create(ctx, &T_r2_G, k_r2)) all_ok = 0;

        T_r1_P1 = *P1;
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &T_r1_P1, k_r1)) all_ok = 0;
        T_r2_P2 = *P2;
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &T_r2_P2, k_r2)) all_ok = 0;
    }

    /* 3. Compute Challenge */
    if (all_ok) {
        build_same_plaintext_hash_input(
                hash_input, R1, S1, P1, R2, S2, P2,
                &T_m, &T_r1_G, &T_r1_P1, &T_r2_G, &T_r2_P2,
                tx_context_id
        );
        // Using SHA256 as placeholder for SHA-512Half
        SHA256(hash_input, sizeof(hash_input), e_scalar);
        if (!secp256k1_ec_seckey_verify(ctx, e_scalar)) all_ok = 0;
    }

    /* 4. Compute Responses s = (k + e*x) */
    if (all_ok) {
        /* Convert amount m to big-endian scalar */
        for (int i = 0; i < 8; ++i) {
            m_scalar[31 - i] = (amount_m >> (i * 8)) & 0xFF;
        }

        /* s_m = k_m + e*m */
        memcpy(em, m_scalar, 32);
        if (!secp256k1_ec_seckey_tweak_mul(ctx, em, e_scalar)) all_ok = 0;
        memcpy(s_m, k_m, 32);
        if (!secp256k1_ec_seckey_tweak_add(ctx, s_m, em)) all_ok = 0;

        /* s_r1 = k_r1 + e*r1 */
        memcpy(er1, randomness_r1, 32);
        if (!secp256k1_ec_seckey_tweak_mul(ctx, er1, e_scalar)) all_ok = 0;
        memcpy(s_r1, k_r1, 32);
        if (!secp256k1_ec_seckey_tweak_add(ctx, s_r1, er1)) all_ok = 0;

        /* s_r2 = k_r2 + e*r2 */
        memcpy(er2, randomness_r2, 32);
        if (!secp256k1_ec_seckey_tweak_mul(ctx, er2, e_scalar)) all_ok = 0;
        memcpy(s_r2, k_r2, 32);
        if (!secp256k1_ec_seckey_tweak_add(ctx, s_r2, er2)) all_ok = 0;
    }

    /* 5. Serialize proof */
    if (all_ok) {
        len = 33; secp256k1_ec_pubkey_serialize(ctx, proof_out + 0,   &len, &T_m, SECP256K1_EC_COMPRESSED);
        len = 33; secp256k1_ec_pubkey_serialize(ctx, proof_out + 33,  &len, &T_r1_G, SECP256K1_EC_COMPRESSED);
        len = 33; secp256k1_ec_pubkey_serialize(ctx, proof_out + 66,  &len, &T_r1_P1, SECP256K1_EC_COMPRESSED);
        len = 33; secp256k1_ec_pubkey_serialize(ctx, proof_out + 99,  &len, &T_r2_G, SECP256K1_EC_COMPRESSED);
        len = 33; secp256k1_ec_pubkey_serialize(ctx, proof_out + 132, &len, &T_r2_P2, SECP256K1_EC_COMPRESSED);
        memcpy(proof_out + 165, s_m, 32);
        memcpy(proof_out + 197, s_r1, 32);
        memcpy(proof_out + 229, s_r2, 32);
        assert(165 + 32 + 32 + 32 == 261);
    }

    /* 6. Clear secrets */
    memset(k_m, 0, 32);
    memset(k_r1, 0, 32);
    memset(k_r2, 0, 32);
    memset(m_scalar, 0, 32);
    memset(s_m, 0, 32);
    memset(s_r1, 0, 32);
    memset(s_r2, 0, 32);
    memset(em, 0, 32);
    memset(er1, 0, 32);
    memset(er2, 0, 32);

    return all_ok;
}

int secp256k1_mpt_verify_same_plaintext(
        const secp256k1_context* ctx,
        const unsigned char* proof,        // Input: 261 bytes
        const secp256k1_pubkey* R1, const secp256k1_pubkey* S1, const secp256k1_pubkey* P1,
        const secp256k1_pubkey* R2, const secp256k1_pubkey* S2, const secp256k1_pubkey* P2,
        const unsigned char* tx_context_id
) {
    /* C90 Declarations */
    secp256k1_pubkey T_m, T_r1_G, T_r1_P1, T_r2_G, T_r2_P2;
    unsigned char s_m[32], s_r1[32], s_r2[32];
    unsigned char e_scalar[32];
    unsigned char hash_input[423];
    int all_ok = 1;

    secp256k1_pubkey lhs, rhs, term1, term2, term3;
    const secp256k1_pubkey* points_to_add[3];
    unsigned char lhs_bytes[33], rhs_bytes[33];
    size_t len;

    /* 1. Deserialize proof */
    if (all_ok) {
        if (secp256k1_ec_pubkey_parse(ctx, &T_m,    proof + 0,   33) != 1) all_ok = 0;
        if (secp256k1_ec_pubkey_parse(ctx, &T_r1_G, proof + 33,  33) != 1) all_ok = 0;
        if (secp256k1_ec_pubkey_parse(ctx, &T_r1_P1, proof + 66, 33) != 1) all_ok = 0;
        if (secp256k1_ec_pubkey_parse(ctx, &T_r2_G, proof + 99,  33) != 1) all_ok = 0;
        if (secp256k1_ec_pubkey_parse(ctx, &T_r2_P2, proof + 132, 33) != 1) all_ok = 0;

        memcpy(s_m, proof + 165, 32);
        memcpy(s_r1, proof + 197, 32);
        memcpy(s_r2, proof + 229, 32);

        if (!secp256k1_ec_seckey_verify(ctx, s_m)) all_ok = 0;
        if (!secp256k1_ec_seckey_verify(ctx, s_r1)) all_ok = 0;
        if (!secp256k1_ec_seckey_verify(ctx, s_r2)) all_ok = 0;
    }

    /* 2. Compute Challenge */
    if (all_ok) {
        build_same_plaintext_hash_input(
                hash_input, R1, S1, P1, R2, S2, P2,
                &T_m, &T_r1_G, &T_r1_P1, &T_r2_G, &T_r2_P2,
                tx_context_id
        );
        SHA256(hash_input, sizeof(hash_input), e_scalar);
        if (!secp256k1_ec_seckey_verify(ctx, e_scalar)) all_ok = 0;
    }

    /* 3. Check Eq 1: s_r1 * G == T_r1_G + e * R1 */
    if (all_ok) {
        if (!secp256k1_ec_pubkey_create(ctx, &lhs, s_r1)) all_ok = 0;

        term1 = *R1;
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &term1, e_scalar)) all_ok = 0;
        points_to_add[0] = &T_r1_G; points_to_add[1] = &term1;
        if (!secp256k1_ec_pubkey_combine(ctx, &rhs, points_to_add, 2)) all_ok = 0;

        len = 33; secp256k1_ec_pubkey_serialize(ctx, lhs_bytes, &len, &lhs, SECP256K1_EC_COMPRESSED);
        len = 33; secp256k1_ec_pubkey_serialize(ctx, rhs_bytes, &len, &rhs, SECP256K1_EC_COMPRESSED);
        if (memcmp(lhs_bytes, rhs_bytes, 33) != 0) all_ok = 0;
    }

    /* 4. Check Eq 2: s_m * G + s_r1 * P1 == T_m + T_r1_P1 + e * S1 */
    if (all_ok) {
        if (!secp256k1_ec_pubkey_create(ctx, &term1, s_m)) all_ok = 0;
        term2 = *P1;
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &term2, s_r1)) all_ok = 0;
        points_to_add[0] = &term1; points_to_add[1] = &term2;
        if (!secp256k1_ec_pubkey_combine(ctx, &lhs, points_to_add, 2)) all_ok = 0;

        term3 = *S1;
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &term3, e_scalar)) all_ok = 0;
        points_to_add[0] = &T_m; points_to_add[1] = &T_r1_P1; points_to_add[2] = &term3;
        if (!secp256k1_ec_pubkey_combine(ctx, &rhs, points_to_add, 3)) all_ok = 0;

        len = 33; secp256k1_ec_pubkey_serialize(ctx, lhs_bytes, &len, &lhs, SECP256K1_EC_COMPRESSED);
        len = 33; secp256k1_ec_pubkey_serialize(ctx, rhs_bytes, &len, &rhs, SECP256K1_EC_COMPRESSED);
        if (memcmp(lhs_bytes, rhs_bytes, 33) != 0) all_ok = 0;
    }

    /* 5. Check Eq 3: s_r2 * G == T_r2_G + e * R2 */
    if (all_ok) {
        if (!secp256k1_ec_pubkey_create(ctx, &lhs, s_r2)) all_ok = 0;

        term1 = *R2;
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &term1, e_scalar)) all_ok = 0;
        points_to_add[0] = &T_r2_G; points_to_add[1] = &term1;
        if (!secp256k1_ec_pubkey_combine(ctx, &rhs, points_to_add, 2)) all_ok = 0;

        len = 33; secp256k1_ec_pubkey_serialize(ctx, lhs_bytes, &len, &lhs, SECP256K1_EC_COMPRESSED);
        len = 33; secp256k1_ec_pubkey_serialize(ctx, rhs_bytes, &len, &rhs, SECP256K1_EC_COMPRESSED);
        if (memcmp(lhs_bytes, rhs_bytes, 33) != 0) all_ok = 0;
    }

    /* 6. Check Eq 4: s_m * G + s_r2 * P2 == T_m + T_r2_P2 + e * S2 */
    if (all_ok) {
        if (!secp256k1_ec_pubkey_create(ctx, &term1, s_m)) all_ok = 0;
        term2 = *P2;
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &term2, s_r2)) all_ok = 0;
        points_to_add[0] = &term1; points_to_add[1] = &term2;
        if (!secp256k1_ec_pubkey_combine(ctx, &lhs, points_to_add, 2)) all_ok = 0;

        term3 = *S2;
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &term3, e_scalar)) all_ok = 0;
        points_to_add[0] = &T_m; points_to_add[1] = &T_r2_P2; points_to_add[2] = &term3;
        if (!secp256k1_ec_pubkey_combine(ctx, &rhs, points_to_add, 3)) all_ok = 0;

        len = 33; secp256k1_ec_pubkey_serialize(ctx, lhs_bytes, &len, &lhs, SECP256K1_EC_COMPRESSED);
        len = 33; secp256k1_ec_pubkey_serialize(ctx, rhs_bytes, &len, &rhs, SECP256K1_EC_COMPRESSED);
        if (memcmp(lhs_bytes, rhs_bytes, 33) != 0) all_ok = 0;
    }

    return all_ok;
}