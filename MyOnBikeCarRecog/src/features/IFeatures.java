package features;

import windowdata.IWindowData;
/**
 * to get the features vector from window data
 * @author fhafon
 *
 */
public interface IFeatures {

	public float[] getFeatures(IWindowData windowData);
	public int getFeaturesType();
}
