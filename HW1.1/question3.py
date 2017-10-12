
#count number of lines for filtered data
from processLine import processLine

def countline_filtered():
	count = 0
	with open("output", "r") as f:
		for line in f:
			count = count + 1
	print count
countline_filtered()
