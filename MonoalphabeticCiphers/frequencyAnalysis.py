import argparse
import re
from string import ascii_lowercase as lower, ascii_uppercase as upper
parser = argparse.ArgumentParser()

# Take arguments for the cipher text as well as where to print the frequency analysis
parser.add_argument("-i", "--inputFile", help="Path and name of the file to read in")
parser.add_argument("-sf", "--singleFrequency", help="File to print single letter frequency analysis to")
parser.add_argument("-tf", "--twoFrequency", help="File to print two letter frequency analysis to")

# Method to calculate the single letter frequency
# Takes an argument of the string letters 
# Returns a decimal of the occurance (occurance of letter/total letters)
def calculateSingleCharFrequencies(text):
    inputAlphaChar = re.findall(r'[a-zA-Z]', text) # strip non alphabet characters
    counts = {}
    # loop through all letters and count occurances
    for char in inputAlphaChar:
        count = counts.get(char, 0)
        counts[char] = count + 1
    sortedList = sorted(counts, key=counts.get, reverse=True) # Sort the list highest number of occurances first
    totalCount = 0
    # get the total occurances
    for item in sortedList:
        totalCount = totalCount + counts[item]
    frequency = {}
    totalPercent = 0
    # calculate the frequency
    for item in sortedList:
        itemCount = counts[item]
        frequency[item] = (itemCount/totalCount)
        totalPercent = totalPercent + frequency[item]
    return frequency

# Method to calculate the two letter frequency
# Takes an argument of the string letters 
# Returns a decimal of the occurance (occurance of letter/total letters)
def calculateTwoLetterFrequencies(text):
    counts = {}
    # loops through every character in the string
    for i in range(1, len(text)):
        char = text[i-1:i+1] # Make a two character pair
        # Check that both characters are alphabet characters
        if char[1] in lower or char[1] in upper:
            if char[0] in lower or char[0] in upper:
                # count occurances
                count = counts.get(char, 0)
                counts[char] = count + 1
    sortedList = sorted(counts, key=counts.get, reverse=True) # Sort the list highest number of occurances first
    totalCount = 0
    # get the total occurances
    for item in sortedList:
        totalCount = totalCount + counts[item]
    frequency = {}
    totalPercent = 0
    # calculate the frequency
    for item in sortedList:
        itemCount = counts[item]
        frequency[item] = (itemCount/totalCount)
        totalPercent = totalPercent + frequency[item]
    return frequency

# Read in file of cipher text
# Takes a file path
# returns string of file contents
def readFile(filename):
    inputFileText = open(filename, 'r')
    inputString = inputFileText.read()
    inputFileText.close()
    return inputString

# Write frequencies to file
# Takes a file path and the dictionary of frrequencies
def writeFrequencyToFile(filename, frequency):
    outputFilePath = "frequency.txt"
    # if a file was passed in use it instead of default
    if filename:
        outputFilePath = filename
    f = open(outputFilePath, 'w') # Open file for writing (will overwrite existing contents)
    for item in frequency:
        f.write("" + item + " {:.2%}".format(frequency[item]) + "\n") # Format the decimal into a percent
    f.close()

def main():
    args = parser.parse_args()
    inputFilePath = args.inputFile
    fileText = readFile(inputFilePath)

    frequency = calculateSingleCharFrequencies(fileText)
    twoLetterFrequency = calculateTwoLetterFrequencies(fileText)
    
    writeFrequencyToFile(args.singleFrequency, frequency)
    writeFrequencyToFile(args.twoFrequency, twoLetterFrequency)

main()