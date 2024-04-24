# exercises

Solve Smithy/Smithy4s problems to solidify your understanding.

## Prerequisites

- Scala syntax highlighting will probably be helpful
- For the general Smithy exercises, no Scala knowledge is needed.

## How to use this project

TODO

- **Do** modify code in `main` directories, including Smithy and Scala (where appropriate).
- Do NOT modify code in `test` directories, unless stated otherwise.
- You don't need to create new Smithy files, unless stated otherwise.

## How to check your solution

The exercises are implemented as Scala tests that you can run with `sbt`.
I recommend that you run `sbt` once, and then interactively give it commands in its REPL.

### For one lesson/video

To check your solutions for e.g. episode 6, write `testOnly ep6` and press ENTER in the `sbt` console.

If you want the task to re-run when you make changes, use `~testOnly ep6` instead.

### For all lessons/videos

In `sbt`, run `test` with no arguments.
