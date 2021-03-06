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
package org.blocks4j.reconf.client.constructors.array;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import org.blocks4j.reconf.client.constructors.ArrayConstructor;
import org.blocks4j.reconf.client.constructors.MethodData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class ArrayConstructorClassTest {

    private MethodData data;
    private Method method;
    private final Class<?> arrayClass = new BigDecimal[0].getClass();
    private final Class<?> targetClass = BigDecimal.class;

    @Before
    public void prepare() throws Exception {
        method = ArrayConstructorClassTarget.class.getMethod("get", new Class<?>[]{});
    }

    @Test
    public void test_null() throws Throwable {
        data = new MethodData(method, targetClass, null);
        Object o = new ArrayConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(arrayClass));
        Assert.assertTrue(((Object[]) o).length == 0);
    }

    @Test(expected=Exception.class)
    public void test_blank() throws Throwable {
        data = new MethodData(method, targetClass, " ");
        new ArrayConstructor().construct(data);
    }

    @Test
    public void test_empty() throws Throwable {
        data = new MethodData(method, targetClass, "");
        Object o = new ArrayConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(arrayClass));
        Assert.assertTrue(((Object[]) o).length == 0);
    }

    @Test
    public void test_one_elem() throws Throwable {
        data = new MethodData(method, targetClass, "['0']");
        Object o = new ArrayConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(arrayClass));
        Assert.assertTrue(((Object[]) o).length == 1);
        Assert.assertTrue(((Object[]) o)[0].equals(BigDecimal.ZERO));
    }

    @Test
    public void test_two_elem() throws Throwable {
        data = new MethodData(method, targetClass, "['1','10']");
        Object o = new ArrayConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(arrayClass));
        Assert.assertTrue(((Object[]) o).length == 2);
        Assert.assertTrue(((Object[]) o)[0].equals(BigDecimal.ONE));
        Assert.assertTrue(((Object[]) o)[1].equals(BigDecimal.TEN));
    }
}

interface ArrayConstructorClassTarget {
    BigDecimal[] get();
}