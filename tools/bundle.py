#!/usr/bin/env python3
"""
Packs several per-Minecraft-version GTLTagger jars into ONE jar (Fabric jar-in-jar), so you only
have to upload a single file. Fabric Loader loads a nested mod only when its own `depends`
(Minecraft version range, Java version, ...) are satisfied, so on any given game exactly one of
the nested builds is used.

Usage:  python3 tools/bundle.py OUTPUT.jar INPUT1.jar INPUT2.jar ...

Each nested jar gets a unique mod id (otherwise five jars would all claim to be "gtltagger").
Nothing else inside the nested jars is modified.
"""
import json, re, sys, zipfile
from pathlib import Path

def main(argv):
    if len(argv) < 3:
        sys.exit(__doc__)
    out, inputs = Path(argv[1]), [Path(p) for p in argv[2:]]
    nested = []   # (name inside bundle, bytes)
    first_meta = None
    seen_ids = set()

    for jar in inputs:
        with zipfile.ZipFile(jar) as zin:
            meta = json.loads(zin.read("fabric.mod.json"))
            mc = meta.get("depends", {}).get("minecraft", "any")
            slug = re.sub(r"[^a-z0-9]+", "-", f"{jar.stem}".lower()).strip("-")
            new_id = ("gtltagger-" + slug.replace("gtltagger", "").strip("-"))[:63].rstrip("-")
            if new_id in seen_ids:
                sys.exit(f"duplicate nested id {new_id!r} (two input jars with the same name?)")
            seen_ids.add(new_id)
            if first_meta is None:
                first_meta = meta
            meta["id"] = new_id
            meta["name"] = f"{meta.get('name', 'GTLTagger')} ({mc})"
            meta.pop("provides", None)
            nested_name = f"META-INF/jars/{new_id}.jar"
            import io
            buf = io.BytesIO()
            with zipfile.ZipFile(buf, "w", zipfile.ZIP_DEFLATED) as zout:
                for item in zin.infolist():
                    data = zin.read(item.filename)
                    if item.filename == "fabric.mod.json":
                        data = json.dumps(meta, indent=2).encode()
                    zout.writestr(item, data)
            nested.append((nested_name, buf.getvalue()))
            print(f"  nested {jar.name:45s} -> {new_id}  (minecraft {mc})")

    assert first_meta is not None
    outer = {
        "schemaVersion": 1,
        "id": "gtltagger",
        "version": first_meta["version"],
        "name": "GTLTagger",
        "description": first_meta.get("description", ""),
        "authors": first_meta.get("authors", []),
        "license": first_meta.get("license", ""),
        "environment": "client",
        "depends": {"fabricloader": ">=0.16.14"},
        "jars": [{"file": name} for name, _ in nested],
    }
    if "icon" in first_meta:
        outer["icon"] = first_meta["icon"]

    with zipfile.ZipFile(out, "w", zipfile.ZIP_DEFLATED) as z:
        z.writestr("fabric.mod.json", json.dumps(outer, indent=2))
        for name, data in nested:
            z.writestr(name, data)
    print(f"wrote {out} ({out.stat().st_size // 1024} KiB, {len(nested)} nested builds)")

if __name__ == "__main__":
    main(sys.argv)
