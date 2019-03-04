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
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier
import java.util.regex.Pattern

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
    def skipDirs = ['.git', '.apt_generated', '.apt_generated_tests'] as Set
    def skipFiles = ['CreateProjectUI.groovy'] as Set
    CompletableFuture<ProjectModel> _future = new CompletableFuture<>()
    Project project
    CreateProjectUI(def project)
    {
        this.project = project
    }

    def getBaseDir()
    {
        project.findProperty('project_base_dir')
    }

    def start()
    {
        def baseDir = getBaseDir()
        if(baseDir != null)
        {
            String baseDirS = String.valueOf(baseDir)
            File baseDirF = new File(baseDirS)
            model.destinationDirectory = baseDirF
        }
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
                        hglue()
                    }
                    hbox {
                        label('Destination Directory: ')
                        textField(id: 'directory', disabledTextColor: Color.BLACK, enabled: false,
                            text: model.destinationDirectory != null ? model.destinationDirectory.absolutePath : '')
                        button(text: '...', actionPerformed: {
                            fileChooser(id: 'destination_directory', fileSelectionMode: DIRECTORIES_ONLY,
                                multiSelectionEnabled: false,
                                selectedFile: model.destinationDirectory,
                                acceptAllFileFilterUsed: false,
                                actionPerformed: { ev ->
                                    def fc = ev.source as JFileChooser
                                    model.destinationDirectory = fc.selectedFile
                                    if (model.destinationDirectory != null)
                                        swing.directory.text = model.destinationDirectory.absolutePath
                                    }).showDialog(swing.ui, 'Select')
                        })

                    }
                    hbox {
                        label('Create Database? ')
                        checkBox(id: 'create_db_chk')
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
                                if (validate()) {
                                    swing.create_project_btn.setVisible(false)
                                    swing.close_btn.setVisible(false)
                                    swing.progress_bar.setVisible(true)
                                    def createProjectFuture = CompletableFuture.supplyAsync({createProject(ev)} as
                                        Supplier <Boolean>)
                                    createProjectFuture.whenComplete({Boolean result, Throwable exception ->
                                        swing.close_btn.setVisible(true)
                                        swing.progress_bar.setVisible(false)
                                        if(result != null && result)
                                        {
                                            SwingUtilities.invokeLater({
                                                _future.complete(model)
                                                swing.close_btn.setText('Close')
                                            })
                                        } else {
                                            SwingUtilities.invokeLater({
                                                swing.create_project_btn.setVisible(true)
                                            })
                                        }
                                    })

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
                        progressBar(
                            id: 'progress_bar',
                            minimum: 0,
                            maximum: 100,
                            visible: false,
                            indeterminate: true
                        )
                    }
                }
                bean(model, appGroup: bind {app_group.text})
                bean(model, appName: bind {app_name.text})
                bean(model, createDB: bind {create_db_chk.selected})
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
'''
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

    class SourceSetCopyInfo
    {
        static class ChildPackageInfo
        {
            File destination
            File origin
            String originPackageDir

            ChildPackageInfo(File destination, File origin, String originPackageDir)
            {
                this.destination = destination
                this.origin = origin
                this.originPackageDir = originPackageDir
            }
        }

        File destination
        File origin
        Set<ChildPackageInfo> childPackages

        SourceSetCopyInfo(
            File destination, File origin,
            ChildPackageInfo... childPackages
        )
        {
            this.destination = destination
            this.origin = origin
            if(childPackages != null && childPackages.length > 0)
                this.childPackages = childPackages as Set
            else
                this.childPackages = [] as Set
        }

        def copy()
        {
            destination.mkdirs()
            project.copy() {
                into destination
                from(origin) {
                    include '**/*'
                    childPackages.forEach({cp ->
                        exclude "${cp.originPackageDir}"
                    })
                    includeEmptyDirs = false
                }
            }
            childPackages.forEach({cp ->
                cp.destination.mkdirs()
                project.copy() {
                    into cp.destination
                    from(cp.origin) {
                        include '**/*'
                    }
                    includeEmptyDirs = false
                }
            })
        }
    }

    def copySourceSet(String sourceSetName, File baseDir, String packageDir)
    {
        def slash = File.separator
        new SourceSetCopyInfo(
            new File(baseDir, "src${slash}main${slash}${sourceSetName}"),
            new File(project.projectDir, "src${slash}main${slash}${sourceSetName}"),
            new SourceSetCopyInfo.ChildPackageInfo(
                new File(baseDir, "src${slash}main${slash}${sourceSetName}${slash}${packageDir}"),
                new File(project.projectDir, "src${slash}main${slash}${sourceSetName}${slash}com${slash}example${slash}app"),
                "com${slash}example${slash}app"
            )
        ).copy()
    }

    def createProject(ev)
    {
        def packageName = model.appGroup + '.' + (model.appName.toLowerCase().replaceAll('[^a-z0-9_]', '_'))
        def slash = File.separator
        def packageDir = packageName.replace('.', slash)
        def groupDir = model.appGroup.replace('.', slash)
        def packageSuppression = packageName.replace('.', '[\\/]')

        def dbName = "${model.appName}-dev" as String
        def dbUser = "${model.appName}-user" as String

        println "appGroup = ${model.appGroup}"
        println "appName = ${model.appName}"
        println "destinationDirectory = ${model.destinationDirectory}"
        println "sourceProjectDir = ${project.projectDir}"
        println "package name = ${packageName}"
        println "database name = ${dbName}"
        println "database user = ${dbUser}"


        File baseDir = new File(model.destinationDirectory, model.appName);
        if(!baseDir.exists() && !baseDir.mkdirs()){
            swing.optionPane().showMessageDialog(swing.ui, 'Unable to create directory: ' + baseDir,
                "Unable To Create Project", JOptionPane.ERROR_MESSAGE)
        }
        if(model.createDB) {
            def errors = createDatabase(dbName, dbUser, model)
            if(errors)
            {
                swing.optionPane().showMessageDialog(swing.ui, errors.join('\n'),
                    'Errors creating database: ', JOptionPane.ERROR_MESSAGE)
            }
        }
        try
        {
            project.copy() {
                into baseDir
                from(project.projectDir) {
                    include 'buildSrc/src/main/groovy/com/proteusframework/build/GitInfo.groovy'
                    include 'buildSrc/src/main/groovy/com/proteusframework/build/package-info.java'
                    include 'buildSrc/src/main/groovy/com/proteusframework/build/Property.groovy'
                    include 'buildSrc/src/main/groovy/com/proteusframework/build/Version.groovy'
                    include 'buildSrc/src/main/groovy/com/proteusframework/build/Deployment.groovy'
//                    include 'buildSrc/src/main/resources/**/*'
                    include 'buildSrc/build.gradle'
                    include 'buildSrc/gradle.properties'
                    include 'buildSrc/buildSrc.iml'
                    include '.idea/**/*'
                    include 'config/**/*'
                    include 'docs/**/*'
                    include 'gradle/**/*'
                    include 'scripts/**/*'
                    include 'src/libraries/**/*'
                    include 'starter-app.iml'
                    include '.idea/modules/starter-app.iml'
                    include 'gradle*'
                    include 'build.gradle'
                    include 'settings.gradle'
//                    include 'src/main/**/*'
//                    include 'src/main/webapp/**/*'
//                    include 'src/main/kotlin/**/*'
//                    include 'src/main/groovy/**/*'

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
.idea/artifacts/
.idea/workspace.xml
.idea/tasks.xml
.idea/modules.xml

atlassian-ide-plugin.xml

spring-shell.log
derby.log
'''
            new File(baseDir, '.idea/vcs.xml').text = '''<?xml version="1.0" encoding="UTF-8"?>
<project version="4">
  <component name="VcsDirectoryMappings">
    <mapping directory="$PROJECT_DIR$" vcs="Git" />
  </component>
</project>
'''
            copySourceSet("java", baseDir, packageDir)
            copySourceSet("groovy", baseDir, packageDir)
            copySourceSet("kotlin", baseDir, packageDir)
            copySourceSet("resources", baseDir, packageDir)
            copySourceSet("webapp", baseDir, packageDir)

            baseDir.traverse(
                [preDir    : {if (skipDirs.contains(it.name)) return SKIP_SUBTREE}], {f ->
                if(f.isFile() && !skipFiles.contains(f.name)) {
                    def content = f.getText('UTF-8')
                    def updatedContent = content
                    if(model.createDB) {
                        if(f.name == 'default.properties') {
                            updatedContent = content.replaceAll('starter-app-dev', dbName)
                                .replaceAll('starter-app-user', dbUser)
                        }
                    }
                    updatedContent = content.replaceAll('com[.]example[.]app', packageName)
                        .replaceAll('com/example/app', packageDir)
                        .replaceAll(Pattern.quote("com[\\/]example[\\/]app"), packageSuppression)
                        .replaceAll('starter-app', model.appName)
                        .replaceAll('com/example', groupDir)
                        .replaceAll('com.example', model.appGroup)
                        .replaceAll('starter_app', model.appName.replace('.', '-'))
                        .replace('task createProject(type: com.proteusframework.build.CreateProjectTask)', '')
                    if (updatedContent != content)
                    {
                        println('Updating ' + f)
                        f.setText(updatedContent, 'UTF-8')
                    }
                    if(f.name == 'starter-app.iml'){
                        f.renameTo(new File(f.getParentFile(), "${model.appName}.iml"))
                    }
                }

                CONTINUE
                })

            def dataSourcesLocal = new File(baseDir, ".idea/dataSources.local.xml")
            println('Updating ' + dataSourcesLocal)
            dataSourcesLocal.setText(
                dataSourcesLocal.getText('UTF-8')
                    .replaceAll('example_app', model.appName),
                'UTF-8'
            )

            new File(baseDir, ".idea/${slash}modules${slash}starter-app_main.iml")
                .renameTo(new File(baseDir, ".idea/${slash}modules${slash}${model.appName}_main.iml"))
            new File(baseDir, ".idea/${slash}modules${slash}starter-app_test.iml")
                .renameTo(new File(baseDir, ".idea/${slash}modules${slash}${model.appName}_test.iml"))
            new File(baseDir, ".idea/${slash}modules${slash}starter-app_libraries.iml")
                .renameTo(new File(baseDir, ".idea/${slash}modules${slash}${model.appName}_libraries.iml"))

            def osName = System.getProperty('os.name', '').toLowerCase()
            def gradleScript = new File(baseDir, getGradlewFileName(osName)).absolutePath
            def command = [gradleScript, '-Dorg.gradle.daemon=false']
            def envp = System.getenv().collect({k, v -> "${k}=${v}"})
            def process = command.execute(envp, baseDir)
            process.consumeProcessOutput(System.out, System.err)
            process.waitFor()
            def gitPath = getGitPath(osName)
            if(new File(gitPath).canExecute()){
                command = [gitPath, 'init']
                process = command.execute(envp, baseDir)
                process.consumeProcessOutput(System.out, System.err)
                process.waitFor()

                command = [gitPath, 'add', '-A']
                process = command.execute(envp, baseDir)
                process.consumeProcessOutput(System.out, System.err)
                process.waitFor()

                command = [gitPath, 'commit', '-m', "Created project, ${model.appName}, from starter-app", '.']
                process = command.execute(envp, baseDir)
                process.consumeProcessOutput(System.out, System.err)
                process.waitFor()
            }


            // Copy webdev project
            File webdevBaseDir = new File(model.destinationDirectory, "${model.appName}-webdev");
            project.copy({
                into webdevBaseDir
                from(new File(project.projectDir.parentFile, 'webdev')) {
                    include '**/*'
                    exclude 'node_modules'
                    exclude 'bower_components'
                    exclude '.idea/workspace.xml'
                    exclude '.idea/tasks.xml'
                    exclude '.idea/artifacts/**'
                }
            })
            new File(webdevBaseDir, '.gitignore').text = '''
# User-specific stuff:
.idea/workspace.xml
.idea/tasks.xml

# Crashlytics plugin (for Android Studio and IntelliJ)
com_crashlytics_export_strings.xml
crashlytics.properties
crashlytics-build.properties


build/
node_modules/
npm-debug.log
bower_components/

# Sass template
.sass-cache
*.css.map
'''
            webdevBaseDir.traverse(
                [preDir    : {if (skipDirs.contains(it.name)) return SKIP_SUBTREE}], {f ->
                if(f.isFile()) {
                    def content = f.getText('UTF-8')
                    def updatedContent = content
                        .replaceAll('starter-app-webdev', "${model.appName}-webdev")
                        .replaceAll('com.example', model.appGroup)
                        .replaceAll(Pattern.quote('<mapping directory="$PROJECT_DIR$/.." vcs="Git" />'),
                            '<mapping directory="\\$PROJECT_DIR\\$" vcs="Git" />')
                    if (updatedContent != content)
                    {
                        println('Updating ' + f)
                        f.setText(updatedContent, 'UTF-8')
                    }
                    if(f.name == 'starter-app-webdev.iml'){
                        f.renameTo(new File(f.getParentFile(), "${model.appName}-webdev.iml"))
                    }
                }

                CONTINUE
                })
            def webServersFile = new File(webdevBaseDir, ".idea/webServers.xml")
            println('Updating ' + webServersFile)
            webServersFile.setText(
                webServersFile.getText('UTF-8')
                    .replaceAll('starter-app', model.appName),
                'UTF-8'
            )
            if(new File(gitPath).canExecute()){
                command = [gitPath, 'init']
                process = command.execute(envp, webdevBaseDir)
                process.consumeProcessOutput(System.out, System.err)
                process.waitFor()

                command = [gitPath, 'add', '-A']
                process = command.execute(envp, webdevBaseDir)
                process.consumeProcessOutput(System.out, System.err)
                process.waitFor()

                command = [gitPath, 'commit', '-m', "Created project, ${model.appName}-webdev, from webdev", '.']
                process = command.execute(envp, webdevBaseDir)
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

    String consumeProcessOutput(Process process) {
        def baos = new ByteArrayOutputStream()
        def ps = new PrintStream(baos)
        process.consumeProcessOutput(System.out, ps)
        process.waitFor()
        return new String(baos.toByteArray(), StandardCharsets.UTF_8)
    }

    String getGitPath(String osName) {
        if (osName.contains('windows')) {
            def userProfile = System.getenv("USERPROFILE") ?: ''
            def scoopGitDir = "${userProfile}\\scoop\\apps\\git\\current\\cmd"
            return getExecutablePath("${scoopGitDir}\\git.exe", "C:\\Program Files\\git\\cmd\\git.exe")
        } else {
            return getExecutablePath("/usr/bin/git", "/usr/local/bin/git")
        }
    }

    String getGradlewFileName(String osName) {
        if (osName.contains('windows')) return 'gradlew.bat'
        else return 'gradlew'
    }

    String getExecutablePath(String expectedPath, String fallbackPath) {
        return (new File(expectedPath).canExecute()
            ? expectedPath
            : fallbackPath
        )
    }

    String getUnixPGExecutablePath(String executable) {
        return getExecutablePath("/usr/local/bin/$executable", "/usr/bin/$executable")
    }

    String getWindowsPGExecutablePath(String executable, String scoopPath) {
        return getExecutablePath("${scoopPath}\\${executable}.exe", "C:\\Program Files\\postgresql\\bin\\${executable}.exe")
    }

    Map<String, String> getPGExecutables(String osName) {
        def executables = [:]
        if (osName.contains('windows')) {
            def userProfile = System.getenv("USERPROFILE") ?: ''
            def scoopPGDir = "${userProfile}\\scoop\\apps\\postgresql\\current\\bin"

            executables['createuser'] = getWindowsPGExecutablePath('createuser', scoopPGDir)
            executables['createdb'] = getWindowsPGExecutablePath('createdb', scoopPGDir)
            executables['pg_restore'] = getWindowsPGExecutablePath('pg_restore', scoopPGDir)
        } else {
            executables['createuser'] = getUnixPGExecutablePath('createuser')
            executables['createdb'] = getUnixPGExecutablePath('createdb')
            executables['pg_restore'] = getUnixPGExecutablePath('pg_restore')
        }
        return executables as Map<String, String>
    }

    def createDatabase(String dbName, String dbUser, ProjectModel model)
    {
        def envp = System.getenv().collect({k, v -> "${k}=${v}"})
        def osName = System.getProperty('os.name', '').toLowerCase()
        def pgExecutables = getPGExecutables(osName)

        def createUser = pgExecutables['createuser']
        def createDb = pgExecutables['createdb']
        def pgRestore = pgExecutables['pg_restore']
        def errors = []

        if(new File(createUser).canExecute()
            && new File(createDb).canExecute()
            && new File(pgRestore).canExecute()) {
            def command = [createUser, '-s', dbUser]
            def process = command.execute(envp, model.destinationDirectory)
            def error = consumeProcessOutput(process)
            if(error) errors.push(error)
            if(error && !error.contains('already exists'))
                return errors
            command = [createDb, dbName]
            process = command.execute(envp, model.destinationDirectory)
            error = consumeProcessOutput(process)
            if(error) {
                errors.push(error)
                return errors
            }

            def artifactory = new ArtifactoryDownloader()
            try
            {
                def bootstrapdbFile = artifactory.downloadFile(artifactory.getArtifactoryCredentials(),
                    new URL("https://repo.proteus"
                        + ".co/artifactory/vt-release-local/net/proteusframework/proteusframework-bootstrapdb/0.17"
                        + ".0/proteusframework-bootstrapdb-0.17.0-20160526182335.pgdump"), '.pgdump')
                command = [pgRestore, '-x', '-O', '-Fc', '-d', dbName, bootstrapdbFile.absolutePath]
                process = command.execute(envp, model.destinationDirectory)
                error = consumeProcessOutput(process)
                if(error) errors.push(error)
                if(!bootstrapdbFile.delete()) errors.push("Warning: Unable to delete: ${bootstrapdbFile.absolutePath}")
            }
            catch(Throwable e)
            {
                errors.push(e.message)
            }
        } else {
            errors.push('Unable to execute Postgres executables for creating database.')
        }
        return errors
    }
}
