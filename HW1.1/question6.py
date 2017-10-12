#find the file with most views
from processLine import processLine
def popular_film():
	with open("output", "r") as f:	
	#put all the film related lines in a list
		for line in f:
			if 'film' in line.split('\t')[0]:
				print line.split('\t')[1] 
				break
popular_film()