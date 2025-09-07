import java.util.Optional;
import java.util.function.Function;

/**
 * Core parser abstraction.  A {@code Parsec<T>} wraps a function that consumes
 * a {@link String} input and produces a {@link ParserResult} describing whether
 * the parse succeeded and the remaining unconsumed input.  Parsers can be
 * combined using the {@link #bind(Function)} method in a monadic style allowing
 * complex grammars to be built from small, reusable components.
 *
 * @param <T> type of the value produced on a successful parse
 */
class Parsec<T> {

    /**
     * Result of running a parser.  The {@code result} field contains the parsed
     * value when the parse succeeds, wrapped in an {@link Optional}.  The
     * {@code parseNext} field holds the remaining portion of the input that was
     * not consumed.
     */
    static class ParserResult<T>{
        private final Optional<T> result;
        private final String parseNext;

        ParserResult(Optional<T> res, String next){
            result = res;
            parseNext=  next;
        }

        /**
         * @return value produced by the parser if present
         */
        Optional<T> getResult() {
            return result;
        }

        /**
         * @return remaining unparsed portion of the original input
         */
        String getParseNext() {
            return parseNext;
        }
    }

    /**
     * parser is a function which takes an input string and yields a
     * {@link ParserResult}.  It is the core of the parser and is hidden behind
     * the {@code Parsec} abstraction so that combinators can manipulate and
     * compose parsers without exposing implementation details.
     */
    private Function<String,ParserResult<T>> parser;

    /**
     * Constructs a new {@code Parsec} from a parsing function.
     *
     * @param p the parser function to wrap in this Parsec
     */
    Parsec(Function<String, ParserResult<T>> p){
        parser = p;
    }

    /**
     * Runs the underlying parser on a given input string.
     *
     * @param str the string to parse
     * @return the parsing result
     */
    ParserResult<T> runParser(String str){
        return parser.apply(str);
    }


    /**
     * Composes this parser with another parser that depends on the value
     * produced by the current one.  If this parser succeeds the resulting value
     * is fed into {@code binder} to obtain the next parser; otherwise the failure
     * is propagated.  This is the essence of the <em>monadic</em> structure and
     * enables building larger parsers from smaller pieces.
     *
     * @param binder the continuation which connects the next parser to this parser
     * @param <U> the type of the next parse
     * @return a new Parsec representing the composed computation
     */
    <U> Parsec<U> bind(Function<T, Parsec<U>> binder) {
        return new Parsec<U>(s -> {
            ParserResult<T> res = runParser(s);
            if (res.getResult().isEmpty()) {
                return new ParserResult<>(Optional.empty(), res.getParseNext());
            }

            return binder.apply(res.getResult().get()).runParser(res.getParseNext());
        });
    }
}


