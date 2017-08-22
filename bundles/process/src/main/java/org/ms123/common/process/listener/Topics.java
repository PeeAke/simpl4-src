package org.ms123.common.process.listener;

/**
 * @author Ronny Bräunlich
 */
public interface Topics {

  public static final String TASK_EVENT_TOPIC = "org/camunda/bpm/extension/osgi/eventing/TaskEvent";

  public static final String EXECUTION_EVENT_TOPIC = "org/camunda/bpm/extension/osgi/eventing/ExecutionEvent";
  
  public static final String ALL_EVENTING_EVENTS_TOPIC = "org/camunda/bpm/extension/osgi/eventing/*";
}
