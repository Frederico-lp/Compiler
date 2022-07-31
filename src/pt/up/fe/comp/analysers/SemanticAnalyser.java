package pt.up.fe.comp.analysers;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;

import java.util.List;

public interface SemanticAnalyser{
  List<Report> getReports();
}
