#!/usr/bin/env python
count = 0
itemset = set()
with open("output", "r") as f:
	for line in f:
		if line.split("\t")[1].endswith('film)'):
			try:
				itemset.add(line.split("\t")[1].split("(")[0])
			except ValueError:
				continue
	f.seek(0)
	for line in f:
		if line.split("\t")[1].endswith('TV_series)'):
			if line.split("\t")[1].split("(")[0] in itemset:
				count = count + 1
				itemset.remove(line.split("\t")[1].split("(")[0])

print count
