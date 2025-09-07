import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Collection of helper combinators for building {@link Parsec} instances.  The
 * functions provided here mirror those found in functional programming parser
 * libraries and can be composed to describe complex grammars in a declarative
 * style.
 */
public class ParsecUtils {

    /**
     * @param <T>
     * @return a parser that represents a failed parsing
     */
    static <T> Parsec<T> failure(){
        return new Parsec<>(s -> new Parsec.ParserResult<>(Optional.empty(),s));
    }

    /**
     * @apiNote This method takes a generic value and boxes it inside a parser
     * @param val the value to be embellished
     * @param <T>
     * @return a new Parser that returns val as a result
     */
    static <T> Parsec<T> unit(T val){
        return new Parsec<T>(s->
            new Parsec.ParserResult<T>(Optional.of(val),s));
    }

    /**
     * @apiNote This method allows us to default to a second parser in case
     * the first parser fails
     * @param p1 the first parser
     * @param p2 the defaulted parser
     * @param <T>
     * @return A parser which encapsulates the logic of tyring the first and defaulting to the second
     */
    static <T> Parsec<T> option(Parsec<T> p1, Parsec<T> p2){
        return new Parsec<>(s -> {
            var result = p1.runParser(s);
            if(result.getResult().isEmpty()){
                return p2.runParser(s);
            }

            return result;
        });
    }

    /**
     * @apiNote This method returns a parser that simply reads the first character from the string
     * @return a Parser
     */
    static Parsec<Character> item(){
        return new Parsec<>(s -> {
            if(s.isEmpty()){
                return new Parsec.ParserResult<Character>(Optional.empty(),s);
            }

            return new Parsec.ParserResult<Character>(Optional.of(s.charAt(0)), s.substring(1));
        });
    }

    /**
     * @apiNote this method returns a Parser that runs a predicate function against the first character
     * @param predicate the predicate to run against the current char
     * @return a Parser that runs the predicate
     */
    static Parsec<Character> satisfy(Function<Character, Boolean> predicate){
       return item().bind(chr -> {
           if(predicate.apply(chr)){
               return unit(chr);
           }
           return failure();
       });
    }


    /**
     * @param c the character to match against
     * @return a Parser that can match the first character in our string to the given character
     */
    static Parsec<Character> charParser(char c){
        return satisfy(c1 -> c1 == c);
    }

    /**
     * @return a Parser that can match a digit
     */
    static Parsec<Character> digit(){
        return satisfy(Character::isDigit);
    }

    /**
     * @return a Parser that can match a lower case letter
     */
    static Parsec<Character> lower(){
        return satisfy(Character::isLowerCase);
    }

    /**
     * @return a Parser that can match an upper case letter
     */
    static Parsec<Character> upper(){
        return satisfy(Character::isUpperCase);
    }

    /**
     * @return a Parser that can match a letter
     */
    static Parsec<Character> letter(){
        return satisfy(Character::isLetter);
    }

    /**
     * @return a Parser that can match alpha-numberic chars
     */
    static Parsec<Character> alphaNum(){
        return satisfy(c-> (Character.isAlphabetic(c) || Character.isDigit(c)));
    }

    /**
     * @return a Parser that can match a quoted string
     */
    static Parsec<String> quotedString(){
        // first read an opening quote, then a sequence of alphanumeric
        // characters, finally a closing quote and return the gathered string
        return charParser('"').bind(c -> some(alphaNum()).bind(lchar -> charParser('"').bind(c2->
                new Parsec<>(s -> {
                    StringBuilder builder = new StringBuilder();
                    lchar.forEach(ch -> builder.append(ch));
                    return new Parsec.ParserResult<>(Optional.of(builder.toString()),s);
                }))));
    }


    /**
     * @param p a Parser
     * @param <T> the Parser's return type
     * @return A list of T - the result of applying p  0 or more times
     */
    static <T> Parsec<List<T>> many(Parsec<T> p) {
        return option(some(p), unit(new ArrayList<>()));
    }

    /**
     * @param p a Parser
     * @param <T> the Parser's return type
     * @return A list of T - the result of applying p successfully at least one time
     */
    static <T> Parsec<List<T>> some(Parsec<T> p){
        // parse the first occurrence then parse zero or more additional
        // occurrences and prepend the first result
        return p.bind(t -> many(p).bind(trs-> {
            List<T> fres = new ArrayList<>();
            fres.add(t);
            fres.addAll(trs);
            return unit(fres);
        }));
    }

    /**
     * @return a parser that can skip multiple spaces
     */
    static Parsec<Optional<Void> >space(){
        return many(satisfy(c -> Character.isSpaceChar(c))).bind(clst ->
                unit(Optional.empty()));
    }

    /**
     * Consumes leading and trailing whitespace around the given parser.  This is
     * useful for token based grammars where insignificant spaces should be
     * ignored.
     */
    static <T> Parsec<T> token(Parsec<T> p){
        return space().bind(v-> p.bind(
                                t -> space().bind(v2 ->
                                        unit(t))));
    }
}
