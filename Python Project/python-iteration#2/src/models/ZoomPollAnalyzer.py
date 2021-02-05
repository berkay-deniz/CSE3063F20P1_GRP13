import logging

import matplotlib.pyplot as plotter
import pandas as pd

from .Anomaly import *
from .AnswerKey import *
from .Poll import *
from .Student import *
from .Question import *
from .Answer import *


class ZoomPollAnalyzer:
    matched_students = {}
    students = {}
    polls = []
    total_attendance_polls = 0
    answer_key_list = []

    # readStudents function takes name of a file which contains all students and their informations
    # then save them into students list
    def read_students(self, students_file):
        pd.set_option('expand_frame_repr', False)
        pd.set_option("display.max_columns", 999)
        pd.set_option('display.max_rows', 999)

        df = pd.read_excel(students_file, header=12, usecols='C,E,H')

        df = df[(df['Öğrenci No'].notnull()) & (df['Öğrenci No'] != 'Öğrenci No')]

        df["FullName"] = df["Adı"] + " " + df["Soyadı"]

        del df['Adı']
        del df['Soyadı']

        # TODO: get from config.json
        df.to_excel('../../StudentList.xlsx')

        student_df = pd.read_excel('../../StudentList.xlsx')
        student_df = student_df.drop(student_df.columns[0], axis=1)
        students_file = student_df.values.tolist()
        for s in students_file:
            self.students[s[0]] = Student(*s, None)

    def read_ans_key(self, file_path):
        # TODO: get from config.json

        file = open(file_path, 'r')
        lines = file.readlines()[2:]
        counter = 0
        poll_id = 0
        poll_name = ''
        question_id = 0
        question_text = ''
        correct_answers = []
        questions = []
        is_first_question = True
        for line in lines:
            line = line.strip()
            if line == '':
                continue
            elif line[0: 4] == 'Poll':
                is_first_question = True
                if counter != 0:
                    questions.append(Question(question_id, question_text, correct_answers))
                    self.answer_key_list.append(AnswerKey(poll_id, poll_name, questions))
                temp = line.split(":")
                poll_id = temp[0].split()[1]
                poll_name = ' '.join(temp[1].split()[:-3])
                questions = []
                correct_answers = []
            else:
                if line.split()[0] == 'Answer':
                    temp = line.split(':', maxsplit=1)
                    answer_id = temp[0].split()[1]
                    answer_text = temp[1].strip()
                    correct_answers.append(Answer(answer_id, answer_text))
                else:
                    if not is_first_question:
                        questions.append(Question(question_id, question_text, correct_answers))
                    is_first_question = False
                    correct_answers = []
                    temp = line.split('.', maxsplit=1)
                    question_id = temp[0]
                    question_text = temp[1].replace('( Multiple Choice)', '').replace('( Single Choice)', '').strip()
            counter += 1
        questions.append(Question(question_id, question_text, correct_answers))
        self.answer_key_list.append(AnswerKey(poll_id, poll_name, questions))

    def read_poll_report(self, file_path):
        df = pd.read_csv(file_path, header=None, skiprows=[0])

        current_poll = None
        first_q_of_current_poll = None

        # TODO: If the poll does not exist in answer keys files then it is an attendance poll.
        attendance_str = "Are you attending this lecture?"

        for r in range(0, len(df) - 1):
            row = df.iloc[r].values
            student = None
            name = row[1]
            email = row[2]

            # If the student with the same name is matched before, get the student from matched_students.
            if name in self.matched_students:
                student = self.matched_students[name]
            # Otherwise, look up for all students and match the name.
            else:
                for s in self.students.values():
                    if s.match(name):
                        student = s
                        student.email = email
                        logging.info(name + ' is matched with ' + s.name)

                if student is None:
                    # TODO: get from config.json
                    max_similarity = 0.63
                    for s in self.students.values():
                        similarity = s.calculate_similarity(name)
                        if similarity > max_similarity:
                            max_similarity = similarity
                            student = s
                            logging.info(name + ' is matched with ' + s.name)

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
                continue

            if student is None:
                current_poll.anomalies.append(Anomaly(name, email))
                continue
            else:
                # Save student match to matched_students in order not to need to match the student again.
                self.matched_students[name] = student

            if first_q_of_current_poll == attendance_str:
                student.attendance += 1
            else:
                # TODO: get date from the meeting object NOT here
                # Set the date of the poll
                if current_poll.date is None:
                    current_poll.date = row[3][:-9]
                for c in range(4, len(row) - 1, 2):
                    if pd.notnull(row[c]):
                        # TODO: split by semicolon
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

    def print_attendance_report(self, students_file):
        attendance_df = pd.read_excel(students_file, usecols='B,C')
        attendance_df.insert(2, "Number Of Attendance Polls", self.total_attendance_polls)
        attendance_df.insert(3, "Attendance Rate", ' ')
        attendance_df.insert(4, "Attendance Percentage", 0)

        for i in range(0, len(self.students.values())):
            current_student = self.students[attendance_df.at[i, 'Öğrenci No']]
            attendance_df.at[i, 'Attendance Rate'] = (
                    str(current_student.attendance) + ' of ' + str(self.total_attendance_polls))
            attendance_df.at[i, 'Attendance Percentage'] = (
                                                                   current_student.attendance / self.total_attendance_polls) * 100

        attendance_df.to_excel("../../attendance.xlsx")
        logging.info("Attendances of the students printed to an excel file successfully.")

    def make_autopct(self, values):
        def my_autopct(pct):
            total = sum(values)
            val = int(round(pct * total / 100.0))
            return '{v:d}'.format(v=val)

        return my_autopct

    def is_answer_true(self, question, answer, poll):
        for x in range(0, len(poll.answer_key.q_and_a), 2):
            if question == poll.answer_key.q_and_a[x]:
                if answer == poll.answer_key.q_and_a[x + 1]:
                    return True
                else:
                    return False

    def print_pie_charts(self, answers_of_questions, poll):
        question_list = []

        fig, axs = plotter.subplots(int(len(poll.answer_key.q_and_a) / 2), 2, figsize=(30, 100))
        plotter.subplots_adjust(wspace=1, hspace=1)
        i = 0
        for question, answer_occurrences in answers_of_questions.items():
            current_label_unicode = ord("A")
            ans_labels = []
            ans_list = []
            occurrence_list = []
            question_list.append(question)
            for answer, occurrence in answer_occurrences.items():
                ans_list.append(answer)
                ans_labels.append(chr(current_label_unicode))
                current_label_unicode += 1
                occurrence_list.append(occurrence)
            axs[i, 0].pie(occurrence_list, labels=ans_labels, autopct=self.make_autopct(occurrence_list), shadow=True)
            axs[i, 0].title.set_text("Question: " + question)
            axs[i, 1].bar(ans_labels, occurrence_list, width=0.8,
                          color=["#009900" if self.is_answer_true(question, answer, poll) else "#b20000" for answer in
                                 ans_list], bottom=None, align='center', data=occurrence_list)
            axs[i, 1].title.set_text("Question: " + question)
            label_str = ""
            for j in range(0, len(ans_list)):
                label_str = label_str + "\n" + ans_labels[j] + ": " + ans_list[j]
            axs[i, 0].set_xlabel(label_str)
            axs[i, 1].set_xlabel(label_str)
            i += 1
        folder_path = "../../poll-plots"
        if not os.path.exists(folder_path):
            os.makedirs(folder_path)
        plotter.savefig(folder_path + "/" + poll.name + ".pdf")
        logging.info("Plots of the poll named '" + poll.name + "' printed to a pdf file successfully.")

    def print_student_results(self, students_file, poll_dfs):
        result_file = "../../StudentResults.xlsx"
        file_name = result_file if os.path.exists(result_file) else students_file
        student_result_df = pd.read_excel(file_name)
        column = len(student_result_df.columns)
        for poll in self.polls:
            name = poll.name
            occurrence = 2
            while name + " Date" in student_result_df.columns:
                name = poll.name + " (" + str(occurrence) + ")"
                occurrence += 1

            student_result_df.insert(column, name + " Date", poll.date)
            student_result_df.insert(column + 1, name + " Questions", 0)
            student_result_df.insert(column + 2, name + " Success (%)", 0)
            column += 3

            df = poll_dfs[poll]
            student_result_df[name + " Questions"] = df["Questions"]
            student_result_df[name + " Success (%)"] = df["Success (%)"]

        student_result_df.to_excel(result_file, index=False)
        logging.info("Results of the students for each poll printed to an excel file successfully.")

    def print_poll_results(self, students_file):
        poll_dfs = {}
        for poll in self.polls:
            poll_result_df = pd.read_excel(students_file, usecols='B, C')

            question_no = 1
            # Insert one column for each question in the poll
            for j in range(0, len(poll.answer_key.q_and_a), 2):
                poll_result_df.insert(question_no + 1, "Q" + str(question_no), 0)
                question_no += 1

            poll_result_df.insert(question_no + 1, "Questions", question_no - 1)
            poll_result_df.insert(question_no + 2, "Success", ' ')
            poll_result_df.insert(question_no + 3, "Success (%)", 0)

            answers_of_questions = {}
            poll.print_student_results(answers_of_questions, poll_result_df, self.students)
            self.print_pie_charts(answers_of_questions, poll)

            poll_dfs[poll] = poll_result_df

            folder_path = "../../poll-results"
            if not os.path.exists(folder_path):
                os.makedirs(folder_path)
            poll_result_df.to_excel(folder_path + "/" + poll.name + ".xlsx")
            logging.info("Results of the poll named '" + poll.name + "' printed to an excel file successfully.")

        self.print_student_results(students_file, poll_dfs)

    def print_absences_and_anomalies(self):
        for poll in self.polls:
            poll.print_anomalies()
            poll.print_absences(self.students)

    def start_system(self):
        root_logger = logging.getLogger()
        root_logger.setLevel(logging.INFO)
        handler = logging.FileHandler('../../logFile.log', 'a', 'utf-8')
        formatter = logging.Formatter('%(name)s - %(asctime)s - %(levelname)s - %(message)s')
        handler.setFormatter(formatter)
        root_logger.addHandler(handler)

        students_file = input("Enter the student list file: ")
        self.read_students("../../" + students_file)
        ans_keys_path = input("Enter answer keys directory: ")
        self.read_files_in_folder(ans_keys_path, "Answer key")
        poll_reports_path = input("Enter poll reports directory: ")
        self.read_files_in_folder(poll_reports_path, "Poll report")
        self.print_attendance_report("../../StudentList.xlsx")
        self.print_poll_results("../../StudentList.xlsx")
        self.print_absences_and_anomalies()
