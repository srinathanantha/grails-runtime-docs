package com.imaginea.labs.grails.runtimedocs;

import groovy.text.GStringTemplateEngine;
import groovy.text.Template;
import groovy.text.TemplateEngine;
import org.codehaus.groovy.tools.groovydoc.ClasspathResourceManager;

import java.io.*;
import java.util.*;


public class GroovyRuntimeDocTemplateEngine {
    private TemplateEngine engine;
    private ClasspathResourceManager resourceManager;
    private Properties properties;
    private Map<String, Template> docTemplates; // cache
    private List<String> docTemplatePaths; // once per documentation set
    private Map<String, Template> packageTemplates; // cache
    private List<String> packageTemplatePaths; // once per package
    private Map<String, Template> classTemplates; // cache
    private List<String> classTemplatePaths; // once per class

    public GroovyRuntimeDocTemplateEngine(  ClasspathResourceManager resourceManager,
                                            String[] docTemplates,
                                            String[] packageTemplates,
                                            String[] classTemplates,
                                            Properties properties) {
        this.resourceManager = resourceManager;
        this.properties = properties;
        this.docTemplatePaths = Arrays.asList(docTemplates);
        this.packageTemplatePaths = Arrays.asList(packageTemplates);
        this.classTemplatePaths = Arrays.asList(classTemplates);
        this.docTemplates = new HashMap<String, Template>();
        this.packageTemplates = new HashMap<String, Template>();
        this.classTemplates = new HashMap<String, Template>();
        engine = new GStringTemplateEngine();

    }

    String applyClassTemplates(ClassDoc classDoc) {
        String templatePath = classTemplatePaths.get(0);
        String templateWithBindingApplied = null;
        try {
            Template t = classTemplates.get(templatePath);
            if (t == null) {
                t = engine.createTemplate(resourceManager.getReader(templatePath));
                classTemplates.put(templatePath, t);
            }
            Map<String, Object> binding = new HashMap<String, Object>();
            binding.put("classDoc", classDoc);
            binding.put("props", properties);
            templateWithBindingApplied = t.make(binding).toString();
        } catch (Exception e) {
            System.out.println("Exception generating file '" + classDoc.getFullPathName() + "' : " + e.getMessage());
        }
        return templateWithBindingApplied;
    }

    String applyPackageTemplate(String template, PackageDoc packageDoc) {
        String templateWithBindingApplied = "";
        try {
            Template t = packageTemplates.get(template);
            if (t == null) {
                t = engine.createTemplate(resourceManager.getReader(template));
                packageTemplates.put(template, t);
            }
            Map<String, Object> binding = new HashMap<String, Object>();
            binding.put("packageDoc", packageDoc);
            binding.put("props", properties);
            templateWithBindingApplied = t.make(binding).toString();
        } catch (Exception e) {
            System.out.println("Exception generating file '" + template + "' : " + e.getMessage());
        }
        return templateWithBindingApplied;
    }

    String applyRootDocTemplate(String template, RootDoc rootDoc) {
        String templateWithBindingApplied = "";
        try {
            Template t = docTemplates.get(template);
            if (t == null) {
                t = engine.createTemplate(resourceManager.getReader(template));
                docTemplates.put(template, t);
            }
            Map<String, Object> binding = new HashMap<String, Object>();
            binding.put("rootDoc", rootDoc);
            binding.put("props", properties);
            templateWithBindingApplied = t.make(binding).toString();
        } catch (Exception e) {
            System.out.println("Exception in generating file '" + template + "' : " + e.getMessage());
        }
        return templateWithBindingApplied;
    }

    Iterator<String> classTemplatesIterator() {
        return classTemplatePaths.iterator();
    }

    Iterator<String> packageTemplatesIterator() {
        return packageTemplatePaths.iterator();
    }

    Iterator<String> docTemplatesIterator() {
        return docTemplatePaths.iterator();
    }

    public void copyBinaryResource(String template, String destFileName) {
        if (resourceManager instanceof ClasspathResourceManager) {
            OutputStream outputStream = null;
            try {
                InputStream inputStream = ((ClasspathResourceManager) resourceManager).getInputStream(template);
                outputStream = new FileOutputStream(destFileName);
                leftShift(outputStream, inputStream);
            } catch (IOException e) {
                System.out.println("Resource " + template + " skipped due to: " + e.getMessage());
            } catch (NullPointerException e) {
                System.out.println("Resource " + template + " not found so skipped");
            } finally {
                closeQuietly(outputStream);
            }
        }
    }

    /* Methods from DefaultGroovyMethods */

    /**
     * Pipe an InputStream into an OutputStream for efficient stream copying.
     *
     * @param self stream on which to write
     * @param in   stream to read from
     * @return the outputstream itself
     * @throws IOException if an I/O error occurs.
     * @since 1.0
     */
    public static OutputStream leftShift(OutputStream self, InputStream in) throws IOException {
        byte[] buf = new byte[1024];
        while (true) {
            int count = in.read(buf, 0, buf.length);
            if (count == -1) break;
            if (count == 0) {
                Thread.yield();
                continue;
            }
            self.write(buf, 0, count);
        }
        self.flush();
        return self;
    }

    /**
     * Close the Closeable. Ignore any problems that might occur.
     *
     * @param c the thing to close
     */
    public static void closeQuietly(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                /* ignore */
            }
        }
    }
}

