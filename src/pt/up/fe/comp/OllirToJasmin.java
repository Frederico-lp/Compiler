package pt.up.fe.comp;

import org.specs.comp.ollir.Type;
import org.specs.comp.ollir.*;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.stream.Collectors;

public class OllirToJasmin {
    private final ClassUnit classUnit;

    private Map<String,Descriptor> varTable;

    private ArrayList<String> imports;

    private String className;

    private String superClass;

    private Method method;

    public OllirToJasmin(ClassUnit classUnit){
        this.classUnit = classUnit;
    }



    public String getFullyQualifiedName(String className){


        String lastName;

        for(var importString : classUnit.getImports()) {

            var splittedImport = importString.split("\\.");

            if (splittedImport.length == 0){
                lastName = importString;
            }
            else {
                lastName = splittedImport[splittedImport.length - 1];
            }

            if (lastName.equals(className)){
                return importString.replace('.', '/');
            }
        }


        return "java/lang/Object";

    }

    public String getCode(){

        var code = new StringBuilder();

        className = classUnit.getClassName();
        imports = classUnit.getImports();
        if (classUnit.getSuperClass() == null)
            superClass = "java/lang/Object";
        else
            superClass = classUnit.getSuperClass();

        code.append(".class public ").append(classUnit.getClassName()).append("\n");
        //System.out.println(code.toString());
        //System.out.println(classUnit.getClassName());
        var superQualifiedName = getFullyQualifiedName(classUnit.getSuperClass());
        code.append(".super ").append(superQualifiedName).append("\n\n");

        String constructor = ".method public <init>()V\n" +
                "       aload_0\n" +
                "       invokenonvirtual ${SUPER_NAME}/<init>()V\n" +
                "       return\n" +
                "    .end method \n";

        code.append(constructor.replace("${SUPER_NAME}",superQualifiedName));

        for (var field :classUnit.getFields()){
            code.append(getCode(field));
        }

        var methods = classUnit.getMethods();
        methods.remove(0);

        for ( var method : methods){
            varTable = method.getVarTable();
            this.method = method;
            code.append(getCode(method));
        }


        return code.toString();

    }

    private String accessModifierToJasmin(AccessModifiers classAccessModifier) {
        return String.valueOf(classAccessModifier.equals(AccessModifiers.DEFAULT) ? AccessModifiers.PUBLIC : classAccessModifier).toLowerCase();
    }

    private String getCode(Field field) {
        return ".field " + accessModifierToJasmin(field.getFieldAccessModifier()) + " " + field.getFieldName() + " " + getJasminType(field.getFieldType()) + "\n";
    }
    public String getCode(Method method){
        var code = new StringBuilder();

        Integer currentReg = 1;
        if(method.isStaticMethod())
            currentReg = 0;

        code.append(".method");
        code.append(" ");

        if (!method.getMethodAccessModifier().name().equals("DEFAULT")){
            code.append(method.getMethodAccessModifier().name().toLowerCase()).append(" ");
        }

        if (method.isStaticMethod()){
            code.append("static ");
        }

        code.append(method.getMethodName()).append("(");

        var methodParamTypes = method.getParams().stream().map(
                element-> getJasminType(element.getType())).collect(Collectors.joining());

        if(!methodParamTypes.isEmpty())
            code.append(methodParamTypes).append(")");
        else
            code.append(")");

        code.append(getJasminType(method.getReturnType())).append("\n");

        code.append(".limit stack 99\n");

        code.append(".limit locals 99\n\n");

        for(var param: method.getParams()){
            code.append(getVar(param));
            System.out.println("param");
            currentReg++;
        }


        for ( var inst : method.getInstructions()){
            System.out.println(inst.toString());
            code.append(getCode(inst));
        }



        //add return
        code.append(".end method \n");

        return code.toString();
    }

    public String getCode(Instruction method){

        if(method instanceof CallInstruction){
            System.out.println("CallInstruction\n");
            return getCode((CallInstruction) method);
        }

        else if(method instanceof ReturnInstruction) {
            System.out.println("ReturnInstruction\n");
            return getCode((ReturnInstruction) method);
        }
        else if(method instanceof AssignInstruction) {
            System.out.println("AssignInstruction\n");
            return getCode((AssignInstruction) method);
        }
        else if(method instanceof CondBranchInstruction) {
            System.out.println("CondBranchInstruction\n");

            return getCode((CondBranchInstruction) method);
        }
        else if(method instanceof GetFieldInstruction) {
            System.out.println("GetFieldInstruction\n");

            return getCode((GetFieldInstruction) method);
        }
        else if(method instanceof PutFieldInstruction) {
            System.out.println("PutFieldInstruction\n");

            return getCode((PutFieldInstruction) method);
        }
        else if(method instanceof GotoInstruction) {
            System.out.println("GotoInstruction\n");

            return getCode((GotoInstruction) method);
        }

        else if(method instanceof OpInstruction) {
            System.out.println("OpInstruction\n");

            return getCode((OpInstruction) method);
        }

        throw new NotImplementedException(method.getClass());


    }

    public String getAssign(CallInstruction method){
        return "";
    }
    public String getCode(CallInstruction instruction){
        //var code = new StringBuilder();

        switch(instruction.getInvocationType()) {
            case invokestatic: {
                System.out.println("Statc");

                return getCodeInvokeStatic(instruction);
            }
            case invokevirtual:{
                System.out.println("VIRTUAL");
                return getCodeInvokeVirtual(instruction);
            }
            case invokespecial: {
                System.out.println("special");

                return getCodeInvokeSpecial(instruction);
            }
            case NEW:{
                System.out.println("new");

                return getCodeNew(instruction);
            }
            case arraylength:
                return getCodeArrayLength(instruction);


            default: {
                throw new NotImplementedException(instruction.getInvocationType());
            }
        }


    }

    private String getCodeArrayLength(CallInstruction instruction) {
        if ( instruction.getFirstArg().isLiteral()){
            return getConstSize(instruction.getFirstArg().toString()) + "arraylength\n";
        }
        return getVar(instruction.getFirstArg()) + "arraylength\n";

    }

    private String getCodeNew(CallInstruction instruction) {
        StringBuilder code = new StringBuilder();
        if (instruction.getReturnType().getTypeOfElement() == ElementType.OBJECTREF) {
            for (Element e : instruction.getListOfOperands()) {
                code.append(getVar(e));
            }

            code.append("new ")
                    .append(((Operand) instruction.getFirstArg()).getName()).append("\n")
                    .append("dup\n");
        }
        else if (instruction.getReturnType().getTypeOfElement() == ElementType.ARRAYREF) {
            code.append(generateNewArray(instruction));
        }



        return code.toString();
    }


    public String getCode(ReturnInstruction instruction){
        StringBuilder code = new StringBuilder();

        if (!instruction.hasReturnValue() || instruction.getOperand() == null) {
            return "return\n";
        }

        ElementType returnType = instruction.getOperand().getType().getTypeOfElement();

        if ( instruction.getOperand().isLiteral()){
            LiteralElement element = (LiteralElement) instruction.getOperand();
            Integer n = Integer.parseInt(element.getLiteral() );
            code.append(getConstSize(n.toString()));

        }
        else if (instruction.getOperand() != null){
            code.append( getVar(instruction.getOperand()));
        }

        code.append(  ((returnType == ElementType.INT32 || returnType == ElementType.BOOLEAN) ? "i" : "a") + "return\n");


        return code.toString();
    }

    public String getCode(AssignInstruction instruction){
        Instruction rhs = instruction.getRhs();
        switch (rhs.getInstType()) {
            case NOPER:
                System.out.println("NOPER");
                return  generateAssignNOper(instruction);
            case BINARYOPER:
                System.out.println("BINARYOPER");
                return generateAssignBinaryOper(instruction);
            case GETFIELD:
                return getCode(instruction);
            case CALL:
                return generateAssignCall(instruction);
            case UNARYOPER:
                return generateAssignNot(instruction);
        }
        return "";
    }


    private String generateAssignNot(AssignInstruction instruction) {
        return  "";
    }

    private String generateAssignCall(AssignInstruction instruction) {

        StringBuilder code = new StringBuilder();
        Instruction rhs = instruction.getRhs();

        return getCode(rhs);
    }






    private String generateAssignBinaryOper(AssignInstruction instruction) {

        System.out.println("BINARY OPER");

        StringBuilder code = new StringBuilder();

        Instruction rhs = instruction.getRhs();

        OperationType operation = ((BinaryOpInstruction) rhs).getOperation().getOpType();
        Element leftElement = ((BinaryOpInstruction) rhs).getLeftOperand();
        Element rightElement = ((BinaryOpInstruction) rhs).getRightOperand();

        Element element = instruction.getDest();

        if (leftElement.isLiteral()) {
            String value = ((LiteralElement) leftElement).getLiteral();
            code.append(getConstSize(value));
        }
        else {
            code.append(getVar(leftElement));
        }
        if (rightElement.isLiteral()) {
            String value = ((LiteralElement) rightElement).getLiteral();
            code.append(getConstSize(value));
        }
        else {
            code.append(getVar(rightElement));
        }
        if (operation.toString().equals("ANDB")){
            code.append("and \n");
        }
        else{
            code.append("i" + operation.toString().toLowerCase() + "\n");
        }

        if (! element.isLiteral())
            code.append(setVar(element));
        else{
            LiteralElement el = (LiteralElement) element;
            Integer n = Integer.parseInt(el.getLiteral() );
            code.append(getConstSize(n.toString()));
        }


        return code.toString();
    }

    private String generateNewArray(CallInstruction instruction) {

        StringBuilder code = new StringBuilder();
        Element element = instruction.getListOfOperands().get(0);
        if (element.isLiteral()){
            Integer n = Integer.parseInt(((LiteralElement) element).getLiteral() );
            code.append(getConstSize(n.toString()));

        }
        else {
            code.append(getVar(element));
        }
        code.append("\nnewarray int \n");


        return code.toString();
    }

    public String getCode(CondBranchInstruction instruction){
        StringBuilder code = new StringBuilder();

        Instruction i = instruction.getCondition();

        code.append(getCode(i));

        code.append("ifne THEN_" + instruction.getLabel() + "\n");
        for ( var e : instruction.getOperands()){
            if (e.isLiteral()){
                Integer n = Integer.parseInt(((LiteralElement)e).getLiteral() );
                getConstSize(n.toString());
            }
            else{
                getVar(e);
            }
        }
        /*InstructionType conditionType = (Op)instruction.getCondition().getInstType();

        switch (instruction.getOperands().) {
            case EQ:
                code.append("\n\t\tif_icmpeq ");
                break;
            case LTH:
                code.append("\n\t\tif_icmplt ");
                break;
            case ANDB:
                code.append("\n\t\tiandb\n\t\ticonst_1\n\t\tif_icmpeq ");
                break;
            default:
                break;
        }
        */

        code.append(instruction.getLabel());

        return code.toString();

    }
    public String getCode( PutFieldInstruction instruction) {
        StringBuilder code = new StringBuilder();
        Element e1 = instruction.getFirstOperand();
        Element e2 = instruction.getSecondOperand();
        Element e3 = instruction.getThirdOperand();
        Operand o1 = (Operand) e1;
        Operand o2 = (Operand) e2;

        String name = o1.getName();
        code.append(getVar(e1));

        if (e3.isLiteral()){
            code.append(getConstSize(e3.toString()));
        }
        else{
            code.append(getVar(e3));
        }


        code.append("\n\t\tputfield " + name + "/" + o2.getName() + " " + getJasminType(method.getReturnType()) + "\n");
        return code.toString();
    }

    public String getCode(GetFieldInstruction instruction){
        StringBuilder code = new StringBuilder();

        Element e1 = instruction.getFirstOperand();
        if (!e1.isLiteral()) {
            code.append("\n a" + getConstSize(e1.toString()));
        }

        code.append("\ngetfield " + className + "/");
        e1 = instruction.getSecondOperand();

        if (!e1.isLiteral()) {
            Operand o1 = (Operand) e1;
            code.append(o1.getName() + " " +  getJasminType(method.getReturnType()));
        }

        return code.toString();
    }

    public String getCode(GotoInstruction instruction){
        return ("\n goto " + instruction.getLabel() + "\n");

    }

    public String getCode(OpInstruction instruction){
        return "";
    }

    public static String getConstSize( String value) {
        int val = Integer.parseInt(value);
        String res, aux;
        if (val >= 0 && val <= 5) aux = "iconst_";
        else if (val > 5 && val <= 128) aux = "bipush ";
        else if (val > 128 && val <= 32768) aux = "sipush ";
        else aux = "ldc ";
        res =  aux + val + "\n";
        return res;
    }
    public String generateAssignNOper( AssignInstruction instruction){
        System.out.println("generate noper\n");
        StringBuilder code = new StringBuilder();

        Instruction rhs = instruction.getRhs();

        Element singleOperand = ((SingleOpInstruction) rhs).getSingleOperand();
        Element dest = instruction.getDest();
        if (singleOperand.isLiteral()) {
            String value = ((LiteralElement) singleOperand).getLiteral();

            code.append(getConstSize(value));

        }


        Element e = ((SingleOpInstruction) rhs).getSingleOperand();

        code.append(setVar(dest));

        return code.toString();
    }


    private String getCodeInvokeSpecial(CallInstruction method){
        var code = new StringBuilder();

        code.append(getVar(method.getFirstArg()));

        code.append("invokespecial ")
                .append((method.getFirstArg().getType().getTypeOfElement() == ElementType.THIS) ? superClass :"L" + className)
                .append(".<init>(");

        for (Element e : method.getListOfOperands())
            code.append(getJasminType(e.getType()));

        code.append(")").append(getJasminType(method.getReturnType())).append("\n");


        return code.toString();
    }

    private String getCodeInvokeVirtual(CallInstruction instruction) {
        StringBuilder code = new StringBuilder();
        code.append(getVar(instruction.getFirstArg()));

        for (Element e : instruction.getListOfOperands()){
            if (! e.isLiteral())
                code.append(getVar(e));

        }


        code.append("\ninvokevirtual ")
                .append(className)
                .append(".").append(((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", ""))
                .append("(");

        for (Element e : instruction.getListOfOperands())
            code.append(getJasminType(e.getType()));

        code.append(")").append(getJasminType(instruction.getReturnType())).append("\n");

        return code.toString();

    }
    private String getCodeInvokeStatic(CallInstruction method) {

        var code = new StringBuilder();

        for (Element operand : method.getListOfOperands()){
            if (! operand.isLiteral())
                code.append(getVar(operand));
        }

        code.append("invokestatic ");


            var methodClass = ((Operand) method.getFirstArg()).getName();

            code.append(((Operand) method.getFirstArg()).getName());

            code.append("/");

            String full = ((LiteralElement) method.getSecondArg()).getLiteral();
            String sub = full.substring(1, full.length() - 1);
            System.out.println(sub);
            code.append(sub);//retorna com aspas resolver
            code.append("(");

            var methodParamTypes =  method.getListOfOperands().stream().map(
                element-> getJasminType(element.getType())).collect(Collectors.joining());
            code.append(methodParamTypes);

            code.append(")");
            code.append(getJasminType(method.getReturnType()));
            code.append("\n");


            return code.toString();
    }



    public String getJasminType(Type type) {


        if(type.getTypeOfElement() == ElementType.ARRAYREF){
            return "[" + getJasminType(((ArrayType) type).getTypeOfElements());
        }
        else if (type.getTypeOfElement() == ElementType.OBJECTREF){
            String className = ((ClassType) type).getName();
            for (String imported : imports) {
                if (imported.endsWith("." + className))
                    return  "[" + "L" + imported.replace('.', '/') + ";";
            }
            return "[" + "L" + className + ";";
        }



        return getJasminType(type.getTypeOfElement());
    }

    public String getJasminType(ElementType type) {
        switch (type) {
            case INT32:
                return "I";
            case BOOLEAN:
                return "Z";
            case STRING:
                return "Ljava/lang/String;";
            case VOID:
                return "V";
            default:
                throw new NotImplementedException(type);
        }
    }

    public String setVar(Element e){
        Operand op = (Operand) e;

        Descriptor value = varTable.get(op.getName());

        if (value != null){
            if(e.getType().getTypeOfElement().equals(ElementType.INT32) || e.getType().getTypeOfElement().equals(ElementType.BOOLEAN)) {
                return "istore " + varTable.get(op.getName()).getVirtualReg() + "\n";
            }
            else return "astore " + varTable.get(op.getName()).getVirtualReg() + "\n";

        }

        return "";


    }

    public String getVar(Element e) {
        Operand op = (Operand) e;
        Descriptor value = varTable.get(op.getName());

        if (value != null) {
            if (e.getType().getTypeOfElement().equals(ElementType.INT32) || e.getType().getTypeOfElement().equals(ElementType.BOOLEAN)) {
                return "iload " + varTable.get(op.getName()).getVirtualReg() + "\n";
            } else return "aload " + varTable.get(op.getName()).getVirtualReg() + "\n";

        }

        return "";
    }


}

