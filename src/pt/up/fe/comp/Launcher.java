package pt.up.fe.comp;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;

public class Launcher {

    public static void main(String[] args) {
        SpecsSystem.programStandardInit();

        SpecsLogs.info("Executing with args: " + Arrays.toString(args));

        // read the input code
        if (args.length != 1) {
            throw new RuntimeException("Expected a single argument, a path to an existing input file.");
        }
        File inputFile = new File(args[0]);
        if (!inputFile.isFile()) {
            throw new RuntimeException("Expected a path to an existing input file, got '" + args[0] + "'.");
        }
        String input = SpecsIo.read(inputFile);

        // Create config
        Map<String, String> config = new HashMap<>();
        config.put("inputFile", args[0]);
        config.put("optimize", "false");
        config.put("registerAllocation", "-1");
        config.put("debug", "false");

        // Instantiate JmmParser
        SimpleParser parser = new SimpleParser();

        // Parse stage
        JmmParserResult parserResult = parser.parse(input, config);

        // Check if there are parsing errors
        if(parserResult.getReports().size() != 1)
           System.out.println(parserResult.getReports());
        else TestUtils.noErrors(parserResult.getReports());

        // Instantiate JmmAnalysis
        AnalysisStage analyser = new AnalysisStage();

        // Analysis stage
        JmmSemanticsResult analysisResult = analyser.semanticAnalysis(parserResult);

        // Check if there are parsing errors
        TestUtils.noErrors(analysisResult.getReports());

        // Instantiate JmmOptimization
        var optimizer = new OptimizationStage();

        // Optimization stage
        var optimizationResult = optimizer.optimize(analysisResult);

        var optimizationResult2 = optimizer.toOllir(optimizationResult);

        var ollirResult = optimizer.optimize(optimizationResult2);

        // Check if there are errors
        TestUtils.noErrors(optimizationResult.getReports());

        var jasminEmmiter = new BackendStage();

        var jasminResult = jasminEmmiter.toJasmin(ollirResult);

        TestUtils.noErrors(jasminResult);

        // ... add remaining stages
    }

}
