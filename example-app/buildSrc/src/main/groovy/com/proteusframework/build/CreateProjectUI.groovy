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

import javax.swing.*
import javax.swing.border.EmptyBorder
import java.awt.*

import static groovy.io.FileVisitResult.CONTINUE
import static groovy.io.FileVisitResult.SKIP_SUBTREE
import static javax.swing.JFileChooser.DIRECTORIES_ONLY

/**
 * UI for creating project.
 * @author Russ Tennant (russ@i2rd.com)
 */
class CreateProjectUI
{
    def swing = new SwingBuilder()
    def model = new ProjectModel()
    Project project
    CreateProjectUI(def project)
    {
        this.project = project
        URL resource = getClass().getResource('proteus-logo.png')
        assert resource != null
        def icon = new ImageIcon(resource)
        def count = 0
        swing.edt {
            frame(id: 'ui', title: 'Create New Project', defaultCloseOperation: JFrame.EXIT_ON_CLOSE, pack: true, visible:
                true,
                iconImage: icon.getImage(), location: [400, 50]) {
                vbox(border: new EmptyBorder(10, 10, 10, 10)) {
                    hbox {
                        label('Artifact Group: ')
                        widget(new PlaceholderTextField(), id: 'app_group', placeholder: 'net.venturetech',
                            columns: 20)
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
                    hbox(border: new EmptyBorder(10, 10, 10, 10)) {
                        button(
                            text: 'Create Project',
                            actionPerformed: {ev ->
                                if (validate() && createProject(ev))
                                    System.exit(0)
                            }
                        )
                        button(
                            text: 'Cancel',
                            actionPerformed: {
                                System.exit(0)
                            }
                        )
                    }
                }
                bean(model, appGroup: bind {app_group.text})
                bean(model, appName: bind {app_name.text})
                bean(model, copyDemo: bind {copy_demo.selected})
            }
        }
    }

    def validate()
    {
        def errors = []
        if (!model.appGroup)
            errors.add('Artifact Group Is Required')
        else if(!model.appGroup.matches('([a-z_]{1}[a-z0-9_]*(\\.[a-z_]{1}[a-z0-9_]*)*)'))
            errors.add('Artifact Group Must Be A Valid Package Name')
        else if(!Character.isLetter(model.appName.charAt(model.appName.length()-1)))
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

        def file = new File('/tmp/test.txt')
        def pw = file.newPrintWriter()
        pw.println model.appGroup
        pw.println model.appName
        pw.println model.copyDemo
        pw.println project.projectDir
        pw.println model.destinationDirectory
        pw.flush()
        def dialog = new JDialog(swing.ui as JFrame, 'Creating Project', true)
        swing.doLater {
            dialog.setSize(600, 100);
            JProgressBar pb
            def panel = swing.panel(border: new EmptyBorder(20, 20, 20, 20)) {
                vbox {
                    pb = progressBar(id: 'progress_bar', minimum: 0, maximum: 100, value: 0, string: 'Creating....',
                        stringPainted: true, indeterminate: true)
                }
            }
            dialog.getContentPane().add(panel)
            dialog.setModal(true)
            dialog.pack()
            dialog.show()
        }
        File baseDir = new File(model.destinationDirectory, model.appName);
        if(!baseDir.exists() && !baseDir.mkdirs()){
            dialog.hide()
            dialog.dispose()
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
                    include 'src/main/webapp/**/*'
                    if (model.copyDemo)
                    {
                        include 'src/demo/**/*'
                    }
                    include '.gitignore'
                    include 'example-app.iml'
                    include 'gradle*'
                    include 'settings.gradle'
                }
            }
            def mainJava = new File(baseDir, "src${slash}main${slash}java${slash}${packageDir}${slash}config")
            def mainRes  = new File(baseDir, "src${slash}main${slash}resources${slash}${packageDir}${slash}config")
            project.copy() {
                into mainJava
                from(project.projectDir) {
                    include 'src/main/java/com/example/app/config/**/*'
                }
            }
            project.copy() {
                into mainRes
                from(project.projectDir) {
                    include 'src/main/resources/com/example/app/config/**/*'
                }
            }

            def skipDirs = ['.git', '.apt_generated', '.apt_generated_tests', 'demo'] as Set
            baseDir.traverse(
                [nameFilter: ~/.*\.java/,
                 preDir    : {if (skipDirs.contains(it.name)) return SKIP_SUBTREE}], {f ->

                def content = f.getText('UTF-8')
                def updatedContent = content.replaceAll('com[.]example[.]app', packageName)
                    .replaceAll('com/example/app', packageDir)
                if(updatedContent != content)
                    f.setText(updatedContent, 'UTF-8')

                CONTINUE
                 })
        }
        catch(e)
        {
            e.printStackTrace(pw)
        }
        pw.close()
        dialog.dispose()
        true
    }

    def foo()
    {


        println()
        System.in.withReader {
            print "Artifact Group (net.venturetech): "
            def appGroup = it.readLine()
            print "Artifact/Project Name: "
            def appName = it.readLine()

            println "You entered ${appGroup}"
        }



        def sharedPanel = {
            swing.panel() {
                label("Shared Panel")
            }
        }




        println files.join('\n')

    }
}
