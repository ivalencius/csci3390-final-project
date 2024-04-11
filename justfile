default:
    just --list

build:
    sbt clean package

set export
LOCAL_DIRS := "."

foo:
    spark-submit --master local[*] --class final_project.verifier target/scala-2.12/final_project_2.12-1.0.jar

log_normal_100:
    spark-submit --master local[*] --class final_project.verifier target/scala-2.12/final_project_2.12-1.0.jar data/log_normal_100.csv exports/log_normal_100_matching.csv

musae: 
    spark-submit --master local[*] --class final_project.verifier target/scala-2.12/final_project_2.12-1.0.jar data/musae_ENGB_edges.csv exports/musae_ENGB_edges.csv

soc-pokec:
    spark-submit --master local[*] --class final_project.verifier target/scala-2.12/final_project_2.12-1.0.jar data/soc-pokec-relationships.csv exports/soc-pokec-relationships.csv

soc-LiveJournal:
    spark-submit --master local[*] --class final_project.verifier target/scala-2.12/final_project_2.12-1.0.jar data/soc-LiveJournal1.csv exports/soc-LiveJournal1.csv

twitter:
    spark-submit --master local[*] --class final_project.verifier target/scala-2.12/final_project_2.12-1.0.jar data/twitter_original_edges.csv exports/twitter_original_edges.csv

com:
        spark-submit --master local[*] --class final_project.verifier target/scala-2.12/final_project_2.12-1.0.jar data/com-orkut.ungraph.csv exports/com-orkut.ungraph.csv