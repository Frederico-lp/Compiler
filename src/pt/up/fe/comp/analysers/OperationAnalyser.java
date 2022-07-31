package pt.up.fe.comp.analysers;

import pt.up.fe.comp.AstNode;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.List;


public class OperationAnalyser extends CompleteSemanticAnalyser {
    private final SymbolTable symbolTable;
    private String methodName;
    private JmmNode firstOp, secondOp;
    private TypeChecker typeChecker;
    private Integer spaces;

    public OperationAnalyser(SymbolTable symbolTable){
        this.symbolTable = symbolTable;
        this.spaces = 0;
        addVisit(AstNode.METHOD_DECLARATION, this::methodVisit);
    }

    private String methodVisit(JmmNode method, Integer dummy){
        methodName = method.get("value");
        this.typeChecker = new TypeChecker(symbolTable, methodName);
        addVisit(AstNode.ADDITION, this::operationVisit);
        addVisit(AstNode.MULTIPLICATION, this::operationVisit);
        addVisit(AstNode.SUBTRACTION, this::operationVisit);
        addVisit(AstNode.DIVISION, this::operationVisit);

        return "";
    }

    private String operationVisit(JmmNode add, Integer dummy){
        List<JmmNode> children = add.getChildren();

        this.firstOp = children.get(0);
        this.secondOp = children.get(1);

        Type firstOpType = typeChecker.searchType(this.firstOp);
        Type secondOpType = typeChecker.searchType(this.secondOp);

        if (firstOpType == null) return "Error on searching type/type invalid";
        if (secondOpType == null) return "Error on searching type/type invalid";


        if (!firstOpType.equals(secondOpType))
            addReport(new Report(ReportType.ERROR, Stage.SEMANTIC,
                    -1, -1,
                    "Type mismatching: trying to perform operation between \"" + firstOpType.getName() + "\" and \""
                            + secondOpType.getName() + "\"."));
        return "";
    }

    public List<Report> getReports(){
        return reports;
    }

}
