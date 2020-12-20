package com.studiesandme.backend

import java.time.Instant
import com.studiesandme.backend.common.NewtypeSpray.Implicits._
import com.studiesandme.backend.common.NewtypeSpray.deriveJsonFormat
import com.studiesandme.backend.tasks.{TaskId, TaskStatus}
import spray.json.{DefaultJsonProtocol, JsNumber, JsString, JsValue, JsonFormat, deserializationError}

trait StudiesAndMeJsonFormatters extends DefaultJsonProtocol {
  implicit val taskIdFormat = deriveJsonFormat(TaskId.apply)

  implicit object TaskStatusFormat extends JsonFormat[TaskStatus] {
    override def write(obj: TaskStatus): JsValue = JsString(obj.value)
    override def read(json: JsValue): TaskStatus = json match {
      case JsString(x) => TaskStatus(x)
      case x =>
        deserializationError("Expected String as JsString, but got " + x)
    }
  }

  implicit object InstantJsonFormat extends JsonFormat[Instant] {
    def write(x:    Instant) = JsNumber(x.toEpochMilli)
    def read(value: JsValue) = value match {
      case JsNumber(x) => Instant.ofEpochMilli(x.longValue())
      case x =>
        deserializationError("Expected Time Instant as JsNumber, but got " + x)
    }
  }
}
