/*
 * Copyright (c) Interactive Information R & D (I2RD) LLC.
 * All Rights Reserved.
 *
 * This software is confidential and proprietary information of
 * I2RD LLC ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered
 * into with I2RD.
 */

package com.proteusframework.build

import groovy.swing.SwingBuilder
import org.gradle.api.Project
import org.slf4j.LoggerFactory

import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.border.TitledBorder
import java.awt.*
import java.util.concurrent.CompletableFuture

import static groovy.io.FileVisitResult.CONTINUE
import static groovy.io.FileVisitResult.SKIP_SUBTREE
import static javax.swing.JFileChooser.DIRECTORIES_ONLY

/**
 * UI for creating project.
 * @author Russ Tennant (russ@i2rd.com)
 */
class CreateProjectUI
{
    def logger = LoggerFactory.getLogger("build")
    def swing = new SwingBuilder()
    def model = new ProjectModel()
    CompletableFuture<ProjectModel> _future = new CompletableFuture<>()
    Project project
    CreateProjectUI(def project)
    {
        this.project = project
    }

    def start()
    {
        URL resource = getClass().getResource('proteus-logo.png')
        assert resource != null
        def icon = new ImageIcon(resource)
        def count = 0
        def upIn = this.&updateInstructions
        swing.edt {
            frame(id: 'ui', title: 'Create New Project', defaultCloseOperation: JFrame.DISPOSE_ON_CLOSE, pack: true,
                visible: true, iconImage: icon.getImage(), location: [400, 50]) {
                vbox(border: new EmptyBorder(10, 10, 10, 10)) {
                    hbox {
                        label('Artifact Group: ')
                        widget(new PlaceholderTextField(), id: 'app_group', placeholder: 'net.venturetech', columns: 20)
                    }
                    hbox {
                        label('Artifact/Project Name: ')
                        widget(new PlaceholderTextField(), id: 'app_name', placeholder: 'labs',
                            columns: 20)
                    }
                    hbox {
                        label('Copy Demo Code: ')
                        checkBox(id: 'copy_demo')
                        hglue()
                    }
                    hbox {
                        label('Destination Directory: ')
                        textField(id: 'directory', disabledTextColor: Color.BLACK, enabled: false)
                        button(text: '...', actionPerformed: {
                            fileChooser(id: 'destination_directory', fileSelectionMode: DIRECTORIES_ONLY,
                                multiSelectionEnabled: false,
                                selectedFile: model.destinationDirectory,
                                acceptAllFileFilterUsed: false, actionPerformed: {ev ->
                                def fc = ev.source as JFileChooser
                                model.destinationDirectory = fc.selectedFile
                                if (model.destinationDirectory != null)
                                    swing.directory.text = model.destinationDirectory.getName()
                                }).showDialog(swing.ui, 'Select')
                        })

                    }
                    hbox(border: new TitledBorder(new EmptyBorder(20, 10, 20, 10), 'Next Steps')) {
                        textArea(id:'instructions', enabled: false, disabledTextColor: Color.BLACK)
                        updateInstructions()
                    }
                    hbox(border: new EmptyBorder(10, 10, 10, 10)) {
                        button(
                            id: 'create_project_btn',
                            text: 'Create Project',
                            actionPerformed: {ev ->
                                if (validate() && createProject(ev)) {
                                    _future.complete(model)
                                    swing.create_project_btn.setVisible(false)
                                    swing.close_btn.setText('Close')
                                }
                            }
                        )
                        button(
                            id: 'close_btn',
                            text: 'Cancel',
                            actionPerformed: {
                                _future.complete(model)
                                swing.ui.dispose()
                            }
                        )
                    }
                }
                bean(model, appGroup: bind {app_group.text})
                bean(model, appName: bind {app_name.text})
                bean(model, copyDemo: bind {copy_demo.selected})
            }
        }
        return _future
    }

    def updateInstructions()
    {
        swing.instructions.text = '''Setup remote git repo.
Open in Intellij IDEA.
Synchronize gradle settings.
Install a database snapshot.
Update the db.url & db.username in default.properties if needed.
Run the "App with LTW".
To run the demo code, you will need to update your ProjectConfig.'''
    }

    def validate()
    {
        def errors = []
        if (!model.appGroup)
            errors.add('Artifact Group Is Required')
        else if(!model.appGroup.matches('([a-z_]{1}[a-z0-9_]*(\\.[a-z_]{1}[a-z0-9_]*)*)'))
            errors.add('Artifact Group Must Be A Valid Package Name')
        else if(!Character.isLetter(model.appGroup.charAt(model.appGroup.length()-1)))
            errors.add('Artifact Group Must End With A Letter')
        if (!model.appName)
            errors.add('Artifact/Project Name Is Required')
        else if(!Character.isLetter(model.appName.charAt(0)))
            errors.add('Artifact/Project Name Must Start With A Letter')
        else if(!Character.isLetter(model.appName.charAt(model.appName.length()-1)))
            errors.add('Artifact/Project Name Must End With A Letter')
        if (!model.destinationDirectory)
            errors.add('Destination Directory Is Required')
        else if (!model.destinationDirectory.canWrite())
            errors.add('Destination Directory Is Not Writeable')
        else if (new File(model.destinationDirectory, model.appName).exists())
            errors.add('Project Already Exists')
        if (errors)
        {
            swing.optionPane().showMessageDialog(swing.ui, errors.join('\n'),
                "Required Arguments", JOptionPane.ERROR_MESSAGE)
            return false;
        }
        return true
    }

    def createProject(ev)
    {
        def packageName = model.appGroup + '.' + (model.appName.toLowerCase().replaceAll('[^a-z0-9_]', '_'))
        def slash = File.separator
        def packageDir = packageName.replace('.', slash)

        println "appGroup = ${model.appGroup}"
        println "appName = ${model.appName}"
        println "copyDemo = ${model.copyDemo}"
        println "destinationDirectory = ${model.destinationDirectory}"
        println "sourceProjectDir = ${project.projectDir}"
        println "package name = ${packageName}"


        File baseDir = new File(model.destinationDirectory, model.appName);
        if(!baseDir.exists() && !baseDir.mkdirs()){
            swing.optionPane().showMessageDialog(swing.ui, 'Unable to create directory: ' + baseDir,
                "Unable To Create Project", JOptionPane.ERROR_MESSAGE)
        }
        try
        {
            project.copy() {
                into baseDir
                from(project.projectDir) {
                    include 'buildSrc/src/**/*'
                    include 'buildSrc/build.gradle'
                    include 'buildSrc/buildSrc.iml'
                    include '.idea/**/*'
                    include 'config/**/*'
                    include 'docs/**/*'
                    include 'gradle/**/*'
                    include 'scripts/**/*'
                    include 'src/main/resources/META-INF/**/*'
                    include 'src/main/resources/intellij/**/*'
                    include 'src/main/webapp/**/*'
                    if (model.copyDemo)
                    {
                        include 'src/demo/**/*'
                    }
                    include 'example-app.iml'
                    include 'gradle*'
                    include 'build.gradle'
                    include 'settings.gradle'

                    exclude '.idea/workspace.xml'
                    exclude '.idea/tasks.xml'
                    exclude '.idea/artifacts/**'
                }
            }
            new File(baseDir, '.gitignore').text = '''
.gradle/
.apt_generated/
.apt_generated_tests/
/build/
/buildSrc/build
/compiletime-aspects/
/runtime-aspects/

.idea/libraries/
.idea/workspace.xml
.idea/tasks.xml
atlassian-ide-plugin.xml

spring-shell.log
'''
            new File(baseDir, '.idea/vcs.xml').text = '''<?xml version="1.0" encoding="UTF-8"?>
<project version="4">
  <component name="VcsDirectoryMappings">
    <mapping directory="$PROJECT_DIR$" vcs="Git" />
  </component>
</project>
'''
            def mainJava = new File(baseDir, "src${slash}main${slash}java${slash}${packageDir}")
            def mainRes  = new File(baseDir, "src${slash}main${slash}resources${slash}${packageDir}")
            mainJava.mkdirs()
            mainRes.mkdirs()
            project.copy() {
                into mainJava
                from(new File(project.projectDir, 'src/main/java/com/example/app')) {
                    include '**/*'
                }
            }
            project.copy() {
                into mainRes
                from(new File(project.projectDir, 'src/main/resources/com/example/app')) {
                    include '**/*'
                }
            }

            def skipDirs = ['.git', '.apt_generated', '.apt_generated_tests', 'demo'] as Set
            def skipFiles = ['CreateProjectUI.groovy'] as Set
            baseDir.traverse(
                [preDir    : {if (skipDirs.contains(it.name)) return SKIP_SUBTREE}], {f ->
                if(f.isFile() && !skipFiles.contains(f.name)) {
                    def content = f.getText('UTF-8')
                    def updatedContent = content.replaceAll('com[.]example[.]app', packageName)
                        .replaceAll('com/example/app', packageDir)
                        .replaceAll('example-app', model.appName)
                        .replaceAll('com.example', model.appGroup)
                        .replaceAll('example_app', model.appName.replace('.', '_'))
                    if (updatedContent != content)
                    {
                        println('Updating ' + f)
                        f.setText(updatedContent, 'UTF-8')
                    }
                    if(f.name == 'example-app.iml'){
                        f.renameTo("${baseDir}${slash}${model.appName}.iml")
                    }
                }

                CONTINUE
                 })

            def gradleScript = new File(baseDir, 'gradlew').absolutePath
            def command = [gradleScript, '-Dorg.gradle.daemon=false']
            def envp = System.getenv().collect({k, v -> "${k}=${v}"})
            def process = command.execute(envp, baseDir)
            process.consumeProcessOutput(System.out, System.err)
            process.waitFor()
            if(new File('/usr/bin/git').canExecute()){
                command = ['/usr/bin/git', 'init']
                process = command.execute(envp, baseDir)
                process.consumeProcessOutput(System.out, System.err)
                process.waitFor()

                command = ['/usr/bin/git', 'add', '-A']
                process = command.execute(envp, baseDir)
                process.consumeProcessOutput(System.out, System.err)
                process.waitFor()

                command = ['/usr/bin/git', 'commit', '-m', "Created project, ${model.appName}, from example-app", '.']
                process = command.execute(envp, baseDir)
                process.consumeProcessOutput(System.out, System.err)
                process.waitFor()
            }
        }
        catch(e)
        {
            e.printStackTrace()
        }
        true
    }

}
