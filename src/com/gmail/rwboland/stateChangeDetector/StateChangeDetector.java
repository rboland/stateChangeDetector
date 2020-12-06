/**
Background:
Amazon Device Security needs to know if a state change of device should trigger black/white listing.

1. If a device's start state is not "OUT_OF_USE" but end state is "OUT_OF_USE",
we should put the DSN to blacklist section in the output file.

2. If a device's start state is "OUT_OF_USE" but end state is not "OUT_OF_USE",
we should put the DSN to whitelist section in the output file.

 
Input log file looks like this:

Explanation:
{datetime} {dsn} {old_state} -> {new_state} means state transition for the dsn, at that datetime.


--------- Input File Starts ------------------------------------

2020-06-15T20:00:44,318 DSN001 IN_USE -> OUT_OF_USE

2020-06-15T20:23:06,840 DSN001 OUT_OF_USE -> IN_STORAGE

2020-06-15T20:23:35,019 DSN001 IN_STORAGE -> OUT_OF_USE

2020-06-15T20:23:06,840 DSN002 OUT_OF_USE -> IN_STORAGE

2020-06-15T20:00:44,318 DSN003 IN_USE -> OUT_OF_USE

2020-06-15T20:00:44,318 DSN004 IN_USE -> OUT_OF_USE

2020-06-15T20:23:06,840 DSN004 OUT_OF_USE -> IN_STORAGE

--------- Input File Ends -------------------------------------- 

We want to generate an output file like this:

--------- Output File Starts ------------------------------------

DSNs needing blacklist:

DSN001
DSN003

 

DSNs needing whitelist:

DSN002

--------- Output File Ends --------------------------------------

 

Explanation:

DSN001's start state is IN_USE and end state is OUT_OF_USE, so needs to be blacklisted
DSN003's start state is IN_USE and end state is OUT_OF_USE, so needs to be blacklisted
DSN002's start state is OUT_OF_USE and end state is IN_STORAGE, so needs to be whitelisted
DSN004's start state is IN_USE and end state is IN_STORAGE, so no actions needed.

** Note:
1. timestamps in the input file will NOT be in order.
2. for any given DSN, you can assume there will not be duplicate timestamps.
 
Requirement:

Please write only one function to solve this problem and focus on optimizing your solution in terms of Time/Space Complexity.
 */
package com.gmail.rwboland.stateChangeDetector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author rwboland@gmail.com
 *
 */
public class StateChangeDetector {

	/**
	 * @param args The input log filename.
	 * @throws IOException, ArrayIndexOutOfBoundsException 
	 */
	public static void main(String[] args) throws IOException {
		// Read the file into an List of lines.
		ArrayList<String[]> splitLines = new ArrayList<String[]>();
		String line;
		String[] splitLine;
		try {
			File logFile=new File(args[0]);  
			FileReader fr=new FileReader(logFile);
			BufferedReader br=new BufferedReader(fr); 
			
			/* Now, let's split each line into fields. The example file shows 
			 * double-spacing, or empty lines, so we'll skip those.  We'll also
			 * at least try to deal with lines that don't follow the spec.
			 */
			while ((line=br.readLine())!=null){
				if (!line.trim().equals("")){
					splitLine = line.split(" ");
					if (splitLine.length == 5) {
						splitLines.add(splitLine);
					} else {
						System.err.println("Line " + line + "not processed.");
					}
				}
			}
			br.close();
		}
		catch(FileNotFoundException e) {
			System.err.println("File " + args[0] + " not found.");
		}
		catch(IOException e) {
			System.err.println("Error opening File " + args[0] );
			throw e;	// Exit on exception
		}
		catch(java.lang.ArrayIndexOutOfBoundsException e) {
			System.err.println("Usage:  java StatChangeDetector <filespec>");
			throw e;
		}
		
		
		/* Now,we'll sort by time.  Since one of the exercise goals is to 
		 * optimize by time/space complexity, I will use the fact that the
		 * lexicographical order of ISO 8601 strings corresponds to their 
		 * chronological order, and NOT instantiate datetime or delta objects.
		 */
		splitLines.sort((String[] s1, String[] s2) -> s1[0].compareTo(s2[0]));
		
		// Sort by DSN (array element 1)
		splitLines.sort((String[] s1, String[] s2) -> s1[1].compareTo(s2[1]));
		
		ArrayList<String> whiteList = new ArrayList<String>();
		ArrayList<String> blackList = new ArrayList<String>();
		String[] first;
		String[] last; 
		String outString = "OUT_OF_USE";
		
		while (splitLines.size() > 0) {
			first = splitLines.remove(0);
			last = first;
			
			// Get the last matching DSN
			while (splitLines.size() > 0 && 
						splitLines.get(0)[1].equals(first[1])) {
				last = splitLines.remove(0);
			}
			
			// Check if DSN transitions in or out of use
			if (first[2].equals(outString)) {
				if (!last[4].equals(outString)){
					whiteList.add(first[1]);
				}
			} else {
				if (last[4].equals(outString)) {
					blackList.add(first[1]);
				}
			}
		}
		
		// Output - Trying to copy formatting exactly as specified
		System.out.println("\nDSNs needing blacklist:\n");
		for (String b : blackList) {
			System.out.println(b);
		}
		
		System.out.println("\n\n\nDSNs needing whitelist:\n");
		for (String w : whiteList) {
			System.out.println(w);
		}
		System.out.println("");
	}
}


