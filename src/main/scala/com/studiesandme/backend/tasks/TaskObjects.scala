package com.studiesandme.backend

import java.util.UUID

import com.studiesandme.backend.common.Newtype
import com.studiesandme.backend.common.NewtypeSpray.Implicits._
import com.studiesandme.backend.common.NewtypeSpray.deriveJsonFormat
import spray.json.RootJsonFormat

package tasks {

  import com.studiesandme.backend.common.NewtypeSpray.deriveRootJsonFormat

  import java.time.Instant

  final case class TaskId(value: UUID) extends Newtype[UUID] {
    override def toString: String = value.toString
  }
  object TaskId {
    implicit val taskIdFormat = deriveJsonFormat(TaskId.apply)
    def generate: TaskId = TaskId(UUID.randomUUID())
  }

  final case class TaskStatus(value: String) extends Newtype[String] {
    //Know that there is a Enum in Scala, but haven't had great success with their implementation
    require(value == "completed" || value == "pending", "TaskStatus must have value 'completed' or 'pending'")
    override def toString: String = value
  }

  final case class CreateTaskInput(
      description: String,
  )
  object CreateTaskInput extends StudiesAndMeJsonFormatters {
    implicit val createTaskInputFormat: RootJsonFormat[CreateTaskInput] =
      jsonFormat1(CreateTaskInput.apply)
  }

  final case class UpdateTaskDescriptionInput(
      id: TaskId,
      description: String
  )
  object UpdateTaskDescriptionInput extends StudiesAndMeJsonFormatters {
    def tupled = (UpdateTaskDescriptionInput.apply _).tupled
    implicit val updateTaskDescriptionInputFormat: RootJsonFormat[UpdateTaskDescriptionInput] =
      jsonFormat2(UpdateTaskDescriptionInput.apply)
  }


  final case class Task(
      id:          TaskId,
      description: String,
      status:      TaskStatus,
      createdAt:   Instant,
      modifiedAt:  Instant,
  )
  object Task extends StudiesAndMeJsonFormatters {
    def tupled = (Task.apply _).tupled

    implicit val taskFormat: RootJsonFormat[Task] =
      jsonFormat5(Task.apply)

    def fromInput(input: CreateTaskInput): Task = {
      Task(
        id          = TaskId.generate,
        description = input.description,
        status      = TaskStatus("pending"),
        createdAt   = Instant.now(),
        modifiedAt  = Instant.now(),
      )
    }
  }

}
