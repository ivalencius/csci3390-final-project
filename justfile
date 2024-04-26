default:
    just --list

build:
    sbt clean package

set export
LOCAL_DIRS := "."

compress:
    find ./exports/*.csv -exec gzip -kf {} \;
    # Check that everything is small enough (<25 MB)
    find ./exports/*.csv.gz -size +25M -exec ls -lhs {} \;

log_normal_100:
    spark-submit --master local[*] --class final_project.maximal target/scala-2.12/final_project_2.12-1.0.jar data/log_normal_100.csv
    find ./exports/log_normal_100_output.csv/*.csv -exec mv {} ./exports/log_normal_100_matching.csv \;
    rm -rf ./exports/log_normal_100_output.csv
    spark-submit --master local[*] --class final_project.verifier target/scala-2.12/final_project_2.12-1.0.jar data/log_normal_100.csv exports/log_normal_100_matching.csv

musae:
    spark-submit --master local[*] --class final_project.maximal target/scala-2.12/final_project_2.12-1.0.jar data/musae_ENGB_edges.csv
    find ./exports/musae_ENGB_edges_output.csv/*.csv -exec mv {} ./exports/musae_ENGB_edges_matching.csv \;
    rm -rf ./exports/musae_ENGB_edges_output.csv
    spark-submit --master local[*] --class final_project.verifier target/scala-2.12/final_project_2.12-1.0.jar data/musae_ENGB_edges.csv exports/musae_ENGB_edges_matching.csv

soc-pokec:
    spark-submit --master local[*] --class final_project.maximal target/scala-2.12/final_project_2.12-1.0.jar data/soc-pokec-relationships.csv
    find ./exports/soc-pokec-relationships_output.csv/*.csv -exec mv {} ./exports/soc-pokec-relationships_matching.csv \;
    rm -rf ./exports/soc-pokec-relationships_output.csv
    spark-submit --master local[*] --class final_project.verifier target/scala-2.12/final_project_2.12-1.0.jar data/soc-pokec-relationships.csv exports/soc-pokec-relationships_matching.csv

soc-LiveJournal:
    spark-submit --master local[*] --class final_project.maximal target/scala-2.12/final_project_2.12-1.0.jar data/soc-LiveJournal1.csv
    find ./exports/soc-LiveJournal1_output.csv/*.csv -exec mv {} ./exports/soc-LiveJournal1_matching.csv \;
    rm -rf ./exports/soc-LiveJournal1_output.csv
    spark-submit --master local[*] --class final_project.verifier target/scala-2.12/final_project_2.12-1.0.jar data/soc-LiveJournal1.csv exports/soc-LiveJournal1_matching.csv

twitter:
    spark-submit --master local[*] --class final_project.maximal target/scala-2.12/final_project_2.12-1.0.jar data/twitter_original_edges.csv
    find ./exports/twitter_original_edges_output.csv/*.csv -exec mv {} ./exports/twitter_original_edges_matching.csv \;
    rm -rf ./exports/twitter_original_edges_output.csv
    spark-submit --master local[*] --class final_project.verifier target/scala-2.12/final_project_2.12-1.0.jar data/twitter_original_edges.csv exports/twitter_original_edges_matching.csv

com:
    spark-submit --master local[*] --class final_project.maximal target/scala-2.12/final_project_2.12-1.0.jar data/com-orkut.ungraph.csv
    find ./exports/com-orkut.ungraph_output.csv/*.csv -exec mv {} ./exports/com-orkut.ungraph_matching.csv \;
    rm -rf ./exports/com-orkut.ungraph_output.csv
    spark-submit --master local[*] --class final_project.verifier target/scala-2.12/final_project_2.12-1.0.jar data/com-orkut.ungraph.csv exports/com-orkut.ungraph_matching.csv