package ut.com.tavant.rally.confluence;

import org.junit.Test;
import com.tavant.rally.confluence.api.MyPluginComponent;
import com.tavant.rally.confluence.impl.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}