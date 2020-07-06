package com.devhc.jobdeploy.utils;

public class NTuple<A> {


    public static <A> NTuple make(A... args) {
        return new NTuple(args);
    }

    private A[] items;


    private NTuple(A[] items) {
        this.items = items;
    }

    public A at(int index) {
        if (index < 0 || items == null || index > items.length - 1) {
            return null;
        }
        return items[index];
    }

    public static void main(String[] args) {
        NTuple t = NTuple.make("a", "b");
        System.out.println(t.at(0));
        System.out.println(t.at(1));
    }
}

