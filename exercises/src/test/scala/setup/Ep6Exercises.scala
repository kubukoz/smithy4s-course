package setup

import cats.syntax.all.*
import smithy4s.Document
import smithy4s.Document.DObject
import smithy4s.Document.DString
import specs.ep6._
import weaver.*

import scala.util.Random

trait Ep6Exercises extends Exercises {
  import ExerciseAPI.*

  val exercises: Int => PartialImpl = makeExercises(
    forOperation(StudentServiceOperation.GetStudent) {
      toDynamicOutputOnly { input =>
        val paramValue =
          input match {
            case DObject(value) if value.sizeIs == 1 =>
              value.head._2 match {
                case DString(value) => value
                case other =>
                  sys.error(
                    s"Expected a string, but got $other (${GetStudentInput.schema.firstFieldId})"
                  )
              }

            case DObject(v) =>
              sys.error(
                s"The operation had ${v.size} inputs, but it should have had 1! Did you cheat? ;)"
              )

            case _ => sys.error("shouldn't be possible: operation inputs are structs")
          }

        GetStudentOutput(name = "Output for input " + paramValue)
      }
    },
    forStaticOperation(StudentServiceOperation.CreateStudent) { input =>
      CreateStudentOutput(
        id = Random.nextInt().show,
        name = input.name,
      )
    },
    forStaticOperation(StudentServiceOperation.ListStudents) { input =>
      ListStudentsOutput(
        List.fill(input.maxStudents.getOrElse(20))(
          Student(name = s"Student ${Random.nextInt()}")
        )
      )
    },
    forStaticOperation(StudentServiceOperation.ListClasses) { _ =>
      ListClassesOutput(
        List(Class("Chemistry", "Maya"), Class("Music", "Eve"))
      )
    },
  )

}
