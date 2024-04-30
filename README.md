# Large Scale Data Processing: Final Project
Authors:
- Ilan Valencius (valencig)
- Steven Roche (sroche14)
- Jason Adhinarta (jasonkena)

__ADD DESCRIPTIVE TEXT ___

---
__Algorithm 1__:  Maximal Matching
---

__Input:__ A graph $G = (V, E)$ with maximum degree $\Delta$

__Output:__ A matching $M$ in $G$

__repeat__: Define $p:=\Delta^{-0.77}$ and $k := \Delta^{0.12}$
  
  1. Permutation: Choose a permutation $\pi$ uniformly at random over the edges in $E$.
  2. Edge-sampling: Let $G^L=(V,L)$ be an edge-sampled subgraph of $G$ where each edge in $E$ is sampled independently with probability $p$.
  3. Vertex partitioning: Choose a function $\chi: V \to [k]$ uniformly at random and form associated vertex partition $V_1,...,V_k$ where $V_i = {v: \chi(v) = i}$.
  4. Each machine $i \in [k]$ receives the graph $G^L[V_i]$ and finds the greedy maximal matching $M_i := \text{GreedyMM}(G^L[V_i], \pi)$.
  5. Return matching $M := \cup^{k}_{i=1} M_i$.

__until__ $\Delta=1$

---
__Algorithm 2__:  GreedyMM
---

__Input:__ A graph $G = (V, E)$ with ordering $\pi$

__Output:__ A maximal matching $M$ in $G$

Initialize an empty graph $M = \varnothing$.

In each iteration let $E'$ denote the incident edges of an edge $e$.

__for__: edge $e \in E$ (in $\pi$ order):

  1. If $E' \notin M \to$ add $e$ to $M$

__end__

---
Behnezhad, S., Hajiaghayi, M. T., & Harris, D. G. (2019, November). Exponentially faster massively parallel maximal matching. In 2019 IEEE 60th Annual Symposium on Foundations of Computer Science (FOCS) (pp. 1637-1649). IEEE. [[paper]](https://arxiv.org/pdf/1901.03744.pdf), [[YouTube description]](https://www.youtube.com/watch?v=axtF2JlRj6k)


## Graph matching
For the final project, you are provided 6 CSV files, each containing an undirected graph, which can be found [here](https://drive.google.com/file/d/1khb-PXodUl82htpyWLMGGNrx-IzC55w8/view?usp=sharing). The files are as follows:  

|          File name          | Number of edges | Size of Matching [edges] | Top result to aim for | Rounds |
| :-------------------------: | :-------------: | :----------------------: | :-------------------: | :-------------------: |
|    com-orkut.ungraph.csv    |    117185083    |          1,325,427       |        1,408,728        | 43 |
| twitter_original_edges.csv  |    63555749     |           :x:            |         94,074         | ? |
|    soc-LiveJournal1.csv     |    42851237     |        1,544,555        |       1,780,692       | 38 |
| soc-pokec-relationships.csv |    22301964     |         587,924          |        664,398        | 26 |
|    musae_ENGB_edges.csv     |      35324      |          2,236           |         2,887         | 14|
|     log_normal_100.csv      |      2671       |            49            |          50           | 8 |


Your goal is to compute a matching as large as possible for each graph. 

### Estimates of computation time
The computation time was the following:
log_normal_100: 18s
musae_ENGB_edges: 34s
soc-pokec-relationships: 47612s
soc-LiveJournal1: 58564s
twitter_original_edges: ~ 4 hours (time improvement due to changing from GCP to BC cluster)
com-orkut.ungraph: ~ 8 hours

### Approach
The general approach of this algorithm is to perform a greedy algorithm on subgraphs which get successively smaller. The paper proves that given the correct parameters of $p$ and $k$, the algorithm can run in $O(n)$ space using $O(\log \log \Delta)$ rounds. Given that we did not implement algorithm 2 which _ensures_ the maximum degree decreases on each run, we are left with a slightly worse algorithm that runs in $O(\log n)$ rounds.

### Advantages
The primary advantage of this algorithm is that it performs very well on the massively parallel computation model. The full benefits of this algorithm are not realized for our testing as we do not have access to a large number of machines. The algorithm should run each subgraph on a separate machine and thus can run extremely fast as running `GreedyMM` is very fast on one machine. Given that we are not using multiple machines this benefit goes unrealized which leads to long run-times.

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
