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

package com.example.app.support.service;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.proteusframework.core.StringFactory;
import net.proteusframework.core.spring.ApplicationContextUtils;

/**
 * Utility class for operating with Amazon SQS queues.
 *
 * @author Todd Chrisman (tchrisman@i2rd.com)
 * @since 3/31/17 2:17 PM
 */
@Repository("com.example.app.support.service.AmazonSQSUtil")
public class AmazonSQSUtil
{
    /**
     * The maximum number of messages to retrieve from a queue (if available). The dequeue operations will break after retrieving at
     * least this many messages.
     */
    public static final int NUMBER_OF_MESSAGES_TO_RETRIEVE = 1000;
    /** Logger */
    private static final Logger _logger = LogManager.getLogger(AmazonSQSUtil.class);
    /** Amazon SQS client */
    private AmazonSQSClient _sqsClient;
    /** AWS Credential Provider */
    @Autowired
    private AWSCredentialsProvider _awsCredentialsProvider;

    /**
     * Get a Amazon Sqs client
     *
     * @return Amazon sqs client
     */
    public AmazonSQSClient getAmazonSqsClient()
    {
        if (_sqsClient != null)
            return _sqsClient;

        _sqsClient = new AmazonSQSClient(_awsCredentialsProvider);
        return _sqsClient;
    }

    /**
     * Get the URL of an Amazon SQS queue using the given queueId. Will attempt to create the queue if it does not exist.
     *
     * @param queueId - the ID of the queue URL to get
     * @return - the URL of the queue
     */
    public String getQueueURL(String queueId)
    {
        final AmazonSQSClient sqsClient = getAmazonSqsClient();
        String queueUrl = null;

        try
        {
            queueUrl = sqsClient.getQueueUrl(_getQueueId(queueId)).getQueueUrl();
        }
        catch (QueueDoesNotExistException e0)
        {
            try
            {
                queueUrl = sqsClient.createQueue(new CreateQueueRequest(_getQueueId(queueId))).getQueueUrl();
            }
            catch (Throwable e)
            {
                _logger.error("Cannot create queue: " + _getQueueId(queueId) + " for Amazon SQS due to ", e);
            }
        }

        if (queueUrl == null)
        {
            _logger
                .error("Failed to send/retrieve message because I couldn't get the Queue Url for the Amazon SQS queue: " +
                       _getQueueId(queueId));
            return "";
        }
        return queueUrl;
    }

    /**
     * Queue a message in an Amazon SQS Queue
     *
     * @param message - the message
     * @param queueId - the queue id.
     * @return {@code true} when the message is successfully sent, {@code false} otherwise.
     */
    public boolean enqueue(String message, String queueId)
    {
        final AmazonSQSClient sqsClient = getAmazonSqsClient();
        String queueUrl = getQueueURL(_getQueueId(queueId));

        if (queueUrl.isEmpty())
            return false;

        SendMessageResult result = sqsClient.sendMessage(queueUrl, message);
        return result.getMessageId() != null;
    }

    /**
     * Retrieve message(s) from an Amazon SQS Queue. The {@link Message}s are returned as is. It is
     * up to the caller to delete the {@link Message} from the queue if the retrieval is determined to be successful.
     *
     * @param queueId - the queue id.
     * @return the messages
     */
    public List<Message> dequeueMessages(String queueId)
    {
        final AmazonSQSClient sqsClient = getAmazonSqsClient();
        String queueUrl = getQueueURL(_getQueueId(queueId));

        if (queueUrl.isEmpty())
            return Collections.emptyList();

        final List<Message> messages = new ArrayList<>();
        int messageCount = 0;
        ReceiveMessageResult messageResult;

        while ((messageResult = sqsClient.receiveMessage(queueUrl)) != null
               && !messageResult.getMessages().isEmpty()
               && messageCount < AmazonSQSUtil.NUMBER_OF_MESSAGES_TO_RETRIEVE)
        {
            if (!messageResult.getMessages().isEmpty())
            {
                messages.addAll(messageResult.getMessages());
                messageCount += messageResult.getMessages().size();
            }
        }

        return messages;
    }

    /**
     * Retrieve message(s) from an Amazon SQS Queue. This automatically extracts the body of each message into a String and deletes
     * the message in the queue.
     *
     * @param queueId - the queue id.
     * @return the messages
     */
    public List<String> dequeueStrings(String queueId)
    {
        final AmazonSQSClient sqsClient = getAmazonSqsClient();
        String queueUrl = getQueueURL(_getQueueId(queueId));

        if (queueUrl.isEmpty())
            return Collections.emptyList();

        List<String> messages = new ArrayList<>();
        int messageCount = 0;
        ReceiveMessageResult messageResult;
        while ((messageResult = sqsClient.receiveMessage(queueUrl)) != null
               && !messageResult.getMessages().isEmpty()
               && messageCount < AmazonSQSUtil.NUMBER_OF_MESSAGES_TO_RETRIEVE)
        {
            for (final Message msg : messageResult.getMessages())
            {
                messages.add(msg.getBody());
                sqsClient.deleteMessage(queueUrl, msg.getReceiptHandle());
                messageCount++;
            }
        }

        return messages;
    }

    /**
     * Delete the given {@link Message} from the queue
     *
     * @param msg - the {@link Message} to delete
     * @param queueId - the queue id.
     */
    public void deleteMessage(Message msg, String queueId)
    {
        final AmazonSQSClient sqsClient = getAmazonSqsClient();
        String queueUrl = getQueueURL(_getQueueId(queueId));

        if (queueUrl.isEmpty())
            return;

        sqsClient.deleteMessage(queueUrl, msg.getReceiptHandle());
    }

    /**
     * Get the queue id. (Relative to deployment context)
     *
     * @param queueId the queue id (not relative to deployment context)
     * @return the queue id.
     */
    private static String _getQueueId(String queueId)
    {
        ApplicationContext context = ApplicationContextUtils.getInstance().getContext();
        if(context == null)
            return queueId;

        // Requires creation of install property if distinct queue names are desired.
        String install = context.getEnvironment().getProperty("install");
        if(StringFactory.isEmptyString(install))
            return queueId;
        else
            return install + '_' + queueId;
    }
}
