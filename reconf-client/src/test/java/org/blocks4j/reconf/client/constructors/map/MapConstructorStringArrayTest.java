/*
 *   Copyright 2013-2015 Blocks4J Team (www.blocks4j.org)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.blocks4j.reconf.client.constructors.map;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.blocks4j.reconf.client.constructors.MapConstructor;
import org.blocks4j.reconf.client.constructors.MethodData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class MapConstructorStringArrayTest {

    private MethodData data;
    private Method method;
    private Class<?> targetClass = HashMap.class;

    @Before
    public void prepare() throws Exception {
        method = MapConstructorStringArrayValueTarget.class.getMethod("get", new Class<?>[]{});
    }

    @Test
    public void test_empty_value() throws Throwable {
        data = new MethodData(method, method.getGenericReturnType(), "['k':[]]");
        Object o = new MapConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(targetClass));
        Map<String, String[]> cast = (Map<String,String[]>) o;
        Assert.assertTrue(cast.size() == 1);
        Assert.assertTrue(cast.entrySet().iterator().next().getKey().equals("k"));
        Assert.assertTrue(cast.entrySet().iterator().next().getValue().length == 0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_open_right_error() throws Throwable {
        data = new MethodData(method, method.getGenericReturnType(), "['k':[]");
        new MapConstructor().construct(data);
    }


    @Test
    public void test_normal_value() throws Throwable {
        data = new MethodData(method, method.getGenericReturnType(), "['k':['v1', 'v2']]");
        Object o = new MapConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(targetClass));
        Map<String, String[]> cast = (Map<String,String[]>) o;
        Assert.assertTrue(cast.size() == 1);
        Assert.assertTrue(cast.entrySet().iterator().next().getKey().equals("k"));
        String[] value = cast.entrySet().iterator().next().getValue();
        Assert.assertEquals(value[0], "v1");
        Assert.assertEquals(value[1], "v2");
    }
}

interface MapConstructorStringArrayValueTarget {
    HashMap<String, String[]> get();
}