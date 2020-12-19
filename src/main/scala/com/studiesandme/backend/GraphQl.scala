package com.studiesandme.backend

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import com.google.inject.Inject
import com.studiesandme.backend.common.SpecialExecutionTactics
import com.studiesandme.backend.tasks.TasksGraphQLSchema
import com.typesafe.scalalogging.StrictLogging
import sangria.ast.Document
import sangria.execution._
import sangria.marshalling.ResultMarshaller
import sangria.marshalling.sprayJson._
import sangria.schema._
import spray.json.{JsObject, JsValue}

import scala.concurrent.Future

case class NotImplementedException() extends RuntimeException("Not implemented yet")

object ErrorCodes extends Enumeration {
  type ErrorCodes = Value
  val Unknown        = Value("0000")
  val NotImplemented = Value("0001")
}

object SchemaDefinition extends SpecialExecutionTactics with TasksGraphQLSchema {

  def QueryType(): ObjectType[GraphQLService, Unit] = ObjectType(
    "Query",
    fields[GraphQLService, Unit](
      TaskQueries.tasks(),
    ),
  )

  def MutationType(): ObjectType[GraphQLService, Unit] = ObjectType(
    "Mutation",
    fields[GraphQLService, Unit](
      TaskMutations.createTask(),
      TaskMutations.markTaskCompleted(),
    ),
  )
}

trait GraphQl {
  def executeGraphQlQuery(
      query:     Document,
      operation: Option[String],
      vars:      Option[JsValue],
  ): Future[(StatusCode, JsValue)]
}

class GraphQlImpl @Inject() (service: GraphQLService) extends GraphQl with StrictLogging with SpecialExecutionTactics {

  val exceptionHandler = ExceptionHandler {
    case (m, e: NotImplementedException) =>
      HandledException(e.getMessage, errorCode(m, ErrorCodes.NotImplemented))
    case (m: ResultMarshaller, e: Throwable) =>
      logger.error("GraphQl failed", e)
      HandledException(e.getMessage, errorCode(m, ErrorCodes.Unknown))
  }

  def executeGraphQlQuery(
      query:     Document,
      operation: Option[String],
      vars:      Option[JsValue],
  ): Future[(StatusCode, JsValue)] = {
    val schema    = Schema(query = SchemaDefinition.QueryType, mutation = Some(SchemaDefinition.MutationType))
    val variables = vars.getOrElse(JsObject.empty)

    Executor
      .execute(
        schema           = schema,
        queryAst         = query,
        userContext      = service,
        variables        = variables,
        operationName    = operation,
        exceptionHandler = exceptionHandler,
      )
      .map(StatusCodes.OK -> _)
      .recover {
        case error: QueryAnalysisError =>
          StatusCodes.BadRequest -> error.resolveError
        case error: ErrorWithResolver =>
          StatusCodes.InternalServerError -> error.resolveError
      }
  }

  private def errorCode(m: ResultMarshaller, error: ErrorCodes.ErrorCodes): Map[String, ResultMarshaller#Node] =
    Map("code" -> m.scalarNode(error.toString, "String", Set.empty))
}
