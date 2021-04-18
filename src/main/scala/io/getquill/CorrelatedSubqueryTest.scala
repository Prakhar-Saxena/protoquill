package io.getquill

import io.getquill.quoter.Dsl._
import io.getquill.quoter.Dsl.autoQuote
import io.getquill.parser._

object CorrelatedSubqueryTest {
  def main(args: Array[String]):Unit = {
    println('\n'*10)

    val ctx = new MirrorContext(MirrorSqlDialect, Literal)
    import ctx._

    case class Person(id: Int, firstName: String, lastName: String, companyId: Int)
    case class Address(id: Int, street: String, zip: Int, personId: Int)

    //COMPLEX
    inline def complex04_1 = quote {
      query[Person]
        .filter(p => query[Address]
          .filter(a => a.personId == p.id).nonEmpty)
    }
    // print(run(complex04_1).string)
    assert(run(complex04_1).string=="SELECT p.id, p.firstName, p.lastName, p.companyId FROM Person p WHERE EXISTS ( SELECT a.* FROM Address a WHERE a.personId = p.id )" ||
      run(complex04_1).string=="SELECT p.id, p.firstName, p.lastName, p.companyId FROM Person p WHERE (SELECT a.* FROM Address a WHERE a.personId = p.id).nonEmpty")

    //compile

    println('\n'*10)
  }
}
