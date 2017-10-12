from __future__ import print_function
import sys
from operator import add
from pyspark import SparkContext


sc = SparkContext(appName="follower")
file = sc.textFile("hdfs:///input")
counts = file.map(lambda word: (word.split("\t")[1], 1)).reduceByKey(lambda a, b: a + b)
countsFile = counts.map(lambda word: (str(word[0]) + "\t" + str(word[1])))
countsFile.saveAsTextFile("hdfs:///follower-output")