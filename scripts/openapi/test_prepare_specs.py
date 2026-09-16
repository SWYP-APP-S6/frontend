import copy
import json
import unittest
from prepare_specs import ROOT, operations, prepare


class PrepareSpecsTest(unittest.TestCase):
    def setUp(self):
        self.source = json.loads((ROOT / 'openapi/mangro-app-openapi-2026-09-15.json').read_text())
        self.mapping = json.loads((ROOT / 'openapi/endpoint-map.json').read_text())
        self.policies = json.loads((ROOT / 'openapi/model-map.json').read_text())

    def generate(self):
        return prepare(self.source, self.mapping, self.policies)

    def test_partition_is_complete_and_disjoint(self):
        result = self.generate()
        counts = {k: len(list(operations(v))) for k, v in result.items()}
        self.assertEqual(counts, {'consumer': 21, 'owner': 12, 'auth': 10})
        endpoints = [f'{m} {p}' for v in result.values() for m, p, _ in operations(v)]
        self.assertEqual(len(set(endpoints)), 43)

    def test_deterministic_and_does_not_mutate_source(self):
        before = copy.deepcopy(self.source)
        self.assertEqual(self.generate(), self.generate())
        self.assertEqual(before, self.source)

    def test_bearer_security_is_preserved_without_invented_operation_exceptions(self):
        self.assertEqual(self.source['security'], [{'bearerAuth': []}])
        for spec in self.generate().values():
            self.assertEqual(spec['security'], self.source['security'])
            self.assertEqual(spec['components']['securitySchemes'], self.source['components']['securitySchemes'])
            for path, item in spec['paths'].items():
                for method, operation in item.items():
                    if method in ('get', 'post', 'put', 'patch', 'delete'):
                        self.assertEqual(operation.get('security'), self.source['paths'][path][method].get('security'))

    def test_new_endpoint_fails_instead_of_being_silently_dropped(self):
        self.source['paths']['/new'] = {'get': {'operationId': 'new'}}
        with self.assertRaisesRegex(ValueError, 'mapping mismatch'):
            self.generate()

    def test_duplicate_method_fails(self):
        self.mapping['POST /auth/refresh']['method'] = self.mapping['POST /auth/logout']['method']
        with self.assertRaisesRegex(ValueError, 'Duplicate'):
            self.generate()

    def test_unmapped_required_enum_fails(self):
        del self.policies['enumDefaults']['ProductRegisterRequest.category']
        with self.assertRaisesRegex(ValueError, 'Missing enum default'):
            self.generate()

    def test_wire_contract_is_preserved_after_resolving_renamed_schemas(self):
        def resolve(value, schemas):
            if isinstance(value, dict):
                if '$ref' in value:
                    return resolve(schemas[value['$ref'].split('/')[-1]], schemas)
                return {k: resolve(v, schemas) for k, v in value.items()
                        if not k.startswith('x-') and k not in ('operationId', 'tags')
                        and not (k == 'format' and v == 'double')}
            if isinstance(value, list):
                return [resolve(v, schemas) for v in value]
            return value
        for spec in self.generate().values():
            for method, path, op in operations(spec):
                original = copy.deepcopy(self.source['paths'][path][method])
                parameters = []
                for parameter in original.get('parameters', []):
                    if parameter.get('schema', {}).get('$ref') == '#/components/schemas/Pageable':
                        for field, schema in self.source['components']['schemas']['Pageable']['properties'].items():
                            parameters.append({'name': field, 'in': 'query', 'required': False,
                                               'schema': schema, 'style': 'form', 'explode': True})
                    else:
                        parameters.append(parameter)
                if 'parameters' in original:
                    original['parameters'] = parameters
                self.assertEqual(resolve(op, spec['components']['schemas']),
                                 resolve(original, self.source['components']['schemas']))

    def test_non_null_recursive_defaults_fail(self):
        model = self.source['components']['schemas']['TokenResponse']
        model['properties']['child'] = {'$ref': '#/components/schemas/TokenResponse'}
        model['required'].append('child')
        with self.assertRaisesRegex(ValueError, 'Recursive non-null'):
            self.generate()

    def test_source_reference_does_not_silently_resolve_to_different_model(self):
        self.mapping['POST /auth/refresh']['method'] = 'token'
        with self.assertRaisesRegex(ValueError, 'Model name collision'):
            self.generate()

    def test_v2_multipart_and_delete_body_are_preserved(self):
        result = self.generate()
        photo = result['owner']['paths']['/owner/products/photos']['post']
        schema = photo['requestBody']['content']['multipart/form-data']['schema']
        self.assertEqual('binary', schema['properties']['file']['format'])
        self.assertEqual(['file'], schema['required'])
        deleted = result['consumer']['paths']['/notifications/device-tokens']['delete']
        self.assertTrue(deleted['x-delete-with-body'])
        self.assertIn('application/json', deleted['requestBody']['content'])
        self.assertNotIn('/owner/products/{id}/available-qty', result['owner']['paths'])

    def test_nullable_fields_and_all_defaults(self):
        for spec in self.generate().values():
            for model in spec['components']['schemas'].values():
                required = model.get('required', [])
                for name, prop in model.get('properties', {}).items():
                    self.assertIn('x-kotlin-default', prop)
                    if name not in required:
                        self.assertEqual('null', prop['x-kotlin-default'])


if __name__ == '__main__':
    unittest.main()
