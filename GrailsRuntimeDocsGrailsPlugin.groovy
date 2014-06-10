class GrailsRuntimeDocsGrailsPlugin {
    // the plugin version
    def version = "1.0.0"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.0.0 > *"
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
The Runtime Docs plugin generates both Java and Groovy documentation for a Grails Project. The groovy documentation is generated from run-time thereby facilitating to display even the dynamic methods and properties added to the grails artifacts from various sources. The HTML docs follows Java 7 API documentation template and also adds "Dynamic Method Summary & Dynamic Method Detail" providing the source information of the dynamic methods.
The plugin also helps to easily navigate through the grails artifacts by further categorizing the classes into Commands, Configs, Controllers, Domains, Services and Tag Libraries.
'''
    def license = "APACHE"

    def issueManagement = [ system: "GIT", url: "https://github.com/srinathanantha/grails-runtime-docs/issues" ]

    def scm = [ url: "https://github.com/srinathanantha/grails-runtime-docs" ]
    
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
