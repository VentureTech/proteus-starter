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

    CreateProjectUI()
    {
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
        if (!model.appName)
            errors.add('Artifact/Project Name Is Required')
        if (!model.destinationDirectory)
            errors.add('Destination Directory Is Required')
        else if (!model.destinationDirectory.canWrite())
            errors.add('Destination Directory Is Not Writeable')
        if (errors)
        {
            swing.optionPane().showMessageDialog(swing.ui, errors.join('\n'),
                "Required Arguments", JOptionPane.INFORMATION_MESSAGE)
            return false;
        }
        return true
    }

    def createProject(ev)
    {
        def file = new File('/tmp/test.txt')
        def pw = file.newPrintWriter()
        pw.println model.appGroup
        pw.println model.appName
        pw.println model.copyDemo
        pw.println model.destinationDirectory
        pw.close()
        def dialog = new JDialog(swing.ui as JFrame, 'Creating Project', true)
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


        def skipDirs = ['.git', '.apt_generated', '.apt_generated_tests', 'demo'] as Set
        def files = []
        projectDir.traverse(
            [nameFilter: ~/.*\.java/,
             preDir    : {if (skipDirs.contains(it.name)) return SKIP_SUBTREE}], {f ->

            if (f.filterLine({it ==~ /.*com.example.app.*/}) as String)
                files.add(f)

            CONTINUE
             })

        println files.join('\n')

    }
}
