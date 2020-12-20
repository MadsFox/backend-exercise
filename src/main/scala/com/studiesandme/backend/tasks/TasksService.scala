package com.studiesandme.backend

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.google.inject.Inject
import com.studiesandme.backend.common.{Health, HealthCheck, Healthy, SpecialExecutionTactics}
import com.studiesandme.backend.tasks._
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.Future

trait TasksService {
  def create(contact: CreateTaskInput): Future[Task]
  def list(): Future[List[Task]]
  def markTaskCompleted(taskId: TaskId): Future[Task]
}

class TasksServiceImpl @Inject() (
    tasksRepository: TasksRepository,
) extends TasksService
    with HealthCheck
    with SpecialExecutionTactics
    with SprayJsonSupport
    with StrictLogging {

  override def isHealthy: Future[Health] = Future.successful(Healthy)

  override def create(input: CreateTaskInput): Future[Task] = {
    val newTask = Task.fromInput(input)
    tasksRepository.create(newTask)
  }

  override def list(): Future[List[Task]] = {
    tasksRepository.list()
  }

  override def markTaskCompleted(taskId: TaskId): Future[Task] =
    tasksRepository.updateTaskStatus(taskId, TaskStatus("completed"))
}
