import re
import pandas as pd
from models.Student import *
from models.Poll import *


class ZoomPollAnalyzer:
    students = []
    polls = []

    # readStudents function takes name of a file which contains all students and their informations
    # then save them into students list
    def read_students(self, studentList):
        pd.set_option('expand_frame_repr', False)
        pd.set_option("display.max_columns", 999)
        pd.set_option('display.max_rows', 999)

        df = pd.read_excel(studentList, header=12, usecols='C,E,H')

        df = df[(df['Öğrenci No'].notnull()) & (df['Öğrenci No'] != 'Öğrenci No')]

        df["FullName"] = df["Adı"] + " " + df["Soyadı"]

        del df['Adı']
        del df['Soyadı']

        df.to_excel('../../StudentList.xlsx')

        student_df = pd.read_excel('../../StudentList.xlsx')
        student_df = student_df.drop(student_df.columns[0], axis=1)
        student_list = student_df.values.tolist()
        for s in student_list:
            self.students.append(Student(*s, None))

    def create_polls(self):
        answer_key_file = open("../../answer-keys/answer-key1.csv", "r")
        answer_key_string = answer_key_file.read()
        split_string = re.findall(r'\"(.+?)\"', answer_key_string)

        poll_name = split_string[0]
        q_and_a = []

        for x in range(1, len(split_string) - 1, 2):
            q_and_a.append({
                split_string[x]: split_string[x + 1]
            })

        poll = Poll(poll_name, q_and_a)

    def read_poll_reports(self, pollReport):
        df = pd.read_csv(pollReport, header=None, skiprows=[0])
        # print(df.iloc[0].values[1])

        current_poll = None
        first_question = None

        for r in range(0, len(df) - 1):
            row = df.iloc[r].values
            student = None
            name = row[1]
            email = row[2]
            for s in self.students:
                if s.match(name):
                    student = s

            if student is None:
                # print(name)
                continue
            # else:
                # print("Listed name: " + student.name)
                # print("Poll name: " + name)

            #if row[4] != first_question:

            for c in range(4, len(row) - 1, 2):
                print("question: " + row[c]+"  answer: " + row[c + 1])


    def start_system(self):
        self.read_students("../../CES3063_Fall2020_rptSinifListesi.XLS")
        self.read_poll_reports("../../CSE3063_20201124_Tue_zoom_PollReport.csv")


zoomPollAnalyzer = ZoomPollAnalyzer()
zoomPollAnalyzer.start_system()
