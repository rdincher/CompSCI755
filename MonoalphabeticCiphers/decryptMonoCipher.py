import argparse
from string import ascii_lowercase as lower, ascii_uppercase as upper
parser = argparse.ArgumentParser()

parser.add_argument("-i", "--inputFile", help="input text")
parser.add_argument("-o", "--outputFile", help="output text")
parser.add_argument("-k", "--key", help="key given as file with \nsourceOrder \nplaintextOrder ")

def shiftChar(cipherText, source, plain):
    translateTable = str.maketrans(source.lower()+source.upper(), plain.lower()+plain.upper())
    return cipherText.translate(translateTable)

def readFile(filename):
    inputFileText = open(filename, 'r')
    inputString = inputFileText.read()
    inputFileText.close()
    return inputString

def writeToFile(filename, text):
    outputFilePath = "decrypted.txt"
    if filename:
        outputFilePath = filename
    f = open(outputFilePath, 'w')
    f.write(text)
    f.close()

def main():
    args = parser.parse_args()
    cipherFile = args.inputFile
    cipherText = readFile(cipherFile)

    keyValue = readFile(args.key)
    keySplit = keyValue.split('\n')
    sourceOrder = keySplit[0]
    print(sourceOrder)
    plainOrder = keySplit[1]
    print(plainOrder)
    plainText = shiftChar(cipherText, sourceOrder, plainOrder)

    writeToFile(args.outputFile, plainText)

main()