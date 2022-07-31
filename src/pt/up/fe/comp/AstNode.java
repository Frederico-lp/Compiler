package pt.up.fe.comp;

import pt.up.fe.specs.util.SpecsStrings;

public enum AstNode {
  START,
  IMPORT_DECLARATION,
  CLASS_DECLARATION,
  METHOD_DECLARATION,
  RETURN_STATEMENT,
  EQUALS,
  TERMINAL,
  STATEMENT,
  IF_CONDITION,
  IF_STATEMENTS,
  ELSE_STATEMENTS,
  VAR_DECLARATION,
  DOT_STATEMENT,
  LOGIC_AND,
  LOGIC_LESS_THAN,
  LOGIC_NOT,
  ADDITION,
  MULTIPLICATION,
  SUBTRACTION,
  DIVISION,
  ARRAY,
  WHILE_CONDITION,
  WHILE_STATEMENTS,
  CALL_EXPRESSION;


  private final String name;

  AstNode(){
    this.name = SpecsStrings.toCamelCase(name(), "_", true);
  }

  @Override
  public String toString(){
    return name;
  }


}
