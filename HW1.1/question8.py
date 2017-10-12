#calculate the number of views for all articles that have titles
# that start with a single number (0-9) and then a character
from processLine import processLine
def unique_article():
    count = 0
    with open("output", "r") as f:
    #filter the title that start with a single number (0-9) and then a character
        for line in f:
            if line[0].isdigit() and line[1].isalpha():
                count = count + int(line.split('\t')[1])
    print count
unique_article()