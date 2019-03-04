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

package com.proteusframework.build;

import org.ajoberstar.grgit.Grgit
import org.slf4j.LoggerFactory

/**
 * Plugin to provide git information
 *
 * @author Russ Tennant (russ@i2rd.com)
 */
public class GitInfo
{
    String branch=''
    String ref=''
    String commit=''
    String author=''

    /**
     * Populate project with a "gitinfo" object.
     * @param project the project.
     * @return the
     */
    @SuppressWarnings(["GroovyUntypedAccess", "GroovyAssignabilityCheck", "GroovyUnusedDeclaration"])
    public static populateProject(def project)
    {
        GitInfo gitInfo = new GitInfo()
        project.ext.gitinfo = gitInfo
        try{
            def repo = project.file('../.git').exists() ? Grgit.open(dir: project.file('..')) : Grgit.open()
            def branch = repo.branch.current
            def commit = repo.head()

            gitInfo.branch = branch.name
            gitInfo.ref = branch.fullName
            gitInfo.commit = commit.id
            gitInfo.author = "${commit.author.name} <${commit.author.email}>".toString()
        } catch(def e) {
            LoggerFactory.getLogger('build').debug(
                'Doesn\'t appear we are a repo. I am okay with it if you are.', e
            )
            println '!!! Remember to setup a git repo at some point.'
        }
    }

}
