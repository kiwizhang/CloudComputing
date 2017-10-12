#!/bin/bash
######################################################################
# Answer script for Project 1 module 2                             ###
# Fill  in the functions below for each question.                  ###
# You may use any other files/scripts/languages                    ###
# in these functions as long as they are in the submission folder. ###
######################################################################

# The filtered data should be put in a file named ‘output’ 


# How many lines emerged in your output files?
# Run your commands/code to process the output file and echo a 
# single number to standard output
answer_1() {
	# Write a function to get the answer to Q1. Do not just echo the answer.
	python question1.py
}

# What was the least popular article in the filtered output? How many total views
# did the least popular article get over the month?
# Run your commands/code to process the output and echo <article_name>\t<total views>
# to standard output.
# Break ties in reverse alphabetic order (if "ABC" and "XYZ" both have the minimum views, return "XYZ")
answer_2() {
	# Write a function to get the answer to Q2. Do not just echo the answer.
	python question2.py
}

# What was the most popular article on December 18, 2015 from the filtered output? 
# How many daily views did the most popular article get on December 18?
# Run your commands/code to process the output and echo <article_name>\t<daily views>
# to standard output
answer_3() {
        # Write a function to get the answer to Q3. Do not just echo the answer.
        python question3.py
}

# What is the most popular article of December 2015 with ZERO views on December 1, 2015?
# Run your commands/code to process the output and echo the answer
answer_4() {
        # Write a function to get the answer to Q4. Do not just echo the answer.
        python question4.py
}

# For how many days over the month was the page titled "Twitter" more popular 
# than the page titled "Apple_Inc." ?
# Run your commands/code to process the dataset and echo a single number to standard output
# Do not hardcode the articles, as we will run your code with different articles as input
# For your convenience, "Twitter" is stored in the variable 'first', and "Apple_Inc." in 'second'.
answer_5() {
	# do not change the following two lines
	# first=$(head -n 1 q5) #Twitter
	# second=$(cat q5 | sed -n 2p) #Apple_Inc.

	# Write a function to get the answer to Q5. Do not just echo the answer.
	python question5.py
}

# Rank the movie titles in the file q6 based on their maximum single-day wikipedia page views 
# (In descending order of page views, with the highest one first):
# Jurassic_Park_(film),The_Hunger_Games_(film),Fifty_Shades_of_Grey_(film),The_Martian_(film),Interstellar_(film)
# Ensure that you print the answers comma separated (As shown in the above line) without spaces
# For your convenience, code to read the file q6 is given below. Feel free to modify.
answer_6() {
	# Write a function to get the answer to Q6. Do not just echo the answer.
	python question6.py
}

# Rank the operating systems in the file q7 based on their total month views page views
# (In descending order of page views, with the highest one first. In descending alphabetical order by name
# if the pageviews are same;
# OS_X,Android_(operating_system),Windows_10,Linux
# Ensure that you print the answers comma separated (As shown in the above line)
# For your convenience, code to read the file q7 is given below. Feel free to modify.
answer_7() {
	# Write a function to get the answer to Q7. Do not just echo the answer.
    # while read line
    # do
    # 	os=$line
    # 	echo "$os,"
    # done < q7
    python question7.py
}

# How many films in the also have a corresponding TV series?
# Films are named <article_name>_([year_]film) 
# TV_series are named <article_name>_([year]_TV_series)
# 1. The article_name must be identical in both the film and TV_series
# 2. The film or TV_series *may* be accompanied by a 4 digit year
# Examples of valid cases:
# a. Concussion_(2015_film) is a match for Concussion_(TV_series)
# b. Scream_Queens_(2015_TV_series) is a match for Scream_Queens_(1929_film)
# Run your commands/code to process the output and echo the answer
answer_8() {
        # Write a function to get the answer to Q8. Do not just echo the answer.
	python question8.py
}

# Find out the number of articles with longest number of strictly decreasing sequence of views
# Example: If 21 articles have strictly decreasing pageviews everyday for 5 days (which is the global maximum), 
# then your script should find these articles from the output file and return 21.
# Run your commands/code to process the output and echo the answer
answer_9() {
        # Write a function to get the answer to Q9. Do not just echo the answer.
	python question9.py

}

# What was the type of the master instance that you used in EMR
# Ungraded question
answer_10() {
    # echo the answer (instance type)
    echo "m3.xlarge"
}

# What was the type of the task/core instances that you used in EMR
# Ungraded question (this means you don't get points for answering, but we deduct points if you don't answer honestly)
answer_11() {
    # echo the answer (instance type)
    echo "m3.xlarge"
}

# How many task/core instances did you use in your EMR run?
# Ungraded question (this means you don't get points for answering, but we deduct points if you don't answer honestly)
answer_12() {
    # echo the answer (instance count)
    echo "8"
}

# What was the execution time of your EMR run? (You can find this in the EMR console on AWS)
# Please echo the number of minutes your job took
# Ungraded question (this means you don't get points for answering, but we deduct points if you don't answer honestly)
answer_13() {
	# echo the answer (execution time in minutes)
	echo "39"
}

# DO NOT MODIFY ANYTHING BELOW THIS LINE
echo "Checking the files: "
if [ -f 'output' ]
then
        echo output file created, good!
else
        echo No output file created
fi

echo "The results of this run are : "
echo "{"

a1=`answer_1`
echo -en ' '\"answer1\": \"$a1\"
echo $a1 > .1.out
echo ","

a2=`answer_2`
echo -en ' '\"answer2\": \"$a2\"
echo $a2 > .2.out
echo ","

a3=`answer_3`
echo -en ' '\"answer3\": \"$a3\"
echo $a3 > .3.out
echo ","

a4=`answer_4`
echo -en ' '\"answer4\": \"$a4\"
echo $a4 > .4.out
echo ","

a5=`answer_5`
echo -en ' '\"answer5\": \"$a5\"
first=$(head -n 1 q5)
second=$(cat q5 | sed -n 2p)
echo "$first,$second,$a5" > .5.out
echo ","

a6=`answer_6`
echo -en ' '\"answer6\": \"$a6\"
echo $a6 > .6.out
echo ","

a7=`answer_7`
echo -en ' '\"answer7\": \"$a7\"
echo $a7 > .7.out
echo ","

a8=`answer_8`
echo -en ' '\"answer8\": \"$a8\"
echo $a8 > .8.out
echo ","

a9=`answer_9`
echo -en ' '\"answer9\": \"$a9\"
echo $a9 > .9.out
echo ","

a10=`answer_10`
echo -en ' '\"answer10\": \"$a10\"
echo $a10 > .10.out
echo ","

a11=`answer_11`
echo -en ' '\"answer11\": \"$a11\"
echo $a11 > .11.out
echo ","

a12=`answer_12`
echo -en ' '\"answer12\": \"$a12\"
echo $a12 > .12.out
echo ","

a13=`answer_13`
echo -en ' '\"answer13\": \"$a13\"
echo $a13 > .13.out
 
echo  "}"

echo ""
echo "If you feel these values are correct please run:"
echo "./submitter -a andrew_id -l <python,java>"

