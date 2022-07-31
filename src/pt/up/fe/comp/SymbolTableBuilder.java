package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.*;
import java.util.stream.Collectors;

public class SymbolTableBuilder implements SymbolTable {
    private List<String> imports;
    private String className;
    private String superClassName;

    private List<Symbol> fields;
    private List<String> methods;

    private Map<String, Type> methodReturnTypes;
    private Map<String, List<Symbol>> methodParameters;
    private Map<String, List<Symbol>> methodLocalVariables;

    public SymbolTableBuilder(){
        this.className = null;
        this.superClassName = null;
        this.imports = new ArrayList<>();
        this.fields = new ArrayList<>();
        this.methods = new ArrayList<>();
        this.methodReturnTypes = new HashMap<>();
        this.methodParameters = new HashMap<>();
        this.methodLocalVariables = new HashMap<>();
    }

    @Override
    public List<String> getImports(){
        return imports;
    }

    @Override
    public String getClassName(){
        return className;
    }

    @Override
    public String getSuper(){
        return superClassName;
    }

    @Override
    public List<Symbol> getFields(){
        return fields;
    }

    @Override
    public List<String> getMethods(){
        return methods;
    }

    @Override
    public Type getReturnType(String methodSignature){
        return methodReturnTypes.get(methodSignature);
    }

    @Override
    public List<Symbol> getParameters(String methodSignature){
        return methodParameters.get(methodSignature);
    }

    @Override
    public List<Symbol> getLocalVariables(String methodSignature){
        return methodLocalVariables.get(methodSignature);
    }

    public void setClassName(String className){
        this.className = className;
    }

    public void setSuperName(String superClassName){
        this.superClassName = superClassName;
    }

    public boolean hasMethod(String methodSignature){
        return methods.contains(methodSignature);
    }

    public void addImport(String importLine){
        imports.add(importLine);
    }

    public void addMethod(String methodSignature, Type returnType, List<Symbol> params,
                          List<Symbol> localVariables){
        methods.add(methodSignature);
        methodReturnTypes.put(methodSignature, returnType);
        methodParameters.put(methodSignature, params);
        methodLocalVariables.put(methodSignature, localVariables);
    }

    @Override
    public boolean isLocalVariable(String methodSignature, String identifier){
        List<String> localNames = methodLocalVariables.get(methodSignature).stream()
            .map(Symbol::getName)
            .collect(Collectors.toList());

        return localNames.lastIndexOf(identifier) != -1;
    }

    @Override
    public boolean isParameter(String methodSignature, String identifier){
        List<String> parameters = methodParameters.get(methodSignature).stream()
            .map(Symbol::getName)
            .collect(Collectors.toList());

        return parameters.lastIndexOf(identifier) != -1;
    }

    @Override
    public boolean isField(String identifier){
        List<String> fieldNames = fields.stream()
            .map(Symbol::getName)
            .collect(Collectors.toList());

        return fieldNames.lastIndexOf(identifier) != -1;
    }

    @Override
    public boolean isImport(String identifier){
        return imports.contains(identifier);
    }



}
