package org.codehaus.jackson.map.convert;

import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.*;

public class TestBeanConversions
    extends org.codehaus.jackson.map.BaseMapTest
{
    final ObjectMapper mapper = new ObjectMapper();

    static class Point {
        public int x, y;

        public int z = -13;

        public Point() { }
        public Point(int a, int b, int c)
        {
            x = a;
            y = b;
            z = c;
        }
    }

    static class PointStrings {
        public final String x, y;

        public PointStrings(String x, String y) {
            this.x = x;
            this.y = y;
        }
    }

    public static class BooleanBean {
        public boolean boolProp;
    }

    static class WrapperBean {
        public BooleanBean x;
    }

    static class ObjectWrapper
    {
        private Object data;

        public ObjectWrapper() { }
        public ObjectWrapper(Object o) { data = o; }

        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
    }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */
    
    public void testBeanConvert()
    {
        // should have no problems convert between compatible beans...
        PointStrings input = new PointStrings("37", "-9");
        Point point = mapper.convertValue(input, Point.class);
        assertEquals(37, point.x);
        assertEquals(-9, point.y);
        // z not included in input, will be whatever default constructor provides
        assertEquals(-13, point.z);
    }
    
    // For [JACKSON-371]; verify that we know property that caused issue...
    // (note: not optimal place for test, but will have to do for now)
    public void testErrorReporting() throws Exception
    {
        //String json = "{\"boolProp\":\"oops\"}";
        // First: unknown property
        try {
            mapper.readValue("{\"unknownProp\":true}", BooleanBean.class);
        } catch (JsonProcessingException e) {
            verifyException(e, "unknownProp");
        }

        // then bad conversion
        try {
            mapper.readValue("{\"boolProp\":\"foobar\"}", BooleanBean.class);
        } catch (JsonProcessingException e) {
            verifyException(e, "boolProp");
        }
    }

    public void testIssue458() throws Exception
    {
        ObjectWrapper a = new ObjectWrapper("foo");
        ObjectWrapper b = new ObjectWrapper(a);
        ObjectWrapper b2 = mapper.convertValue(b, ObjectWrapper.class);
        ObjectWrapper a2 = mapper.convertValue(b2.getData(), ObjectWrapper.class);
        assertEquals("foo", a2.getData());
    }

    // [JACKSON-710]: should work regardless of wrapping...
    public void testWrapping() throws Exception
    {
        ObjectMapper wrappingMapper = new ObjectMapper();
        wrappingMapper.enable(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE);
        wrappingMapper.enable(SerializationConfig.Feature.WRAP_ROOT_VALUE);
        Point input = new Point(1, 2, 3);
        // conversion is ok, even if it's bogus one
        Point output = wrappingMapper.convertValue(input, Point.class);
        assertEquals(1, output.x);
        assertEquals(2, output.y);
        assertEquals(3, output.z);
    }
}
