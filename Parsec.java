import java.util.Optional;
import java.util.function.Function;

class Parsec<T> {

    /**
     * @param <T> the underlying type of the result
     */
    static class ParserResult<T>{
        Optional<T> result;
        String parseNext;
        ParserResult(Optional<T> res, String next){
            result = res;
            parseNext=  next;
        }
    }

    /**
     * parser is a function which takes a String and returns
     * a ParserResult
     */
    private Function<String,ParserResult<T>> parser;

    /**
     * @param p the parser function to wrap in this Parsec
     */
    Parsec(Function<String, ParserResult<T>> p){
        parser = p;
    }

    /**
     * @param str the string to parse
     * @return the parsing result
     */
    ParserResult<T> runParser(String str){
        return parser.apply(str);
    }


    /**
     * @apiNote bind is the magic that allows combining parsers with one another
     * it takes a continuation function and controls how this function gets called
     * based on the result of the result of the current Parser
     * @param binder the continuation which connects the next parser to this parser
     * @param <U> the type of the next parse
     * @return a new Parsec
     */
    <U> Parsec<U> bind(Function<T, Parsec<U>> binder) {
        return new Parsec<U>(s -> {
            ParserResult<T> res = runParser(s);
            if (res.result.isEmpty()) {
                return new ParserResult(Optional.empty(), res.parseNext);
            }

            return binder.apply((res.result.get())).runParser(res.parseNext);
        });
    }
}


