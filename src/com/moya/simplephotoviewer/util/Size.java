package com.moya.simplephotoviewer.util;

public class Size {
	public int mW;
	public int mH;
	
	public Size(){
		set(-1,-1);
	}
	
	public Size(int x, int y){
		set(x,y);
	}

	public Size(Size size){
		set(size.mW,size.mH);
	}
	
	public void set(int w, int h){
		mW = w;
		mH = h;
	}
}
