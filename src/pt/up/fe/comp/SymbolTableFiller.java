package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SymbolTableFiller extends PreorderJmmVisitor<SymbolTableBuilder, Integer> {
    private final List<Report> reports;

    public SymbolTableFiller(){
        this.reports = new ArrayList<>();

        addVisit(AstNode.IMPORT_DECLARATION, this::importDeclarationVisit);
        addVisit(AstNode.CLASS_DECLARATION, this::classDeclarationVisit);
        addVisit(AstNode.METHOD_DECLARATION, this::methodDeclarationVisit);
    }

    public Integer importDeclarationVisit(JmmNode importNode, SymbolTableBuilder symbolTable){
        var importString = importNode.get("value");
        if(importNode.getChildren().size() != 0){
            importString += "." + importNode.getChildren().stream()
                .map(id -> id.get("value"))
                .collect(Collectors.joining("."));
        }
        symbolTable.addImport(importString);
        return 0;
    }

    public Integer classDeclarationVisit(JmmNode classNode, SymbolTableBuilder symbolTable){
        symbolTable.setClassName(classNode.get("value"));

        for(var child : classNode.getChildren()){
            if(child.getKind().equals("ClassExtends"))
                symbolTable.setSuperName(child.get("value"));
        }

        return 0;
    }

    public Integer methodDeclarationVisit(JmmNode methodNode, SymbolTableBuilder symbolTable){
        var methodName = methodNode.get("value");
        if(symbolTable.hasMethod(methodName)){
            reports.add(Report.newError(Stage.SEMANTIC,
                Integer.parseInt(methodNode.get("line")),
                Integer.parseInt(methodNode.get("col")),
                "The method " + methodName + "has already been declared before",
                null));
        }

        var methodFirstChild = methodNode.getJmmChild(0);
        var methodChildren = methodNode.getChildren();

        List<Symbol> parameters = new ArrayList<>();
        List<Symbol> localVariables = new ArrayList<>();

        var isArray = false;
        var value = "";
        var type = "";
        for(var child : methodChildren){
            if(child.getKind().equals("Parameter")){
                value = child.get("value");
                type = child.getJmmChild(0).get("type");
                isArray = child.getJmmChild(0).getOptional("isArray")
                    .map(Boolean::valueOf)
                    .orElse(false);

                parameters.add(new Symbol(new Type(type, isArray), value));
            }

            else if(child.getKind().equals("VarDeclaration")){
                value = child.get("value");
                type = child.getJmmChild(0).get("type");
                isArray = child.getJmmChild(0).getOptional("isArray")
                    .map(Boolean::valueOf)
                    .orElse(false);
                localVariables.add(new Symbol(new Type(type, isArray), value));
            }

            else if(child.getKind().equals("MainParameter")){
                value = child.get("value");
                type = child.get("type");
                parameters.add(new Symbol(new Type(type, true), value));
            }
        }

        if(methodName.equals("main"))
            symbolTable.addMethod(methodName, new Type("void", false), parameters, localVariables);

        else{
            isArray = methodFirstChild.getOptional("isArray")
                .map(Boolean::valueOf)
                .orElse(false);

            symbolTable.addMethod(methodName, new Type(methodFirstChild.get("type"), isArray), parameters,
                localVariables);
        }

        return 0;
    }

    public List<Report> getReports() {
        return reports;
    }
}
