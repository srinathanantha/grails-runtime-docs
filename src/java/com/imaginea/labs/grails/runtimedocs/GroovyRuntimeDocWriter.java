package com.imaginea.labs.grails.runtimedocs;

import java.io.*;
import java.util.Iterator;

/**
 * Writes RuntimeDocs Resources to Destination.
 */
public class GroovyRuntimeDocWriter {
    private GroovyRuntimeDocTemplateEngine templateEngine;
    public static final String FS = "/";
    public static final String BSS = "\\";
    public static final String RESOURCES = "resources";

    public GroovyRuntimeDocWriter(GroovyRuntimeDocTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
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
            writeToOutput(destFileName, renderedSrc);
        }
    }

    public void writePackages(RootDoc rootDoc, String destDir) throws Exception {
        for (PackageDoc packageDoc : rootDoc.getPackages()) {
            if (new File(packageDoc.getPackagePath()).isAbsolute()) continue;
            makeOutputArea(destDir + FS + packageDoc.getPackagePath());
            writePackageToOutput(packageDoc, destDir);
        }
        StringBuilder sb = new StringBuilder();
        for (PackageDoc packageDoc : rootDoc.getPackages()) {
            sb.append(packageDoc.getName());
            sb.append("\n");
        }
        String destFileName = destDir + FS + "package-list";
        System.out.println("Generating " + destFileName);
        writeToOutput(destFileName, sb.toString());
    }

    public void writePackageToOutput(PackageDoc packageDoc, String destDir) throws Exception {
        Iterator<String> templates = templateEngine.packageTemplatesIterator();
        while (templates.hasNext()) {
            String template = templates.next();
            String renderedSrc = templateEngine.applyPackageTemplate(template, packageDoc);
            String fileName = new File(template).getName();
            String filePath = destDir + FS + packageDoc.getPackagePath() + FS + fileName;
            System.out.println("Generating " + filePath);
            writeToOutput(filePath, renderedSrc);
        }
    }

    public void writeRoot(RootDoc rootDoc, String destDir) throws Exception {
        makeOutputArea(destDir);
        makeOutputArea(destDir + FS + RESOURCES);
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
                writeToOutput(filePath, renderedSrc);
            }
        }
    }

    private boolean hasBinaryExtension(String template) {
        return template.endsWith(".gif") || template.endsWith(".ico");
    }

    private void makeOutputArea(String filename) {
        File dir = new File(filename);
        dir.mkdirs();
    }

    /*
        Taken from FileOutputTool
    */
    private void writeToOutput(String fileName, String text) throws Exception {
        File file = new File(fileName);
        file.getParentFile().mkdirs();
        write(file, text);
    }


    /**
     * Write the text to the File. Taken from DefaultGroovyMethods.
     *
     * @param file a File
     * @param text the text to write to the File
     * @throws IOException if an IOException occurs.
     * @since 1.0
     */
    public void write(File file, String text) throws IOException {
        Writer writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(text);
            writer.flush();

            Writer temp = writer;
            writer = null;
            temp.close();
        } finally {
            closeWithWarning(writer);
        }
    }

    /**
     * Close the Closeable. Logging a warning if any problems occur.
     * Taken from DefaultGroovyMethods.
     *
     * @param c the thing to close
     */
    public void closeWithWarning(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                System.out.println("Caught exception during close(): " + e);
            }
        }
    }
}