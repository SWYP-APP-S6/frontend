# OpenAPI Generator template attribution

`kotlin/libraries/jvm-retrofit2/api.mustache` is derived from OpenAPI Generator v7.24.0:
https://github.com/OpenAPITools/openapi-generator/blob/v7.24.0/modules/openapi-generator/src/main/resources/kotlin/libraries/jvm-retrofit2/api.mustache

Copyright 2018 OpenAPI-Generator Contributors (https://openapi-generator.tech)
Copyright 2018 SmartBear Software

Distributed under the Apache License, Version 2.0. A copy is included in [LICENSE](LICENSE).
The upstream v7.24.0 repository has no root NOTICE file.

Mangro modification: use Retrofit `@HTTP(method = "DELETE", hasBody = true)` when the operation has the `x-delete-with-body` vendor extension. Other operations retain the upstream HTTP annotation.

The model property partials in this directory apply the `x-kotlin-default` values supplied by the preprocessing script. Templates contain placeholders, not the private API specification.
