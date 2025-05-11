package cn.bincker;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class MarkdownParseTest {
    @Test
    void test() throws IOException {
        var options = new MutableDataSet();
        var parser = Parser.builder(options).build();
        var document = parser.parseReader(new InputStreamReader(new FileInputStream("blog/test.md")));
        for (Node node : document.getChildren()) {
            if (node instanceof Heading && ((Heading) node).getLevel() == 1){
                System.out.println(((Heading) node).getText());
            }
        }
    }
}
