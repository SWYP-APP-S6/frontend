"""Deterministic client naming overlay. The delivered server specification is immutable."""
import argparse
import copy
import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
METHODS = {'get', 'post', 'put', 'patch', 'delete', 'head', 'options'}


def operations(spec):
    for path, item in spec['paths'].items():
        for method, operation in item.items():
            if method in METHODS:
                yield method, path, operation


def refs(value):
    if isinstance(value, dict):
        if '$ref' in value:
            yield value['$ref']
        for child in value.values():
            yield from refs(child)
    elif isinstance(value, list):
        for child in value:
            yield from refs(child)


def pascal(value):
    return value[0].upper() + value[1:]


def prepare(source, mapping, policies):
    actual = {f'{m.upper()} {p}' for m, p, _ in operations(source)}
    if actual != set(mapping):
        raise ValueError(f'Endpoint mapping mismatch: {actual ^ set(mapping)}')
    output = {}
    schemas = source['components']['schemas']
    for module in ('auth', 'consumer', 'owner'):
        spec = {k: copy.deepcopy(v) for k, v in source.items() if k not in ('paths', 'components', 'tags', 'servers')}
        spec['servers'] = [{'url': policies['baseUrl']}]
        spec['paths'] = {}
        generated = {}
        seen = set()

        def clone(name, direction, target=None):
            target = target or (name if name.endswith(direction) else name + direction)
            if target in generated:
                if generated[target]['x-server-schema'] != name:
                    raise ValueError('Model name collision: ' + target)
                return target
            original = schemas[name]
            model = copy.deepcopy(original)
            generated[target] = model
            model['x-server-schema'] = name
            required = set(model.get('required', []))
            for field, prop in model.get('properties', {}).items():
                nullable = field not in required or prop.get('nullable', False) or 'null' in (prop.get('type') if isinstance(prop.get('type'), list) else [])
                if '$ref' in prop:
                    child = clone(prop['$ref'].split('/')[-1], direction)
                    prop['$ref'] = '#/components/schemas/' + child
                    default = child + '()'
                elif prop.get('type') == 'array':
                    if '$ref' in prop['items']:
                        child = clone(prop['items']['$ref'].split('/')[-1], direction)
                        prop['items']['$ref'] = '#/components/schemas/' + child
                    default = 'emptySet()' if prop.get('uniqueItems') else 'emptyList()'
                elif 'enum' in prop:
                    value = prop.get('default', policies['enumDefaults'].get(name + '.' + field))
                    if not nullable and value not in prop['enum']:
                        raise ValueError(f'Missing enum default: {name}.{field}')
                    default = pascal(field) + '.' + str(value)
                elif prop.get('type') == 'string':
                    default = '""'
                elif prop.get('type') == 'integer':
                    default = '0L' if prop.get('format') == 'int64' else '0'
                elif prop.get('type') == 'number':
                    if name + '.' + field not in policies['doubleFields']:
                        raise ValueError(f'Unmapped decimal: {name}.{field}')
                    prop['format'] = 'double'
                    default = '0.0'
                elif prop.get('type') == 'boolean':
                    default = 'false'
                elif prop.get('type') == 'object' and 'additionalProperties' in prop:
                    default = 'emptyMap()'
                elif not prop:
                    # Unconstrained JSON (including null), not a Kotlin Any serializer.
                    prop['x-kotlin-type'] = 'kotlinx.serialization.json.JsonElement'
                    default = 'kotlinx.serialization.json.JsonNull'
                else:
                    raise ValueError(f'Unsupported property: {name}.{field}: {prop}')
                prop['x-kotlin-default'] = 'null' if nullable else default
            return target

        for method, path, original in operations(source):
            rule = mapping[f'{method.upper()} {path}']
            if rule['module'] != module:
                continue
            if rule['method'] in seen:
                raise ValueError('Duplicate client method: ' + rule['method'])
            seen.add(rule['method'])
            operation = copy.deepcopy(original)
            operation['x-server-operation-id'] = original['operationId']
            operation['operationId'] = rule['method']
            operation['tags'] = [rule['section']]
            for media_type, body in operation.get('requestBody', {}).get('content', {}).items():
                if '$ref' in body['schema']:
                    name = body['schema']['$ref'].split('/')[-1]
                    body['schema']['$ref'] = '#/components/schemas/' + clone(name, 'Request', pascal(rule['method']) + 'Request')
                elif media_type != 'multipart/form-data':
                    raise ValueError('Unsupported inline body: ' + path)
            if method == 'delete' and 'requestBody' in operation:
                operation['x-delete-with-body'] = True
            for code, response in operation['responses'].items():
                for content in response.get('content', {}).values():
                    name = content['schema']['$ref'].split('/')[-1]
                    target = pascal(rule['method']) + 'Response' if code.startswith('2') else None
                    content['schema']['$ref'] = '#/components/schemas/' + clone(name, 'Response', target)
            # Backend confirmed page/size/repeated sort on 2026-09-13.
            parameters = []
            for parameter in operation.get('parameters', []):
                if parameter.get('schema', {}).get('$ref') == '#/components/schemas/Pageable':
                    for field, schema in schemas['Pageable']['properties'].items():
                        parameters.append({'name': field, 'in': 'query', 'required': False,
                                           'schema': copy.deepcopy(schema), 'style': 'form', 'explode': True})
                else:
                    parameters.append(parameter)
            if 'parameters' in operation:
                operation['parameters'] = parameters
            for param in operation.get('parameters', []):
                if '$ref' in param.get('schema', {}):
                    name = param['schema']['$ref'].split('/')[-1]
                    param['schema']['$ref'] = '#/components/schemas/' + clone(name, 'Request')
                elif param.get('schema', {}).get('type') == 'number':
                    param['schema']['format'] = 'double'
            item = spec['paths'].setdefault(path, {k: copy.deepcopy(v) for k, v in source['paths'][path].items() if k not in METHODS})
            item[method] = operation
        # Non-null object defaults must terminate; collections and nullable refs do not recurse.
        def check_defaults(name, stack):
            if name in stack:
                raise ValueError('Recursive non-null defaults: ' + ' -> '.join(stack + [name]))
            for prop in generated[name].get('properties', {}).values():
                if '$ref' in prop and prop.get('x-kotlin-default') != 'null':
                    check_defaults(prop['$ref'].split('/')[-1], stack + [name])
        for name in generated:
            check_defaults(name, [])
        spec['components'] = {'schemas': generated, 'securitySchemes': copy.deepcopy(source['components']['securitySchemes'])}
        for ref in refs(spec):
            value = spec
            for key in ref.removeprefix('#/').split('/'):
                value = value[key]
        output[module] = spec
    return output


def load_inputs():
    raw = (ROOT / 'openapi/swyp-app-api-20260913-v2.json').read_bytes()
    expected = (ROOT / 'openapi/spec.sha256').read_text().split()[0]
    if hashlib.sha256(raw).hexdigest() != expected:
        raise ValueError('Source checksum mismatch; update the snapshot and checksum together')
    source = json.loads(raw)
    mapping = json.loads((ROOT / 'openapi/endpoint-map.json').read_text())
    policies = json.loads((ROOT / 'openapi/model-map.json').read_text())
    # Private, explicitly labelled client contract until the new server snapshot is delivered.
    supplement = json.loads((ROOT / 'openapi/terms-supplement.json').read_text())
    for target, additions in [(source['paths'], supplement['paths']),
                              (source['components']['schemas'], supplement['schemas']),
                              (mapping, supplement['mapping'])]:
        if set(target) & set(additions):
            raise ValueError('Supplement conflicts with source; remove it after updating the official snapshot')
        target.update(additions)
    return source, mapping, policies


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--output', type=Path, default=ROOT / 'build/openapi')
    args = parser.parse_args()
    result = prepare(*load_inputs())
    args.output.mkdir(parents=True, exist_ok=True)
    for module, spec in result.items():
        (args.output / f'{module}.json').write_text(json.dumps(spec, ensure_ascii=False, indent=2, sort_keys=True) + '\n')


if __name__ == '__main__':
    main()
