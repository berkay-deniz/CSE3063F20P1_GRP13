from difflib import SequenceMatcher


def similar(a, b):
    return SequenceMatcher(None, a, b).ratio()


class Student:
    def __init__(self, id, name, email):
        self.id = id
        self.name = name
        self.email = email

    def match(self, student_name):
        if similar(self.name, student_name) > 0.70:
            return True
        elif similar(self.name, student_name) > 0.55:
            full_name = self.name.split()
            last_name = full_name[-1]
            student_full_name = student_name.split()
            student_last_name = student_full_name[-1]
            if student_last_name == last_name:
                for name_element in full_name:
                    if similar(name_element == student_full_name[0]) > 0.75:
                        return True
                return False
        else:
            return False



