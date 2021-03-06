package windowdata;

/**
 * 
 * @author fhafon
 *
 */
public interface IWindowData
{
	/**
	 * 
	 * @param data
	 * @return if full window
	 */
	public boolean addData(float[] data);

	/**
	 * Set window size
	 * @param pair size
	 * @return if correct size
	 */
	public boolean setWindowSize(int size);

	/**
	 * Get all data in present window
	 */
	public float[] getData(int dataIndex);
	
	/**
	 * reset and delete window data
	 */
	public void clean();

}
