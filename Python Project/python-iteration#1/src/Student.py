from difflib import SequenceMatcher


def similar(a, b):
    return SequenceMatcher(None, a, b).ratio()


class Student:
    def __init__(self, id, name, email):
        self.id = id
        self.name = name
        self.email = email

    def match(self, student_name):
        student_fullname = student_name.split()
        number_array = '0123456789'
        if student_fullname[0][0] in number_array:
            student_fullname = student_fullname[1:]
            student_name = ''
            for word in student_fullname:
                student_name += word
                student_name += ' '
        if similar(self.name.lower(), student_name.lower()) > 0.70:
            return True
        elif similar(self.name.lower(), student_name.lower()) > 0.55:
            full_name = self.name.split()
            last_name = full_name[-1]
            student_full_name = student_name.split()
            student_last_name = student_full_name[-1]
            if student_last_name.lower() == last_name.lower():
                for name_element in full_name:
                    if similar(name_element.lower(),student_full_name[0].lower()) > 0.75:
                        return True
            return False
        else:
            return False







