package pt.up.fe.comp.analysers;

import pt.up.fe.comp.AstNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.List;

public class CallAnalyser extends CompleteSemanticAnalyser{
    private SymbolTable symbolTable;
    private String methodName;
    private TypeChecker typeChecker;

    private JmmNode firstOp;
    private JmmNode secondOp;

    public CallAnalyser(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        addVisit(AstNode.METHOD_DECLARATION, this::methodVisit);
    }

    private String methodVisit(JmmNode method, Integer dummy) {
        methodName = method.get("value");
        this.typeChecker = new TypeChecker(symbolTable, methodName);
        addVisit(AstNode.DOT_STATEMENT, this::dotVisitor);
        return "";
    }

    private String dotVisitor(JmmNode dot, Integer dummy) {
        List<JmmNode> children = dot.getChildren();

        this.firstOp = children.get(0);
        this.secondOp = children.get(1);
        Type firstOpType = typeChecker.searchType(this.firstOp);

        if (firstOpType == null){
            if (this.firstOp.getNumChildren() > 1){
                if (!this.symbolTable.getMethods().contains(this.firstOp.getJmmChild(1).get("value"))) {
                    return "";
                }
            }

            else if (!symbolTable.getImports().contains(this.firstOp.getJmmChild(0).get("value")))
                addReport(new Report(ReportType.ERROR, Stage.SEMANTIC,
                        -1, -1,
                        "Call from class \"" + this.firstOp.getJmmChild(0).get("value") + "\" not imported."));

                return "";
        }

        if (this.firstOp.getOptional("value").isPresent())
            if (this.firstOp.get("value").equals("this") && this.methodName.equals("main")) {
                addReport(new Report(ReportType.ERROR, Stage.SEMANTIC,
                        -1, -1,
                        "Call of non-static \"" + this.secondOp.get("value") + "\" variable in a static context."));

                return "";
            }


        if (this.secondOp.getKind().equals("CallExpression")){
            if (this.symbolTable.getClassName().equals(firstOpType.getName())){
                String superClassName = symbolTable.getSuper();
                if (!this.symbolTable.getMethods().contains(this.secondOp.get("value"))){
                    if (!this.symbolTable.getImports().contains(firstOpType.getName()) &&
                            superClassName == null)
                        addReport(new Report(ReportType.ERROR, Stage.SEMANTIC,
                                -1, -1,
                                "Call for method \"" + this.secondOp.get("value") + "\" not declared in scope."));
                }
                else{
                    List<Symbol> params = this.symbolTable.getParameters(this.secondOp.get("value"));
                    if (secondOp.getNumChildren() != params.size())
                        addReport(new Report(ReportType.ERROR, Stage.SEMANTIC,
                                -1, -1,
                                "Call for method \"" + this.secondOp.get("value") + "\" with wrong number of arguments."));
                    else{
                        for (int i = 0; i < params.size(); i++){
                            Type childType = typeChecker.searchType(secondOp.getJmmChild(i));
                            if (childType == null) return "Error on searching type/type invalid";
                            if (!childType.equals(params.get(i).getType()))
                                addReport(new Report(ReportType.ERROR, Stage.SEMANTIC,
                                        -1, -1,
                                        "Call for method \"" + this.secondOp.get("value") + "\" with " +
                                                " parameter of wrong type: called with \"" + childType.getName() +
                                                "\" when should be \"" + params.get(i).getType().getName() + "\" ."));
                        }
                    }
                }
            }
        }

        return "";
    }
}
