from __future__ import print_function
import sys
from operator import add
from pyspark import SparkContext


sc = SparkContext(appName="follower")
#parse the file from hdfs
file = sc.textFile("hdfs:///input")
#map each line by the followee as the key
counts = file.map(lambda word: (word.split("\t")[1], 1)).reduceByKey(lambda a, b: a + b)
#reformat the file to match the requirement
countsFile = counts.map(lambda word: (str(word[0]) + "\t" + str(word[1])))
#save the file in hdfs
countsFile.saveAsTextFile("hdfs:///follower-output")