class Poll:

    def __init__(self, name, answer_key):
        self.name = name
        self.answer_key = answer_key
        self.student_answers = {}
        self.date = None

    def save_student_answer(self, student, question, answer):
        if student not in self.student_answers:
            self.student_answers[student] = {}
        self.student_answers[student][question] = answer