package io.getquill.context

import scala.quoted._
import io.getquill.parser.ParserFactory
import io.getquill.metaprog.ExtractorsBundle
import io.getquill.MetaDsl
import io.getquill.SchemaMeta
import io.getquill.Unquote
import io.getquill.util.LoadObject
import io.getquill.querySchema

object SchemaMetaMacro {

  // inline def schemaMeta[T](inline entity: String, inline columns: (T => (Any, String))*): SchemaMeta[T] = 
  // SchemaMeta(quote { querySchema[T](entity, columns: _*) }, "1234") // TODO Don't need to generate a UID here.It can be static.
  def apply[T, P <: ParserFactory](qm: Expr[MetaDsl[P]], entity: Expr[String], columns: Expr[Seq[(T => (Any, String))]])(using Quotes, Type[T], Type[P]): Expr[SchemaMeta[T]] = {
    val tmc = new ExtractorsBundle
    import tmc._
    val parserFactory = LoadObject[P].get
    val uuid = Expr(java.util.UUID.randomUUID().toString)
    val exprs = 
      (columns match {
        case GenericSeq(argsExprs) => argsExprs
      }).toList
    //val quote = quoteImpl('{ $qm.querySchema[T]($entity, ${Expr.ofList(exprs)}: _*) })
    val quote = QuoteMacro('{ querySchema[T]($entity, $columns: _*) })
    '{ SchemaMeta($quote, $uuid) }
  }
}
