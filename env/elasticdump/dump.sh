until elasticdump --input=http://elasticsearch:9200/.kibana_1 --output=$;
do
    elasticdump --input=/var/elasticdump/kibana_mapping.json --output=http://elasticsearch:9200/.kibana_1 --type=mapping
    elasticdump --input=/var/elasticdump/kibana_data.json --output=http://elasticsearch:9200/.kibana_1 --type=data
done