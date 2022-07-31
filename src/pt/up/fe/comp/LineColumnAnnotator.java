package pt.up.fe.comp;

import com.javacc.parser.BaseNode;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

public class LineColumnAnnotator extends PreorderJmmVisitor<Integer, Integer> {
  public LineColumnAnnotator(){
    setDefaultVisit(this::setLineColumn);
  }

  private Integer setLineColumn(JmmNode node, Integer dummy){
    var baseNode = (BaseNode) node;

    node.put("line", Integer.toString(baseNode.getBeginLine()));
    node.put("col", Integer.toString(baseNode.getBeginColumn()));

    return 0;
  }
}
