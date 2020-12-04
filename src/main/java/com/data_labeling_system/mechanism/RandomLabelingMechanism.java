package com.data_labeling_system.mechanism;
import java.util.ArrayList;
import java.util.Random;

import com.sun.tools.javac.util.List;

import jdk.internal.org.objectweb.asm.Label;
import sun.security.jca.GetInstance.Instance; 

public class RandomLabelingMechanism extends LabelingMechanism{
	 
	        @Override 
	        public List<A>gnment assign(User user,Instance instance,List<Label> labels,int maxNumOfLabels) 
	        {	
	        	ArrayList<Label> arrli= new ArrayList<Label>(); 
	        	
	        	Random rand = new Random(); 
	        	int numOfLabels = rand.nextInt(maxNumOfLabels) + 1;
	        	
	        	for (int i = 0; i < numOfLabels; i++) {
	        		
		        	int max = labels.size();
			        int rand_int1 = rand.nextInt(max) + 1;
			        
			        arrli.add(labels.get(rand_int1));
			        labels.remove(rand_int1);
			        
			        return new Assignment(instance, arrli, user,Date);
	        	}
	        	
	        }    
	        
	  
	        
}
