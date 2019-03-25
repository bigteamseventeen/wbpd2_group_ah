package com.callumcarmicheal.wframe.library;

public class Tuple3<T1,T2,T3> {
	public T1 x;
	public T2 y;
	public T3 z;
	
	public Tuple3(){}
	
	public Tuple3(T1 x, T2 y, T3 z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
}