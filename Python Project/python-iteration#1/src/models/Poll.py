class Poll:
    def __init__(self, name, answer_key, student_answers):
        self.name = name
        self.answer_key = answer_key
        self.student_answers = student_answers

    def save_student_answer(self, student, question, answer):
        self.student_answers[student][question] = answer
