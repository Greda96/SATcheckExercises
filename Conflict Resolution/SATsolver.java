import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Stack;
import java.security.SecureRandom;
import java.io.*;
import javax.swing.JOptionPane;

public class SATsolver {
	
	//The decision level is always initialized with 0 at the beginning and incremented by 1 after each decision.
	public static int decisionLevel = 0;


	//public static int dbackLevel;

	//Literal that is forced to be true due to the unit clause rule
	static int unitLiteral;

	//The negation of the unit Literal
	static int negatedUnitLiteral;


	public static int antecedentPointer;


	public static ArrayList<Integer> antecedent;


	static ArrayList<Integer> unitClause = new ArrayList<Integer>();

	//List of clauses aka. the formula
	public static ArrayList<ArrayList<Integer>> clauseList = new ArrayList<ArrayList<Integer>>();


	//List of variables
	static ArrayList<Integer> variables = new ArrayList<Integer>();

	
	static int numberOfClauses;

	
	static int numberOfVariables; 

	//The clause that becomes conflicting (all literals assigned 0) because of the current assignment
	public static ArrayList<Integer> conflictingClause;

	//The index of the conflicting clause in the list of clauses/the formula
	public static int conflictingClauseIndex;


	public static int lastAssignedLit;

	//public static int negatedLastAssignedLit;

	public static int varOfLit;


	public static ArrayList<Integer> currConflictClause;


	//Result of the binary resolution
	public static ArrayList<Integer> resolvent;

	
	public static int firstUIP;

	
	public static ArrayList<Integer> assertingClause = new ArrayList<Integer>();

	
	public static int backtrackLevel;
	
	
	public static ArrayList<Integer> confZeroClause = new ArrayList<Integer>();
	
	//Flag that is set to true when a conflict occurred
	public static boolean conflict = false;
	
	//Flag in order to store conflict formula only after 1st conflict
	//involving this formula has occurred
	public static boolean conflictOutput = false;
	
	public static ArrayList<ArrayList<Integer>> setOfconflictClauses;
	
	public static String trailAsString = "";
	
	public static ArrayList<ArrayList<Integer>> listOfResolutionClauses = new ArrayList<ArrayList<Integer>>();
	
	public static ArrayList<Integer> listOfResolutionVariables = new ArrayList<Integer>();
	
	public static ArrayList<ArrayList<Integer>> resolutionSolutionList = new ArrayList<ArrayList<Integer>>(); 
	
	public static ArrayList<Integer> conflictingClauseIndexForTask = new ArrayList<>();
	
	public static String cnfClauseForTask;
	


/*====================================================================================================	
 *                                         Formula generator 
 * ===================================================================================================
 */
	//Helper method for the formula generator
	public static ArrayList<Integer> clauseLiteralRange(int size, int lwBound, int uppBound) {
	    SecureRandom random = new SecureRandom();
	    random.setSeed(System.currentTimeMillis());
	    ArrayList<Integer> clause = new ArrayList<Integer>();
	    for (int i = 0; i < size; i++) {
	    	int rand = random.nextInt(uppBound) - lwBound;
	    	if((!clause.contains(rand)) && (!clause.contains(-rand)) && (rand != 0)) {
	    		clause.add(rand);
	    	}
	    }
	    return clause;
	}	
		
	//Method that generates formula randomly
	public static ArrayList<ArrayList<Integer>> formulaGenerator() {
		SecureRandom random = new SecureRandom();
		random.setSeed(System.currentTimeMillis());
		for(int i = 0; i < 5; i++) {	
			ArrayList<Integer> formulaClause = clauseLiteralRange(random.nextInt(3)+3,5,10);
			if(!formulaClause.isEmpty()) {
				formulaClause.add(0);
				clauseList.add(formulaClause);
			}
			continue;
		}
		return clauseList;
	}
	
/*====================================================================================================
	
	/*
	 * trail as stack: Contains literals, the assignment for the literal and the reasons for the literals assignment
	 * 
	 */
	public static Stack<ArrayList<Object>> trail = new Stack<ArrayList<Object>>();
	
	public static Stack<ArrayList<Object>> initialTrail = new Stack<ArrayList<Object>>();
			
	//Method for extracting the variables contained in the formula to create the List of Variables
		public static  ArrayList<Integer>  collectVariables(ArrayList<ArrayList<Integer>> formula) {
			for(ArrayList<Integer> clause: clauseList) {
				for(int i = 0; i < clause.size(); i++) { 
					if(clause.get(i) > 0 && !variables.contains(clause.get(i))) {
						variables.add(clause.get(i)); 
					}else if(clause.get(i) < 0 && !variables.contains(-clause.get(i))) {
						variables.add(-clause.get(i));	
					}else if(clause.get(i) == 0) {
						continue;
		 			}
				}
			Collections.sort(variables);
			numberOfVariables = variables.size();
			numberOfClauses = clauseList.size();
			}
			return variables;
			}
		
		

	public static ArrayList<Integer> tmpVars = new ArrayList<Integer>();

	@SuppressWarnings("unchecked")
		public static ArrayList<Integer> copyVariables() {
			tmpVars = (ArrayList<Integer>) variables.clone(); 
			return tmpVars;
		}

	/*
	 * 
	 * We create a one "shallow" copy (tmpClauseList) of our list of clauses (clauseList)
	 * to work with it during the SAT solving process, and also one "deep" copy (deepCopyOfClauseList) 
	 * which represents the clause in its original form and doesn't get modified during SAT solving
	 * so that it can be used for later reference.
	 * The list of clauses (clauseList) will later hold our unit clauses and will be used to reference them later on.
	 * 
	 */
	
	//Shallow copy of clauseList
	public static ArrayList<ArrayList<Integer>> tmpClauseList;
	@SuppressWarnings("unchecked")
		public static ArrayList<ArrayList<Integer>> copyFormula() {
			tmpClauseList = (ArrayList<ArrayList<Integer>>) clauseList.clone();
			return tmpClauseList;
	}


	//Deep copy of clauseList
	public static ArrayList<ArrayList<Integer>> deepCopyOfClauseList;

	public static ArrayList<ArrayList<Integer>> deepCopyFormula(ArrayList<ArrayList<Integer>> clauseListToCopy) {
		deepCopyOfClauseList = new ArrayList<ArrayList<Integer>>(clauseListToCopy.size());
	    for(ArrayList<Integer> clause : clauseListToCopy) {
	    	@SuppressWarnings("unchecked")
			ArrayList<Integer> cloneOfClause = (ArrayList<Integer>) clause.clone();
	        deepCopyOfClauseList.add(cloneOfClause);
	    }
	    return deepCopyOfClauseList;
	} 


	
		/*
		 * The decide()-method is for the decisions we make while SAT solving.
		 * 
		 * In the 1st for loop:
		 * If we have set a literal in a clause to true we remove it from the clause list, since the clause 
		 * is already satisfied.
		 * 
		 * In the 2nd for loop:
		 * Here we search for the clause that contains the negated form of the next literal to be assigned
		 * and remove that literal from the clause afterwards.
		 * By this we mimic an assignment of the literal to the value "false" 
		 * 
		 * Every time we make an decision we increment the decision counter (decision) by one.
		 * And push the negated form of the assigned variable to the trail, since we agreed 
		 * on the permanent assignment of true (= 1) for the literals that are pushed to the trail.
		 * We write null as 3rd parameter for the decision trail entry, since we freely chose the decision and guessed the assignment
		 * therefore there is no reason in form of an implication forced from a clause in the input formula
		 * 
		 */
		public static boolean decide() {
			/*
			 * We exit the decide()-method if we cannot make a decision, this is if tmpVars is empty, 
			 * which is equivalent to the fact that all variables are assigned (or the whole formula is empty)
			 * 
			 */
			if(tmpVars.isEmpty()) {
				return false;
			}
			
			
			decisionLevel++;
			int nextAssign = tmpVars.get(0); 
			int negatedNextAssign = -nextAssign;
			if(!litInTrail(negatedNextAssign)) {
				Object[] decisionEntry = {negatedNextAssign,1,null}; 
				ArrayList<Object> decisionTrailEntry = new ArrayList<>(Arrays.asList(decisionEntry));
				trail.push(decisionTrailEntry);
			}
			
			/*
			 * In this for loop we loop over the copy of the clause List (tmpClauseList) 
			 * and store the reason for an assignment and its position into the variables
			 * antecedent and antecedentPointer
			 * 
			 * In the first if-Statement we check if the next Assignment negated is contained 
			 * in the current clause (tmpClause the element in the current for-loop iteration). If yes, we remove the entire clause, because 
			 * after making a decision we set variables to false but push the negated variable onto the trail
			 * since the trail only contains literals that are set to true. And after that
			 * we decrease i by one. We do nothing otherwise (no else-statement).
			 * 
			 * The following if-statement proves if the next assigned literal is contained in the current clause
			 * (same tmpClause as before). If yes, we remove that literal from the tmpClause and decrease i by one afterwards
			 * We do nothing otherwise (no else-statement).
			 * 
			 */
			for(int i = 0; i < tmpClauseList.size(); i++) {
				ArrayList<Integer> tmpClause = tmpClauseList.get(i);
				
				antecedentPointer = tmpClauseList.indexOf((Object) tmpClause); 
				antecedent = tmpClauseList.get(antecedentPointer); 			
				
			    if(tmpClause.contains(negatedNextAssign)) {
			    	tmpClauseList.remove(tmpClause);
			    	i--;
			    }
			    	
			    	if(tmpClause.contains((Object) nextAssign)) {
			    		tmpClause.remove((Object) nextAssign);
			    		i--; 
			    	}    
			}
			
			//Removes the element at index 0 in tmpVars to indicate that the variable is assigned a value
			tmpVars.remove(0); 
			
			
			//Here we print the reduced list of variables every time we make an assignment
			ArrayList<Integer> copytmpVars = new ArrayList<Integer>();
			for(int i = 0; i < tmpVars.size(); i++) {
				copytmpVars.add(tmpVars.get(i));
				}
			tmpVars = copytmpVars;
			
			return true;
		}
		
		//Helper function that checks if a literal is already in the trail
		static boolean litInTrail(int searchedLiteral) {
			for(int i = 0; i < trail.size(); i++) {
				if(trail.get(i).get(0).equals(searchedLiteral)) {
					return true;
				}
			}
			return false;
		}
		
		
		/*
		 * findUnitClause() is a helper function for the BCP()-method.
		 * It checks for unit clauses, which are (due to our implementation) clauses with a single literal. 
		 * We look for clauses of size 2 since all clauses contain a 0 as a clause delimiter, which leaves
		 * unit clauses to have a 2nd element in addition to the unit literal. 
		 * 
		 * We return true if a unit clause is found otherwise, we exit the method with false which is equivalent 
		 * to the fact that no further unit clause was found.
		 * 
		 */
		static boolean findUnitClause() { 
			for(int i = 0; i < tmpClauseList.size(); i++) {
				ArrayList<Integer> tmpClause = tmpClauseList.get(i);
				if(tmpClause.size() == 2) { 
					
					unitLiteral = tmpClause.get(0);
					negatedUnitLiteral = -unitLiteral;
					unitClause = tmpClause; 
					
					antecedentPointer = tmpClauseList.indexOf((Object) unitClause); 
					antecedent = tmpClauseList.get(antecedentPointer);
					
					tmpClauseList.remove((Object) unitClause);
					
					
					for(int j = 0; j < tmpClauseList.size(); j++) {
						ArrayList<Integer> tmpoClause = tmpClauseList.get(j);			
					    if(tmpoClause.contains(unitLiteral)) {
					    	tmpClauseList.remove(tmpoClause);
					    	j--;
					    	}
					    	if(tmpoClause.contains((Object) negatedUnitLiteral)) {
					    		tmpoClause.remove((Object) negatedUnitLiteral);
					    	}
					}
					return true;
				}
			} 
			return false; 
		}
			
		
		
		/*
		 * BCP()-method
		 * 
		 * One trail entry gained from BCP() consists of a list with 3 entries:
		 * The 1st entry is the literal that made a clause unit.
		 * The 2nd entry is the assignment for this literal, which
		 * is true due otherwise the unit clause cannot be satisfied.
		 * The 3rd entry holds the reason for that assignment, which is
		 * the unit clause itself.
		 * 
		 * We agree on the permanent assignment of true (= 1) for the literals that are pushed to the trail.
		 * 
		 */
		public static boolean BCP(boolean writeConflictToFile) {
			while(findUnitClause() == true) {
				Object[] trailEntry = {unitLiteral,1,antecedent}; 
				ArrayList<Object> trailEntryList = new ArrayList<>(Arrays.asList(trailEntry));
				trail.push(trailEntryList); 
				int nextUnitLiteral = Math.abs(unitLiteral);
				tmpVars.remove((Object) nextUnitLiteral);
				
				/*
				 * If we reached this point we've encountered a conflict, this is represented
				 * by the tmpClauseList (which get's reduced every time an assignment is made)
				 * that contains a list entry that only contains the value 0. This means that there
				 * is a clause which is only left with the clause delimiter, which only 
				 * can happen, if all literals in it have been removed.
				 * This is mimicking the assignment to the value false to every literal contained in the clause
				 * this means that with this clause we get the conflicting clause.
				 * 
				 */
				if((((tmpClauseList.size() == 1) && (tmpClauseList.get(0).get(0) == 0)) && tmpVars.isEmpty()) || tmpClauseList.contains((Object) confZeroClause)) { 
					System.out.println(" ");
					//Method call that prints the trail to the console
					//printTrail(trail);
					//Parse trail to lateX code 
					//trailAsString = trailToLateX(trail); 
					//System.out.println("CONFLICT");
					
					
					if(conflictOutput == false && writeConflictToFile) {
						//We set the conflict flag to true since a conflict occurred
						conflictOutput = true;
						//Here the conflict formula gets stored into the (already existing) file ConflictTestDatei.txt
						String fileName = "ConflictFormulaDictionary.txt";
						try(FileWriter fileWriter = new FileWriter(fileName, true);
								PrintWriter printWriter = new PrintWriter(fileWriter);) {
							  printWriter.println(deepCopyOfClauseList);
						}catch(IOException e) {
							System.out.println("ERROR" + e);
						}
					}
					return false;
				} 
			}
			return true;
		}
		
		
		/*
		 * The binaryResolution(ArrayList<Integer> conflicting_clause, ArrayList<Integer> reason, int variable)-method
		 * performs resolution after we've detected a conflict. It takes two clauses as input and a variable, the clauses are then 
		 * resolved with respect to this variable.
		 * The method gets (under some conditions/criteria repeatedly) called in the conflictAnalysis()-method
		 * 
		 */
		@SuppressWarnings("unchecked")
		public static ArrayList<Integer> binaryResolution(ArrayList<Integer> conflicting_clause, ArrayList<Integer> reason, int variable) {
			//System.out.print("This is the resolvent of " + conflicting_clause + " and " + reason + " after eliminating " + Math.abs(variable));
			listOfResolutionClauses.add(conflicting_clause);
			listOfResolutionClauses.add((ArrayList<Integer>) reason.clone());
			listOfResolutionVariables.add(Math.abs(variable));
			
			//System.out.println(listOfResolutionClauses);
			//System.out.println(listOfResolutionVariables);
			
			int negatedVariable = -variable;
			for(Integer lit : conflicting_clause){
				   if (!(reason.contains((Object) lit)) && ((lit != variable) && (lit != negatedVariable))) {
					    reason.add(0, lit);
				   }
				}
			 	if(reason.contains((Object) variable)) {
			 		reason.remove((Object) variable);
			   }else if(reason.contains((Object) negatedVariable)) {
				   reason.remove((Object) negatedVariable);
			   } 
			 	
			 	resolvent = new ArrayList<Integer>(reason.size());
			 	  for(Integer inte : reason) {
				        resolvent.add(inte);
				    }
			 	 //RESOLVENT(S)
			return resolvent;	
		}
		
		
	
		/* 
		 * The method isAsserting(ArrayList<Integer> currClause) checks if the input Clause currClause is asserting.
		 * Therefore we search in the trail if there is a match between the variable of the current literal (varOfLit)
		 * and the variables of the literals contained in the trail. 
		 * With cnt we remember how many matches we've encountered. Every time there is a match we increment cnt by 1. 
		 * If we counted more than one match the current clause is not asserting and we return false, we return true otherwise.
		 * The condition in the while loop makes sure we do not surpass the current decision level. This is ensured by 
		 * the fact that we only loop as long as we find a decision level delimiter, represented by null entries in the second position
		 * of a trail element, that stored null as reason for a literal assigned through the decision method decision()
		 * (decision -> antecedent equals nil/null).
		 * 
		 * conflictingClauseIndex is the position of the conflicting clause gained from binary resolution in the conflict Analysis.
		 * It is incremented only for the purpose of using fresh conflicting clause names during conflict resolution, where every produced conflicting clause gets 
		 * the name c_i where i is increasing with the number of resolution steps
		 * 
		 */
		static boolean isAsserting(ArrayList<Integer> currClause) {
			
			int cnt = 0;
			int i = trail.size()-1;
			
			
				 while(trail.get(i).get(2) != null) {
					i--;
					for(Integer lit : currClause) {
					varOfLit = Math.abs(lit);
					if(Math.abs((int) trail.get(i).get(0)) == varOfLit) {
						cnt++;
					}else{
						if(lit != 0) {
						firstUIP = lit; //The first UIP is actually a positive node but for implementational reasons (see backtracking) it can be < 0 here
						}
					}
				}	
			} 
				if(Math.abs((int) trail.get(i).get(0)) == varOfLit) {
						cnt++;
				} 
			conflictingClause = currClause;
			conflictingClauseIndex++;
			
			return (cnt > 1 ? false : true);
	}
		
		
		@SuppressWarnings("unchecked")
		public static boolean conflictAnalysis() {
			
			//Parse the initial trail (before backtracking) for the exercise into Latex code 
			for(int i = 0; i < trail.size(); i++) {
				initialTrail.add((ArrayList<Object>) trail.get(i).clone());
			}
			trailAsString = trailToLateX(initialTrail);
			
			conflict = true; 
			
			
			
			//We want to recognize formulas that produce conflicts at DL0 already, by this flag
			if(decisionLevel == 0) {
				conflict = true;
				return false; 
			}
			
			/*
			 * In the 1st for loop we determine the conflicting clause and its index/position in the clause list
			 * 
			 */
			for(int i = 0; i < clauseList.size(); i++) {
				if(clauseList.get(i).size() == 1 && !clauseList.get(i).isEmpty()) {
					
					conflictingClause = new ArrayList<Integer>(deepCopyOfClauseList.get(clauseList.indexOf((Object) clauseList.get(i))).size());
				 	  for(Integer inte : deepCopyOfClauseList.get(clauseList.indexOf((Object) clauseList.get(i)))) {
				 		 conflictingClause.add(inte);
					    }
					conflictingClauseIndex = clauseList.indexOf(clauseList.get(i));
				}
			}			
			
			//Add the index for the conflicting clause to the conflictingClauseIndexForTask List
			conflictingClauseIndexForTask.add(conflictingClauseIndex);
			cnfClauseForTask = "c_" + conflictingClauseIndexForTask.get(0);
				
				//Configuration for the 1st Resolution Step:
				int j = trail.size()-2; 
				int k = j+1;
				
				
				currConflictClause = new ArrayList<Integer>(deepCopyOfClauseList.get(clauseList.indexOf(trail.get(k).get(2))).size());
			 	  for(Integer intVal : deepCopyOfClauseList.get(clauseList.indexOf(trail.get(k).get(2)))) {
			 		 currConflictClause.add(intVal);
				    }
			 	  
			 	
				lastAssignedLit = (int) trail.get(k).get(0);
				
				//FIRST RESOLUTION STEP
				//Do one resolution step and then check if the resulting clause is asserting
				listOfResolutionClauses.add(binaryResolution(conflictingClause, currConflictClause, lastAssignedLit));
				
				while(!isAsserting(resolvent)) {			
					
					currConflictClause = new ArrayList<Integer>(deepCopyOfClauseList.get(clauseList.indexOf(trail.get(j).get(2))).size());
				 	  for(Integer intVAL : deepCopyOfClauseList.get(clauseList.indexOf(trail.get(j).get(2)))) {
				 		 currConflictClause.add(intVAL);
					    }
						
				 	  
						lastAssignedLit = (int) trail.get(j).get(0);
						listOfResolutionClauses.add(binaryResolution(conflictingClause, currConflictClause, lastAssignedLit));
						//binaryResolution(conflictingClause, currConflictClause, lastAssignedLit);
						j--;			
					} 
			
				
			//Copy resolvent into the new variable assertingClause	  
			assertingClause = new ArrayList<Integer>(resolvent.size());
			 	  for(Integer intVAL : resolvent) {
			 		 assertingClause.add(intVAL);
				    }	
			 
			 	getResolvent(listOfResolutionClauses);
			
			//tmp counts the null entries in the trail, since they denote a new decision level (decision -> antecedent equals nil/null).
			int tmp = 0;
			int d = trail.size()-1;
			int removalPosition = 1;
			
			/* ========================================================= BACKTRACKING ========================================================================
			 * (0) Prepocessing
			 * 
			 * First determine if backtracking is possible, which is when the decision level is larger than 0, return false otherwise.
			 * If backtracking is possible we start with by removing all assignments in the current decision level, since for the "asserting"-criterion
			 * to be met we only look in the decision level "above" the current one and after backtracking the current decision level always gets erased 
			 * 
			 */
			while(trail.get(d).get(2) != null) {
				trail.remove((Object) trail.get(d));
				d--;
			}
			if(trail.get(d).get(2) == null) {
				tmp++;
				trail.remove((Object) trail.get(d));
			}
			

			
			
			/*
			 * (1) Determine the Backtracking Level
			 * 
			 * Here we want to determine the decision level we want to backtrack to.
			 * Therefore we search the rest of the trail (from the current decision level upwards)
			 * We compare either the literal in its pure form or its variable with the literals contained 
			 * in the trail, because we want to backtrack in the 2nd highest decision level "in" the asserting
			 * clause, for which we want to find a representation of one of our literals in the asserting clause in the trail.
			 * 
			 * With tmp we remember how many decision levels we've passed so far by counting the null entries we are visiting.
			 * Every time we see a null we increment tmp by 1. 
			 * The removalPosition marks the position we've found that matching variable. Now we reference the decision level we've
			 * found that matching literal in by removalPosition. In the 1st for loop for this purpose 
			 * we are comparing with the entry "null" in the second position of every trail element, since the null entries mark a decision level
			 * (decision -> antecedent equals nil/null).
			 * If the trail element that contains our matching literal has a neighbouring element to the left that contains a null at the 2nd position
			 * we know that that adjacent element marks a decision level above the decision level of our matching literal and therefore needs to be removed 
			 * which is done in the next for loop.
			 * 
			 */
			for(int p = trail.size()-1; p >= 0; p--) {
				for(int el: assertingClause) {
					if(((int) trail.get(p).get(0) == el) || ((int) trail.get(p).get(0) == -el)) {
							removalPosition = p;
							break;
							} 
						}
					}
					
			
			
			//TODO
			/*
			 *
			 * 
			 * (2) Backtrack
			 * 
			 * For the case that we backtrack to decision level 0 we simply remove 
			 * every decision level below it
			 * 
			 * Otherwise In the following for loop we remove from removalPosition onwards every entry in the trail
			 * after we have determined the decision level we want to backtrack to. 
			 * 
			 */
			//TODO
			//CASE: Backtrack to DL0 faulty
			
			if((trail.size() == 2) && (trail.get(1).get(2) == null)) { 
				trail.remove(1);										
				tmp++;
				}else{
			for(int q = removalPosition; q >= 0; q++) {
			 if(q == trail.size()-1) { 
				 break;
			 }else if((q > removalPosition) && !(trail.size()-1 < q)) {
					trail.subList(q, trail.size()-1).clear();
			 		}else{
			 			break;
			 		}
				}
			}
			
	
			
			
			/*
			 * We've incremented tmp every time we removed a decision level
			 * while backtracking therefore we need to subtract that from the current one
			 */
			decisionLevel = decisionLevel - tmp; 
			
			backtrackLevel = decisionLevel;
			
			
			//System.out.println(decisionLevel);
			//System.out.println(" ");
			//System.out.println("This is the removal position " + removalPosition);
			//System.out.println("We backtrack to Decision Level " + backtrackLevel);
			//System.out.println( "In total we need to remove " + tmp + " decision level(s) after backtracking");
			
			/*=======================================================================================================================*/
			
			int var = -firstUIP;
			
			//ASSERTING CLAUSE
			//System.out.println("This is the asserting Clause " + assertingClause);
			
			/*
			 * We add the asserting clause to our input formula
			 * 
			 * (We add resolvent and not assertingClause, because we want to have it appended to the original formula 
			 * in it's "pure" form (without any assignments made to it))
			 * 
			 */
			deepCopyOfClauseList.add(resolvent); 
			
			//Store the entries of deepCopyOfClauseList in tmpClauseList
			tmpClauseList = new ArrayList<ArrayList<Integer>>();
		 	  for(ArrayList<Integer> arrValue : deepCopyOfClauseList) {
		 		 tmpClauseList.add(arrValue);
			    }  
		 	  
			//Remove var (negation of the first UIP) from the asserting clause since it is already being assigned
			assertingClause.remove((Object) firstUIP);
			
			//System.out.println("this is first UIP " + firstUIP);
			
			/*
			 * In order to mimic the assignment from the first SAT solving iteration before we
			 * entered the conflict Analysis we need to remove the already assigned variable in the asserting clause (firstUIP)
			 * in every clause it appears, since it cannot contribute to the satisfiability 
			 * of the asserting clause. And we also need to remove every clause entirely that contains its negation
			 * (var) since the clause is then already satisfied. 
			 * 
			 */
			for(int c = 0; c < tmpClauseList.size(); c++) {
				ArrayList<Integer> tmporalCl = tmpClauseList.get(c); 
				if(tmporalCl.contains((Object) firstUIP)) {
					tmporalCl.remove((Object) firstUIP);
					c--;
					}
				if(tmporalCl.contains((Object) var)) {
					tmpClauseList.remove((Object) tmporalCl);
					c--;
				}
				
			}			
			
			/*
			 * We want to make sure we do not have a double entry in the trail,
			 * after backtracking to a decision level with more than just the variable that makes the
			 * asserting clause asserting on it 
			 * 
			 */
			ArrayList<ArrayList<Integer>> antec = new ArrayList<ArrayList<Integer>>();
			for(int v = 0; v < trail.size(); v++) {
					antec.add((ArrayList<Integer>) trail.get(v).get(2));
			}
			
			tmpClauseList.removeAll(antec);
			
			
			/*
			 * Replace the last element of tmpClauseList by the modified asserting clause after we removed var, 
			 * to ensure we work with a clause whose literals are correctly assigned
			 * 
			 */
			tmpClauseList.set(tmpClauseList.size()-1, assertingClause);
		 	
			
			
			
			/*
			 * We want to make sure that we add a supposed unit clause (here the assertingClause under the current assignment)
			 * to our clauseList which holds the current unitClauses for later reference.
			 * 
			 */
			clauseList.add(assertingClause);
			
			/*
			 * In order the decide()-method to be invoked after a conflict we need to refill the list 
			 * that holds the variables that are not yet assigned (tmpVars).
			 * Because the elements in tmpVars have been successively removed after every assignment made
			 * up to the point it gets emptied (which is the termination condition in the decide()-method).
			 * 
			 * We refill that list with the variables that are not contained in the current trail. 
			 * In order to determine those variables we extract the literals contained in the current trail, 
			 * which are then stored in the list tmpTrailLitList.
			 * 
			 * We then remove those variables from the original variable list, that holds all
			 * variables that are contained in the formula (variables)
			 * 
			 * Finally we add those variables in tmpVars which was empty due to the preceded
			 * SAT solving procedure  
			 * 
			 */
			ArrayList<Integer> tmpTrailLitList = new ArrayList<Integer>();
			for(int i = 0; i < trail.size(); i++) {
				tmpTrailLitList.add(Math.abs((int) trail.get(i).get(0)));
			}
			
			//Copy the variable list
			ArrayList<Integer> temporaryCopyOfvariables = new ArrayList<Integer>();
			for(int f = 0; f < variables.size(); f++) {
				temporaryCopyOfvariables.add(variables.get(f));
			}
			
			//Remove all elements from tmpTrailLitList that are contained in temporaryCopyOfvariables
			temporaryCopyOfvariables.removeAll(tmpTrailLitList);
			
			
			
			//Refill tmpVars with the variables that aren't assigned yet and sort the list
			for(int g = 0; g < temporaryCopyOfvariables.size(); g++) {
				tmpVars.add(temporaryCopyOfvariables.get(g));
			}
			
			//We need to make sure that tmpVars is sorted, because we agreed on choosing the smallest variable for the next decision 
			Collections.sort(tmpVars);
			
		
			return true;
		} 
	
		/*
		 * 
		 * Additional parameter writeConflictToFile as flag that
		 * ensures that only the function call of DPLL_CDSLalgo is able
		 * to write new conflict formulas into formula dictionary and not
		 * the function call in TeXgenerator
		 */
		 static boolean DPLL_CDCLalgo(ArrayList<ArrayList<Integer>> formula, boolean writeConflictToFile) { 
			if(!BCP(writeConflictToFile)) {
				return false;
			}
			while(true) {
				if(!decide()) {
					return true;
				}
				while (!BCP(writeConflictToFile)) {
					if(!conflictAnalysis()) {
						return false;
					}
				}
			}
		} 
		
		 
		 
		 
		/*
		 * Method that prints the assignment trail to the console
		 * We reference clauseList here because it holds all our unit clauses
		 * 
		 */
		public static void printTrail(Stack<ArrayList<Object>> currTrail) {
			int dlCnt = 0;
			int i = 0;
			if(!(currTrail.get(0).get(2) == null)) {
				System.out.print("DL0: " + currTrail.get(0).get(0) + ":" + "c_" + clauseList.indexOf((Object) currTrail.get(0).get(2)) + " ");
				i++;
			}else {
				System.out.print("DL0:-");
			}
			while(i < currTrail.size()) {
				if(currTrail.get(i).get(2) == null) {
					dlCnt++;
					System.out.println("");
					System.out.print("DL" + dlCnt + ":" + currTrail.get(i).get(0) + ":" + "nil");
				}else{
					if(!(clauseList.indexOf((Object) currTrail.get(i).get(2)) == -1)) {
						System.out.print(", " + currTrail.get(i).get(0) + ":" + "c_" + clauseList.indexOf((Object) currTrail.get(i).get(2)));
					}else{
						System.out.print(", " + currTrail.get(i).get(0) + ":" + "c_" + deepCopyOfClauseList.indexOf((Object) currTrail.get(i).get(2)));
					}	
				}
				i++;
			}	
			System.out.println(" ");
		}
		
		
		 // \\
		//$DL0: -$ \\
		//$DL1: \neg A:nil\ E: c_{0}$\\
		//$DL2: B:nil\ C:c_{1}\ D: c_{2}$ 
		//\\ \\
		
		/*
		 * Helper method that converts trail to latex format
		 * We reference clauseList here because it holds all our unit clauses
		 * 
		 */
		static String literalToChar(int lit) {
			String literalToChar = "";
			if(lit < 0) {
				literalToChar += "\\neg ";
		 	}
			switch(Math.abs(lit)) {
	 		case 1:
	 			literalToChar += "A ";
	 			break;
	 		case 2: 
	 			literalToChar += "B ";
	 			break;
	 		case 3: 
	 			literalToChar += "C ";
	 			break;
	 		case 4: 
	 			literalToChar += "D ";
	 			break;
	 		case 5: 
	 			literalToChar += "E ";
	 			break;
			}
			return literalToChar;
		}
		
		static String trailToLateX(Stack<ArrayList<Object>> currentTrail) {
			
			String latexTrail = "";
			int dlCnt = 0;
			int i = 0;
			if(!(currentTrail.get(0).get(2) == null)) {
				latexTrail += "$DL0: " + literalToChar((int) currentTrail.get(0).get(0)) + ":" + "c_" + "{" + clauseList.indexOf((Object) currentTrail.get(0).get(2)) + "}";
				i++;
			}else {
				latexTrail += "$DL0: -";
			}
			while(i < currentTrail.size()) {
				if(currentTrail.get(i).get(2) == null) {
					dlCnt++;
					latexTrail += "$\\\\ $DL" + dlCnt + ":" + literalToChar((int) currentTrail.get(i).get(0)) + ":" + "nil";
				}else{
					if(!(clauseList.indexOf((Object) currentTrail.get(i).get(2)) == -1)) {
						latexTrail += ", " + literalToChar((int) currentTrail.get(i).get(0)) + ":" + "c_" + "{" + clauseList.indexOf((Object) currentTrail.get(i).get(2)) + "}";
					}else{
						latexTrail += ", " + literalToChar((int) currentTrail.get(i).get(0)) + ":" + "c_" + "{" + deepCopyOfClauseList.indexOf((Object) currentTrail.get(i).get(2)) + "}";
					}	
				}
				i++;
			}	
			latexTrail += "$";
			return latexTrail;
		}
			
		
		//Method to retrieve solution from binary resolution task
		@SuppressWarnings("unchecked")
		public static ArrayList<ArrayList<Integer>> getResolvent(ArrayList<ArrayList<Integer>> listFromResolution) {
			for(int i = 0; i < listOfResolutionClauses.size(); i++) {
				if(listOfResolutionClauses.size() == 3) {
					//for(int k = 0; k < listOfResolutionClauses.size(); k++) {
						//resolutionSolutionList.add((ArrayList<Integer>) listOfResolutionClauses.get(k).clone());
						resolutionSolutionList.add((ArrayList<Integer>) listOfResolutionClauses.get(2).clone());
						break;	
					//} 	
				}
				else if(((i % 3) == 0) && (i > 0)) {
					resolutionSolutionList.add((ArrayList<Integer>) listOfResolutionClauses.get(i-1).clone());
				}else if(i == listOfResolutionClauses.size()-1) {
					resolutionSolutionList.add((ArrayList<Integer>) listOfResolutionClauses.get(i).clone());
				}
			}
			return resolutionSolutionList;
		}
		
	
		
		
		public static void main(String[] args) {
			// TODO Auto-generated method stub
			 
			 
			 /*
			  * In order to detect a conflict we need to check if tmpClauseList contains an entry with only 0 in it  
			  * and therefore define that list (confZeroClause) to use it for later comparison
			  * 
			  */
			 confZeroClause.add(0);
		
		
		 
			 
		
		int numberOfConflictsGenerated = 0;	 
		while(numberOfConflictsGenerated <= TeXgenerator.numberOfConflictsToGenerate) {
			conflictOutput = false;
			decisionLevel = 0;
			variables.clear();
			tmpVars.clear();
			trail.clear();
			if(tmpClauseList != null) {
				tmpClauseList.clear();
			}
			if(deepCopyOfClauseList != null) {
				deepCopyOfClauseList.clear();
			}
			clauseList.clear(); 
			//Generate the formula (clauseList) (!!!!)
		 	formulaGenerator();
			
			//System.out.println("Test formula: " + clauseList);
			 
			 
			//Print the list of variables extracted from the List of clauses 
			collectVariables(clauseList);
			 
			 
			
			//Creates a "shallow" copy of clauseList (the input formula)
			copyFormula();
			 
			
			//Creates a "deep" copy of clauseList (the input formula) (BUT which gets modified in the conflictAnalysis, this is a PROBLEM!!!)  (!!!!!)
			deepCopyFormula(clauseList);
			
			
			
			//Copy the List of Variables 
			copyVariables();
			 
			
			/*
			 * Feed the input formula into the DPLL+CDCL SAT solver along with the
			 * flag for writeConflictToFile set to true, to catch conflict formulas,
			 * that are being produced in this class (and not from TeXgenerator)
			 * 
			 */
			DPLL_CDCLalgo(clauseList, true);
			
			
			 if(conflict != true) {
				continue;
			} 
			numberOfConflictsGenerated++; 
			
			//Prints the (final) trail to the console
			 //printTrail(trail); 
			
			
			
				}	 
		
		System.out.println("Have fun! :)");
		
			}
		
			
	 


}
