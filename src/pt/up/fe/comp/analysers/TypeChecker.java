package pt.up.fe.comp.analysers;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.List;

public class TypeChecker {
    private SymbolTable symbolTable;
    private String methodName;
    private List<Symbol> params;
    private List<Symbol> fields;
    private List<Symbol> locals;


    public TypeChecker(SymbolTable symbolTable, String methodName) {
        this.symbolTable = symbolTable;
        this.methodName = methodName;
    }

    public Type searchType(JmmNode search){
        if (search.getOptional("value").isPresent())
            if (search.get("value").equals("this"))
                return new Type(symbolTable.getClassName(), false);

        if (search.getOptional("type").isPresent()){
            if (search.getOptional("isArray").isPresent()){
                if (search.get("isArray").equals("true"))
                    return new Type(search.get("type"), true);
                else
                    return new Type(search.get("type"), false);
            }
            else
                return new Type(search.get("type"), false);
        }
        this.locals = symbolTable.getLocalVariables(methodName);
        this.params = symbolTable.getParameters(methodName);
        this.fields = symbolTable.getFields();

        if (search.getKind().equals("Addition") || search.getKind().equals("Subtraction") ||
                search.getKind().equals("Multiplication") || search.getKind().equals("Division")){
            Type type1 = searchType(search.getJmmChild(0));
            Type type2 = searchType(search.getJmmChild(1));
            Type intType = new Type("int", false);
            Type intTypeArray = new Type("int", true);

            if (type1.equals(intTypeArray)){
                if (type2.equals(intType) || type2.equals(intTypeArray)) return intType;
            }
            else if (type2.equals(intTypeArray)) {
                if (type1.equals(intType) || type1.equals(intTypeArray)) return intType;
            }
            else if (type1.equals(intType) && type2.equals(intType)) return intType;
            else return null;
        }

        if (search.getKind().equals("LogicLessThan") || search.getKind().equals("LogicAnd")){
            Type type1 = searchType(search.getJmmChild(0));
            if (type1 == null) return null;
            Type type2 = searchType(search.getJmmChild(1));
            if (type2 == null) return null;
            Type booleanType = new Type("boolean", false);
            Type booleanTypeArray = new Type("boolean", true);

            if (type1.equals(booleanType) && type2.equals(booleanType)) return booleanType;
            else if (type1.equals(booleanTypeArray) && type2.equals(booleanTypeArray)) return booleanTypeArray;
            else return null;
        }


        if (search.getKind().equals("DotStatement")) {
            if (!this.symbolTable.getMethods().contains(search.getJmmChild(1).get("value"))) return null;
        else
        if (search.getJmmChild(1).getKind().equals("CallExpression")) {
                if (search.getJmmChild(0).getOptional("isArray").isPresent()) {
                    if (search.getJmmChild(0).get("isArray").equals("true"))
                        return new Type(symbolTable.getReturnType(search.getJmmChild(1).get("value")).getName(),
                                true);
                    else
                        return new Type(symbolTable.getReturnType(search.getJmmChild(1).get("value")).getName(),
                                false);
                } else {
                        return new Type(symbolTable.getReturnType(search.getJmmChild(1).get("value")).getName(),
                                false);
                }
            }
        }

        if (search.getJmmChild(0).getKind().equals("NewStatement")) {
            if (search.getJmmChild(0).getOptional("isArray").isPresent()) {
                if (search.getJmmChild(0).get("isArray").equals("true"))
                    return new Type(search.getJmmChild(0).get("type"), true);
                else
                    return new Type(search.getJmmChild(0).get("type"), false);
            }
            else return new Type(search.getJmmChild(0).get("type"), false);
        }

        String searchName;
        if (search.getKind().equals("Array")){
            searchName = search.getJmmChild(0).getJmmChild(0).get("value");
        }
        else searchName = search.getJmmChild(0).get("value");

        for (Symbol l : locals){
            if (l.getName().equals(searchName)) return l.getType();
        }
        for (Symbol l : params){
            if (l.getName().equals(searchName)) return l.getType();
        }
        for (Symbol l : fields){
            if (l.getName().equals(searchName)) return l.getType();
        }
        return null;
    }
}
