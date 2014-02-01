package trainergenerator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Used to merge libsvm training/testing files of bicycle and car. 
 * @author job
 *
 */
public class TrainingFilesMerger {

	private static void p(String s){
		System.out.print(s);
	}
	
	/**
	 * Add a prefix to the label of each class to make a difference between car and bicycle states.
	 * Add more features if needed (MyFeatures3 and MyFeatures4 previous state)
	 * @param features
	 * @param trainingBike
	 * @param trainingCar
	 * @throws IOException
	 */
	private static void merge(String features, BufferedReader trainingBike, BufferedReader trainingCar) throws IOException{
		String preFile1 = "1"; // bike {1 2 3 4} --> {11 12 13 14}
		String preFile2 = "2"; // car  {1 2 3 4} --> {21 22 23 24}
		
		boolean firstLine = true;
		
		while(true){
			String line = trainingBike.readLine();
			if(line == null) break;
			if(!firstLine){
				p("\n");
			}else{
				firstLine = false;
			}

			StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");
			
			String target = st.nextToken();
			
			p(preFile1);
			p(target);
			p(" ");
			
			int m = st.countTokens()/2;
			int index;
			String value;
			for(int j = 0; j < m; j++){
				index = Integer.parseInt(st.nextToken());
				value = st.nextToken();
				p(index + ":" + value + " ");
				if(features.equals("3") && index == 8){
					p("9:0.0 10:0.0 11:0.0 12:0.0 ");
				}else if(features.equals("4") && index == 12){
					p("13:0.0 14:0.0 15:0.0 16:0.0 ");
				}
			}
		}

		while(true){
			String line = trainingCar.readLine();
			if(line == null) break;
			if(!firstLine){
				p("\n");
			}else{
				firstLine = false;
			}

			StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");
			
			String target = st.nextToken();
			
			p(preFile2);
			p(target);
			p(" ");
			
			int m = st.countTokens()/2;
			int index;
			String value;
			for(int j = 0; j < m; j++){
				index = Integer.parseInt(st.nextToken());
				value = st.nextToken();
				
				if(features.equals("3")){
					if(index == 5){
						p("5:0.0 6:0.0 7:0.0 8:0.0 ");
					}
					if(index >= 5)
						p((index+4) + ":" + value + " ");
					else
						p(index + ":" + value + " ");
					
				}else if(features.equals("4")){
					if(index == 9){
						p("9:0.0 10:0.0 11:0.0 12:0.0 ");
					}
					if(index >= 9)
						p((index+4) + ":" + value + " ");
					else
						p(index + ":" + value + " ");
				}else{
					p(index + ":" + value + " ");
				}
			}
		}
	}
	
	public static void main(String argv[]) throws IOException
	{
		if(argv.length != 3){
			String s = " ERROR argv (eg: java TrainingFilesMerger 1 trainingBike.64.1.txt trainingCar.64.1.txt)";
			System.out.println(s);
			System.exit(1);
		}
		
		BufferedReader file1 = null, file2 = null;
		
		try {
			file1 = new BufferedReader(new FileReader(argv[1]));
			file2 = new BufferedReader(new FileReader(argv[2]));
			merge(argv[0], file1, file2);
		} catch(FileNotFoundException e) {
			System.out.println("couldn't read files");
			System.exit(1);
		} catch(ArrayIndexOutOfBoundsException e) {
			System.out.println("couldn't read files");
			System.exit(1);
		}
		
		file1.close();
		file2.close();
		
		
	}
	
}
