import json
import os
import pandas as pd


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

    def print_student_results(self, answers_of_questions, poll_result_df, students, configuration):
        student_poll_df = pd.DataFrame(columns=["Question Text", "Correct Answers", "Given Answers", "Correct"])
        num_of_questions = len(self.answer_key.questions)

        if not os.path.exists(configuration.student_poll_results_dir_path):
            os.makedirs(configuration.student_poll_results_dir_path)

        for student_index in range(0, len(students.values())):
            student = students[poll_result_df.at[student_index, 'Öğrenci No']]
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
                # Save question text to student poll file
                student_poll_df.at[ans_key_index + 1, "Question Text"] = question_text
                student_poll_df.at[ans_key_index + 1, "Correct"] = 0
                correct_answers = question.correct_answers
                # Save correct answers to student poll file
                correct_answers_string = ""
                for correct_answer in correct_answers:
                    correct_answers_string = correct_answers_string + correct_answer + ";"
                correct_answers_string = correct_answers_string[0: -1]
                student_poll_df.at[ans_key_index + 1, "Correct Answers"] = correct_answers_string

                # First time the question answered
                if question_text not in answers_of_questions:
                    answers_of_questions[question_text] = {}
                # Get given answers to the current question of the student
                std_answers = all_std_answers[question_text]
                # Save given answers to the student poll file
                given_answers_string = ""
                for given_answer in std_answers:
                    given_answers_string = given_answers_string + given_answer + ";"
                given_answers_string = given_answers_string[0: -1]
                student_poll_df.at[ans_key_index + 1, "Given Answers"] = given_answers_string

                for answer in std_answers:
                    # First time the answer encountered
                    if answer not in answers_of_questions[question_text]:
                        answers_of_questions[question_text][answer] = 0
                    # Increment the occurrence of the answer
                    answers_of_questions[question_text][answer] += 1
                    for correct_answer in correct_answers:
                        if question_text in all_std_answers and answer == correct_answer:
                            poll_result_df.at[student_index, question_text] = 1
                            num_of_correct_ans += 1
                            student_poll_df.at[ans_key_index + 1, "Correct"] = 1
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

            poll_result_df.at[student_index, "Correct Answers"] = num_of_correct_ans
            poll_result_df.at[student_index, "Wrong Answers"] = num_of_answered_questions - num_of_correct_ans
            poll_result_df.at[student_index, "Empty Answers"] = num_of_questions - num_of_answered_questions
            poll_result_df.at[student_index, "Success"] = num_of_correct_ans / num_of_questions
            poll_result_df.at[student_index, "Success (%)"] = 100 * num_of_correct_ans / num_of_questions

            # student_poll_df.to_excel(configuration.student_poll_results_dir_path + "/"
            #                          + "Poll_" + self.poll_id + "_" + self.name.replace(" ", "_") + "_"
            #                          + self.date.replace(" ", "_").replace("-", "_").replace(":", "_") + "_"
            #                          + student.name.replace(" ", "_") + "_" + str(student.student_id) + ".xlsx")

    def print_absences_and_anomalies(self, students, dir_path):
        if not os.path.exists(dir_path):
            os.makedirs(dir_path)
        for student in students.values():
            if student not in self.student_answers.keys():
                self.absents.append(student)

        anomalies_json = json.dumps([anomaly.to_dict() for anomaly in self.anomalies], indent=4, ensure_ascii=False)
        absents_json = json.dumps([student.to_dict() for student in self.absents], indent=4, ensure_ascii=False)
        f = open(dir_path + "/" + "Poll_" + self.poll_id + "_" + self.name.replace(" ", "_") + "_"
                 + self.date.replace(" ", "_").replace("-", "_").replace(":", "_") + ".json", "w",encoding="utf8")

        data = {"Zoom poll report name": self.name,
                "Students in BYS list but don't exist in this poll report (Absence)": json.loads(absents_json),
                "Students in this poll report but don't exist in BYS Student List (Anomalies)":
                    json.loads(anomalies_json)}

        f.write(json.dumps(data, indent=4, ensure_ascii=False))
        f.close()
