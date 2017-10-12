#!/usr/bin/env python

dictmap = {}
with open("q7", "r") as f:
	for line in f:
		mystring = line.strip()
		dictmap[mystring] = 0
	
with open("output", "r") as t:
	for line in t:
		if line.split('\t')[1] in dictmap:
			dictmap[line.split('\t')[1]] = line.split('\t')[0]
	sortedmap = sorted(dictmap.items(), key=lambda x: (int(x[1]),x[0]), reverse = True)
	keylist = []
	item = ''
	for k in sortedmap:
		item = item + "," + k[0] 
	print item[1:]
