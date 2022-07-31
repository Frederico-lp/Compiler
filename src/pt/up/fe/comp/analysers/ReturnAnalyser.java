package pt.up.fe.comp.analysers;

import pt.up.fe.comp.AstNode;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

public class ReturnAnalyser extends CompleteSemanticAnalyser{
    private SymbolTable symbolTable;
    private String methodName;
    private TypeChecker typeChecker;

    private JmmNode returnNode;
    private String returnType;

    public ReturnAnalyser(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        addVisit(AstNode.METHOD_DECLARATION, this::methodVisit);
    }

    private String methodVisit(JmmNode method, Integer dummy) {
        this.returnType = method.getJmmChild(0).get("type");
        this.methodName = method.get("value");
        this.typeChecker = new TypeChecker(symbolTable, methodName);
        addVisit(AstNode.RETURN_STATEMENT, this::returnVisitor);

        return "";
    }

    private String returnVisitor(JmmNode ret, Integer dummy) {

        this.returnNode = ret.getJmmChild(0);
        Type typeGiven = typeChecker.searchType(this.returnNode);
        if (typeGiven == null) return "Error on searching type/type invalid";
        if (!typeGiven.getName().equals(returnType))
            addReport(new Report(ReportType.ERROR, Stage.SEMANTIC,
                    -1, -1,
                    "Type mismatching: return given is of type \"" + typeGiven.getName()
                            + "\" while return expected was of type \"" + returnType +
                            "\"."));
        return "";
    }
}
