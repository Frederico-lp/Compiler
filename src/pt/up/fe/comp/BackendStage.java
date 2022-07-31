package pt.up.fe.comp;

import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.util.Collections;

public class BackendStage implements JasminBackend {
    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        System.out.println("JASMIN\n");

        String jasminCode = new OllirToJasmin(ollirResult.getOllirClass()).getCode();
        System.out.println(jasminCode);
        //ollirResult.getOllirClass().getMethod(0); //gerar codigo com base nisto porque pode se identificar exatamente o metodo e etc que se quer
        return new JasminResult(ollirResult, jasminCode , Collections.emptyList());
    }
}
