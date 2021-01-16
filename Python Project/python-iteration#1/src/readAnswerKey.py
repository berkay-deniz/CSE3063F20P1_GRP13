import re
from models.Poll import *


answerKeyFile = open("../../answer-keys/answer-key1.csv", "r")
answerKeyString = answerKeyFile.read()
splitString = re.findall(r'\"(.+?)\"', answerKeyString)

pollName = splitString[0]
qAndA = []

for x in range(1, len(splitString) - 1, 2):
    qAndA.append({
        splitString[x]: splitString[x + 1]
    })

poll = Poll(pollName, qAndA)
