package activityrecognition;

public interface IActivityRecognizer {
	
	public boolean predict(float[] featuresList, String modelFile, int[] label, double probs[]);
	
}
