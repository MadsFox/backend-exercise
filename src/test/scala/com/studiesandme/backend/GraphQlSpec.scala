package com.studiesandme.backend

import com.studiesandme.backend.common.Graphient.{GraphqlCall, Mutation, Query}
import com.studiesandme.backend.common.{QueryGenerator, StandardSpec, UnitTestSupport}
import com.studiesandme.backend.tasks._
import sangria.schema.Schema
import spray.json.DefaultJsonProtocol
import sangria.ast.Document
import org.mockito.Mockito._
import spray.json._

import java.time.Instant
import java.util.UUID
import scala.concurrent.Future

class GraphQlSpec extends StandardSpec with DefaultJsonProtocol with UnitTestSupport with TasksGraphQLSchema {
  var graphQl: GraphQl        = _
  var service: GraphQLService = _

  before {
    service = mock[GraphQLService]
    graphQl = new GraphQlImpl(service)
  }

  val schema = Schema(query = SchemaDefinition.QueryType, mutation = Some(SchemaDefinition.MutationType))

  it must "handle 'listTasks query" in {
    val taskList: List[Task] = List()
    when(service.listTasks())
      .thenReturn(Future.successful(taskList))
    val query = generateQuery(Query(TaskQueries.tasks()))

    val result = graphQl.executeGraphQlQuery(query, None, None).futureValue

    result._2 shouldBe QueryResponseDTO(Some(JsObject("tasks" -> taskList.toJson)), None).toJson
    verify(service).listTasks()
  }

  it must "handle 'markTaskCompleted query" in {
    val taskId: TaskId = TaskId.generate
    val task: Task =
      Task(
        id = taskId,
        "test task",
        "completed",
        Instant.now(),
        Instant.now())
    when(service.markTaskCompleted(taskId))
      .thenReturn(Future.successful(task))
    val query = generateQuery(Mutation(TaskMutations.markTaskCompleted()))

    val variables = Option(JsObject("id" -> taskId.toJson))

    val result = graphQl.executeGraphQlQuery(query, None, variables).futureValue

    result._2.asJsObject.getFields("description", "status", "id") shouldBe
      QueryResponseDTO(Some(JsObject("markTaskCompleted" -> task.toJson)), None).toJson.asJsObject.getFields("description", "status", "id")
    verify(service).markTaskCompleted(taskId)
  }

  private def generateQuery[Ctx, T](query: GraphqlCall[Ctx, T]): Document = {
    val generatedQuery = new QueryGenerator(schema).generateQuery(query)
    println(generatedQuery)
    generatedQuery.right.get
  }
}
