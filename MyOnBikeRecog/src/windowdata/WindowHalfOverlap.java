package windowdata;

import android.util.Log;

public class WindowHalfOverlap implements IWindowData
{
	private String TAG = "WindowHalfOverlap";
	private int wSize;
	private float[] window;
	private int nextIndex;
	
	public WindowHalfOverlap(){
		wSize = 16;
		window = new float[wSize];
		nextIndex = 0;
	}
	
	public boolean addData(float data){
		if(nextIndex >= wSize){
			// window was full
			nextIndex = wSize/2;
			
			// copy to begin
			for(int i = 0; i < wSize/2; i++){
				window[i] = window[i+(wSize/2)];
			}
		}else if(nextIndex == wSize-1){
			window[nextIndex++] = data;
			return true;
		}
		
		window[nextIndex++] = data;
		
		return false;
	}
	
	public boolean setWindowSize(int size){
		if(size < 4 || size%2!=0)
			return false;
		wSize = size;
		return true;
	}
	
	public float[] getData(){
		return window;
	}
}
