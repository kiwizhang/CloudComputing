#count the number of lines in original file
def countline():
	num_lines = sum(1 for line in open('pagecounts-20151201-000000'))
	print num_lines
countline()