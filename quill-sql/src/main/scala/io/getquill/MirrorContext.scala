package io.getquill

import io.getquill.context.mirror._
import io.getquill.context._
import io.getquill.Quoted
import io.getquill.idiom.Idiom
import io.getquill.NamingStrategy

sealed trait Dummy
object DummyInst extends Dummy

// TODO Dialect <: Idiom, Naming <: NamingStrategy are not actually needed so can remove them but that causes a compile-time error. Need to figure that out
// TODO Modify some major unit tests with embedded etc... to make sure this works in all cases
trait MirrorColumnResolving[Dialect <: Idiom, Naming <: NamingStrategy] { self: MirrorContextBase[Dialect, Naming] =>
  given mirrorResover: ColumnResolver with {
    def apply(resultRow: Row, columnName: String): Int = resultRow.indexOfKey(columnName)
  }
}

class MirrorContext[Dialect <: Idiom, Naming <: NamingStrategy](val idiom: Dialect, val naming: Naming) extends MirrorContextBase[Dialect, Naming]

trait MirrorContextBase[Dialect <: Idiom, Naming <: NamingStrategy]
extends Context[Dialect, Naming] 
with MirrorDecoders with MirrorEncoders { self =>
  override type Result[T] = T
  override type RunQueryResult[T] = QueryMirror[T]
  override type PrepareRow = Row
  override type ResultRow = Row
  override type RunActionResult = ActionMirror
  override type RunActionReturningResult[T] = ActionReturningMirror[T]
  override type RunBatchActionResult = BatchActionMirror

  override type DatasourceContext = Unit
  override def context: DatasourceContext = ()

  // TODO Not needed, get rid of this
  implicit val d: Dummy = DummyInst

  case class QueryMirror[T](string: String, prepareRow: PrepareRow, extractor: Extractor[T], executionType: ExecutionType) {
    def string(pretty: Boolean): String =
      if (pretty)
        idiom.format(string)
      else
        string
  }

  case class ActionMirror(string: String, prepareRow: PrepareRow, executionType: ExecutionType)
  case class ActionReturningMirror[T](string: String, prepareRow: PrepareRow, extractor: Extractor[T], returningBehavior: ReturnAction, executionType: ExecutionType)
  case class BatchActionMirror(groups: List[(String, List[Row])], executionType: ExecutionType)

  override def executeQuery[T](string: String, prepare: Prepare = identityPrepare, extractor: Extractor[T] = identityExtractor)(executionType: ExecutionType, dc: DatasourceContext) =
    QueryMirror(string, prepare(Row())._2, extractor, executionType)

  override def executeAction[T](string: String, prepare: Prepare = identityPrepare)(executionType: ExecutionType, dc: DatasourceContext): Result[RunActionResult] =
    ActionMirror(string, prepare(Row())._2, executionType)

  def executeActionReturning[T](sql: String, prepare: Prepare = identityPrepare, extractor: Extractor[T], returningBehavior: ReturnAction)(executionType: ExecutionType, dc: DatasourceContext): Result[RunActionReturningResult[T]] =
    ActionReturningMirror[T](sql, prepare(Row())._2, extractor, returningBehavior, executionType)

  override def executeBatchAction(groups: List[BatchGroup])(executionType: ExecutionType, dc: DatasourceContext): Result[RunBatchActionResult] =
    BatchActionMirror(
      groups.map {
        case BatchGroup(string, prepare) =>
          (string, prepare.map(_(Row())._2))
      },
      executionType
    )
    
}