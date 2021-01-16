class Poll:
    student_answers = None

    def __init__(self, name, answer_key):
        self.name = name
        self.answer_key = answer_key

    def save_student_answer(self, student, question, answer):
        self.student_answers[student][question] = answer
