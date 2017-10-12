#count the number of lines in original file
def countline():
	num_lines = sum(1 for line in open('output'))
	print num_lines
countline()