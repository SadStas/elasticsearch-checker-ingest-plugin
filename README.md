# Elasticsearch checker Ingest Processor

Use this plugin if you need validate or check document field by some rules

## Usage

Validate email
```
PUT _ingest/pipeline/email-checker-pipeline
{
    "description": "Check is email valid by simple regex",
    "processors": [
        {
            "checker" : {
                "source_field": "email",
                "source_field_type": "string",
                "result_field": "is_email_valid",
                "check_operator": "match",
                "check_argument": "(^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$)"
            }
        }
    ]
}

PUT /my-index/my-type/1?pipeline=email-checker-pipeline
{
  "email" : "valid@email.com"
}

GET /my-index/my-type/1
{
  "email" : "valid@email.com",
  "is_email_valid": true
}
```

Check email domain is "gmail.com" or "yahoo.com" using prepare operator
```
PUT _ingest/pipeline/email-domain-checker-pipeline
{
    "description": "Check is email domain",
    "processors": [
        {
            "checker" : {
                "source_field": "email",
                "source_field_type": "string",
                "result_field": "is_email_has_popular_domain",
                "prepare_operator": "match",
                "prepare_argument": "^\\w*@([\\w\\.]*)$",
                "check_operator": "in",
                "check_argument": ["yahoo.com", "gmail.com"]
            }
        }
    ]
}

PUT /my-index/my-type/1?pipeline=email-domain-checker-pipeline
{
  "email" : "email@gmail.com"
}

GET /my-index/my-type/1
{
  "email" : "email@gmail.com",
  "is_email_has_popular_domain": true
}
```

## Configuration

| Parameter | Type | Values | Description |
| --- | --- | --- | --- |
| source_field | string | * | any document field |
| source_field_type | string | string, integer | now supported only strings and integers |
| result_field | string | * | field in document to write boolean result |
| check_operator | string | for source_field_type=string: equal, match, contains, in\nfor source_field_type=integer: equal, in, more, less | - |
| check_argument | string, integer, array | * | - |
| prepare_operator (optional) | string | match, split | operator to prepare value before check |
| prepare_argument (required with prepare_operator) | string | * | argument to prepare value before check |
| prepare_item (optional) | integer | * | split or match item (0 as default) |

## Setup

In order to install this plugin, you need to create a zip distribution first by running

```bash
gradle clean check
```

This will produce a zip file in `build/distributions`.

After building the zip file, you can install it like this

```bash
bin/elasticsearch-plugin install file:///path/to/ingest-checker/build/distribution/ingest-checker-0.0.1-SNAPSHOT.zip
```

## Bugs & TODO

* Fix lot of bugs
* Add more operators and move source_field_types

## Links

Created by [tutorial](https://www.elastic.co/blog/writing-your-own-ingest-processor-for-elasticsearch) with using [python magic script](https://github.com/audreyr/cookiecutter)

