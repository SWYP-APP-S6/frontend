"""Verify codegen invariants against actual Kotlin output, after openApiGenerate."""
import re
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
mapping = json.loads((ROOT / 'openapi/endpoint-map.json').read_text())
for module, count in [('auth', 1), ('consumer', 7), ('owner', 4)]:
    root = ROOT / 'remote' / module / 'build/generated/openapi'
    services = list(root.rglob('*Service.kt'))
    assert len(services) == count, (module, services)
    actual_methods = [name for path in services for name in re.findall(r'suspend fun (\w+)\(', path.read_text())]
    expected_methods = [rule['method'] for rule in mapping.values() if rule['module'] == module]
    assert sorted(actual_methods) == sorted(expected_methods), (module, actual_methods)
    assert not list(root.rglob('UpdateAvailableQty*.kt')), module
    models = list(root.rglob('model/*.kt'))
    assert models, module
    for path in models:
        assert path.stem.endswith(('Request', 'Response')), path
        source = path.read_text()
        properties = re.findall(r'    val (`?\w+`?): (.+)', source)
        for name, declaration in properties:
            assert ' = ' in declaration, (path, name)
            assert name.strip('`')[0].islower(), (path, name)
        assert source.count('@SerialName(value = ') >= len(properties), path
    print(f'{module}: {len(services)} services, {len(models)} models checked')
