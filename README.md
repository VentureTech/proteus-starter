Proteus Examples
================

Web Development
---------------

The web development (*/proteus-examples/webdev*) project is an Intellij IDEA or WebStorm project with standard styles and JavaScript for proteus applications. This is used in combination with the projects created from the Example App described below.



Example App
-----------

The example app (*/proteus-examples/example-app*) is a gradle project with dependencies on proteus framework that you can download [here](https://github.com/VentureTech/proteus-examples/releases/). The project is preconfigured for use with Intellij IDEA version 2016+. Standard settings are provided for Intellij IDEA.

<strong>Use</strong>

* Run ./gradlew createProject and follow the instructions

#### OR

* Download and unpack the project.
* Open the project in Intellij. 
* Rename the project to match your needs - both project name and directory name unless the directory will become the root of the git repo.
* Update gradle.properties (especially app_group and app_vesion). You'll want to copy the read access properties for the artifact repo to your /home/$USER/.gradle/gradle.properties* file.
* Decide on a good package name for your project and update the files so that they don't use com.example.
* Modify com.example.app.config.ProjectConfig to suit your project. 
* Add -Dspring.config=com.example.app.config.ProjectConfig to all of your launch configurations (update for actual class name)
* Import the standard settings (File->Import Settings) from */proteus-examples/example-app/config/idea/settings.jar*. Make sure the __i2rd__ Code Style scheme is selected in the IDEA settings dialog.
* Create a git repo where you host your code such as github and push the project up.
* Code!


For documentation on the Proteus Framework, head over to http://docs.proteusframework.com
