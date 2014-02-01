package windowdata;

import android.util.Log;

/**
 * This class stores a pair number elements (each element is a float[] data),
 * and implements a 50% overlapping version of a windowData.
 * When it fills it can returns the data stored for features analysis 
 * @author job
 *
 */
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
	
	/**
	 * wSize must be pair
	 * floatsPerData is the length of each float[] data stored in each addData(float[] data)
	 * @param wSize
	 * @param floatsPerData
	 */
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
	
	/**
	 * Each element of a windowData is a float[] data. So if windowData=
	 * {{a,b},{c,d},{e,f},{g,h}} then getData(0)={a,c,e,g} and getData(1)={b,d,f,h}
	 */
	public float[] getData(int dataIndex){
		return window[dataIndex];
	}
	
	/**
	 * reset windowData
	 */
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
