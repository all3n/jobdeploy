package com.devhc;
import com.google.common.base.Joiner;
import java.util.Arrays;
public class App{
	public static void main(String args[]){
		System.out.println(Joiner.on(",").join(Arrays.asList(1, 5, 7)));
	}
}
