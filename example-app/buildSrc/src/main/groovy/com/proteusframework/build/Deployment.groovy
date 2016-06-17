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

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.sqs.AmazonSQSClient
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

/**
 * Utility for posting deployment commands.
 * @author russ (russ@venturetech.net)
 */
class DeployUtil
{
    static final String DEPLOY_QUEUE = 'deploy_queue'

    AmazonSQSClient client;
    Project project;

    DeployUtil(Project project, String id, String secret)
    {
        this.project = project;
        client = new AmazonSQSClient(new BasicAWSCredentials(id, secret))
    }

    def autoDeploy(String deploymentContext)
    {
        def version = project.findProperty('app_version').replace('-SNAPSHOT', '')
        client.sendMessage(
            project.findProperty(DEPLOY_QUEUE),
            $/{"action": "auto-deploy", data: {"context": "${deploymentContext}", "version": "$version"}/$
        )
    }
}

class DeployTask extends DefaultTask
{
    String id
    String secret
    String context = "qa"

    @TaskAction
    def sendMessage() {
        new DeployUtil(getProject(), id, secret).autoDeploy(context)
    }

}
