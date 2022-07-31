package pt.up.fe.comp.analysers;

import pt.up.fe.comp.AstNode;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.List;

public class ArrayAnalyser extends CompleteSemanticAnalyser{
    private final SymbolTable symbolTable;
    private String methodName;
    private TypeChecker typeChecker;
    private JmmNode arrayTerminal;
    private JmmNode arrayIndex;

    public ArrayAnalyser(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        addVisit(AstNode.METHOD_DECLARATION, this::methodVisit);
    }

    private String methodVisit(JmmNode method, Integer dummy) {
        methodName = method.get("value");
        this.typeChecker = new TypeChecker(symbolTable, methodName);
        addVisit(AstNode.ARRAY, this::arrayVisitor);
        return "";
    }

    private String arrayVisitor(JmmNode array, Integer dummy) {

        List<JmmNode> children = array.getChildren();

        this.arrayTerminal = children.get(0);
        this.arrayIndex = children.get(1);

        Type arrayType = typeChecker.searchType(this.arrayTerminal);
        Type callType = typeChecker.searchType(this.arrayIndex);

        if (arrayType == null) return "Error on searching type/type invalid";
        if (callType == null) return "Error on searching type/type invalid";

        if (!arrayType.isArray())
            addReport(new Report(ReportType.ERROR, Stage.SEMANTIC,
                -1, -1,
                "Type mismatching: trying to get an array from type \"" + arrayType.getName() + "\"."));

        if(!callType.equals(new Type("int", false)))
            addReport(new Report(ReportType.ERROR, Stage.SEMANTIC,
                    -1, -1,
                    "Type mismatching: trying to index an array with type \"" + callType.getName() + "\"."));
        return "";
    }

}
