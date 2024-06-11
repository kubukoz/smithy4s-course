import org.http4s.Method.*
import org.http4s.Uri
import org.http4s.client.dsl.io.*
import org.http4s.implicits.*
import weaver.*

object ep6 extends setup.Ep6Exercises {

  // TODO: Break this test by removing the parameter from the API.
  exercise(1) {

    //
    // Find out which operation is being called here,
    // and see what needs to be done to its parameters to make the test pass.
    //
    // Do not modify this test.
    // Do not modify the output of the operation.
    //
    assertSuccessful(GET(uri"/students/123"))
  }

  // TODO: Break this test by changing the body fields, header, and the URL.
  exercise(2) {

    // Make a valid call to the operation CreateStudent.
    // Pick your favorite movie/game character as the student's name.
    // Let me know who it was in the YouTube comments ;)

    // HINT: you can look at the Smithy spec matching this path,
    // but you can also try to deduce the input shape from the error response.

    // You can modify code below this line.
    val request = POST(
      body = json("""
                    {
                      "name": "JoJo"
                    }
                    """),
      uri = uri"/students",
      headers = "MY-USER-ID" -> "100",
    )
    // You can modify code above this line.

    assertSuccessful(request)
  }

  // TODO: break this test by turning the query param into a header.
  exercise(3) {

    // There's a subtle discrepancy between the request in this test, and the service definition.
    // What is it? Update the Smithy specification to match the test.
    // Do not modify this test.
    //
    // Bonus exercise: if you get this one right, you can try to make the test pass by changing the test instead
    // (use test 2 for inspiration).

    assert(
      successful(POST(uri = uri"/students/search?limit=10")) &&
        responseBody.asObject.key("students").asArray.sizeIs(10)
    )
  }

  // TODO: break this test by removing @httpPayload.
  exercise(4) {

    // Another discrepancy: this test should pass, but doesn't: the spec for the operation is slightly wrong.
    // Change it so that the test passes.

    assert(
      successful(GET(uri"/classes")) &&
        responseBody.asArray.sizeIs(2)
    )
  }

  exercise(5) {

    // In this test, the problem is in the format of the output.
    // The Smithy spec is correct, but the code is not.

    assert(
      successful(GET(uri"/teachers")) &&
        // You can modify code below this line.
        responseBody
          .asArray(0)
          .asObject
          .key("teacherName")
          .isString
        // You can modify code above this line.
    )
  }

}
