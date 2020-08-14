package de.jcup.hijson.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class BigJSONFileGenerator {
    
    public static void main(String[] args) throws IOException {
        BigJSONFileGenerator generator = new BigJSONFileGenerator();
        generator.generateFile(new File("./testscripts/gen/bigscript01-150kb-no-new-lines.json"), 1000L, false);
        generator.generateFile(new File("./testscripts/gen/bigscript02-1.5MB-no-new-lines.json"), 10000L, false);
        generator.generateFile(new File("./testscripts/gen/bigscript03-15MB-no-new-lines.json"), 100000L, false);
    }

    private void generateFile(File file, long amountOfTestUnits, boolean withNewLine) throws IOException {
        file.getParentFile().mkdirs();
        file.delete();
        
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        handleNewLine(sb,withNewLine);
        sb.append("\"testUnits\" : [");
        handleNewLine(sb,withNewLine);
        for (long x=0;x<amountOfTestUnits;x++) {
            sb.append("{");
            handleNewLine(sb,withNewLine);
            sb.append("\"unit-null\" : null , ");
            handleNewLine(sb,withNewLine);
            sb.append("\"unit-boolean-true\" : true , ");
            handleNewLine(sb,withNewLine);
            sb.append("\"unit-boolean-false\" : false , ");
            handleNewLine(sb,withNewLine);
            sb.append("\"unit-long\" : "+x+" , ");
            handleNewLine(sb,withNewLine);
            sb.append("\"unit-string\" : \""+x+"\" , ");
            handleNewLine(sb,withNewLine);
            double f = x+0.1;
            sb.append("\"unit-double\" : "+f);
            handleNewLine(sb,withNewLine);
            sb.append(" }");
            if (x<amountOfTestUnits-1) {
                sb.append(",");
            }
        }
        sb.append("]");
        handleNewLine(sb,withNewLine);
        sb.append(" }");
        handleNewLine(sb,withNewLine);
        
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(sb.toString());
        }
        
//        Files.write(file.toPath(), sb.toString().getBytes(Charset.defaultCharset()));
    }
    
    private void handleNewLine(StringBuilder sb, boolean enabled) {
        if (enabled) {
            sb.append("\n");
        }
    }

}
