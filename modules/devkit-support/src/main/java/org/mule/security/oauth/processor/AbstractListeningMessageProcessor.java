/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.security.oauth.processor;

import org.mule.DefaultMuleEvent;
import org.mule.DefaultMuleMessage;
import org.mule.MessageExchangePattern;
import org.mule.RequestContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.callback.SourceCallback;
import org.mule.api.processor.MessageProcessor;
import org.mule.devkit.processor.DevkitBasedMessageProcessor;

import java.util.Map;

public abstract class AbstractListeningMessageProcessor extends DevkitBasedMessageProcessor
    implements SourceCallback
{

    /**
     * Message processor that will get called for processing incoming events
     */
    private MessageProcessor messageProcessor;

    /**
     * Retrieves messageProcessor
     */
    public MessageProcessor getMessageProcessor()
    {
        return this.messageProcessor;
    }

    public AbstractListeningMessageProcessor(String operationName)
    {
        super(operationName);
    }

    /**
     * Sets the message processor that will "listen" the events generated by this
     * message source
     * 
     * @param listener Message processor
     */
    public void setListener(MessageProcessor listener)
    {
        this.messageProcessor = listener;
    }

    /**
     * Implements {@link SourceCallback#process(org.mule.api.MuleEvent)}. This
     * message source will be passed on to the actual pojo's method as a callback
     * mechanism.
     */
    @Override
    public Object process(Object message) throws Exception
    {
        MuleMessage muleMessage;
        muleMessage = new DefaultMuleMessage(message, getMuleContext());
        MuleEvent muleEvent;
        muleEvent = new DefaultMuleEvent(muleMessage, MessageExchangePattern.ONE_WAY, getFlowConstruct());
        try
        {
            MuleEvent responseEvent;
            responseEvent = messageProcessor.process(muleEvent);
            if ((responseEvent != null) && (responseEvent.getMessage() != null))
            {
                return responseEvent.getMessage().getPayload();
            }
        }
        catch (Exception e)
        {
            throw e;
        }
        return null;
    }

    /**
     * Implements {@link SourceCallback#process(org.mule.api.MuleEvent)}. This
     * message source will be passed on to the actual pojo's method as a callback
     * mechanism.
     */
    @Override
    public Object process(Object message, Map<String, Object> properties) throws Exception
    {
        MuleMessage muleMessage;
        muleMessage = new DefaultMuleMessage(message, properties, null, null, getMuleContext());
        MuleEvent muleEvent;
        muleEvent = new DefaultMuleEvent(muleMessage, MessageExchangePattern.ONE_WAY, getFlowConstruct());
        try
        {
            MuleEvent responseEvent;
            responseEvent = messageProcessor.process(muleEvent);
            if ((responseEvent != null) && (responseEvent.getMessage() != null))
            {
                return responseEvent.getMessage().getPayload();
            }
        }
        catch (Exception e)
        {
            throw e;
        }
        return null;
    }

    /**
     * Implements {@link SourceCallback#process()}. This message source will be
     * passed on to the actual pojo's method as a callback mechanism.
     */
    @Override
    public Object process() throws Exception
    {
        try
        {
            MuleEvent responseEvent;
            responseEvent = messageProcessor.process(RequestContext.getEvent());
            if ((responseEvent != null) && (responseEvent.getMessage() != null))
            {
                return responseEvent.getMessage().getPayload();
            }
        }
        catch (Exception e)
        {
            throw e;
        }
        return null;
    }

    /**
     * Implements {@link SourceCallback#processEvent(org.mule.api.MuleEvent)}. This
     * message source will be passed on to the actual pojo's method as a callback
     * mechanism.
     */
    public MuleEvent processEvent(MuleEvent event) throws MuleException
    {
        return messageProcessor.process(event);
    }
    
    /**
     * Not valid for this class or its extensions
     * @throws UnsupportedOperationException
     */
    @Override
    protected MuleEvent doProcess(MuleEvent event) throws Exception
    {
        throw new UnsupportedOperationException("Listening message processors cannot execute this method");
    }

}
