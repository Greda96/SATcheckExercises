import java.util.ArrayList;
import javax.swing.JOptionPane;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import static java.nio.file.StandardOpenOption.*;
import java.nio.file.*;
import java.io.OutputStream;
import java.io.BufferedOutputStream;



public class TeXgenerator {
	
	
	//Here the number of conflicts to generate can be specified 
	public static int numberOfConflictsToGenerate = 1;
	
	
	public static String exerciselatexTemplate = "\\documentclass[a4paper,11pt,fleqn]{article}\r\n"
			+ "\\usepackage[a4paper, left=2cm, right=2cm, top=2cm, bottom=3cm, headheight=14pt, headsep=1.2cm]{geometry}\r\n"
			+ "\r\n"
			+ "\\usepackage{mathrsfs}\r\n"
			+ "\\usepackage{amsmath}\r\n"
			+ "\\usepackage{mathtools}\r\n"
			+ "\\usepackage{amssymb}\r\n"
			+ "\\usepackage{microtype}\r\n"
			+ "\\usepackage{stmaryrd}\r\n"
			+ "\\usepackage{bussproofs}\r\n"
			+ "\\usepackage{yfonts}\r\n"
			+ "\\usepackage{multirow} \r\n"
			+ "\\usepackage[tikz]{bclogo}\r\n"
			+ "\\usepackage{etoolbox}\r\n"
			+ "\\usepackage{tcolorbox}\r\n"
			+ "\r\n"
			+ "\r\n"
			+ "\r\n"
			+ "\r\n"
			+ "\\begin{document}\r\n"
			+ "\\title{\\huge \\bfseries{SATcheck - Exercise\\#%1s}}\r\n"
			+ "\\author{\\textbf{Topic:} Conflict Resolution, DPLL+CDCL}\r\n"
			+ "\\date{\\scshape \\small RWTH Aachen University -- \\today \\smallskip \\vspace{1ex}\\hrule}\r\n"
			+ "\r\n"
			+ "\r\n"
			+ "\\maketitle\r\n"
			+ "\r\n"
			+ "\r\n"
			+ "\\newenvironment{mybclogo}[1]{\r\n"
			+ "\\begin{bclogo}[logo =\\bclampe , couleur=blue!10,arrondi=0.1, couleurBarre=white!10,marge=10,\r\n"
			+ "epBord=0.5,noborder=true,ombre=false]{#1}\r\n"
			+ "}{\r\n"
			+ "\\end{bclogo}\r\n"
			+ "}\r\n"
			+ "\r\n"
			+ "\\begin{tcolorbox}[width= 10cm, sharp corners, colback=white, colframe=blue!10, title=\\textcolor{black}{\\textbf{Exercise Timer}}]\r\n"
			+ "\\noindent \\begin{tabular}{|c|c|c|}\r\n"
			+ "\\hline\r\n"
			+ "Starting Time&End Time& Duration\\\\\r\n"
			+ "\\hline\r\n"
			+ "&&\\hspace{20pt} \\ldots min\\\\\r\n"
			+ "\\hline\r\n"
			+ "\\end{tabular}\r\n"
			+ "\\\\ \\\\ \\textit{(How long did the exercise take you?)}\r\n"
			+ "\\end{tcolorbox}\r\n"
			+ "\r\n"
			+ "\r\n"
			+ "\\section*{\\underline{Task}}\r\n"
			+ "Consider the following propositional logic formula in CNF: \\\\\r\n"
			+ "$%2s$ \\\\ \\\\\r\n"
			+ "Furthermore assume the following trail: \\\\\r\n"
			+ "%3s \\\\\r\n"
			+ "\\\\ \\\\\r\n"
			+ "We have encountered a conflict at the current decision level. Apply conflict resolution to $%4s$ \\textit{till the first unique implication point}. How many new clauses (i.e. clauses that are not already contained in the original formula), are generated during the whole resolution process? Write down the clauses in a row separated by a comma e.g.: clause1, clause2, \\ldots ,clauseN.\r\n"
			+ "\r\n"
			+ "\r\n"
			+ "\\begin{mybclogo}{Hint(s)}\r\n"
			+ "\\begin{itemize}\r\n"
			+ "\\item Read the task carefully.\r\n"
			+ "\\item Recall the definitions of the terms \\textit{conflicting clause}, \\textit{conflict clause}, \\textit{first unique implication point}.\r\n"
			+ "\\end{itemize}\r\n"
			+ "\\end{mybclogo}\r\n"
			+ "\r\n"
			+ "\\subsection*{Your solution goes here:}\r\n"
			+ "\r\n"
			+ "\\clearpage\r\n"
			+ "\\newpage\r\n"
			+ "\\mbox{~}\r\n"
			+ "\\subsection*{Place for scratchwork:} \\textit{(hopefully this is sufficient for you.)}\r\n"
			+ "\r\n"
			+ "\\end{document}\r\n"
			+ "";
	
		
	
	public static String solutionLatexTemplate = "\\documentclass[a4paper,11pt,fleqn]{article}\r\n"
			+ "\\usepackage[a4paper, left=2cm, right=2cm, top=2cm, bottom=3cm, headheight=14pt, headsep=1.2cm]{geometry}\r\n"
			+ "\r\n"
			+ "\\usepackage{mathrsfs}\r\n"
			+ "\\usepackage{amsmath}\r\n"
			+ "\\usepackage{mathtools}\r\n"
			+ "\\usepackage{amssymb}\r\n"
			+ "\\usepackage{microtype}\r\n"
			+ "\\usepackage{stmaryrd}\r\n"
			+ "\\usepackage{bussproofs}\r\n"
			+ "\\usepackage{yfonts}\r\n"
			+ "\\usepackage{multirow} \r\n"
			+ "\\usepackage[tikz]{bclogo}\r\n"
			+ "\\usepackage{etoolbox}\r\n"
			+ "\\usepackage{tcolorbox}\r\n"
			+ "\r\n"
			+ "\r\n"
			+ "\r\n"
			+ "\r\n"
			+ "\\begin{document}\r\n"
			+ "\\title{\\huge \\bfseries{SATcheck - Solution\\#%1s}}\r\n"
			+ "\\author{\\textbf{Topic:} Conflict Resolution, DPLL+CDCL}\r\n"
			+ "\\date{\\scshape \\small RWTH Aachen University -- \\today \\smallskip \\vspace{1ex}\\hrule}\r\n"
			+ "\r\n"
			+ "\r\n"
			+ "\\maketitle\r\n"
			+ "\r\n"
			+ "\r\n"
			+ "\\newenvironment{mybclogo}[1]{\r\n"
			+ "\\begin{bclogo}[logo =\\bclampe , couleur=blue!10,arrondi=0.1, couleurBarre=white!10,marge=10,\r\n"
			+ "epBord=0.5,noborder=true,ombre=false]{#1}\r\n"
			+ "}{\r\n"
			+ "\\end{bclogo}\r\n"
			+ "}\r\n"
			+ "\r\n"
			+ "\\begin{tcolorbox}[width= 10cm, sharp corners, colback=white, colframe=blue!10, title=\\textcolor{black}{\\textbf{Exercise Timer}}]\r\n"
			+ "\\noindent \\begin{tabular}{|c|c|c|}\r\n"
			+ "\\hline\r\n"
			+ "Starting Time&End Time& Duration\\\\\r\n"
			+ "\\hline\r\n"
			+ "&&\\hspace{20pt} \\ldots min\\\\\r\n"
			+ "\\hline\r\n"
			+ "\\end{tabular}\r\n"
			+ "\\\\ \\\\ \\textit{(How long did the exercise take you?)}\r\n"
			+ "\\end{tcolorbox}\r\n"
			+ "\r\n"
			+ "\r\n"
			+ "\\section*{\\underline{Task}}\r\n"
			+ "Consider the following propositional logic formula in CNF: \\\\\r\n"
			+ "$%2s$ \\\\ \\\\\r\n"
			+ "Furthermore assume the following trail: \\\\\r\n"
			+ "%3s \\\\\r\n"
			+ "\\\\ \\\\\r\n"
			+ "We have encountered a conflict at the current decision level. Apply conflict resolution to $%4s$ \\textit{till the first unique implication point}. How many new clauses (i.e. clauses that are not already contained in the original formula), are generated during the whole resolution process? Write down the clauses in a row separated by a comma e.g.: clause1, clause2, \\ldots ,clauseN.\r\n"
			+ "\r\n"
			+ "\r\n"
			+ "\\begin{mybclogo}{Hint(s)}\r\n"
			+ "\\begin{itemize}\r\n"
			+ "\\item Read the task carefully.\r\n"
			+ "\\item Recall the definitions of the terms \\textit{conflicting clause}, \\textit{conflict clause}, \\textit{first unique implication point}.\r\n"
			+ "\\end{itemize}\r\n"
			+ "\\end{mybclogo}\r\n"
			+ "\r\n"
			+ "\\subsection*{Your solution goes here:}\r\n"
			+ "%5s new clause(s) were produced during the whole resolution process. The clause(s) is/are given by: \\\\ $%5s$\r\n"
			+ "   \r\n"
			+ "\\end{document}\r\n"
			+ "";

	
	
	
	
	/*
	 * Helper method getLittleLists() removes commas from the elements in the String list,
	 * i.e. removes the commas from the "clauses".
	 * The elements contained in the list of Strings are then parsed into integer and stored into 
	 * a new list of the type ArrayList of Integers
	 * 
	 */
	public static ArrayList<Integer> getLittleLists(String intList) {
		ArrayList<Integer> littleList = new ArrayList<Integer>();
			String list[] = intList.split(", ?");
			for(String el : list ) {
				littleList.add(Integer.parseInt(el));
			}
		return littleList;
	}
	
	
	/*
	 * Helper method getList() removes the brackets "[" and "]" from the elements contained in the list of Strings
	 * are then reassembled into a new list, (which is now a ArrayList of ArrayList of Integers) after having
	 * removed also the commas (via getLittleLists() method) from the elements contained in the list, to form the original formula,
	 * which is now no longer in the array list notation.
	 * 
	 */
	public static ArrayList<ArrayList<Integer>> getList(String list) {
		ArrayList<ArrayList<Integer>> freshList = new ArrayList<ArrayList<Integer>>();
		for(String al : list.substring(2,list.length()-2).split("\\], ?\\[")) {
			freshList.add(getLittleLists(al));
		}
		return freshList;
	}
	
	public static String generateLatex(ArrayList<ArrayList<Integer>> formula) {
		//$c_0 : (A\vee E)\wedge c_1 : (\neg B\vee C)\wedge c_2 : (\neg B\vee \neg D)\wedge c_3 : (\neg C\vee D\vee E)\wedge c_4 : (\neg B\vee \neg C\vee D\vee \neg E)$
		String formulaConcat = "";
		int indexCnt = 0;
		boolean firstClause = true;
		for(ArrayList<Integer> clause : formula) {
		 	if(firstClause) {
		 		firstClause = false;
		 	}else{
		 		formulaConcat += "\\wedge ";
		 	}
			 formulaConcat += "c_" + indexCnt + " : (";
			 boolean firstLit = true;
			 for(Integer literal : clause) {
				 	if(firstLit) {
				 		firstLit = false;
				 	}else {
				 		formulaConcat += "\\vee ";
				 	}
				 	if(literal < 0) {
				 		formulaConcat += "\\neg ";
				 	}
				 	switch(Math.abs(literal)) {
				 		case 0:
				 			formulaConcat = formulaConcat.substring(0, formulaConcat.length()-6) + ")"; //truncates excessive \vee at the end plus the whitespace
				 			break;
				 			
				 		case 1: 
				 			formulaConcat += " A ";
				 			break;
				 		
				 		case 2: 
				 			formulaConcat += " B ";
				 			break;
				 			
				 		case 3: 
				 			formulaConcat += " C ";
				 			break;
				 			
				 		case 4: 
				 			formulaConcat += " D ";
				 			break;
				 			
				 		case 5: 
				 			formulaConcat += " E ";
				 			break;	
				 	}
			 }
			 indexCnt++;
		}
		return formulaConcat;
	}
	
	//Method to generate Latex output for raw solution of task (without scratchwork)
	public static String generateLatexResolventsList(ArrayList<ArrayList<Integer>> bunchOfresolvents) {
			String resolventsConcat ="";
			boolean firstClause = true;
			for(ArrayList<Integer> clause : bunchOfresolvents) {
			 	if(firstClause) {
			 		firstClause = false;
			 	}else{
			 		resolventsConcat += ", ";
			 	}
			 	resolventsConcat += "(";
			 boolean firstLit = true;
			 for(Integer literal : clause) {
				 	if(firstLit) {
				 		firstLit = false;
				 	}else {
				 		resolventsConcat += "\\vee ";
				 	}
				 	if(literal < 0) {
				 		resolventsConcat += "\\neg ";
				 	}
			switch(Math.abs(literal)) {
	 		case 0:
	 			resolventsConcat = resolventsConcat.substring(0, resolventsConcat.length()-6) + ")"; //truncates excessive \vee at the end plus the whitespace
	 			break;
	 			
	 		case 1: 
	 			resolventsConcat += " A ";
	 			break;
	 		
	 		case 2: 
	 			resolventsConcat += " B ";
	 			break;
	 			
	 		case 3: 
	 			resolventsConcat += " C ";
	 			break;
	 			
	 		case 4: 
	 			resolventsConcat += " D ";
	 			break;
	 			
	 		case 5: 
	 			resolventsConcat += " E ";
	 			break;	
						}
			 	}	
		}
	return resolventsConcat;
}
	
	
	
	
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
	
	
	int numberOfConflictsToGenerate = Integer.parseInt(JOptionPane.showInputDialog(null, "How many exercises do you want to generate?", "SATcheck", JOptionPane.QUESTION_MESSAGE));
		
	//Reads (string) lines from the the file passed as argument to FileReader(...)
	BufferedReader formulafReader = new BufferedReader(new FileReader("ConflictFormulaDictionary.txt"));	
	ArrayList<ArrayList<ArrayList<Integer>>> listOfFormulas = new ArrayList<>();
	
	/*
	 * Passing the lines from the input file, containing string representation of the list that holds
	 * all formulas and storing them into a new list (here freshList of type ArrayList<ArrayList<ArrayList<Integer>>>), 
	 * after having removed brackets and commas from it (see getList()).
	 * Then we take a single element from that list, which is a "cleaned" formula (no commas, no brackets)
	 * We then parse our formula to Latex code (via helper method generateLatex())
	 * and append it to our latex template, by replacing the string specifier %s by the formula (see String.format(...)).
	 *  
	 */
	ArrayList<ArrayList<Integer>> testFormel = new ArrayList<ArrayList<Integer>>() ;
	int i = 0;
	int j = 0;
	String strToIntFormula = formulafReader.readLine();
	System.out.println(strToIntFormula);
	while(i < numberOfConflictsToGenerate) { 
		//Clear all lists, to avoid overrides in next iteration
		SATsolver.clauseList.clear();
		testFormel.clear();
		SATsolver.confZeroClause.clear();
		SATsolver.trail.clear();
		SATsolver.initialTrail.clear();
		SATsolver.variables.clear();
		SATsolver.tmpVars.clear();
		SATsolver.listOfResolutionClauses.clear();
		SATsolver.listOfResolutionVariables.clear();
		SATsolver.resolutionSolutionList.clear();
		SATsolver.trailAsString = "";
		SATsolver.conflictingClauseIndexForTask.clear();
		SATsolver.cnfClauseForTask = "";
		if(SATsolver.tmpClauseList != null) {
			SATsolver.tmpClauseList.clear();
		}
		if(SATsolver.deepCopyOfClauseList != null) {
			SATsolver.deepCopyOfClauseList.clear();
		}
		
		
		listOfFormulas.add(getList(strToIntFormula));
		ArrayList<ArrayList<Integer>> holdIntFormula = getList(strToIntFormula);
		
		//Properly deep copy the holdIntFormula by adding cloned elements to new list
		//otherwise holdIntFormula gets overridden by SAT.clauseList
		for(ArrayList<Integer> el : holdIntFormula) {
			testFormel.add((ArrayList<Integer>) el.clone());
		}
		
		
		strToIntFormula = formulafReader.readLine(); //get the next formula
		
		//System.out.println(testFormel);
		
		for(int k = 0; k < testFormel.size(); k++) {
			SATsolver.clauseList.add(testFormel.get(k));
		}

		
		 /*
		  * In order to detect a conflict we need to check if tmpClauseList contains an entry with only 0 in it  
		  * and therefore define that list (confZeroClause) to use it for later comparison
		  * 
		  */
		
		//System.out.println("Test formula: " + SATsolver.clauseList);
		 
		 
		//Print the list of variables extracted from the List of clauses 
		 SATsolver.collectVariables(SATsolver.clauseList);
		 
		 
		
		//Creates a "shallow" copy of clauseList (the input formula)
		SATsolver.copyFormula();
		 
		
		//Creates a "deep" copy of clauseList (the input formula) 
		SATsolver.deepCopyFormula(SATsolver.clauseList);
		
		
		//Copy the List of Variables 
		SATsolver.copyVariables();
		
		/*
		 * We call DPLL_CDCLalgo on formulas contained in our .txt file containing the conflict formulas,
		 * that are all generated in the SATsolver class, in order to then create Latex files for every such 
		 * formula.
		 */
		SATsolver.DPLL_CDCLalgo(SATsolver.clauseList, false);
		
		
		//Skip every formula where the trail appears to be empty (e.g. due to conflict at DL0), and therefore don't produce Latex file for it!!
		if(SATsolver.trailAsString.isEmpty()) {
			continue;
		}
		
		
		
		//Replace format specifier in latexTemplate by Exercise number, the formula, the trail and the label ("c_i") of the conflicting clause 
		String exercise = String.format(exerciselatexTemplate,i, generateLatex(holdIntFormula), SATsolver.trailAsString, SATsolver.cnfClauseForTask);
	    byte data[] = exercise.getBytes();
	    Path p = Paths.get("./Exercise" + i + ".tex");
	    try (OutputStream out = new BufferedOutputStream(
	      Files.newOutputStream(p, CREATE, APPEND))) {
	      out.write(data, 0, data.length);
	    } catch (IOException x) {
	      System.err.println(x);
	    	}
		i++;
		
		//Replace format specifier in latexTemplate by Exercise number, the formula, the trail, the label ("c_i") of the conflicting clause, the formula, the trail, number of clauses contained in solution of resolution, clauses contained in solution of resolution
		String solution = String.format(solutionLatexTemplate,j, generateLatex(holdIntFormula), SATsolver.trailAsString, SATsolver.cnfClauseForTask,SATsolver.resolutionSolutionList.size() ,generateLatexResolventsList(getList(SATsolver.resolutionSolutionList.toString())));
		byte dataSol [] = solution.getBytes();
		Path sol = Paths.get("./Solution" + j + ".tex");
		 try (OutputStream out = new BufferedOutputStream(
			      Files.newOutputStream(sol, CREATE, APPEND))) {
			      out.write(dataSol, 0, dataSol.length);
			    } catch (IOException y) {
			      System.err.println(y);
			    	}
				j++;
	}
	
	formulafReader.close();
	
	
	
	
	
	
	
	
	
	
	}
	
}


