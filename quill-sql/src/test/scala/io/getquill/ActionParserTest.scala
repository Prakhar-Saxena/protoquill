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

object ActionParserTest {
  def main(args: Array[String]):Unit = {
    println('\n'*10)

    val ctx = new MirrorContext(MirrorSqlDialect, Literal)
    import ctx._

    case class Person(name:String, age:Int)

    "INSERT AP01" - {
      "AP01_1" - {
        inline def ap01_1 = quote {
          query[Person].insert(Person("John",21))
        }
        run(ap01_1).string mustEqual "INSERT INTO Person (name,age) VALUES ('John', 21)"
      }

      "AP01_2" - {
        inline def ap01_2 = quote {
          query[Person].insert(_.name -> "John", _.age -> 21)
        }
        run(ap01_2).string mustEqual "INSERT INTO Person (name,age) VALUES ('John', 21)"
      }
    }
    

    "UPDATE AP02" - {
      "AP02_1" - {
        inline def ap02_1 = quote {
          query[Person].update(_.name -> "Joe")
        }
        run(ap02_1).string mustEqual "UPDATE Person SET name = 'Joe'"
      }

      "AP02_2" - {
        inline def ap02_2 = quote {
          query[Person].filter(_.name == "John").update(_.name -> "Joe")
        }
        run(ap02_2).string mustEqual "UPDATE Person SET name = 'Joe' WHERE name = 'John'"
      }
    }
    
    "DELETE AP03" - {
      "AP03_1" - {
        inline def ap03_1 = quote {
          query[Person].delete
        }
        run(ap03_1).string mustEqual "DELETE FROM Person"
      }

      "AP03_2" - {
        inline def ap03_2 = quote {
          query[Person].filter(_.name == "John").delete
        }
        run(ap03_2).string mustEqual "DELETE FROM Person WHERE name = 'John'"
      }
    }
    
    println('\n'*10)
  }
}