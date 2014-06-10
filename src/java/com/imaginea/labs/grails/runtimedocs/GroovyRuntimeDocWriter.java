package com.imaginea.labs.grails.runtimedocs;

import org.codehaus.groovy.tools.groovydoc.OutputTool;

import java.io.File;
import java.util.Iterator;
import java.util.Properties;

/**
 * Writes RuntimeDocs Resources to Destination.
 */
public class GroovyRuntimeDocWriter {
    private OutputTool output;
    private GroovyRuntimeDocTemplateEngine templateEngine;
    public static final String FS = "/";
    public static final String BSS = "\\";
    public static final String RESOURCES = "resources";
    private Properties properties;

    public GroovyRuntimeDocWriter(OutputTool output, GroovyRuntimeDocTemplateEngine templateEngine, Properties properties) {
        this.output = output;
        this.templateEngine = templateEngine;
        this.properties = properties;
    }

    public void writeClasses(RootDoc rootDoc, String destDir) throws Exception {
        for (ClassDoc classDoc : rootDoc.getAllClasses()) {
            writeClassToOutput(classDoc, destDir);
        }
    }

    public void writeClassToOutput(ClassDoc classDoc, String destDir) throws Exception {
        String destFileName = destDir + BSS + classDoc.getFullPathName() + ".html";
        String renderedSrc = templateEngine.applyClassTemplates(classDoc);
        if (renderedSrc != null) {
            System.out.println("Generating " + destFileName);
            output.writeToOutput(destFileName, renderedSrc);
        }
    }

    public void writePackages(RootDoc rootDoc, String destDir) throws Exception {
        for (PackageDoc packageDoc : rootDoc.getPackages()) {
            if (new File(packageDoc.getPackagePath()).isAbsolute()) continue;
            output.makeOutputArea(destDir + FS + packageDoc.getPackagePath());
            writePackageToOutput(packageDoc, destDir);
        }
        StringBuilder sb = new StringBuilder();
        for (PackageDoc packageDoc : rootDoc.getPackages()) {
            sb.append(packageDoc.getName());
            sb.append("\n");
        }
        String destFileName = destDir + FS + "package-list";
        System.out.println("Generating " + destFileName);
        output.writeToOutput(destFileName, sb.toString());
    }

    public void writePackageToOutput(PackageDoc packageDoc, String destDir) throws Exception {
        Iterator<String> templates = templateEngine.packageTemplatesIterator();
        while (templates.hasNext()) {
            String template = templates.next();
            String renderedSrc = templateEngine.applyPackageTemplate(template, packageDoc);
            String fileName = new File(template).getName();
            String filePath = destDir + FS + packageDoc.getPackagePath() + FS + fileName;
            System.out.println("Generating " + filePath);
            output.writeToOutput(filePath, renderedSrc);
        }
    }

    public void writeRoot(RootDoc rootDoc, String destDir) throws Exception {
        output.makeOutputArea(destDir);
        output.makeOutputArea(destDir + FS + RESOURCES);
        writeRootDocToOutput(rootDoc, destDir);
    }

    public void writeRootDocToOutput(RootDoc rootDoc, String destDir) throws Exception {
        Iterator<String> templates = templateEngine.docTemplatesIterator();
        while (templates.hasNext()) {
            String template = templates.next();
            String fileName = new File(template).getName();
            String filePath;
            if (hasBinaryExtension(template)) {
                filePath = destDir + FS + RESOURCES + FS + fileName;
                System.out.println("Generating " + filePath);
                templateEngine.copyBinaryResource(template, filePath);
            } else {
                filePath = destDir + FS + fileName;
                System.out.println("Generating " + filePath);
                String renderedSrc = templateEngine.applyRootDocTemplate(template, rootDoc);
                output.writeToOutput(filePath, renderedSrc);
            }
        }
    }

    private boolean hasBinaryExtension(String template) {
        return template.endsWith(".gif") || template.endsWith(".ico");
    }
}