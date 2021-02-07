from .Anomaly import *
from .AnswerKey import *
from .Configuration import *
from .Meeting import *
from .OutputManager import *
from .Poll import *
from .Question import *
from .Student import *


class ZoomPollAnalyzer:

    def __init__(self):
        self.configuration = Configuration()
        self.matched_students = {}
        self.students = {}
        self.meetings = []
        self.total_days = 0
        self.answer_key_list = []
        self.processed_dates = set()
        self.output_manager = OutputManager(self.configuration)

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
            if s.match(name, self.configuration):
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

        file = open(file_path, encoding="utf8")
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

        if date in self.processed_dates:
            logging.error("The session with date " + date + " has already processed.")
            return

        self.processed_dates.add(date)

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
        self.output_manager.print_attendance_report(self.total_days, self.students)
        self.output_manager.print_poll_results(self.meetings, self.students)
        self.output_manager.print_absences_and_anomalies(self.meetings, self.students)
