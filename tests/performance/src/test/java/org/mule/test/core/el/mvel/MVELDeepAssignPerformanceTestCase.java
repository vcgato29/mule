/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.core.el.mvel;

import static org.mule.runtime.core.api.MessageExchangePattern.ONE_WAY;
import static org.mule.runtime.core.api.config.MuleProperties.OBJECT_EXPRESSION_LANGUAGE;
import static org.mule.tck.MuleTestUtils.getTestFlow;

import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.message.InternalMessage;
import org.mule.runtime.core.construct.Flow;
import org.mule.runtime.core.el.mvel.MVELExpressionLanguage;
import org.mule.tck.junit4.AbstractMuleContextTestCase;

import java.util.Random;

import org.databene.contiperf.PerfTest;
import org.databene.contiperf.Required;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

public class MVELDeepAssignPerformanceTestCase extends AbstractMuleContextTestCase {

  @Rule
  public ContiPerfRule rule = new ContiPerfRule();

  final protected String mel = "payload.firstName = 'Tom';"
      + "payload.lastName = 'Fennelly';"
      + "payload.contact.address = 'Male';"
      + "payload.contact.telnum = '4';"
      + "payload.sin = 'Ireland';"
      + "payload;";

  final protected Payload payload = new Payload();

  protected Event event;
  protected Flow flow;

  @Before
  public void before() throws Exception {
    ((MVELExpressionLanguage) muleContext.getRegistry().lookupObject(OBJECT_EXPRESSION_LANGUAGE)).setAutoResolveVariables(false);
    event = createMuleEvent();
    flow = getTestFlow(muleContext);
    // Warmup
    for (int i = 0; i < 5000; i++) {
      muleContext.getExpressionManager().evaluate(mel, event, flow);
    }
  }

  /**
   * Cold start: - New expression for each iteration - New context (message) for each iteration
   */
  @Test
  @PerfTest(duration = 30000, threads = 1, warmUp = 10000)
  @Required(median = 4000)
  public void mvelColdStart() {
    for (int i = 0; i < 1000; i++) {
      muleContext.getExpressionManager().evaluate(mel + new Random().nextInt(), createMuleEvent(), flow);
    }
  }

  /**
   * Warm start: - Same expression for each iteration - New context (message) for each iteration
   */
  @Test
  @PerfTest(duration = 30000, threads = 1, warmUp = 10000)
  @Required(median = 25)
  public void mvelWarmStart() {
    for (int i = 0; i < 1000; i++) {
      muleContext.getExpressionManager().evaluate(mel, event, flow);
    }
  }

  /**
   * Hot start: - Same expression for each iteration - Same context (message) for each iteration
   */
  @Test
  @PerfTest(duration = 30000, threads = 1, warmUp = 10000)
  @Required(median = 25)
  public void mvelHotStart() {
    for (int i = 0; i < 1000; i++) {
      muleContext.getExpressionManager().evaluate(mel, event, flow);
    }
  }

  @Ignore
  @Test
  @PerfTest(duration = 30000, threads = 1, warmUp = 10000)
  public void createEventBaseline() {
    for (int i = 0; i < 1000; i++) {
      createMuleEvent();
    }
  }

  protected Event createMuleEvent() {
    try {
      return eventBuilder().message(InternalMessage.builder().payload(payload).build()).exchangePattern(ONE_WAY).build();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static class Payload {

    public String firstName;
    public String lastName;
    public Contact contact = new Contact();
    public String sin;
  }

  public static class Contact {

    public String address;
    public String telnum;
  }
}