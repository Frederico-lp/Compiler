package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.util.Collections;

public class OptimizationStage implements JmmOptimization {
//    Override
//    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
//        return null;
//    }

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {
        var ollirGenerator = new OllirGenerator(semanticsResult.getSymbolTable());
        ollirGenerator.visit(semanticsResult.getRootNode());
        String ollirCode = ollirGenerator.getCode();

        System.out.println("ollir code" + ollirCode);

        return new OllirResult(semanticsResult, ollirCode, Collections.emptyList());
    }

//    @Override
//    public OllirResult optimize(OllirResult ollirResult) {
//        return null;
//    }
}
