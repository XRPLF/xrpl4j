#include "secp256k1_mpt.h"
#include <openssl/sha.h>
#include <openssl/rand.h>
#include <string.h>
#include <assert.h>

//Internal Helper Functions

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
 * IMPORTANT: This function MUST NOT be called with amount = 0.
 */
static int compute_amount_point(
        const secp256k1_context* ctx,
        secp256k1_pubkey* mG,
        uint64_t amount)
{
    unsigned char amount_scalar[32] = {0};
    /* This function assumes amount != 0 */
    assert(amount != 0);

    /* Convert amount to 32-byte BIG-ENDIAN scalar */
    for (int i = 0; i < 8; ++i) {
        amount_scalar[31 - i] = (amount >> (i * 8)) & 0xFF;
    }
    return secp256k1_ec_pubkey_create(ctx, mG, amount_scalar);
}


/**
 * Builds the challenge hash input for the NON-ZERO amount case.
 * Format: DomainSep || C1(33) || C2(33) || Pk(33) || mG(33) || T1(33) || T2(33) || TxID(32)
 * Total size = 23 + 33*6 + 32 = 253 bytes
 */
static void build_challenge_hash_input_nonzero(
        unsigned char hash_input[253],
        const secp256k1_pubkey* c1, const secp256k1_pubkey* c2,
        const secp256k1_pubkey* pk, const secp256k1_pubkey* mG,
        const secp256k1_pubkey* T1, const secp256k1_pubkey* T2,
        const unsigned char* tx_context_id)
{
    const char* domain_sep = "MPT_POK_PLAINTEXT_PROOF"; // 23 bytes
    size_t offset = 0;
    size_t len;
    secp256k1_context* ser_ctx = secp256k1_context_create(SECP256K1_CONTEXT_NONE);

    memcpy(hash_input + offset, domain_sep, strlen(domain_sep));
    offset += strlen(domain_sep);

    len = 33; secp256k1_ec_pubkey_serialize(ser_ctx, hash_input + offset, &len, c1, SECP256K1_EC_COMPRESSED); offset += len;
    len = 33; secp256k1_ec_pubkey_serialize(ser_ctx, hash_input + offset, &len, c2, SECP256K1_EC_COMPRESSED); offset += len;
    len = 33; secp256k1_ec_pubkey_serialize(ser_ctx, hash_input + offset, &len, pk, SECP256K1_EC_COMPRESSED); offset += len;
    len = 33; secp256k1_ec_pubkey_serialize(ser_ctx, hash_input + offset, &len, mG, SECP256K1_EC_COMPRESSED); offset += len;
    len = 33; secp256k1_ec_pubkey_serialize(ser_ctx, hash_input + offset, &len, T1, SECP256K1_EC_COMPRESSED); offset += len;
    len = 33; secp256k1_ec_pubkey_serialize(ser_ctx, hash_input + offset, &len, T2, SECP256K1_EC_COMPRESSED); offset += len;

    memcpy(hash_input + offset, tx_context_id, 32); offset += 32;

    assert(offset == 253);
    secp256k1_context_destroy(ser_ctx);
}

/**
 * Builds the challenge hash input for the ZERO amount case.
 * Format: DomainSep || C1(33) || C2(33) || Pk(33) || T1(33) || T2(33) || TxID(32)
 * Total size = 23 + 33*5 + 32 = 220 bytes
 */
static void build_challenge_hash_input_zero(
        unsigned char hash_input[220],
        const secp256k1_pubkey* c1, const secp256k1_pubkey* c2,
        const secp256k1_pubkey* pk,
        const secp256k1_pubkey* T1, const secp256k1_pubkey* T2,
        const unsigned char* tx_context_id)
{
    const char* domain_sep = "MPT_POK_PLAINTEXT_PROOF"; // 23 bytes
    size_t offset = 0;
    size_t len;
    secp256k1_context* ser_ctx = secp256k1_context_create(SECP256K1_CONTEXT_NONE);

    memcpy(hash_input + offset, domain_sep, strlen(domain_sep));
    offset += strlen(domain_sep);

    len = 33; secp256k1_ec_pubkey_serialize(ser_ctx, hash_input + offset, &len, c1, SECP256K1_EC_COMPRESSED); offset += len;
    len = 33; secp256k1_ec_pubkey_serialize(ser_ctx, hash_input + offset, &len, c2, SECP256K1_EC_COMPRESSED); offset += len;
    len = 33; secp256k1_ec_pubkey_serialize(ser_ctx, hash_input + offset, &len, pk, SECP256K1_EC_COMPRESSED); offset += len;
    len = 33; secp256k1_ec_pubkey_serialize(ser_ctx, hash_input + offset, &len, T1, SECP256K1_EC_COMPRESSED); offset += len;
    len = 33; secp256k1_ec_pubkey_serialize(ser_ctx, hash_input + offset, &len, T2, SECP256K1_EC_COMPRESSED); offset += len;

    memcpy(hash_input + offset, tx_context_id, 32); offset += 32;

    assert(offset == 220);
    secp256k1_context_destroy(ser_ctx);
}


int secp256k1_equality_plaintext_prove(
        const secp256k1_context* ctx,
        unsigned char* proof,
        const secp256k1_pubkey* c1,
        const secp256k1_pubkey* c2,
        const secp256k1_pubkey* pk_recipient,
        uint64_t amount,
        const unsigned char* randomness_r,
        const unsigned char* tx_context_id)
{
    /* C90 Declarations */
    unsigned char t_scalar[32];
    unsigned char e_scalar[32];
    unsigned char s_scalar[32];
    unsigned char er_scalar[32];
    secp256k1_pubkey T1, T2;
    size_t len;

    /* Executable Code */

    /* 1. Generate random scalar t */
    if (!generate_random_scalar(ctx, t_scalar)) return 0;

    /* 2. Compute commitments T1 = t*G, T2 = t*Pk */
    if (!secp256k1_ec_pubkey_create(ctx, &T1, t_scalar)) {
        memset(t_scalar, 0, 32); return 0;
    }
    T2 = *pk_recipient;
    if (!secp256k1_ec_pubkey_tweak_mul(ctx, &T2, t_scalar)) {
        memset(t_scalar, 0, 32); return 0;
    }

    /* 3. Compute challenge e = H(...) */
    if (amount == 0) {
        unsigned char hash_input[220];
        build_challenge_hash_input_zero(hash_input, c1, c2, pk_recipient, &T1, &T2, tx_context_id);
        SHA256(hash_input, sizeof(hash_input), e_scalar);
    } else {
        secp256k1_pubkey mG;
        unsigned char hash_input[253];
        if (!compute_amount_point(ctx, &mG, amount)) {
            memset(t_scalar, 0, 32); return 0;
        }
        build_challenge_hash_input_nonzero(hash_input, c1, c2, pk_recipient, &mG, &T1, &T2, tx_context_id);
        SHA256(hash_input, sizeof(hash_input), e_scalar);
    }

    /* Ensure e is a valid scalar */
    if (!secp256k1_ec_seckey_verify(ctx, e_scalar)) {
        memset(t_scalar, 0, 32); return 0;
    }

    /* 4. Compute s = (t + e*r) mod q */
    memcpy(er_scalar, randomness_r, 32);
    if (!secp256k1_ec_seckey_tweak_mul(ctx, er_scalar, e_scalar)) {
        memset(t_scalar, 0, 32); return 0;
    }
    memcpy(s_scalar, t_scalar, 32);
    if (!secp256k1_ec_seckey_tweak_add(ctx, s_scalar, er_scalar)) {
        memset(t_scalar, 0, 32); return 0;
    }

    /* 5. Format the proof = T1(33) || T2(33) || s(32) */
    len = 33; secp256k1_ec_pubkey_serialize(ctx, proof,      &len, &T1, SECP256K1_EC_COMPRESSED);
    len = 33; secp256k1_ec_pubkey_serialize(ctx, proof + 33, &len, &T2, SECP256K1_EC_COMPRESSED);
    memcpy(proof + 66, s_scalar, 32);

    /* 6. Clear secret data */
    memset(t_scalar, 0, 32);
    memset(s_scalar, 0, 32);
    memset(er_scalar, 0, 32);

    return 1;
}

int secp256k1_equality_plaintext_verify(
        const secp256k1_context* ctx,
        const unsigned char* proof,
        const secp256k1_pubkey* c1,
        const secp256k1_pubkey* c2,
        const secp256k1_pubkey* pk_recipient,
        uint64_t amount,
        const unsigned char* tx_context_id)
{
    /* C90 Declarations */
    secp256k1_pubkey T1, T2;
    unsigned char s_scalar[32];
    unsigned char e_scalar[32];
    secp256k1_pubkey lhs_eq1, rhs_eq1_term2, rhs_eq1;
    secp256k1_pubkey lhs_eq2, rhs_eq2, rhs_eq2_term2_base;
    const secp256k1_pubkey* points_to_add[2];
    unsigned char lhs_bytes[33], rhs_bytes[33];
    size_t len;

    /* Executable Code */

    /* 1. Deserialize proof into T1 (33), T2 (33), s_scalar (32) */
    if (secp256k1_ec_pubkey_parse(ctx, &T1, proof,      33) != 1) return 0;
    if (secp256k1_ec_pubkey_parse(ctx, &T2, proof + 33, 33) != 1) return 0;
    memcpy(s_scalar, proof + 66, 32);
    if (!secp256k1_ec_seckey_verify(ctx, s_scalar)) return 0; /* s cannot be 0 */

    /* 2. Recompute challenge e' = H(...) */
    if (amount == 0) {
        unsigned char hash_input[220];
        build_challenge_hash_input_zero(hash_input, c1, c2, pk_recipient, &T1, &T2, tx_context_id);
        SHA256(hash_input, sizeof(hash_input), e_scalar);
    } else {
        secp256k1_pubkey mG;
        unsigned char hash_input[253];
        if (!compute_amount_point(ctx, &mG, amount)) return 0;
        build_challenge_hash_input_nonzero(hash_input, c1, c2, pk_recipient, &mG, &T1, &T2, tx_context_id);
        SHA256(hash_input, sizeof(hash_input), e_scalar);
    }
    if (!secp256k1_ec_seckey_verify(ctx, e_scalar)) return 0; /* e cannot be 0 */


    /* 3. Check Equation 1: s*G == T1 + e'*C1 */
    if (!secp256k1_ec_pubkey_create(ctx, &lhs_eq1, s_scalar)) return 0;
    rhs_eq1_term2 = *c1;
    if (!secp256k1_ec_pubkey_tweak_mul(ctx, &rhs_eq1_term2, e_scalar)) return 0;
    points_to_add[0] = &T1; points_to_add[1] = &rhs_eq1_term2;
    if (!secp256k1_ec_pubkey_combine(ctx, &rhs_eq1, points_to_add, 2)) return 0;

    len = 33; secp256k1_ec_pubkey_serialize(ctx, lhs_bytes, &len, &lhs_eq1, SECP256K1_EC_COMPRESSED);
    len = 33; secp256k1_ec_pubkey_serialize(ctx, rhs_bytes, &len, &rhs_eq1, SECP256K1_EC_COMPRESSED);
    if (memcmp(lhs_bytes, rhs_bytes, 33) != 0) return 0; // Eq 1 failed

    /* 4. Check Equation 2: s*Pk == T2 + e'*Y */
    /* 4a. LHS = s*Pk */
    lhs_eq2 = *pk_recipient;
    if (!secp256k1_ec_pubkey_tweak_mul(ctx, &lhs_eq2, s_scalar)) return 0;

    /* 4b. Define Y (the base for the second part of the proof) */
    if (amount == 0) {
        rhs_eq2_term2_base = *c2; // Y = C2
    } else {
        secp256k1_pubkey mG;
        compute_amount_point(ctx, &mG, amount);
        if (!secp256k1_ec_pubkey_negate(ctx, &mG)) return 0;
        points_to_add[0] = c2; points_to_add[1] = &mG;
        if (!secp256k1_ec_pubkey_combine(ctx, &rhs_eq2_term2_base, points_to_add, 2)) return 0; // Y = C2 - mG
    }

    /* 4c. RHS term = e'*Y */
    if (!secp256k1_ec_pubkey_tweak_mul(ctx, &rhs_eq2_term2_base, e_scalar)) return 0;
    /* 4d. RHS = T2 + (e'*Y) */
    points_to_add[0] = &T2; points_to_add[1] = &rhs_eq2_term2_base;
    if (!secp256k1_ec_pubkey_combine(ctx, &rhs_eq2, points_to_add, 2)) return 0;

    /* 4e. Compare LHS == RHS */
    len = 33; secp256k1_ec_pubkey_serialize(ctx, lhs_bytes, &len, &lhs_eq2, SECP256K1_EC_COMPRESSED);
    len = 33; secp256k1_ec_pubkey_serialize(ctx, rhs_bytes, &len, &rhs_eq2, SECP256K1_EC_COMPRESSED);
    if (memcmp(lhs_bytes, rhs_bytes, 33) != 0) return 0; // Eq 2 failed

    return 1; /* Both equations passed */
}