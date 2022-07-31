package pt.up.fe.comp.analysers;

import pt.up.fe.comp.AstNode;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

public class TerminalAnalyser extends CompleteSemanticAnalyser{
  private final SymbolTable symbolTable;
  private String currentMethod;

  public TerminalAnalyser(SymbolTable symbolTable) {
    this.symbolTable = symbolTable;
    this.currentMethod = "";

    addVisit(AstNode.METHOD_DECLARATION, this::methodVisit);
    addVisit(AstNode.TERMINAL, this::terminalVisit);
    setDefaultVisit(this::defaultVisit);
  }

  public String defaultVisit(JmmNode node, Integer dummy){
    return "";
  }

  public String methodVisit(JmmNode methodNode, Integer dummy){
    currentMethod = methodNode.get("value");
    return "";
  }

  private String terminalVisit(JmmNode terminalNode, Integer dummy){
    if(terminalNode.getNumChildren() != 1)
      return "";

    var terminalChild = terminalNode.getJmmChild(0);
    if(terminalChild.getKind().equals("Id")){
      var id = terminalChild.get("value");
      if(!symbolTable.isLocalVariable(currentMethod, id) && !symbolTable.isField(id) &&
          !symbolTable.isParameter(currentMethod, id) && !symbolTable.isImport(id)){

        addReport(new Report(
            ReportType.ERROR,
            Stage.SEMANTIC,
            -1,
            -1,
            "Using undeclared variable \"" + id + "\" ."));
      }
    }

    return "";
  }
}
