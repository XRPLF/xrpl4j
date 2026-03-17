#include "secp256k1_mpt.h"
#include <string.h>
#include <stdio.h>

static void get_h_generator(const secp256k1_context* ctx, secp256k1_pubkey* h) {
    unsigned char h_scalar[32] = {0};
    h_scalar[31] = 0x03; // H = 3G
    if (!secp256k1_ec_pubkey_create(ctx, h, h_scalar)) {
        fprintf(stderr, "ABORT: secp256k1_ec_pubkey_create failed\n");
    }
}

int secp256k1_mpt_pedersen_commit(
        const secp256k1_context* ctx,
        secp256k1_pubkey* commitment,
        uint64_t amount,
        const unsigned char* rho
) {
    secp256k1_pubkey mG, rH, H;
    unsigned char m_scalar[32] = {0};

    // 1. Calculate m * G
    for (int i = 0; i < 8; i++) {
        m_scalar[31 - i] = (amount >> (i * 8)) & 0xFF;
    }
    if (!secp256k1_ec_pubkey_create(ctx, &mG, m_scalar)) {
        return 0;
    }

    // 2. Calculate rho * H
    get_h_generator(ctx, &H);
    rH = H;
    if (!secp256k1_ec_pubkey_tweak_mul(ctx, &rH, rho)) {
        return 0;
    }

    // 3. Combine: mG + rH
    const secp256k1_pubkey* points[2] = {&mG, &rH};
    if (!secp256k1_ec_pubkey_combine(ctx, commitment, points, 2)) {
        return 0;
    }

    return 1;
}