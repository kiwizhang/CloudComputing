
#filter the data and sort the data

#filter the data and sort the data
from processLine import processLine
saveList = []
def print_to_output():
    #apply filter conditions to the data and save them in a list
    with open("pagecounts-20151201-000000", "r") as f:
        for line in f:
            processLine(line, saveList)
    #sort the filtered data by number of views in ascending order
        newlist = sorted(saveList, key = lambda x: int(x.split('\t')[1]), reverse = True)
        for line in newlist:
            print line
print_to_output()