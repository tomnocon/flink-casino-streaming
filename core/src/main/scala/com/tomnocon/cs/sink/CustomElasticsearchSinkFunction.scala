package com.tomnocon.cs.sink

import org.apache.flink.api.common.functions.RuntimeContext
import org.apache.flink.streaming.connectors.elasticsearch.{ElasticsearchSinkFunction, RequestIndexer}
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.Requests
import scala.collection.JavaConversions.mapAsJavaMap

class CustomElasticsearchSinkFunction extends ElasticsearchSinkFunction[ElasticsearchEvent]{

  override def process(event: ElasticsearchEvent, runtimeContext: RuntimeContext, requestIndexer: RequestIndexer): Unit = {
    requestIndexer.add(createIndexRequest(event))
  }

  def createIndexRequest(event: ElasticsearchEvent): IndexRequest = {
    Requests.indexRequest()
      .id(event.id)
      .index(event.index)
      .`type`("_doc")
      .source(mapAsJavaMap(event.data))
  }
}
