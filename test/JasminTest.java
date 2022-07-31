import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;

import java.util.Collections;

public class JasminTest {
    @Test
    public void testWithOllir(){
        var ollirResult = new OllirResult(SpecsIo.getResource("fixtures/public/cpf/4_jasmin/arithmetic/Arithmetic_add.ollir"), Collections.emptyMap());
        var jasminResult = TestUtils.backend(ollirResult);
        System.out.println("RUN RESULT: ");
        jasminResult.run();
    }

}