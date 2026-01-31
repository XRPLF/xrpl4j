#include "secp256k1_mpt.h"
#include <openssl/sha.h>
#include <openssl/rand.h>
#include <string.h>
#include <assert.h>

/* --- Internal Helpers --- */

static int generate_random_scalar(const secp256k1_context* ctx, unsigned char* scalar) {
    do {
        if (RAND_bytes(scalar, 32) != 1) return 0;
    } while (secp256k1_ec_seckey_verify(ctx, scalar) != 1);
    return 1;
}

/**
 *  Builds the challenge hash: Domain || PublicInputs || Commitments || TxID
 */
static void build_hash_input(
        unsigned char* hash_out, // Output: 32-byte hash
        size_t n,
        const secp256k1_pubkey* R, const secp256k1_pubkey* S, const secp256k1_pubkey* Pk,
        const secp256k1_pubkey* T_m,
        const secp256k1_pubkey* T_rG,
        const secp256k1_pubkey* T_rP,
        const unsigned char* tx_id
) {
    SHA256_CTX sha_ctx;
    const char* domain = "MPT_POK_SAME_PLAINTEXT_PROOF";
    unsigned char buf[33];
    size_t len = 33;
    size_t i;
    secp256k1_context* ser_ctx = secp256k1_context_create(SECP256K1_CONTEXT_NONE);

    SHA256_Init(&sha_ctx);
    SHA256_Update(&sha_ctx, domain, strlen(domain));

    // Public Inputs (R, S, Pk for each ciphertext)
    for (i = 0; i < n; ++i) {
        secp256k1_ec_pubkey_serialize(ser_ctx, buf, &len, &R[i], SECP256K1_EC_COMPRESSED);
        SHA256_Update(&sha_ctx, buf, 33);
        secp256k1_ec_pubkey_serialize(ser_ctx, buf, &len, &S[i], SECP256K1_EC_COMPRESSED);
        SHA256_Update(&sha_ctx, buf, 33);
        secp256k1_ec_pubkey_serialize(ser_ctx, buf, &len, &Pk[i], SECP256K1_EC_COMPRESSED);
        SHA256_Update(&sha_ctx, buf, 33);
    }

    // Commitments
    secp256k1_ec_pubkey_serialize(ser_ctx, buf, &len, T_m, SECP256K1_EC_COMPRESSED);
    SHA256_Update(&sha_ctx, buf, 33);

    for (i = 0; i < n; ++i) {
        secp256k1_ec_pubkey_serialize(ser_ctx, buf, &len, &T_rG[i], SECP256K1_EC_COMPRESSED);
        SHA256_Update(&sha_ctx, buf, 33);
        secp256k1_ec_pubkey_serialize(ser_ctx, buf, &len, &T_rP[i], SECP256K1_EC_COMPRESSED);
        SHA256_Update(&sha_ctx, buf, 33);
    }

    SHA256_Update(&sha_ctx, tx_id, 32);
    SHA256_Final(hash_out, &sha_ctx);
    secp256k1_context_destroy(ser_ctx);
}

/* --- Public API --- */

size_t secp256k1_mpt_prove_same_plaintext_multi_size(size_t n) {
    // (1 point T_m + 2*N points T_r) * 33 + (1 scalar s_m + N scalars s_r) * 32
    return ((1 + 2 * n) * 33) + ((1 + n) * 32);
}

int secp256k1_mpt_prove_same_plaintext_multi(
        const secp256k1_context* ctx,
        unsigned char* proof_out,
        size_t* proof_len,
        uint64_t amount_m,
        size_t n,
        const secp256k1_pubkey* R,
        const secp256k1_pubkey* S,
        const secp256k1_pubkey* Pk,
        const unsigned char* r_array,
        const unsigned char* tx_id
) {
    size_t required_len = secp256k1_mpt_prove_same_plaintext_multi_size(n);
    if (*proof_len < required_len) { *proof_len = required_len; return 0; }
    *proof_len = required_len;

    unsigned char k_m[32];
    unsigned char k_r[n][32];
    secp256k1_pubkey T_m;
    secp256k1_pubkey T_rG[n];
    secp256k1_pubkey T_rP[n];
    unsigned char e[32];
    unsigned char s_m[32];
    unsigned char s_r[n][32];

    size_t i;
    int ok = 1;

    /* 1. Generate Randomness & Commitments */
    if (!generate_random_scalar(ctx, k_m)) return 0;
    if (!secp256k1_ec_pubkey_create(ctx, &T_m, k_m)) ok = 0;

    for (i = 0; i < n; ++i) {
        if (!generate_random_scalar(ctx, k_r[i])) ok = 0;
        if (!secp256k1_ec_pubkey_create(ctx, &T_rG[i], k_r[i])) ok = 0;

        T_rP[i] = Pk[i];
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &T_rP[i], k_r[i])) ok = 0;
    }

    if (!ok) return 0;

    /* 2. Compute Challenge e */
    build_hash_input(e, n, R, S, Pk, &T_m, T_rG, T_rP, tx_id);
    // Ensure e is valid
    if (!secp256k1_ec_seckey_verify(ctx, e)) return 0;

    /* 3. Compute Responses */
    /* s_m = k_m + e * m */
    unsigned char m_scalar[32] = {0};
    for (i = 0; i < 8; ++i) m_scalar[31 - i] = (amount_m >> (i * 8)) & 0xFF;

    memcpy(s_m, k_m, 32);
    unsigned char term[32];
    memcpy(term, m_scalar, 32);
    if (!secp256k1_ec_seckey_tweak_mul(ctx, term, e)) return 0;
    if (!secp256k1_ec_seckey_tweak_add(ctx, s_m, term)) return 0;

    /* s_ri = k_ri + e * ri */
    for (i = 0; i < n; ++i) {
        memcpy(s_r[i], k_r[i], 32);
        memcpy(term, &r_array[i*32], 32); // Extract r_i from flat array
        if (!secp256k1_ec_seckey_tweak_mul(ctx, term, e)) return 0;
        if (!secp256k1_ec_seckey_tweak_add(ctx, s_r[i], term)) return 0;
    }

    /* 4. Serialize Proof */
    size_t offset = 0;
    size_t len = 33;

    // Points
    secp256k1_ec_pubkey_serialize(ctx, proof_out + offset, &len, &T_m, SECP256K1_EC_COMPRESSED); offset += 33;
    for(i=0; i<n; ++i) {
        secp256k1_ec_pubkey_serialize(ctx, proof_out + offset, &len, &T_rG[i], SECP256K1_EC_COMPRESSED); offset += 33;
    }
    for(i=0; i<n; ++i) {
        secp256k1_ec_pubkey_serialize(ctx, proof_out + offset, &len, &T_rP[i], SECP256K1_EC_COMPRESSED); offset += 33;
    }

    // Scalars
    memcpy(proof_out + offset, s_m, 32); offset += 32;
    for(i=0; i<n; ++i) {
        memcpy(proof_out + offset, s_r[i], 32); offset += 32;
    }

    return 1;
}

int secp256k1_mpt_verify_same_plaintext_multi(
        const secp256k1_context* ctx,
        const unsigned char* proof,
        size_t proof_len,
        size_t n,
        const secp256k1_pubkey* R,
        const secp256k1_pubkey* S,
        const secp256k1_pubkey* Pk,
        const unsigned char* tx_id
) {
    if (proof_len != secp256k1_mpt_prove_same_plaintext_multi_size(n)) return 0;

    /* Deserialize */
    size_t offset = 0;
    secp256k1_pubkey T_m;
    secp256k1_pubkey T_rG[n];
    secp256k1_pubkey T_rP[n];
    unsigned char s_m[32];
    unsigned char s_r[n][32];
    size_t i;

    if (!secp256k1_ec_pubkey_parse(ctx, &T_m, proof + offset, 33)) return 0; offset += 33;
    for(i=0; i<n; ++i) {
        if (!secp256k1_ec_pubkey_parse(ctx, &T_rG[i], proof + offset, 33)) return 0; offset += 33;
    }
    for(i=0; i<n; ++i) {
        if (!secp256k1_ec_pubkey_parse(ctx, &T_rP[i], proof + offset, 33)) return 0; offset += 33;
    }

    memcpy(s_m, proof + offset, 32); offset += 32;
    for(i=0; i<n; ++i) {
        memcpy(s_r[i], proof + offset, 32); offset += 32;
    }

    /* Recompute Challenge */
    unsigned char e[32];
    build_hash_input(e, n, R, S, Pk, &T_m, T_rG, T_rP, tx_id);

    /* Verify Equations */
    secp256k1_pubkey lhs, rhs, term, SmG;
    const secp256k1_pubkey* add_pt[3];
    unsigned char b1[33], b2[33];
    size_t len;

    // Precompute s_m * G
    if (!secp256k1_ec_pubkey_create(ctx, &SmG, s_m)) return 0;

    for(i=0; i<n; ++i) {
        /* Check 1: s_ri * G == T_ri_G + e * R_i */
        if (!secp256k1_ec_pubkey_create(ctx, &lhs, s_r[i])) return 0;

        term = R[i];
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &term, e)) return 0;
        add_pt[0] = &T_rG[i]; add_pt[1] = &term;
        if (!secp256k1_ec_pubkey_combine(ctx, &rhs, add_pt, 2)) return 0;

        len = 33; secp256k1_ec_pubkey_serialize(ctx, b1, &len, &lhs, SECP256K1_EC_COMPRESSED);
        len = 33; secp256k1_ec_pubkey_serialize(ctx, b2, &len, &rhs, SECP256K1_EC_COMPRESSED);
        if (memcmp(b1, b2, 33) != 0) return 0;

        /* Check 2: s_m * G + s_ri * P_i == T_m + T_ri_P + e * S_i */
        /* LHS = SmG + s_r[i] * Pk[i] */
        term = Pk[i];
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &term, s_r[i])) return 0;
        add_pt[0] = &SmG; add_pt[1] = &term;
        if (!secp256k1_ec_pubkey_combine(ctx, &lhs, add_pt, 2)) return 0;

        /* RHS = T_m + T_rP[i] + e * S[i] */
        term = S[i];
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &term, e)) return 0;
        add_pt[0] = &T_m; add_pt[1] = &T_rP[i]; add_pt[2] = &term;
        if (!secp256k1_ec_pubkey_combine(ctx, &rhs, add_pt, 3)) return 0;

        len = 33; secp256k1_ec_pubkey_serialize(ctx, b1, &len, &lhs, SECP256K1_EC_COMPRESSED);
        len = 33; secp256k1_ec_pubkey_serialize(ctx, b2, &len, &rhs, SECP256K1_EC_COMPRESSED);
        if (memcmp(b1, b2, 33) != 0) return 0;
    }

    return 1;
}