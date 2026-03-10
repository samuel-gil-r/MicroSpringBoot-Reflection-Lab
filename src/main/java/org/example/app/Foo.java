package org.example.app;

public class Foo {

    public static void m1() { }

    public static void m2() { }

    public static void m3() {
        throw new RuntimeException("Boom");
    }

    public static void m4() { }

    public static void m5() { }

    public static void m6() { }

    public static void m7() {
        throw new RuntimeException("Crash");
    }

    public static void m8() { }
}