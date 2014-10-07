## Grails Runtime Docs

An API Documentation plug-in for Grails Projects that also displays the dynamic methods and properties added to the grails artifacts along with their source information.

The groovy documentation is generated from runtime which facilitates to capture the dynamic methods and properties added to the grails artifacts from various sources. The HTML docs follows Java 7 API documentation template and adds "Dynamic Method Summary & Dynamic Method Detail" providing the source information of the dynamic methods.
The plugin also helps to easily navigate through the grails artifacts by further categorizing the classes into Commands, Configs, Controllers, Domains, Services and Tag Libraries.

### Usage

Generate the documentation for your Grails Project by simply executing the following command:

> grails runtime-docs

Java and Groovy documentation is then generated in target/docs directory of your project.


### Java Documentation Scope
The default scope set for Java documentation is Protected, which means documentation will be generated for all public/protected classes.

Note: To change the default scope, modify the 'access' property in 'RuntimeDocs.groovy' under 'javadoc' target to one of these available modes : public, protected, package, or private.


### Groovy Documentation Scope
The default properties for Groovy documentation allows the plug-in to generate documentation for all classes including package/private classes.

Note: To enable/disable generation of documentation for a particular scope, set the values for the available publicScope, protectedScope, packageScope, and privateScope to true or false in 'RuntimeDocs.groovy' under 'groovydoc' target.
