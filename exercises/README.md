# exercises

Solve Smithy/Smithy4s problems to solidify your understanding.

## Prerequisites

- A code editor, e.g. [IntelliJ](https://www.jetbrains.com/help/idea/get-started-with-scala.html) or [VS Code](https://scalameta.org/metals/docs/editors/vscode)
- Scala/Smithy syntax highlighting will be helpful.
- **For the general Smithy exercises, no Scala knowledge is needed.** The changes you'll make to the Scala code will be minimal, if any.
- [sbt](https://www.scala-sbt.org/download/), for checking your solutions.

## How to use this project

The exercises are meant to complement the videos to help you learn, and to solidify your knowledge with practice.

Exercises grouped by video: there's one test suite (e.g. `object ep6`) for each video.

Each exercise is represented by a test.

- **Do** modify code in `main` directories, including Smithy and Scala (where appropriate).
- Do NOT modify code in `test` directories, unless stated otherwise.
- You don't need to create new Smithy files, unless stated otherwise.


## How to check your solution

The exercises are implemented as Scala tests that you can run with `sbt`, or your IDE/editor's test runner.
I recommend that you run `sbt` once, and then interactively give it commands in its REPL.

### For one lesson/video

To check your solutions for e.g. episode 6, write `testOnly ep6` and press ENTER in the `sbt` console.

If you want the task to re-run when you make changes, use `~testOnly ep6` instead.

**Note:** changes in Smithy files may not trigger a re-run.

### For all lessons/videos

In `sbt`, run `test` with no arguments.
