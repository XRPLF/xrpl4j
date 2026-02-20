#include "secp256k1_mpt.h"
#include <openssl/sha.h>
#include <openssl/rand.h>
#include <string.h>
#include <assert.h>
#include <stdio.h>

/* --- Internal Helpers --- */

static int generate_random_scalar(const secp256k1_context* ctx, unsigned char* scalar_bytes) {
    do {
        if (RAND_bytes(scalar_bytes, 32) != 1) return 0;
    } while (secp256k1_ec_seckey_verify(ctx, scalar_bytes) != 1);
    return 1;
}

static void get_h_generator(const secp256k1_context* ctx, secp256k1_pubkey* h) {
    unsigned char h_scalar[32] = {0};
    h_scalar[31] = 0x03;
    if (!secp256k1_ec_pubkey_create(ctx, h, h_scalar)) {
        fprintf(stderr, "ABORT: secp256k1_ec_pubkey_create failed\n");
    }
}

static void build_link_challenge_hash(
        const secp256k1_context* ctx,
        unsigned char hash_input[290],
        const secp256k1_pubkey* c1, const secp256k1_pubkey* c2,
        const secp256k1_pubkey* pk, const secp256k1_pubkey* pcm,
        const secp256k1_pubkey* T1, const secp256k1_pubkey* T2, const secp256k1_pubkey* T3,
        const unsigned char* context_id)
{
    const char* domain_sep = "MPT_ELGAMAL_PEDERSEN_LINK";
    size_t offset = 0, len;
    memset(hash_input, 0, 290);
    memcpy(hash_input + offset, domain_sep, 25);
    offset += 27;

    const secp256k1_pubkey* points[] = {c1, c2, pk, pcm, T1, T2, T3};
    for (int i = 0; i < 7; i++) {
        len = 33;
        secp256k1_ec_pubkey_serialize(ctx, hash_input + offset, &len, points[i], SECP256K1_EC_COMPRESSED);
        offset += 33;
    }
    memcpy(hash_input + offset, context_id, 32);
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
    unsigned char km[32], kr[32], krho[32], e[32], sm[32], sr[32], srho[32], m_sc[32] = {0};
    secp256k1_pubkey T1, T2, T3, H, mG, rPk, rhoH;
    size_t len = 33;

    if (!generate_random_scalar(ctx, km) || !generate_random_scalar(ctx, kr) || !generate_random_scalar(ctx, krho)) return 0;
    if (!secp256k1_ec_pubkey_create(ctx, &T1, kr)) return 0;
    if (!secp256k1_ec_pubkey_create(ctx, &mG, km)) return 0;
    rPk = *pk;
    if (!secp256k1_ec_pubkey_tweak_mul(ctx, &rPk, kr)) return 0;
    const secp256k1_pubkey* add_t2[2] = {&mG, &rPk};
    if (!secp256k1_ec_pubkey_combine(ctx, &T2, add_t2, 2)) return 0;
    get_h_generator(ctx, &H);
    rhoH = H;
    if (!secp256k1_ec_pubkey_tweak_mul(ctx, &rhoH, krho)) return 0;
    const secp256k1_pubkey* add_t3[2] = {&mG, &rhoH};
    if (!secp256k1_ec_pubkey_combine(ctx, &T3, add_t3, 2)) return 0;

    unsigned char hash_input[290];
    build_link_challenge_hash(ctx, hash_input, c1, c2, pk, pcm, &T1, &T2, &T3, context_id);
    SHA256(hash_input, 290, e);

    for (int i = 0; i < 8; i++) m_sc[31-i] = (amount >> (i*8)) & 0xFF;

    memcpy(sm, m_sc, 32);
    if (!secp256k1_ec_seckey_tweak_mul(ctx, sm, e)) return 0;
    if (!secp256k1_ec_seckey_tweak_add(ctx, sm, km)) return 0;
    memcpy(sr, r, 32);
    if (!secp256k1_ec_seckey_tweak_mul(ctx, sr, e)) return 0;
    if (!secp256k1_ec_seckey_tweak_add(ctx, sr, kr)) return 0;
    memcpy(srho, rho, 32);
    if (!secp256k1_ec_seckey_tweak_mul(ctx, srho, e)) return 0;
    if (!secp256k1_ec_seckey_tweak_add(ctx, srho, krho)) return 0;

    len = 33; secp256k1_ec_pubkey_serialize(ctx, proof, &len, &T1, SECP256K1_EC_COMPRESSED);
    len = 33; secp256k1_ec_pubkey_serialize(ctx, proof+33, &len, &T2, SECP256K1_EC_COMPRESSED);
    len = 33; secp256k1_ec_pubkey_serialize(ctx, proof+66, &len, &T3, SECP256K1_EC_COMPRESSED);
    memcpy(proof+99, sm, 32); memcpy(proof+131, sr, 32); memcpy(proof+163, srho, 32);
    return 1;
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
    secp256k1_pubkey T1_p, T2_p, T3_p;
    secp256k1_pubkey lhs, rhs, H, mG, term2;
    unsigned char sm[32], sr[32], srho[32], e[32], e_neg[32];
    unsigned char hash_input[290];

    if (!secp256k1_ec_pubkey_parse(ctx, &T1_p, proof, 33)) return 0;
    if (!secp256k1_ec_pubkey_parse(ctx, &T2_p, proof + 33, 33)) return 0;
    if (!secp256k1_ec_pubkey_parse(ctx, &T3_p, proof + 66, 33)) return 0;

    memcpy(sm,   proof + 99,  32);
    memcpy(sr,   proof + 131, 32);
    memcpy(srho, proof + 163, 32);

    if (secp256k1_ec_seckey_verify(ctx, sm) != 1) return 0;
    if (secp256k1_ec_seckey_verify(ctx, sr) != 1) return 0;
    if (secp256k1_ec_seckey_verify(ctx, srho) != 1) return 0;

    build_link_challenge_hash(ctx, hash_input, c1, c2, pk, pcm, &T1_p, &T2_p, &T3_p, context_id);
    SHA256(hash_input, sizeof(hash_input), e);
    if (secp256k1_ec_seckey_verify(ctx, e) != 1) return 0;

    memcpy(e_neg, e, 32);
    if (!secp256k1_ec_seckey_negate(ctx, e_neg)) return 0;

#define COMBINE2(out, A, B) do {                               \
        secp256k1_pubkey _sum;                                 \
        const secp256k1_pubkey* _pts[2] = { (A), (B) };         \
        if (!secp256k1_ec_pubkey_combine(ctx, &_sum, _pts, 2))  \
            return 0;                                          \
        (out) = _sum;                                          \
    } while (0)

#define EQ_PUBKEY(A, B) do {                                                   \
        unsigned char _a[33], _b[33];                                           \
        size_t _l = 33;                                                        \
        if (!secp256k1_ec_pubkey_serialize(ctx, _a, &_l, (A), SECP256K1_EC_COMPRESSED)) return 0; \
        _l = 33;                                                               \
        if (!secp256k1_ec_pubkey_serialize(ctx, _b, &_l, (B), SECP256K1_EC_COMPRESSED)) return 0; \
        if (memcmp(_a, _b, 33) != 0) return 0;                                 \
    } while (0)

    /* Eq 1 */
    if (!secp256k1_ec_pubkey_create(ctx, &lhs, sr)) return 0;
    rhs = *c1; if (!secp256k1_ec_pubkey_tweak_mul(ctx, &rhs, e_neg)) return 0;
    COMBINE2(lhs, &lhs, &rhs);
    EQ_PUBKEY(&lhs, &T1_p);

    /* Eq 2 */
    if (!secp256k1_ec_pubkey_create(ctx, &mG, sm)) return 0;
    term2 = *pk; if (!secp256k1_ec_pubkey_tweak_mul(ctx, &term2, sr)) return 0;
    COMBINE2(lhs, &mG, &term2);
    rhs = *c2; if (!secp256k1_ec_pubkey_tweak_mul(ctx, &rhs, e_neg)) return 0;
    COMBINE2(lhs, &lhs, &rhs);
    EQ_PUBKEY(&lhs, &T2_p);

    /* Eq 3 */
    get_h_generator(ctx, &H);
    term2 = H; if (!secp256k1_ec_pubkey_tweak_mul(ctx, &term2, srho)) return 0;
    COMBINE2(lhs, &mG, &term2);
    rhs = *pcm; if (!secp256k1_ec_pubkey_tweak_mul(ctx, &rhs, e_neg)) return 0;
    COMBINE2(lhs, &lhs, &rhs);
    EQ_PUBKEY(&lhs, &T3_p);

#undef COMBINE2
#undef EQ_PUBKEY
    return 1;
}