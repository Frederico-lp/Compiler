package pt.up.fe.comp.analysers;

import pt.up.fe.comp.AstNode;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.List;

public class WhileAnalyser extends CompleteSemanticAnalyser{
    private final SymbolTable symbolTable;
    private String methodName;
    private TypeChecker typeChecker;
    private JmmNode condition;


    public WhileAnalyser(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        addVisit(AstNode.METHOD_DECLARATION, this::methodVisit);
    }

    private String methodVisit(JmmNode method, Integer dummy) {
        methodName = method.get("value");
        this.typeChecker = new TypeChecker(symbolTable, methodName);
        addVisit(AstNode.WHILE_CONDITION, this::whileVisitor);
        return "";
    }

    private String whileVisitor(JmmNode while_c, Integer dummy) {
        List<JmmNode> children = while_c.getChildren();
        this.condition = children.get(0);
        Type condType = typeChecker.searchType(this.condition);

        if (condType == null) return "Error on searching type/type invalid";

        if (condType.isArray())
            addReport(new Report(ReportType.ERROR, Stage.SEMANTIC,
                    -1, -1,
                    "Array in while condition."));

        return "";
    }

    }