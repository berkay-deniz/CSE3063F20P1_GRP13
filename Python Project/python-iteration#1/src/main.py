import os
import re
import pandas as pd
import logging
from models.Student import *
from models.Poll import *
from models.AnswerKey import *


class ZoomPollAnalyzer:
    matched_students = {}
    students = {}
    polls = []
    total_attendance_polls = 0
    answer_key_list = []

    # readStudents function takes name of a file which contains all students and their informations
    # then save them into students list
    def read_students(self, student_list):
        pd.set_option('expand_frame_repr', False)
        pd.set_option("display.max_columns", 999)
        pd.set_option('display.max_rows', 999)

        df = pd.read_excel(student_list, header=12, usecols='C,E,H')

        df = df[(df['Öğrenci No'].notnull()) & (df['Öğrenci No'] != 'Öğrenci No')]

        df["FullName"] = df["Adı"] + " " + df["Soyadı"]

        del df['Adı']
        del df['Soyadı']

        df.to_excel('../../StudentList.xlsx')

        student_df = pd.read_excel('../../StudentList.xlsx')
        student_df = student_df.drop(student_df.columns[0], axis=1)
        student_list = student_df.values.tolist()
        for s in student_list:
            # s[0]
            self.students[s[0]] = Student(*s, None)

    def read_ans_key(self, file_path):
        answer_key_file = open(file_path, "r")
        answer_key_string = answer_key_file.read()
        regex = re.compile(r'\"(.+?)\"', flags=re.DOTALL)
        split_string = regex.findall(answer_key_string)

        poll_name = split_string[0]
        q_and_a = []

        for x in range(1, len(split_string) - 1, 2):
            q_and_a.append(split_string[x])
            q_and_a.append(split_string[x + 1])

        answer_key = AnswerKey(poll_name, q_and_a)
        self.answer_key_list.append(answer_key)

    def read_poll_report(self, file_path):
        df = pd.read_csv(file_path, header=None, skiprows=[0])
        # print(df.iloc[0].values[1])

        current_poll = None
        first_q_of_current_poll = None

        attendance_str = "Are you attending this lecture?"

        for r in range(0, len(df) - 1):
            row = df.iloc[r].values
            student = None
            name = row[1]

            # If the student with the same name is matched before, get the student from matched_students.
            if name in self.matched_students:
                student = self.matched_students[name]
            # Otherwise, look up for all students and match the name.
            else:
                for s in self.students.values():
                    if s.match(name):
                        student = s
                        student.email = row[2]

                if student is None:
                    max_similarity = 0.63
                    for s in self.students.values():
                        similarity = s.similarity(name)
                        if similarity > max_similarity:
                            max_similarity = similarity
                            student = s

            if student is None:
                # print("Not matched: " + name)
                continue
            else:
                # Save student match to matched_students in order not to need to match the student again.
                self.matched_students[name] = student
                # print("Listed name: " + student.name)
                # print("Poll name: " + name)
                # print()

            # Boolean value to check if there is an answer key - poll questions match
            answer_key_available = True

            # row[4] gives the first question answered by current student
            if row[4] != first_q_of_current_poll:
                answer_key_available = False
                if row[4] == attendance_str:
                    first_q_of_current_poll = attendance_str
                    self.total_attendance_polls += 1
                else:
                    for answer_key in self.answer_key_list:
                        if all(row[4:len(row) - 1:2] == answer_key.q_and_a[0:len(answer_key.q_and_a):2]):
                            poll_occurrence = 1
                            for poll in self.polls:
                                # Update poll name as poll-2 if poll already exists.
                                # or update poll name as poll-3 if poll-2 already exists.
                                if (poll_occurrence == 1 and poll.name == answer_key.poll_name) or \
                                        (poll.name == answer_key.poll_name + "-" + str(poll_occurrence)):
                                    poll_occurrence += 1

                            poll_name = answer_key.poll_name if poll_occurrence == 1 \
                                else answer_key.poll_name + "-" + str(poll_occurrence)
                            current_poll = Poll(poll_name, answer_key)
                            self.polls.append(current_poll)
                            first_q_of_current_poll = current_poll.answer_key.q_and_a[0]
                            answer_key_available = True
                            break

                    if not answer_key_available:
                        ans_key_index = 0
                        row_index = 4
                        # Iterate through row columns and current poll's answer keys to check if the all questions that
                        # student answered is also in the current poll's answer key.
                        while current_poll is not None:
                            if row[row_index] == current_poll.answer_key.q_and_a[ans_key_index]:
                                row_index += 2
                            ans_key_index += 2

                            # If the loop reaches to end of the row columns, this means every question student answered
                            # is also in the answer key, so we understand that the same poll is being processed.
                            if len(row) == row_index:
                                answer_key_available = True
                                break
                            # If the loop reaches to end of the answer key, the questions in the answer key don't match
                            # with the questions in the current row, so we understand that a new poll has arrived.
                            elif len(current_poll.answer_key.q_and_a) == ans_key_index:
                                break

            if not answer_key_available:
                # TODO: Print info about no answer key found only for one student
                continue

            if first_q_of_current_poll == attendance_str:
                student.attendance += 1
            else:
                for c in range(4, len(row) - 1, 2):
                    if pd.notnull(row[c]):
                        current_poll.save_student_answer(student, row[c], row[c + 1])

    def read_files_in_folder(self, folder_path, file_type):
        folder_path = "../../" + folder_path
        if not os.path.exists(folder_path):
            logging.error(file_type + " path given does not exist.")
            exit(-1)
        if os.path.isdir(folder_path):
            for file in os.listdir(folder_path):
                file_path = folder_path + "/" + file
                if os.path.isfile(file_path):
                    if file_type == "Answer key":
                        self.read_ans_key(file_path)
                    elif file_type == "Poll report":
                        self.read_poll_report(file_path)
        else:
            logging.error(file_type + " path given is not a directory.")
            exit(-1)

    def attendance_report(self, student_list):
        attendance_df = pd.read_excel(student_list, usecols='B,C')
        attendance_df.insert(2, "Number Of Attendance Polls", self.total_attendance_polls)
        attendance_df.insert(3, "Attendance Rate", ' ')
        attendance_df.insert(4, "Attendance Percentage", 0)

        for i in range(0, len(self.students.values())):
            current_student = self.students[attendance_df.at[i, 'Öğrenci No']]
            attendance_df.at[i, 'Attendance Rate'] = (
                    str(current_student.attendance) + ' of ' + str(self.total_attendance_polls))
            attendance_df.at[i, 'Attendance Percentage'] = (
                                                                   current_student.attendance / self.total_attendance_polls) * 100

        attendance_df.to_excel("attendance.xlsx")

    def print_poll_results(self, student_list):

        for poll in self.polls:
            poll_result_df = pd.read_excel(student_list, usecols='B,C')

            question_no = 1
            # Insert one column for each question in the poll
            for j in range(0, len(poll.answer_key.q_and_a), 2):
                poll_result_df.insert(question_no + 1, "Q" + str(question_no), 0)
                question_no += 1

            poll_result_df.insert(question_no + 1, "Number of Questions", question_no - 1)
            poll_result_df.insert(question_no + 2, "Success Rate", ' ')
            poll_result_df.insert(question_no + 3, "Success Percentage", 0)

            for i in range(0, len(self.students.values())):
                student = self.students[poll_result_df.at[i, 'Öğrenci No']]
                for
                attendance_df.at[i, 'Attendance Rate'] = (str(student.attendance) + ' of ' + str(
                    self.total_attendance_polls))
                attendance_df.at[i, 'Attendance Percentage'] = (self.students[
                                                                    attendance_df.at[i, 'Öğrenci No']].attendance /
                                                                self.total_attendance_polls) * 100

                ans_key_index = 0
                student_answers_index = 0
                while True:
                    if self.polls[i].student_answers[student_answers_index] == self.polls[i].answer_key.q_and_a[
                        ans_key_index]:
                        student_answers_index += 2
                    ans_key_index += 2

                    if len(row) == student_answers_index:
                        answer_key_available = True
                        break
                    elif len(current_poll.answer_key.q_and_a) == ans_key_index:
                        break

            if not os.path.exists('../../poll-results'):
                os.makedirs('../../poll-results')
            poll_result_df.to_excel("../../poll-results/" + self.polls[i].name + ".xlsx")

    def start_system(self):
        self.read_students("../../CES3063_Fall2020_rptSinifListesi.XLS")
        ans_keys_path = input("Enter answer keys directory: ")
        self.read_files_in_folder(ans_keys_path, "Answer key")
        poll_reports_path = input("Enter poll reports directory: ")
        self.read_files_in_folder(poll_reports_path, "Poll report")
        # for student in self.students:
        #   print(student.name + ": " + str(student.attendance))
        #   if student.name == "BERKAY DENİZ":
        #        for v in self.polls[0].student_answers[student].values():
        #            print(v)
        print()
        self.attendance_report("../../StudentList.xlsx")
        self.print_poll_results("../../StudentList.xlsx")


zoomPollAnalyzer = ZoomPollAnalyzer()
zoomPollAnalyzer.start_system()
