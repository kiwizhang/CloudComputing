
with open("links.csv", "r") as f:
	for line in f:
		words = line.split(",")
		print words[0] + "&" + words[1][:-1] + "," + line[:-1]