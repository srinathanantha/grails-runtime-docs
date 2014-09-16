import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import org.codehaus.groovy.tools.groovydoc.ClasspathResourceManager

/**
 * Gant Script that generates Java and Groovy documentation for a Grails Application
 * from runtime including the dynamically added methods and properties.
 *
 * @author Srinath Anantha
 */

includeTargets << grailsScript("_GrailsBootstrap")
includeTargets << grailsScript("_GrailsRun")

javaDocDir = "${grailsSettings.docsOutputDir}\\api"
groovyDocDir = "${grailsSettings.docsOutputDir}\\gapi"
docEncoding = "UTF-8"
docSourceLevel = "1.7"
links = ['http://docs.oracle.com/javase/7/docs/api/']

target(default: "Generates Java and Groovy documentation for a Grails Application from runtime including the dynamically added methods and properties.") {
    depends(clean, packageApp, classpath, compile, javadoc, groovydoc, createIndex)
}

target(setupDoc: "Sets up the doc directories") {
    ant.mkdir(dir: grailsSettings.docsOutputDir)
    ant.mkdir(dir: javaDocDir)
    ant.mkdir(dir: groovyDocDir)
}

target(javadoc: "Produces Java Documentation") {
    depends(parseArguments, setupDoc)

    File javaDir = new File("${grailsSettings.sourceDir}/java")
    if (javaDir.listFiles().find { !it.name.startsWith(".")}) {
        try {
            ant.javadoc(access: "protected",
                destdir: javaDocDir,
                encoding: docEncoding,
                classpathref: "grails.compile.classpath",
                use: "yes",
                windowtitle: grailsAppName,
                docencoding: docEncoding,
                charset: docEncoding,
                source: docSourceLevel,
                useexternalfile: "yes",
                breakiterator: "true",
                linksource: "yes",
                maxmemory: "128m",
                failonerror: false,
                sourcepath: javaDir.absolutePath) {
                // Generate javadoc for *.java files in grails-app sub-directories
                File grailsAppDir = new File("${grailsSettings.baseDir}/grails-app");
                File[] dirList = grailsAppDir.listFiles();
                for (int i = 0; i < dirList.length; i++) {
                    if (dirList[i].isDirectory() & !dirList[i].name.startsWith(".")) {
                        sourcepath(location: dirList[i])
                    }
                }
                for (i in links) {
                    link(href: i)
                }
            }
        }
        catch (Exception e) {
            event("StatusError", ["Error generating javadoc: ${e.message}"])
        }
    }
}


target(groovydoc: "Produces Groovy Documentation with runtime properties") {
    depends(setupDoc, loadApp, runApp)

    Properties properties = new Properties();
    properties.setProperty("windowTitle", "$grailsAppName API");
    properties.setProperty("docTitle", "$grailsAppName <br> API Specification");
    properties.setProperty("header", "$grailsAppName API");
    properties.setProperty("footer", "$grailsAppName API");
    properties.setProperty("publicScope", "true");
    properties.setProperty("protectedScope", "true");
    properties.setProperty("packageScope", "true");
    properties.setProperty("privateScope", "true");
    properties.setProperty("author", "true");
    properties.setProperty("processScripts", "true");
    properties.setProperty("includeMainForScripts", "true");
    properties.setProperty("overviewFile", "");

    String TEMPLATE_BASEDIR = "com/imaginea/labs/grails/runtimedocs/templates/";
    String[] DEFAULT_DOC_TEMPLATES = [ // top level templates
        TEMPLATE_BASEDIR + "topLevel/index.html",
        TEMPLATE_BASEDIR + "topLevel/index-all.html",
        TEMPLATE_BASEDIR + "topLevel/overview-frame.html",
        TEMPLATE_BASEDIR + "topLevel/overview-summary.html",
        TEMPLATE_BASEDIR + "topLevel/all-classes-frame.html",
        TEMPLATE_BASEDIR + "topLevel/all-configs-frame.html",
        TEMPLATE_BASEDIR + "topLevel/all-controllers-frame.html",
        TEMPLATE_BASEDIR + "topLevel/all-services-frame.html",
        TEMPLATE_BASEDIR + "topLevel/all-domains-frame.html",
        TEMPLATE_BASEDIR + "topLevel/all-commands-frame.html",
        TEMPLATE_BASEDIR + "topLevel/all-tag-libraries-frame.html",
        TEMPLATE_BASEDIR + "topLevel/help-doc.html",
        TEMPLATE_BASEDIR + "topLevel/stylesheet.css",
        TEMPLATE_BASEDIR + "topLevel/resources/inherit.gif",
        TEMPLATE_BASEDIR + "topLevel/resources/groovy.ico",
        TEMPLATE_BASEDIR + "topLevel/resources/background.gif",
        TEMPLATE_BASEDIR + "topLevel/resources/tab.gif",
        TEMPLATE_BASEDIR + "topLevel/resources/titlebar.gif",
        TEMPLATE_BASEDIR + "topLevel/resources/titlebar_end.gif"
    ];
    String[] DEFAULT_PACKAGE_TEMPLATES = [ // package level templates
        TEMPLATE_BASEDIR + "packageLevel/package-frame.html",
        TEMPLATE_BASEDIR + "packageLevel/package-summary.html"];

    String[] DEFAULT_CLASS_TEMPLATES = [// class level templates
        TEMPLATE_BASEDIR + "classLevel/runtimeClassDoc.html"];

    Class[] classArgs = [ClasspathResourceManager.class, String[].class, String[].class, String[].class, Properties.class];
    Object[] objectArgs = [new ClasspathResourceManager(),
        DEFAULT_DOC_TEMPLATES,
        DEFAULT_PACKAGE_TEMPLATES,
        DEFAULT_CLASS_TEMPLATES,
        properties];

    def clazz = classLoader.loadClass("com.imaginea.labs.grails.runtimedocs.GroovyRuntimeDocTemplateEngine", true);
    def templateEngine = clazz.getConstructor(classArgs).newInstance(objectArgs);
    if (templateEngine != null) {
        classArgs = [templateEngine.getClass()];
        objectArgs = [templateEngine];
        clazz = classLoader.loadClass("com.imaginea.labs.grails.runtimedocs.GroovyRuntimeDocWriter", true);
        def writer = clazz.getConstructor(classArgs).newInstance(objectArgs);

        classArgs = [DefaultGrailsApplication.class];
        objectArgs = [grailsApp];
        clazz = classLoader.loadClass("com.imaginea.labs.grails.runtimedocs.RootDoc", true);
        def rootDoc = clazz.getConstructor(classArgs).newInstance(objectArgs);

        writer.writeRoot(rootDoc, groovyDocDir);
        writer.writePackages(rootDoc, groovyDocDir);
        writer.writeClasses(rootDoc, groovyDocDir);
    } else {
        throw new UnsupportedOperationException("No template engine was found");
    }
}

target(createIndex: "Produces an index.html page in the root directory") {
    new File("${grailsSettings.docsOutputDir}/all-docs.html").withWriter { writer ->
        writer.write """
<html>
	<head>
		<title>$grailsAppName Documentation</title>
	</head>

	<body>
		<a href="api/index.html">Java API docs</a><br />
		<a href="gapi/index.html">Groovy API docs</a><br />
	</body>
</html>
"""
    }
}
