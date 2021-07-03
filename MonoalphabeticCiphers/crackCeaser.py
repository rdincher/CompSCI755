import argparse
from string import ascii_lowercase as lower, ascii_uppercase as upper
parser = argparse.ArgumentParser()

parser.add_argument("-i", "--input", help="input text")

def shiftChar(cipherText, shiftValue):
    translateTable = str.maketrans(lower+upper, lower[shiftValue:] + lower[:shiftValue] + upper[shiftValue:] + upper[:shiftValue])
    return cipherText.translate(translateTable)

def main():
    args = parser.parse_args()
    cipherText = args.input
    for x in range(1, 26):
        plainText = shiftChar(cipherText, -x)
        print(plainText, x)


main()