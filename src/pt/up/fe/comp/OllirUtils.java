package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

public class OllirUtils {
    public static String getCode(Symbol symbol){
        return symbol.getName() + "." + getCode(symbol.getType());
    }


    public static String getCode(Type type){
        StringBuilder code = new StringBuilder();

        if(type.isArray())
            code.append("array.");

        code.append(getOllirType(type.getName()));

        return code.toString();

    }

    public static String getOllirType(String jmmType){
        switch (jmmType) {
            case "void":
                return "V";
            case "int":
                return "i32";
            case "boolean":
                return "bool";
            default:
                return jmmType;
        }
    }

    public static String getTerminalValue(JmmNode terminal){
        if(terminal.getNumChildren() == 0){
            return terminal.get("value");
        }
        else{
            if(terminal.getChildren().get(0).getNumChildren() == 0){
                return terminal.getChildren().get(0).get("value");
            }
            else{
                return terminal.getChildren().get(0).getChildren().get(0).get("value");
            }
        }
    }

    public static String getTerminalType(JmmNode terminal) {
        var parent = terminal.getJmmParent();

        if(terminal.getKind().equals("Terminal") && terminal.getNumChildren() == 0){
            return getOllirType(terminal.get("type"));
        }

        while (!parent.getKind().equals("Start")) {
            for (var sibling : parent.getChildren()) {
                if (sibling.getKind().equals("VarDeclaration") || sibling.getKind().equals("Parameter")) {
                    if (sibling.get("value").equals(getTerminalValue(terminal))) {
                        /*
                        if(sibling.getOptional("isArray").isPresent()){
                            return "array." + getOllirType(sibling.getChildren().get(0).get("type"));
                        }
                        else

                         */
                            return getOllirType(sibling.getChildren().get(0).get("type"));
                    }
                }
            }
            parent = parent.getJmmParent();
        }
        return null;
    }

    /*
    public static String operationHelper(JmmNode expression, String operation){
        String name1 = null, name2 = null, type1 = null, type2 = null;

        String code = "";
        if(operation.equals("Addition")){
            var child1 = expression.getChildren().get(0);
            var child2 = expression.getChildren().get(1);
            name1 = OllirUtils.getTerminalValue(child1);
            type1 = OllirUtils.getTerminalType(child1);
            name2 = OllirUtils.getTerminalValue(child2);
            type2 = OllirUtils.getTerminalType(child2);
            code.append(lastVariable + "." + type1 + " :=." + type1 + " ");
            newVariable += "z";
            ollirCode.append(name1 + "." + type1 + " +." + type1 + " " + name2 + "." + type2 + ";\n" );
        }
    }

     */

}
