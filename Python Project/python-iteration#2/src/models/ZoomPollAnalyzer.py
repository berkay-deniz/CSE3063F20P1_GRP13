import logging

import matplotlib.pyplot as plotter
import pandas as pd

from datetime import datetime
from .Anomaly import *
from .AnswerKey import *
from .Poll import *
from .Student import *
from .Question import *
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
                    correct_answers.append(answer_text)
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
        tokens = None

        checked_students = set()

        file = open(file_path,encoding="utf8")
        lines_to_read = [3, 3]
        for position, line in enumerate(file):
            if position in lines_to_read:
                tokens = line.split(",")
                break

        file.close()

        meeting = None
        meeting_topic = tokens[0]
        meeting_id = tokens[1]
        date = tokens[2]

        for m in self.meetings:
            if m.meeting_id == meeting_id:
                meeting = m

        if meeting is None:
            meeting = Meeting(meeting_id, meeting_topic)
            self.meetings.append(meeting)

        df = pd.read_csv(file_path, header=None, skiprows=6, names=list(range(0, 25)), dtype=str)

        attendance_checked = False
        current_poll = None
        first_q_of_current_poll = None

        for r in range(0, len(df) - 1):
            row = df.iloc[r].values
            name = row[1]
            email = row[2]

            student = self.match_student(name, email)

            # Boolean value to check if there is an answer key - poll questions match
            answer_key_available = True

            # row[4] gives the first question answered by current student
            if row[4] != first_q_of_current_poll:
                answer_key_available = False

                for answer_key in self.answer_key_list:
                    is_matched = True
                    for question_in_report in row[4:len(row) - 1:2]:
                        if pd.isnull(question_in_report):
                            break
                        for question in answer_key.questions:
                            if question.text == question_in_report:
                                break
                        else:
                            # Executing when the inner loop does NOT BREAK
                            is_matched = False
                            break
                        # Executing when the inner loop BREAK
                        continue

                    if is_matched:
                        current_poll = Poll(answer_key)
                        meeting.polls.append(current_poll)
                        first_q_of_current_poll = row[4]
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

            if student is None:
                if current_poll is not None:
                    current_poll.anomalies.append(Anomaly(name, email))
                continue
            else:
                # Save student match to matched_students in order not to need to match the student again.
                self.matched_students[name] = student

            if not answer_key_available:
                # This means that it is an attendance poll
                if not attendance_checked:
                    self.total_days += 1
                if student not in checked_students:
                    student.attendance += 1
                    checked_students.add(student)
                attendance_checked = True
                continue

            # Set the date of the poll
            current_poll.date = date
            for c in range(4, len(row) - 1, 2):
                if pd.isnull(row[c]):
                    break
                answers = []
                if "; " in row[c + 1]:
                    answers.append(row[c + 1])
                else:
                    answer_strings = row[c + 1].split(";")
                    for answer_string in answer_strings:
                        answers.append(answer_string)
                current_poll.save_student_answers(student, row[c], answers)

        # Count any poll as an attendance poll if there is no attendance poll in the meeting.
        if not attendance_checked:
            self.total_days += 1
            for r in range(0, len(df) - 1):
                row = df.iloc[r].values
                name = row[1]
                email = row[2]

                student = self.match_student(name, email)

                if student is None:
                    continue

                if student not in checked_students:
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

    def print_attendance_report(self):
        attendance_df = pd.read_excel(self.configuration.organized_student_list_file_path, usecols='B,C')
        attendance_df.insert(2, "Total Meeting Days", self.total_days)
        attendance_df.insert(3, "Attendance", 0)
        attendance_df.insert(4, "Attendance Rate", 0.0)
        attendance_df.insert(5, "Attendance Percentage", 0)

        for i in range(0, len(self.students.values())):
            current_student = self.students[attendance_df.at[i, 'Öğrenci No']]
            attendance_df.at[i, 'Attendance'] = current_student.attendance
            attendance_df.at[i, 'Attendance Rate'] = current_student.attendance / self.total_days
            attendance_df.at[i, 'Attendance Percentage'] = (current_student.attendance / self.total_days) * 100

        attendance_df.to_excel(self.configuration.attendance_report_file_path)
        logging.info("Attendances of the students printed to an excel file successfully.")

    def make_autopct(self, values):
        def my_autopct(pct):
            total = sum(values)
            val = int(round(pct * total / 100.0))
            return '{v:d}'.format(v=val)

        return my_autopct

    def is_answer_true(self, question, answer, poll):
        for x in range(0, len(poll.answer_key.questions), 1):
            if question == poll.answer_key.questions[x].text:
                for correct_answer in poll.answer_key.questions[x].correct_answers:
                    if answer == correct_answer:
                        return True
                return False

    def print_pie_charts(self, answers_of_questions, poll):
        question_list = []

        num_of_questions = len(poll.answer_key.questions)
        fig, axs = plotter.subplots(num_of_questions, 2, figsize=(30, num_of_questions * 15))
        plotter.subplots_adjust(wspace=1, hspace=1)
        i = 0
        for question, answer_occurrences in answers_of_questions.items():
            pie_location = axs[0] if num_of_questions == 1 else axs[i, 0]
            bar_location = axs[1] if num_of_questions == 1 else axs[i, 1]
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
            pie_location.pie(occurrence_list, labels=ans_labels, autopct=self.make_autopct(occurrence_list),
                             shadow=True)
            question_string = "Question: " + question if len(question) < 150 else question[:int(
                len(question) / 2)] + "\n" + question[int(len(question) / 2):]
            pie_location.title.set_text(question_string)
            bar_location.bar(ans_labels, occurrence_list, width=0.8,
                             color=["#009900" if self.is_answer_true(question, answer, poll) else "#b20000" for answer
                                    in ans_list], bottom=None, align='center', data=occurrence_list)
            bar_location.title.set_text(question_string)
            label_str = ""
            for j in range(0, len(ans_list)):
                label_str = label_str + "\n" + ans_labels[j] + ": " + ans_list[j]
            pie_location.set_xlabel(label_str)
            bar_location.set_xlabel(label_str)
            i += 1
        folder_path = self.configuration.plots_dir_path
        if not os.path.exists(folder_path):
            os.makedirs(folder_path)
        plotter.savefig(folder_path + "/" + poll.name + ".pdf")
        logging.info("Plots of the poll named '" + poll.name + "' printed to a pdf file successfully.")

    def print_global_results(self, poll_dfs):
        all_polls = []
        for meeting in self.meetings:
            all_polls += meeting.polls

        all_polls.sort(key=lambda current_poll: datetime.strptime(current_poll.date, "%Y-%m-%d %H:%M:%S"))

        student_result_df = pd.read_excel(self.configuration.organized_student_list_file_path)
        column = len(student_result_df.columns)

        total_num_of_questions = 0
        for poll in all_polls:
            if poll not in poll_dfs.keys():
                continue

            total_num_of_questions += len(poll.answer_key.questions)

            name = poll.name
            occurrence = 2
            while name + " Date" in student_result_df.columns:
                name = poll.name + " (" + str(occurrence) + ")"
                occurrence += 1

            column_name = "Poll_" + poll.poll_id + "_" + poll.name.replace(" ", "_") + "_" \
                          + poll.date.replace(" ", "_").replace("-", "_").replace(":", "_")
            student_result_df.insert(column, column_name, poll.date)
            column += 1

            df = poll_dfs[poll]
            student_result_df[column_name] = df["Correct Answers"]

        student_result_df.insert(column, "Total Accuracy (%)", 0)
        student_result_df["Total Accuracy (%)"] = \
            100 * student_result_df.iloc[:, 3 - len(student_result_df.columns):].sum(axis=1) / total_num_of_questions

        student_result_df.to_excel(self.configuration.global_analytics_file_path, index=False)
        logging.info("Results of the students for each poll printed to an excel file successfully.")

    def print_poll_results(self):
        poll_dfs = {}
        for meeting in self.meetings:
            for poll in meeting.polls:
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
                poll_result_df.insert(question_no + 5, "Success", 0.0)
                poll_result_df.insert(question_no + 6, "Success (%)", 0)

                answers_of_questions = {}
                poll.print_student_results(answers_of_questions, poll_result_df, self.students, self.configuration)
                self.print_pie_charts(answers_of_questions, poll)

                poll_dfs[poll] = poll_result_df

                if not os.path.exists(self.configuration.poll_results_dir_path):
                    os.makedirs(self.configuration.poll_results_dir_path)
                poll_result_df.to_excel(self.configuration.poll_results_dir_path + "/"
                                        + "Poll_" + poll.poll_id + "_" + poll.name.replace(" ", "_") + "_"
                                        + poll.date.replace(" ", "_").replace("-", "_").replace(":", "_") + ".xlsx")
                logging.info("Results of the poll named '" + poll.name + "' printed to an excel file successfully.")

            self.print_global_results(poll_dfs)

    def print_absences_and_anomalies(self):
        for meeting in self.meetings:
            for poll in meeting.polls:
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
        self.print_attendance_report()
        self.print_poll_results()
        self.print_absences_and_anomalies()
