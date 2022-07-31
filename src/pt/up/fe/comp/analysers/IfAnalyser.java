package pt.up.fe.comp.analysers;

import pt.up.fe.comp.AstNode;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

public class IfAnalyser extends CompleteSemanticAnalyser{
  private SymbolTable symbolTable;
  private TypeChecker checker;

  public IfAnalyser(SymbolTable symbolTable) {
    this.symbolTable = symbolTable;

    addVisit(AstNode.METHOD_DECLARATION, this::methodVisit);
    addVisit(AstNode.IF_CONDITION, this::ifConditionVisit);

  }

  private String methodVisit(JmmNode methodNode, Integer dummy){
    this.checker = new TypeChecker(symbolTable, methodNode.get("value"));
    return "";
  }

  private String ifConditionVisit(JmmNode conditionNode, Integer dummy){
    Type booleanType = new Type("boolean", false);

    if (conditionNode.getJmmChild(0).getKind().equals("LogicLessThan") ||
            conditionNode.getJmmChild(0).getKind().equals("LogicAnd")) {
      Type type1 = checker.searchType(conditionNode.getJmmChild(0).getJmmChild(0));
      Type type2 = checker.searchType(conditionNode.getJmmChild(0).getJmmChild(1));

      if (type1 == null) return "Error on searching type/type invalid";
      if (type2 == null) return "Error on searching type/type invalid";


      if (type1.equals(booleanType) && type2.equals(booleanType)) return "";
      else {
        addReport(new Report(
                  ReportType.ERROR,
                  Stage.SEMANTIC,
                  -1,
                  -1,
                  "If condition: trying to evaluate a non boolean result."
          )
        );
      }
    }
    else{
      JmmNode child1 = conditionNode.getJmmChild(0);
      Type type1 = checker.searchType(child1);
      if (type1 == null) return "Error on searching type/type invalid";

      if(!type1.equals(booleanType)){
        addReport(new Report(
                        ReportType.ERROR,
                        Stage.SEMANTIC,
                        -1,
                        -1,
                        "If condition: trying to evaluate a non boolean result."
                )
        );
      }
    }
    return "";

  }

}
