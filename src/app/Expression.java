package app;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

	public static String delims = " \t*+-/()[]";
	//FINAL VERSION!
    private static void buildSyms(String expr,String delim, ArrayList<Variable> vars,ArrayList<Array> arrays){
	    	if(expr.length()<1){
	    		return;
	    	}
	    	StringTokenizer piece = new StringTokenizer(expr,delim);
			while(piece.hasMoreTokens()) { 
				String savedTok=piece.nextToken();
				if(savedTok.indexOf("[")!=-1){	
					String s = savedTok.substring(0,savedTok.indexOf("["));
					String s2 = savedTok.substring(savedTok.indexOf("[")+1);
					if(savedTok.indexOf("[")!=savedTok.length()-1){
						buildSyms(s2,delim,vars,arrays);
					}
					Array arr=new Array(s);
					if(arrays.indexOf(arr)==-1) {
						arrays.add(arr);
					}//if close
				}
				else {
						if(savedTok.indexOf(']')!=-1){
							savedTok = savedTok.substring(0,savedTok.indexOf(']'));
						}
						Variable num=new Variable(savedTok);
						boolean isNum=true;
						try{
							double d=Double.parseDouble(savedTok);
							
						}
						catch(NumberFormatException e) {
							//System.out.println("HERE:"+savedTok);
							isNum=false;
						}
		    				if(vars.indexOf(num)==-1 && savedTok.indexOf("]")==-1 && isNum==false) {
		    					vars.add(num);
		    				}
					}
				}
		}
    /**
     * Populates the vars list with simple variables, and arrays lists with arrays
     * in the expression. For every variable (simple or array), a SINGLE instance is created 
     * and stored, even if it appears more than once in the expression.
     * At this time, values for all variables and all array items are set to
     * zero - they will be loaded from a file in the loadVariableValues method.
     * 
     * @param expr The expression
     * @param vars The variables array list - already created by the caller
     * @param arrays The arrays array list - already created by the caller
     */
	
	public static void 
    makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	/** COMPLETE THIS METHOD **/
    	/** DO NOT create new vars and arrays - they are already created before being sent in
    	 ** to this method - you just need to fill them in.
    	 **/
		
		String s=expr.replaceAll(" ", "");
		String deliminator=new String();
		deliminator=" \t*+-/()";
		buildSyms(s,deliminator,vars,arrays);
    }
    
    /**
     * Loads values for variables and arrays in the expression
     * 
     * @param sc Scanner for values input
     * @throws IOException If there is a problem with the input 
     * @param vars The variables array list, previously populated by makeVariableLists
     * @param arrays The arrays array list - previously populated by makeVariableLists
     */
    public static void 
    loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays) 
    throws IOException {
        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
            int numTokens = st.countTokens();
            String tok = st.nextToken();
            Variable var = new Variable(tok);
            Array arr = new Array(tok);
            int vari = vars.indexOf(var);
            int arri = arrays.indexOf(arr);
            if (vari == -1 && arri == -1) {
            	continue;
            }
            int num = Integer.parseInt(st.nextToken());
            if (numTokens == 2) { // scalar symbol
                vars.get(vari).value = num;
            } else { // array symbol
            	arr = arrays.get(arri);
            	arr.values = new int[num];
                // following are (index,val) pairs
                while (st.hasMoreTokens()) {
                    tok = st.nextToken();
                    StringTokenizer stt = new StringTokenizer(tok," (,)");
                    int index = Integer.parseInt(stt.nextToken());
                    int val = Integer.parseInt(stt.nextToken());
                    arr.values[index] = val;              
                }
            }
        }
    }
    
    //search arraylists (arrays & variables) for value
    private static int find_object(String name, int list, ArrayList<Variable> vars,ArrayList<Array> arrays){
	    	if(list == 1){ //search arrays
	    		for(Array x : arrays){
	    			if(x.name.equals(name)){
	    				return arrays.indexOf(x);
	    			}
	    		}
	    	}else{ // search variables
	    		for(Variable x : vars){
	    			if(x.name.equals(name)){
	    				return vars.indexOf(x);
	    			}
	    		}
	    	}
	    	return -1;
    }
    
    private static void calculateOnce(Stack<Float> num, Stack<Character>operation) { //to calculate once
	  	float holdNum=num.pop();
	  	char holdOper=operation.pop();
	  	float secondHoldNum=num.pop();
	  	float result=0;
	  	if(holdOper=='+') {
	  		result=holdNum+secondHoldNum;
	  	}
	  	else if(holdOper=='-') {
	  		result=secondHoldNum-holdNum;
	  	}
	  	else if(holdOper=='*') {
	  		result=holdNum*secondHoldNum;
	  	}
	  	else {
	  		result=secondHoldNum/holdNum;
	  	}
	  		
	  	num.push(result);
	  	
	  	
	  }
    
    private static float calculateEnd(Stack<Float> num, Stack<Character> operation) { //calculate the result
  		while(!num.isEmpty() && !operation.isEmpty()) {
  			calculateOnce(num,operation);
  		}
  		return num.pop();
    }
    /**
     * Evaluates the expression.
     * 
     * @param vars The variables array list, with values for all variables in the expression
     * @param arrays The arrays array list, with values for all array items
     * @return Result of evaluation
     */
    public static float 
    evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	/** COMPLETE THIS METHOD **/
    	// following line just a placeholder for compilation
		String s = expr;
		//next evaluate array vals:
		if(s.indexOf("[")!=-1){
			s = array_eval(s,vars,arrays);
		}
    		float result = calculate(s, vars, arrays);
		return result;
    }
    
    //evaluate the array
    private static String array_eval(String s, ArrayList<Variable> vars, ArrayList<Array> arrays){
	    
    		while(s.indexOf("[")!=-1){
	    		String term = getArrayTerms(s);
	    		if(term.indexOf("[")!=-1){
	    			term = array_eval(term,vars, arrays);
	    		}
				int f = (int)calculate(term, vars, arrays);
				int i = s.indexOf("[");
				if(i>0){
					i-=1;
				}
				while(i>0){
					if(s.charAt(i) == '+' || s.charAt(i) == '-' || s.charAt(i) == '*' || s.charAt(i) == '/' || s.charAt(i)=='[' || s.charAt(i) ==' '|| s.charAt(i)=='(' || s.charAt(i)==')'){
						i++;
						break;
					}
					i--;
				}	
				String array_val = s.substring(i,s.indexOf("["))+"["+f+"]";
				float f2;

				if(i==0 && array_val.indexOf(']')==array_val.length()-1){
					//System.out.println("Go Search:");
					int ind= find_object(array_val.substring(0, array_val.indexOf('[')),1, vars,arrays);
					//System.out.println(ind);
					if(ind==-1 || ind > arrays.size() || arrays.get(ind)==null){
						f2=(float)0.0;
					}else{
						if(f>-1 && arrays.get(ind).values!=null && f<arrays.get(ind).values.length){
							f2 = arrays.get(ind).values[f];
						}else{
							f2 = (float)0.0;
						}
					}
				}else{
					f2= calculate(array_val,vars,arrays);
				}
				
				s = s.substring(0, i)+f2+s.substring(get_corresponding_bracket_ind(s)+1);
			}
	    	return s;
    }
   
    //master: it evaluates the expression
    private static float calculate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays){
    		
		String s = expr;
		
		//simplify: first calculate all values inside parantheses (PEMDAS)
		while(s.indexOf("(")!=-1){
			String term = getParanthesesTerms(s);
			//System.out.println("\nCalculating...\n");
			float f = calculate(term, vars, arrays);
		    //code will fail for exponential values
	 
			term = "("+term+")";
			String t = s.replace(term, f+"");
			s =t;

			//System.out.println("updated term: "+s);
		}
		expr = s;
		
    	
	    	Stack<Float> num = getNumStack(expr, vars,arrays);
	   
	    	Stack<Character> chV = getOpStack(expr, num);
     	//Stack<Float> rev_num = reverse(num);
     	//num = rev_num;
	    	//System.out.println("Top of Reverse:"+rev_num.peek());
	    	
	    //	Stack<Character> rev_chV = reverse(chV);
	    	//chV = rev_chV;
	    //	System.out.println("Top of Ops:"+rev_chV.peek());
	    	
	    	if(num.size()==1){
	    		if(chV.size()==1 && chV.peek()=='-') {
	    			return -1*num.pop();
	    		}
	    		return num.pop();
	    	}else if(chV.size()==num.size()) {
	    		if(chV.peek().charValue()=='-') {
	    			chV.pop();
	    			if(chV.size()>0) {
	    				chV.pop();
	    				chV.push(new Character('+'));
	    			}else {
	    				return  -1*num.pop();
	    			}
	    		}
	    	}
	    	Stack<Character> temp = new Stack<Character>();
	    Stack<Float> temp_n = new Stack<Float>();
	    
	    	//enforce order of operations (complete multiplication and divide and then reverse)
	    	while(!chV.isEmpty() && !num.isEmpty()){ 
	    		//System.out.println("char:"+chV.size());
	    		//System.out.println("num:"+num.size());
	    		char op = chV.pop().charValue();
	    		if(op=='*'){
	    			//ignore = true;
	    			float n1 = 0;
	    			//if(!temp_n.isEmpty()) {
	    			//	n1 = temp_n.pop();
	    			//}else {
	    				n1 = num.pop().floatValue();
	    			//}
	    			//System.out.println("multiply 1: "+n1);
	    			float n2 = num.pop().floatValue();
	    			//System.out.println("multiply 2: "+n2);
	    			float p = n1*n2;
	    			//System.out.println("product: "+(n1*n2));
	    			num.push(p);
	    		}else if(op=='/'){
	    			//ignore = true;
	    			float d = num.pop().floatValue();
	    			float n = num.pop().floatValue();
	    			//System.out.println("denom 1: "+d);
	    			//System.out.println("numer 1: "+n);
	    			float r = n/d;
	    			//System.out.println("result: "+(r));
	    			num.push(r);
	    		}else{
	    			temp.push(op);
	    			Float a = num.pop();
	    			temp_n.push((a));
	    			//System.out.println("SKIP HERE:"+a.floatValue());
	    		}
	    		//temp_n.push(num.pop());
	    		//ignore = false;
		}
    	
	    	while(!num.isEmpty()){
	    		temp_n.push(num.pop());
	    	}
	    	chV = temp;
	    	//chV = reverse(chV);
	    	
	    	num = temp_n;
	    	//num = reverse(num);
	    	
	    	while(!chV.isEmpty() && num.size()>1){
	    		char v = chV.pop().charValue();
	    		//System.out.println(v);
	    		float result =0;
	    		switch(v){
	    			case '+': 
	    				float n1 = num.pop().floatValue();
	    				float n2 = num.pop().floatValue();
	    				//System.out.println("Add: "+n1+"+"+n2);
	    				result = n1 + n2;
	    				//System.out.println("result:"+ result);
	    				num.push(result);
	    				break;
	    			case '-': 
	    				//System.out.println("chV size:"+(chV.size()+1));
	    				//System.out.println("num size:"+(num.size()));
	    				float n_1 = num.pop().floatValue();
	    				//System.out.println("First:"+n_1);
	    				float n_2 = num.pop().floatValue();
	    				//System.out.println("Second:"+n_2);
	    				//System.out.println("Subtract: "+n_1+"-"+n_2);
	    				result = n_1-n_2;
//	    				result= n_1+ -1*n_2;
	    				//System.out.println("result:"+ result);
	    				num.push(result);
	    				break;
	    			case '/': 
	    				float numer = num.pop().floatValue();
	    				float denom = num.pop().floatValue();
	    				//System.out.println("numer:"+numer);
	    				//System.out.println("denom: "+ denom);
	    				//System.out.println("result:"+ (numer/denom));
	    				num.push(numer/denom);
	    				break;
	    			case '*':
	    				//System.out.println("HERE?");
	    				num.push(num.pop().floatValue()*num.pop().floatValue());
	    				break;
					default:
						 num.push((float)0.0);
						break;
	    		}
	    	}
	    //	System.out.println("RETURNING:"+num.peek());
	    	return num.pop();
    }
    private static Stack reverse(Stack val) {
    		Stack ret = new Stack();
    		while(!val.isEmpty()) {
    			ret.push(val.pop());
    		}
    		return ret;
    }
    
   //evaluate symbols (arrays and variables):
    private static float evaluateSyms(String expr,String delim, ArrayList<Variable> vars, ArrayList<Array> arrays){
  		if(expr.length()<1){
      		return (float)0.0;
      	}
  		if(expr.indexOf("[")!=-1){
  			expr = array_eval(expr,vars,arrays);
  		}
      	StringTokenizer piece = new StringTokenizer(expr,delim);
  		while(piece.hasMoreTokens()) { 
  			String savedTok=piece.nextToken();
  			
  			if(savedTok.indexOf("[")!=-1){
  				//System.out.println("evaluating array:"+savedTok);
  				String s = savedTok.substring(0,savedTok.indexOf("[")); //name of the array
  				//System.out.println(s);
  				String s2 = savedTok.substring(savedTok.indexOf("[")+1, savedTok.indexOf("]")); //stuff inside of the array
  				//System.out.println(s2);
  				if(s2.indexOf("[")!=-1){
  					s2 = array_eval(s2,vars,arrays);
  				}
  				if(savedTok.indexOf("[")!=savedTok.length()-1){
  						int indx = find_object(s,1, vars, arrays);
  						if(indx !=-1){
  							Array val = arrays.get(indx);
  							indx = (int) calculate(s2, vars, arrays);
  							return val.values[indx];
  						}
  				}
  			}else {
  				if(savedTok.indexOf("(")!=-1){ // do stuff inside parantheses
  					//System.out.println("Here parantheses:"+savedTok);
  					String s = savedTok.substring(0,savedTok.indexOf("(")); //name of the array
  					String s2 = savedTok.substring(savedTok.indexOf("(")+1, savedTok.indexOf(")")); //stuff inside of the array
  					return calculate(s2, vars, arrays);
  				}	
  				boolean isNum=true;
  				try{
  					//System.out.println("NUM: "+savedTok);
  					float f=Float.parseFloat(savedTok);
  					return f;	
  				}
  				catch(NumberFormatException e) {
  					//Code will fail for exponential numbers eg. 2.1E-7
//  					Float test = new Float(new Float(2.1E-7));
//  					System.out.println("TEST:"+(new Double(test.doubleValue())));
  					isNum=false;
  				}
  				if(savedTok.indexOf("]")==-1 && isNum==false) {
  					int indx = find_object(savedTok,2,vars, arrays);
  					return vars.get(find_object(savedTok,2,vars,arrays)).value;
  				}
  			}
  		}
  		return (float)0;
      }
      
    //get stack of operations:
    private static Stack<Character> getOpStack(String expr, Stack<Float> nums){
    	////System.out.println("OpStack:"+expr);
    	Stack<Float> temp = new Stack<Float>();
    	Stack<Character> chV = new Stack<Character>();
    	char [] vals = expr.toCharArray();
    	boolean inside_parantheses = false;
    	for(int i=0; i< vals.length; i++){
    		if(vals[i]=='('){
    			inside_parantheses = true;
    		}else if(vals[i]==')'){
    			inside_parantheses = false;
    		}
    		if(inside_parantheses == false && (vals[i]=='+' || vals[i] == '-' || vals[i] == '*' || vals[i]=='/')){
    				if(i>0) {
    					boolean prev_sym = (vals[i-1]=='+' || vals[i-1] == '-' || vals[i-1] == '*' || vals[i-1]=='/');
    					if(prev_sym && vals[i] == '-' && vals[i-1] !='-') {
    						float v = nums.pop().floatValue();
    						//System.out.println(vals[i-1]+""+vals[i]);
    						//System.out.println("VAL:-"+v);
    						nums.push(new Float(-1*v));
    					}else {
    						chV.push(new Character(vals[i]));
    					}
    				}else {
    					temp.push(nums.pop());
    					chV.push(new Character(vals[i]));
    				}
    				
    		} 
    	} 
    		while(!temp.isEmpty()) {
    			nums.push(temp.pop());
    		}
    		//Stack<Character> temp = new Stack<Character>();
		//reverse stack to make it FIFO
    	
		return chV;
    }
    
    //get stack of numbers:
    private static Stack<Float> getNumStack(String expr,ArrayList<Variable> vars, ArrayList<Array> arrays ){
    		Stack<Float>num=new Stack<Float>();
		String deliminator=new String();
		deliminator=" \t*+-/()";
		float result = 0;
		// 	(moved code block that was here to beginning of calculate)
			StringTokenizer piece = new StringTokenizer(expr,deliminator);
			while(piece.hasMoreTokens()){
				String savedTok=piece.nextToken();
				Float f = new Float(evaluateSyms(savedTok,deliminator, vars, arrays));
				//System.out.println(f);
				num.push(f);
			}
		return num;
    }
    
    //find the index of closing bracket matching opening bracket
    private static int get_corresponding_bracket_ind(String expr){
		int last_bracket_ind = expr.indexOf("]");
		int count_open_bracket = 0;
		
		//to deal with case like A[B[2]]
		for(int i = 0; i< expr.length(); i++){
			if(expr.charAt(i)==']'){
				count_open_bracket --;
				if(count_open_bracket == 0){
					last_bracket_ind = i;
					break;
				}
			}else if(expr.charAt(i) == '['){
				count_open_bracket ++;
			}
		}
		return last_bracket_ind;
    }
    
    private static int get_corresponding_paran_ind(String expr){
		int last_bracket_ind = expr.indexOf(")");
		int count_open_bracket = 0;
		
		//to deal with case like A[B[2]]
		for(int i = 0; i< expr.length(); i++){
			if(expr.charAt(i)==')'){
				count_open_bracket --;
				if(count_open_bracket == 0){
					last_bracket_ind = i;
					break;
				}
			}else if(expr.charAt(i) == '('){
				count_open_bracket ++;
			}
		}
		return last_bracket_ind;
    }
    
    //returns substring inside array brackets:
    private static String getArrayTerms(String expr){
	    	if(expr.indexOf("[")!=-1 && expr.indexOf("]") !=-1){
	    		int last_bracket_ind = get_corresponding_bracket_ind(expr);
	    		return expr.substring(expr.indexOf("[")+1, last_bracket_ind);
	    	}else{
	    		return expr;
	    	}
    }
    
    //returns substring inside parantheses:
    private static String getParanthesesTerms(String expr){
    	
    	if(expr.indexOf('(') !=-1 && expr.indexOf(")")!=-1) {
    		int i = get_corresponding_paran_ind(expr);
    		return expr.substring(expr.indexOf("(")+1, i);
    	}else{
    		return expr;
    	}
    	
    }
    
    /**
     * Utility method, prints the symbols in the variables list
     */
    public static void printVariables(ArrayList<Variable> vars) {
        for (Variable v: vars) {
            //System.out.println(v);
        }
    }
    
    /**
     * Utility method, prints the symbols in the arrays list
     */
    public static void printArrays(ArrayList<Array> arrays) {
    		for (Array a: arrays) {
    			//System.out.println(a);
    		}
    }
}
