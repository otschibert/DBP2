import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Locale;

public class MyVeryFirstTest {

    @Test
    public void toUpperCaseConvertsAllLettersToUpperCase(){
        //given - was ist gegeben?
        String teststring = "kleinGROSS";

        //when - was wird überprüft?
        String result = teststring.toUpperCase();

        //then - hat es funktioniert?
        Assertions.assertEquals("KLEIN" +
                "GROSS", result);
    }
}
