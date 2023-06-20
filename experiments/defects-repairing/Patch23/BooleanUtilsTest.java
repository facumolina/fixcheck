/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.lang;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Unit tests {@link org.apache.commons.lang.BooleanUtils}.
 *
 * @author Stephen Colebourne
 * @author Matthew Hawthorne
 * @version $Id$
 */
public class BooleanUtilsTest extends TestCase {

    public BooleanUtilsTest(String name) {
        super(name);
    }

//    public static void main(String[] args) {
//        TestRunner.run(suite());
//    }

//    public static Test suite() {
//        TestSuite suite = new TestSuite(BooleanUtilsTest.class);
//        suite.setName("BooleanUtils Tests");
//        return suite;
//    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    //-----------------------------------------------------------------------
    public void testConstructor() {
        assertNotNull(new BooleanUtils());
        Constructor[] cons = BooleanUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(BooleanUtils.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(BooleanUtils.class.getModifiers()));
    }
    
    //-----------------------------------------------------------------------
    public void test_negate_Boolean() {
        assertSame(null, BooleanUtils.negate(null));
        assertSame(Boolean.TRUE, BooleanUtils.negate(Boolean.FALSE));
        assertSame(Boolean.FALSE, BooleanUtils.negate(Boolean.TRUE));
    }

    //-----------------------------------------------------------------------
    public void test_isTrue_Boolean() {
        assertEquals(true, BooleanUtils.isTrue(Boolean.TRUE));
        assertEquals(false, BooleanUtils.isTrue(Boolean.FALSE));
        assertEquals(false, BooleanUtils.isTrue((Boolean) null));
    }

    public void test_isNotTrue_Boolean() {
        assertEquals(false, BooleanUtils.isNotTrue(Boolean.TRUE));
        assertEquals(true, BooleanUtils.isNotTrue(Boolean.FALSE));
        assertEquals(true, BooleanUtils.isNotTrue((Boolean) null));
    }

    //-----------------------------------------------------------------------
    public void test_isFalse_Boolean() {
        assertEquals(false, BooleanUtils.isFalse(Boolean.TRUE));
        assertEquals(true, BooleanUtils.isFalse(Boolean.FALSE));
        assertEquals(false, BooleanUtils.isFalse((Boolean) null));
    }

    public void test_isNotFalse_Boolean() {
        assertEquals(true, BooleanUtils.isNotFalse(Boolean.TRUE));
        assertEquals(false, BooleanUtils.isNotFalse(Boolean.FALSE));
        assertEquals(true, BooleanUtils.isNotFalse((Boolean) null));
    }

    //-----------------------------------------------------------------------
    public void test_toBooleanObject_boolean() {
        assertSame(Boolean.TRUE, BooleanUtils.toBooleanObject(true));
        assertSame(Boolean.FALSE, BooleanUtils.toBooleanObject(false));
    }

    public void test_toBoolean_Boolean() {
        assertEquals(true, BooleanUtils.toBoolean(Boolean.TRUE));
        assertEquals(false, BooleanUtils.toBoolean(Boolean.FALSE));
        assertEquals(false, BooleanUtils.toBoolean((Boolean) null));
    }

    public void test_toBooleanDefaultIfNull_Boolean_boolean() {
        assertEquals(true, BooleanUtils.toBooleanDefaultIfNull(Boolean.TRUE, true));
        assertEquals(true, BooleanUtils.toBooleanDefaultIfNull(Boolean.TRUE, false));
        assertEquals(false, BooleanUtils.toBooleanDefaultIfNull(Boolean.FALSE, true));
        assertEquals(false, BooleanUtils.toBooleanDefaultIfNull(Boolean.FALSE, false));
        assertEquals(true, BooleanUtils.toBooleanDefaultIfNull((Boolean) null, true));
        assertEquals(false, BooleanUtils.toBooleanDefaultIfNull((Boolean) null, false));
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    public void test_toBoolean_int() {
        assertEquals(true, BooleanUtils.toBoolean(1));
        assertEquals(true, BooleanUtils.toBoolean(-1));
        assertEquals(false, BooleanUtils.toBoolean(0));
    }
    
    public void test_toBooleanObject_int() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(1));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(-1));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(0));
    }
    
    public void test_toBooleanObject_Integer() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(new Integer(1)));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(new Integer(-1)));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(new Integer(0)));
        assertEquals(null, BooleanUtils.toBooleanObject((Integer) null));
    }
    
    //-----------------------------------------------------------------------
    public void test_toBoolean_int_int_int() {
        assertEquals(true, BooleanUtils.toBoolean(6, 6, 7));
        assertEquals(false, BooleanUtils.toBoolean(7, 6, 7));
        try {
            BooleanUtils.toBoolean(8, 6, 7);
            fail();
        } catch (IllegalArgumentException ex) {}
    }
    
    public void test_toBoolean_Integer_Integer_Integer() {
        Integer six = new Integer(6);
        Integer seven = new Integer(7);

        assertEquals(true, BooleanUtils.toBoolean((Integer) null, null, seven));
        assertEquals(false, BooleanUtils.toBoolean((Integer) null, six, null));
        try {
            BooleanUtils.toBoolean(null, six, seven);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        assertEquals(true, BooleanUtils.toBoolean(new Integer(6), six, seven));
        assertEquals(false, BooleanUtils.toBoolean(new Integer(7), six, seven));
        try {
            BooleanUtils.toBoolean(new Integer(8), six, seven);
            fail();
        } catch (IllegalArgumentException ex) {}
    }
    
    //-----------------------------------------------------------------------
    public void test_toBooleanObject_int_int_int() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(6, 6, 7, 8));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(7, 6, 7, 8));
        assertEquals(null, BooleanUtils.toBooleanObject(8, 6, 7, 8));
        try {
            BooleanUtils.toBooleanObject(9, 6, 7, 8);
            fail();
        } catch (IllegalArgumentException ex) {}
    }
    
    public void test_toBooleanObject_Integer_Integer_Integer_Integer() {
        Integer six = new Integer(6);
        Integer seven = new Integer(7);
        Integer eight = new Integer(8);

        assertSame(Boolean.TRUE, BooleanUtils.toBooleanObject((Integer) null, null, seven, eight));
        assertSame(Boolean.FALSE, BooleanUtils.toBooleanObject((Integer) null, six, null, eight));
        assertSame(null, BooleanUtils.toBooleanObject((Integer) null, six, seven, null));
        try {
            BooleanUtils.toBooleanObject(null, six, seven, eight);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(new Integer(6), six, seven, eight));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(new Integer(7), six, seven, eight));
        assertEquals(null, BooleanUtils.toBooleanObject(new Integer(8), six, seven, eight));
        try {
            BooleanUtils.toBooleanObject(new Integer(9), six, seven, eight);
            fail();
        } catch (IllegalArgumentException ex) {}
    }
    
    //-----------------------------------------------------------------------
    public void test_toInteger_boolean() {
        assertEquals(1, BooleanUtils.toInteger(true));
        assertEquals(0, BooleanUtils.toInteger(false));
    }
    
    public void test_toIntegerObject_boolean() {
        assertEquals(new Integer(1), BooleanUtils.toIntegerObject(true));
        assertEquals(new Integer(0), BooleanUtils.toIntegerObject(false));
    }
    
    public void test_toIntegerObject_Boolean() {
        assertEquals(new Integer(1), BooleanUtils.toIntegerObject(Boolean.TRUE));
        assertEquals(new Integer(0), BooleanUtils.toIntegerObject(Boolean.FALSE));
        assertEquals(null, BooleanUtils.toIntegerObject((Boolean) null));
    }
    
    //-----------------------------------------------------------------------
    public void test_toInteger_boolean_int_int() {
        assertEquals(6, BooleanUtils.toInteger(true, 6, 7));
        assertEquals(7, BooleanUtils.toInteger(false, 6, 7));
    }
    
    public void test_toInteger_Boolean_int_int_int() {
        assertEquals(6, BooleanUtils.toInteger(Boolean.TRUE, 6, 7, 8));
        assertEquals(7, BooleanUtils.toInteger(Boolean.FALSE, 6, 7, 8));
        assertEquals(8, BooleanUtils.toInteger(null, 6, 7, 8));
    }
    
    public void test_toIntegerObject_boolean_Integer_Integer() {
        Integer six = new Integer(6);
        Integer seven = new Integer(7);
        assertEquals(six, BooleanUtils.toIntegerObject(true, six, seven));
        assertEquals(seven, BooleanUtils.toIntegerObject(false, six, seven));
    }
    
    public void test_toIntegerObject_Boolean_Integer_Integer_Integer() {
        Integer six = new Integer(6);
        Integer seven = new Integer(7);
        Integer eight = new Integer(8);
        assertEquals(six, BooleanUtils.toIntegerObject(Boolean.TRUE, six, seven, eight));
        assertEquals(seven, BooleanUtils.toIntegerObject(Boolean.FALSE, six, seven, eight));
        assertEquals(eight, BooleanUtils.toIntegerObject((Boolean) null, six, seven, eight));
        assertEquals(null, BooleanUtils.toIntegerObject((Boolean) null, six, seven, null));
    }
    
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    public void test_toBooleanObject_String() {
        assertEquals(null, BooleanUtils.toBooleanObject((String) null));
        assertEquals(null, BooleanUtils.toBooleanObject(""));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("false"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("no"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("off"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("FALSE"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("NO"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("OFF"));
        assertEquals(null, BooleanUtils.toBooleanObject("oof"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("true"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("yes"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("on"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("TRUE"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("ON"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("YES"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("TruE"));
    }
    
    public void test_toBooleanObject_String_String_String_String() {
        assertSame(Boolean.TRUE, BooleanUtils.toBooleanObject((String) null, null, "N", "U"));
        assertSame(Boolean.FALSE, BooleanUtils.toBooleanObject((String) null, "Y", null, "U"));
        assertSame(null, BooleanUtils.toBooleanObject((String) null, "Y", "N", null));
        try {
            BooleanUtils.toBooleanObject((String) null, "Y", "N", "U");
            fail();
        } catch (IllegalArgumentException ex) {}

        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("Y", "Y", "N", "U"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("N", "Y", "N", "U"));
        assertEquals(null, BooleanUtils.toBooleanObject("U", "Y", "N", "U"));
        try {
            BooleanUtils.toBooleanObject(null, "Y", "N", "U");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            BooleanUtils.toBooleanObject("X", "Y", "N", "U");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

    //-----------------------------------------------------------------------
    public void test_toBoolean_String() {
        boolean b1 = BooleanUtils.toBoolean((String) null);
        assertEquals(false, b1);
        boolean b2 = BooleanUtils.toBoolean("");
        assertEquals(false, b2);
        boolean b3 = BooleanUtils.toBoolean("off");
        assertEquals(false, b3);
        boolean b4 = BooleanUtils.toBoolean("oof");
        assertEquals(false, b4);
        boolean b5 = BooleanUtils.toBoolean("yep");
        assertEquals(false, b5);
        boolean b6 = BooleanUtils.toBoolean("trux");
        assertEquals(false, b6);
        boolean b7 = BooleanUtils.toBoolean("false");
        assertEquals(false, b7);
        boolean b8 = BooleanUtils.toBoolean("a");
        assertEquals(false, b8);
        boolean b9 = BooleanUtils.toBoolean("true");
        assertEquals(true, b9); // interned handled differently
        boolean b10 = BooleanUtils.toBoolean(new StringBuffer("tr").append("ue").toString());
        assertEquals(true, b10);
        boolean b11 = BooleanUtils.toBoolean("truE");
        assertEquals(true, b11);
        boolean b12 = BooleanUtils.toBoolean("trUe");
        assertEquals(true, b12);
        boolean b13 = BooleanUtils.toBoolean("trUE");
        assertEquals(true, b13);
        boolean b14 = BooleanUtils.toBoolean("tRue");
        assertEquals(true, b14);
        boolean b15 = BooleanUtils.toBoolean("tRuE");
        assertEquals(true, b15);
        boolean b16 = BooleanUtils.toBoolean("tRUe");
        assertEquals(true, b16);
        boolean b17 = BooleanUtils.toBoolean("tRUE");
        assertEquals(true, b17);
        boolean b18 = BooleanUtils.toBoolean("TRUE");
        assertEquals(true, b18);
        boolean b19 = BooleanUtils.toBoolean("TRUe");
        assertEquals(true, b19);
        boolean b20 = BooleanUtils.toBoolean("TRuE"); 
        assertEquals(true, b20);
        boolean b21 = BooleanUtils.toBoolean("TRue");
        assertEquals(true, b21);
        boolean b22 = BooleanUtils.toBoolean("TrUE");
        assertEquals(true, b22);
        boolean b23 = BooleanUtils.toBoolean("TrUe");
        assertEquals(true, b23);
        boolean b24 = BooleanUtils.toBoolean("TruE");
        assertEquals(true, b24);
        boolean b25 = BooleanUtils.toBoolean("True");
        assertEquals(true, b25);
        boolean b26 = BooleanUtils.toBoolean("on");
        assertEquals(true, b26);
        boolean b27 = BooleanUtils.toBoolean("oN");
        assertEquals(true, b27);
        boolean b28 = BooleanUtils.toBoolean("On");
        assertEquals(true, b28);
        boolean b29 = BooleanUtils.toBoolean("ON");
        assertEquals(true, b29);
        boolean b30 = BooleanUtils.toBoolean("yes");
        assertEquals(true, b30);
        boolean b31 = BooleanUtils.toBoolean("yeS");
        assertEquals(true, b31);
        boolean b32 = BooleanUtils.toBoolean("yEs");
        assertEquals(true, b32);
        boolean b33 = BooleanUtils.toBoolean("yES");
        assertEquals(true, b33);
        boolean b34 = BooleanUtils.toBoolean("Yes");
        assertEquals(true, b34);
        boolean b35 = BooleanUtils.toBoolean("YeS");
        assertEquals(true, b35);
        boolean b36 = BooleanUtils.toBoolean("YEs");
        assertEquals(true, b36);
        boolean b37 = BooleanUtils.toBoolean("YES");
        assertEquals(true, b37);
        boolean b38 = BooleanUtils.toBoolean("yes?");
        assertEquals(false, b38);
        boolean b39 = BooleanUtils.toBoolean("tru");
        assertEquals(false, b39);
    }

    public void test_toBoolean_String_String_String() {
        assertEquals(true, BooleanUtils.toBoolean((String) null, null, "N"));
        assertEquals(false, BooleanUtils.toBoolean((String) null, "Y", null));
        try {
            BooleanUtils.toBooleanObject((String) null, "Y", "N", "U");
            fail();
        } catch (IllegalArgumentException ex) {}
        
        assertEquals(true, BooleanUtils.toBoolean("Y", "Y", "N"));
        assertEquals(false, BooleanUtils.toBoolean("N", "Y", "N"));
        try {
            BooleanUtils.toBoolean(null, "Y", "N");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            BooleanUtils.toBoolean("X", "Y", "N");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

    //-----------------------------------------------------------------------
    public void test_toStringTrueFalse_Boolean() {
        assertEquals(null, BooleanUtils.toStringTrueFalse((Boolean) null));
        assertEquals("true", BooleanUtils.toStringTrueFalse(Boolean.TRUE));
        assertEquals("false", BooleanUtils.toStringTrueFalse(Boolean.FALSE));
    }
    
    public void test_toStringOnOff_Boolean() {
        assertEquals(null, BooleanUtils.toStringOnOff((Boolean) null));
        assertEquals("on", BooleanUtils.toStringOnOff(Boolean.TRUE));
        assertEquals("off", BooleanUtils.toStringOnOff(Boolean.FALSE));
    }
    
    public void test_toStringYesNo_Boolean() {
        assertEquals(null, BooleanUtils.toStringYesNo((Boolean) null));
        assertEquals("yes", BooleanUtils.toStringYesNo(Boolean.TRUE));
        assertEquals("no", BooleanUtils.toStringYesNo(Boolean.FALSE));
    }
    
    public void test_toString_Boolean_String_String_String() {
        assertEquals("U", BooleanUtils.toString((Boolean) null, "Y", "N", "U"));
        assertEquals("Y", BooleanUtils.toString(Boolean.TRUE, "Y", "N", "U"));
        assertEquals("N", BooleanUtils.toString(Boolean.FALSE, "Y", "N", "U"));
    }
    
    //-----------------------------------------------------------------------
    public void test_toStringTrueFalse_boolean() {
        assertEquals("true", BooleanUtils.toStringTrueFalse(true));
        assertEquals("false", BooleanUtils.toStringTrueFalse(false));
    }
    
    public void test_toStringOnOff_boolean() {
        assertEquals("on", BooleanUtils.toStringOnOff(true));
        assertEquals("off", BooleanUtils.toStringOnOff(false));
    }
    
    public void test_toStringYesNo_boolean() {
        assertEquals("yes", BooleanUtils.toStringYesNo(true));
        assertEquals("no", BooleanUtils.toStringYesNo(false));
    }
    
    public void test_toString_boolean_String_String_String() {
        assertEquals("Y", BooleanUtils.toString(true, "Y", "N"));
        assertEquals("N", BooleanUtils.toString(false, "Y", "N"));
    }
    
    //  testXor
    //  -----------------------------------------------------------------------
    public void testXor_primitive_nullInput() {
        final boolean[] b = null;
        try {
            BooleanUtils.xor(b);
            fail("Exception was not thrown for null input.");
        } catch (IllegalArgumentException ex) {}
    }

    public void testXor_primitive_emptyInput() {
        try {
            BooleanUtils.xor(new boolean[] {});
            fail("Exception was not thrown for empty input.");
        } catch (IllegalArgumentException ex) {}
    }

    public void testXor_primitive_validInput_2items() {
        assertTrue(
            "True result for (true, true)",
            ! BooleanUtils.xor(new boolean[] { true, true }));

        assertTrue(
            "True result for (false, false)",
            ! BooleanUtils.xor(new boolean[] { false, false }));

        assertTrue(
            "False result for (true, false)",
            BooleanUtils.xor(new boolean[] { true, false }));

        assertTrue(
            "False result for (false, true)",
            BooleanUtils.xor(new boolean[] { false, true }));
    }

    public void testXor_primitive_validInput_3items() {
        assertTrue(
            "False result for (false, false, true)",
            BooleanUtils.xor(new boolean[] { false, false, true }));

        assertTrue(
            "False result for (false, true, false)",
            BooleanUtils.xor(new boolean[] { false, true, false }));

        assertTrue(
            "False result for (true, false, false)",
            BooleanUtils.xor(new boolean[] { true, false, false }));

        assertTrue(
            "True result for (true, true, true)",
            ! BooleanUtils.xor(new boolean[] { true, true, true }));

        assertTrue(
            "True result for (false, false)",
            ! BooleanUtils.xor(new boolean[] { false, false, false }));

        assertTrue(
            "True result for (true, true, false)",
            ! BooleanUtils.xor(new boolean[] { true, true, false }));

        assertTrue(
            "True result for (true, false, true)",
            ! BooleanUtils.xor(new boolean[] { true, false, true }));

        assertTrue(
            "False result for (false, true, true)",
            ! BooleanUtils.xor(new boolean[] { false, true, true }));
    }

    public void testXor_object_nullInput() {
        final Boolean[] b = null;
        try {
            BooleanUtils.xor(b);
            fail("Exception was not thrown for null input.");
        } catch (IllegalArgumentException ex) {}
    }

    public void testXor_object_emptyInput() {
        try {
            BooleanUtils.xor(new Boolean[] {});
            fail("Exception was not thrown for empty input.");
        } catch (IllegalArgumentException ex) {}
    }
    
    public void testXor_object_nullElementInput() {
        try {
            BooleanUtils.xor(new Boolean[] {null});
            fail("Exception was not thrown for null element input.");
        } catch (IllegalArgumentException ex) {}
    }

    public void testXor_object_validInput_2items() {
        assertTrue(
            "True result for (true, true)",
            ! BooleanUtils
                .xor(new Boolean[] { Boolean.TRUE, Boolean.TRUE })
                .booleanValue());

        assertTrue(
            "True result for (false, false)",
            ! BooleanUtils
                .xor(new Boolean[] { Boolean.FALSE, Boolean.FALSE })
                .booleanValue());

        assertTrue(
            "False result for (true, false)",
            BooleanUtils
                .xor(new Boolean[] { Boolean.TRUE, Boolean.FALSE })
                .booleanValue());

        assertTrue(
            "False result for (false, true)",
            BooleanUtils
                .xor(new Boolean[] { Boolean.FALSE, Boolean.TRUE })
                .booleanValue());
    }

    public void testXor_object_validInput_3items() {
        assertTrue(
            "False result for (false, false, true)",
            BooleanUtils
                .xor(
                    new Boolean[] {
                        Boolean.FALSE,
                        Boolean.FALSE,
                        Boolean.TRUE })
                .booleanValue());

        assertTrue(
            "False result for (false, true, false)",
            BooleanUtils
                .xor(
                    new Boolean[] {
                        Boolean.FALSE,
                        Boolean.TRUE,
                        Boolean.FALSE })
                .booleanValue());

        assertTrue(
            "False result for (true, false, false)",
            BooleanUtils
                .xor(
                    new Boolean[] {
                        Boolean.TRUE,
                        Boolean.FALSE,
                        Boolean.FALSE })
                .booleanValue());

        assertTrue(
            "True result for (true, true, true)",
            ! BooleanUtils
                .xor(new Boolean[] { Boolean.TRUE, Boolean.TRUE, Boolean.TRUE })
                .booleanValue());

        assertTrue(
            "True result for (false, false)",
            ! BooleanUtils.xor(
                    new Boolean[] {
                        Boolean.FALSE,
                        Boolean.FALSE,
                        Boolean.FALSE })
                .booleanValue());

        assertTrue(
            "True result for (true, true, false)",
            ! BooleanUtils.xor(
                    new Boolean[] {
                        Boolean.TRUE,
                        Boolean.TRUE,
                        Boolean.FALSE })
                .booleanValue());

        assertTrue(
            "True result for (true, false, true)",
            ! BooleanUtils.xor(
                    new Boolean[] {
                        Boolean.TRUE,
                        Boolean.FALSE,
                        Boolean.TRUE })
                .booleanValue());

        assertTrue(
            "False result for (false, true, true)",
            ! BooleanUtils.xor(
                    new Boolean[] {
                        Boolean.FALSE,
                        Boolean.TRUE,
                        Boolean.TRUE })
                .booleanValue());
                
    }

}
