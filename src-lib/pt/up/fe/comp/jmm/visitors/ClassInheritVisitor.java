package pt.up.fe.comp.jmm.visitors;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.specs.util.SpecsCollections;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ClassInheritVisitor extends PreorderJmmVisitor<Boolean, List<String>> {
    public ClassInheritVisitor() {
        addVisit("ClassInherit", this::visitClassInherit);

        setDefaultValue(() -> Collections.emptyList());

        setReduceSimple(this::reduceSimple);
    }

    private List<String> reduceSimple(List<String> list1, List<String> list2) {
        return SpecsCollections.concatList(list1, list2);
    }

    private List<String> visitClassInherit(JmmNode importDecl, Boolean dummy) {

        var importString = importDecl.getChildren().stream()
                .map(id -> id.get("name"))
                .collect(Collectors.joining("."));

        return Arrays.asList(importString);
    }
}
