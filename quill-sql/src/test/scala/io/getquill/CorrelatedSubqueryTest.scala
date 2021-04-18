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

object CorrelatedSubqueryTest {
  def main(args: Array[String]):Unit = {
    println('\n'*10)

    val ctx = new MirrorContext(MirrorSqlDialect, Literal)
    import ctx._

    case class Person(id: Int, firstName: String, lastName: String, companyId: Int)
    case class Address(id: Int, street: String, zip: Int, personId: Int)

    "COMPLEX04_1" - {
      inline def complex04_1 = quote {
        query[Person]
          .filter(p => query[Address]
            .filter(a => a.personId == p.id).nonEmpty)
      }
      // print(run(complex04_1).string)
      // run(complex04_1).string mustEqual "SELECT p.id, p.firstName, p.lastName, p.companyId FROM Person p WHERE EXISTS ( SELECT a.* FROM Address a WHERE a.personId = p.id )" ||
      complex04_1).string mustEqual "SELECT p.id, p.firstName, p.lastName, p.companyId FROM Person p WHERE (SELECT a.* FROM Address a WHERE a.personId = p.id).nonEmpty"
    }

    //compilee

    println('\n'*10)
  }
}
