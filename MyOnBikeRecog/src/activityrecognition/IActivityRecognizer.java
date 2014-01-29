package activityrecognition;

public interface IActivityRecognizer {
	
	public boolean predict(float[] featuresList, String modelFile, int labels[], double probs[]);
	
}
