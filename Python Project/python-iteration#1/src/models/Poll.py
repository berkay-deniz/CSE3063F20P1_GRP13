import json
import os


class Poll:
    def __init__(self, name, answer_key):
        self.name = name
        self.answer_key = answer_key
        self.student_answers = {}
        self.date = None
        self.anomalies = []
        self.absents = []

    def save_student_answer(self, student, question, answer):
        if student not in self.student_answers:
            self.student_answers[student] = {}
        self.student_answers[student][question] = answer

    def print_student_results(self, answers_of_questions, poll_result_df, students):
        for i in range(0, len(students.values())):
            student = students[poll_result_df.at[i, 'Öğrenci No']]
            num_of_correct_ans = 0
            ans_key_index = 0
            while True:
                q_and_a = self.answer_key.q_and_a
                question = q_and_a[ans_key_index]
                correct_ans = q_and_a[ans_key_index + 1]
                # If current student have not entered the current poll, continue with the next student.
                if student not in self.student_answers:
                    break
                # First time the question answered
                if question not in answers_of_questions:
                    answers_of_questions[question] = {}
                # Get answers of the student
                std_answers = self.student_answers[student]
                # Get answer to the current question of the student
                std_answer = std_answers[question]
                # First time the answer encountered
                if std_answer not in answers_of_questions[question]:
                    answers_of_questions[question][std_answer] = 0
                # Increment the occurrence of the answer
                answers_of_questions[question][std_answer] += 1
                if question in std_answers and std_answer == correct_ans:
                    poll_result_df.at[i, 'Q' + str(int(ans_key_index / 2) + 1)] = 1
                    num_of_correct_ans += 1
                ans_key_index += 2

                if len(q_and_a) == ans_key_index:
                    break

            poll_result_df.at[i, 'Success'] = str(num_of_correct_ans) + " of " + str(int(len(q_and_a) / 2))
            poll_result_df.at[i, 'Success (%)'] = 100 * num_of_correct_ans / (len(q_and_a) / 2)

    def print_anomalies(self):
        folder_path = "../../poll-anomalies"
        if not os.path.exists(folder_path):
            os.makedirs(folder_path)
        json_str = json.dumps([anomaly.to_dict() for anomaly in self.anomalies], indent=4)
        f = open(folder_path + "/" + self.name + ".json", "w")
        f.write(json_str)
        f.close()

    def print_absences(self, students):
        folder_path = "../../poll-absences"
        if not os.path.exists(folder_path):
            os.makedirs(folder_path)

        for student in students.values():
            if student not in self.student_answers.keys():
                self.absents.append(student)

        json_str = json.dumps([student.to_dict() for student in self.absents], indent=4)
        f = open(folder_path + "/" + self.name + ".json", "w")
        f.write(json_str)
        f.close()
