# Project context

This repository is a starter template for a greenfield Java project used in an introductory software engineering course in an undergraduate computer science program. Students use it as the starting point for their own projects.

# Default user context

Unless the user says otherwise, assume that you are assisting a student working on a project in this repository. If the user identifies themselves as an instructor or another project stakeholder, adapt your response to that role.

# Student profile

* Prior knowledge: Basic Java and OOP concepts.
* Level of programming experience: Intermediate
* IDE and level of expertise: terminal and intermediate

# Guidance for interacting with users

* Explain the rationale for significant actions: what you did and why.
* Keep explanations brief but instructive, supporting learning through responsible use of AI. For example:

  * When suggesting a Git command, briefly explain what it does.
  * Add explanatory Javadoc comments to all classes and to nontrivial methods and fields when their purpose or behavior is not obvious.
  * Make generated code as self-explanatory as possible, and include explanatory comments where they improve understanding.
  * When faced with a design choice, choose the simplest option that is sufficient for the requirements, while briefly explaining relevant more advanced alternatives.

# Project-specific requirements

## Java version:

Ensure that Java 25 is used when running the application or build tasks. On macOS, use `sdk use java 25.0.3.fx-zulu` to switch to Java 25 if needed.

## Coding standard

All Java code in this project follows the SE-EDU Java coding standard
(intermediate level). The rules are captured in the project skill
`seedu-java-coding-standard`; load it before writing or editing any `.java`
file, and apply it to new code as well as to code being changed.

The two rules most often missed here: imports are grouped `static`, `java`,
`javax`, `org`, `com`, then `goat`, with a blank line between groups; and
comments use American spelling.

## Testing

JUnit 5 tests live in `src/test/java`, mirroring the package of the class
under test (`goat.task.TaskList` is tested by `src/test/java/goat/task/TaskListTest.java`).
Run them with `./gradlew test`; `./gradlew build` runs them too.

Name test methods `featureUnderTest_testScenario_expectedBehavior()`,
e.g. `delete_outOfRange_exceptionThrown()`.

**Coverage target: the top ~50% highest-value methods**, judged by how much
logic they carry and how much breaks if they are wrong. In practice that means
parsing, the task list's numbering and range rules, the save-file format, and
what each command does when it runs.

Keep the tests in step with the code: whenever a method in that top half
changes, gains a branch, or is added, update or add its tests in the same
change rather than leaving it for later.

## Git: project-specific

Use lightweight tags unless the user requests an annotated tag.
Do not commit or push unless explicitly asked.
