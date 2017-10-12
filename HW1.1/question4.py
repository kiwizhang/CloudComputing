#the most popular article in the filtered output
from processLine import processLine
def popular_article():
    saveList = []
    with open("output", "r") as f:
        for line in f:
            print line.split('\t')[0]
            break
popular_article()