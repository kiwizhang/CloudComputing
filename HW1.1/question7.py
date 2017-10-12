#the most popular article in the filtered output
from processLine import processLine
def article_in_range():
    first_index = 0
    last_index = 0
    #create the ascending list by number of views
    with open("output", "r") as f:
    #the index of first element larger than 2500
        for line in f:
            if int(line.split('\t')[1]) <= 2500:
                break
            else:
                last_index = last_index + 1

    #the index of the last element smaller than 3000
    with open("output", "r") as f:
        for line in f:   
            if int(line.split('\t')[1]) <= 3000:
                break
            else:
                first_index = first_index + 1

    #calculate the number of the article in the range
    range_number = last_index - first_index 
    print range_number

article_in_range()