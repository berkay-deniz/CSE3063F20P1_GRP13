import json
import os


class Poll:
    def __init__(self, answer_key):
        self.poll_id = answer_key.poll_id
        self.name = answer_key.name
        self.answer_key = answer_key
        self.student_answers = {}
        self.date = None
        self.anomalies = []
        self.absents = []

    def save_student_answers(self, student, question, answers):
        if student not in self.student_answers:
            self.student_answers[student] = {}
        self.student_answers[student][question] = answers

    def print_student_results(self, answers_of_questions, poll_result_df, students):
        num_of_questions = len(self.answer_key.questions)
        for i in range(0, len(students.values())):
            student = students[poll_result_df.at[i, 'Öğrenci No']]
            num_of_correct_ans = 0
            ans_key_index = 0
            # If current student have not entered the current poll, continue with the next student.
            if student not in self.student_answers:
                continue
            # Get answers of the student
            all_std_answers = self.student_answers[student]
            num_of_answered_questions = len(all_std_answers)
            while True:
                question = self.answer_key.questions[ans_key_index]
                question_text = question.text
                correct_answers = question.correct_answers
                # First time the question answered
                if question_text not in answers_of_questions:
                    answers_of_questions[question_text] = {}
                # Get answer to the current question of the student
                std_answers = all_std_answers[question_text]
                # First time the answer encountered
                for answer in std_answers:
                    if answer not in answers_of_questions[question_text]:
                        answers_of_questions[question_text][answer] = 0
                    # Increment the occurrence of the answer
                    answers_of_questions[question_text][answer] += 1
                    for correct_answer in correct_answers:
                        if question_text in all_std_answers and answer == correct_answer:
                            poll_result_df.at[i, question_text] = 1
                            num_of_correct_ans += 1
                            break
                    else:
                        # Executing when the inner loop does NOT BREAK
                        continue
                    # Executing when the inner loop BREAK
                    break

                ans_key_index += 1

                # Break when the all questions processed
                if num_of_questions == ans_key_index:
                    break

            poll_result_df.at[i, "Correct Answers"] = num_of_correct_ans
            poll_result_df.at[i, "Wrong Answers"] = num_of_answered_questions - num_of_correct_ans
            poll_result_df.at[i, "Empty Answers"] = num_of_questions - num_of_answered_questions
            poll_result_df.at[i, 'Success'] = (1.0 * num_of_correct_ans) / num_of_questions
            poll_result_df.at[i, 'Success (%)'] = 100 * num_of_correct_ans / num_of_questions

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
