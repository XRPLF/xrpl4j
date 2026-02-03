/**
* @file proof_link.c
* @brief Zero-Knowledge Proof Linking ElGamal Ciphertexts and Pedersen Commitments.
*
* This module implements a Sigma protocol to prove that an ElGamal ciphertext
* and a Pedersen commitment encode the same underlying plaintext value \f$ m \f$,
* without revealing \f$ m \f$ or the blinding factors.
*
* @details
        * **Statement:**
* The prover demonstrates knowledge of scalars \f$ (m, r, \rho) \f$ such that:
* 1. \f$ C_1 = r \cdot G \f$ (ElGamal Ephemeral Key)
* 2. \f$ C_2 = m \cdot G + r \cdot P \f$ (ElGamal Masked Value)
* 3. \f$ PC_m = m \cdot G + \rho \cdot H \f$ (Pedersen Commitment)
*
* **Protocol (Schnorr-style):**
* 1. **Commitment:**
* Prover samples nonces \f$ k_m, k_r, k_\rho \f$ and computes:
* - \f$ T_1 = k_r \cdot G \f$
* - \f$ T_2 = k_m \cdot G + k_r \cdot P \f$
* - \f$ T_3 = k_m \cdot G + k_\rho \cdot H \f$
*
* 2. **Challenge:**
* \f[ e = H(\text{"MPT_ELGAMAL_PEDERSEN_LINK"} \parallel C_1 \parallel C_2 \parallel P \parallel PC_m \parallel T_1 \parallel T_2 \parallel T_3 \parallel \dots) \f]
*
* 3. **Response:**
* - \f$ s_m = k_m + e \cdot m \f$
* - \f$ s_r = k_r + e \cdot r \f$
* - \f$ s_\rho = k_\rho + e \cdot \rho \f$
*
* 4. **Verification:**
* Verifier checks:
* - \f$ s_r \cdot G \stackrel{?}{=} T_1 + e \cdot C_1 \f$
* - \f$ s_m \cdot G + s_r \cdot P \stackrel{?}{=} T_2 + e \cdot C_2 \f$
* - \f$ s_m \cdot G + s_\rho \cdot H \stackrel{?}{=} T_3 + e \cdot PC_m \f$
        *
        * **Security Context:**
* This proof prevents "bait-and-switch" attacks where a user sends a valid range proof
* for a small amount (e.g., 10) but updates the ledger balance with a large or negative
* amount (e.g., 1,000,000 or -5). It binds the two representations together.
*
* @see [Spec (ConfidentialMPT_20260201.pdf) Section 3.3.5] Linking ElGamal Ciphertexts and Pedersen Commitments
*/
#include "secp256k1_mpt.h"
#include <openssl/sha.h>
#include <openssl/rand.h>
#include <string.h>
#include <assert.h>
#include <stdio.h>

/* --- Internal Helpers --- */

static int pubkey_equal(const secp256k1_context* ctx, const secp256k1_pubkey* pk1, const secp256k1_pubkey* pk2) {
    return secp256k1_ec_pubkey_cmp(ctx, pk1, pk2) == 0;
}

static int generate_random_scalar(const secp256k1_context* ctx, unsigned char* scalar_bytes) {
    do {
        if (RAND_bytes(scalar_bytes, 32) != 1) return 0;
    } while (secp256k1_ec_seckey_verify(ctx, scalar_bytes) != 1);
    return 1;
}

static void build_link_challenge_hash(
        const secp256k1_context* ctx,
        unsigned char* e_out,
        const secp256k1_pubkey* c1, const secp256k1_pubkey* c2,
        const secp256k1_pubkey* pk, const secp256k1_pubkey* pcm,
        const secp256k1_pubkey* T1, const secp256k1_pubkey* T2, const secp256k1_pubkey* T3,
        const unsigned char* context_id)
{
    SHA256_CTX sha;
    unsigned char buf[33];
    unsigned char h[32];
    size_t len;
    const char* domain = "MPT_ELGAMAL_PEDERSEN_LINK";

    SHA256_Init(&sha);
    SHA256_Update(&sha, domain, strlen(domain));

    /* Helper Macro */
#define SER_AND_HASH(pk_ptr) do { \
        len = 33; \
        secp256k1_ec_pubkey_serialize(ctx, buf, &len, pk_ptr, SECP256K1_EC_COMPRESSED); \
        SHA256_Update(&sha, buf, 33); \
    } while(0)

    SER_AND_HASH(c1);
    SER_AND_HASH(c2);
    SER_AND_HASH(pk);
    SER_AND_HASH(pcm);
    SER_AND_HASH(T1);
    SER_AND_HASH(T2);
    SER_AND_HASH(T3);

#undef SER_AND_HASH

    if (context_id) {
        SHA256_Update(&sha, context_id, 32);
    }

    SHA256_Final(h, &sha);
    secp256k1_mpt_scalar_reduce32(e_out, h);
}

/* --- Prover Implementation --- */

int secp256k1_elgamal_pedersen_link_prove(
        const secp256k1_context* ctx,
        unsigned char* proof,
        const secp256k1_pubkey* c1,
        const secp256k1_pubkey* c2,
        const secp256k1_pubkey* pk,
        const secp256k1_pubkey* pcm,
        uint64_t amount,
        const unsigned char* r,
        const unsigned char* rho,
        const unsigned char* context_id)
{
    unsigned char km[32], kr[32], krho[32];
    unsigned char e[32];
    unsigned char sm[32], sr[32], srho[32];
    unsigned char term[32];
    unsigned char m_scalar[32] = {0};

    secp256k1_pubkey T1, T2, T3;
    secp256k1_pubkey H, mG;
    size_t len;
    int ok = 0;

    /* 0. Validate Witnesses */
    if (!secp256k1_ec_seckey_verify(ctx, r)) return 0;
    if (!secp256k1_ec_seckey_verify(ctx, rho)) return 0;

    /* 1. Generate Nonces */
    if (!generate_random_scalar(ctx, km)) goto cleanup;
    if (!generate_random_scalar(ctx, kr)) goto cleanup;
    if (!generate_random_scalar(ctx, krho)) goto cleanup;

    /* 2. Compute Commitments */

    /* T1 = kr * G */
    if (!secp256k1_ec_pubkey_create(ctx, &T1, kr)) goto cleanup;

    /* T2 = km * G + kr * Pk */
    if (!secp256k1_ec_pubkey_create(ctx, &mG, km)) goto cleanup; // km*G
    secp256k1_pubkey krPk = *pk;
    if (!secp256k1_ec_pubkey_tweak_mul(ctx, &krPk, kr)) goto cleanup; // kr*Pk
    const secp256k1_pubkey* add_t2[2] = {&mG, &krPk};
    if (!secp256k1_ec_pubkey_combine(ctx, &T2, add_t2, 2)) goto cleanup;

    /* T3 = km * G + krho * H */
    /* Note: mG (km*G) is reused here */
    if (!secp256k1_mpt_get_h_generator(ctx, &H)) goto cleanup;
    secp256k1_pubkey krhoH = H;
    if (!secp256k1_ec_pubkey_tweak_mul(ctx, &krhoH, krho)) goto cleanup; // krho*H
    const secp256k1_pubkey* add_t3[2] = {&mG, &krhoH};
    if (!secp256k1_ec_pubkey_combine(ctx, &T3, add_t3, 2)) goto cleanup;

    /* 3. Challenge */
    build_link_challenge_hash(ctx, e, c1, c2, pk, pcm, &T1, &T2, &T3, context_id);

    /* 4. Responses */
    /* Convert amount to scalar */
    for (int i = 0; i < 8; i++) m_scalar[31 - i] = (amount >> (i * 8)) & 0xFF;

    /* sm = km + e * m */
    memcpy(sm, km, 32);
    memcpy(term, m_scalar, 32);
    if (!secp256k1_ec_seckey_tweak_mul(ctx, term, e)) goto cleanup;
    if (!secp256k1_ec_seckey_tweak_add(ctx, sm, term)) goto cleanup;

    /* sr = kr + e * r */
    memcpy(sr, kr, 32);
    memcpy(term, r, 32);
    if (!secp256k1_ec_seckey_tweak_mul(ctx, term, e)) goto cleanup;
    if (!secp256k1_ec_seckey_tweak_add(ctx, sr, term)) goto cleanup;

    /* srho = krho + e * rho */
    memcpy(srho, krho, 32);
    memcpy(term, rho, 32);
    if (!secp256k1_ec_seckey_tweak_mul(ctx, term, e)) goto cleanup;
    if (!secp256k1_ec_seckey_tweak_add(ctx, srho, term)) goto cleanup;

    /* 5. Serialize Proof (195 bytes) */
    unsigned char* ptr = proof;
    len = 33; if (!secp256k1_ec_pubkey_serialize(ctx, ptr, &len, &T1, SECP256K1_EC_COMPRESSED)) goto cleanup; ptr += 33;
    len = 33; if (!secp256k1_ec_pubkey_serialize(ctx, ptr, &len, &T2, SECP256K1_EC_COMPRESSED)) goto cleanup; ptr += 33;
    len = 33; if (!secp256k1_ec_pubkey_serialize(ctx, ptr, &len, &T3, SECP256K1_EC_COMPRESSED)) goto cleanup; ptr += 33;

    memcpy(ptr, sm, 32); ptr += 32;
    memcpy(ptr, sr, 32); ptr += 32;
    memcpy(ptr, srho, 32); ptr += 32;

    ok = 1;

    cleanup:
    /* Securely clear secrets */
    OPENSSL_cleanse(km, 32);
    OPENSSL_cleanse(kr, 32);
    OPENSSL_cleanse(krho, 32);
    OPENSSL_cleanse(m_scalar, 32);
    OPENSSL_cleanse(term, 32);
    OPENSSL_cleanse(sm, 32);
    OPENSSL_cleanse(sr, 32);
    OPENSSL_cleanse(srho, 32);
    return ok;
}

/* --- Verifier Implementation --- */

int secp256k1_elgamal_pedersen_link_verify(
        const secp256k1_context* ctx,
        const unsigned char* proof,
        const secp256k1_pubkey* c1,
        const secp256k1_pubkey* c2,
        const secp256k1_pubkey* pk,
        const secp256k1_pubkey* pcm,
        const unsigned char* context_id)
{
    secp256k1_pubkey T1, T2, T3;
    secp256k1_pubkey LHS, RHS, term, mG, H;
    const secp256k1_pubkey* pts[2];
    unsigned char sm[32], sr[32], srho[32], e[32];
    const unsigned char* ptr = proof;
    int ok = 0;

    /* 1. Deserialize */
    if (!secp256k1_ec_pubkey_parse(ctx, &T1, ptr, 33)) goto cleanup; ptr += 33;
    if (!secp256k1_ec_pubkey_parse(ctx, &T2, ptr, 33)) goto cleanup; ptr += 33;
    if (!secp256k1_ec_pubkey_parse(ctx, &T3, ptr, 33)) goto cleanup; ptr += 33;

    memcpy(sm, ptr, 32); ptr += 32;
    memcpy(sr, ptr, 32); ptr += 32;
    memcpy(srho, ptr, 32); ptr += 32;

    /* Sanity Check Scalars */
    if (!secp256k1_ec_seckey_verify(ctx, sm)) goto cleanup;
    if (!secp256k1_ec_seckey_verify(ctx, sr)) goto cleanup;
    if (!secp256k1_ec_seckey_verify(ctx, srho)) goto cleanup;

    /* 2. Challenge */
    build_link_challenge_hash(ctx, e, c1, c2, pk, pcm, &T1, &T2, &T3, context_id);

    /* 3. Verification Equations */

    /* Eq 1: sr * G == T1 + e * C1 */
    {
        if (!secp256k1_ec_pubkey_create(ctx, &LHS, sr)) goto cleanup; // sr*G

        term = *c1;
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &term, e)) goto cleanup; // e*C1
        pts[0] = &T1; pts[1] = &term;
        if (!secp256k1_ec_pubkey_combine(ctx, &RHS, pts, 2)) goto cleanup; // T1 + e*C1

        if (!pubkey_equal(ctx, &LHS, &RHS)) goto cleanup;
    }

    /* Eq 2: sm * G + sr * Pk == T2 + e * C2 */
    {
        /* LHS = sm*G + sr*Pk */
        if (!secp256k1_ec_pubkey_create(ctx, &mG, sm)) goto cleanup; // sm*G

        term = *pk;
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &term, sr)) goto cleanup; // sr*Pk

        pts[0] = &mG; pts[1] = &term;
        if (!secp256k1_ec_pubkey_combine(ctx, &LHS, pts, 2)) goto cleanup;

        /* RHS = T2 + e*C2 */
        term = *c2;
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &term, e)) goto cleanup; // e*C2

        pts[0] = &T2; pts[1] = &term;
        if (!secp256k1_ec_pubkey_combine(ctx, &RHS, pts, 2)) goto cleanup;

        if (!pubkey_equal(ctx, &LHS, &RHS)) goto cleanup;
    }

    /* Eq 3: sm * G + srho * H == T3 + e * Pcm */
    {
        /* LHS = sm*G (reusing mG calculated above) + srho*H */
        if (!secp256k1_mpt_get_h_generator(ctx, &H)) goto cleanup;

        term = H;
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &term, srho)) goto cleanup; // srho*H

        pts[0] = &mG; pts[1] = &term;
        if (!secp256k1_ec_pubkey_combine(ctx, &LHS, pts, 2)) goto cleanup;

        /* RHS = T3 + e*Pcm */
        term = *pcm;
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &term, e)) goto cleanup; // e*Pcm

        pts[0] = &T3; pts[1] = &term;
        if (!secp256k1_ec_pubkey_combine(ctx, &RHS, pts, 2)) goto cleanup;

        if (!pubkey_equal(ctx, &LHS, &RHS)) goto cleanup;
    }

    ok = 1;

    cleanup:
    return ok;
}
