package com.studiesandme.backend.tasks

import com.studiesandme.backend.common.SpecialExecutionTactics
import sangria.macros.derive.{GraphQLInputTypeLookup, deriveInputObjectType}
import com.studiesandme.backend.{BaseGraphQLSchema, GraphQLService}
import sangria.marshalling.sprayJson._
import sangria.schema.{Argument, Field, InputObjectType, ListType, LongType, ObjectType, StringType, fields}

trait TasksGraphQLSchema extends BaseGraphQLSchema with SpecialExecutionTactics {
  import ScalarHelpers._

  val TaskType = ObjectType[Unit, Task](
    name = "task",
    fields[Unit, Task](
      Field("id", TaskIdType, resolve          = _.value.id),
      Field("description", StringType, resolve = _.value.description),
      Field("status", TaskStatusType, resolve  = _.value.status),
      Field(
        "createdAt",
        LongType,
        resolve = _.value.createdAt.getEpochSecond(),
      ),
      Field(
        "modifiedAt",
        LongType,
        resolve = _.value.modifiedAt.getEpochSecond(),
      ),
    ),
  )

  implicit val CreateTaskInputType: InputObjectType[CreateTaskInput] =
    deriveInputObjectType[CreateTaskInput]()
  val TaskIdArg = Argument("id", TaskIdType)
  val CreateTaskInputArg =
    Argument("input", CreateTaskInputType)

  implicit val UpdateTaskDescriptionInputType: InputObjectType[UpdateTaskDescriptionInput] =
    deriveInputObjectType[UpdateTaskDescriptionInput]()
  val UpdateTaskDescriptionInputArg =
    Argument("updateTaskDescriptionInput", UpdateTaskDescriptionInputType)


  object TaskQueries {
    def tasks(): Field[GraphQLService, Unit] = Field(
      "tasks",
      ListType(TaskType),
      description = Some("Returns all tasks"),
      resolve = c =>
        for {
          result <- c.ctx.listTasks()
        } yield result,
    )
  }

  object TaskMutations {
    def createTask(): Field[GraphQLService, Unit] = Field(
      "createTask",
      TaskType,
      description = Some("Create new task"),
      arguments   = CreateTaskInputArg :: Nil,
      resolve     = c => c.ctx.createTask(c.arg(CreateTaskInputArg)),
    )

    def markTaskCompleted(): Field[GraphQLService, Unit] = Field(
      "markTaskCompleted",
      TaskType,
      description = Some("Mark existing task completed"),
      arguments   = TaskIdArg :: Nil,
      resolve     = c => c.ctx.markTaskCompleted(c.arg(TaskIdArg)),
    )

    def updateTaskDescription(): Field[GraphQLService, Unit] = Field(
      "updateTaskDescription",
      TaskType,
      description = Some("Mark existing task completed"),
      arguments   = UpdateTaskDescriptionInputArg :: Nil,
      resolve     = c => c.ctx.updateTaskDescription(c.arg(UpdateTaskDescriptionInputArg)),
    )
  }
}
