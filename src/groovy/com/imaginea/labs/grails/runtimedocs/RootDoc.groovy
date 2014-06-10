package com.imaginea.labs.grails.runtimedocs

import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import org.codehaus.groovy.grails.plugins.web.filters.FiltersConfigArtefactHandler
import org.grails.plugin.resources.artefacts.ResourceMapperArtefactHandler
import org.grails.plugin.resources.artefacts.ResourcesArtefactHandler

/**
 * Builds Data Structures needed for documenting both compile-time and runtime properties of all Grails Artifacts  
 *
 * @author Srinath Anantha
 */
class RootDoc {
    Set<ClassDoc> allClasses = new TreeSet<ClassDoc>();
    Set<ClassDoc> allConfigClasses = new TreeSet<ClassDoc>();
    Set<ClassDoc> allCommands = new TreeSet<ClassDoc>();
    Set<ClassDoc> allControllers = new TreeSet<ClassDoc>();
    Set<ClassDoc> allDomains = new TreeSet<ClassDoc>();
    Set<PackageDoc> allPackages = new TreeSet<PackageDoc>();
    Set<ClassDoc> allServices = new TreeSet<ClassDoc>();
    Set<ClassDoc> allTagLibs = new TreeSet<ClassDoc>();

    public static final DEFAULT_PACKAGE = "default";

    public static Map<String, ClassDoc> allClassesMap = new HashMap<String, ClassDoc>();

    public RootDoc(DefaultGrailsApplication grailsApp) {
        buildRootDoc(grailsApp);
    }

    void buildRootDoc(DefaultGrailsApplication grailsApp) {
        grailsApp.allClasses.each { def clazz ->
            if (!allClassesMap.containsKey(clazz.getName())) {
                PackageDoc packageDoc = allPackages.find {
                    it.getName() == (clazz.getPackage() ? clazz.getPackage().getName() : DEFAULT_PACKAGE)
                }
                packageDoc = packageDoc ?: new PackageDoc(clazz.getPackage()?.getName());
                ClassDoc classDoc = new ClassDoc(clazz, clazz.getMetaClass());
                allClasses.add(classDoc);
                allClassesMap.put(clazz.getName(), classDoc);
                packageDoc.addClassDoc(classDoc);
                allPackages.add(packageDoc);
                if (grailsApp.isControllerClass(clazz))
                    allControllers.add(classDoc);
                else if (grailsApp.isServiceClass(clazz))
                    allServices.add(classDoc);
                else if (grailsApp.isDomainClass(clazz))
                    allDomains.add(classDoc);
                else if (grailsApp.isTagLibClass(clazz))
                    allTagLibs.add(classDoc);
                else if (clazz.getSimpleName().endsWith("Command"))
                    allCommands.add(classDoc);
                else if (grailsApp.isBootstrapClass(clazz) || grailsApp.isCodecClass(clazz)
                        || grailsApp.isUrlMappingsClass(clazz)
                        || clazz.getSimpleName().endsWith("Config")
                        || grailsApp.isArtefactOfType(FiltersConfigArtefactHandler.TYPE, clazz)
                        || grailsApp.isArtefactOfType(ResourcesArtefactHandler.TYPE, clazz)
                        || grailsApp.isArtefactOfType(ResourceMapperArtefactHandler.TYPE, clazz))
                    allConfigClasses.add(classDoc);
            }
        }
    }

    public TreeSet<ClassDoc> getAllClasses() {
        return allClasses;
    }

    public TreeSet<ClassDoc> getAllConfigClasses() {
        return allConfigClasses;
    }

    public TreeSet<ClassDoc> getAllControllers() {
        return allControllers;
    }

    public TreeSet<ClassDoc> getAllServices() {
        return allServices;
    }

    public TreeSet<ClassDoc> getAllDomains() {
        return allDomains;
    }

    public TreeSet<ClassDoc> getAllCommands() {
        return allCommands;
    }

    public TreeSet<ClassDoc> getAllTagLibs() {
        return allTagLibs;
    }

    public static ClassDoc getClassDoc(Class clazz) {
        return allClassesMap.get(clazz.getName());
    }

    // TODO
    public String getDescription() {
        return "";
    }

    public TreeSet<PackageDoc> getPackages() {
        return allPackages;
    }

    // TODO
    public String getSummary() {
        return "";
    }
}
