# SATcheckExercises

An exercise generator that creates exercises to practice conflict resolution within DPLL+CDCL SAT solving. 

## Technical details

The exercise generator was written entirely in the Java programming language. The Java version used during time of implementation is Java 17.


## How to generate exercises? 

### The download phase
First download the Conflict Resolution Folder, in which you find the following documents:

- SATsolver.java
- TeXgenerator.java
- ConflictFormulaDictionary.txt

If you choose not to download the Conflict Resolution folder but store the individual files to another location, then note that all 3 files need to be stored in the same project folder, in order for the exercise generator to work!

### Actual generation of exercises
Now to generate exercises compile and run TeXgenerator.java. A dialog box will open asking you to specify the number of exercises to generate. The generated Exercises will be automatically saved together with the Solution in the same location where the 3 files listed above were saved. 

The produced Exercises and Solution .tex files are named "ExerciseX" and "SolutionX" respectively, where X stands for the number of the Exercise. It should be emphasized here that a file name can only be assigned once. Generating an ExerciseX file where ExerciseX already exists in the project folder will overwrite the first ExerciseX.tex file and will not create a copy of it, so it may be helpful to rename or relocate the files after using the Exercise generator again.

To generate the latex file use e.g. Pdflatex or Kile. But note that you may have to install additional packages.
Otherwise there are many online converters available on the WWW which convert a .tex file into a .pdf file.

However if things just won't work for you, in the Exercise Sample folder, a generated exercise including solution has been provided in tex data format as well as PDF for reference.

 
