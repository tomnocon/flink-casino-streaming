package com.tomnocon.cs.model

import org.apache.flink.streaming.api.functions.AssignerWithPunctuatedWatermarks
import org.apache.flink.streaming.api.watermark.Watermark

class MachineEventWatermarkEmitter extends AssignerWithPunctuatedWatermarks[MachineEvent]{
  override def checkAndGetNextWatermark(lastElement: MachineEvent, extractedTimestamp: Long): Watermark = {
    new Watermark(extractedTimestamp)
  }

  override def extractTimestamp(element: MachineEvent, previousElementTimestamp: Long): Long = {
    element.timestamp
  }
}
