package org.wso2.financial.services.accelerator.data.publisher;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DataPublisherFactoryTest {

    @Test
    public void testWrapReturnsDefaultPooledObject() {
        Object publisher = new Object();
        DataPublisherFactory<Object> factory = new DataPublisherFactory<>();
        PooledObject<Object> pooled = factory.wrap(publisher);
        Assert.assertTrue(pooled instanceof DefaultPooledObject);
        Assert.assertEquals(pooled.getObject(), publisher);
    }
}
