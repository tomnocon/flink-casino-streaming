package com.tomnocon.cs.sink

case class ElasticsearchEvent(id: String, index: String, data: Map[String, Any]) {

  override def toString: String = s"ElasticsearchEvent{id=$id, index=$index, data=$data}"
}
