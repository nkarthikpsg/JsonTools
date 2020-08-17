# JsonTools

A Json Generator tool to generate Jsons from template

## Installation

### Docker

An image is available on [Docker Hub](https://hub.docker.com/r/knatarajan/json-tools/). Run:

```sh
docker run -ti -p=8080:8080 knatarajan/json-tools
```

## Available dynamic fields

{{$uid}} - Generates 16 digit UUID

{{$index}} - Index within iteration 

{{$index_num}} - Index within iteration and generates a Number type e.g. "ChildTypeSerial": 2

{{$repeat(10)}} - Runs iteration from 1 to 10.

{{$repeat(11,20)}} - Runs iteration from 11 to 20.

## Sample Input

```http
POST /api/tools/json/generate HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
    "ParentId": "id_aug_30_01",
    "ParentRandomId": "{{$uid}}",
    "ParentName": "First Last",
    "ParentAge": 30,
    "Child": [
        {
            "{{$repeat(3)}}": {
                "ChildType": "Type1",
                "ChildId": "Type1_{{$index}}",
                "ChildTypeSerial": "{{$index_num}}"
            }
        },
        {
            "{{$repeat(2)}}": {
                "ChildType": "Type2",
                "ChildId": "Type3_{{$index}}",
                "ChildTypeSerial": "{{$index_num}}",
                "GrandChild": [
                    {
                        "{{$repeat(2)}}": {
                            "GarndChildType": "Type2",
                            "GrandChildId": "Type2_{{$index}}",
                            "ChildTypeSerial": "{{$index_num}}"
                        }
                    }
                ]
            }
        }
    ],
    "Sibling": [
        {
            "{{$repeat(3)}}": {
                "SiblingType": "Type1",
                "SiblingId": "Type1_{{$index}}",
                "SiblingTypeSerial": "{{$index_num}}"
            }
        }
    ]
}
```

Returns JSON structured like this:

```json
{
  "anomalies": [
    "2018-01-10",
    "2018-01-13"
  ]
}
```

