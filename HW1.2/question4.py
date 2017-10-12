#!/usr/bin/env python

with open("output", "r") as f:
	count = 0
	for line in f:
		temp = line.split('\t')[2][9:]
		temp2 = line.split('\t')[0]
		if temp.isdigit():
				if int(temp) == 0:
					if int(temp2) > count:
						count = int(temp2)
						target = line
	print target.split('\t')[1]
