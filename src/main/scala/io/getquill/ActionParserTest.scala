package io.getquill

import io.getquill.quoter.Dsl._
import io.getquill.quoter.Dsl.autoQuote
import io.getquill.parser._

object ActionParserTest {
  def main(args: Array[String]):Unit = {
    println('\n'*10)

    val ctx = new MirrorContext(MirrorSqlDialect, Literal)
    import ctx._

    case class Person(name:String, age:Int)

    //INSERT AP01

    inline def ap01_1 = quote {
      query[Person].insert(Person("John",21))
    }
    assert(run(ap01_1).string=="INSERT INTO Person (name,age) VALUES ('John', 21)")

    inline def ap01_2 = quote {
      query[Person].insert(_.name -> "John", _.age -> 21)
    }
    assert(run(ap01_2).string=="INSERT INTO Person (name,age) VALUES ('John', 21)")


    //UPDATE AP02

    inline def ap02_1 = quote {
      query[Person].update(_.name -> "Joe")
    }
    assert(run(ap02_1).string=="UPDATE Person SET name = 'Joe'")

    inline def ap02_2 = quote {
      query[Person].filter(_.name == "John").update(_.name -> "Joe")
    }
    assert(run(ap02_2).string=="UPDATE Person SET name = 'Joe' WHERE name = 'John'")

    
    //DELETE AP03
    
    inline def ap03_1 = quote {
      query[Person].delete
    }
    assert(run(ap03_1).string=="DELETE FROM Person")

    inline def ap03_2 = quote {
      query[Person].filter(_.name == "John").delete
    }
    assert(run(ap03_2).string=="DELETE FROM Person WHERE name = 'John'")

    println('\n'*10)
  }
}