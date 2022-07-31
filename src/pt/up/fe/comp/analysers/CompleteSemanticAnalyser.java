package pt.up.fe.comp.analysers;

import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.List;

public abstract class CompleteSemanticAnalyser extends PreorderJmmVisitor<Integer, String> implements SemanticAnalyser{
  protected final List<Report> reports = new ArrayList<>();

  @Override
  public List<Report> getReports() {
    return reports;
  }

  protected void addReport(Report report) {
    reports.add(report);
  }

}
