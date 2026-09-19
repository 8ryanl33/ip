---
name: seedu-java-coding-standard
description: The SE-EDU Java coding standard (intermediate level) that all Java code in this project must follow. Use when writing, reviewing or editing any .java file here - naming, layout, imports, statements, comments and Javadoc.
---

# SE-EDU Java coding standard (intermediate)

Source: https://se-education.org/guides/conventions/java/intermediate.html

Apply these to every `.java` file in this project, main and test alike.

## Naming

| Thing | Rule | Example |
| --- | --- | --- |
| Package | all lower case | `goat.task` |
| Class / enum | noun, PascalCase | `TaskList`, `CommandType` |
| Method | verb, camelCase | `getName()`, `computeTotalWidth()` |
| Variable | camelCase | `taskNumber` |
| Constant | UPPER_CASE with underscores | `MAX_ITERATIONS`, `BY_MARKER` |
| Test method | `featureUnderTest_testScenario_expectedBehavior()` | `delete_outOfRange_exceptionThrown()` |

- Write all names in English.
- Do not uppercase abbreviations inside a name: `exportHtmlSource()`, not `exportHTMLSource()`.
- Give a variable a name as long as its scope is wide. `i`, `j`, `k`, `m`, `n`
  are fine for loop counters; `j` and `k` only for nested loops.
- Prefix booleans with `is`, `has`, `was`, `can`: `isDone`, `hasLicense()`,
  `canEvaluate()`, `void setFound(boolean isFound)`.
- Name a collection in the plural: `Collection<Point> points`, `int[] values`.
- Give related constants a common prefix: `COLOR_RED`, `COLOR_GREEN`.

## Layout

- Indent 4 spaces. Never tabs.
- Line length: aim under 110 characters, never over 120.
- Indent a wrapped line by 8 spaces (twice the normal indent) relative to
  its parent line.
- Break **after** a comma and **before** an operator (including `.`).
  Keep a method name attached to its opening parenthesis. Prefer breaking at
  the highest syntactic level available.
- Use K&R ("Egyptian") braces: the opening brace ends the line that opens
  the block, never starts a line of its own.
- Wrap every loop and conditional body in braces, even a single statement.
- Put the conditional on its own line.
- Separate logical units within a block with one blank line.

### Whitespace

```java
a = (b + c) * d;          // operators surrounded by spaces
while (true) {            // reserved word followed by a space
doSomething(a, b, c, d);  // comma followed by a space
for (i = 0; i < 10; i++)  // semicolon followed by a space
```

## Statements

- Put every class in a package.
- List every import explicitly. No wildcard imports, and no fully qualified
  names written inline where an import would do.
- **Import order**, each group separated from the next by a blank line:
  1. `import static ...`
  2. `java.*`
  3. `javax.*`
  4. `org.*`
  5. `com.*`
  6. this project's own imports (`goat.*`)
- Attach an array specifier to the type: `int[] a`, never `int a[]`.
- Initialize a variable where it is declared, and declare it in the smallest
  scope that works.
- Never declare a class variable `public` unless the class is a data class
  with no behavior. Constants are exempt.

## Comments and Javadoc

- Write comments in English, using **American spelling** (`behavior`, not
  `behaviour`; `recognized`, not `recognised`).
- Write a header comment for every public class and method. Three exceptions:
  getters and setters, overridden methods whose parent comment applies
  exactly, and test classes and methods.
- Javadoc format:
  - `/**` alone on its own line, following `*`s aligned under it, a space
    after each `*`.
  - First sentence is a short summary. For a method it starts with a verb in
    the third person: "Returns...", "Adds...", "Sends..." - never "Return" or
    "Returning".
  - Blank line between the description and the `@` block.
  - End each parameter description with punctuation.
  - No blank line between the comment and what it documents.
  - `@param` for every parameter or for none.
  - `@return` may be omitted when the method returns nothing, or when the
    return is obvious from the summary.
  - Use `{@inheritDoc}` when an overridden method needs the parent comment
    plus something of its own.
- A one-line member comment is fine: `/** Description */`
