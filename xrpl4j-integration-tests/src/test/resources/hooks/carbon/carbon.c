#include <stdint.h>
#include "../hookapi.h"

int64_t cbak(int64_t reserved)
{
    return 0;
}

int64_t hook(int64_t reserved ) {

    TRACESTR("Carbon: started");

    // before we start calling hook-api functions we should tell the hook how many tx we intend to create
    etxn_reserve(1); // we are going to emit 1 transaction

    // this api fetches the AccountID of the account the hook currently executing is installed on
    // since hooks can be triggered by both incoming and ougoing transactions this is important to know
    unsigned char hook_accid[20];
    hook_account((uint32_t)hook_accid, 20);

    // NB:
    //  almost all of the hook apis require a buffer pointer and buffer length to be supplied ... to make this a
    //  little easier to code a macro: `SBUF(your_buffer)` expands to `your_buffer, sizeof(your_buffer)`

    // next fetch the sfAccount field from the originating transaction
    uint8_t account_field[20];
    int32_t account_field_len = otxn_field(SBUF(account_field), sfAccount);
    TRACEVAR(account_field_len);
    if (account_field_len < 20)                                   // negative values indicate errors from every api
        rollback(SBUF("Carbon: sfAccount field missing!!!"), 1);  // this code could never be hit in prod
                                                                  // but it's here for completeness

    // compare the "From Account" (sfAccount) on the transaction with the account the hook is running on
    int equal = 0; BUFFER_EQUAL(equal, hook_accid, account_field, 20);
    if (!equal)
    {
        // if the accounts are not equal (memcmp != 0) the otxn was sent to the hook account by someone else
        // accept() it and end the hook execution here
        accept(SBUF("Carbon: Incoming transaction"), 2);
    }

    // execution to here means the user has sent a valid transaction FROM the account the hook is installed on

    // fetch the sent Amount
    // Amounts can be 384 bits or 64 bits. If the Amount is an XRP value it will be 64 bits.
    unsigned char amount_buffer[48];
    int64_t amount_len = otxn_field(SBUF(amount_buffer), sfAmount);
    int64_t drops_to_send = 1000; // this will be the default


    if (amount_len != 8)
    {
        // you can trace the behaviour of your hook using the trace(buf, size, as_hex) api
        // which will output to xrpld's trace log
        TRACESTR("Carbon: Non-xrp transaction detected, sending default 1000 drops to rfCarbon");
    } else
    {
        TRACESTR("Carbon: XRP transaction detected, computing 1% to send to rfCarbon");
        int64_t otxn_drops = AMOUNT_TO_DROPS(amount_buffer);
        TRACEVAR(otxn_drops);
        if (otxn_drops > 100000)   // if its less we send the default amount. or if there was an error we send default
            drops_to_send = (int64_t)((double)otxn_drops * 0.01f); // otherwise we send 1%
    }

    TRACEVAR(drops_to_send);

    // hooks communicate accounts via the 20 byte account ID, this can be generated from an raddr like so
    // a more efficient way to do this is precompute the account-id from the raddr (if the raddr never changes)
    uint8_t carbon_accid[20];
    int64_t ret = util_accid(
            SBUF(carbon_accid),                                   /* <-- generate into this buffer  */
            SBUF("rfCarbonVNTuXckX6x2qTMFmFSnm6dEWGX") );         /* <-- from this r-addr           */
    TRACEVAR(ret);

    // fees for emitted transactions are based on how many txn your hook is emitted, whether or not this triggering
    // was caused by a previously emitted transaction and how large the new emitted transaction is in bytes
    // we need to precompute this before populating the payment transaction, as it is a field inside the tx
    int64_t fee_base = etxn_fee_base(PREPARE_PAYMENT_SIMPLE_SIZE);

    // create a buffer to write the emitted transaction into
    unsigned char tx[PREPARE_PAYMENT_SIMPLE_SIZE];

    // we will use an XRP payment macro, this will populate the buffer with a serialized binary transaction
    // Parameter list: ( buf_out, drops_amount, drops_fee, to_address, dest_tag, src_tag )
    PREPARE_PAYMENT_SIMPLE(tx, drops_to_send++, fee_base, carbon_accid, 0, 0);

    // emit the transaction
    uint8_t emithash[32];
    emit(SBUF(emithash), SBUF(tx));

    // accept and allow the original transaction through
    accept(SBUF("Carbon: Emitted transaction"), 0);
    return 0;

}
