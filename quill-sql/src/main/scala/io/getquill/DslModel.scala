package io.getquill

import io.getquill.parser._
import scala.quoted._
import scala.annotation.StaticAnnotation
import io.getquill.util.printer.AstPrinter
import scala.deriving._
import io.getquill.generic.GenericEncoder
import io.getquill.quotation.NonQuotedException
import scala.annotation.compileTimeOnly
import io.getquill.Query
import io.getquill.EntityQuery

object EntityQuery {
  def apply[T] = new EntityQuery[T]() { }
}

def querySchema[T](entity: String, columns: (T => (Any, String))*): EntityQuery[T] = NonQuotedException()

trait EntityQuery[T] extends EntityQueryModel[T]

case class Quoted[+T](val ast: io.getquill.ast.Ast, lifts: List[Planter[_, _]], runtimeQuotes: List[QuotationVase]) {
  override def toString = io.getquill.util.Messages.qprint(this).plainText
}

// Planters contain trees that can be re-inserted into compile-time code.
// For example, a ScalarPlanter is re-inserted into the PrepareRow sequence
// Note that we cannot assume the unquote is just T since unquoted values can be 
// different e.g. in EagerEntityListPlaner
sealed trait Planter[T, PrepareRow] {
  def unquote: T
  def uid: String
}

case class InjectableEagerPlanter[T, PrepareRow](inject: _ => T, encoder: GenericEncoder[T, PrepareRow], uid: String) extends Planter[T, PrepareRow] {
  def unquote: T =
    throw new RuntimeException("Unquotation can only be done from a quoted block.")
}

case class EagerPlanter[T, PrepareRow](value: T, encoder: GenericEncoder[T, PrepareRow], uid: String) extends Planter[T, PrepareRow] {
  def unquote: T =
    throw new RuntimeException("Unquotation can only be done from a quoted block.")
}

case class LazyPlanter[T, PrepareRow](value: T, uid: String) extends Planter[T, PrepareRow] {
  def unquote: T =
    throw new RuntimeException("Unquotation can only be done from a quoted block.")
}

// Equivalent to CaseClassValueLift
case class EagerEntitiesPlanter[T, PrepareRow](value: Iterable[T], uid: String) extends Planter[Query[T], PrepareRow] {
  def unquote: Query[T] =
    throw new RuntimeException("Unquotation can only be done from a quoted block.")
}

// Stores runtime quotation tree. This is holder for quotations that are not inline thus can never be re-inserted into
// the ast (i.e. the ground... metaphorically speaking), therefore this holder is called Vase. The contents of the
// QuotationVase are only inserted back in during runtime.
case class QuotationVase(quoted: Quoted[Any], uid: String)

// Quotations go from a QuotationLot directly inline into the tree or put into a QuotationVase
// to be added into the runtime inlining later
// NOTE: Don't want to include quoted: Quoted[T] since the inner stored type might be different that the thing returned. 
//       currently there's no use-case for that but perhaps in future.
trait QuotationLot[+T](uid: String) {
  // TODO I think we should get rid of this operator. Unquote should be put on this via an implicit class which causes
  // invocation of the unquote macro?
  def unquote: T =
    throw new RuntimeException("Unquotation can only be done from a quoted block.")
}

case class Unquote[+T](quoted: Quoted[T], uid: String) extends QuotationLot[T](uid)

// TODO Does this need to be covariant? It is in current quill. Need to look up what use cases they are for covariant schemas.
case class SchemaMeta[T](val entity: Quoted[EntityQuery[T]], uid: String) extends QuotationLot[EntityQuery[T]](uid)

case class InsertMeta[T](val entity: Quoted[T], uid: String) extends QuotationLot[T](uid)

case class UpdateMeta[T](val entity: Quoted[T], uid: String) extends QuotationLot[T](uid)

// enum ActionMetaType { Insert, Update }
// or with traits?
// trait ActionMetaType; object ActionMetaType { trait Insert extends ActionMetaType; trait Update extends ActionMetaType }

// trait ActionMeta[T, MetaType]
// case class InsertMeta[T](val entity: Quoted[T], uid: String) extends ActionMeta[T, Insert] with QuotationLot[T](entity, uid)

// Then ActionMacro will take a MT (i.e. MetaType) generic argument that will control what to summon and what kind of AST
// element Ast.Insert or Ast.Update to return (also there should probably be 'Delete' meta type which does not summon a column-excluding meta) 

case class QueryMeta[T, R](val entity: Quoted[Query[T] => Query[R]], uid: String, extract: R => T) extends QuotationLot[Query[T] => Query[R]](uid)

// TODO Rename to EntityLift
// Equivalent to CaseClassValueLift
case class CaseClassLift[T](val entity: Quoted[T], uid: String) extends QuotationLot[T](uid)
