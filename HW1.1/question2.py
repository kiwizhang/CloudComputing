
#count total number of views for original file. 
def countView():
	count = 0
	with open("output", "r") as f:
		for line in f:
			
				count = count + int(line.split()[2])
#For the lines with only 3 elements, we take the second element as the number of views
			else:
				count = count + int(line.split()[1])
		print count
countView()