package org.wso2.financial.services.accelerator.data.publisher.thrift.publisher;

import java.util.Map;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class InputValidatorTest {

    @Test
    public void testSingletonInstance() {
        InputValidator instance1 = InputValidator.getInstance();
        InputValidator instance2 = InputValidator.getInstance();
        assertSame(instance1, instance2, "InputValidator should be a singleton");
    }

    @Test
    public void testGetEventProperties_ResponseSchema() {
        Map<String, Class> props = InputValidator.getInstance().getEventProperties(ThriftMetricSchema.THRIFT_RESPONSE);
        assertNotNull(props);
        assertTrue(props.containsKey(Constants.API_CONTEXT));
        assertEquals(props.get(Constants.API_CONTEXT), String.class);
    }

    @Test
    public void testGetEventProperties_ErrorSchema() {
        Map<String, Class> props = InputValidator.getInstance().getEventProperties(ThriftMetricSchema.THRIFT_ERROR);
        assertNotNull(props);
        assertTrue(props.containsKey(Constants.ERROR_CODE));
        assertEquals(props.get(Constants.ERROR_CODE), String.class);
    }

    @Test
    public void testGetEventProperties_ThrottleOutSchema() {
        Map<String, Class> props = InputValidator.getInstance().getEventProperties(ThriftMetricSchema.
                THRIFT_THROTTLE_OUT);
        assertNotNull(props);
        assertTrue(props.containsKey(Constants.THROTTLED_OUT_REASON));
        assertEquals(props.get(Constants.THROTTLED_OUT_REASON), String.class);
    }

    @Test
    public void testGetEventProperties_UnknownSchema() {
        // Simulate unknown schema by passing null
        Map<String, Class> props = InputValidator.getInstance().getEventProperties(null);
        assertNotNull(props);
        assertTrue(props.isEmpty(), "Unknown schema should return empty map");
    }
}

