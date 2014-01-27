package features;

import windowdata.IWindowData;

public interface IFeatures {

	public float[] getFeatures(IWindowData windowData);
	public int getFeaturesType();
}
