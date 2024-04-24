import org.http4s.Method.*
import org.http4s.Uri
import org.http4s.client.dsl.io.*
import org.http4s.implicits.*
import weaver.*

object ep6 extends setup.Ep6Exercises {

  exercise("Exercise 1") {
    //
    // Find out which operation is being called here,
    // and see what needs to be done to its parameters to make the test pass.
    //
    // Do not modify this test.
    // Do not modify the output of the operation.
    //
    assertSuccessful(GET(uri"/students/123"))
  }

  exercise("Exercise 2") {
    // Make a valid call to the operation CreateStudent.
    // Pick your favorite movie/game character as the student's name.
    // Let me know who it was in the YouTube comments ;)

    // HINT: you can look at the Smithy spec matching this path,
    // but you can also try to deduce the input shape from the error response.

    // You can modify code below this line.
    val request = POST(uri"/students").withEntity(
      json("""
      {
        "name": "JoJo"
      }
      """)
    )
    // You can modify code above this line.

    assertSuccessful(request)
  }

  exercise("Exercise 3") {
    // There's a subtle discrepancy between the request in this test, and the service definition.
    // What is it? Update the Smithy specification to match the test.
    // Do not modify this test.

    assert(
      successful(GET(uri"/students?limit=10")) &&
        responseBody.asObject.key("students").asArray.sizeIs(10)
    )
  }

  exercise("Exercise 4") {
    // Another discrepancy: this test should pass, but doesn't: the spec for the operation is slightly wrong.
    // Change it so that the test passes.

    assert(
      successful(GET(uri"/classes")) &&
        responseBody.asArray.sizeIs(2)
    )
  }

}
