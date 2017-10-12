#!/usr/bin/env python
import sys

#get the least number of views of views for original file. 
def countView():
	count = sys.maxint
	with open("output", "r") as f:
		for line in f:	
			temp = line.split('\t')[0].strip()
			if temp.isdigit():
				if int(temp) < count:	
					count = int(temp)
					min_line = line
				elif int(temp) == count:
					if line.split('\t')[1] > min_line.split('\t')[1]:
						min_line = line

#For the lines with only 3 elements, we take the second element as the number of views
		print min_line.split('\t')[1] + '\t' + min_line.split('\t')[0]
countView()