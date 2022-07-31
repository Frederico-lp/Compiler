package pt.up.fe.comp;

import pt.up.fe.comp.analysers.*;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnalysisStage implements JmmAnalysis {
    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        var symbolTable = new SymbolTableBuilder();

        var symbolTableFiller = new SymbolTableFiller();
        symbolTableFiller.visit(parserResult.getRootNode(), symbolTable);
        List<Report> reports = new ArrayList<>(symbolTableFiller.getReports());

        List<CompleteSemanticAnalyser> analysers = Arrays.asList(
            new OperationAnalyser(symbolTable),
            new ArrayAnalyser(symbolTable),
            new AssignmentAnalyser(symbolTable),
            new CallAnalyser(symbolTable),
            new ReturnAnalyser(symbolTable),
            new TerminalAnalyser(symbolTable),
            new IfAnalyser(symbolTable),
            new WhileAnalyser(symbolTable)

        );

        for(var analyser : analysers){
            analyser.visit(parserResult.getRootNode());
            reports.addAll(analyser.getReports());

            if(analyser.getReports().size() != 0)
                break;
        }


        return new JmmSemanticsResult(parserResult, symbolTable, reports);
    }
}

