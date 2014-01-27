package windowdata;

import android.util.Log;

public class WindowHalfOverlap implements IWindowData
{
	private String TAG = "WindowHalfOverlap";
	private int wSize;
	private int floatsPerData;
	private float[][] window;
	private int nextIndex;
	
	public WindowHalfOverlap(){
		wSize = 64;
		floatsPerData = 1;
		clean();
	}
	
	public WindowHalfOverlap(int wSize, int floatsPerData){
		this.wSize = wSize;
		this.floatsPerData = floatsPerData;
		clean();
	}
	
	public boolean addData(float[] data){
		boolean full = false;
		if(nextIndex >= wSize){
			// window was full
			nextIndex = wSize/2;
			
			// copy to begin
			for(int j = 0; j < wSize/2; j++){
				for(int i = 0; i < floatsPerData; i++){
					window[i][j] = window[i][j+(wSize/2)];
				}
			}
		}else if(nextIndex == wSize-1){
			full = true;
		}
		
		for(int i = 0; i < floatsPerData; i++){
			window[i][nextIndex] = data[i];
		}
		nextIndex++;
		
		return full;
	}
	
	public boolean setWindowSize(int size){
		if(size < 4 || size%2!=0)
			return false;
		wSize = size;
		return true;
	}
	
	public float[] getData(int dataIndex){
		return window[dataIndex];
	}
	
	public void clean(){
		window = new float[floatsPerData][wSize];
		nextIndex = 0;
	}
	
	public static void testMe(){
		String TAG = "testMe";
		WindowHalfOverlap w = new WindowHalfOverlap(8, 2);
		for(int i = 0; i < 24; i++){
			if(w.addData(new float[]{i,-i})){
				float[] data1 = w.getData(0);
				float[] data2 = w.getData(1);
				String s = "data: [ ";
				for(int j = 0; j < data1.length; j++){
					s+="("+data1[j]+" "+data2[j]+") ";
				}
				s+="]";
				Log.d(TAG,s);
			}
		}
	}
}
