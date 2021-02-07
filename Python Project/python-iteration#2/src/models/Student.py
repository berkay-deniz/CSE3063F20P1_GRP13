from difflib import SequenceMatcher


def similar(a, b):
    return SequenceMatcher(None, a, b).ratio()


class Student:

    def __init__(self, student_id, name, email):
        self.student_id = student_id
        self.name = name
        self.email = email
        self.attendance = 0

    def to_dict(self):
        return {"student_no": self.student_id, "student_email": self.email, "student_name": self.name}

    def match(self, student_name, configuration):
        translation_table1 = str.maketrans("ğĞıİöÖüÜşŞçÇ", "gGiIoOuUsScC")
        translation_table2 = str.maketrans("IİÖÜ", "ıiöü")
        numbers = "0123456789"
        temp = student_name
        student_name = ""
        for char in temp:
            if char not in numbers:
                student_name += char
        if (similar(self.name.lower(), student_name.lower()) > configuration.exact_match_threshold) or \
                (similar(self.name.translate(translation_table1).lower(),
                         student_name.lower()) > configuration.exact_match_threshold) or \
                (similar(self.name.translate(translation_table2).lower(),
                         student_name.lower()) > configuration.exact_match_threshold):
            return True
        else:
            full_name = self.name.split()
            last_name = full_name[-1]
            student_full_name = student_name.split()
            student_last_name = student_full_name[-1]
            if (student_last_name.lower() == last_name.lower()) or \
                    (student_last_name.lower() == last_name.translate(translation_table2).lower()) or \
                    (student_last_name.lower() == last_name.translate(translation_table1).lower()):
                for name_element in full_name:
                    if (similar(name_element.lower(),
                                student_full_name[0].lower()) > configuration.partial_match_threshold) or \
                            (similar(name_element.translate(translation_table1).lower(),
                                     student_full_name[0].lower()) > configuration.partial_match_threshold) or \
                            (similar(name_element.translate(translation_table2).lower(),
                                     student_full_name[0].lower()) > configuration.partial_match_threshold):
                        return True
            return False

    def calculate_similarity(self, student_name):
        numbers = "0123456789"
        temp = student_name
        student_name = ""
        for char in temp:
            if char not in numbers:
                student_name += char
        return similar(self.name.lower(), student_name.lower())
