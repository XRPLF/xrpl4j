# XRPL `definitions.json` Generation Guide

The `definitions.json` file is the central registry for the XRP Ledger's binary serialization format. It contains the
mappings for **Field IDs**, **Transaction Types**, and **Ledger Entry Types** (In the future it may even contain even
more mappings, if https://github.com/XRPLF/XRPL-Standards/discussions/418 is implemented).

If you are adding new features to `xrpl4j` that touch the binary codec, the `definitions.json` file in this folder will
need to be regenerated.

## 🛠 Generation Options

### Option 1: Using the `rippled` Binary

If you have a local build of the `xrpld` C++ server, you can export the definitions directly from the source code.

```bash
# Export definitions to a local file
./rippled --get_definitions > definitions.json
```

### Option 2: Using the `xrpl.js` Library

If you don't have a local build of the `xrpld` C++ server, you can use the `xrpl.js` library (specifically the
ripple-binary-codec package) to fetch the definitions.

```bash
# Navigate to the codec package
cd packages/ripple-binary-codec

# Run the update script
npm run update-definitions
```

### Option 3: Using a Live `rippled` Node

You can fetch definitions from any live rippled node via the JSON-RPC interface.

```bash
curl -H 'Content-Type: application/json' -d '{
    "method": "server_definitions",
    "params": [{}]
}' [https://s1.ripple.com:51234/](https://s1.ripple.com:51234/)
```

NOTE: The aboveRPC response includes a `"result": { ... }` wrapper. To use this as a standard definitions.json, you
must extract the object inside `result` and remove the status field.
