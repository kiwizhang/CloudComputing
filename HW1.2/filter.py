#!/usr/bin/env python2

#filter the data and sort the data

#filter the data and sort the data
import os
from processLine import processLine
saveList = []
filename = os.environ['map_input_file']

def print_to_output():
    #apply filter conditions to the data and save them in a list
    with open(filename, "r") as f:
        for line in f:
            processLine(line, saveList)
    #sort the filtered data by number of views in ascending order
        newlist = sorted(saveList, key = lambda x: int(x.split('\t')[1]), reverse = True)
        for line in newlist:
            print line
print_to_output()