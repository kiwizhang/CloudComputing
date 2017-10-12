#! /bin/bash

######################################################################
# Answer script for Project 3 module 1 Fill in the functions below ###
# for each question.                                               ###
######################################################################

# Answer question 1-6 by completing the bash commands inline.
# Do NOT use java, python, etc. for these questions.

# Question 1
# How many rows in million_songs_metadata.csv contain 'Aerosmith' (Case sensitive)?
# Write your bash commands/code to process the dataset and output a
# single number to standard output.
answer_1() {
	grep -P 'Aerosmith' million_songs_metadata.csv | wc -l

}

# Question 2
# What is the total number of track_id(s) in million_songs_metadata.csv with
# artist_name containing "Bob Marley" (Case sensitive)?
# Write your grep commands/code to process the dataset and output a
# single number to standard output.
answer_2() {
	awk ' BEGIN {FS = ","} ; {if ($7 ~ /Bob Marley/) count++ } END {print count}' million_songs_metadata.csv
}

# Question 3
# How many rows in million_songs_metadata.csv contain 'The Beatles' (Case sensitive) on column 7?
# Write your awk commands/code to process the dataset and output a
# single number to standard output.
answer_3() {
	awk ' BEGIN {FS = ","} ; {if ($7 ~ /The Beatles/) count++ } END {print count}' million_songs_metadata.csv

}

# Question 4
# Write commands to do the equivalent of the SQL query SELECT AVG(duration) FROM songs;.
# Write your bash commands/code to process the dataset and output a
# single number to standard output.
answer_4() {
	awk ' BEGIN {FS = ","} ; { sum += $8 } END {print sum / NR; }' million_songs_metadata.csv
}

# Question 5
# Invoke the awk / bash program or the set of commands that you wrote
# to merge the two files into the file million_songs_metadata_and_sales.csv
# in current folder.
# No output is needed here.
answer_5() {
	join -t, -1 1 -2 1  million_songs_sales_data.csv million_songs_metadata.csv > million_songs_metadata_and_sales.csv
}

# Question 6
# Find the artist with maximum sales using the file million_songs_metadata_and_sales.csv.
# The output of your command(s) should be the artist name.
# NOTE: Artists can have many different artist_names, but only one artist_id,
# which is unique to each artist. You should find the maximum sales based
# on artist_id, and return any of that artist_id’s valid artist_name as the result.
# Write your bash commands/code to process the dataset and output a
# single line of answer to standard output.
answer_6() {
	awk ' BEGIN {FS = ",";}; {a[$7]+=$3; name[$7] = $9} END{for(i in a) {if(a[i] > a[maxIndex]) maxIndex = i;}; print name[maxIndex]}' million_songs_metadata_and_sales.csv

}

# Answer question 7-11 by completing the corresponding method(s) in MySQLTasks.java.

# Qustion 7
# Write a SQL query that returns the trackid of the song with the maximum duration.
# Complete the corresponding method(s) in MySQLTasks.java.
answer_7() {
  java MySQLTasks q7
}

# Question 8
# A database index is a data structure that improves the speed of data retrieval.
# Identify the field that will improve the performance of query in question 7
# and create a database index on that field.
# Complete the corresponding method(s) in MySQLTasks.java.
INDEX_NAME="replace_this_with_your_index_name"
answer_8() {
	java MySQLTasks q8 $INDEX_NAME
}

# Question 9
# Write a SQL query that returns the trackid of the song with the maximum duration
# This is the same query as Question 7. Do you see any difference in performance?
# Complete the corresponding method(s) in MySQLTasks.java.
answer_9() {
	java MySQLTasks q9
}

#Question 10
# Write the SQL query that is equivalent to the command grep -P 'The Beatles' million_songs_metadata.csv | wc -l.
# Complete the corresponding method(s) in MySQLTasks.java.
answer_10() {
	java MySQLTasks q10
}

#Question 11
# Which artist has the third-most number of rows in table songs?
# The output should be the name of the artist.
# Please use artist_id as the unique identifier of the artist. If the artist_id has several artist_names associated,
# return any one of them.
# Complete the corresponding method(s) in MySQLTasks.java.
answer_11() {
	java MySQLTasks q11
}


# Answer question 12-16 corresponding to your experiments with sysbench benchmarks.

# Answer the following questions corresponding to your experiments on t1.micro instance

# Question 12
# Please output the RPS (Request per Second) values obtained from
# the first three iterations of FileIO sysbench executed on t1.micro
# instance with magnetic EBS attached.
answer_12() {
	# Echo single numbers on line 1, 3, and 5 within quotation marks
	echo "116.05"
	echo ,
	echo "127.34"
	echo ,
	echo "121.73"
}

# Question 13
# Please output the RPS (Request per Second) values obtained from
# the first three iterations of FileIO sysbench executed on t1.micro
# instance with SSD EBS attached.
answer_13() {
	# Echo single numbers on line 1, 3, and 5 within quotation marks
	echo "712.80"
	echo ,
	echo "726.66"
	echo ,
	echo "726.33"
}

# Answer the following questions corresponding to your experiments on m3.large instance

# Question 14
# Please output the RPS (Request per Second) values obtained from
# the first three iterations of FileIO sysbench executed on m3.large
# instance with magnetic EBS attached.
answer_14() {
	# Echo single numbers on line 1, 3, and 5 within quotation marks
	echo "362.25"
	echo ,
	echo "596.84"
	echo ,
	echo "790.80"
}

# Question 15
# Please output the RPS (Request per Second) values obtained from
# the first three iterations of FileIO sysbench executed on m3.large
# instance with SSD EBS attached.
answer_15() {
	# Echo single numbers on line 1, 3, and 5 within quotation marks
	echo "1867.99"
	echo ,
	echo "2182.33"
	echo ,
	echo "2187.66"
}

# Question 16
# For the FileIO benchmark in m3.large, why does the RPS value vary in each run
# for both Magnetic and SSD-backed EBS volumes? Did the RPS value in t1.micro
# vary as significantly as in m3.large? Why do you think this is the case?
answer_16() {
	# Put your answer with a simple paragraph in a file called "answer_16"
	# Do not change the code below
	if [ -f answer_16 ]
	then
		echo "Answered"
	else
		echo "Not answered"
	fi
}

# Answer question 17-21 by completing the corresponding method(s) in HBaseTasks.java.

# Question 17
# What was that song whose name started with "Total" and ended with "Water"?
# Write an HBase query that finds the track that the person is looking for.
# The title starts with "Total" and ends with "Water", both are case sensitive.
answer_17() {
    java HBaseTasks q17;
}

# Question 18
# I don't remember the exact title, it was that song by "Kanye West", and the
# title started with either "Apologies" or "Confessions". Not sure which...
# Write an HBase query that finds the track that the person is looking for.
# The artist_name contains "Kanye West", and the title starts with either
# "Apologies" or "Confessions" (Case sensitive).
answer_18() {
    java HBaseTasks q18;
}

# Question 19
# There was that new track by "Bob Marley" that was really long. Do you know?
# Write an HBase query that finds the track the person is looking for.
# The artist_name has a prefix of "Bob Marley", duration greater than 400,
# and year 2000 and onwards (Case sensitive).
answer_19() {
    java HBaseTasks q19;
}

# Question 20
# I heard a really great song about "Family" by this really cute singer,
# I think his name was "Consequence" or something...
# Write an HBase query that finds the track the person is looking for.
# The track has an artist_hotttnesss of at least 1, and the artist_name
# contains "Consequence". Also, the title contains "Family" (Case sensitive).
answer_20() {
    java HBaseTasks q20;
}

# Question 21
# Hey what was that "Love" song that "Gwen Guthrie" came out with in 1990?
# No, no, it wasn't the sad one, nothing "Bitter" or "Never"...
# Write an HBase query that finds the track the person is looking for.
# The track has an artist_name prefix of "Gwen Guthrie", the title contains "Love"
# but does NOT contain "Bitter" or "Never".
answer_21() {
    java HBaseTasks q21;
}


##############################################
### DO NOT MODIFY ANYTHING BELOW THIS LINE ###
##############################################
echo "{"

if [ -z $1 ] || [ $1 == "files" ]
then

    echo -en ' '\"answer1\": \"`answer_1`\"
    echo ","

    echo -en ' '\"answer2\": \"`answer_2`\"
    echo ","

    echo -en ' '\"answer3\": \"`answer_3`\"
    echo ","

    echo -en ' '\"answer4\": \"`answer_4`\"
    echo ","

    answer_5 &> /dev/null
    if [ -f 'million_songs_metadata_and_sales.csv' ]
    then
        echo -en ' '\"answer5\": \"'million_songs_metadata_and_sales.csv' file created\"
        echo ","
    else
        echo -en ' '\"answer5\": \"'million_songs_metadata_and_sales.csv' file not created\"
        echo ","
    fi

    echo -en ' '\"answer6\": \"`answer_6`\"
    if [ -z $1 ]
    then
        echo ","
    fi
fi

if [ -z $1 ] || [ $1 == "mysql" ]
then
    javac MySQLTasks.java > /dev/null

    # Disable caching
    `mysql --skip-column-names --batch -u root -pdb15319root song_db -e "set global query_cache_size = 0" &> /dev/null`

    # Drop any existing index
    INDEX_FIELD=`mysql --skip-column-names --batch -u root -pdb15319root song_db -e "describe songs" | grep MUL | cut -f1`
    if [ "$INDEX_FIELD" ]
    then
        `mysql --skip-column-names --batch -u root -pdb15319root song_db -e "drop index $INDEX_NAME on songs" > /dev/null`
    fi
    START_TIME=$(date +%s.%N)
    TID=`answer_7 | tail -1`
    END_TIME=$(date +%s.%N)
    RUN_TIME=$(echo "$END_TIME - $START_TIME" | bc)
    echo -en ' '\"answer7\": \"$TID,$RUN_TIME\"
    echo ","

    answer_8 > /dev/null
    INDEX_FIELD=`mysql --skip-column-names --batch -u root -pdb15319root song_db -e "describe songs" | grep MUL | cut -f1`
    echo -en ' '\"answer8\": \"$INDEX_FIELD\"
    echo ","

    START_TIME=$(date +%s.%N)
    TID=`answer_9 | tail -1`
    END_TIME=$(date +%s.%N)
    RUN_TIME=$(echo "$END_TIME - $START_TIME" | bc)
    echo -en ' '\"answer9\": \"$TID,$RUN_TIME\"
    echo ","

    echo -en ' '\"answer10\": \"`answer_10`\"
    echo ","

    echo -en ' '\"answer11\": \"`answer_11`\"
    if [ -z $1 ]
    then
        echo ","
    fi
    rm MySQLTasks.class > /dev/null
fi

if [ -z $1 ] || [ $1 == "scaling" ]
then
    echo -en ' '\"answer12\": \"`answer_12`\"
    echo ","

    echo -en ' '\"answer13\": \"`answer_13`\"
    echo ","

    echo -en ' '\"answer14\": \"`answer_14`\"
    echo ","

    echo -en ' '\"answer15\": \"`answer_15`\"
    echo ","

    echo -en ' '\"answer16\": \"`answer_16`\"
    if [ -z $1 ]
    then
        echo ","
    fi
fi

if [ -z $1 ] || [ $1 == "hbase" ]
then
    javac HBaseTasks.java > /dev/null
    echo -en ' '\"answer17\": \"`answer_17`\"
    echo ","

    echo -en ' '\"answer18\": \"`answer_18`\"
    echo ","

    echo -en ' '\"answer19\": \"`answer_19`\"
    echo ","

    echo -en ' '\"answer20\": \"`answer_20`\"
    echo ","

    echo -en ' '\"answer21\": \"`answer_21`\"
    rm HBaseTasks.class > /dev/null
fi
echo
echo  "}"
