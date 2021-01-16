import re
from models.Poll import *


def split_double_quotes(text):
    return re.findall(r'\"(.+?)\"', text)


answerKeyFile = open("../../answer-keys/answer-key1.csv", "r")
answerKeyString = answerKeyFile.read()
splitString = split_double_quotes(answerKeyString)

pollName = splitString[0]
qAndA = []

for x in range(1, len(splitString) - 1, 2):
    qAndA.append({
        splitString[x]: splitString[x + 1]
    })

poll = Poll(pollName, qAndA)
