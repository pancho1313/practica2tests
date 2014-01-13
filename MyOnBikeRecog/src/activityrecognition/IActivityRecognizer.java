package activityrecognition;

public interface IActivityRecognizer {

	public final int UNKNOWN = 0;
	public final int ON_BICYCLE = 0;
	public final int WALKING = 0;
	public final int ON_CAR = 0;
	public final int NOT_MOVING = 0;
	public final int TILTING = 0;
	
	public int getUserRecognizedActivity(float[] features);
	
}