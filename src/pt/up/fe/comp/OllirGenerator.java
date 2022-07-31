package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OllirGenerator extends AJmmVisitor<Integer, Integer> {
    private final StringBuilder ollirCode;
    private final SymbolTable symbolTable;
    private String lastVariable = "zz";
    private String newVariable = "zz";

    public OllirGenerator(SymbolTable symbolTable){
        this.ollirCode = new StringBuilder();
        this.symbolTable = symbolTable;


        addVisit(AstNode.START, this::programVisit);
        addVisit(AstNode.CLASS_DECLARATION, this::classDeclVisit);
        addVisit(AstNode.METHOD_DECLARATION, this::methodDeclVisit);
        addVisit(AstNode.STATEMENT, this::exprStmtVisit);
        addVisit(AstNode.VAR_DECLARATION, this::fieldVisit);
        addVisit(AstNode.RETURN_STATEMENT, this::returnVisit );
        addVisit(AstNode.EQUALS, this::assignVisit);
        addVisit(AstNode.DOT_STATEMENT, this::functionCallVisit);

        addVisit(AstNode.LOGIC_AND, this::expressionVisit);
        addVisit(AstNode.LOGIC_LESS_THAN, this::expressionVisit);
        addVisit(AstNode.ADDITION, this::expressionVisit);
        addVisit(AstNode.SUBTRACTION, this::expressionVisit);
        addVisit(AstNode.MULTIPLICATION, this::expressionVisit);
        addVisit(AstNode.DIVISION, this::expressionVisit);
        addVisit(AstNode.LOGIC_NOT, this::expressionVisit);

        addVisit(AstNode.IF_CONDITION, this::ifCondVisit);
        addVisit(AstNode.IF_STATEMENTS, this::ifStmtVisit);
        addVisit(AstNode.ELSE_STATEMENTS, this::elseStmtVisit);

        addVisit(AstNode.WHILE_CONDITION, this::whileCondVisit);
        addVisit(AstNode.WHILE_STATEMENTS, this::whileStmtVisit);
    }

    public String getCode(){
        return ollirCode.toString();
    }

    private Integer programVisit(JmmNode program, Integer integer){
        for(var importString: symbolTable.getImports()){
            ollirCode.append("import ").append(importString).append(";\n");
        }

        for(var child : program.getChildren())
            visit(child);

        return 0;
    }

    private Integer classDeclVisit(JmmNode classDecl, Integer integer){
        ollirCode.append("public ").append(symbolTable.getClassName());
        var superClass = symbolTable.getSuper();

        if(superClass != null)
            ollirCode.append(" extends ").append(superClass);

        ollirCode.append(" {\n");

        for(var child: classDecl.getChildren()) {
            visit(child);
        }

        ollirCode.append("}\n");

        return 0;
    }

    private Integer fieldVisit(JmmNode varDecl, Integer integer) {
        var name = varDecl.get("value");
        var type = OllirUtils.getOllirType(varDecl.getChildren().get(0).get("type"));

        if(varDecl.getJmmParent().getKind().equals("ClassDeclaration")){
            if(varDecl.getChildren().get(0).getOptional("isArray").isPresent()){
                ollirCode.append(".field " + name + "." + type+ ".array;\n");

            }
            else ollirCode.append(".field " + name + "." + type+ ";\n");
        }

        return 0;
    }

    private Integer returnVisit(JmmNode returnStmt, Integer integer) {
        var ret = returnStmt.getChildren().get(0);

        var parentMethod = returnStmt.getJmmParent().get("value");
        var type = OllirUtils.getCode(symbolTable.getReturnType(parentMethod));
        //ollirCode.append(OllirUtils.getCode(symbolTable.getReturnType(methodSignature)));
        String name;

        if(ret.getKind().equals("Terminal")){

            if(ret.getNumChildren() != 0){
                //id
                name = ret.getChildren().get(0).get("value");
            }
            else {
                name = ret.get("value");
            }
            ollirCode.append("ret." + type + " " + name + "." + type + ";\n");
        }
        else {
            for(var child: returnStmt.getChildren())
                visit(child);

            ollirCode.append("ret.");
            ollirCode.append(type + " " + lastVariable + "." + type + ";\n");

            // if operation
        }


        return 0;
    }

    private Integer assignVisit(JmmNode assign, Integer integer) {
        String var1, var2;
        String type1 = null, type2 = null;
        var siblings = assign.getJmmParent().getChildren();
        if(assign.getNumChildren() == 2){   //normal amount of children
            var child1 = assign.getChildren().get(0);
            var child2 = assign.getChildren().get(1);


            if(child1.getNumChildren() == 0){
                var1 = child1.get("value");
            }
            else{
                if(child1.getChildren().get(0).getOptional("type").isPresent())
                    var1 = child1.getChildren().get(0).get("type");
                else var1 = child1.getChildren().get(0).get("value");
            }
            if(child2.getNumChildren() == 0){
                var2 = child2.get("value");
            }
            else{
                if(child1.getChildren().get(0).getOptional("type").isPresent())
                    var2 = child1.getChildren().get(0).get("type");
                else var2 = child1.getChildren().get(0).get("value");
            }

            System.out.println(siblings);
            for(var sibling: siblings){
                if(sibling.getKind().equals("VarDeclaration")){
                    if(sibling.get("value").equals(var1)){
                        type1 = sibling.getChildren().get(0).get("type");
                    }
                    else if(sibling.get("value").equals(var2)){
                        type2 = sibling.getChildren().get(0).get("type");
                    }
                }
            }
            if(type1 == null)
                type1 = type2;
            if(type2 == null)
                type2 = type1;


            for(var child: assign.getChildren())
                visit(child);

            if(!(type1 == null))
                if(!(type2 == null))
                    ollirCode.append(var1 + "." + OllirUtils.getOllirType(type1) + " :=." + OllirUtils.getOllirType(type1) + " " + var2 + "." + OllirUtils.getOllirType(type2) + ";\n");

        }
        else{
            System.out.println(assign.getNumChildren() + "num childs\n\n");
        }

        return 0;
    }



    private Integer methodDeclVisit(JmmNode methodDecl, Integer integer){
        var methodSignature = methodDecl.get("value");

        ollirCode.append(".method public ");
        if(methodSignature.equals("main"))
            ollirCode.append("static ");

        ollirCode.append(methodSignature + "(");

        var params = symbolTable.getParameters(methodSignature);

        var paramCode = params.stream()
                .map(symbol -> OllirUtils.getCode(symbol))
                .collect(Collectors.joining(", "));

        ollirCode.append(paramCode);

        ollirCode.append(").");

        ollirCode.append(OllirUtils.getCode(symbolTable.getReturnType(methodSignature)));

        ollirCode.append(" {\n");

        int lastParamIndex = -1;

        for(int i = 0; i < methodDecl.getNumChildren(); i++){
            //ver isto em baixo
            if(methodDecl.getJmmChild(i).getKind().equals("Parameter")){
                lastParamIndex = i;
            }
        }

        var statements = methodDecl.getChildren().subList(lastParamIndex + 1, methodDecl.getNumChildren());

        for(var statement: statements){
            visit(statement);
        }



        ollirCode.append("}\n");
        return 0;


    }

    //em baixo acho q vai ser diferente
    private Integer exprStmtVisit(JmmNode exprStmt, Integer integer){
        visit(exprStmt.getJmmChild(0));
        ollirCode.append(";\n");
        return 0;

    }

    private Integer functionCallVisit(JmmNode functionCall, Integer integer) {
        ollirCode.append("invokestatic(");
        String name = null, type = null;

        for(var child: functionCall.getChildren()){
            if(child.getKind().equals("Terminal")){
                ollirCode.append(OllirUtils.getTerminalValue(child)+ ", ");
            }
            else if(child.getKind().equals("CallExpression")){
                ollirCode.append("\"" + child.get("value") + "\"");

                if (child.getNumChildren() != 0){
                    var terminalNode = child.getChildren().get(0);

                    name = OllirUtils.getTerminalValue(terminalNode);
                    type = OllirUtils.getTerminalType(terminalNode);

                    ollirCode.append(", " + name + "." + type);

                }
                else{

                }



            }
        }
        //em baixo devia ser chamada ao get expression type a partir de member calll (?)
        ollirCode.append(").").append("V;\n");


        return 0;
    }

    private Integer expressionVisit(JmmNode expression, Integer integer) {
        if(expression.getJmmParent().getKind().equals("IfCondition") || expression.getJmmParent().getKind().equals("WhileCondition")){
            specialExpressionVisit(expression, integer);
            return 0;
        }


        String name1 = null, name2 = null, type1 = null, type2 = null;
        JmmNode child1, child2;
        switch (expression.getKind()){
            case "Addition":
                child1 = expression.getChildren().get(0);
                child2 = expression.getChildren().get(1);
                name1 = OllirUtils.getTerminalValue(child1);
                type1 = OllirUtils.getTerminalType(child1);
                name2 = OllirUtils.getTerminalValue(child2);
                type2 = OllirUtils.getTerminalType(child2);
                ollirCode.append(lastVariable + "." + type1 + " :=." + type1 + " ");
                newVariable += "z";
                ollirCode.append(name1 + "." + type1 + " +." + type1 + " " + name2 + "." + type2 + ";\n" );
                break;

            case "Subtraction":
                child1 = expression.getChildren().get(0);
                child2 = expression.getChildren().get(1);
                name1 = OllirUtils.getTerminalValue(child1);
                type1 = OllirUtils.getTerminalType(child1);
                name2 = OllirUtils.getTerminalValue(child2);
                type2 = OllirUtils.getTerminalType(child2);
                ollirCode.append(lastVariable + "." + type1 + " :=." + type1 + " ");
                newVariable += "z";
                ollirCode.append(name1 + "." + type1 + " -." + type1 + " " + name2 + "." + type2 + ";\n" );
                break;

            case "Multiplication":
                child1 = expression.getChildren().get(0);
                child2 = expression.getChildren().get(1);
                name1 = OllirUtils.getTerminalValue(child1);
                type1 = OllirUtils.getTerminalType(child1);
                name2 = OllirUtils.getTerminalValue(child2);
                type2 = OllirUtils.getTerminalType(child2);
                ollirCode.append(lastVariable + "." + type1 + " :=." + type1 + " ");
                newVariable += "z";
                ollirCode.append(name1 + "." + type1 + " *." + type1 + " " + name2 + "." + type2 + ";\n" );


            case "LogicLessThan":
                child1 = expression.getChildren().get(0);
                child2 = expression.getChildren().get(1);
                name1 = OllirUtils.getTerminalValue(child1);
                type1 = OllirUtils.getTerminalType(child1);
                name2 = OllirUtils.getTerminalValue(child2);
                type2 = OllirUtils.getTerminalType(child2);
                ollirCode.append(lastVariable + "." + type1 + " :=." + type1 + " ");
                newVariable += "z";
                ollirCode.append(name1 + "." + type1 + " <." + type1 + " " + name2 + "." + type2 + ";\n" );
                break;


            case "LogicAnd":
                child1 = expression.getChildren().get(0);
                child2 = expression.getChildren().get(1);
                name1 = OllirUtils.getTerminalValue(child1);
                type1 = OllirUtils.getTerminalType(child1);
                name2 = OllirUtils.getTerminalValue(child2);
                type2 = OllirUtils.getTerminalType(child2);
                ollirCode.append(lastVariable + "." + type1 + " :=." + type1 + " ");
                newVariable += "z";
                ollirCode.append(name1 + "." + type1 + " &&." + type1 + " " + name2 + "." + type2 + ";\n" );
                break;

            case "Division":
                child1 = expression.getChildren().get(0);
                child2 = expression.getChildren().get(1);
                name1 = OllirUtils.getTerminalValue(child1);
                type1 = OllirUtils.getTerminalType(child1);
                name2 = OllirUtils.getTerminalValue(child2);
                type2 = OllirUtils.getTerminalType(child2);
                ollirCode.append(lastVariable + "." + type1 + " :=." + type1 + " ");
                newVariable += "z";
                ollirCode.append(name1 + "." + type1 + " /." + type1 + " " + name2 + "." + type2 + ";\n" );
                break;



        }



        return 0;
    }


    private Integer whileCondVisit(JmmNode whileCond, Integer integer) {
        ollirCode.append("Loop:\n");

        List<String> vars = new ArrayList<>();
        Integer count = 0;
        for(var child: whileCond.getChildren()){
            vars.add("bool"+count);
            ollirCode.append(vars.get(count) + ".bool :=.bool ");
            visit(child);
            count++;
        }

        ollirCode.append(";\nif(");
        for(var varr: vars){
            ollirCode.append(varr + ".bool");
            if(!varr.equals(vars.get(vars.size() - 1)))
                ollirCode.append(" & ");
        }
        //ollirCode.deleteCharAt(ollirCode.length() - 1);
        //ollirCode.deleteCharAt(ollirCode.length() - 1);
        ollirCode.append(") goto EndLoop;\n");

        return 0;
    }

    private Integer whileStmtVisit(JmmNode whileStmt, Integer integer) {
        /*
        for(var child: whileStmt.getChildren())
            visit(child);

         */



        ollirCode.append("goto Loop;\n");

        ollirCode.append("EndLoop:\n");


        return 0;
    }

    private Integer ifCondVisit(JmmNode ifCond, Integer integer) {

        List<String> vars = new ArrayList<>();
        Integer count = 0;

        if(ifCond.getChildren().get(0).getKind().equals("Terminal")) {
            ollirCode.append("if(");
            ollirCode.append(OllirUtils.getTerminalValue(ifCond.getChildren().get(0)) + "." + OllirUtils.getTerminalType(ifCond.getChildren().get(0)));

        }
        else{

            for(var child: ifCond.getChildren()){
                vars.add("bool"+count);
                ollirCode.append(vars.get(count) + ".bool :=.bool ");
                visit(child);
                count++;
            }

            ollirCode.append(";\nif(");


            for(var varr: vars) {
                ollirCode.append(varr + ".bool");
                if (!varr.equals(vars.get(vars.size() - 1)))
                    ollirCode.append(" & ");
            }
        }

        ollirCode.append(") goto IfLabel;\ngoto ElseLabel;\n");




        return 0;
    }

    private Integer ifStmtVisit(JmmNode ifStmt, Integer integer) {
        for(var child: ifStmt.getChildren())
            visit(child);

        return 0;
    }

    private Integer elseStmtVisit(JmmNode elseStmt, Integer integer) {
        for(var child: elseStmt.getChildren())
            visit(child);

        return 0;
    }

    private void specialExpressionVisit(JmmNode expression, Integer integer) {
        String name1 = null, name2 = null, type1 = null, type2 = null;
        JmmNode child1, child2;
        switch (expression.getKind()){
            case "LogicLessThan":
                child1 = expression.getChildren().get(0);
                child2 = expression.getChildren().get(1);
                name1 = OllirUtils.getTerminalValue(child1);
                System.out.println("child1 is" + child1);
                type1 = OllirUtils.getTerminalType(child1);
                name2 = OllirUtils.getTerminalValue(child2);
                type2 = OllirUtils.getTerminalType(child2);
                ollirCode.append(name1 + "." + type1 + " <." + type1 + " " + name2 + "." + type2);
                break;


        }

    }







}
