"""Deterministic client naming overlay. The delivered server specification is immutable."""
import argparse
import copy
import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
METHODS = {'get', 'post', 'put', 'patch', 'delete', 'head', 'options'}
INPUT_FILES = (
    'mangro-app-openapi-2026-09-18-v3-merged.json',
    'endpoint-map.json',
    'model-map.json',
    'spec.sha256',
)


def check_inputs(directory):
    missing = [name for name in INPUT_FILES if not (directory / name).is_file()]
    if missing:
        raise ValueError('Missing OpenAPI inputs: ' + ', '.join(missing) + '; see remote/README.md')


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


def client_mapping(mapping):
    # /users/me belongs to both roles. Keep older private mapping bundles usable.
    result = copy.deepcopy(mapping)
    if 'GET /users/me' in result:
        result['GET /users/me']['module'] = 'user'
    for endpoint, config in result.items():
        if endpoint.split(' ', 1)[1].startswith('/notifications'):
            config['module'] = 'user'
    return result


def prepare(source, mapping, policies):
    mapping = client_mapping(mapping)
    actual = {f'{m.upper()} {p}' for m, p, _ in operations(source)}
    if actual != set(mapping):
        raise ValueError(f'Endpoint mapping mismatch: {actual ^ set(mapping)}')
    output = {}
    schemas = source['components']['schemas']
    for module in ('auth', 'consumer', 'owner', 'user'):
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
                # Member tokens and signup tokens are mutually exclusive in login responses.
                if direction == 'Response' and name == 'KakaoLoginResponse' and field in ('accessToken', 'refreshToken', 'signupToken'):
                    prop['x-kotlin-type'] = 'kotlin.String' if nullable else 'kotlin.String?'
                    prop['x-kotlin-default'] = 'null'
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
                    if code.startswith('2'):
                        envelope = generated[target]
                        if not {'status', 'code', 'data'} <= envelope.get('properties', {}).keys():
                            raise ValueError('Missing base response envelope: ' + name)
                        data = envelope['properties']['data']
                        if '$ref' in data:
                            data_type = f"com.swyp.mangro.remote.{module}.model." + data['$ref'].split('/')[-1]
                        elif data.get('type') == 'array' and '$ref' in data['items']:
                            item_type = f"com.swyp.mangro.remote.{module}.model." + data['items']['$ref'].split('/')[-1]
                            data_type = f'kotlin.collections.List<{item_type}>'
                        elif data.get('type') == 'array' and data['items'].get('type') == 'string':
                            data_type = 'kotlin.collections.List<kotlin.String>'
                        elif data.get('x-kotlin-type') == 'kotlinx.serialization.json.JsonElement':
                            data_type = 'kotlinx.serialization.json.JsonElement'
                        else:
                            raise ValueError('Unsupported response data: ' + name)
                        if operation.get('x-response-data-type', data_type) != data_type:
                            raise ValueError('Inconsistent success response data: ' + path)
                        operation['x-response-data-type'] = data_type
                        operation['x-response-nullable-data'] = data_type == 'kotlinx.serialization.json.JsonElement'
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


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--output', type=Path, default=ROOT / 'build/openapi')
    args = parser.parse_args()
    check_inputs(ROOT / 'openapi')
    raw = (ROOT / 'openapi' / INPUT_FILES[0]).read_bytes()
    expected = (ROOT / 'openapi/spec.sha256').read_text().split()[0]
    if hashlib.sha256(raw).hexdigest() != expected:
        raise ValueError('Source checksum mismatch; update the snapshot and checksum together')
    result = prepare(json.loads(raw), json.loads((ROOT / 'openapi/endpoint-map.json').read_text()), json.loads((ROOT / 'openapi/model-map.json').read_text()))
    args.output.mkdir(parents=True, exist_ok=True)
    for module, spec in result.items():
        (args.output / f'{module}.json').write_text(json.dumps(spec, ensure_ascii=False, indent=2, sort_keys=True) + '\n')


if __name__ == '__main__':
    main()
