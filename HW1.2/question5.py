#!/usr/bin/env python

itemlist = []
with open("q5", "r") as t:
	for line in t:
		itemlist.append(line.strip())
first = itemlist[0]
second = itemlist[1]

with open("output", "r") as f:
	for line in f:
		if line.split('\t')[1] == first:
			firstline = line
		if line.split('\t')[1] == second:
			secondline = line
	count = 0
	for i in range(2,33):
		if int(firstline.split('\t')[i][9:]) > int(secondline.split('\t')[i][9:]):
			count = count + 1		
	print count



