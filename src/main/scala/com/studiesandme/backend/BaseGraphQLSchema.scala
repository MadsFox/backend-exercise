package com.studiesandme.backend

import java.util.UUID
import com.studiesandme.backend.common.Newtype
import com.studiesandme.backend.tasks.{TaskId, TaskStatus}
import sangria.schema.{Argument, ScalarAlias, ScalarType, StringType}
import sangria.validation.ValueCoercionViolation

import scala.util.Try

trait BaseGraphQLSchema {
  def extractBaseId[A <: UUID](
      id:  String,
      map: String => Either[String, A],
  ): Either[BaseIdCoercionViolation, A] = {
    map(id) match {
      case Right(x)    => Right(x)
      case Left(error) => Left(BaseIdCoercionViolation(error))
    }
  }

  case class BaseIdCoercionViolation(error: String) extends ValueCoercionViolation(error)

  object ScalarHelpers {

    def scalarAliasFor[T: ScalarTypeOf, NT <: Newtype[T]](
        constructor: T => NT,
    ): ScalarAlias[NT, T] = {
      ScalarAlias[NT, T](
        implicitly[ScalarTypeOf[T]].typeOf,
        _.value,
        value => Right(constructor(value)),
      )
    }
    implicit val scalarTypeOfString = new ScalarTypeOf[String] {
      val typeOf = StringType
    }

    trait ScalarTypeOf[T] {
      val typeOf: ScalarType[T]
    }

    /* We define a general ScalarType for parsing incoming IDs as UUIDs.
    Schemas that extend BaseGraphQLSchema should implement a ScalarAlias
    that handles the aliasing for their respective internal ID types. */
    implicit val IDType = ScalarType[UUID](
      name = "ID",
      description = Some(
        "The `ID` scalar type represents identifier data in UUID format",
      ),
      coerceOutput = sangria.schema.valueOutput,
      coerceUserInput = {
        case s: String =>
          Try(UUID.fromString(s)).toEither match {
            case Right(uuid) => Right(uuid)
            case Left(_)     => Left(UUIDCoercionViolation)
          }
        case _ => Left(UUIDCoercionViolation)
      },
      coerceInput = {
        case sangria.ast.StringValue(s, _, _, _, _) =>
          Try(UUID.fromString(s)).toEither match {
            case Right(uuid) => Right(uuid)
            case Left(_)     => Left(UUIDCoercionViolation)
          }
        case _ => Left(UUIDCoercionViolation)
      },
    )

    implicit val scalarTypeOfID = new ScalarTypeOf[UUID] {
      val typeOf = IDType
    }

    case object UUIDCoercionViolation extends ValueCoercionViolation("String value expected")

    implicit val TaskIdType = scalarAliasFor(TaskId.apply)

    implicit val TaskStatusType = scalarAliasFor(TaskStatus.apply)

    implicit val TaskIdTypeArg = Argument("taskId", TaskIdType)
  }
}
