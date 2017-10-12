#!/usr/bin/env python

dictmap = {}
with open("q6", "r") as f:
	for line in f:
		mystring = line.strip().replace('\n', ' ').replace('\r', '')
		dictmap[mystring] = 0
	
with open("output", "r") as t:
	for line in t:
		if line.split('\t')[1] in dictmap:
			count = 0
			for i in range(2,32):
				if int(line.split('\t')[i][9:]) > count:
					count = int(line.split('\t')[i][9:])
			dictmap[line.split('\t')[1]] = count
	sortedmap = sorted(dictmap.items(), key=lambda x: x[1], reverse = True)
	keylist = []
	item = ''
	for k in sortedmap:
		item = item + "," + k[0] 
	print item[1:]
