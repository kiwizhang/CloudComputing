#!/usr/bin/env python

global_max = 0
numofitem = 0
count = 0
with open("output", "r") as f:
	for line in f:
		countlist = []
		count = 0
		for i in range(2,32):
			if int(line.split('\t')[i][9:]) > int(line.split('\t')[i + 1][9:]):
				count = count + 1
			else:
				countlist.append(count)
				count = 0
		if len(countlist) == 0:
			countlist.append(count)
		maxvalue = max(countlist)
		if maxvalue > global_max:
			global_max = maxvalue
			numofitem = 1
		elif maxvalue == global_max:
			numofitem = numofitem + 1
print numofitem
			
