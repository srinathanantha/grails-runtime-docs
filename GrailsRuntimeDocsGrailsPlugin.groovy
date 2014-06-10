class GrailsRuntimeDocsGrailsPlugin {
    // the plugin version
    def version = "1.0.0"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.x > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    def environments = ['development', 'test']

    def scopes = [excludes:'war']

    def author = "Srinath Anantha"
    def authorEmail = "srinath.anantha@gmail.com"
    def title = "Grails Runtime Docs"
    def description = '''\\
Generates Java and Groovy documentation for a Grails Application from runtime including the dynamically added methods and properties.
Helps to easily navigate through the Grails artifacts by categorising all the classes into Controllers, Services, Domains, Commands
and Tag Libraries.
'''
    def license = "APACHE"

    def issueManagement = [ system: "GIT", url: "https://github.com/srinathanantha/grails-runtime-docs/issues" ]

    def scm = [ url: "http://svn.grails-plugins.codehaus.org/browse/grails-plugins/" ]
    
    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/grails-runtime-docs"

    def doWithWebDescriptor = { xml ->
    }

    def doWithSpring = {
    }

    def doWithDynamicMethods = { ctx ->
    }

    def doWithApplicationContext = { applicationContext ->
    }

    def onChange = { event ->
    }

    def onConfigChange = { event ->
    }
}
