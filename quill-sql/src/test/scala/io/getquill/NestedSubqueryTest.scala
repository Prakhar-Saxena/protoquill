package io.getquill

import scala.language.implicitConversions
import io.getquill.Dsl._
import io.getquill.Quoted
import io.getquill._
import io.getquill._
import io.getquill.ast._
import io.getquill.QuotationLot
import io.getquill.QuotationVase
import io.getquill.context.ExecutionType
import org.scalatest._
import io.getquill.quat.quatOf
import io.getquill.context.ExecutionType.Static
import io.getquill.context.ExecutionType.Dynamic

object NestedSubqueryTest {
  def main(args: Array[String]):Unit = {
    println('\n'*10)

    val ctx = new MirrorContext(MirrorSqlDialect, Literal)
    import ctx._

    case class Person(id: Int, firstName: String, lastName: String, companyId: Int)
    case class Address(id: Int, street: String, zip: Int, personId: Int)

    "COMPLEX03_1" in {
      inline def complex03_1 = quote {
        query[Person]
          .nested
            .nested
              .nested
                .nested
      }
      // print(run(complex03_1).string)
      complex03_1).string mustEqual "SELECT x.id, x.firstName, x.lastName, x.companyId FROM (SELECT x.id, x.firstName, x.lastName, x.companyId FROM (SELECT x.id, x.firstName, x.lastName, x.companyId FROM (SELECT x.id, x.firstName, x.lastName, x.companyId FROM (SELECT x.id, x.firstName, x.lastName, x.companyId FROM Person x) AS x) AS x) AS x) AS x"
    }

    //compilee

    println('\n'*10)
  }
}
