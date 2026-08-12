import glob
import json
import os
import requests
import time

time.sleep(10)

SCHEMA_REGISTRY_URL = 'http://kafka-0:8083'

TOPIC_MAP = {
  'BookDescription': [
    'book-description.local.kafka_demo.book-description.v1',
    'kafka-playground-app-book-description.local.kafka_demo.book-description.v1-store-changelog',
  ],
  'Character': [
    'character.local.kafka_demo.character.v1',
  ],
  'Employee': [
    'employee.local.kafka_demo.employee.v1',
  ],
  # add any other schema -> topic mappings here
}

for file in glob.glob('/schemas/*.avsc'):
  name = os.path.basename(file).replace('.avsc', '')

  if name not in TOPIC_MAP:
    print(f'SKIP {name}: no topic mapping defined')
    continue

  with open(file, 'r') as f:
    schema = f.read()

  for topic in TOPIC_MAP[name]:
    subject = f'{topic}-value'

    r = requests.post(
        f'{SCHEMA_REGISTRY_URL}/subjects/{subject}/versions',
        headers={'Content-Type': 'application/vnd.schemaregistry.v1+json'},
        json={'schema': schema}
    )

    if r.ok:
      schema_id = r.json().get('id')
      print(f'OK   {subject} -> id {schema_id}')
    else:
      print(f'FAIL {subject} ({r.status_code}): {r.text}')
