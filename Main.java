import java.util.ArrayList;
import java.util.List;

public class Main {

    public static class Student{
        private String name;
        private int id;
        private List<Integer> grades;

        Student(String name, int id, List<Integer> grades){
            this.name = name;
            this.id = id;
            this.grades = grades;
        }

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

    public static Parsec<String> parseStudentName(){
        return ParsecUtils.quotedString().bind(name -> {
            if (!name.equals("name")){
                return ParsecUtils.failure();
            }

            return ParsecUtils.charParser(':').bind(c1 ->
                    ParsecUtils.some(ParsecUtils.letter()).bind(actualName -> {
                        StringBuilder nameBuilder = new StringBuilder();
                        actualName.stream().forEach(nameBuilder::append);
                        return (Parsec<String>)(ParsecUtils.unit(nameBuilder.toString()));
                    }));
        });
    }

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
                        return (Parsec<Integer>)(ParsecUtils.unit(actualId));
                    }));
        });
    }

    public static Parsec<List<Integer> > gradeParser(Parsec<Integer> num, Parsec<Character> del, Parsec<Character> done){
        return num.bind(n1 -> ParsecUtils.option(del,done).bind(c -> {
                if(c.equals(']')) {
                    List<Integer> res = new ArrayList<>();
                    res.add(n1);
                    return ParsecUtils.unit(res);
                }

                return gradeParser(num,del,done).bind(rest -> {
                   List<Integer> res = new ArrayList<>();
                   res.add(n1);
                   res.addAll(rest);
                   return ParsecUtils.unit(res);
                });
        }));
    }


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

    public static void main(String[] args) {
       var result = studentFromJson().runParser("{\"name\":guy,\"id\":12345,\"grades\":[100,90,80,100,96,10]}");
       if(result.result.isPresent()){
           System.out.println(result.result.get());
           return;
       }

       System.out.println("Failed Parsing");
    }
}
