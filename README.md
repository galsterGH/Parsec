# Parsec

Parsec is a tiny library demonstrating how <em>parser combinators</em> can be
implemented in Java using a monadic design.  A parser combinator is a function
that consumes an input string and produces a result along with the remaining
unparsed input.  By composing small parsers together we can describe complex
grammars in a highly declarative style without writing a traditional lexer or
parser.

## Core Concepts

### `Parsec<T>`

The `Parsec` class wraps a function from `String` to `ParserResult<T>` where
`T` is the type produced on a successful parse.  `ParserResult` contains the
parsed value (wrapped in `Optional`) and the rest of the input.  Parsers are
chained together using the `bind` method which embodies the monadic
composition law: if the first parser succeeds its value is passed to a function
that produces the next parser; if it fails the failure is propagated.

### Primitive Parsers and Combinators

`ParsecUtils` provides a collection of building blocks:

* **`item`** – consumes a single character.
* **`satisfy`** – consumes a character only if it satisfies a predicate.
* **`charParser`, `digit`, `letter`, ...** – specialized variants of
  `satisfy` for common character classes.
* **`many` / `some`** – iterate a parser zero or more or one or more times.
* **`option`** – try one parser and fall back to another.
* **`quotedString`, `token`, `space`** – utility parsers for common patterns.

Each of these returns a new `Parsec` allowing them to be freely combined.

## Example: Parsing a Student

`Main` contains a worked example that parses a limited JSON representation of a
student record:

```json
{"name":guy,"id":12345,"grades":[100,90,80]}
```

The parsing process is broken down into small parsers for each field
(`parseStudentName`, `parseStudentId`, `parseStudentGrades`).  These are then
chained together by `studentFromJson` to produce a `Student` object.

To experiment, run the program:

```bash
javac *.java
java Main
```

## Testing

The repository includes a simple test harness (`ParsecTest`) which exercises the
JSON parser and stress‑tests the grade list parser.  The tests can be executed
with:

```bash
javac *.java
java ParsecTest
```

## Further Reading

Parser combinators originate in the functional programming world and are well
known from Haskell's [Parsec](https://hackage.haskell.org/package/parsec)
library.  This project mirrors the same ideas in a small amount of Java code so
that the underlying theory is easy to see.
