# Large Scale Data Processing: Final Project
Authors:
- Ilan Valencius (valencig)
- Steven Roche (sroche14)
- Jason Adhinarta (jasonkena)

## Graph matching
For the final project, you are provided 6 CSV files, each containing an undirected graph, which can be found [here](https://drive.google.com/file/d/1khb-PXodUl82htpyWLMGGNrx-IzC55w8/view?usp=sharing). The files are as follows:  

|           File name           |        Number of edges       | Size of Matching [edges] |
| :---------------------------: | :--------------------------: | :----------------------: |
| com-orkut.ungraph.csv         | 117185083                    | :x: |
| twitter_original_edges.csv    | 63555749                     | :x: |
| soc-LiveJournal1.csv          | 42851237                     | :x: |
| soc-pokec-relationships.csv   | 22301964                     | :x: |
| musae_ENGB_edges.csv          | 35324                        | :x: |
| log_normal_100.csv            | 2671                         | :x: |

Your goal is to compute a matching as large as possible for each graph. 

### Estimates of computation time
TODO

### Approach
TODO

### Advantages
TODO

### Input format
Each input file consists of multiple lines, where each line contains 2 numbers that denote an undirected edge. For example, the input below is a graph with 3 edges.  
1,2  
3,2  
3,4  

### Output format
Your output should be a CSV file listing all of the matched edges, 1 on each line. For example, the ouput below is a 2-edge matching of the above input graph. Note that `3,4` and `4,3` are the same since the graph is undirected.  
1,2  
4,3  

## Deliverables
* The output file (matching) for each test case.
  * For naming conventions, if the input file is `XXX.csv`, please name the output file `XXX_matching.csv`.
  * You'll need to compress the output files into a single ZIP or TAR file before pushing to GitHub. If they're still too large, you can upload the files to Google Drive and include the sharing link in your report.
* The code you've applied to produce the matchings.
  * You should add your source code to the same directory as `verifier.scala` and push it to your repository.
* A project report that includes the following:
  * A table containing the size of the matching you obtained for each test case. The sizes must correspond to the matchings in your output files.
  * An estimate of the amount of computation used for each test case. For example, "the program runs for 15 minutes on a 2x4 N1 core CPU in GCP." If you happen to be executing mulitple algorithms on a test case, report the total running time.
  * Description(s) of your approach(es) for obtaining the matchings. It is possible to use different approaches for different cases. Please describe each of them as well as your general strategy if you were to receive a new test case.
  * Discussion about the advantages of your algorithm(s). For example, does it guarantee a constraint on the number of shuffling rounds (say `O(log log n)` rounds)? Does it give you an approximation guarantee on the quality of the matching? If your algorithm has such a guarantee, please provide proofs or scholarly references as to why they hold in your report.
* A presentation during class time on 5/2 (Thu) or 4/30 (Tue).
  * Note that the presentation date is before the final project submission deadline. This means that you could still be working on the project when you present. You may present the approaches you're currently trying. You can also present a preliminary result, like the matchings you have at the moment.

## Grading policy
* Quality of matchings (40%)
  * For each test case, you'll receive at least 70% of full credit if your matching size is at least half of the best answer in the class.
  * **You will receive a 0 for any case where the verifier does not confirm that your output is a matching.** Please do not upload any output files that do not pass the verifier.
* Project report (35%)
  * Your report grade will be evaluated using the following criteria:
    * Discussion of the merits of your algorithms such as the theoretical merits (i.e. if you can show your algorithm has certain guarantee).
    * Depth of technicality
    * Novelty
    * Completeness
    * Readability
* Presentation (15%)
* Formatting (10%)
  * If the format of your submission does not adhere to the instructions (e.g. output file naming conventions), points will be deducted in this category.

## You must do the following to receive full credit:
1. Create your report in the ``README.md`` and push it to your repo.
2. In the report, you must include your (and any partner's) full name in addition to any collaborators.
3. Submit a link to your repo in the Canvas assignment.

## Early submission bonus
The deadline of the final project is on 5/4 (Friday) 11:59PM. 
**If you submit by 5/3 (Thu) 11:59PM, you will get 5% boost on the final project grade.**
The submission time is calculated from the last commit in the Git log.
**No extension beyond 5/4 11:59PM will be granted, even if you have unused late days.**
