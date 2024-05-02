# Large Scale Data Processing: Final Project
Authors:
- Ilan Valencius (valencig)
- Steven Roche (sroche14)
- Jason Adhinarta (jasonkena)

Compressed csvs for the matchings are in `exports/` (tarball also provided).

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
| :-------------------------: | :-------------: | :----------------------: | :-------------------: | :----: |
|    com-orkut.ungraph.csv    |    117185083    |        1,325,427         |       1,408,728       |   43   |
| twitter_original_edges.csv  |    63555749     |          91,735          |        94,074         |  157   |
|    soc-LiveJournal1.csv     |    42851237     |        1,544,555         |       1,780,692       |   38   |
| soc-pokec-relationships.csv |    22301964     |         587,924          |        664,398        |   26   |
|    musae_ENGB_edges.csv     |      35324      |          2,236           |         2,887         |   14   |
|     log_normal_100.csv      |      2671       |            49            |          50           |   8    |


Your goal is to compute a matching as large as possible for each graph. 

### Estimates of computation time
<b>The computation time was the following (using 2x4 N2 core CPU in GCP): </b> <br />
log_normal_100: 18s <br />
musae_ENGB_edges: 34s <br />
soc-pokec-relationships: 47612s <br />
soc-LiveJournal1: 58564s <br />
<b>(Using 1 node with 256 GB of RAM):</b> <br />
twitter_original_edges: ~ 4 hours (time improvement due to changing from GCP to BC cluster) <br />
com-orkut.ungraph: ~ 8 hours <br />

### Approach
The general approach of this algorithm is to perform a greedy algorithm on subgraphs which get successively smaller. The paper proves that given the correct parameters of $p$ and $k$, the algorithm can run in $O(n)$ space using $O(\log \log \Delta)$ rounds. Given that we did not implement algorithm 2 which _ensures_ the maximum degree decreases on each run, we are left with a slightly worse algorithm that runs in $O(\log n)$ rounds. This becomes a noticeable issue for the twitter graph where the maximum degree does not decrease quickly due to a large number of nodes with very high degrees.

### Advantages
The primary advantage of this algorithm is that it performs very well on the massively parallel computation model. The full benefits of this algorithm are not realized for our testing as we do not have access to a large number of machines. The algorithm should run each subgraph on a separate machine and thus can run extremely fast as running `GreedyMM` is very fast on one machine. Given that we are not using multiple machines this benefit goes unrealized which leads to long run-times.
