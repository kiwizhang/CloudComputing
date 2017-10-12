#!/usr/bin/env python

with open("output", "r") as f:
	count = 0
	for line in f:
		temp = line.split('\t')[19][9:]
		if temp.isdigit():
				if int(temp) > count:	
					count = int(temp)
					max_line = line
	print max_line.split('\t')[1] + '\t' + str(count)

