[![Build Status](https://travis-ci.org/ida-mediafoundry/jetpack-template-type-sync.svg?branch=master)](https://travis-ci.org/ida-mediafoundry/jetpack-template-type-sync) [![codecov](https://codecov.io/gh/ida-mediafoundry/jetpack-template-type-sync/branch/master/graph/badge.svg)](https://codecov.io/gh/ida-mediafoundry/jetpack-template-type-sync)
# Jetpack - Template-Type Sync
(powered by iDA Mediafoundry)

This Jetpack tool allows you to copy template-type policies to existing Templates.

Navigate to http://localhost:4502/libs/granite/configurations/content/view.html/conf
And Select "Sync Template-types with Templates"

## Description

Editable Templates could be managed by the authoring team.
Template-Types are managed by the development team.
Developers can assign default policies to components on these template-types, if new templates are created
from these template-types, the policies are immediately configured on the new template.

This tool will sync the template-type policy config to already existing templates.


## Modules

The main parts of this projects are:

* core: Java bundle containing all core functionality like OSGI services, Sling Models and WCMCommand.
* ui.apps: contains the /apps part containing the html, js, css and .content.xml files.


## How to build

To build all the modules run in the project root directory the following command with Maven 3:

    mvn clean install

If you have a running AEM instance you can build and package the whole project and deploy into AEM with  

    mvn clean install -PautoInstallPackage
    
Or to deploy it to a publish instance, run

    mvn clean install -PautoInstallPackagePublish
    
Or alternatively

    mvn clean install -PautoInstallPackage -Daem.port=4503

Or to deploy only the bundle to the author, run

    mvn clean install -PautoInstallBundle


## Testing

There are three levels of testing contained in the project:

unit test in core: this show-cases classic unit testing of the code contained in the bundle. To test, execute:

    mvn clean test
