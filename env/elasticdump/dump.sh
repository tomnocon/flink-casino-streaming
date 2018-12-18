elasticdump --input=http://elasticsearch:9200/.kibana_1 --output=$ --retryAttempts 10 --retryDelay 2000
elasticdump --input=/var/elasticdump/kibana_mapping.json --output=http://elasticsearch:9200/.kibana_1 --type=mapping
elasticdump --input=/var/elasticdump/kibana_data.json --output=http://elasticsearch:9200/.kibana_1 --type=data