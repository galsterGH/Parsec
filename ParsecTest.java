import java.util.List;

public class ParsecTest {
    public static void main(String[] args) {
        testStudentFromJson();
        testGradeParserLongList();
        System.out.println("All tests passed");
    }

    static void testStudentFromJson() {
        String json = "{\"name\":guy,\"id\":12345,\"grades\":[100,90,80]}";
        var res = Main.studentFromJson().runParser(json);
        assert res.getResult().isPresent();
        Main.Student s = res.getResult().get();
        assert s.toString().equals("name: guy id: 12345 grades: [100,90,80]");
    }

    static void testGradeParserLongList() {
        StringBuilder gradesBuilder = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            if (i > 0) gradesBuilder.append(",");
            gradesBuilder.append("1");
        }
        gradesBuilder.append("]");
        Parsec<List<Integer>> parser = Main.gradeParser(
                ParsecUtils.some(ParsecUtils.digit()).bind(digits -> {
                    int num = 0;
                    for (char d : digits) {
                        num = num * 10 + (d - '0');
                    }
                    return ParsecUtils.unit(num);
                }),
                ParsecUtils.charParser(','),
                ParsecUtils.charParser(']'));
        var res = parser.runParser(gradesBuilder.toString());
        assert res.getResult().isPresent();
        assert res.getResult().get().size() == 1000;
    }
}
