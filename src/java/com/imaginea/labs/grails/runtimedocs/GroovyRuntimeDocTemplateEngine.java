package com.imaginea.labs.grails.runtimedocs;

import groovy.text.GStringTemplateEngine;
import groovy.text.Template;
import groovy.text.TemplateEngine;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.tools.groovydoc.ClasspathResourceManager;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
        String templateWithBindingApplied = "";
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
                DefaultGroovyMethods.leftShift(outputStream, inputStream);
            } catch (IOException e) {
                System.err.println("Resource " + template + " skipped due to: " + e.getMessage());
            } catch (NullPointerException e) {
                System.err.println("Resource " + template + " not found so skipped");
            } finally {
                DefaultGroovyMethods.closeQuietly(outputStream);
            }
        }
    }
}

