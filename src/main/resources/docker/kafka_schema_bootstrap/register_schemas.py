import glob
import json
import os
import requests
import time

time.sleep(10)

for file in glob.glob('/schemas/*.avsc'):
  name = os.path.basename(file).replace('.avsc', '')
  subject = f'{name}-value'

  with open(file, 'r') as f:
    schema = f.read()

  r = requests.post(
      f'http://kafka-0:8083/subjects/{subject}/versions',
      headers={'Content-Type': 'application/vnd.schemaregistry.v1+json'},
      json={'schema': schema}
  )

  print(subject, r.status_code, r.text)
