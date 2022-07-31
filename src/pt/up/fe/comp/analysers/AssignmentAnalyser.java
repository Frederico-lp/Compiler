package pt.up.fe.comp.analysers;

import pt.up.fe.comp.AstNode;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.List;

public class AssignmentAnalyser extends CompleteSemanticAnalyser{
    private SymbolTable symbolTable;
    private TypeChecker typeChecker;

    private String methodName;
    private JmmNode firstOp;
    private JmmNode secondOp;

    public AssignmentAnalyser(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        addVisit(AstNode.METHOD_DECLARATION, this::methodVisit);
    }

    private String methodVisit(JmmNode method, Integer dummy) {
        methodName = method.get("value");
        this.typeChecker = new TypeChecker(symbolTable, methodName);
        addVisit(AstNode.EQUALS, this::assignmentVisitor);
        return "";
    }

    private String assignmentVisitor(JmmNode assign, Integer dummy) {
        List<JmmNode> children = assign.getChildren();

        this.firstOp = children.get(0);
        this.secondOp = children.get(1);

        Type firstOpType = typeChecker.searchType(this.firstOp);
        Type secondOpType = typeChecker.searchType(this.secondOp);

        if (firstOpType == null) return "Error on searching type/type invalid";
        if (secondOpType == null) return "Error on searching type/type invalid";

        if (symbolTable.getImports().contains(firstOpType.getName()) &&
                symbolTable.getImports().contains(secondOpType.getName())) return "";

        String superClassName = symbolTable.getSuper();

        //If the assignment is between operators of different types and
        // the second type doesn't extend the first one
        if (!firstOpType.equals(secondOpType) && !firstOpType.getName().equals(superClassName))
            addReport(new Report(ReportType.ERROR, Stage.SEMANTIC,
                    -1, -1,
                    "Type mismatching: trying to assign type \"" + firstOpType.getName() + "\" to object of type \""
                            + secondOpType.getName() + "\"."));
        return "";
    }
}
