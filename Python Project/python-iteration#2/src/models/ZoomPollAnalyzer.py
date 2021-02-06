import logging

import matplotlib.pyplot as plotter
import pandas as pd

from .Anomaly import *
from .AnswerKey import *
from .Poll import *
from .Student import *
from .Question import *
from .Answer import *
from .Meeting import *
from .Configuration import *


class ZoomPollAnalyzer:

    def __init__(self):
        self.configuration = Configuration()
        self.matched_students = {}
        self.students = {}
        self.meetings = []
        self.total_days = 0
        self.answer_key_list = []
        self.polls=[]

    # readStudents function takes name of a file which contains all students and their informations
    # then save them into students list
    def read_students(self):
        pd.set_option('expand_frame_repr', False)
        pd.set_option("display.max_columns", 999)
        pd.set_option('display.max_rows', 999)

        df = pd.read_excel(self.configuration.student_list_file_path, header=12, usecols='C,E,H')

        df = df[(df['Öğrenci No'].notnull()) & (df['Öğrenci No'] != 'Öğrenci No')]

        df["FullName"] = df["Adı"] + " " + df["Soyadı"]

        del df['Adı']
        del df['Soyadı']

        df.to_excel(self.configuration.organized_student_list_file_path)

        student_df = pd.read_excel(self.configuration.organized_student_list_file_path)
        student_df = student_df.drop(student_df.columns[0], axis=1)
        students_file = student_df.values.tolist()
        for s in students_file:
            self.students[s[0]] = Student(*s, None)

    def read_ans_key(self, file_path):
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

    def match_student(self, name, email):
        # If the student with the same name is matched before, get the student from matched_students.
        if name in self.matched_students:
            return self.matched_students[name]

        # Otherwise, look up for all students and match the name.
        student = None
        for s in self.students.values():
            if s.match(name, self.configuration.exact_match_threshold, self.configuration.partial_match_threshold):
                student = s
                student.email = email
                logging.info(name + ' is matched with ' + s.name)

        if student is None:
            max_similarity = self.configuration.max_similarity_threshold
            for s in self.students.values():
                similarity = s.calculate_similarity(name)
                if similarity > max_similarity:
                    max_similarity = similarity
                    student = s
                    student.email = email
                    logging.info(name + ' is matched with ' + s.name)

        return student

    def read_poll_report(self, file_path):
        df = pd.read_csv(file_path, header=None)

        meeting = None
        meeting_topic = df.iloc[3][0]
        meeting_id = df.iloc[3][1]
        date = df.iloc[3][2]
        for m in self.meetings:
            if m.meeting_id == meeting_id:
                meeting = m

        if meeting is None:
            meeting = Meeting(meeting_id, meeting_topic)
            self.meetings.append(meeting)

        attendance_checked = False
        current_poll = None
        first_q_of_current_poll = None

        for r in range(6, len(df) - 1):
            row = df.iloc[r].values
            name = row[1]
            email = row[2]

            student = self.match_student(name, email)

            if student is None:
                current_poll.anomalies.append(Anomaly(name, email))
                continue
            else:
                # Save student match to matched_students in order not to need to match the student again.
                self.matched_students[name] = student

            # Boolean value to check if there is an answer key - poll questions match
            answer_key_available = True

            # row[4] gives the first question answered by current student
            if row[4] != first_q_of_current_poll:
                answer_key_available = False

                for answer_key in self.answer_key_list:
                    if all(row[4:len(row) - 1:2] == answer_key.questions[0:len(answer_key.questions):1].text):
                        current_poll = Poll(answer_key)
                        meeting.polls.append(current_poll)
                        first_q_of_current_poll = current_poll.answer_key.questions[0].text
                        answer_key_available = True
                        break

                    if not answer_key_available:
                        ans_key_index = 0
                        row_index = 4
                        # Iterate through row columns and current poll's answer keys to check if the all questions that
                        # student answered is also in the current poll's answer key.
                        while current_poll is not None:
                            if row[row_index] == current_poll.answer_key.questions[ans_key_index].text:
                                row_index += 2
                            ans_key_index += 1

                            # If the loop reaches to end of the row columns, this means every question student answered
                            # is also in the answer key, so we understand that the same poll is being processed.
                            if len(row) == row_index:
                                answer_key_available = True
                                break
                            # If the loop reaches to end of the answer key, the questions in the answer key don't match
                            # with the questions in the current row, so we understand that a new poll has arrived.
                            elif len(current_poll.answer_key.questions) == ans_key_index:
                                break

            if not answer_key_available:
                # This means that it is an attendance poll
                self.total_days += 1
                student.attendance += 1
                attendance_checked = True
                continue

            # Set the date of the poll
            current_poll.date = date
            for c in range(4, len(row) - 1, 2):
                if pd.notnull(row[c]):
                    answers = Answer(None, row[c + 1].split(";"))
                    current_poll.save_student_answers(student, row[c], answers)

        # Count any poll as an attendance poll if there is no attendance poll in the meeting.
        if not attendance_checked:
            checked_students = set()
            for r in range(6, len(df) - 1):
                row = df.iloc[r].values
                name = row[1]
                email = row[2]

                student = self.match_student(name, email)

                if student is None:
                    continue

                if student not in checked_students:
                    self.total_days += 1
                    student.attendance += 1
                    checked_students.add(student)

    def read_files_in_folder(self, folder_path, file_type):
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
        attendance_df.insert(2, "Number Of Attendance Polls", self.total_days)
        attendance_df.insert(3, "Attendance Rate", ' ')
        attendance_df.insert(4, "Attendance Percentage", 0)

        for i in range(0, len(self.students.values())):
            current_student = self.students[attendance_df.at[i, 'Öğrenci No']]
            attendance_df.at[i, 'Attendance Rate'] = (
                    str(current_student.attendance) + ' of ' + str(self.total_days))
            attendance_df.at[i, 'Attendance Percentage'] = (
                                                                   current_student.attendance / self.total_days) * 100

        attendance_df.to_excel(self.configuration.attendance_report_file_path)
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
        folder_path = self.configuration.plots_dir_path
        if not os.path.exists(folder_path):
            os.makedirs(folder_path)
        plotter.savefig(folder_path + "/" + poll.name + ".pdf")
        logging.info("Plots of the poll named '" + poll.name + "' printed to a pdf file successfully.")

    def print_student_results(self, poll_dfs):
        result_file = self.configuration.global_analytics_file_path
        file_name = result_file if os.path.exists(result_file) else self.configuration.organized_student_list_file_path
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

    def print_poll_results(self):
        poll_dfs = {}
        for poll in self.polls:
            poll_result_df = pd.read_excel(self.configuration.organized_student_list_file_path, usecols='B, C')

            question_no = 1
            # Insert one column for each question in the poll
            for question in poll.answer_key.questions:
                poll_result_df.insert(question_no + 1, question.text, 0)
                question_no += 1

            poll_result_df.insert(question_no + 1, "Questions", question_no - 1)
            poll_result_df.insert(question_no + 2, "Correct Answers", 0)
            poll_result_df.insert(question_no + 3, "Wrong Answers", 0)
            poll_result_df.insert(question_no + 4, "Empty Answers", 0)
            poll_result_df.insert(question_no + 5, "Success", 0)
            poll_result_df.insert(question_no + 6, "Success (%)", 0)

            answers_of_questions = {}
            poll.print_student_results(answers_of_questions, poll_result_df, self.students)
            self.print_pie_charts(answers_of_questions, poll)

            poll_dfs[poll] = poll_result_df

            if not os.path.exists(self.configuration.poll_results_dir_path):
                os.makedirs(self.configuration.poll_results_dir_path)
            # TODO: Change poll result file as .ods (not necessary)
            poll_result_df.to_excel(self.configuration.poll_results_dir_path + "/"
                                    + "Poll_" + poll.poll_id + "_" + poll.name.replace(" ", "_") + "_"
                                    + poll.date.replace(" ", "_").replace("-", "_").replace(":", "_") + ".xlsx")
            logging.info("Results of the poll named '" + poll.name + "' printed to an excel file successfully.")

        self.print_student_results(poll_dfs)
    #TODO: Check if it is works.
    def print_absences_and_anomalies(self):
        for poll in self.polls:
            poll.print_absences_and_anomalies(self.students, self.configuration.absences_and_anomalies_dir_path)

    def start_system(self):
        root_logger = logging.getLogger()
        root_logger.setLevel(logging.INFO)
        handler = logging.FileHandler(self.configuration.log_file_path, 'a', 'utf-8')
        formatter = logging.Formatter('%(name)s - %(asctime)s - %(levelname)s - %(message)s')
        handler.setFormatter(formatter)
        root_logger.addHandler(handler)

        self.read_students()
        self.read_files_in_folder(self.configuration.answer_keys_dir_path, "Answer key")
        self.read_files_in_folder(self.configuration.poll_reports_dir_path, "Poll report")
        self.print_attendance_report(self.configuration.attendance_report_file_path)
        self.print_poll_results()
        self.print_absences_and_anomalies()
