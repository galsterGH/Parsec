import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Main {

    /**
     * Simple domain object used in the example parser.  It mirrors the fields in
     * the JSON input and provides a {@link #toString()} implementation so tests
     * can easily verify parsing behaviour.
     */
    public static class Student{
        private String name;
        private int id;
        private List<Integer> grades;

        Student(String name, int id, List<Integer> grades){
            this.name = name;
            this.id = id;
            this.grades = grades;
        }

        @Override
        public String toString(){
            StringBuilder temp = new StringBuilder();
            temp.append("name: " + name + " id: " + Integer.toString(id)+ " grades: [");

            for(Integer g : grades){
                temp.append(g.toString() + ",");
            }

            temp.delete(temp.length() - 1,temp.length());
            temp.append("]");
            return temp.toString();
        }
    }

    /**
     * Parser for the field "name" in the JSON input.  It ensures that the
     * current token is the string "name" followed by a colon and then consumes a
     * sequence of letters representing the student's actual name.
     */
    public static Parsec<String> parseStudentName(){
        return ParsecUtils.quotedString().bind(name -> {
            if (!name.equals("name")){
                return ParsecUtils.failure();
            }

            return ParsecUtils.charParser(':').bind(c1 ->
                    ParsecUtils.some(ParsecUtils.letter()).bind(actualName -> {
                        StringBuilder nameBuilder = new StringBuilder(actualName.size());
                        for(char ch : actualName){
                            nameBuilder.append(ch);
                        }
                        return ParsecUtils.unit(nameBuilder.toString());
                    }));
        });
    }

    /**
     * Parser for the field "id" which reads a quoted key, a colon and a number
     * representing the student's identifier.
     */
    public static Parsec<Integer> parseStudentId(){
        return ParsecUtils.quotedString().bind(id -> {
            if (!id.equals("id")){
                return ParsecUtils.failure();
            }

            return ParsecUtils.charParser(':').bind(c1 ->
                    ParsecUtils.some(ParsecUtils.digit()).bind(actualIdLst -> {
                        Integer actualId = 0;
                        for(char i : actualIdLst){
                            actualId = actualId*10 + (i - '0');
                        }
                        return ParsecUtils.unit(actualId);
                    }));
        });
    }

    /**
     * Generic parser for a comma separated list of numbers enclosed by a
     * terminating character.  The parser supplied by {@code num} parses each
     * individual number while {@code del} consumes the delimiter between
     * numbers and {@code done} marks the end of the list.  This parser is used
     * by {@link #parseStudentGrades()} but can be reused for other lists.
     */
    public static Parsec<List<Integer> > gradeParser(Parsec<Integer> num, Parsec<Character> del, Parsec<Character> done){
        return new Parsec<>(s -> {
            List<Integer> res = new ArrayList<>();
            String rest = s;

            // parse the first number outside the loop so we can require at least one value
            var firstNum = num.runParser(rest);
            if(firstNum.getResult().isEmpty()){
                return new Parsec.ParserResult<>(Optional.empty(), s);
            }
            res.add(firstNum.getResult().get());
            rest = firstNum.getParseNext();

            // continue reading numbers until the terminating token is reached
            while(true){
                var sep = ParsecUtils.option(del, done).runParser(rest);
                if(sep.getResult().isEmpty()){
                    return new Parsec.ParserResult<>(Optional.empty(), rest);
                }
                char c = sep.getResult().get();
                rest = sep.getParseNext();
                if(c == ']'){
                    break;
                }

                var nextNum = num.runParser(rest);
                if(nextNum.getResult().isEmpty()){
                    return new Parsec.ParserResult<>(Optional.empty(), rest);
                }
                res.add(nextNum.getResult().get());
                rest = nextNum.getParseNext();
            }

            return new Parsec.ParserResult<>(Optional.of(res), rest);
        });
    }


    /**
     * Parser for the "grades" field which leverages {@link #gradeParser} to
     * read a list of integers enclosed in square brackets.
     */
    public static Parsec<List<Integer>> parseStudentGrades() {
        return ParsecUtils.quotedString().bind(grades -> {
            if (!grades.equals("grades")) {
                return ParsecUtils.failure();
            }

            return ParsecUtils.charParser(':').bind(c1 ->
                    ParsecUtils.charParser('[').bind(c2 ->
                            gradeParser(ParsecUtils.some(ParsecUtils.digit()).bind(digits -> {
                                int num = 0;
                                for (char d : digits) {
                                    num = num * 10 + (d - '0');
                                }

                                return ParsecUtils.unit(num);
                            }), ParsecUtils.charParser(','), ParsecUtils.charParser(']'))));
        });
    }

    /**
     * High level parser for a {@link Student} instance.  It recognises a small
     * subset of JSON containing the keys {@code name}, {@code id} and
     * {@code grades}.  Whitespace is skipped and each field parser is chained
     * together using {@code bind} to enforce ordering.
     */
    public static Parsec<Student> studentFromJson(){
        return ParsecUtils.space().bind( v1 ->
        ParsecUtils.charParser('{').bind(c->
                parseStudentName().bind(
                        name -> ParsecUtils.charParser(',').bind(c2 -> parseStudentId().bind(
                                id -> ParsecUtils.charParser(',').bind(c3 ->
                                        parseStudentGrades().bind(grades -> ParsecUtils.charParser('}').bind(c4 ->{
                                                Student stud = new Student(name,id,grades);
                                                return ParsecUtils.unit(stud);
                        }))))))));
    }

    /**
     * Entry point used for manual experimentation.  Running the program will
     * attempt to parse a fixed JSON string and print either the resulting
     * {@link Student} or a failure message.
     */
    public static void main(String[] args) {
       var result = studentFromJson().runParser("{\"name\":guy,\"id\":12345,\"grades\":[100,90,80,100,96,10]}");
       if(result.getResult().isPresent()){
           System.out.println(result.getResult().get());
           return;
       }

       System.out.println("Failed Parsing");
    }
}
