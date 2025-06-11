## Version 4 to Version 5 Migration Guide

This guide explains a breaking change in v5.0.0 and how to upgrade from v4.1.0 to v5.0.0.

With the [XLS-0070-credentials](https://github.com/XRPLF/XRPL-Standards/tree/master/XLS-0070-credentials) amendment, the
`DepositPreAuth` transaction can now preauthorize **credentials**, not just an account like before.

Because of this change, new `DepositPreAuthObject` ledger objects on the XRPL might have:

- **No `Authorize` field**
- A new field called **`AuthorizeCredentials`**

So in v5.0.0, the `Authorize` field is now optional.

When calling `ledger_entry` or `account_objects` in rippled (starting v5.0.0), the response for a `DepositPreAuthObject`
will include **either** `Authorize` **or** `AuthorizeCredentials`, **not both**.

Before using the response, check if the `Authorize` field is present.