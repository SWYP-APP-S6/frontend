import copy
import json
import unittest
from prepare_specs import INPUT_FILES, ROOT, operations, prepare


class PrepareSpecsTest(unittest.TestCase):
    def setUp(self):
        self.source = json.loads((ROOT / 'openapi' / INPUT_FILES[0]).read_text())
        self.mapping = json.loads((ROOT / 'openapi/endpoint-map.json').read_text())
        self.policies = json.loads((ROOT / 'openapi/model-map.json').read_text())

    def generate(self):
        return prepare(self.source, self.mapping, self.policies)

    def test_legacy_mapping_moves_only_profile_to_shared_user_module(self):
        self.mapping['GET /users/me']['module'] = 'consumer'
        result = self.generate()
        self.assertIn('/users/me', result['user']['paths'])
        self.assertNotIn('/users/me', result['consumer']['paths'])
        self.assertIn('/users/me/location', result['consumer']['paths'])
        self.assertEqual('consumer', self.mapping['GET /users/me']['module'])

    def test_partition_is_complete_and_disjoint(self):
        result = self.generate()
        counts = {k: len(list(operations(v))) for k, v in result.items()}
        self.assertEqual(counts, {'consumer': 15, 'owner': 17, 'auth': 10, 'user': 7})
        endpoints = [f'{m} {p}' for v in result.values() for m, p, _ in operations(v)]
        self.assertEqual(len(set(endpoints)), 49)

    def test_0918_owner_product_list_contract(self):
        owner = self.generate()['owner']
        operation = owner['paths']['/owner/products']['get']
        self.assertEqual('fetchMyProducts', operation['operationId'])
        self.assertEqual(
            'com.swyp.mangro.remote.owner.model.OwnerProductListResponse',
            operation['x-response-data-type'],
        )
        parameters = {parameter['name']: parameter['schema'] for parameter in operation['parameters']}
        self.assertEqual({'filter', 'page', 'size'}, set(parameters))
        self.assertEqual(['RUNNING_LOW', 'SOLD_OUT'], parameters['filter']['enum'])
        self.assertEqual(0, parameters['page']['default'])
        self.assertEqual(20, parameters['size']['default'])
        self.assertEqual(100, parameters['size']['maximum'])
        schemas = owner['components']['schemas']
        self.assertEqual(
            '#/components/schemas/PageResponseOwnerProductSummaryResponse',
            schemas['OwnerProductListResponse']['properties']['products']['$ref'],
        )

    def test_v3_owner_ingredient_contracts(self):
        owner = self.generate()['owner']
        self.assertIn('/owner/ingredients', owner['paths'])
        self.assertIn('/owner/ingredients/recommendations', owner['paths'])
        for path in ('/owner/ingredients', '/owner/ingredients/recommendations'):
            self.assertEqual(
                'kotlin.collections.List<com.swyp.mangro.remote.owner.model.IngredientTagResponse>',
                owner['paths'][path]['get']['x-response-data-type'],
            )
        schemas = owner['components']['schemas']
        for name in ('ProductDetailResponse', 'ProductPreviewResponse'):
            ingredient_tags = schemas[name]['properties']['ingredientTags']
            self.assertEqual(
                '#/components/schemas/IngredientTagResponse',
                ingredient_tags['items']['$ref'],
            )

    def test_owner_update_contracts(self):
        result = self.generate()
        self.assertIn('/notifications/device-tokens', result['user']['paths'])
        self.assertNotIn('/notifications', result['consumer']['paths'])
        self.assertIn('delete', result['user']['paths']['/users/me'])
        self.assertIn('/owner/holds/cancel-candidates', result['owner']['paths'])
        self.assertIn('/owner/holds/cancel', result['owner']['paths'])
        schemas = result['owner']['components']['schemas']
        self.assertNotIn('cancelOverflow', schemas['UpdateStockRequest']['properties'])
        for name in ('OwnerHomeResponse', 'OwnerHoldListResponse'):
            self.assertIn('serverTime', schemas[name]['required'])
        for module, path in [('owner', '/owner/holds'), ('user', '/notifications')]:
            params = result[module]['paths'][path]['get']['parameters']
            self.assertNotIn('sort', [p['name'] for p in params])
            self.assertEqual(100, next(p for p in params if p['name'] == 'size')['schema']['maximum'])

    def test_v2_consumer_pagination_is_explicit_and_bounded(self):
        result = self.generate()
        for path in ('/holds', '/recipes'):
            params = {p['name']: p['schema'] for p in result['consumer']['paths'][path]['get']['parameters']}
            self.assertNotIn('sort', params)
            self.assertNotIn('pageable', params)
            self.assertEqual(0, params['page']['default'])
            self.assertEqual(0, params['page']['minimum'])
            self.assertEqual(20, params['size']['default'])
            self.assertEqual(1, params['size']['minimum'])
            self.assertEqual(100, params['size']['maximum'])
        self.assertIn('category', {p['name'] for p in result['consumer']['paths']['/recipes']['get']['parameters']})

    def test_v2_pickup_time_contract_metadata_is_preserved(self):
        source = self.source['components']['schemas']['ProductRegisterRequest']['properties']['pickupEndAt']
        generated = self.generate()['owner']['components']['schemas']['RegisterProductRequest']['properties']['pickupEndAt']
        self.assertEqual('2026-09-17T22:00:00', generated['example'])
        self.assertEqual(source['description'], generated['description'])
        self.assertIn('한국 시간', generated['description'])

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
        deleted = result['user']['paths']['/notifications/device-tokens']['delete']
        self.assertTrue(deleted['x-delete-with-body'])
        self.assertIn('application/json', deleted['requestBody']['content'])
        self.assertNotIn('/owner/products/{id}/available-qty', result['owner']['paths'])

    def test_login_allows_absent_tokens_but_signup_returns_token_response(self):
        auth = self.generate()['auth']
        properties = auth['components']['schemas']['KakaoLoginResponse']['properties']
        for field in ('accessToken', 'refreshToken', 'signupToken'):
            self.assertEqual('kotlin.String?', properties[field]['x-kotlin-type'])
            self.assertEqual('null', properties[field]['x-kotlin-default'])
        signup = auth['paths']['/auth/signup']['post']
        self.assertEqual('com.swyp.mangro.remote.auth.model.TokenResponse', signup['x-response-data-type'])
        self.assertFalse(signup['x-response-nullable-data'])

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
